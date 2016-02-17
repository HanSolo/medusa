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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


/**
 * Created by hansolo on 11.12.15.
 */
public class Section implements Comparable<Section> {
    public final SectionEvent ENTERED_EVENT = new SectionEvent(this, null, SectionEvent.SECTION_ENTERED);
    public final SectionEvent LEFT_EVENT    = new SectionEvent(this, null, SectionEvent.SECTION_LEFT);
    private double                _start;
    private DoubleProperty        start;
    private double                _stop;
    private DoubleProperty        stop;
    private String                _text;
    private StringProperty        text;
    private Image                 _icon;
    private ObjectProperty<Image> icon;
    private Color                 _color;
    private ObjectProperty<Color> color;
    private Color                 _highlightColor;
    private ObjectProperty<Color> highlightColor;
    private Color                 _textColor;
    private ObjectProperty<Color> textColor;
    private double                checkedValue;


    // ******************** Constructors **************************************
    /**
     * Represents an area of a given range, defined by a start and stop value.
     * This class is used for regions and areas in many gauges. It is possible
     * to check a value against the defined range and fire events in case the
     * value enters or leaves the defined region.
     */
    public Section() {
        this(-1, -1, "", null, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
    }
    public Section(final double START, final double STOP) {
        this(START, STOP, "", null, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT);
    }
    public Section(final double START, final double STOP, final Color COLOR) {
        this(START, STOP, "", null, COLOR, COLOR, Color.TRANSPARENT);
    }
    public Section(final double START, final double STOP, final Color COLOR, final Color HIGHLIGHT_COLOR) {
        this(START, STOP, "", null, COLOR, HIGHLIGHT_COLOR, Color.TRANSPARENT);
    }
    public Section(final double START, final double STOP, final Image ICON, final Color COLOR) {
        this(START, STOP, "", ICON, COLOR, COLOR, Color.WHITE);
    }
    public Section(final double START, final double STOP, final String TEXT, final Color COLOR) {
        this(START, STOP, TEXT, null, COLOR, COLOR, Color.WHITE);
    }
    public Section(final double START, final double STOP, final String TEXT, final Color COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, null, COLOR, COLOR, TEXT_COLOR);
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON, final Color COLOR, final Color TEXT_COLOR) {
        this(START, STOP, TEXT, ICON, COLOR, COLOR, TEXT_COLOR);
    }
    public Section(final double START, final double STOP, final String TEXT, final Image ICON, final Color COLOR, final Color HIGHLIGHT_COLOR, final Color TEXT_COLOR) {
        _start          = START;
        _stop           = STOP;
        _text           = TEXT;
        _icon           = ICON;
        _color          = COLOR;
        _highlightColor = HIGHLIGHT_COLOR;
        _textColor      = TEXT_COLOR;
        checkedValue    = -Double.MAX_VALUE;
    }


    // ******************** Methods *******************************************
    /**
     * Returns the value where the section begins.
     * @return the value where the section begins
     */
    public double getStart() { return null == start ? _start : start.get(); }
    /**
     * Defines the value where the section begins.
     * @param START
     */
    public void setStart(final double START) {
        if (null == start) {
            _start = START;
        } else {
            start.set(START);
        }
    }
    public DoubleProperty startProperty() {
        if (null == start) { start = new SimpleDoubleProperty(Section.this, "start", _start); }
        return start;
    }

    /**
     * Returns the value where the section ends.
     * @return the value where the section ends
     */
    public double getStop() { return null == stop ? _stop : stop.get(); }
    /**
     * Defines the value where the section ends.
     * @param STOP
     */
    public void setStop(final double STOP) {
        if (null == stop) {
            _stop = STOP;
        } else {
            stop.set(STOP);
        }
    }
    public DoubleProperty stopProperty() {
        if (null == stop) { stop = new SimpleDoubleProperty(Section.this, "stop", _stop); }
        return stop;
    }

