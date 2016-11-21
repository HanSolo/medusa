/*
 * Copyright (c) 2015 by Gerrit Grunwald
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.paint.Color;


/**
 * Created by hansolo on 11.12.15.
 */
public class Marker implements Comparable<Marker>{
    public enum MarkerType { STANDARD, DOT, TRIANGLE }
    private static final Color DEFAULT_MARKER_COLOR  = Color.rgb(123, 30, 202);
    public final MarkerEvent   MARKER_PRESSED_EVENT  = new MarkerEvent(Marker.this, null, MarkerEvent.MARKER_PRESSED);
    public final MarkerEvent   MARKER_RELEASED_EVENT = new MarkerEvent(Marker.this, null, MarkerEvent.MARKER_RELEASED);
    public final MarkerEvent   VALUE_CHANGED_EVENT   = new MarkerEvent(Marker.this, null, MarkerEvent.VALUE_CHANGED);
    public final MarkerEvent   COLOR_CHANGED_EVENT   = new MarkerEvent(Marker.this, null, MarkerEvent.COLOR_CHANGED);
    public final MarkerEvent   TEXT_CHANGED_EVENT    = new MarkerEvent(Marker.this, null, MarkerEvent.TEXT_CHANGED);
    public final MarkerEvent   TYPE_CHANGED_EVENT    = new MarkerEvent(Marker.this, null, MarkerEvent.TYPE_CHANGED);
    public final MarkerEvent   EXCEEDED_EVENT        = new MarkerEvent(Marker.this, null, MarkerEvent.MARKER_EXCEEDED);
    public final MarkerEvent   UNDERRUN_EVENT        = new MarkerEvent(Marker.this, null, MarkerEvent.MARKER_UNDERRUN);
    private double                     _value;
    private DoubleProperty             value;
    private String                     _text;
    private StringProperty             text;
    private Color                      _color;
    private ObjectProperty<Color>      color;
    private MarkerType                 _markerType;
    private ObjectProperty<MarkerType> markerType;
    private double                     checkedValue;
    private String                     styleClass;


