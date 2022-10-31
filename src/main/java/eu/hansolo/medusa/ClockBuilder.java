/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.medusa;

import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.events.AlarmEvt;
import eu.hansolo.medusa.events.MedusaEvt;
import eu.hansolo.medusa.events.TimeEvt;
import eu.hansolo.toolbox.evt.EvtObserver;
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

    public final B dayVisible(final boolean VISIBLE) {
        properties.put("dayVisible", new SimpleBooleanProperty(VISIBLE));
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

    public final B onAlarm(final EvtObserver<MedusaEvt> LISTENER) {
        properties.put("onAlarm", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onTimeEvent(final EvtObserver<MedusaEvt> LISTENER) {
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
        final Clock clock;
        if (properties.containsKey("skinType")) {
            ClockSkinType skinType = ((ObjectProperty<ClockSkinType>) properties.get("skinType")).get();
            clock = new Clock(skinType);
            switch(skinType) {
                case YOTA2:
                    clock.setBackgroundPaint(Color.rgb(40, 42, 48));
                    clock.setHourTickMarkColor(Color.rgb(255, 255, 255));
                    clock.setMinuteTickMarkColor(Color.rgb(255, 255, 255, 0.5));
                    clock.setHourColor(Color.WHITE);
                    clock.setMinuteColor(Color.WHITE);
                    clock.setKnobColor(Color.WHITE);
                    clock.setTextColor(Color.rgb(255, 255, 255, 0.5));
                    clock.setDateColor(Color.rgb(255, 255, 255));
                    break;
                case LCD:
                    clock.setBorderPaint(Color.WHITE);
                    clock.setForegroundPaint(Color.WHITE);
                    break;
                case PEAR:
                    clock.setBackgroundPaint(Color.BLACK);
                    clock.setHourColor(Color.WHITE);
                    clock.setMinuteColor(Color.WHITE);
                    clock.setSecondColor(Color.rgb(255, 165, 24));
                    clock.setHourTickMarkColor(Color.WHITE);
                    clock.setMinuteTickMarkColor(Color.rgb(115, 115, 115));
                    clock.setTickLabelColor(Color.WHITE);
                    clock.setDateColor(Color.WHITE);
                    clock.setDateVisible(true);
                    clock.setSecondsVisible(true);
                    clock.setTextVisible(false);
                    clock.setTitleVisible(false);
                    break;
                case PLAIN:
                    clock.setBackgroundPaint(Color.rgb(29, 29, 29));
                    clock.setHourColor(Color.rgb(190, 190, 190));
                    clock.setMinuteColor(Color.rgb(190, 190, 190));
                    clock.setSecondColor(Color.rgb(0, 244, 0));
                    clock.setDateColor(Color.rgb(190, 190, 190));
                    clock.setSecondsVisible(true);
                    clock.setHourTickMarkColor(Color.rgb(240, 240, 240));
                    clock.setMinuteTickMarkColor(Color.rgb(240, 240, 240));
                    break;
                case DB:
                    clock.setDiscreteSeconds(false);
                    clock.setDiscreteMinutes(true);
                    clock.setSecondColor(Color.rgb(167, 0, 0));
                    clock.setSecondsVisible(true);
                    break;
                case ROUND_LCD:
                    clock.setTextVisible(true);
                    clock.setDateVisible(true);
                    break;
                case FAT:
                    clock.setDiscreteMinutes(true);
                    break;
                case SLIM:
                    clock.setSecondsVisible(true);
                    clock.setDateVisible(true);
                    clock.setDayVisible(true);
                    clock.setHourColor(Color.WHITE);
                    clock.setMinuteColor(Color.rgb(0,191,255));
                    clock.setSecondColor(Color.WHITE);
                    clock.setDateColor(Color.WHITE);
                    break;
                case MINIMAL:
                    clock.setBackgroundPaint(Color.rgb(255, 255, 255, 0.3));
                    clock.setTextColor(Color.WHITE);
                    clock.setMinuteColor(Color.rgb(59, 209, 255));
                    clock.setSecondColor(Color.rgb(255, 255, 255, 0.8));
                    clock.setSecondsVisible(true);
                    clock.setDateVisible(true);
                    break;
                case DIGITAL:
                    clock.setTextVisible(true);
                    clock.setDateVisible(true);
                    clock.setSecondsVisible(true);
                    break;
                case TEXT:
                    clock.setTextVisible(true);
                    clock.setDateVisible(true);
                    clock.setSecondsVisible(true);
                    break;
                case DESIGN:
                    clock.setDiscreteHours(false);
                    clock.setDiscreteMinutes(false);
                    clock.setDiscreteSeconds(false);
                    clock.setTextVisible(false);
                    clock.setDateVisible(false);
                    clock.setSecondsVisible(false);
                    clock.setHourColor(Color.RED);
                    clock.setBackgroundPaint(Color.WHITE);
                    break;
                case INDUSTRIAL:
                    clock.setBackgroundPaint(Color.web("#efefef"));
                    clock.setHourColor(Color.web("#2a2a2a"));
                    clock.setMinuteColor(Color.web("#2a2a2a"));
                    clock.setSecondColor(Color.web("#d1222b"));
                    clock.setHourTickMarkColor(Color.BLACK);
                    clock.setMinuteTickMarkColor(Color.BLACK);
                    clock.setTickLabelsVisible(false);
                    clock.setTickLabelColor(Color.BLACK);
                    clock.setDateColor(Color.BLACK);
                    clock.setDateVisible(false);
                    clock.setSecondsVisible(true);
                    clock.setTextVisible(false);
                    clock.setTextColor(Color.BLACK);
                    clock.setTitleVisible(false);
                    clock.setTitleColor(Color.BLACK);
                    clock.setBorderPaint(Color.BLACK);
                    clock.setBorderWidth(5);
                    break;
                case TILE:
                    clock.setBackgroundPaint(Color.rgb(42,42,42));
                    clock.setHourColor(Color.rgb(238, 238, 238));
                    clock.setMinuteColor(Color.rgb(238, 238, 238));
                    clock.setSecondColor(Color.rgb(238, 238, 238));
                    clock.setKnobColor(Color.rgb(238, 238, 238));
                    clock.setHourTickMarkColor(Color.rgb(238, 238, 238));
                    clock.setMinuteTickMarkColor(Color.rgb(238, 238, 238));
                    clock.setDateColor(Color.rgb(238, 238, 238));
                    clock.setDateVisible(false);
                    clock.setSecondsVisible(false);
                    clock.setTextVisible(false);
                    clock.setTextColor(Color.rgb(238, 238, 238));
                    clock.setTitleVisible(true);
                    clock.setTitleColor(Color.rgb(238, 238, 238));
                    break;
                case DIGI:
                    clock.setTextVisible(true);
                    clock.setDateVisible(true);
                    break;
                case MORPHING:
                    break;
            }
        } else {
            clock = new Clock();
        }

        // Make sure that alarms, sections, areas and markers will be added first
        if (properties.keySet().contains("alarmsArray")) {
            clock.setAlarms(((ObjectProperty<Alarm[]>) properties.get("alarmsArray")).get());
        }
        if(properties.keySet().contains("alarmsList")) {
            clock.setAlarms(((ObjectProperty<List<Alarm>>) properties.get("alarmsList")).get());
        }

        if (properties.keySet().contains("sectionsArray")) {
            clock.setSections(((ObjectProperty<TimeSection[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            clock.setSections(((ObjectProperty<List<TimeSection>>) properties.get("sectionsList")).get());
        }

        if (properties.keySet().contains("areasArray")) {
            clock.setAreas(((ObjectProperty<TimeSection[]>) properties.get("areasArray")).get());
        }
        if(properties.keySet().contains("areasList")) {
            clock.setAreas(((ObjectProperty<List<TimeSection>>) properties.get("areasList")).get());
        }

        for (String key : properties.keySet()) {
            switch (key) {
                case "prefSize"               -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    clock.setPrefSize(dim.getWidth(), dim.getHeight());
                }
                case "minSize"                -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    clock.setMinSize(dim.getWidth(), dim.getHeight());
                }
                case "maxSize"                -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    clock.setMaxSize(dim.getWidth(), dim.getHeight());
                }
                case "prefWidth"              -> clock.setPrefWidth(((DoubleProperty) properties.get(key)).get());
                case "prefHeight"             -> clock.setPrefHeight(((DoubleProperty) properties.get(key)).get());
                case "minWidth"               -> clock.setMinWidth(((DoubleProperty) properties.get(key)).get());
                case "minHeight"              -> clock.setMinHeight(((DoubleProperty) properties.get(key)).get());
                case "maxWidth"               -> clock.setMaxWidth(((DoubleProperty) properties.get(key)).get());
                case "maxHeight"              -> clock.setMaxHeight(((DoubleProperty) properties.get(key)).get());
                case "scaleX"                 -> clock.setScaleX(((DoubleProperty) properties.get(key)).get());
                case "scaleY"                 -> clock.setScaleY(((DoubleProperty) properties.get(key)).get());
                case "layoutX"                -> clock.setLayoutX(((DoubleProperty) properties.get(key)).get());
                case "layoutY"                -> clock.setLayoutY(((DoubleProperty) properties.get(key)).get());
                case "translateX"             -> clock.setTranslateX(((DoubleProperty) properties.get(key)).get());
                case "translateY"             -> clock.setTranslateY(((DoubleProperty) properties.get(key)).get());
                case "padding"                -> clock.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
                case "styleClass"             -> {
                    clock.getStyleClass().setAll("gauge");
                    clock.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
                }
                case "time"                   -> clock.setTime(((ObjectProperty<ZonedDateTime>) properties.get(key)).get());
                case "title"                  -> clock.setTitle(((StringProperty) properties.get(key)).get());
                case "text"                   -> clock.setText(((StringProperty) properties.get(key)).get());
                case "checkSectionsForValue"  -> clock.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
                case "checkAreasForValue"     -> clock.setCheckAreasForValue(((BooleanProperty) properties.get(key)).get());
                case "sectionsVisible"        -> clock.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
                case "highlightSections"      -> clock.setHighlightSections(((BooleanProperty) properties.get(key)).get());
                case "areasVisible"           -> clock.setAreasVisible(((BooleanProperty) properties.get(key)).get());
                case "highlightAreas"         -> clock.setHighlightAreas(((BooleanProperty) properties.get(key)).get());
                case "discreteSeconds"        -> clock.setDiscreteSeconds(((BooleanProperty) properties.get(key)).get());
                case "discreteMinutes"        -> clock.setDiscreteMinutes(((BooleanProperty) properties.get(key)).get());
                case "discreteHours"          -> clock.setDiscreteHours(((BooleanProperty) properties.get(key)).get());
                case "secondsVisible"         -> clock.setSecondsVisible(((BooleanProperty) properties.get(key)).get());
                case "titleVisible"           -> clock.setTitleVisible(((BooleanProperty) properties.get(key)).get());
                case "textVisible"            -> clock.setTextVisible(((BooleanProperty) properties.get(key)).get());
                case "dateVisible"            -> clock.setDateVisible(((BooleanProperty) properties.get(key)).get());
                case "dayVisible"             -> clock.setDayVisible(((BooleanProperty) properties.get(key)).get());
                case "nightMode"              -> clock.setNightMode(((BooleanProperty) properties.get(key)).get());
                case "autoNightMode"          -> clock.setAutoNightMode(((BooleanProperty) properties.get(key)).get());
                case "backgroundPaint"        -> clock.setBackgroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
                case "borderPaint"            -> clock.setBorderPaint(((ObjectProperty<Paint>) properties.get(key)).get());
                case "borderWidth"            -> clock.setBorderWidth(((DoubleProperty) properties.get(key)).get());
                case "foregroundPaint"        -> clock.setForegroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
                case "titleColor"             -> clock.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "textColor"              -> clock.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "dateColor"              -> clock.setDateColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "hourTickMarkColor"      -> clock.setHourTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "minuteTickMarkColor"    -> clock.setMinuteTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "tickLabelColor"         -> clock.setTickLabelColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "alarmColor"             -> clock.setAlarmColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "hourTickMarksVisible"   -> clock.setHourTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "minuteTickMarksVisible" -> clock.setMinuteTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "tickLabelsVisible"      -> clock.setTickLabelsVisible(((BooleanProperty) properties.get(key)).get());
                case "hourColor"              -> clock.setHourColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "minuteColor"            -> clock.setMinuteColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "secondColor"            -> clock.setSecondColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "knobColor"              -> clock.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "lcdDesign"              -> clock.setLcdDesign(((ObjectProperty<LcdDesign>) properties.get(key)).get());
                case "onAlarm"                -> clock.addClockObserver(AlarmEvt.ANY, ((ObjectProperty<EvtObserver<MedusaEvt>>) properties.get(key)).get());
                case "onTimeEvent"            -> clock.addClockObserver(TimeEvt.ANY, ((ObjectProperty<EvtObserver<MedusaEvt>>) properties.get(key)).get());
                case "alarmsEnabled"          -> clock.setAlarmsEnabled(((BooleanProperty) properties.get(key)).get());
                case "alarmsVisible"          -> clock.setAlarmsVisible(((BooleanProperty) properties.get(key)).get());
                case "lcdCrystalEnabled"      -> clock.setLcdCrystalEnabled(((BooleanProperty) properties.get(key)).get());
                case "shadowsEnabled"         -> clock.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
                case "lcdFont"                -> clock.setLcdFont(((ObjectProperty<LcdFont>) properties.get(key)).get());
                case "locale"                 -> clock.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
                case "animated"               -> clock.setAnimated(((BooleanProperty) properties.get(key)).get());
                case "animationDuration"      -> clock.setAnimationDuration(((LongProperty) properties.get(key)).get());
                case "running"                -> clock.setRunning(((BooleanProperty) properties.get(key)).get());
                case "customFontEnabled"      -> clock.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
                case "customFont"             -> clock.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
            }
        }
        return clock;
    }
}
