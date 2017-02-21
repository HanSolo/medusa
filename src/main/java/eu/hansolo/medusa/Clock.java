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

import eu.hansolo.medusa.events.AlarmEvent;
import eu.hansolo.medusa.events.AlarmEventListener;
import eu.hansolo.medusa.events.TimeEvent;
import eu.hansolo.medusa.events.TimeEvent.TimeEventType;
import eu.hansolo.medusa.events.TimeEventListener;
import eu.hansolo.medusa.events.UpdateEvent;
import eu.hansolo.medusa.events.UpdateEvent.EventType;
import eu.hansolo.medusa.events.UpdateEventListener;
import eu.hansolo.medusa.skins.ClockSkin;
import eu.hansolo.medusa.skins.DBClockSkin;
import eu.hansolo.medusa.skins.DesignClockSkin;
import eu.hansolo.medusa.skins.DigitalClockSkin;
import eu.hansolo.medusa.skins.FatClockSkin;
import eu.hansolo.medusa.skins.IndustrialClockSkin;
import eu.hansolo.medusa.skins.LcdClockSkin;
import eu.hansolo.medusa.skins.MinimalClockSkin;
import eu.hansolo.medusa.skins.PearClockSkin;
import eu.hansolo.medusa.skins.PlainClockSkin;
import eu.hansolo.medusa.skins.RoundLcdClockSkin;
import eu.hansolo.medusa.skins.SlimClockSkin;
import eu.hansolo.medusa.skins.TextClockSkin;
import eu.hansolo.medusa.skins.TileClockSkin;
import eu.hansolo.medusa.tools.Helper;
import eu.hansolo.medusa.tools.TimeSectionComparator;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.LongProperty;
import javafx.beans.property.LongPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;


/**
 * Created by hansolo on 28.01.16.
 */
public class Clock extends Control {
    public enum ClockSkinType { CLOCK, YOTA2, LCD, PEAR, PLAIN, DB, FAT, ROUND_LCD, SLIM, MINIMAL, DIGITAL, TEXT, DESIGN, INDUSTRIAL, TILE }

    public  static final int                  SHORT_INTERVAL   = 20;
    public  static final int                  LONG_INTERVAL    = 1000;
    public  static final Color                DARK_COLOR       = Color.rgb(36, 36, 36);
    public  static final Color                BRIGHT_COLOR     = Color.rgb(223, 223, 223);
    private        final UpdateEvent          RESIZE_EVENT     = new UpdateEvent(Clock.this, EventType.RESIZE);
    private        final UpdateEvent          REDRAW_EVENT     = new UpdateEvent(Clock.this, EventType.REDRAW);
    private        final UpdateEvent          VISIBILITY_EVENT = new UpdateEvent(Clock.this, EventType.VISIBILITY);
    private        final UpdateEvent          LCD_EVENT        = new UpdateEvent(Clock.this, EventType.LCD);
    private        final UpdateEvent          RECALC_EVENT     = new UpdateEvent(Clock.this, EventType.RECALC);
    private        final UpdateEvent          SECTION_EVENT    = new UpdateEvent(Clock.this, UpdateEvent.EventType.SECTION);
    private        final UpdateEvent          FINISHED_EVENT   = new UpdateEvent(Clock.this, UpdateEvent.EventType.FINISHED);

    private volatile ScheduledFuture<?>       periodicTickTask;
    private static   ScheduledExecutorService periodicTickExecutorService;

    // Alarm events
    private List<UpdateEventListener>         listenerList          = new CopyOnWriteArrayList<>();
    private List<AlarmEventListener>          alarmListenerList     = new CopyOnWriteArrayList<>();
    private List<TimeEventListener>           timeEventListenerList = new CopyOnWriteArrayList<>();

    private ObjectProperty<ZonedDateTime>     time;
    private LongProperty                      currentTime;
    private ZoneId                            zoneId;
    private Timeline                          timeline;
    private int                               updateInterval;
    private ClockSkinType                     skinType;
    private String                            _title;
    private StringProperty                    title;
    private boolean                           _checkSectionsForValue;
    private BooleanProperty                   checkSectionsForValue;
    private boolean                           _checkAreasForValue;
    private BooleanProperty                   checkAreasForValue;
    private ObservableList<TimeSection>       sections;
    private boolean                           _sectionsVisible;
    private BooleanProperty                   sectionsVisible;
    private boolean                           _highlightSections;
    private BooleanProperty                   highlightSections;
    private ObservableList<TimeSection>       areas;
    private boolean                           _areasVisible;
    private BooleanProperty                   areasVisible;
    private boolean                           _highlightAreas;
    private BooleanProperty                   highlightAreas;
    private String                            _text;
    private StringProperty                    text;
    private boolean                           _discreteSeconds;
    private BooleanProperty                   discreteSeconds;
    private boolean                           _discreteMinutes;
    private BooleanProperty                   discreteMinutes;
    private boolean                           _discreteHours;
    private BooleanProperty                   discreteHours;
    private boolean                           _secondsVisible;
    private BooleanProperty                   secondsVisible;
    private boolean                           _titleVisible;
    private BooleanProperty                   titleVisible;
    private boolean                           _textVisible;
    private BooleanProperty                   textVisible;
    private boolean                           _dateVisible;
    private BooleanProperty                   dateVisible;
    private boolean                           _nightMode;
    private BooleanProperty                   nightMode;
    private boolean                           _running;
    private BooleanProperty                   running;
    private boolean                           _autoNightMode;
    private BooleanProperty                   autoNightMode;
    private Paint                             _backgroundPaint;
    private ObjectProperty<Paint>             backgroundPaint;
    private Paint                             _borderPaint;
    private ObjectProperty<Paint>             borderPaint;
    private double                            _borderWidth;
    private DoubleProperty                    borderWidth;
    private Paint                             _foregroundPaint;
    private ObjectProperty<Paint>             foregroundPaint;
    private Color                             _titleColor;
    private ObjectProperty<Color>             titleColor;
    private Color                             _textColor;
    private ObjectProperty<Color>             textColor;
    private Color                             _dateColor;
    private ObjectProperty<Color>             dateColor;
    private Color                             _hourTickMarkColor;
    private ObjectProperty<Color>             hourTickMarkColor;
    private Color                             _minuteTickMarkColor;
    private ObjectProperty<Color>             minuteTickMarkColor;
    private Color                             _tickLabelColor;
    private ObjectProperty<Color>             tickLabelColor;
    private Color                             _alarmColor;
    private ObjectProperty<Color>             alarmColor;
    private boolean                           _hourTickMarksVisible;
    private BooleanProperty                   hourTickMarksVisible;
    private boolean                           _minuteTickMarksVisible;
    private BooleanProperty                   minuteTickMarksVisible;
    private boolean                           _tickLabelsVisible;
    private BooleanProperty                   tickLabelsVisible;
    private Color                             _hourColor;
    private ObjectProperty<Color>             hourColor;
    private Color                             _minuteColor;
    private ObjectProperty<Color>             minuteColor;
    private Color                             _secondColor;
    private ObjectProperty<Color>             secondColor;
    private Color                             _knobColor;
    private ObjectProperty<Color>             knobColor;
    private LcdDesign                         _lcdDesign;
    private ObjectProperty<LcdDesign>         lcdDesign;
    private boolean                           _alarmsEnabled;
    private BooleanProperty                   alarmsEnabled;
    private boolean                           _alarmsVisible;
    private BooleanProperty                   alarmsVisible;
    private ObservableList<Alarm>             alarms;
    private List<Alarm>                       alarmsToRemove;
    private boolean                           _lcdCrystalEnabled;
    private BooleanProperty                   lcdCrystalEnabled;
    private boolean                           _shadowsEnabled;
    private BooleanProperty                   shadowsEnabled;
    private LcdFont                           _lcdFont;
    private ObjectProperty<LcdFont>           lcdFont;
    private Locale                            _locale;
    private ObjectProperty<Locale>            locale;
    private TickLabelLocation                 _tickLabelLocation;
    private ObjectProperty<TickLabelLocation> tickLabelLocation;
    private boolean                           _animated;
    private BooleanProperty                   animated;
    private long                              animationDuration;
    private boolean                           _customFontEnabled;
    private BooleanProperty                   customFontEnabled;
    private Font                              _customFont;
    private ObjectProperty<Font>              customFont;