    // ******************** Constructors **************************************
    /**
     *
     */
    public Marker() {
        this(0, "", DEFAULT_MARKER_COLOR, MarkerType.STANDARD, "");
    }
    public Marker(final double VALUE, final String TEXT) {
        this(VALUE, TEXT, DEFAULT_MARKER_COLOR, MarkerType.STANDARD, "");
    }
    public Marker(final double VALUE, final Color COLOR) {
        this(VALUE, "", COLOR, MarkerType.STANDARD, "");
    }
    public Marker(final double VALUE, final MarkerType TYPE) {
        this(VALUE, "", DEFAULT_MARKER_COLOR, TYPE, "");
    }
    public Marker(final double VALUE, final Color COLOR, final MarkerType TYPE) {
        this(VALUE, "", COLOR, TYPE, "");
    }
    public Marker(final double VALUE, final String TEXT, final MarkerType TYPE) {
        this(VALUE, TEXT, DEFAULT_MARKER_COLOR, TYPE, "");
    }
    public Marker(final double VALUE, final String TEXT, final Color COLOR) {
        this(VALUE, TEXT, COLOR, MarkerType.STANDARD, "");
    }
    public Marker(final double VALUE, final String TEXT, final Color COLOR, final MarkerType TYPE) {
        this(VALUE, TEXT, COLOR, TYPE, "");
    }
    public Marker(final double VALUE, final String TEXT, final Color COLOR, final MarkerType TYPE, final String STYLE_CLASS) {
        _value       = VALUE;
        _text        = TEXT;
        _color       = COLOR;
        _markerType  = null == TYPE ? MarkerType.STANDARD : TYPE;
        checkedValue = -Double.MAX_VALUE;
        styleClass   = STYLE_CLASS;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the value that was defined for the marker.
     * @return the value that was defined for the marker
     */
    public double getValue() { return null == value ? _value : value.get(); }
    /**
     * Defines the value for the marker
     * @param VALUE
     */
    public void setValue(final double VALUE) {
        if (null == value) {
            _value = VALUE;
        } else {
            value.set(VALUE);
        }
        fireMarkerEvent(VALUE_CHANGED_EVENT);
    }
    public DoubleProperty valueProperty() {
        if (null == value) { value = new SimpleDoubleProperty(this, "value", _value); }
        return value;
    }

    /**
     * Returns the text that was defined for the marker.
     * This text can be used as a description and will be
     * used in tooltips.
     * @return the text that was defined for the marker
     */
    public String getText() { return null == text ? _text : text.get(); }
    /**
     * Defines a text for this marker. This text can be
     * used as a description and will be used in tooltips.
     * @param TEXT
     */
    public void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
        } else {
            text.set(TEXT);
        }
        fireMarkerEvent(TEXT_CHANGED_EVENT);
    }
    public StringProperty textProperty() {
        if (null == text) { text = new SimpleStringProperty(this, "text", _text); }
        return text;
    }

    /**
     * Returns the color that will be used to colorize
     * the marker.
     * @return the color that will be used to colorize the marker
     */
    public Color getColor() { return null == color ? _color : color.get(); }
    /**
     * Defines the color that will be used to colorize the marker.
     * @param COLOR
     */
    public void setColor(final Color COLOR) {
        if (null == color) {
            _color = COLOR;
        } else {
            color.set(COLOR);
        }
        fireMarkerEvent(COLOR_CHANGED_EVENT);
    }
    public ObjectProperty<Color> colorProperty() {
        if (null == color) { color = new SimpleObjectProperty<>(Marker.this, "color", _color); }
        return color;
    }

    /**
     * Returns the shape that will be used to visualize the marker.
     * The values are STANDARD, DOT, TRAPEZOID.
     * @return the shape that will be used to visualize the marker
     */
    public MarkerType getMarkerType() { return null == markerType ? _markerType : markerType.get(); }
    /**
     * Defines the shape that will be used to visualize the marker.
     * The values are STANDARD, DOT, TRAPEZOID.
     * @param TYPE
     */
    public void setMarkerType(final MarkerType TYPE) {
        if (null == markerType) {
            _markerType = null == TYPE ? MarkerType.STANDARD : TYPE;
        } else {
            markerType.set(TYPE);
        }
        fireMarkerEvent(TYPE_CHANGED_EVENT);
    }
    public ObjectProperty<MarkerType> markerTypeProperty() {
        if (null == markerType) {
            markerType = new ObjectPropertyBase<MarkerType>(_markerType) {
                @Override public void set(final MarkerType TYPE) { super.set(null == TYPE ? MarkerType.STANDARD : TYPE); }
                @Override public Object getBean() { return Marker.this; }
                @Override public String getName() { return "markerType"; }
            };
        }
        return markerType;
    }

    /**
     * Returns the style class that can be used to colorize the marker.
     * This is not implemented in the current available skins.
     * @return the style class that can be used to colorize the marker
     */
    public String getStyleClass() { return styleClass; }
    /**
     * Defines the style class that can be used to colorize the marker.
     * This is not implemented in the current available marker.
     * @param STYLE_CLASS
     */
    public void setStyleClass(final String STYLE_CLASS) { styleClass = STYLE_CLASS; }
    
    public boolean equals(final Marker MARKER) {
        return (Double.compare(MARKER.getValue(), getValue()) == 0 && MARKER.getText().equals(getText()));
    }

    /**
     * Checks if a given value is smaller/bigger than the stored
     * value. With those checks it can be detected if the current
     * value exceeds or underruns the marker. In both cases an
     * event will be fired.
     * @param VALUE
     */
    public void checkForValue(final double VALUE) {
        boolean wasSmaller = Double.compare(checkedValue, VALUE) < 0;
        boolean wasBigger  = Double.compare(checkedValue, VALUE) > 0;
        boolean isBigger   = Double.compare(VALUE, checkedValue) > 0;
        boolean isSmaller  = Double.compare(VALUE, checkedValue) < 0;

        if (wasSmaller && isBigger) {
            fireMarkerEvent(EXCEEDED_EVENT);
        } else if (wasBigger && isSmaller) {
            fireMarkerEvent(UNDERRUN_EVENT);
        }
        checkedValue = VALUE;
    }

    @Override public int compareTo(final Marker MARKER) {
        if (Double.compare(getValue(), MARKER.getValue()) < 0) return -1;
        if (Double.compare(getValue(), MARKER.getValue()) > 0) return 1;
        return 0;
    }

    @Override public String toString() {
        return new StringBuilder().append("{\n")
                                  .append("\"value\":").append(getValue()).append(",\n")
                                  .append("\"text\":\"").append(getText()).append("\",\n")
                                  .append("\"color\":\n").append(getColor().toString().substring(0, 8).replace("0x", "#")).append("\",\n")
                                  .append("\"type\":\"").append(getMarkerType().name()).append("\"\n")
                                  .append("}")
                                  .toString();
    }


    // ******************** Event handling ************************************
    public final ObjectProperty<EventHandler<MarkerEvent>> onMarkerExceededProperty() { return onMarkerExceeded; }
    public final void setOnMarkerExceeded(EventHandler<MarkerEvent> value) { onMarkerExceededProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnMarkerExceeded() { return onMarkerExceededProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onMarkerExceeded = new SimpleObjectProperty<>(Marker.this, "onMarkerExceeded");

    public final ObjectProperty<EventHandler<MarkerEvent>> onMarkerUnderrunProperty() { return onMarkerUnderrun; }
    public final void setOnMarkerUnderrun(EventHandler<MarkerEvent> value) { onMarkerUnderrunProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnMarkerUnderrun() { return onMarkerUnderrunProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onMarkerUnderrun = new SimpleObjectProperty<>(Marker.this, "onMarkerUnderrun");

    public final ObjectProperty<EventHandler<MarkerEvent>> onValueChangedProperty() { return onValueChanged; }
    public final void setOnValueChanged(EventHandler<MarkerEvent> value) { onValueChangedProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnValueChanged() { return onValueChangedProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onValueChanged = new SimpleObjectProperty<>(Marker.this, "onValueChanged");

    public final ObjectProperty<EventHandler<MarkerEvent>> onColorChangedProperty() { return onColorChanged; }
    public final void setOnColorChanged(EventHandler<MarkerEvent> value) { onColorChangedProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnColorChanged() { return onColorChangedProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onColorChanged = new SimpleObjectProperty<>(Marker.this, "onColorChanged");

    public final ObjectProperty<EventHandler<MarkerEvent>> onTextChangedProperty() { return onTextChanged; }
    public final void setOnTextChanged(EventHandler<MarkerEvent> value) { onTextChangedProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnTextChanged() { return onTextChangedProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onTextChanged = new SimpleObjectProperty<>(Marker.this, "onTextChanged");

    public final ObjectProperty<EventHandler<MarkerEvent>> onTypeChangedProperty() { return onTypeChanged; }
    public final void setOnTypeChanged(EventHandler<MarkerEvent> value) { onTypeChangedProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnTypeChanged() { return onTypeChangedProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onTypeChanged = new SimpleObjectProperty<>(Marker.this, "onTypeChanged");
    
    public final ObjectProperty<EventHandler<MarkerEvent>> onMarkerPressedProperty() { return onMarkerPressed; }
    public final void setOnMarkerPressed(EventHandler<MarkerEvent> value) { onMarkerPressedProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnMarkerPressed() { return onMarkerPressedProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onMarkerPressed = new SimpleObjectProperty<>(Marker.this, "onMarkerPressed");

    public final ObjectProperty<EventHandler<MarkerEvent>> onMarkerReleasedProperty() { return onMarkerReleased; }
    public final void setOnMarkerReleased(EventHandler<MarkerEvent> value) { onMarkerReleasedProperty().set(value); }
    public final EventHandler<MarkerEvent> getOnMarkerReleased() { return onMarkerReleasedProperty().get(); }
    private ObjectProperty<EventHandler<MarkerEvent>> onMarkerReleased = new SimpleObjectProperty<>(Marker.this, "onMarkerReleased");

    public void fireMarkerEvent(final MarkerEvent EVENT) {
        final EventHandler<MarkerEvent> HANDLER;
        final EventType                 TYPE = EVENT.getEventType();

        if (MarkerEvent.MARKER_PRESSED == TYPE) {
            HANDLER = getOnMarkerPressed();
        } else if (MarkerEvent.MARKER_RELEASED == TYPE) {
            HANDLER = getOnMarkerReleased();
        } else if (MarkerEvent.VALUE_CHANGED == TYPE) {
            HANDLER = getOnValueChanged();
        } else if (MarkerEvent.COLOR_CHANGED == TYPE) {
            HANDLER = getOnColorChanged();
        } else if (MarkerEvent.TEXT_CHANGED == TYPE) {
            HANDLER = getOnTextChanged();
        } else if (MarkerEvent.TYPE_CHANGED == TYPE) {
            HANDLER = getOnTypeChanged();
        } else if (MarkerEvent.MARKER_EXCEEDED == TYPE) {
            HANDLER = getOnMarkerExceeded();
        } else if (MarkerEvent.MARKER_UNDERRUN == TYPE) {
            HANDLER = getOnMarkerUnderrun();
        } else {
            HANDLER = null;
        }
        if (null == HANDLER) return;
        Platform.runLater(() -> HANDLER.handle(EVENT));
    }


    // ******************** Inner Classes *************************************
    public static class MarkerEvent extends Event {
        public static final EventType<MarkerEvent> MARKER_EXCEEDED = new EventType(ANY, "MARKER_EXCEEDED");
        public static final EventType<MarkerEvent> MARKER_UNDERRUN = new EventType(ANY, "MARKER_UNDER_RUN");
        public static final EventType<MarkerEvent> MARKER_PRESSED  = new EventType(ANY, "MARKER_PRESSED");
        public static final EventType<MarkerEvent> MARKER_RELEASED = new EventType(ANY, "MARKER_RELEASED");
        public static final EventType<MarkerEvent> VALUE_CHANGED   = new EventType(ANY, "VALUE_CHANGED");
        public static final EventType<MarkerEvent> COLOR_CHANGED   = new EventType(ANY, "COLOR_CHANGED");
        public static final EventType<MarkerEvent> TEXT_CHANGED    = new EventType(ANY, "TEXT_CHANGED");
        public static final EventType<MarkerEvent> TYPE_CHANGED    = new EventType(ANY, "TYPE_CHANGED");


        // ******************** Constructors **************************************
        public MarkerEvent(final Object SOURCE, final EventTarget TARGET, EventType<MarkerEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}

