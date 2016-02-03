/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.paint.Color;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


/**
 * Created by hansolo on 28.01.16.
 */
public class Alarm {
    public enum Repetition {
        ONCE,
        HOURLY,
        DAILY,
        WEEKLY
    }
    public static final boolean          ARMED   = true;
    public static final boolean          UNARMED = false;

    public        final AlarmMarkerEvent ALARM_MARKER_PRESSED_EVENT  = new AlarmMarkerEvent(Alarm.this, null, AlarmMarkerEvent.ALARM_MARKER_PRESSED);
    public        final AlarmMarkerEvent ALARM_MARKER_RELEASED_EVENT = new AlarmMarkerEvent(Alarm.this, null, AlarmMarkerEvent.ALARM_MARKER_RELEASED);
    
    private             Repetition       repetition;
    private             ZonedDateTime    time;
    private             boolean          armed;
    private             String           text;
    private             Command          command;
    private             Color            color;


    // ******************** Constructors **************************************
    public Alarm() {
        this(Repetition.ONCE, ZonedDateTime.now().plus(5, ChronoUnit.MINUTES), true, "", null, Clock.DARK_COLOR);
    }
    public Alarm(final ZonedDateTime TIME) {
        this(Repetition.ONCE, TIME, true, "", null, Clock.DARK_COLOR);
    }
    public Alarm(final ZonedDateTime TIME, final Color COLOR) {
        this(Repetition.ONCE, TIME, true, "", null, COLOR);
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME) {
        this(REPETITION, TIME, true, "", null, Clock.DARK_COLOR);
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final Color COLOR) {
        this(REPETITION, TIME, true, "", null, COLOR);
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final boolean ARMED) {
        this(REPETITION, TIME, ARMED, "");
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final boolean ARMED, final String TEXT) {
        this(REPETITION, TIME, ARMED, TEXT, null, Clock.DARK_COLOR);
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final boolean ARMED, final String TEXT, final Command COMMAND) {
        this(REPETITION, TIME, ARMED, TEXT, COMMAND, Clock.DARK_COLOR);
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final boolean ARMED, final String TEXT, final Command COMMAND, final Color COLOR) {
        repetition = REPETITION;
        time       = TIME;
        armed      = ARMED;
        text       = TEXT;
        command    = COMMAND;
        color      = COLOR;
    }


    // ******************** Methods *******************************************
    public Repetition getRepetition() { return repetition; }
    public void setRepetition(final Repetition REPETITION) { repetition = REPETITION; }

    public ZonedDateTime getTime() { return time; }
    public void setTime(final ZonedDateTime TIME) { time = TIME; }

    public boolean isArmed() { return armed; }
    public void setArmed(final boolean ARMED) { armed = ARMED; }

    public String getText() { return text; }
    public void setText(final String TEXT) { text = TEXT; }

    public Command getCommand() { return command; }
    public void setCommand(final Command COMMAND) { command = COMMAND; }
    public void executeCommand() { if (null != command) command.execute(); }

    public Color getColor() { return color; }
    public void setColor(final Color COLOR) { color = COLOR; }

    @Override public String toString() {
        return new StringBuilder()
            .append("{\n")
            .append("\"reptition\":\"").append(repetition.name()).append("\",\n")
            .append("\"time\":\"").append(time).append("\",\n")
            .append("\"armed\":").append(armed).append(",\n")
            .append("\"color\":\"").append(getColor().toString().substring(0,8).replace("0x", "#")).append("\",\n")
            .append("\"text\":\"").append(text).append("\"\n")
            .append("}")
            .toString();
    }

    
    // ******************** Event Handling ************************************
    public final ObjectProperty<EventHandler<AlarmMarkerEvent>> onMarkerPressedProperty() { return onMarkerPressed; }
    public final void setOnMarkerPressed(EventHandler<AlarmMarkerEvent> value) { onMarkerPressedProperty().set(value); }
    public final EventHandler<AlarmMarkerEvent> getOnMarkerPressed() { return onMarkerPressedProperty().get(); }
    private ObjectProperty<EventHandler<AlarmMarkerEvent>> onMarkerPressed = new SimpleObjectProperty<>(Alarm.this, "onMarkerPressed");

    public final ObjectProperty<EventHandler<AlarmMarkerEvent>> onMarkerReleasedProperty() { return onMarkerReleased; }
    public final void setOnMarkerReleased(EventHandler<AlarmMarkerEvent> value) { onMarkerReleasedProperty().set(value); }
    public final EventHandler<AlarmMarkerEvent> getOnMarkerReleased() { return onMarkerReleasedProperty().get(); }
    private ObjectProperty<EventHandler<AlarmMarkerEvent>> onMarkerReleased = new SimpleObjectProperty<>(Alarm.this, "onMarkerReleased");

    public void fireAlarmMarkerEvent(final AlarmMarkerEvent EVENT) {
        final EventHandler<AlarmMarkerEvent> HANDLER;
        final EventType                 TYPE = EVENT.getEventType();

        if (AlarmMarkerEvent.ALARM_MARKER_PRESSED == TYPE) {
            HANDLER = getOnMarkerPressed();
        } else if (AlarmMarkerEvent.ALARM_MARKER_RELEASED == TYPE) {
            HANDLER = getOnMarkerReleased();
        } else {
            HANDLER = null;
        }
        if (null == HANDLER) return;
        Platform.runLater(() -> HANDLER.handle(EVENT));
    }


    // ******************** Inner Classes *************************************
    public static class AlarmMarkerEvent extends Event {;
        public static final EventType<AlarmMarkerEvent> ALARM_MARKER_PRESSED  = new EventType(ANY, "ALARM_MARKER_PRESSED");
        public static final EventType<AlarmMarkerEvent> ALARM_MARKER_RELEASED = new EventType(ANY, "ALARM_MARKER_RELEASED");


        // ******************** Constructors **************************************
        public AlarmMarkerEvent(final Object SOURCE, final EventTarget TARGET, EventType<AlarmMarkerEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}