    /**
     * Returns the text that was set for the section.
     * @return the text that was set for the section
     */
    public String getText() { return null == text ? _text : text.get(); }
    /**
     * Defines a text for the section.
     * @param TEXT
     */
    public void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
        } else {
            text.set(TEXT);
        }
    }
    public StringProperty textProperty() {
        if (null == text) { text = new SimpleStringProperty(Section.this, "text", _text); }
        return text;
    }

    /**
     * Returns the image that was defined for the section.
     * In some skins the image will be drawn (e.g. SimpleSkin).
     * @return the image that was defined for the section
     */
    public Image getImage() { return null == icon ? _icon : icon.get(); }
    /**
     * Defines an image for the section.
     * In some skins the image will be drawn (e.g. SimpleSkin)
     * @param IMAGE
     */
    public void setIcon(final Image IMAGE) {
        if (null == icon) {
            _icon = IMAGE;
        } else {
            icon.set(IMAGE);
        }
    }
    public ObjectProperty<Image> iconProperty() {
        if (null == icon) { icon = new SimpleObjectProperty<>(this, "icon", _icon); }
        return icon;
    }

    /**
     * Returns the color that will be used to colorize the section in
     * a gauge.
     * @return the color that will be used to colorize the section
     */
    public Color getColor() { return null == color ? _color : color.get(); }
    /**
     * Defines the color that will be used to colorize the section in
     * a gauge.
     * @param COLOR
     */
    public void setColor(final Color COLOR) {
        if (null == color) {
            _color = COLOR;
        } else {
            color.set(COLOR);
        }
    }
    public ObjectProperty<Color> colorProperty() {
        if (null == color) { color = new SimpleObjectProperty<>(Section.this, "color", _color); }
        return color;
    }

    /**
     * Returns the color that will be used to colorize the section in
     * a gauge when it is highlighted.
     * @return the color that will be used to colorize a highlighted section
     */
    public Color getHighlightColor() { return null == highlightColor ? _highlightColor : highlightColor.get(); }
    /**
     * Defines the color that will be used to colorize a highlighted section
     * @param COLOR
     */
    public void setHighlightColor(final Color COLOR) {
        if (null == highlightColor) {
            _highlightColor = COLOR;
        } else {
            highlightColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> highlightColorProperty() {
        if (null == highlightColor) { highlightColor = new SimpleObjectProperty<>(Section.this, "highlightColor", _highlightColor); }
        return highlightColor;
    }
    
    /**
     * Returns the color that will be used to colorize the section text.
     * @return the color that will be used to colorize the section text
     */
    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    /**
     * Defines the color that will be used to colorize the section text.
     * @param COLOR
     */
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) { textColor = new SimpleObjectProperty<>(Section.this, "textColor", _textColor); }
        return textColor;
    }

    /**
     * Returns true if the given value is within the range between
     * section.getStart() and section.getStop()
     * @param VALUE
     * @return true if the given value is within the range of the section
     */
    public boolean contains(final double VALUE) {
        return (Double.compare(VALUE, getStart()) >= 0 && Double.compare(VALUE, getStop()) <= 0);
    }

    /**
     * Checks if the section contains the given value and fires an event
     * in case the value "entered" or "left" the section. With this one
     * can react if a value enters/leaves a specific region in a gauge.
     * @param VALUE
     */
    public void checkForValue(final double VALUE) {
        boolean wasInSection = contains(checkedValue);
        boolean isInSection  = contains(VALUE);
        if (!wasInSection && isInSection) {
            fireSectionEvent(ENTERED_EVENT);
        } else if (wasInSection && !isInSection) {
            fireSectionEvent(LEFT_EVENT);
        }
        checkedValue = VALUE;
    }

    public boolean equals(final Section SECTION) {
        return (Double.compare(SECTION.getStart(), getStart()) == 0 &&
                Double.compare(SECTION.getStop(), getStop()) == 0 &&
                SECTION.getText().equals(getText()));
    }

    @Override public int compareTo(final Section SECTION) {
        if (Double.compare(getStart(), SECTION.getStart()) < 0) return -1;
        if (Double.compare(getStart(), SECTION.getStart()) > 0) return 1;
        return 0;
    }

    @Override public String toString() {
        return new StringBuilder()
            .append("{\n")
            .append("\"text\":\"").append(getText()).append("\",\n")
            .append("\"startValue\":").append(getStart()).append(",\n")
            .append("\"stopValue\":").append(getStop()).append(",\n")
            .append("\"color\":\"").append(getColor().toString().substring(0,8).replace("0x", "#")).append("\",\n")
            .append("\"highlightColor\":\"").append(getHighlightColor().toString().substring(0,8).replace("0x", "#")).append("\",\n")
            .append("\"textColor\":\"").append(getTextColor().toString().substring(0,8).replace("0x", "#")).append("\"\n")
            .append("}")
            .toString();
    }


    // ******************** Event handling ************************************
    public final ObjectProperty<EventHandler<SectionEvent>> onSectionEnteredProperty() { return onSectionEntered; }
    public final void setOnSectionEntered(EventHandler<SectionEvent> value) { onSectionEnteredProperty().set(value); }
    public final EventHandler<SectionEvent> getOnSectionEntered() { return onSectionEnteredProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onSectionEntered = new SimpleObjectProperty<>(this, "onSectionEntered");

    public final ObjectProperty<EventHandler<SectionEvent>> onSectionLeftProperty() { return onSectionLeft; }
    public final void setOnSectionLeft(EventHandler<SectionEvent> value) { onSectionLeftProperty().set(value); }
    public final EventHandler<SectionEvent> getOnSectionLeft() { return onSectionLeftProperty().get(); }
    private ObjectProperty<EventHandler<SectionEvent>> onSectionLeft = new SimpleObjectProperty<>(this, "onSectionLeft");

    public void fireSectionEvent(final SectionEvent EVENT) {
        final EventHandler<SectionEvent> HANDLER;
        final EventType                  TYPE = EVENT.getEventType();
        if (SectionEvent.SECTION_ENTERED == TYPE) {
            HANDLER = getOnSectionEntered();
        } else if (SectionEvent.SECTION_LEFT == TYPE) {
            HANDLER = getOnSectionLeft();
        } else {
            HANDLER = null;
        }

        if (null == HANDLER) return;

        HANDLER.handle(EVENT);
    }


    // ******************** Inner Classes *************************************
    public static class SectionEvent extends Event {
        public static final EventType<SectionEvent> SECTION_ENTERED = new EventType(ANY, "SECTION_ENTERED");
        public static final EventType<SectionEvent> SECTION_LEFT    = new EventType(ANY, "SECTION_LEFT");


        // ******************** Constructors **************************************
        public SectionEvent(final Object SOURCE, final EventTarget TARGET, EventType<SectionEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}