    // ******************** Constructors **************************************
    public Clock() {
        this(ClockSkinType.CLOCK, ZonedDateTime.now());
    }
    public Clock(final ClockSkinType SKIN) {
        this(SKIN, ZonedDateTime.now());
    }
    public Clock(final ZonedDateTime TIME) {
        this(ClockSkinType.CLOCK, TIME);
    }
    public Clock(final long EPOCH_SECONDS) {
        this(ClockSkinType.CLOCK, ZonedDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_SECONDS), ZoneId.systemDefault()));
    }
    public Clock(final ClockSkinType SKIN, final ZonedDateTime TIME) {
        setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        skinType = SKIN;
        getStyleClass().add("clock");

        init(TIME);
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init(final ZonedDateTime TIME) {
        time                    = new ObjectPropertyBase<ZonedDateTime>(TIME) {
            @Override protected void invalidated() {
                if (!isRunning() && isAnimated()) {
                    long animationDuration = getAnimationDuration();
                    timeline.stop();
                    final KeyValue KEY_VALUE = new KeyValue(currentTime, TIME.toEpochSecond());
                    final KeyFrame KEY_FRAME = new KeyFrame(javafx.util.Duration.millis(animationDuration), KEY_VALUE);
                    timeline.getKeyFrames().setAll(KEY_FRAME);
                    timeline.setOnFinished(e -> fireUpdateEvent(FINISHED_EVENT));
                    timeline.play();
                } else {
                    currentTime.set(TIME.toEpochSecond());
                    fireUpdateEvent(FINISHED_EVENT);
                }
            }
            @Override public Object getBean() { return Clock.this; }
            @Override public String getName() { return "time"; }
        };
        currentTime             = new LongPropertyBase(time.get().toEpochSecond()) {
            @Override protected void invalidated() {}
            @Override public Object getBean() { return Clock.this; }
            @Override public String getName() { return "currentTime"; }
        };
        zoneId                  = time.get().getZone();
        timeline                = new Timeline();
        timeline.setOnFinished(e -> fireUpdateEvent(FINISHED_EVENT));
        updateInterval          = LONG_INTERVAL;
        _checkSectionsForValue  = false;
        _checkAreasForValue     = false;
        sections                = FXCollections.observableArrayList();
        _secondsVisible         = false;
        _highlightSections      = false;
        areas                   = FXCollections.observableArrayList();
        _areasVisible           = false;
        _highlightAreas         = false;
        _text                   = "";
        _discreteSeconds        = true;
        _discreteMinutes        = true;
        _discreteHours          = false;
        _secondsVisible         = false;
        _titleVisible           = false;
        _textVisible            = false;
        _dateVisible            = false;
        _nightMode              = false;
        _running                = false;
        _autoNightMode          = false;
        _backgroundPaint        = Color.TRANSPARENT;
        _borderPaint            = Color.TRANSPARENT;
        _borderWidth            = 1;
        _foregroundPaint        = Color.TRANSPARENT;
        _titleColor             = DARK_COLOR;
        _textColor              = DARK_COLOR;
        _dateColor              = DARK_COLOR;
        _hourTickMarkColor      = DARK_COLOR;
        _minuteTickMarkColor    = DARK_COLOR;
        _tickLabelColor         = DARK_COLOR;
        _alarmColor             = DARK_COLOR;
        _hourTickMarksVisible   = true;
        _minuteTickMarksVisible = true;
        _tickLabelsVisible      = true;
        _hourColor              = DARK_COLOR;
        _minuteColor            = DARK_COLOR;
        _secondColor            = DARK_COLOR;
        _knobColor              = DARK_COLOR;
        _lcdDesign              = LcdDesign.STANDARD;
        _alarmsEnabled          = false;
        _alarmsVisible          = false;
        alarms                  = FXCollections.observableArrayList();
        alarmsToRemove          = new ArrayList<>();
        _lcdCrystalEnabled      = false;
        _shadowsEnabled         = false;
        _lcdFont                = LcdFont.DIGITAL_BOLD;
        _locale                 = Locale.US;
        _tickLabelLocation      = TickLabelLocation.INSIDE;
        _animated               = false;
        animationDuration       = 10000;
        _customFontEnabled      = false;
        _customFont             = Fonts.robotoRegular(12);
    }

    private void registerListeners() { disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1)); }


    // ******************** Methods *******************************************
    /**
     * Returns the current time of the clock.
     * @return the current time of the clock
     */
    public ZonedDateTime getTime() { return time.get(); }
    /**
     * Defines the current time of the clock.
     * @param TIME
     */
    public void setTime(final ZonedDateTime TIME) { time.set(TIME); }
    public void setTime(final long EPOCH_SECONDS) {
        time.set(ZonedDateTime.ofInstant(Instant.ofEpochSecond(EPOCH_SECONDS), getZoneId()));
    }
    public ObjectProperty<ZonedDateTime> timeProperty() { return time; }

    /**
     * Returns the current time in epoch seconds
     * @return the current time in epoch seconds
     */
    public long getCurrentTime() { return currentTime.get(); }
    public ReadOnlyLongProperty currentTimeProperty() { return currentTime; }

    public ZoneId getZoneId() { return zoneId; }

    /**
     * Returns the title of the clock. The title
     * could be used to show for example the current
     * city or timezone.
     * @return the title of the clock
     */
    public String getTitle() { return null == title ? _title : title.get(); }
    /**
     * Defines the title of the clock. The title
     * could be used to show for example the current
     * city or timezone
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            title.set(TITLE);
        }
    }
    public StringProperty titleProperty() {
        if (null == title) {
            title  = new StringPropertyBase(_title) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "title"; }
            };
            _title = null;
        }
        return title;
    }

    /**
     * Returns the text that was defined for the clock.
     * This text could be used for additional information.
     * @return the text that was defined for the clock
     */
    public String getText() { return null == text ? _text : text.get(); }
    /**
     * Define the text for the clock.
     * This text could be used for additional information.
     * @param TEXT
     */
    public void setText(final String TEXT) {
        if (null == text) {
            _text = TEXT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            text.set(TEXT);
        }
    }
    public StringProperty textProperty() {
        if (null == text) {
            text  = new StringPropertyBase(_text) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "text"; }
            };
            _text = null;
        }
        return text; 
    }

    /**
     * Returns true if the clock will check each section for
     * the current time and the section will fire events in
     * case the current time enters or leaves a section.
     * This section events can be used to control something like
     * switching on/off lights etc.
     * @return true if the clock will check each section for the current time
     */
    public boolean getCheckSectionsForValue() { return null == checkSectionsForValue ? _checkSectionsForValue : checkSectionsForValue.get(); }
    /**
     * Defines if the clock will check each section for
     * the current time and the section will fire events
     * in case the current time enters or leaves a section.
     * This section events can be used to control something like
     * switching on/off lights etc.
     * @param CHECK
     */
    public void setCheckSectionsForValue(final boolean CHECK) {
        if (null == checkSectionsForValue) {
            _checkSectionsForValue = CHECK;
        } else {
            checkSectionsForValue.set(CHECK);
        }
    }
    public BooleanProperty checkSectionsForValueProperty() {
        if (null == checkSectionsForValue) { checkSectionsForValue = new SimpleBooleanProperty(Clock.this, "checkSectionsForValue", _checkSectionsForValue); }
        return checkSectionsForValue;
    }

    /**
     * Returns true if the clock will check each area for
     * the current time and the area will fire events in
     * case the current time enters or leaves a section.
     * This area events can be used to control something like
     * switching on/off lights etc.
     * @return true if the clock will check each are for the current time
     */
    public boolean getCheckAreasForValue() { return null == checkAreasForValue ? _checkAreasForValue : checkAreasForValue.get(); }
    /**
     * Defines if the clock will check each area for
     * the current time and the area will fire events in
     * case the current time enters or leaves a section.
     * This area events can be used to control something like
     * switching on/off lights etc.
     * @param CHECK
     */
    public void setCheckAreasForValue(final boolean CHECK) {
        if (null == checkAreasForValue) {
            _checkAreasForValue = CHECK;
        } else {
            checkAreasForValue.set(CHECK);
        }
    }
    public BooleanProperty checkAreasForValueProperty() {
        if (null == checkAreasForValue) { checkAreasForValue = new SimpleBooleanProperty(Clock.this, "checkAreasForValue", _checkAreasForValue); }
        return checkAreasForValue;
    }

    /**
     * Returns an observable list of TimeSection objects. The sections
     * will be used to colorize areas with a special meaning.
     * TimeSections in the Medusa library usually are less eye-catching than
     * Areas.
     * @return an observable list of TimeSection objects
     */
    public ObservableList<TimeSection> getSections() { return sections; }
    /**
     * Sets the sections to the given list of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTIONS
     */
    public void setSections(final List<TimeSection> SECTIONS) {
        sections.setAll(SECTIONS);
        Collections.sort(sections, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTIONS
     */
    public void setSections(final TimeSection... SECTIONS) { setSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given TimeSection to the list of sections.
     * Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTION
     */
    public void addSection(final TimeSection SECTION) {
        if (null == SECTION) return;
        sections.add(SECTION);
        Collections.sort(sections, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Removes the given TimeSection from the list of sections.
     * Sections in the Medusa library usually are less eye-catching
     * than Areas.
     * @param SECTION
     */
    public void removeSection(final TimeSection SECTION) {
        if (null == SECTION) return;
        sections.remove(SECTION);
        Collections.sort(sections, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of sections.
     */
    public void clearSections() {
        sections.clear();
        fireUpdateEvent(SECTION_EVENT);
    }

    /**
     * Returns true if the sections should be drawn in the clock.
     * @return true if the sections should be drawn in the clock.
     */
    public boolean getSectionsVisible() { return null == sectionsVisible ? _sectionsVisible : sectionsVisible.get(); }
    /**
     * Defines if the sections should be drawn in the clock.
     * @param VISIBLE
     */
    public void setSectionsVisible(final boolean VISIBLE) {
        if (null == sectionsVisible) {
            _sectionsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            sectionsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionsVisibleProperty() {
        if (null == sectionsVisible) {
            sectionsVisible = new BooleanPropertyBase(_sectionsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "sectionsVisible"; }
            };
        }
        return sectionsVisible;
    }

    /**
     * Returns true if sections should be highlighted in case they
     * contain the current time.
     * @return true if sections should be highlighted
     */
    public boolean isHighlightSections() { return null == highlightSections ? _highlightSections : highlightSections.get(); }
    /**
     * Defines if sections should be highlighted in case they
     * contain the current time.
     * @param HIGHLIGHT
     */
    public void setHighlightSections(final boolean HIGHLIGHT) {
        if (null == highlightSections) {
            _highlightSections = HIGHLIGHT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            highlightSections.set(HIGHLIGHT);
        }
    }
    public BooleanProperty highlightSectionsProperty() {
        if (null == highlightSections) {
            highlightSections = new BooleanPropertyBase(_highlightSections) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "highlightSections"; }
            };
        }
        return highlightSections;
    }

    /**
     * Returns an observable list of TimeSection objects. The sections
     * will be used to colorize areas with a special meaning.
     * Areas in the Medusa library usually are more eye-catching
     * than Sections.
     * @return an observable list of TimeSection objects
     */
    public ObservableList<TimeSection> getAreas() { return areas; }
    /**
     * Sets the areas to the given list of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Areas in the Medusa library usually are more eye-catching
     * than Sections.
     * @param AREAS
     */
    public void setAreas(final List<TimeSection> AREAS) {
        areas.setAll(AREAS);
        Collections.sort(areas, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Sets the areas to the given array of TimeSection objects. The
     * sections will be used to colorize areas with a special
     * meaning. Areas in the Medusa library usually are more eye-catching
     * than Sections.
     * @param AREAS
     */
    public void setAreas(final TimeSection... AREAS) { setAreas(Arrays.asList(AREAS)); }
    /**
     * Adds the given TimeSection to the list of areas.
     * Areas in the Medusa library usually are more eye-catching
     * than Sections.
     * @param AREA
     */
    public void addArea(final TimeSection AREA) {
        if (null == AREA) return;
        areas.add(AREA);
        Collections.sort(areas, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Removes the given TimeSection from the list of areas.
     * Areas in the Medusa library usually are more eye-catching
     * than Sections.
     * @param AREA
     */
    public void removeArea(final TimeSection AREA) {
        if (null == AREA) return;
        areas.remove(AREA);
        Collections.sort(areas, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of areas
     */
    public void clearAreas() {
        areas.clear();
        fireUpdateEvent(SECTION_EVENT);
    }

    /**
     * Returns true if the areas should be drawn in the clock.
     * @return true if the areas should be drawn in the clock
     */
    public boolean getAreasVisible() { return null == areasVisible ? _areasVisible : areasVisible.get(); }
    /**
     * Defines if the areas should be drawn in the clock.
     * @param VISIBLE
     */
    public void setAreasVisible(final boolean VISIBLE) {
        if (null == areasVisible) {
            _areasVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            areasVisible.set(VISIBLE);
        }
    }
    public BooleanProperty areasVisibleProperty() {
        if (null == areasVisible) {
            areasVisible = new BooleanPropertyBase(_areasVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "areasVisible"; }
            };
        }
        return areasVisible;
    }

    /**
     * Returns true if areas should be highlighted in case they
     * contain the current time.
     * @return true if areas should be highlighted
     */
    public boolean isHighlightAreas() { return null == highlightAreas ? _highlightAreas : highlightAreas.get(); }
    /**
     * Defines if areas should be highlighted in case they
     * contain the current time.
     * @param HIGHLIGHT
     */
    public void setHighlightAreas(final boolean HIGHLIGHT) {
        if (null == highlightAreas) {
            _highlightAreas = HIGHLIGHT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            highlightAreas.set(HIGHLIGHT);
        }
    }
    public BooleanProperty highlightAreasProperty() {
        if (null == highlightAreas) {
            highlightAreas = new BooleanPropertyBase(_highlightAreas) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "highlightAreas"; }
            };
        }
        return highlightAreas;
    }

    /**
     * Returns true if the second hand of the clock should move
     * in discrete steps of 1 second. Otherwise it will move continuously like
     * in an automatic clock.
     * @return true if the second hand of the clock should move in discrete steps of 1 second
     */
    public boolean isDiscreteSeconds() { return null == discreteSeconds ? _discreteSeconds : discreteSeconds.get(); }
    /**
     * Defines if the second hand of the clock should move in
     * discrete steps of 1 second. Otherwise it will move continuously like
     * in an automatic clock.
     * @param DISCRETE
     */
    public void setDiscreteSeconds(boolean DISCRETE) {
        if (null == discreteSeconds) {
            _discreteSeconds = DISCRETE;
            stopTask(periodicTickTask);
            if (isAnimated()) return;
            scheduleTickTask();
        } else {
            discreteSeconds.set(DISCRETE);
        }
    }
    public BooleanProperty discreteSecondsProperty() {
        if (null == discreteSeconds) {
            discreteSeconds = new BooleanPropertyBase() {
                @Override protected void invalidated() {
                    stopTask(periodicTickTask);
                    if (isAnimated()) return;
                    scheduleTickTask();
                }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "discreteSeconds"; }
            };
        }
        return discreteSeconds;
    }

    /**
     * Returns true if the minute hand of the clock should move in
     * discrete steps of 1 minute. Otherwise it will move continuously like
     * in an automatic clock.
     * @return true if the minute hand of the clock should move in discrete steps of 1 minute
     */
    public boolean isDiscreteMinutes() { return null == discreteMinutes ? _discreteMinutes : discreteMinutes.get(); }
    /**
     * Defines if the minute hand of the clock should move in
     * discrete steps of 1 minute. Otherwise it will move continuously like
     * in an automatic clock.
     * @param DISCRETE
     */
    public void setDiscreteMinutes(boolean DISCRETE) {
        if (null == discreteMinutes) {
            _discreteMinutes = DISCRETE;
            stopTask(periodicTickTask);
            if (isAnimated()) return;
            scheduleTickTask();
        } else {
            discreteMinutes.set(DISCRETE);
        }
    }
    public BooleanProperty discreteMinutesProperty() {
        if (null == discreteMinutes) {
            discreteMinutes = new BooleanPropertyBase() {
                @Override protected void invalidated() {
                    stopTask(periodicTickTask);
                    if (isAnimated()) return;
                    scheduleTickTask();
                }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "discreteMinutes"; }
            };
        }
        return discreteMinutes;
    }

    /**
     * Returns true if the hour hand of the clock should move in
     * discrete steps of 1 hour. This behavior was more or less
     * implemented to realize the clock of clocks and should usually
     * be false.
     * @return true if the hour hand of the clock should move in discrete steps of 1 hour
     */
    public boolean isDiscreteHours() { return null == discreteHours ? _discreteHours : discreteHours.get(); }
    /**
     * Defines if the hour hand of the clock should move in
     * discrete steps of 1 hour. This behavior was more or less
     * implemented to realize the clock of clocks and should usually
     * be false.
     * @param DISCRETE
     */
    public void setDiscreteHours(final boolean DISCRETE) {
        if (null == discreteHours) {
            _discreteHours = DISCRETE;
        } else {
            discreteHours.set(DISCRETE);
        }
    }
    public BooleanProperty discreteHoursProperty() {
        if (null == discreteHours) { discreteHours = new SimpleBooleanProperty(Clock.this, "discreteHours", _discreteHours); }
        return discreteHours;
    }

    /**
     * Returns true if the second hand of the clock will be drawn.
     * @return true if the second hand of the clock will be drawn.
     */
    public boolean isSecondsVisible() { return null == secondsVisible ? _secondsVisible : secondsVisible.get(); }
    /**
     * Defines if the second hand of the clock will be drawn.
     * @param VISIBLE
     */
    public void setSecondsVisible(boolean VISIBLE) { 
        if (null == secondsVisible) {
            _secondsVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            secondsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty secondsVisibleProperty() { 
        if (null == secondsVisible) {
            secondsVisible = new BooleanPropertyBase(_secondsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "secondsVisible"; }
            };
        }
        return secondsVisible; 
    }

    /**
     * Returns true if the title of the clock will be drawn.
     * @return true if the title of the clock will be drawn
     */
    public boolean isTitleVisible() { return null == titleVisible ? _titleVisible : titleVisible.get(); }
    /**
     * Defines if the title of the clock will be drawn.
     * @param VISIBLE
     */
    public void setTitleVisible(final boolean VISIBLE) {
        if (null == titleVisible) {
            _titleVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            titleVisible.set(VISIBLE);
        }
    }
    public BooleanProperty titleVisibleProperty() {
        if (null == titleVisible) {
            titleVisible = new BooleanPropertyBase(_titleVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "titleVisible"; }
            };
        }
        return titleVisible;
    }

    /**
     * Returns true if the text of the clock will be drawn.
     * @return true if the text of the clock will be drawn
     */
    public boolean isTextVisible() { return null == textVisible ? _textVisible : textVisible.get(); }
    /**
     * Defines if the text of the clock will be drawn.
     * @param VISIBLE
     */
    public void setTextVisible(final boolean VISIBLE) {
        if (null == textVisible) {
            _textVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            textVisible.set(VISIBLE);
        }
    }
    public BooleanProperty textVisibleProperty() {
        if (null == textVisible) {
            textVisible = new BooleanPropertyBase(_textVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "textVisible"; }
            };
        }
        return textVisible;
    }

    /**
     * Returns true if the date of the clock will be drawn.
     * @return true if the date of the clock will be drawn
     */
    public boolean isDateVisible() { return null == dateVisible ? _dateVisible : dateVisible.get(); }
    /**
     * Defines if the date of the clock will be drawn.
     * @param VISIBLE
     */
    public void setDateVisible(final boolean VISIBLE) {
        if (null == dateVisible) {
            _dateVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            dateVisible.set(VISIBLE);
        }

    }
    public BooleanProperty dateVisibleProperty() {
        if (null == dateVisible) {
            dateVisible = new BooleanPropertyBase(_dateVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "dateVisible"; }
            };
        }
        return dateVisible;
    }

    /**
     * Returns true if the clock is in night mode (NOT USED AT THE MOMENT)
     * @return true if the clock is in night mode (NOT USED AT THE MOMENT)
     */
    public boolean isNightMode() { return null == nightMode ? _nightMode : nightMode.get(); }
    /**
     * Defines if the clock is in night mode (NOT USED AT THE MOMENT)
     * @param MODE
     */
    public void setNightMode(boolean MODE) { 
        if (null == nightMode) {
            _nightMode = MODE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            nightMode.set(MODE);
        }
    }
    public BooleanProperty nightModeProperty() { 
        if (null == nightMode) {
            nightMode = new BooleanPropertyBase(_nightMode) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "nightMode"; }
            };
        }
        return nightMode; 
    }

    /**
     * Returns true if the clock is running and shows the current time.
     * The clock will only start running if animated == false.
     * @return true if the clock is running
     */
    public boolean isRunning() { return null == running ? _running : running.get(); }
    /**
     * Defines if the clock is running.
     * The clock will only start running if animated == false;
     * @param RUNNING
     */
    public void setRunning(boolean RUNNING) { 
        if (null == running) {
            _running = RUNNING;
            if (RUNNING && !isAnimated()) { scheduleTickTask(); } else { stopTask(periodicTickTask); }
        } else {
            running.set(RUNNING);
        }
    }
    public BooleanProperty runningProperty() { 
        if (null == running) {
            running = new BooleanPropertyBase(_running) {
            @Override protected void invalidated() {
                if (get() && !isAnimated()) { scheduleTickTask(); } else { stopTask(periodicTickTask); }
            }
            @Override public Object getBean() { return Clock.this; }
            @Override public String getName() { return "running"; }
        }; }    
        return running; 
    }

    /**
     * Returns true if the clock is in auto night mode (NOT USED AT THE MOMENT).
     * The idea is that the clock can switch the colors from bright to dark
     * automatically in dependence on the time of day.
     * @return true if the clock is in auto night mode (NOT USED AT THE MOMENT)
     */
    public boolean isAutoNightMode() { return null == autoNightMode ? _autoNightMode : autoNightMode.get(); }
    /**
     * Defines if the clock is in auto night mode (NOT USED AT THE MOMENT)
     * @param MODE
     */
    public void setAutoNightMode(boolean MODE) { 
        if (null == autoNightMode) {
            _autoNightMode = MODE;
        } else {
            autoNightMode.set(MODE);
        }         
    }
    public BooleanProperty autoNightModeProperty() {
        if (null == autoNightMode) { autoNightMode = new SimpleBooleanProperty(Clock.this, "autoNightMode", _autoNightMode); }
        return autoNightMode;
    }

    /**
     * Returns the Paint object that will be used to fill the clock background.
     * This is usally a Color object.
     * @return the Paint object that will be used to fill the clock background
     */
    public Paint getBackgroundPaint() { return null == backgroundPaint ? _backgroundPaint : backgroundPaint.get(); }
    /**
     * Defines the Paint object that will be used to fill the clock background.
     * This is usally a Color object.
     * @param PAINT
     */
    public void setBackgroundPaint(final Paint PAINT) {
        if (null == backgroundPaint) {
            _backgroundPaint = PAINT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            backgroundPaint.set(PAINT);
        }
    }
    public ObjectProperty<Paint> backgroundPaintProperty() {
        if (null == backgroundPaint) {
            backgroundPaint  = new ObjectPropertyBase<Paint>(_backgroundPaint) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "backgroundPaint"; }
            };
            _backgroundPaint = null;
        }
        return backgroundPaint;
    }

    /**
     * Returns the Paint object that will be used to draw the border of the clock.
     * Usually this is a Color object.
     * @return the Paint object that will be used to draw the border of the clock
     */
    public Paint getBorderPaint() { return null == borderPaint ? _borderPaint : borderPaint.get(); }
    /**
     * Defines the Paint object that will be used to draw the border of the clock.
     * Usually this is a Color object.
     * @param PAINT
     */
    public void setBorderPaint(final Paint PAINT) {
        if (null == borderPaint) {
            _borderPaint = PAINT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            borderPaint.set(PAINT);
        }
    }
    public ObjectProperty<Paint> borderPaintProperty() {
        if (null == borderPaint) {
            borderPaint  = new ObjectPropertyBase<Paint>(_borderPaint) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "borderPaint"; }
            };
            _borderPaint = null;
        }
        return borderPaint;
    }

    /**
     * Returns the width in pixels that will be used to draw the border of the clock.
     * The value will be clamped between 0 and 50 pixels.
     * @return the width in pixels that will be used to draw the border of the clock
     */
    public double getBorderWidth() { return null == borderWidth ? _borderWidth : borderWidth.get(); }
    /**
     * Defines the width in pixels that will be used to draw the border of the clock.
     * The value will be clamped between 0 and 50 pixels.
     * @param WIDTH
     */
    public void setBorderWidth(final double WIDTH) {
        if (null == borderWidth) {
            _borderWidth = Helper.clamp(0.0, 50.0, WIDTH);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            borderWidth.set(WIDTH);
        }
    }
    public DoubleProperty borderWidthProperty() {
        if (null == borderWidth) {
            borderWidth = new DoublePropertyBase(_borderWidth) {
                @Override protected void invalidated() {
                    final double WIDTH = get();
                    if (WIDTH < 0 || WIDTH > 50) set(Helper.clamp(0.0, 50.0, WIDTH));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "borderWidth"; }
            };
        }
        return borderWidth;
    }

    /**
     * Returns the Paint object that will be used to fill the foreground of the clock.
     * This could be used to visualize glass effects etc. and is only rarely used.
     * @return the Paint object that will be used to fill the foreground of the clock
     */
    public Paint getForegroundPaint() { return null == foregroundPaint ? _foregroundPaint : foregroundPaint.get(); }
    /**
     * Defines the Paint object that will be used to fill the foreground of the clock.
     * This could be used to visualize glass effects etc. and is only rarely used.
     * @param PAINT
     */
    public void setForegroundPaint(final Paint PAINT) {
        if (null == foregroundPaint) {
            _foregroundPaint = PAINT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            foregroundPaint.set(PAINT);
        }
    }
    public ObjectProperty<Paint> foregroundPaintProperty() {
        if (null == foregroundPaint) {
            foregroundPaint  = new ObjectPropertyBase<Paint>(_foregroundPaint) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "foregroundPaint"; }
            };
            _foregroundPaint = null;
        }
        return foregroundPaint;
    }

    /**
     * Returns the color that will be used to colorize the title of the clock.
     * @return the color that will be used to colorize the title of the clock
     */
    public Color getTitleColor() { return null == titleColor ? _titleColor : titleColor.get(); }
    /**
     * Defines the color that will be used to colorize the title of the clock
     * @param COLOR
     */
    public void setTitleColor(final Color COLOR) {
        if (null == titleColor) {
            _titleColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            titleColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> titleColorProperty() {
        if (null == titleColor) {
            titleColor  = new ObjectPropertyBase<Color>(_titleColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "titleColor"; }
            };
            _titleColor = null;
        }
        return titleColor;
    }

    /**
     * Returns the color that will be used to colorize the text of the clock.
     * @return the color that will be used to colorize the text of the clock
     */
    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    /**
     * Defines the color that will be used to colorize the text of the clock.
     * @param COLOR
     */
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            textColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) {
            textColor  = new ObjectPropertyBase<Color>(_textColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "textColor"; }
            };
            _textColor = null;
        }
        return textColor;
    }

    /**
     * Returns the color that will be used to colorize the date of the clock.
     * @return the color that will be used to colorize the date of the clock
     */
    public Color getDateColor() { return null == dateColor ? _dateColor : dateColor.get(); }
    /**
     * Defines the color that will be used to colorize the date of the clock
     * @param COLOR
     */
    public void setDateColor(final Color COLOR) {
        if (null == dateColor) {
            _dateColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            dateColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> dateColorProperty() {
        if (null == dateColor) {
            dateColor  = new ObjectPropertyBase<Color>(_dateColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "dateColor"; }
            };
            _dateColor = null;
        }
        return dateColor;
    }

    /**
     * Returns the color that will be used to colorize the hour tickmarks of the clock.
     * @return the color that will be used to colorize the hour tickmarks of the clock
     */
    public Color getHourTickMarkColor() { return null == hourTickMarkColor ? _hourTickMarkColor : hourTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the hour tickmarks of the clock.
     * @param COLOR
     */
    public void setHourTickMarkColor(final Color COLOR) {
        if (null == hourTickMarkColor) {
            _hourTickMarkColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            hourTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> hourTickMarkColorProperty() {
        if (null == hourTickMarkColor) {
            hourTickMarkColor  = new ObjectPropertyBase<Color>(_hourTickMarkColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "hourTickMarkColor"; }
            };
            _hourTickMarkColor = null;
        }
        return hourTickMarkColor;
    }

    /**
     * Returns the color that will be used to colorize the minute tickmarks of the clock.
     * @return the color that will be used to colorize the minute tickmarks of the clock
     */
    public Color getMinuteTickMarkColor() { return null == minuteTickMarkColor ? _minuteTickMarkColor : minuteTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the minute tickmarks of the clock.
     * @param COLOR
     */
    public void setMinuteTickMarkColor(final Color COLOR) {
        if (null == minuteTickMarkColor) {
            _minuteTickMarkColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minuteTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> minuteTickMarkColorProperty() {
        if (null == minuteTickMarkColor) {
            minuteTickMarkColor  = new ObjectPropertyBase<Color>(_minuteTickMarkColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "minuteTickMarkColor"; }
            };
            _minuteTickMarkColor = null;
        }
        return minuteTickMarkColor;
    }

    /**
     * Returns the color that will be used to colorize the ticklabels of the clock.
     * @return the color that will be used to colorize the ticklabels of the clock
     */
    public Color getTickLabelColor() { return null == tickLabelColor ? _tickLabelColor : tickLabelColor.get(); }
    /**
     * Defines the color that will be used to colorize the ticklabels of the clock.
     * @param COLOR
     */
    public void setTickLabelColor(final Color COLOR) {
        if (null == tickLabelColor) {
            _tickLabelColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> tickLabelColorProperty() {
        if (null == tickLabelColor) {
            tickLabelColor  = new ObjectPropertyBase<Color>(_tickLabelColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "tickLabelColor"; }
            };
            _tickLabelColor = null;
        }
        return tickLabelColor;
    }

    /**
     * Returns the color that will be used to colorize the alarm icon.
     * @return the color that will be used to colorize the alarm icon
     */
    public Color getAlarmColor() { return null == alarmColor ? _alarmColor : alarmColor.get(); }
    /**
     * Defines the color that will be used to colorize the alarm icon
     * @param COLOR
     */
    public void setAlarmColor(final Color COLOR) {
        if (null == alarmColor) {
            _alarmColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            alarmColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> alarmColorProperty() {
        if (null == alarmColor) {
            alarmColor  = new ObjectPropertyBase<Color>(_alarmColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "alarmColor"; }
            };
            _alarmColor = null;
        }
        return alarmColor;
    }

    /**
     * Returns true if the hour tickmarks will be drawn.
     * @return true if the hour tickmarks will be drawn
     */
    public boolean isHourTickMarksVisible() { return null == hourTickMarksVisible ? _hourTickMarksVisible : hourTickMarksVisible.get(); }
    /**
     * Defines if the hour tickmarks will be drawn.
     * @param VISIBLE
     */
    public void setHourTickMarksVisible(final boolean VISIBLE) {
        if (null == hourTickMarksVisible) {
            _hourTickMarksVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            hourTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty hourTickMarksVisibleProperty() {
        if (null == hourTickMarksVisible) {
            hourTickMarksVisible = new BooleanPropertyBase(_hourTickMarksVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "hourTickMarksVisible"; }
            };
        }
        return hourTickMarksVisible;
    }

    /**
     * Returns true if the minute tickmarks will be drawn.
     * @return true if the minute tickmarks will be drawn
     */
    public boolean isMinuteTickMarksVisible() { return null == minuteTickMarksVisible ? _minuteTickMarksVisible : minuteTickMarksVisible.get(); }
    /**
     * Defines if the minute tickmarks will be drawn.
     * @param VISIBLE
     */
    public void setMinuteTickMarksVisible(final boolean VISIBLE) {
        if (null == minuteTickMarksVisible) {
            _minuteTickMarksVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minuteTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty minuteTickMarksVisibleProperty() {
        if (null == minuteTickMarksVisible) {
            minuteTickMarksVisible = new BooleanPropertyBase(_minuteTickMarksVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "minuteTickMarksVisible"; }
            };
        }
        return minuteTickMarksVisible;
    }

    /**
     * Returns true if the ticklabels will be drawn.
     * @return true if the ticklabels will be drawn
     */
    public boolean isTickLabelsVisible() { return null == tickLabelsVisible ? _tickLabelsVisible : tickLabelsVisible.get(); }
    /**
     * Defines if the ticklabels will be drawn.
     * @param VISIBLE
     */
    public void setTickLabelsVisible(final boolean VISIBLE) {
        if (null == tickLabelsVisible) {
            _tickLabelsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty tickLabelsVisibleProperty() {
        if (null == tickLabelsVisible) {
            tickLabelsVisible = new BooleanPropertyBase(_tickLabelsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "tickLabelsVisible"; }
            };
        }
        return tickLabelsVisible;
    }

    /**
     * Returns the color that will be used to colorize the hour hand of the clock.
     * @return the color that will be used to colorize the hour hand of the clock
     */
    public Color getHourColor() { return null == hourColor ? _hourColor : hourColor.get(); }
    /**
     * Defines the color that will be used to colorize the hour hand of the clock
     * @param COLOR
     */
    public void setHourColor(final Color COLOR) {
        if (null == hourColor) {
            _hourColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            hourColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> hourColorProperty() {
        if (null == hourColor) {
            hourColor  = new ObjectPropertyBase<Color>(_hourColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "hourColor"; }
            };
            _hourColor = null;
        }
        return hourColor;
    }

    /**
     * Returns the color that will be used to colorize the minute hand of the clock.
     * @return the color that will be used to colorize the minute hand of the clock
     */
    public Color getMinuteColor() { return null == minuteColor ? _minuteColor : minuteColor.get(); }
    /**
     * Defines the color that will be used to colorize the minute hand of the clock.
     * @param COLOR
     */
    public void setMinuteColor(final Color COLOR) {
        if (null == minuteColor) {
            _minuteColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minuteColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> minuteColorProperty() {
        if (null == minuteColor) {
            minuteColor  = new ObjectPropertyBase<Color>(_minuteColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "minuteColor"; }
            };
            _minuteColor = null;
        }
        return minuteColor;
    }

    /**
     * Returns the color that will be used to colorize the second hand of the clock.
     * @return the color that will be used to colorize the second hand of the clock
     */
    public Color getSecondColor() { return null == secondColor ? _secondColor : secondColor.get(); }
    /**
     * Defines the color that will be used to colorize the second hand of the clock
     * @param COLOR
     */
    public void setSecondColor(final Color COLOR) {
        if (null == secondColor) {
            _secondColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            secondColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> secondColorProperty() {
        if (null == secondColor) {
            secondColor  = new ObjectPropertyBase<Color>(_secondColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "secondColor"; }
            };
            _secondColor = null;
        }
        return secondColor;
    }

    /**
     * Returns the color that will be used to colorize the knob of the clock (if available)
     * @return the color that will be used to colorize the knob of the clock (if available)
     */
    public Color getKnobColor() { return null == knobColor ? _knobColor : knobColor.get(); }
    /**
     * Defines the color that will be used to colorize the knob of the clock (if available)
     * @param COLOR
     */
    public void setKnobColor(final Color COLOR) {
        if (null == knobColor) {
            _knobColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            knobColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> knobColorProperty() {
        if (null == knobColor) {
            knobColor  = new ObjectPropertyBase<Color>(_knobColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "knobColor"; }
            };
            _knobColor = null;
        }
        return knobColor;
    }

    /**
     * Returns the LcdDesign that will be used to visualize the LCD display.
     * This is currently only used in the LcdSkin.
     * @return the LcdDesign that will be used to visualize th LCD display
     */
    public LcdDesign getLcdDesign() { return null == lcdDesign ? _lcdDesign : lcdDesign.get(); }
    /**
     * Defines the LcdDesign that will be used to visualize the LCD display.
     * This is currently only used in the LcdSkin.
     * @param DESIGN
     */
    public void setLcdDesign(final LcdDesign DESIGN) {
        if (null == lcdDesign) {
            _lcdDesign = DESIGN;
            fireUpdateEvent(LCD_EVENT);
        } else {
            lcdDesign.set(DESIGN);
        }
    }
    public ObjectProperty<LcdDesign> lcdDesignProperty() {
        if (null == lcdDesign) {
            lcdDesign  = new ObjectPropertyBase<LcdDesign>(_lcdDesign) {
                @Override protected void invalidated() { fireUpdateEvent(LCD_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "lcdDesign"; }
            };
            _lcdDesign = null;
        }
        return lcdDesign;
    }

    /**
     * Returns true if alarms are enabled.
     * If false then no alarms will be triggered
     * @return true if alarms are enabled
     */
    public boolean isAlarmsEnabled() { return null == alarmsEnabled ? _alarmsEnabled : alarmsEnabled.get(); }
    /**
     * Defines if alarms are enabled.
     * If false then no alarms will be triggered.
     * @param CHECK
     */
    public void setAlarmsEnabled(final boolean CHECK) {
        if (null == alarmsEnabled) {
            _alarmsEnabled = CHECK;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            alarmsEnabled.set(CHECK);
        }
    }
    public BooleanProperty alarmsEnabledProperty() {
        if (null == alarmsEnabled) {
            alarmsEnabled = new BooleanPropertyBase(_alarmsEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "alarmsEnabled"; }
            };
        }
        return alarmsEnabled;
    }

    /**
     * Returns true if alarm markers should be drawn.
     * @return true if alarm markers should be drawn
     */
    public boolean isAlarmsVisible() { return null == alarmsVisible ? _alarmsVisible : alarmsVisible.get(); }
    /**
     * Defines if alarm markers should be drawn.
     * @param VISIBLE
     */
    public void setAlarmsVisible(final boolean VISIBLE) {
        if (null == alarmsVisible) {
            _alarmsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            alarmsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty alarmsVisibleProperty() {
        if (null == alarmsVisible) {
            alarmsVisible = new BooleanPropertyBase(_alarmsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "alarmsVisible"; }
            };
        }
        return alarmsVisible;
    }

    /**
     * Returns an observable list of Alarm objects.
     * @return an observable list of Alarm objects
     */
    public ObservableList<Alarm> getAlarms() { return alarms; }
    /**
     * Sets the alarms to the given list of Alarm objects.
     * @param ALARMS
     */
    public void setAlarms(final List<Alarm> ALARMS) { alarms.setAll(ALARMS); }
    /**
     * Sets the alarms to the given array of Alarm objects.
     * @param ALARMS
     */
    public void setAlarms(final Alarm... ALARMS) { setAlarms(Arrays.asList(ALARMS)); }
    /**
     * Adds the given Alarm object from the list of alarms.
     * @param ALARM
     */
    public void addAlarm(final Alarm ALARM) { if (!alarms.contains(ALARM)) alarms.add(ALARM); }
    /**
     * Removes the given Alarm object from the list of alarms.
     * @param ALARM
     */
    public void removeAlarm(final Alarm ALARM) { if (alarms.contains(ALARM)) alarms.remove(ALARM); }
    /**
     * Clears the list of alarms.
     */
    public void clearAlarms() { alarms.clear(); }

    /**
     * Returns true if the crystal effect of the LCD display will be drawn.
     * This feature could decrease the performance if you run it on
     * embedded devices because it will calculate a bitmap image where
     * each pixel will be calculated.
     * @return true if the crystal effect of the LCD display will be drawn
     */
    public boolean isLcdCrystalEnabled() { return null == lcdCrystalEnabled ? _lcdCrystalEnabled : lcdCrystalEnabled.get(); }
    /**
     * Defines if the crystal effect of the LCD display will be drawn.
     * This feature could decrease the performance if you run it on
     * embedded devices because it will calculate a bitmap image where
     * each pixel will be calculated.
     * @param ENABLED
     */
    public void setLcdCrystalEnabled(final boolean ENABLED) {
        if (null == lcdCrystalEnabled) {
            _lcdCrystalEnabled = ENABLED;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            lcdCrystalEnabled.set(ENABLED);
        }
    }
    public BooleanProperty lcdCrystalEnabledProperty() {
        if (null == lcdCrystalEnabled) {
            lcdCrystalEnabled = new BooleanPropertyBase(_lcdCrystalEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "lcdCrystalEnabled"; }
            };
        }
        return lcdCrystalEnabled;
    }

    /**
     * Returns true if effects like shadows will be drawn.
     * @return true if effects like shadows will be drawn
     */
    public boolean getShadowsEnabled() { return null == shadowsEnabled ? _shadowsEnabled : shadowsEnabled.get(); }
    /**
     * Defines if effects like shadows will be drawn.
     * @param ENABLED
     */
    public void setShadowsEnabled(final boolean ENABLED) {
        if (null == shadowsEnabled) {
            _shadowsEnabled = ENABLED;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            shadowsEnabled.set(ENABLED);
        }
    }
    public BooleanProperty shadowsEnabledProperty() {
        if (null == shadowsEnabled) {
            shadowsEnabled = new BooleanPropertyBase(_shadowsEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "shadowsEnabled"; }
            };
        }
        return shadowsEnabled;
    }

    /**
     * Returns the font that will be used to visualize the LCD
     * if the clock has a LCD display or for the LcdClockSkin.
     * The values are STANDARD, LCD, SLIM, DIGITAL_BOLD, ELEKTRA
     * @return the font that will be used to visualize the LCD
     */
    public LcdFont getLcdFont() { return null == lcdFont ? _lcdFont : lcdFont.get(); }
    /**
     * Defines the font that will be used to visualize the LCD value
     * if the clock has a LCD display or for the LcdClockSkin.
     * The values are STANDARD, LCD, SLIM, DIGITAL_BOLD, ELEKTRA
     * @param FONT
     */
    public void setLcdFont(final LcdFont FONT) {
        if (null == lcdFont) {
            _lcdFont = FONT;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            lcdFont.set(FONT);
        }
    }
    public ObjectProperty<LcdFont> lcdFontProperty() {
        if (null == lcdFont) {
            lcdFont  = new ObjectPropertyBase<LcdFont>(_lcdFont) {
                @Override protected void invalidated() { fireUpdateEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "lcdFont"; }
            };
            _lcdFont = null;
        }
        return lcdFont;
    }

    /**
     * Returns the Locale that will be used to format the date in
     * some ClockSkins.
     * @return the Locale that will be used to format the date
     */
    public Locale getLocale() { return null == locale ? _locale : locale.get(); }
    /**
     * Defines the Locale that will be used to format the date in
     * some ClockSkins.
     * @param LOCALE
     */
    public void setLocale(final Locale LOCALE) {
        if (null == locale) {
            _locale = LOCALE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            locale.set(LOCALE);
        }
    }
    public ObjectProperty<Locale> localeProperty() {
        if (null == locale) {
            locale  = new ObjectPropertyBase<Locale>(_locale) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "locale"; }
            };
            _locale = null;
        }
        return locale;
    }

    /**
     * Returns the location of the ticklabels. The values are
     * INSIDE and OUTSIDE. The location of the ticklabels has an
     * influence on the size of the tickmarks and length of the hands.
     * (NOT USED AT THE MOMENT)
     * @return the location of the ticklabels
     */
    public TickLabelLocation getTickLabelLocation() { return null == tickLabelLocation ? _tickLabelLocation : tickLabelLocation.get(); }
    /**
     * Defines the location of the ticklabels. The values are
     * INSIDE and OUTSIDE. The location of the ticklabels has an
     * influence on the size of the tickmarks and length of the hands.
     * (NOT USED AT THE MOMENT)
     * @param LOCATION
     */
    public void setTickLabelLocation(final TickLabelLocation LOCATION) {
        if (null == tickLabelLocation) {
            _tickLabelLocation = LOCATION;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelLocation.set(LOCATION);
        }
    }
    public ObjectProperty<TickLabelLocation> tickLabelLocationProperty() {
        if (null == tickLabelLocation) {
            tickLabelLocation  = new ObjectPropertyBase<TickLabelLocation>(_tickLabelLocation) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "tickLabelLocation"; }
            };
            _tickLabelLocation = null;
        }
        return tickLabelLocation;
    }

    /**
     * Returns true if the clock hands should be animated when set to
     * another time. This could be used to visualize the movement of the
     * clock hands when the time changes.
     * If set to true the clock can not be started with setRunning(true).
     * @return true if the clock hands should be animated when set to another time
     */
    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    /**
     * Defines if the clock hands should be animated when set to
     * another time. This could be used to visualize the movement of the
     * clock hands when the time changes.
     * If set to true the clock can not be started with setRunning(true).
     * @param ANIMATED
     */
    public void setAnimated(final boolean ANIMATED) {
        if (null == animated) {
            _animated = ANIMATED;
        } else {
            animated.set(ANIMATED);
        }
    }
    public BooleanProperty animatedProperty() {
        if (null == animated) { animated = new SimpleBooleanProperty(Clock.this, "animated", _animated); }
        return animated;
    }

    /**
     * Returns the duration in milliseconds that will be used to animate
     * the hands of the clock from the current time to the given time.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     * @return the duration in milliseconds that will be used to animate the clock hands
     */
    public long getAnimationDuration() { return animationDuration; }
    /**
     * Defines the duration in milliseconds that will be used to animate
     * the hands of the clock from the current time to the given time.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     * @param ANIMATION_DURATION
     */
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = Helper.clamp(10, 20000, ANIMATION_DURATION); }

    /**
     * Returns true if the control uses the given customFont to
     * render all text elements.
     * @return true if the control uses the given customFont
     */
    public boolean isCustomFontEnabled() { return null == customFontEnabled ? _customFontEnabled : customFontEnabled.get(); }
    /**
     * Defines if the control should use the given customFont
     * to render all text elements
     * @param ENABLED
     */
    public void setCustomFontEnabled(final boolean ENABLED) {
        if (null == customFontEnabled) {
            _customFontEnabled = ENABLED;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            customFontEnabled.set(ENABLED);
        }
    }
    public BooleanProperty customFontEnabledProperty() {
        if (null == customFontEnabled) {
            customFontEnabled = new BooleanPropertyBase(_customFontEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "customFontEnabled"; }
            };
        }
        return customFontEnabled;
    }

    /**
     * Returns the given custom Font that can be used to render
     * all text elements. To enable the custom font one has to set
     * customFontEnabled = true
     * @return the given custom Font
     */
    public Font getCustomFont() { return null == customFont ? _customFont : customFont.get(); }
    /**
     * Defines the custom font that can be used to render all
     * text elements. To enable the custom font one has to set
     * customFontEnabled = true
     * @param FONT
     */
    public void setCustomFont(final Font FONT) {
        if (null == customFont) {
            _customFont = FONT;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            customFont.set(FONT);
        }
    }
    public ObjectProperty<Font> customFontProperty() {
        if (null == customFont) {
            customFont = new ObjectPropertyBase<Font>() {
                @Override protected void invalidated() { fireUpdateEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Clock.this; }
                @Override public String getName() { return "customFont"; }
            };
            _customFont = null;
        }
        return customFont;
    }

    /**
     * Calling this method will check the current time against all Alarm
     * objects in alarms. The Alarm object will fire events in case the
     * time is after the alarm time.
     * @param TIME
     */
    private void checkAlarms(final ZonedDateTime TIME) {
        alarmsToRemove.clear();
        for (Alarm alarm : alarms) {
            final ZonedDateTime ALARM_TIME = alarm.getTime();
            switch (alarm.getRepetition()) {
                case ONCE:
                    if (TIME.isAfter(ALARM_TIME)) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(Clock.this, alarm));
                            alarm.executeCommand();
                        }
                        alarmsToRemove.add(alarm);
                    }
                    break;
                case HALF_HOURLY:
                    if ((ALARM_TIME.getMinute() == TIME.getMinute() ||
                        ALARM_TIME.plusMinutes(30).getMinute() == TIME.getMinute()) &&
                        ALARM_TIME.getSecond() == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(Clock.this, alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
                case HOURLY:
                    if (ALARM_TIME.getMinute() == TIME.getMinute() &&
                        ALARM_TIME.getSecond() == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(Clock.this, alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
                case DAILY:
                    if (ALARM_TIME.getHour()   == TIME.getHour() &&
                        ALARM_TIME.getMinute() == TIME.getMinute() &&
                        ALARM_TIME.getSecond() == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(Clock.this, alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
                case WEEKLY:
                    if (ALARM_TIME.getDayOfWeek() == TIME.getDayOfWeek() &&
                        ALARM_TIME.getHour()      == TIME.getHour() &&
                        ALARM_TIME.getMinute()    == TIME.getMinute() &&
                        ALARM_TIME.getSecond()    == TIME.getSecond()) {
                        if (alarm.isArmed()) {
                            fireAlarmEvent(new AlarmEvent(Clock.this, alarm));
                            alarm.executeCommand();
                        }
                    }
                    break;
            }
        }
        for (Alarm alarm : alarmsToRemove) {
            removeAlarm(alarm);
        }
    }

    /**
     * Calling this method will check for the current time of the day and
     * switches on/off the night mode.
     * @param TIME
     */
    private void checkForNight(final ZonedDateTime TIME) {
        int hour   = TIME.getHour();
        int minute = TIME.getMinute();

        if (0 <= hour && minute >= 0 && hour <= 5 && minute <= 59|| 17 <= hour && minute <= 59 && hour <= 23 && minute <= 59) {
            if(isNightMode()) return;
            setNightMode(true);
        } else {
            if (!isNightMode()) return;
            setNightMode(false);
        }
    }

    private void tick() { Platform.runLater(() -> {
        if (isAnimated()) return;
        ZonedDateTime oldTime = getTime();
        setTime(getTime().plus(Duration.ofMillis(updateInterval)));
        ZonedDateTime now = time.get();
        if (isAlarmsEnabled()) checkAlarms(now);
        if (isAutoNightMode()) checkForNight(now);
        if (getCheckSectionsForValue()) {
            int listSize = sections.size();
            for (int i = 0 ; i < listSize ; i++) { sections.get(i).checkForValue(LocalTime.from(now)); }
        }
        if (getCheckAreasForValue()) {
            int listSize = areas.size();
            for (int i = 0 ; i < listSize ; i++) { areas.get(i).checkForValue(LocalTime.from(now)); }
        }

        if (timeEventListenerList.isEmpty()) return;
        // Fire TimeEvents
        if (oldTime.getSecond() != now.getSecond()) fireTimeEvent(new TimeEvent(Clock.this, now, TimeEventType.SECOND));
        if (oldTime.getMinute() != now.getMinute()) fireTimeEvent(new TimeEvent(Clock.this, now, TimeEventType.MINUTE));
        if (oldTime.getHour() != now.getHour()) fireTimeEvent(new TimeEvent(Clock.this, now, TimeEventType.HOUR));
    }); }


    // ******************** Scheduled tasks ***********************************
    private synchronized static void enableTickExecutorService() {
        if (null == periodicTickExecutorService) {
            periodicTickExecutorService = new ScheduledThreadPoolExecutor(1, getThreadFactory("ClockTick", true));
        }
    }
    private synchronized void scheduleTickTask() {
        enableTickExecutorService();
        stopTask(periodicTickTask);

        updateInterval = (isDiscreteMinutes() && isDiscreteSeconds()) ? LONG_INTERVAL : SHORT_INTERVAL;
        periodicTickTask = periodicTickExecutorService.scheduleAtFixedRate(() -> tick(), 0, updateInterval, TimeUnit.MILLISECONDS);
    }

    private static ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }

    private void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }

    /**
     * Calling this method will stop all threads. This is needed when using
     * JavaFX on mobile devices when the device goes to sleep mode.
     */
    public void stop() {
        if (null != periodicTickTask) { stopTask(periodicTickTask); }
        if (null != periodicTickExecutorService) { periodicTickExecutorService.shutdownNow(); }
    }

    private void createShutdownHook() { Runtime.getRuntime().addShutdownHook(new Thread(() -> stop())); }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        switch(skinType) {
            case YOTA2     : return new ClockSkin(Clock.this);
            case LCD       : return new LcdClockSkin(Clock.this);
            case PEAR      : return new PearClockSkin(Clock.this);
            case PLAIN     : return new PlainClockSkin(Clock.this);
            case DB        : return new DBClockSkin(Clock.this);
            case FAT       : return new FatClockSkin(Clock.this);
            case ROUND_LCD : return new RoundLcdClockSkin(Clock.this);
            case SLIM      : return new SlimClockSkin(Clock.this);
            case MINIMAL   : return new MinimalClockSkin(Clock.this);
            case DIGITAL   : return new DigitalClockSkin(Clock.this);
            case TEXT      : return new TextClockSkin(Clock.this);
            case DESIGN    : return new DesignClockSkin(Clock.this);
            case INDUSTRIAL: return new IndustrialClockSkin(Clock.this);
            case TILE      : return new TileClockSkin(Clock.this);
            case CLOCK     :
            default        : return new ClockSkin(Clock.this);
        }
    }

    @Override public String getUserAgentStylesheet() {
        return getClass().getResource("clock.css").toExternalForm();
    }

    public ClockSkinType getSkinType() { return skinType; }
    public void setSkinType(ClockSkinType SKIN) {
        skinType = SKIN;
        switch(SKIN) {
            case YOTA2:
                setBackgroundPaint(Color.rgb(40, 42, 48));
                setHourTickMarkColor(Color.rgb(255, 255, 255));
                setMinuteTickMarkColor(Color.rgb(255, 255, 255, 0.5));
                setHourColor(Color.WHITE);
                setMinuteColor(Color.WHITE);
                setKnobColor(Color.WHITE);
                super.setSkin(new ClockSkin(Clock.this));
                break;
            case LCD:
                setBorderPaint(Color.WHITE);
                setForegroundPaint(Color.WHITE);
                super.setSkin(new LcdClockSkin(Clock.this));
                break;
            case PEAR:
                setBackgroundPaint(Color.BLACK);
                setHourColor(Color.WHITE);
                setMinuteColor(Color.WHITE);
                setSecondColor(Color.rgb(255, 165, 24));
                setHourTickMarkColor(Color.WHITE);
                setMinuteTickMarkColor(Color.rgb(115, 115, 115));
                setTickLabelColor(Color.WHITE);
                setDateColor(Color.WHITE);
                setDateVisible(true);
                setSecondsVisible(true);
                setTextVisible(false);
                setTitleVisible(false);
                super.setSkin(new PearClockSkin(Clock.this));
                break;
            case PLAIN:
                setBackgroundPaint(Color.rgb(29, 29, 29));
                setHourColor(Color.rgb(190, 190, 190));
                setMinuteColor(Color.rgb(190, 190, 190));
                setSecondColor(Color.rgb(0, 244, 0));
                setDateColor(Color.rgb(190, 190, 190));
                setSecondsVisible(true);
                setHourTickMarkColor(Color.rgb(240, 240, 240));
                setMinuteTickMarkColor(Color.rgb(240, 240, 240));
                super.setSkin(new PlainClockSkin(Clock.this));
                break;
            case DB:
                setDiscreteSeconds(false);
                setDiscreteMinutes(true);
                setSecondColor(Color.rgb(167, 0, 0));
                setSecondsVisible(true);

                super.setSkin(new DBClockSkin(Clock.this));
                break;
            case FAT:
                setDiscreteMinutes(true);
                super.setSkin(new FatClockSkin(Clock.this));
                break;
            case ROUND_LCD:
                setTextVisible(true);
                setDateVisible(true);
                super.setSkin(new RoundLcdClockSkin(Clock.this));
                break;
            case SLIM:
                setSecondsVisible(true);
                setDateVisible(true);
                setHourColor(Color.WHITE);
                setMinuteColor(Color.rgb(0,191,255));
                setSecondColor(Color.WHITE);
                setDateColor(Color.WHITE);
                super.setSkin(new SlimClockSkin(Clock.this));
                break;
            case MINIMAL:
                setBackgroundPaint(Color.rgb(255, 255, 255, 0.3));
                setMinuteColor(Color.rgb(59, 209, 255));
                setTextColor(Color.WHITE);
                setSecondColor(Color.rgb(255, 255, 255, 0.8));
                setSecondsVisible(true);
                setDateVisible(true);
                super.setSkin(new MinimalClockSkin(Clock.this));
                break;
            case DIGITAL:
                setTextVisible(true);
                setDateVisible(true);
                setSecondsVisible(true);
                super.setSkin(new DigitalClockSkin(Clock.this));
                break;
            case TEXT:
                setTextVisible(true);
                setDateVisible(true);
                setSecondsVisible(true);
                super.setSkin(new TextClockSkin(Clock.this));
                break;
            case DESIGN:
                setDiscreteHours(false);
                setDiscreteMinutes(false);
                setDiscreteSeconds(false);
                setTextVisible(false);
                setDateVisible(false);
                setSecondsVisible(false);
                setHourColor(Color.RED);
                setBackgroundPaint(Color.WHITE);
                super.setSkin(new DesignClockSkin(Clock.this));
                break;
            case INDUSTRIAL:
                setBackgroundPaint(Color.web("#efefef"));
                setHourColor(Color.web("#2a2a2a"));
                setMinuteColor(Color.web("#2a2a2a"));
                setSecondColor(Color.web("#d1222b"));
                setHourTickMarkColor(Color.BLACK);
                setMinuteTickMarkColor(Color.BLACK);
                setTickLabelsVisible(false);
                setTickLabelColor(Color.BLACK);
                setDateColor(Color.BLACK);
                setDateVisible(false);
                setSecondsVisible(true);
                setTextVisible(false);
                setTextColor(Color.BLACK);
                setTitleVisible(false);
                setTitleColor(Color.BLACK);
                setBorderPaint(Color.BLACK);
                setBorderWidth(5);
                super.setSkin(new IndustrialClockSkin(Clock.this));
                break;
            case TILE:
                setBackgroundPaint(Color.rgb(42,42,42));
                setHourColor(Color.rgb(238, 238, 238));
                setMinuteColor(Color.rgb(238, 238, 238));
                setSecondColor(Color.rgb(238, 238, 238));
                setKnobColor(Color.rgb(238, 238, 238));
                setHourTickMarkColor(Color.rgb(238, 238, 238));
                setMinuteTickMarkColor(Color.rgb(238, 238, 238));
                setDateColor(Color.rgb(238, 238, 238));
                setDateVisible(false);
                setSecondsVisible(false);
                setTextVisible(false);
                setTextColor(Color.rgb(238, 238, 238));
                setTitleVisible(true);
                setTitleColor(Color.rgb(238, 238, 238));
                super.setSkin(new TileClockSkin(Clock.this));
                break;
            case CLOCK:
                setHourTickMarkColor(Color.rgb(255, 255, 255));
                setMinuteTickMarkColor(Color.rgb(255, 255, 255, 0.5));
                setHourColor(Color.WHITE);
                setMinuteColor(Color.WHITE);
                setKnobColor(Color.WHITE);
                setKnobColor(Color.WHITE);
            default:
                super.setSkin(new ClockSkin(Clock.this));
                break;
        }
        fireUpdateEvent(RESIZE_EVENT);
    }


    // ******************** Event handling ************************************
    public void setOnUpdate(final UpdateEventListener LISTENER) { addUpdateEventListener(LISTENER); }
    public void addUpdateEventListener(final UpdateEventListener LISTENER) { if(!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeUpdateEventListener(final UpdateEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireUpdateEvent(final UpdateEvent EVENT) {
        int listSize = listenerList.size();
        for (int i = 0 ; i < listSize ; i++) { listenerList.get(i).onUpdateEvent(EVENT); }
    }


    public void setOnAlarm(final AlarmEventListener LISTENER) { addAlarmEventListener(LISTENER); }
    public void addAlarmEventListener(final AlarmEventListener LISTENER) { if (!alarmListenerList.contains(LISTENER)) alarmListenerList.add(LISTENER); }
    public void removeAlarmEventListener(final AlarmEventListener LISTENER) { if (alarmListenerList.contains(LISTENER)) alarmListenerList.remove(LISTENER); }

    public void fireAlarmEvent(final AlarmEvent EVENT) {
        int listSize = alarmListenerList.size();
        for (int i = 0 ; i < listSize ; i++) { alarmListenerList.get(i).onAlarmEvent(EVENT); }
    }


    public void setOnTimeEvent(final TimeEventListener LISTENER) { addTimeEventListener(LISTENER); }
    public void addTimeEventListener(final TimeEventListener LISTENER) { if (!timeEventListenerList.contains(LISTENER)) timeEventListenerList.add(LISTENER); }
    public void removeTimeEventListener(final TimeEventListener LISTENER) { if (timeEventListenerList.contains(LISTENER)) timeEventListenerList.remove(LISTENER); }

    public void fireTimeEvent(final TimeEvent EVENT) {
        int listSize = timeEventListenerList.size();
        for (int i = 0 ; i < listSize ; i++) { timeEventListenerList.get(i).onTimeEvent(EVENT); }
    }
}
