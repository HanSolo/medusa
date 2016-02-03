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
import eu.hansolo.medusa.events.UpdateEvent;
import eu.hansolo.medusa.events.UpdateEvent.EventType;
import eu.hansolo.medusa.events.UpdateEventListener;
import eu.hansolo.medusa.skins.*;
import eu.hansolo.medusa.tools.Helper;
import eu.hansolo.medusa.tools.TimeSectionComparator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.LocalTime;
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


/**
 * Created by hansolo on 28.01.16.
 */
public class Clock extends Control {
    public enum ClockSkinType { CLOCK, YOTA2, LCD, PEAR, PLAIN, DB, FAT, ROUND_LCD }

    public  static final int                  SHORT_INTERVAL   = 20;
    public  static final int                  LONG_INTERVAL    = 1000;
    public  static final Color                DARK_COLOR       = Color.rgb(36, 36, 36);
    public  static final Color                BRIGHT_COLOR     = Color.rgb(223, 223, 223);
    private        final UpdateEvent          REDRAW_EVENT     = new UpdateEvent(Clock.this, EventType.REDRAW);
    private        final UpdateEvent          VISIBILITY_EVENT = new UpdateEvent(Clock.this, EventType.VISIBILITY);
    private        final UpdateEvent          LCD_EVENT        = new UpdateEvent(Clock.this, EventType.LCD);
    private        final UpdateEvent          RECALC_EVENT     = new UpdateEvent(Clock.this, EventType.RECALC);
    private        final UpdateEvent          SECTION_EVENT    = new UpdateEvent(Clock.this, UpdateEvent.EventType.SECTION);
    private        final UpdateEvent          FINISHED_EVENT   = new UpdateEvent(Clock.this, UpdateEvent.EventType.FINISHED);

    private volatile ScheduledFuture<?>       periodicTickTask;
    private static   ScheduledExecutorService periodicTickExecutorService;

    // Alarm events
    private List<UpdateEventListener>         listenerList      = new CopyOnWriteArrayList();
    private List<AlarmEventListener>          alarmListenerList = new CopyOnWriteArrayList();

    private ObjectProperty<ZonedDateTime>     time;
    private LongProperty                      currentTime;
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
    private ObservableList<TimeSection>       areas;
    private boolean                           _areasVisible;
    private BooleanProperty                   areasVisible;
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
    public Clock(final ClockSkinType SKIN, final ZonedDateTime TIME) {
        skinType = SKIN;
        getStyleClass().add("clock");

        init(TIME);
    }

