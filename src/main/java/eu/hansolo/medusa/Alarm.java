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


import javafx.scene.paint.Color;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


/**
 * Created by hansolo on 28.01.16.
 */
public class Alarm {
    public static enum Repetition {
        ONCE,
        HOURLY,
        DAILY,
        WEEKLY
    }
    public static final boolean ARMED   = true;
    public static final boolean UNARMED = false;

    private Repetition    repetition;
    private ZonedDateTime time;
    private boolean       armed;
    private String        text;
    private Command       command;
    private Color         color;


    // ******************** Constructors **************************************
    public Alarm() {
        this(Repetition.ONCE, ZonedDateTime.now().plus(5, ChronoUnit.MINUTES));
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME) {
        this(REPETITION, TIME, true);
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final boolean ARMED) {
        this(REPETITION, TIME, ARMED, "");
    }
    public Alarm(final Repetition REPETITION, final ZonedDateTime TIME, final boolean ARMED, final String TEXT) {
        this(REPETITION, TIME, ARMED, TEXT, null);
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
}
