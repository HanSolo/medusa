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

import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.events.AlarmEventListener;
import eu.hansolo.medusa.events.TimeEventListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 28.01.16.
 */
public class ClockBuilder <B extends ClockBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected ClockBuilder() {}


    // ******************** Methods *******************************************
    public static final ClockBuilder create() {
        return new ClockBuilder();
    }

    public final B skinType(final ClockSkinType TYPE) {
        properties.put("skinType", new SimpleObjectProperty<>(TYPE));
        return (B) this;
    }

    public final B time(final ZonedDateTime TIME) {
        properties.put("time", new SimpleObjectProperty<>(TIME));
        return (B)this;
    }

    public final B title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B checkSectionsForValue(final boolean CHECK) {
        properties.put("checkSectionsForValue", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B checkAreasForValue(final boolean CHECK) {
        properties.put("checkAreasForValue", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B sections(final TimeSection... SECTIONS) {
        properties.put("sectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B sections(final List<TimeSection> SECTIONS) {
        properties.put("sectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B sectionsVisible(final boolean VISIBLE) {
        properties.put("sectionsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B highlightSections(final boolean HIGHLIGHT) {
        properties.put("highlightSections", new SimpleBooleanProperty(HIGHLIGHT));
        return (B)this;
    }

    public final B areas(final TimeSection... AREAS) {
        properties.put("areasArray", new SimpleObjectProperty<>(AREAS));
        return (B)this;
    }

    public final B areas(final List<TimeSection> AREAS) {
        properties.put("areasList", new SimpleObjectProperty<>(AREAS));
        return (B)this;
    }

    public final B areasVisible(final boolean VISIBLE) {
        properties.put("areasVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B highlightAreas(final boolean HIGHLIGHT) {
        properties.put("highlightAreas", new SimpleBooleanProperty(HIGHLIGHT));
        return (B)this;
    }

    public final B discreteSeconds(final boolean DISCRETE) {
        properties.put("discreteSeconds", new SimpleBooleanProperty(DISCRETE));
        return (B)this;
    }

    public final B discreteMinutes(final boolean DISCRETE) {
        properties.put("discreteMinutes", new SimpleBooleanProperty(DISCRETE));
        return (B)this;
    }

    public final B discreteHours(final boolean DISCRETE) {
        properties.put("discreteHours", new SimpleBooleanProperty(DISCRETE));
        return (B)this;
    }

    public final B secondsVisible(final boolean VISIBLE) {
        properties.put("secondsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B titleVisible(final boolean VISIBLE) {
        properties.put("titleVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B textVisible(final boolean VISIBLE) {
        properties.put("textVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B dateVisible(final boolean VISIBLE) {
        properties.put("dateVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B nightMode(final boolean MODE) {
        properties.put("nightMode", new SimpleBooleanProperty(MODE));
        return (B)this;
    }

    public final B autoNightMode(final boolean MODE) {
        properties.put("autoNightMode", new SimpleBooleanProperty(MODE));
        return (B)this;
    }

    public final B running(final boolean RUNNING) {
        properties.put("running", new SimpleBooleanProperty(RUNNING));
        return (B)this;
    }

    public final B backgroundPaint(final Paint PAINT) {
        properties.put("backgroundPaint", new SimpleObjectProperty<>(PAINT));
        return (B)this;
    }

    public final B borderPaint(final Paint PAINT) {
        properties.put("borderPaint", new SimpleObjectProperty<>(PAINT));
        return (B)this;
    }

    public final B borderWidth(final double WIDTH) {
        properties.put("borderWidth", new SimpleDoubleProperty(WIDTH));
        return (B)this;
    }

    public final B foregroundPaint(final Paint PAINT) {
        properties.put("foregroundPaint", new SimpleObjectProperty<>(PAINT));
        return (B)this;
    }

    public final B titleColor(final Color COLOR) {
        properties.put("titleColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B textColor(final Color COLOR) {
        properties.put("textColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B dateColor(final Color COLOR) {
        properties.put("dateColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B hourTickMarkColor(final Color COLOR) {
        properties.put("hourTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B minuteTickMarkColor(final Color COLOR) {
        properties.put("minuteTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B tickLabelColor(final Color COLOR) {
        properties.put("tickLabelColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B alarmColor(final Color COLOR) {
        properties.put("alarmColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B hourTickMarksVisible(final boolean VISIBLE) {
        properties.put("hourTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B minuteTickMarksVisible(final boolean VISIBLE) {
        properties.put("minuteTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B tickLabelsVisible(final boolean VISIBLE) {
        properties.put("tickLabelsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B hourColor(final Color COLOR) {
        properties.put("hourColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B minuteColor(final Color COLOR) {
        properties.put("minuteColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B secondColor(final Color COLOR) {
        properties.put("secondColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B knobColor(final Color COLOR) {
        properties.put("knobColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B lcdDesign(final LcdDesign DESIGN) {
        properties.put("lcdDesign", new SimpleObjectProperty<>(DESIGN));
        return (B)this;
    }

    public final B alarmsEnabled(final boolean ENABLED) {
        properties.put("alarmsEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B alarmsVisible(final boolean VISIBLE) {
        properties.put("alarmsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B alarms(final Alarm... ALARMS) {
        properties.put("alarmsArray", new SimpleObjectProperty<>(ALARMS));
        return (B)this;
    }

    public final B alarms(final List<Alarm> ALARMS) {
        properties.put("alarmsList", new SimpleObjectProperty<>(ALARMS));
        return (B)this;
    }

    public final B onAlarm(final AlarmEventListener LISTENER) {
        properties.put("onAlarm", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onTimeEvent(final TimeEventListener LISTENER) {
        properties.put("onTimeEvent", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B lcdCrystalEnabled(final boolean ENABLED) {
        properties.put("lcdCrystalEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B shadowsEnabled(final boolean ENABLED) {
        properties.put("shadowsEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B lcdFont(final LcdFont FONT) {
        properties.put("lcdFont", new SimpleObjectProperty<>(FONT));
        return (B)this;
    }

    public final B locale(final Locale LOCALE) {
        properties.put("locale", new SimpleObjectProperty<>(LOCALE));
        return (B)this;
    }

    public final B animated(final boolean ANIMATED) {
        properties.put("animated", new SimpleBooleanProperty(ANIMATED));
        return (B)this;
    }

    public final B animationDuration(final long DURATION) {
        properties.put("animationDuration", new SimpleLongProperty(DURATION));
        return (B)this;
    }

    public final B customFontEnabled(final boolean ENABLED) {
        properties.put("customFontEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B customFont(final Font FONT) {
        properties.put("customFont", new SimpleObjectProperty(FONT));
        return (B)this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B)this;
    }
    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B)this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B)this;
    }
    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B)this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B)this;
    }
    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B)this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B)this;
    }
    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B)this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B)this;
    }
    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B)this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B)this;
    }
    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B)this;
    }

    public final B padding(final Insets INSETS) {
        properties.put("padding", new SimpleObjectProperty<>(INSETS));
        return (B)this;
    }

    public final Clock build() {
        final Clock CONTROL;
        if (properties.containsKey("skinType")) {
            ClockSkinType skinType = ((ObjectProperty<ClockSkinType>) properties.get("skinType")).get();
            CONTROL = new Clock(skinType);

            switch(skinType) {
                case YOTA2:
                    CONTROL.setBackgroundPaint(Color.rgb(40, 42, 48));
                    CONTROL.setHourTickMarkColor(Color.rgb(255, 255, 255));
                    CONTROL.setMinuteTickMarkColor(Color.rgb(255, 255, 255, 0.5));
                    CONTROL.setHourColor(Color.WHITE);
                    CONTROL.setMinuteColor(Color.WHITE);
                    CONTROL.setKnobColor(Color.WHITE);
                    CONTROL.setTextColor(Color.rgb(255, 255, 255, 0.5));
                    CONTROL.setDateColor(Color.rgb(255, 255, 255));
                    break;
                case LCD:
                    CONTROL.setBorderPaint(Color.WHITE);
                    CONTROL.setForegroundPaint(Color.WHITE);
                    break;
                case PEAR:
                    CONTROL.setBackgroundPaint(Color.BLACK);
                    CONTROL.setHourColor(Color.WHITE);
                    CONTROL.setMinuteColor(Color.WHITE);
                    CONTROL.setSecondColor(Color.rgb(255, 165, 24));
                    CONTROL.setHourTickMarkColor(Color.WHITE);
                    CONTROL.setMinuteTickMarkColor(Color.rgb(115, 115, 115));
                    CONTROL.setTickLabelColor(Color.WHITE);
                    CONTROL.setDateColor(Color.WHITE);
                    CONTROL.setDateVisible(true);
                    CONTROL.setSecondsVisible(true);
                    CONTROL.setTextVisible(false);
                    CONTROL.setTitleVisible(false);
                    break;
                case PLAIN:
                    CONTROL.setBackgroundPaint(Color.rgb(29, 29, 29));
                    CONTROL.setHourColor(Color.rgb(190, 190, 190));
                    CONTROL.setMinuteColor(Color.rgb(190, 190, 190));
                    CONTROL.setSecondColor(Color.rgb(0, 244, 0));
                    CONTROL.setDateColor(Color.rgb(190, 190, 190));
                    CONTROL.setSecondsVisible(true);
                    CONTROL.setHourTickMarkColor(Color.rgb(240, 240, 240));
                    CONTROL.setMinuteTickMarkColor(Color.rgb(240, 240, 240));
                    break;
                case DB:
                    CONTROL.setDiscreteSeconds(false);
                    CONTROL.setDiscreteMinutes(true);
                    CONTROL.setSecondColor(Color.rgb(167, 0, 0));
                    CONTROL.setSecondsVisible(true);
                    break;
                case ROUND_LCD:
                    CONTROL.setTextVisible(true);
                    CONTROL.setDateVisible(true);
                    break;
                case FAT:
                    CONTROL.setDiscreteMinutes(true);
                    break;
                case SLIM:
                    CONTROL.setSecondsVisible(true);
                    CONTROL.setDateVisible(true);
                    CONTROL.setHourColor(Color.WHITE);
                    CONTROL.setMinuteColor(Color.rgb(0,191,255));
                    CONTROL.setSecondColor(Color.WHITE);
                    CONTROL.setDateColor(Color.WHITE);
                    break;
                case MINIMAL:
                    CONTROL.setBackgroundPaint(Color.rgb(255, 255, 255, 0.3));
                    CONTROL.setTextColor(Color.WHITE);
                    CONTROL.setMinuteColor(Color.rgb(59, 209, 255));
                    CONTROL.setSecondColor(Color.rgb(255, 255, 255, 0.8));
                    CONTROL.setSecondsVisible(true);
                    CONTROL.setDateVisible(true);
                    break;
                case DIGITAL:
                    CONTROL.setTextVisible(true);
                    CONTROL.setDateVisible(true);
                    CONTROL.setSecondsVisible(true);
                    break;
                case TEXT:
                    CONTROL.setTextVisible(true);
                    CONTROL.setDateVisible(true);
                    CONTROL.setSecondsVisible(true);
                    break;
                case DESIGN:
                    CONTROL.setDiscreteHours(false);
                    CONTROL.setDiscreteMinutes(false);
                    CONTROL.setDiscreteSeconds(false);
                    CONTROL.setTextVisible(false);
                    CONTROL.setDateVisible(false);
                    CONTROL.setSecondsVisible(false);
                    CONTROL.setHourColor(Color.RED);
                    CONTROL.setBackgroundPaint(Color.WHITE);
                    break;
                case INDUSTRIAL:
                    CONTROL.setBackgroundPaint(Color.web("#efefef"));
                    CONTROL.setHourColor(Color.web("#2a2a2a"));
                    CONTROL.setMinuteColor(Color.web("#2a2a2a"));
                    CONTROL.setSecondColor(Color.web("#d1222b"));
                    CONTROL.setHourTickMarkColor(Color.BLACK);
                    CONTROL.setMinuteTickMarkColor(Color.BLACK);
                    CONTROL.setTickLabelsVisible(false);
                    CONTROL.setTickLabelColor(Color.BLACK);
                    CONTROL.setDateColor(Color.BLACK);
                    CONTROL.setDateVisible(false);
                    CONTROL.setSecondsVisible(true);
                    CONTROL.setTextVisible(false);
                    CONTROL.setTextColor(Color.BLACK);
                    CONTROL.setTitleVisible(false);
                    CONTROL.setTitleColor(Color.BLACK);
                    CONTROL.setBorderPaint(Color.BLACK);
                    CONTROL.setBorderWidth(5);
                    break;
                case TILE:
                    CONTROL.setBackgroundPaint(Color.rgb(42,42,42));
                    CONTROL.setHourColor(Color.rgb(238, 238, 238));
                    CONTROL.setMinuteColor(Color.rgb(238, 238, 238));
                    CONTROL.setSecondColor(Color.rgb(238, 238, 238));
                    CONTROL.setKnobColor(Color.rgb(238, 238, 238));
                    CONTROL.setHourTickMarkColor(Color.rgb(238, 238, 238));
                    CONTROL.setMinuteTickMarkColor(Color.rgb(238, 238, 238));
                    CONTROL.setDateColor(Color.rgb(238, 238, 238));
                    CONTROL.setDateVisible(false);
                    CONTROL.setSecondsVisible(false);
                    CONTROL.setTextVisible(false);
                    CONTROL.setTextColor(Color.rgb(238, 238, 238));
                    CONTROL.setTitleVisible(true);
                    CONTROL.setTitleColor(Color.rgb(238, 238, 238));
                    break;
            }
        } else {
            CONTROL = new Clock();
        }

        // Make sure that alarms, sections, areas and markers will be added first
        if (properties.keySet().contains("alarmsArray")) {
            CONTROL.setAlarms(((ObjectProperty<Alarm[]>) properties.get("alarmsArray")).get());
        }
        if(properties.keySet().contains("alarmsList")) {
            CONTROL.setAlarms(((ObjectProperty<List<Alarm>>) properties.get("alarmsList")).get());
        }

        if (properties.keySet().contains("sectionsArray")) {
            CONTROL.setSections(((ObjectProperty<TimeSection[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            CONTROL.setSections(((ObjectProperty<List<TimeSection>>) properties.get("sectionsList")).get());
        }

        if (properties.keySet().contains("areasArray")) {
            CONTROL.setAreas(((ObjectProperty<TimeSection[]>) properties.get("areasArray")).get());
        }
        if(properties.keySet().contains("areasList")) {
            CONTROL.setAreas(((ObjectProperty<List<TimeSection>>) properties.get("areasList")).get());
        }

        for (String key : properties.keySet()) {
            if ("prefSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setPrefSize(dim.getWidth(), dim.getHeight());
            } else if("minSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMinSize(dim.getWidth(), dim.getHeight());
            } else if("maxSize".equals(key)) {
                Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                CONTROL.setMaxSize(dim.getWidth(), dim.getHeight());
            } else if("prefWidth".equals(key)) {
                CONTROL.setPrefWidth(((DoubleProperty) properties.get(key)).get());
            } else if("prefHeight".equals(key)) {
                CONTROL.setPrefHeight(((DoubleProperty) properties.get(key)).get());
            } else if("minWidth".equals(key)) {
                CONTROL.setMinWidth(((DoubleProperty) properties.get(key)).get());
            } else if("minHeight".equals(key)) {
                CONTROL.setMinHeight(((DoubleProperty) properties.get(key)).get());
            } else if("maxWidth".equals(key)) {
                CONTROL.setMaxWidth(((DoubleProperty) properties.get(key)).get());
            } else if("maxHeight".equals(key)) {
                CONTROL.setMaxHeight(((DoubleProperty) properties.get(key)).get());
            } else if("scaleX".equals(key)) {
                CONTROL.setScaleX(((DoubleProperty) properties.get(key)).get());
            } else if("scaleY".equals(key)) {
                CONTROL.setScaleY(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutX".equals(key)) {
                CONTROL.setLayoutX(((DoubleProperty) properties.get(key)).get());
            } else if ("layoutY".equals(key)) {
                CONTROL.setLayoutY(((DoubleProperty) properties.get(key)).get());
            } else if ("translateX".equals(key)) {
                CONTROL.setTranslateX(((DoubleProperty) properties.get(key)).get());
            } else if ("translateY".equals(key)) {
                CONTROL.setTranslateY(((DoubleProperty) properties.get(key)).get());
            } else if ("padding".equals(key)) {
                CONTROL.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
            } else if ("styleClass".equals(key)) {
                CONTROL.getStyleClass().setAll("gauge");
                CONTROL.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
            } else if ("time".equals(key)) {
                CONTROL.setTime(((ObjectProperty<ZonedDateTime>) properties.get(key)).get());
            } else if ("title".equals(key)) {
                CONTROL.setTitle(((StringProperty) properties.get(key)).get());
            } else if ("text".equals(key)) {
                CONTROL.setText(((StringProperty) properties.get(key)).get());
            } else if ("checkSectionsForValue".equals(key)) {
                CONTROL.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
            } else if ("checkAreasForValue".equals(key)) {
                CONTROL.setCheckAreasForValue(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsVisible".equals(key)) {
                CONTROL.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("highlightSections".equals(key)) {
                CONTROL.setHighlightSections(((BooleanProperty) properties.get(key)).get());
            } else if ("areasVisible".equals(key)) {
                CONTROL.setAreasVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("highlightAreas".equals(key)) {
                CONTROL.setHighlightAreas(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteSeconds".equals(key)) {
                CONTROL.setDiscreteSeconds(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteMinutes".equals(key)) {
                CONTROL.setDiscreteMinutes(((BooleanProperty) properties.get(key)).get());
            } else if ("discreteHours".equals(key)) {
                CONTROL.setDiscreteHours(((BooleanProperty) properties.get(key)).get());
            } else if ("secondsVisible".equals(key)) {
                CONTROL.setSecondsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("titleVisible".equals(key)) {
                CONTROL.setTitleVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("textVisible".equals(key)) {
                CONTROL.setTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("dateVisible".equals(key)) {
                CONTROL.setDateVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("nightMode".equals(key)) {
                CONTROL.setNightMode(((BooleanProperty) properties.get(key)).get());
            } else if ("autoNightMode".equals(key)) {
                CONTROL.setAutoNightMode(((BooleanProperty) properties.get(key)).get());
            } else if ("backgroundPaint".equals(key)) {
                CONTROL.setBackgroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
            } else if ("borderPaint".equals(key)) {
                CONTROL.setBorderPaint(((ObjectProperty<Paint>) properties.get(key)).get());
            } else if ("borderWidth".equals(key)) {
                CONTROL.setBorderWidth(((DoubleProperty) properties.get(key)).get());
            } else if ("foregroundPaint".equals(key)) {
                CONTROL.setForegroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
            } else if ("titleColor".equals(key)) {
                CONTROL.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("textColor".equals(key)) {
                CONTROL.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("dateColor".equals(key)) {
                CONTROL.setDateColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("hourTickMarkColor".equals(key)) {
                CONTROL.setHourTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minuteTickMarkColor".equals(key)) {
                CONTROL.setMinuteTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("tickLabelColor".equals(key)) {
                CONTROL.setTickLabelColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("alarmColor".equals(key)) {
                CONTROL.setAlarmColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("hourTickMarksVisible".equals(key)) {
                CONTROL.setHourTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("minuteTickMarksVisible".equals(key)) {
                CONTROL.setMinuteTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("tickLabelsVisible".equals(key)) {
                CONTROL.setTickLabelsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("hourColor".equals(key)) {
                CONTROL.setHourColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minuteColor".equals(key)) {
                CONTROL.setMinuteColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("secondColor".equals(key)) {
                CONTROL.setSecondColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("knobColor".equals(key)) {
                CONTROL.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("lcdDesign".equals(key)) {
                CONTROL.setLcdDesign(((ObjectProperty<LcdDesign>) properties.get(key)).get());
            } else if ("onAlarm".equals(key)) {
                CONTROL.setOnAlarm(((ObjectProperty<AlarmEventListener>) properties.get(key)).get());
            } else if ("onTimeEvent".equals(key)) {
                CONTROL.setOnTimeEvent(((ObjectProperty<TimeEventListener>) properties.get(key)).get());
            } else if ("alarmsEnabled".equals(key)) {
                CONTROL.setAlarmsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("alarmsVisible".equals(key)) {
                CONTROL.setAlarmsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("lcdCrystalEnabled".equals(key)) {
                CONTROL.setLcdCrystalEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("shadowsEnabled".equals(key)) {
                CONTROL.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("lcdFont".equals(key)) {
                CONTROL.setLcdFont(((ObjectProperty<LcdFont>) properties.get(key)).get());
            } else if ("locale".equals(key)) {
                CONTROL.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
            } else if("animated".equals(key)) {
                CONTROL.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if("animationDuration".equals(key)) {
                CONTROL.setAnimationDuration(((LongProperty) properties.get(key)).get());
            } else if ("running".equals(key)) {
                CONTROL.setRunning(((BooleanProperty) properties.get(key)).get());
            } else if ("customFontEnabled".equals(key)) {
                CONTROL.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("customFont".equals(key)) {
                CONTROL.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
            }
        }
        return CONTROL;
    }
}