    private void init(final ZonedDateTime TIME) {
        time                    = new ObjectPropertyBase<ZonedDateTime>(TIME) {
            @Override public void set(final ZonedDateTime TIME) {
                super.set(TIME);

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
        currentTime             = new SimpleLongProperty(Clock.this, "currentTime", time.get().toEpochSecond());
        timeline                = new Timeline();
        timeline.setOnFinished(e -> fireUpdateEvent(FINISHED_EVENT));
        updateInterval          = LONG_INTERVAL;
        _checkSectionsForValue  = false;
        _checkAreasForValue     = false;
        sections                = FXCollections.observableArrayList();
        _secondsVisible         = false;
        areas                   = FXCollections.observableArrayList();
        _areasVisible           = false;
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
    }


    // ******************** Methods *******************************************
    public ZonedDateTime getTime() { return time.get(); }
    public void setTime(ZonedDateTime TIME) { time.set(TIME); }
    public ObjectProperty<ZonedDateTime> timeProperty() { return time; }

    public long getCurrentTime() { return currentTime.get(); }
    public ReadOnlyLongProperty currentTimeProperty() { return currentTime; }

    public String getTitle() { return null == title ? _title : title.get(); }
    public void setTitle(String TITLE) {
        if (null == title) {
            _title = TITLE;
        } else {
            title.set(TITLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public StringProperty titleProperty() {
        if (null == title) { title = new SimpleStringProperty(Clock.this, "title", _title); }
        return title;
    }
    
    public String getText() { return null == text ? _text : text.get(); }
    public void setText(String TEXT) { 
        if (null == text) {
            _text = TEXT;
        } else {
            text.set(TEXT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public StringProperty textProperty() {
        if (null == text) { text = new SimpleStringProperty(Clock.this, "text", _text); }
        return text; 
    }

    public boolean getCheckSectionsForValue() { return null == checkSectionsForValue ? _checkSectionsForValue : checkSectionsForValue.get(); }
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

    public boolean getCheckAreasForValue() { return null == checkAreasForValue ? _checkAreasForValue : checkAreasForValue.get(); }
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
    
    public ObservableList<TimeSection> getSections() { return sections; }
    public void setSections(final List<TimeSection> SECTIONS) {
        sections.setAll(SECTIONS);
        Collections.sort(sections, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    public void setSections(final TimeSection... SECTIONS) { setSections(Arrays.asList(SECTIONS)); }
    public void addSection(final TimeSection SECTION) {
        if (null == SECTION) return;
        sections.add(SECTION);
        Collections.sort(sections, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    public void removeSection(final TimeSection SECTION) {
        if (null == SECTION) return;
        sections.remove(SECTION);
        Collections.sort(sections, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    public void clearSections() {
        sections.clear();
        fireUpdateEvent(SECTION_EVENT);
    }

    public boolean getSectionsVisible() { return null == sectionsVisible ? _sectionsVisible : sectionsVisible.get(); }
    public void setSectionsVisible(final boolean VISIBLE) {
        if (null == sectionsVisible) {
            _sectionsVisible = VISIBLE;
        } else {
            sectionsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty sectionsVisibleProperty() {
        if (null == sectionsVisible) { sectionsVisible = new SimpleBooleanProperty(Clock.this, "sectionsVisible", _sectionsVisible); }
        return sectionsVisible;
    }

    public ObservableList<TimeSection> getAreas() { return areas; }
    public void setAreas(final List<TimeSection> AREAS) {
        areas.setAll(AREAS);
        Collections.sort(areas, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    public void setAreas(final TimeSection... AREAS) { setAreas(Arrays.asList(AREAS)); }
    public void addArea(final TimeSection AREA) {
        if (null == AREA) return;
        areas.add(AREA);
        Collections.sort(areas, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    public void removeArea(final TimeSection AREA) {
        if (null == AREA) return;
        areas.remove(AREA);
        Collections.sort(areas, new TimeSectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    public void clearAreas() {
        areas.clear();
        fireUpdateEvent(SECTION_EVENT);
    }

    public boolean getAreasVisible() { return null == areasVisible ? _areasVisible : areasVisible.get(); }
    public void setAreasVisible(final boolean VISIBLE) {
        if (null == areasVisible) {
            _areasVisible = VISIBLE;
        } else {
            areasVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty areasVisibleProperty() {
        if (null == areasVisible) { areasVisible = new SimpleBooleanProperty(Clock.this, "areasVisible", _areasVisible); }
        return areasVisible;
    }

    public boolean isDiscreteSeconds() { return null == discreteSeconds ? _discreteSeconds : discreteSeconds.get(); }
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
                @Override public void set(boolean DISCRETE) {
                    super.set(DISCRETE);
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

    public boolean isDiscreteMinutes() { return null == discreteMinutes ? _discreteMinutes : discreteMinutes.get(); }
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
                @Override public void set(boolean DISCRETE) {
                    super.set(DISCRETE);
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

    public boolean isDiscreteHours() { return null == discreteHours ? _discreteHours : discreteHours.get(); }
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

    public boolean isSecondsVisible() { return null == secondsVisible ? _secondsVisible : secondsVisible.get(); }
    public void setSecondsVisible(boolean VISIBLE) { 
        if (null == secondsVisible) {
            _secondsVisible = VISIBLE;
        } else {
            secondsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty secondsVisibleProperty() { 
        if (null == secondsVisible) { secondsVisible = new SimpleBooleanProperty(Clock.this, "secondsVisible", _secondsVisible); }    
        return secondsVisible; 
    }

    public boolean isTitleVisible() { return null == titleVisible ? _titleVisible : titleVisible.get(); }
    public void setTitleVisible(final boolean VISIBLE) {
        if (null == titleVisible) {
            _titleVisible = VISIBLE;
        } else {
            titleVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty titleVisibleProperty() {
        if (null == titleVisible) { titleVisible = new SimpleBooleanProperty(Clock.this, "titleVisible", _titleVisible); }
        return titleVisible;
    }

    public boolean isTextVisible() { return null == textVisible ? _textVisible : textVisible.get(); }
    public void setTextVisible(final boolean VISIBLE) {
        if (null == textVisible) {
            _textVisible = VISIBLE;
        } else {
            textVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty textVisibleProperty() {
        if (null == textVisible) { textVisible = new SimpleBooleanProperty(Clock.this, "textVisible", _textVisible); }
        return textVisible;
    }

    public boolean isDateVisible() { return null == dateVisible ? _dateVisible : dateVisible.get(); }
    public void setDateVisible(final boolean VISIBLE) {
        if (null == dateVisible) {
            _dateVisible = VISIBLE;
        } else {
            dateVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty dateVisibleProperty() {
        if (null == dateVisible) { dateVisible = new SimpleBooleanProperty(Clock.this, "dateVisible", _dateVisible); }
        return dateVisible;
    }

    public boolean isNightMode() { return null == nightMode ? _nightMode : nightMode.get(); }
    public void setNightMode(boolean MODE) { 
        if (null == nightMode) {
            _nightMode = MODE;
        } else {
            nightMode.set(MODE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty nightModeProperty() { 
        if (null == nightMode) { nightMode = new SimpleBooleanProperty(Clock.this, "nightMode", _nightMode); }    
        return nightMode; 
    }

    public boolean isRunning() { return null == running ? _running : running.get(); }
    public void setRunning(boolean RUNNING) { 
        if (null == running) {
            _running = RUNNING;
            if (RUNNING && !isAnimated()) { scheduleTickTask(); } else { stopTask(periodicTickTask); }
        } else {
            running.set(RUNNING);
        }
    }
    public BooleanProperty runningProperty() { 
        if (null == running) { new BooleanPropertyBase(_running) {
            @Override public void set(boolean RUNNING) {
                super.set(RUNNING);
                if (RUNNING && !isAnimated()) { scheduleTickTask(); } else { stopTask(periodicTickTask); }
            }
            @Override public Object getBean() { return Clock.this; }
            @Override public String getName() { return "running"; }
        }; }    
        return running; 
    }

    public boolean isAutoNightMode() { return null == autoNightMode ? _autoNightMode : autoNightMode.get(); }
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

    public Paint getBackgroundPaint() { return null == backgroundPaint ? _backgroundPaint : backgroundPaint.get(); }
    public void setBackgroundPaint(final Paint PAINT) {
        if (null == backgroundPaint) {
            _backgroundPaint = PAINT;
        } else {
            backgroundPaint.set(PAINT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Paint> backgroundPaintProperty() {
        if (null == backgroundPaint) { backgroundPaint = new SimpleObjectProperty<>(Clock.this, "backgroundPaint", _backgroundPaint); }
        return backgroundPaint;
    }

    public Paint getBorderPaint() { return null == borderPaint ? _borderPaint : borderPaint.get(); }
    public void setBorderPaint(final Paint PAINT) {
        if (null == borderPaint) {
            _borderPaint = PAINT;
        } else {
            borderPaint.set(PAINT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Paint> borderPaintProperty() {
        if (null == borderPaint) { borderPaint = new SimpleObjectProperty<>(Clock.this, "borderPaint", _borderPaint); }
        return borderPaint;
    }

    public Paint getForegroundPaint() { return null == foregroundPaint ? _foregroundPaint : foregroundPaint.get(); }
    public void setForegroundPaint(final Paint PAINT) {
        if (null == foregroundPaint) {
            _foregroundPaint = PAINT;
        } else {
            foregroundPaint.set(PAINT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Paint> foregroundPaintProperty() {
        if (null == foregroundPaint) { foregroundPaint = new SimpleObjectProperty<>(Clock.this, "foregroundPaint", _foregroundPaint); }
        return foregroundPaint;
    }

    public Color getTitleColor() { return null == titleColor ? _titleColor : titleColor.get(); }
    public void setTitleColor(final Color COLOR) {
        if (null == titleColor) {
            _titleColor = COLOR;
        } else {
            titleColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> titleColorProperty() {
        if (null == titleColor) { titleColor = new SimpleObjectProperty<>(Clock.this, "titleColor", _titleColor); }
        return titleColor;
    }

    public Color getTextColor() { return null == textColor ? _textColor : textColor.get(); }
    public void setTextColor(final Color COLOR) {
        if (null == textColor) {
            _textColor = COLOR;
        } else {
            textColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> textColorProperty() {
        if (null == textColor) { textColor = new SimpleObjectProperty<>(Clock.this, "textColor", _textColor); }
        return textColor;
    }

    public Color getDateColor() { return null == dateColor ? _dateColor : dateColor.get(); }
    public void setDateColor(final Color COLOR) {
        if (null == dateColor) {
            _dateColor = COLOR;
        } else {
            dateColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> dateColorProperty() {
        if (null == dateColor) { dateColor = new SimpleObjectProperty<>(Clock.this, "dateColor", _dateColor); }
        return dateColor;
    }

    public Color getHourTickMarkColor() { return null == hourTickMarkColor ? _hourTickMarkColor : hourTickMarkColor.get(); }
    public void setHourTickMarkColor(final Color COLOR) {
        if (null == hourTickMarkColor) {
            _hourTickMarkColor = COLOR;
        } else {
            hourTickMarkColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> hourTickMarkColorProperty() {
        if (null == hourTickMarkColor) { hourTickMarkColor = new SimpleObjectProperty<>(Clock.this, "hourTickMarkColor", _hourTickMarkColor); }
        return hourTickMarkColor;
    }

    public Color getMinuteTickMarkColor() { return null == minuteTickMarkColor ? _minuteTickMarkColor : minuteTickMarkColor.get(); }
    public void setMinuteTickMarkColor(final Color COLOR) {
        if (null == minuteTickMarkColor) {
            _minuteTickMarkColor = COLOR;
        } else {
            minuteTickMarkColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> minuteTickMarkColorProperty() {
        if (null == minuteTickMarkColor) { minuteTickMarkColor = new SimpleObjectProperty<>(Clock.this, "minuteTickMarkColor", _minuteTickMarkColor); }
        return minuteTickMarkColor;
    }

    public Color getTickLabelColor() { return null == tickLabelColor ? _tickLabelColor : tickLabelColor.get(); }
    public void setTickLabelColor(final Color COLOR) {
        if (null == tickLabelColor) {
            _tickLabelColor = COLOR;
        } else {
            tickLabelColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> tickLabelColorProperty() {
        if (null == tickLabelColor) { tickLabelColor = new SimpleObjectProperty<>(Clock.this, "tickLabelColor", _tickLabelColor); }
        return tickLabelColor;
    }

    public Color getAlarmColor() { return null == alarmColor ? _alarmColor : alarmColor.get(); }
    public void setAlarmColor(final Color COLOR) {
        if (null == alarmColor) {
            _alarmColor = COLOR;
        } else {
            alarmColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> alarmColorProperty() {
        if (null == alarmColor) { alarmColor = new SimpleObjectProperty<>(Clock.this, "alarmColor", _alarmColor); }
        return alarmColor;
    }

    public boolean isHourTickMarksVisible() { return null == hourTickMarksVisible ? _hourTickMarksVisible : hourTickMarksVisible.get(); }
    public void setHourTickMarksVisible(final boolean VISIBLE) {
        if (null == hourTickMarksVisible) {
            _hourTickMarksVisible = VISIBLE;
        } else {
            hourTickMarksVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty hourTickMarksVisibleProperty() {
        if (null == hourTickMarksVisible) { hourTickMarksVisible = new SimpleBooleanProperty(Clock.this, "hourTickMarksVisible", _hourTickMarksVisible); }
        return hourTickMarksVisible;
    }

    public boolean isMinuteTickMarksVisible() { return null == minuteTickMarksVisible ? _minuteTickMarksVisible : minuteTickMarksVisible.get(); }
    public void setMinuteTickMarksVisible(final boolean VISIBLE) {
        if (null == minuteTickMarksVisible) {
            _minuteTickMarksVisible = VISIBLE;
        } else {
            minuteTickMarksVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty minuteTickMarksVisibleProperty() {
        if (null == minuteTickMarksVisible) { minuteTickMarksVisible = new SimpleBooleanProperty(Clock.this, "minuteTickMarksVisible", _minuteTickMarksVisible); }
        return minuteTickMarksVisible;
    }

    public boolean isTickLabelsVisible() { return null == tickLabelsVisible ? _tickLabelsVisible : tickLabelsVisible.get(); }
    public void setTickLabelsVisible(final boolean VISIBLE) {
        if (null == tickLabelsVisible) {
            _tickLabelsVisible = VISIBLE;
        } else {
            tickLabelsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty tickLabelsVisibleProperty() {
        if (null == tickLabelsVisible) { tickLabelsVisible = new SimpleBooleanProperty(Clock.this, "tickLabelsVisible", _tickLabelsVisible); }
        return tickLabelsVisible;
    }

    public Color getHourColor() { return null == hourColor ? _hourColor : hourColor.get(); }
    public void setHourColor(final Color COLOR) {
        if (null == hourColor) {
            _hourColor = COLOR;
        } else {
            hourColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> hourColorProperty() {
        if (null == hourColor) { hourColor = new SimpleObjectProperty<>(Clock.this, "hourColor", _hourColor); }
        return hourColor;
    }

    public Color getMinuteColor() { return null == minuteColor ? _minuteColor : minuteColor.get(); }
    public void setMinuteColor(final Color COLOR) {
        if (null == minuteColor) {
            _minuteColor = COLOR;
        } else {
            minuteColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> minuteColorProperty() {
        if (null == minuteColor) { minuteColor = new SimpleObjectProperty<>(Clock.this, "minuteColor", _minuteColor); }
        return minuteColor;
    }

    public Color getSecondColor() { return null == secondColor ? _secondColor : secondColor.get(); }
    public void setSecondColor(final Color COLOR) {
        if (null == secondColor) {
            _secondColor = COLOR;
        } else {
            secondColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> secondColorProperty() {
        if (null == secondColor) { secondColor = new SimpleObjectProperty<>(Clock.this, "secondColor", _secondColor); }
        return secondColor;
    }

    public Color getKnobColor() { return null == knobColor ? _knobColor : knobColor.get(); }
    public void setKnobColor(final Color COLOR) {
        if (null == knobColor) {
            _knobColor = COLOR;
        } else {
            knobColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> knobColorProperty() {
        if (null == knobColor) { knobColor = new SimpleObjectProperty<>(Clock.this, "knobColor", _knobColor); }
        return knobColor;
    }

    public LcdDesign getLcdDesign() { return null == lcdDesign ? _lcdDesign : lcdDesign.get(); }
    public void setLcdDesign(final LcdDesign DESIGN) {
        if (null == lcdDesign) {
            _lcdDesign = DESIGN;
        } else {
            lcdDesign.set(DESIGN);
        }
        fireUpdateEvent(LCD_EVENT);
    }
    public ObjectProperty<LcdDesign> lcdDesignProperty() {
        if (null == lcdDesign) { lcdDesign = new SimpleObjectProperty<>(Clock.this, "lcdDesign", _lcdDesign); }
        return lcdDesign;
    }

    public boolean isAlarmsEnabled() { return null == alarmsEnabled ? _alarmsEnabled : alarmsEnabled.get(); }
    public void setAlarmsEnabled(final boolean CHECK) {
        if (null == alarmsEnabled) {
            _alarmsEnabled = CHECK;
        } else {
            alarmsEnabled.set(CHECK);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty alarmsEnabledProperty() {
        if (null == alarmsEnabled) { alarmsEnabled = new SimpleBooleanProperty(Clock.this, "alarmsEnabled", _alarmsEnabled); }
        return alarmsEnabled;
    }

    public boolean isAlarmsVisible() { return null == alarmsVisible ? _alarmsVisible : alarmsVisible.get(); }
    public void setAlarmsVisible(final boolean VISIBLE) {
        if (null == alarmsVisible) {
            _alarmsVisible = VISIBLE;
        } else {
            alarmsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty alarmsVisibleProperty() {
        if (null == alarmsVisible) { alarmsVisible = new SimpleBooleanProperty(Clock.this, "alarmsVisible", _alarmsVisible); }
        return alarmsVisible;
    }

    public ObservableList<Alarm> getAlarms() { return alarms; }
    public void setAlarms(final List<Alarm> ALARMS) { alarms.setAll(ALARMS); }
    public void setAlarms(final Alarm... ALARMS) { setAlarms(Arrays.asList(ALARMS)); }
    public void addAlarm(final Alarm ALARM) { if (!alarms.contains(ALARM)) alarms.add(ALARM); }
    public void removeAlarm(final Alarm ALARM) { if (alarms.contains(ALARM)) alarms.remove(ALARM); }
    public void clearAlarms() { alarms.clear(); }

    public boolean isLcdCrystalEnabled() { return null == lcdCrystalEnabled ? _lcdCrystalEnabled : lcdCrystalEnabled.get(); }
    public void setLcdCrystalEnabled(final boolean ENABLED) {
        if (null == lcdCrystalEnabled) {
            _lcdCrystalEnabled = ENABLED;
        } else {
            lcdCrystalEnabled.set(ENABLED);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty lcdCrystalEnabledProperty() {
        if (null == lcdCrystalEnabled) { lcdCrystalEnabled = new SimpleBooleanProperty(Clock.this, "lcdCrystalEnabled", _lcdCrystalEnabled); }
        return lcdCrystalEnabled;
    }

    public boolean getShadowsEnabled() { return null == shadowsEnabled ? _shadowsEnabled : shadowsEnabled.get(); }
    public void setShadowsEnabled(final boolean ENABLED) {
        if (null == shadowsEnabled) {
            _shadowsEnabled = ENABLED;
        } else {
            shadowsEnabled.set(ENABLED);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty shadowsEnabledProperty() {
        if (null == shadowsEnabled) { shadowsEnabled = new SimpleBooleanProperty(Clock.this, "shadowsEnabled", _shadowsEnabled); }
        return shadowsEnabled;
    }

    public LcdFont getLcdFont() { return null == lcdFont ? _lcdFont : lcdFont.get(); }
    public void setLcdFont(final LcdFont FONT) {
        if (null == lcdFont) {
            _lcdFont = FONT;
        } else {
            lcdFont.set(FONT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<LcdFont> lcdFontProperty() {
        if (null == lcdFont) { lcdFont = new SimpleObjectProperty<>(Clock.this, "lcdFont", _lcdFont); }
        return lcdFont;
    }

    public Locale getLocale() { return null == locale ? _locale : locale.get(); }
    public void setLocale(final Locale LOCALE) {
        if (null == locale) {
            _locale = LOCALE;
        } else {
            locale.set(LOCALE);
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public ObjectProperty<Locale> localeProperty() {
        if (null == locale) { locale = new SimpleObjectProperty<>(Clock.this, "locale", _locale); }
        return locale;
    }

    public TickLabelLocation getTickLabelLocation() { return null == tickLabelLocation ? _tickLabelLocation : tickLabelLocation.get(); }
    public void setTickLabelLocation(final TickLabelLocation LOCATION) {
        if (null == tickLabelLocation) {
            _tickLabelLocation = LOCATION;
        } else {
            tickLabelLocation.set(LOCATION);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<TickLabelLocation> tickLabelLocationProperty() {
        if (null == tickLabelLocation) { tickLabelLocation = new SimpleObjectProperty<>(Clock.this, "tickLabelLocation", _tickLabelLocation); }
        return tickLabelLocation;
    }

    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
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

    public long getAnimationDuration() { return animationDuration; }
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = Helper.clamp(10l, 20000l, ANIMATION_DURATION); }


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
                case HOURLY:
                    if (ALARM_TIME.getHour()   == TIME.getMinute() &&
                        ALARM_TIME.getMinute() == TIME.getSecond()) {
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
    }); }


    // ******************** Scheduled tasks ***********************************
    private synchronized static void enableTickExecutorService() {
        if (null == periodicTickExecutorService) {
            periodicTickExecutorService = new ScheduledThreadPoolExecutor(1, getThreadFactory("ClockTick", false));
        }
    }
    private synchronized void scheduleTickTask() {
        enableTickExecutorService();
        stopTask(periodicTickTask);

        updateInterval   = (isDiscreteMinutes() && isDiscreteSeconds()) ? LONG_INTERVAL : SHORT_INTERVAL;

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

    public void stop() {
        if (null == periodicTickTask) return;
        stopTask(periodicTickTask);
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        switch(skinType) {
            case YOTA2    : return new ClockSkin(Clock.this);
            case LCD      : return new LcdClockSkin(Clock.this);
            case PEAR     : return new PearClockSkin(Clock.this);
            case PLAIN    : return new PlainClockSkin(Clock.this);
            case DB       : return new DBClockSkin(Clock.this);
            case FAT      : return new FatClockSkin(Clock.this);
            case ROUND_LCD: return new RoundLcdClockSkin(Clock.this);
            case CLOCK    :
            default       : return new ClockSkin(Clock.this);
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
                break;
            case FAT:
                setDiscreteMinutes(true);
                break;
            case ROUND_LCD:
                setTextVisible(true);
                setDateVisible(true);
                break;
            case CLOCK:
            default:
                super.setSkin(new ClockSkin(Clock.this));
                break;
        }
    }


    // ******************** Event handling ************************************
    public void setOnUpdate(final UpdateEventListener LISTENER) { addUpdateEventListener(LISTENER); }
    public void addUpdateEventListener(final UpdateEventListener LISTENER) { listenerList.add(LISTENER); }
    public void removeUpdateEventListener(final UpdateEventListener LISTENER) { listenerList.remove(LISTENER); }

    public void fireUpdateEvent(final UpdateEvent EVENT) {
        int listSize = listenerList.size();
        for (int i = 0 ; i < listSize ; i++) { listenerList.get(i).onUpdateEvent(EVENT); }
    }


    public void setOnAlarm(final AlarmEventListener LISTENER) { addAlarmEventListener(LISTENER); }
    public void addAlarmEventListener(final AlarmEventListener LISTENER) { alarmListenerList.add(LISTENER); }
    public void removeAlarmEventListener(final AlarmEventListener LISTENER) { alarmListenerList.remove(LISTENER); }

    public void fireAlarmEvent(final AlarmEvent EVENT) {
        int listSize = alarmListenerList.size();
        for (int i = 0 ; i < listSize ; i++) { alarmListenerList.get(i).onAlarmEvent(EVENT); }
    }
}
