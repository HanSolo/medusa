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

import eu.hansolo.medusa.events.UpdateEvent;
import eu.hansolo.medusa.events.UpdateEventListener;
import eu.hansolo.medusa.skins.*;
import eu.hansolo.medusa.tools.Data;
import eu.hansolo.medusa.tools.GradientLookup;
import eu.hansolo.medusa.tools.Helper;
import eu.hansolo.medusa.tools.MarkerComparator;
import eu.hansolo.medusa.tools.MovingAverage;
import eu.hansolo.medusa.tools.SectionComparator;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.util.Duration;


/**
 * Created by hansolo on 11.12.15.
 */
public class Gauge extends Control {
    public enum NeedleType { BIG, FAT, STANDARD, SCIENTIFIC, AVIONIC, VARIOMETER }

    public enum NeedleShape { ANGLED, ROUND, FLAT }

    public enum NeedleSize {
        THIN(0.015),
        STANDARD(0.025),
        THICK(0.05);

        public final double FACTOR;

        NeedleSize(final double FACTOR) {
            this.FACTOR = FACTOR;
        }
    }

    public enum NeedleBehavior { STANDARD, OPTIMIZED }

    public enum KnobType { STANDARD, PLAIN, METAL, FLAT }

    public enum LedType { STANDARD, FLAT }

    public enum ScaleDirection { CLOCKWISE, COUNTER_CLOCKWISE, LEFT_TO_RIGHT, RIGHT_TO_LEFT, BOTTOM_TO_TOP, TOP_TO_BOTTOM }

    public enum SkinType {
        AMP, BULLET_CHART, DASHBOARD, FLAT, GAUGE, INDICATOR, KPI,
        MODERN, SIMPLE, SLIM, SPACE_X, QUARTER, HORIZONTAL, VERTICAL,
        LCD, TINY, BATTERY, LEVEL, LINEAR, DIGITAL, SIMPLE_DIGITAL, SECTION,
        BAR, WHITE, CHARGE, SIMPLE_SECTION, TILE_KPI, TILE_TEXT_KPI, TILE_SPARK_LINE
    }

    public static final  Color   DARK_COLOR          = Color.rgb(36, 36, 36);
    public static final  Color   BRIGHT_COLOR        = Color.rgb(223, 223, 223);
    private static final long    LED_BLINK_INTERVAL  = 500l;
    private static final int     MAX_NO_OF_DECIMALS  = 3;

    public final  ButtonEvent    BTN_PRESSED_EVENT   = new ButtonEvent(ButtonEvent.BTN_PRESSED);
    public final  ButtonEvent    BTN_RELEASED_EVENT  = new ButtonEvent(ButtonEvent.BTN_RELEASED);
    private final ThresholdEvent EXCEEDED_EVENT      = new ThresholdEvent(ThresholdEvent.THRESHOLD_EXCEEDED);
    private final ThresholdEvent UNDERRUN_EVENT      = new ThresholdEvent(ThresholdEvent.THRESHOLD_UNDERRUN);
    private final UpdateEvent    RECALC_EVENT        = new UpdateEvent(Gauge.this, UpdateEvent.EventType.RECALC);
    private final UpdateEvent    REDRAW_EVENT        = new UpdateEvent(Gauge.this, UpdateEvent.EventType.REDRAW);
    private final UpdateEvent    RESIZE_EVENT        = new UpdateEvent(Gauge.this, UpdateEvent.EventType.RESIZE);
    private final UpdateEvent    LED_EVENT           = new UpdateEvent(Gauge.this, UpdateEvent.EventType.LED);
    private final UpdateEvent    LCD_EVENT           = new UpdateEvent(Gauge.this, UpdateEvent.EventType.LCD);
    private final UpdateEvent    VISIBILITY_EVENT    = new UpdateEvent(Gauge.this, UpdateEvent.EventType.VISIBILITY);
    private final UpdateEvent    INTERACTIVITY_EVENT = new UpdateEvent(Gauge.this, UpdateEvent.EventType.INTERACTIVITY);
    private final UpdateEvent    FINISHED_EVENT      = new UpdateEvent(Gauge.this, UpdateEvent.EventType.FINISHED);
    private final UpdateEvent    SECTION_EVENT       = new UpdateEvent(Gauge.this, UpdateEvent.EventType.SECTION);
    private final UpdateEvent    ALERT_EVENT         = new UpdateEvent(Gauge.this, UpdateEvent.EventType.ALERT);
    private final UpdateEvent    VALUE_EVENT         = new UpdateEvent(Gauge.this, UpdateEvent.EventType.VALUE);

    private static volatile Future               blinkFuture;
    private static ScheduledExecutorService      blinkService = new ScheduledThreadPoolExecutor(1, Helper.getThreadFactory("BlinkTask", true));
    private static volatile Callable<Void>       blinkTask;

    // Update events
    private List<UpdateEventListener>            listenerList = new CopyOnWriteArrayList<>();

    // Data related
    private DoubleProperty                       value;
    private DoubleProperty                       oldValue;      // last value
    private DoubleProperty                       currentValue;
    private DoubleProperty                       formerValue;   // last current value
    private double                               _minValue;
    private DoubleProperty                       minValue;
    private double                               _maxValue;
    private DoubleProperty                       maxValue;
    private double                               _range;
    private DoubleProperty                       range;
    private double                               _threshold;
    private DoubleProperty                       threshold;
    private String                               _title;
    private StringProperty                       title;
    private String                               _subTitle;
    private StringProperty                       subTitle;
    private String                               _unit;
    private StringProperty                       unit;
    private boolean                              _averagingEnabled;
    private BooleanProperty                      averagingEnabled;
    private int                                  _averagingPeriod;
    private IntegerProperty                      averagingPeriod;
    private MovingAverage                        movingAverage;
    private ObservableList<Section>              sections;
    private ObservableList<Section>              areas;
    private ObservableList<Section>              tickMarkSections;
    private ObservableList<Section>              tickLabelSections;
    private ObservableList<Marker>               markers;
    // UI related
    private SkinType                             skinType;
    private boolean                              _startFromZero;
    private BooleanProperty                      startFromZero;
    private boolean                              _returnToZero;
    private BooleanProperty                      returnToZero;
    private Color                                _zeroColor;
    private ObjectProperty<Color>                zeroColor;
    private double                               _minMeasuredValue;
    private DoubleProperty                       minMeasuredValue;
    private double                               _maxMeasuredValue;
    private DoubleProperty                       maxMeasuredValue;
    private boolean                              _minMeasuredValueVisible;
    private BooleanProperty                      minMeasuredValueVisible;
    private boolean                              _maxMeasuredValueVisible;
    private BooleanProperty                      maxMeasuredValueVisible;
    private boolean                              _oldValueVisible;
    private BooleanProperty                      oldValueVisible;
    private boolean                              _valueVisible;
    private BooleanProperty                      valueVisible;
    private Paint                                _backgroundPaint;
    private ObjectProperty<Paint>                backgroundPaint;
    private Paint                                _borderPaint;
    private ObjectProperty<Paint>                borderPaint;
    private double                               _borderWidth;
    private DoubleProperty                       borderWidth;
    private Paint                                _foregroundPaint;
    private ObjectProperty<Paint>                foregroundPaint;
    private Color                                _knobColor;
    private ObjectProperty<Color>                knobColor;
    private KnobType                             _knobType;
    private ObjectProperty<KnobType>             knobType;
    private Pos                                  _knobPosition;
    private ObjectProperty<Pos>                  knobPosition;
    private boolean                              _knobVisible;
    private BooleanProperty                      knobVisible;
    private boolean                              _animated;
    private BooleanProperty                      animated;
    private long                                 animationDuration;
    private double                               _startAngle;
    private DoubleProperty                       startAngle;
    private double                               _angleRange;
    private DoubleProperty                       angleRange;
    private double                               _angleStep;
    private DoubleProperty                       angleStep;
    private boolean                              _autoScale;
    private BooleanProperty                      autoScale;
    private boolean                              _shadowsEnabled;
    private BooleanProperty                      shadowsEnabled;
    private boolean                              _barEffectEnabled;
    private BooleanProperty                      barEffectEnabled;
    private ScaleDirection                       _scaleDirection;
    private ObjectProperty<ScaleDirection>       scaleDirection;
    private TickLabelLocation                    _tickLabelLocation;
    private ObjectProperty<TickLabelLocation>    tickLabelLocation;
    private TickLabelOrientation                 _tickLabelOrientation;
    private ObjectProperty<TickLabelOrientation> tickLabelOrientation;
    private Color                                _tickLabelColor;
    private ObjectProperty<Color>                tickLabelColor;
    private Color                                _tickMarkColor;
    private ObjectProperty<Color>                tickMarkColor;
    private Color                                _majorTickMarkColor;
    private ObjectProperty<Color>                majorTickMarkColor;
    private double                               _majorTickMarkLengthFactor;
    private DoubleProperty                       majorTickMarkLengthFactor;
    private double                               _majorTickMarkWidthFactor;
    private DoubleProperty                       majorTickMarkWidthFactor;
    private Color                                _mediumTickMarkColor;
    private ObjectProperty<Color>                mediumTickMarkColor;
    private double                               _mediumTickMarkLengthFactor;
    private DoubleProperty                       mediumTickMarkLengthFactor;
    private double                               _mediumTickMarkWidthFactor;
    private DoubleProperty                       mediumTickMarkWidthFactor;
    private Color                                _minorTickMarkColor;
    private ObjectProperty<Color>                minorTickMarkColor;
    private double                               _minorTickMarkLengthFactor;
    private DoubleProperty                       minorTickMarkLengthFactor;
    private double                               _minorTickMarkWidthFactor;
    private DoubleProperty                       minorTickMarkWidthFactor;
    private TickMarkType                         _majorTickMarkType;
    private ObjectProperty<TickMarkType>         majorTickMarkType;
    private TickMarkType                         _mediumTickMarkType;
    private ObjectProperty<TickMarkType>         mediumTickMarkType;
    private TickMarkType                         _minorTickMarkType;
    private ObjectProperty<TickMarkType>         minorTickMarkType;
    private Locale                               _locale;
    private ObjectProperty<Locale>               locale;
    private int                                  _decimals;
    private IntegerProperty                      decimals;
    private int                                  _tickLabelDecimals;
    private IntegerProperty                      tickLabelDecimals;
    private NeedleType                           _needleType;
    private ObjectProperty<NeedleType>           needleType;
    private NeedleShape                          _needleShape;
    private ObjectProperty<NeedleShape>          needleShape;
    private NeedleSize                           _needleSize;
    private ObjectProperty<NeedleSize>           needleSize;
    private NeedleBehavior                       _needleBehavior;
    private ObjectProperty<NeedleBehavior>       needleBehavior;
    private Color                                _needleColor;
    private ObjectProperty<Color>                needleColor;
    private Color                                _needleBorderColor;
    private ObjectProperty<Color>                needleBorderColor;
    private Color                                _barColor;
    private ObjectProperty<Color>                barColor;
    private Color                                _barBorderColor;
    private ObjectProperty<Color>                barBorderColor;
    private Color                                _barBackgroundColor;
    private ObjectProperty<Color>                barBackgroundColor;
    private LcdDesign                            _lcdDesign;
    private ObjectProperty<LcdDesign>            lcdDesign;
    private LcdFont                              _lcdFont;
    private ObjectProperty<LcdFont>              lcdFont;
    private Color                                _ledColor;
    private ObjectProperty<Color>                ledColor;
    private LedType                              _ledType;
    private ObjectProperty<LedType>              ledType;
    private Color                                _titleColor;
    private ObjectProperty<Color>                titleColor;
    private Color                                _subTitleColor;
    private ObjectProperty<Color>                subTitleColor;
    private Color                                _unitColor;
    private ObjectProperty<Color>                unitColor;
    private Color                                _valueColor;
    private ObjectProperty<Color>                valueColor;
    private Color                                _thresholdColor;
    private ObjectProperty<Color>                thresholdColor;
    private Color                                _averageColor;
    private ObjectProperty<Color>                averageColor;
    private boolean                              _checkSectionsForValue;
    private BooleanProperty                      checkSectionsForValue;
    private boolean                              _checkAreasForValue;
    private BooleanProperty                      checkAreasForValue;
    private boolean                              _checkThreshold;
    private BooleanProperty                      checkThreshold;
    private boolean                              _innerShadowEnabled;
    private BooleanProperty                      innerShadowEnabled;
    private boolean                              _thresholdVisible;
    private BooleanProperty                      thresholdVisible;
    private boolean                              _averageVisible;
    private BooleanProperty                      averageVisible;
    private boolean                              _sectionsVisible;
    private BooleanProperty                      sectionsVisible;
    private boolean                              _sectionsAlwaysVisible;
    private BooleanProperty                      sectionsAlwaysVisible;
    private boolean                              _sectionTextVisible;
    private BooleanProperty                      sectionTextVisible;
    private boolean                              _sectionIconsVisible;
    private BooleanProperty                      sectionIconsVisible;
    private boolean                              _highlightSections;
    private BooleanProperty                      highlightSections;
    private boolean                              _areasVisible;
    private BooleanProperty                      areasVisible;
    private boolean                              _areaTextVisible;
    private BooleanProperty                      areaTextVisible;
    private boolean                              _areaIconsVisible;
    private BooleanProperty                      areaIconsVisible;
    private boolean                              _highlightAreas;
    private BooleanProperty                      highlightAreas;
    private boolean                              _tickMarkSectionsVisible;
    private BooleanProperty                      tickMarkSectionsVisible;
    private boolean                              _tickLabelSectionsVisible;
    private BooleanProperty                      tickLabelSectionsVisible;
    private boolean                              _markersVisible;
    private BooleanProperty                      markersVisible;
    private boolean                              _majorTickMarksVisible;
    private BooleanProperty                      majorTickMarksVisible;
    private boolean                              _mediumTickMarksVisible;
    private BooleanProperty                      mediumTickMarksVisible;
    private boolean                              _minorTickMarksVisible;
    private BooleanProperty                      minorTickMarksVisible;
    private boolean                              _tickMarkRingVisible;
    private BooleanProperty                      tickMarkRingVisible;
    private boolean                              _tickLabelsVisible;
    private BooleanProperty                      tickLabelsVisible;
    private boolean                              _onlyFirstAndLastTickLabelVisible;
    private BooleanProperty                      onlyFirstAndLastTickLabelVisible;
    private double                               _majorTickSpace;
    private DoubleProperty                       majorTickSpace;
    private double                               _minorTickSpace;
    private DoubleProperty                       minorTickSpace;
    private boolean                              _lcdVisible;
    private BooleanProperty                      lcdVisible;
    private boolean                              _lcdCrystalEnabled;
    private BooleanProperty                      lcdCrystalEnabled;
    private boolean                              _ledVisible;
    private BooleanProperty                      ledVisible;
    private boolean                              _ledOn;
    private BooleanProperty                      ledOn;
    private boolean                              _ledBlinking;
    private BooleanProperty                      ledBlinking;
    private Orientation                          _orientation;
    private ObjectProperty<Orientation>          orientation;
    private boolean                              _gradientBarEnabled;
    private BooleanProperty                      gradientBarEnabled;
    private GradientLookup                       gradientLookup;
    private boolean                              _customTickLabelsEnabled;
    private BooleanProperty                      customTickLabelsEnabled;
    private ObservableList<String>               customTickLabels;
    private double                               _customTickLabelFontSize;
    private DoubleProperty                       customTickLabelFontSize;
    private boolean                              _interactive;
    private BooleanProperty                      interactive;
    private String                               _buttonTooltipText;
    private StringProperty                       buttonTooltipText;
    private boolean                              _keepAspect;
    private BooleanProperty                      keepAspect;
    private boolean                              _customFontEnabled;
    private BooleanProperty                      customFontEnabled;
    private Font                                 _customFont;
    private ObjectProperty<Font>                 customFont;
    private boolean                              _alert;
    private BooleanProperty                      alert;
    private String                               _alertMessage;
    private StringProperty                       alertMessage;
    private boolean                              _smoothing;
    private BooleanProperty                      smoothing;
    private String                               formatString;

    // others
    private double   originalMinValue;
    private double   originalMaxValue;
    private double   originalThreshold;
    private Timeline timeline;
    private Instant  lastCall;
    private boolean  withinSpeedLimit;


    // ******************** Constructors **************************************
    public Gauge() {
        this(SkinType.GAUGE);
    }
    public Gauge(final SkinType SKIN) {
        setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        skinType = SKIN;
        getStyleClass().add("gauge");

        init();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        _minValue                           = 0;
        _maxValue                           = 100;
        value                               = new DoublePropertyBase(_minValue) {
            @Override protected void invalidated() {
                final double VALUE = get();
                withinSpeedLimit = !(Instant.now().minusMillis(getAnimationDuration()).isBefore(lastCall));
                lastCall = Instant.now();
                if (isAnimated() && withinSpeedLimit) {
                    long animationDuration = isReturnToZero() ? (long) (0.2 * getAnimationDuration()) : getAnimationDuration();
                    timeline.stop();

                    final KeyValue KEY_VALUE;
                    if (NeedleBehavior.STANDARD == getNeedleBehavior()) {
                        KEY_VALUE = new KeyValue(currentValue, VALUE, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                    } else {
                        double ov  = getOldValue();
                        double min = getMinValue();
                        double max = getMaxValue();
                        double cv  = getCurrentValue();
                        double tmpValue;
                        if (Math.abs(VALUE - ov) > getRange() * 0.5) {
                            if (ov < VALUE) {
                                tmpValue = min - max + VALUE;
                            } else {
                                tmpValue = ov + max - ov + min + VALUE - getRange();
                            }
                            KEY_VALUE = new KeyValue(currentValue, tmpValue, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                        } else {
                            if (cv < min) currentValue.set(max + cv);
                            KEY_VALUE = new KeyValue(currentValue, VALUE, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                        }
                    }
                    final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(animationDuration), KEY_VALUE);
                    timeline.getKeyFrames().setAll(KEY_FRAME);
                    timeline.play();
                } else {
                    currentValue.set(VALUE);
                    fireUpdateEvent(FINISHED_EVENT);
                }
                if (isAveragingEnabled()) { movingAverage.addData(new Data(VALUE)); }
            }
            @Override public void set(final double VALUE) {
                super.set(VALUE);
                fireUpdateEvent(VALUE_EVENT);
            }
            @Override public Object getBean() { return Gauge.this; }
            @Override public String getName() { return "value"; }
        };
        oldValue                            = new SimpleDoubleProperty(Gauge.this, "oldValue", value.get());
        currentValue                        = new DoublePropertyBase(value.get()) {
            @Override protected void invalidated() {
                final double VALUE = get();
                if (isCheckThreshold()) {
                    double thrshld = getThreshold();
                    if (formerValue.get() < thrshld && VALUE > thrshld) {
                        fireEvent(EXCEEDED_EVENT);
                    } else if (formerValue.get() > thrshld && VALUE < thrshld) {
                        fireEvent(UNDERRUN_EVENT);
                    }
                }
                if (VALUE < getMinMeasuredValue()) {
                    setMinMeasuredValue(VALUE);
                } else if (VALUE > getMaxMeasuredValue()) {
                    setMaxMeasuredValue(VALUE);
                }
                formerValue.set(VALUE);
            }
            @Override public void set(final double VALUE) { super.set(VALUE); }
            @Override public Object getBean() { return Gauge.this; }
            @Override public String getName() { return "currentValue";}
        };
        formerValue                         = new SimpleDoubleProperty(Gauge.this, "formerValue", value.get());
        _range                              = _maxValue - _minValue;
        _threshold                          = _maxValue;
        _title                              = "";
        _subTitle                           = "";
        _unit                               = "";
        _averagingEnabled                   = false;
        _averagingPeriod                    = 10;
        movingAverage                       = new MovingAverage(_averagingPeriod);
        sections                            = FXCollections.observableArrayList();
        areas                               = FXCollections.observableArrayList();
        tickMarkSections                    = FXCollections.observableArrayList();
        tickLabelSections                   = FXCollections.observableArrayList();
        markers                             = FXCollections.observableArrayList();

        _startFromZero                      = false;
        _returnToZero                       = false;
        _zeroColor                          = DARK_COLOR;
        _minMeasuredValue                   = _maxValue;
        _maxMeasuredValue                   = _minValue;
        _minMeasuredValueVisible            = false;
        _maxMeasuredValueVisible            = false;
        _oldValueVisible                    = false;
        _valueVisible                       = true;
        _backgroundPaint                    = Color.TRANSPARENT;
        _borderPaint                        = Color.TRANSPARENT;
        _borderWidth                        = 1;
        _foregroundPaint                    = Color.TRANSPARENT;
        _knobColor                          = Color.rgb(204, 204, 204);
        _knobType                           = KnobType.STANDARD;
        _knobPosition                       = Pos.CENTER;
        _knobVisible                        = true;
        _animated                           = false;
        animationDuration                   = 800;
        _startAngle                         = 320;
        _angleRange                         = 280;
        _angleStep                          = _angleRange / _range;
        _autoScale                          = true;
        _shadowsEnabled                     = false;
        _barEffectEnabled                   = false;
        _scaleDirection                     = ScaleDirection.CLOCKWISE;
        _tickLabelLocation                  = TickLabelLocation.INSIDE;
        _tickLabelOrientation               = TickLabelOrientation.HORIZONTAL;
        _tickLabelColor                     = DARK_COLOR;
        _tickMarkColor                      = DARK_COLOR;
        _majorTickMarkColor                 = DARK_COLOR;
        _majorTickMarkLengthFactor          = 0.42;
        _majorTickMarkWidthFactor           = 0.275;
        _mediumTickMarkColor                = DARK_COLOR;
        _mediumTickMarkLengthFactor         = 0.41;
        _mediumTickMarkWidthFactor          = 0.175;
        _minorTickMarkColor                 = DARK_COLOR;
        _minorTickMarkLengthFactor          = 0.40;
        _minorTickMarkWidthFactor           = 0.1125;
        _majorTickMarkType                  = TickMarkType.LINE;
        _mediumTickMarkType                 = TickMarkType.LINE;
        _minorTickMarkType                  = TickMarkType.LINE;
        _locale                             = Locale.US;
        _decimals                           = 1;
        _tickLabelDecimals                  = 0;
        _needleType                         = NeedleType.STANDARD;
        _needleShape                        = NeedleShape.ANGLED;
        _needleSize                         = NeedleSize.STANDARD;
        _needleBehavior                     = NeedleBehavior.STANDARD;
        _needleColor                        = Color.rgb(200, 0, 0);
        _needleBorderColor                  = Color.TRANSPARENT;
        _barColor                           = BRIGHT_COLOR;
        _barBorderColor                     = Color.TRANSPARENT;
        _barBackgroundColor                 = DARK_COLOR;
        _lcdDesign                          = LcdDesign.STANDARD;
        _lcdFont                            = LcdFont.DIGITAL_BOLD;
        _ledColor                           = Color.RED;
        _ledType                            = LedType.STANDARD;
        _titleColor                         = DARK_COLOR;
        _subTitleColor                      = DARK_COLOR;
        _unitColor                          = DARK_COLOR;
        _valueColor                         = DARK_COLOR;
        _thresholdColor                     = Color.CRIMSON;
        _averageColor                       = Color.MAGENTA;
        _checkSectionsForValue              = false;
        _checkAreasForValue                 = false;
        _checkThreshold                     = false;
        _innerShadowEnabled                 = false;
        _thresholdVisible                   = false;
        _averageVisible                     = false;
        _sectionsVisible                    = false;
        _sectionsAlwaysVisible              = false;
        _sectionTextVisible                 = false;
        _sectionIconsVisible                = false;
        _highlightSections                  = false;
        _areasVisible                       = false;
        _areaTextVisible                    = false;
        _areaIconsVisible                   = false;
        _highlightAreas                     = false;
        _tickMarkSectionsVisible            = false;
        _tickLabelSectionsVisible           = false;
        _markersVisible                     = false;
        _tickLabelsVisible                  = true;
        _onlyFirstAndLastTickLabelVisible   = false;
        _majorTickMarksVisible              = true;
        _mediumTickMarksVisible             = true;
        _minorTickMarksVisible              = true;
        _tickMarkRingVisible                = false;
        _majorTickSpace                     = 10;
        _minorTickSpace                     = 1;
        _lcdVisible                         = false;
        _lcdCrystalEnabled                  = false;
        _ledVisible                         = false;
        _ledOn                              = false;
        _ledBlinking                        = false;
        _orientation                        = Orientation.HORIZONTAL;
        _gradientBarEnabled                 = false;
        _customTickLabelsEnabled            = false;
        customTickLabels                    = FXCollections.observableArrayList();
        _customTickLabelFontSize            = 18;
        _interactive                        = false;
        _buttonTooltipText                  = "";
        _keepAspect                         = true;
        _customFontEnabled                  = false;
        _customFont                         = Fonts.robotoRegular(12);
        _alert                              = false;
        _alertMessage                       = "";
        _smoothing                          = false;
        formatString                        = "%.2f";

        originalMinValue                    = -Double.MAX_VALUE;
        originalMaxValue                    = Double.MAX_VALUE;
        originalThreshold                   = Double.MAX_VALUE;
        lastCall                            = Instant.now();
        timeline                            = new Timeline();
        timeline.setOnFinished(e -> {
            if (isReturnToZero() && Double.compare(currentValue.get(), 0.0) != 0.0) {
                final KeyValue KEY_VALUE2 = new KeyValue(value, 0, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                final KeyFrame KEY_FRAME2 = new KeyFrame(Duration.millis((long) (0.8 * getAnimationDuration())), KEY_VALUE2);
                timeline.getKeyFrames().setAll(KEY_FRAME2);
                timeline.play();
            }
            fireUpdateEvent(FINISHED_EVENT);
        });
    }

    private void registerListeners() {
        disabledProperty().addListener(o -> setOpacity(isDisabled() ? 0.4 : 1));
        valueProperty().addListener((o, ov, nv) -> oldValue.set(ov.doubleValue()));
    }


    // ******************** Data related methods ******************************

    /**
     * Returns the value of the Gauge. If animated == true this value represents
     * the value at the end of the animation. Where currentValue represents the
     * current value during the animation.
     *
     * @return the value of the gauge
     */
    public double getValue() { return value.get(); }
    /**
     * Sets the value of the Gauge to the given double. If animated == true this
     * value will be the end value after the animation is finished.
     *
     * @param VALUE
     */
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    /**
     * Returns the current value of the Gauge. If animated == true this value
     * represents the current value during the animation. Otherwise it's returns
     * the same value as the getValue() method.
     *
     * @return the current value of the gauge
     */
    public double getCurrentValue() { return currentValue.get(); }
    public ReadOnlyDoubleProperty currentValueProperty() { return currentValue; }

    /**
     * Returns the last value of the Gauge. This will not be the last value during
     * an animation but the final last value after the animation was finished.
     * If you need to get the last value during an animation you should use
     * formerValue instead.
     *
     * @return the last value of the gauge
     */
    public double getOldValue() { return oldValue.get(); }
    public ReadOnlyDoubleProperty oldValueProperty() { return oldValue; }

    /**
     * Returns the last value of the Gauge. This will be the last value during
     * an animation.
     * If you need to get the last value after the animation is finished or if
     * you don't use animation at all (when using real values) you should use
     * oldValue instead.
     *
     * @return the last value of the gauge during an animation
     */
    public double getFormerValue() { return formerValue.get(); }
    public ReadOnlyDoubleProperty formerValueProperty() { return formerValue; }

    /**
     * Returns the minimum value of the scale. This value represents the lower
     * limit of the visible gauge values.
     *
     * @return the minimum value of the gauge scale
     */
    public double getMinValue() { return null == minValue ? _minValue : minValue.get(); }
    /**
     * Sets the minimum value of the gauge scale to the given value
     *
     * @param VALUE
     */
    public void setMinValue(final double VALUE) {
        if (Status.RUNNING == timeline.getStatus()) { timeline.jumpTo(Duration.ONE); }
        if (null == minValue) {
            if (VALUE > getMaxValue()) { setMaxValue(VALUE); }
            _minValue = Helper.clamp(-Double.MAX_VALUE, getMaxValue(), VALUE);
            setRange(getMaxValue() - _minValue);
            if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = _minValue;
            if (isStartFromZero() && _minValue < 0) setValue(0);
            if (Double.compare(originalThreshold, getThreshold()) < 0) { setThreshold(Helper.clamp(_minValue, getMaxValue(), originalThreshold)); }
            updateFormatString();
            fireUpdateEvent(RECALC_EVENT);
            if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
        } else {
            minValue.set(VALUE);
        }
    }
    public DoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new DoublePropertyBase(_minValue) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE > getMaxValue()) setMaxValue(VALUE);
                    setRange(getMaxValue() - VALUE);
                    if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = VALUE;
                    if (isStartFromZero() && _minValue < 0) Gauge.this.setValue(0);
                    if (Double.compare(originalThreshold, getThreshold()) < 0) { setThreshold(Helper.clamp(VALUE, getMaxValue(), originalThreshold)); }
                    updateFormatString();
                    fireUpdateEvent(RECALC_EVENT);
                    if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minValue";}
            };
        }
        return minValue;
    }

    /**
     * Returns the maximum value of the scale. This value represents the upper limit
     * of the visible gauge values.
     *
     * @return the maximum value of the gauge scale
     */
    public double getMaxValue() { return null == maxValue ? _maxValue : maxValue.get(); }
    /**
     * Sets the maximum value of the gauge scale to the given value
     *
     * @param VALUE
     */
    public void setMaxValue(final double VALUE) {
        if (Status.RUNNING == timeline.getStatus()) { timeline.jumpTo(Duration.ONE); }
        if (null == maxValue) {
            if (VALUE < getMinValue()) { setMinValue(VALUE); }
            _maxValue = Helper.clamp(getMinValue(), Double.MAX_VALUE, VALUE);
            setRange(_maxValue - getMinValue());
            if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = _maxValue;
            if (Double.compare(originalThreshold, getThreshold()) > 0) { setThreshold(Helper.clamp(getMinValue(), _maxValue, originalThreshold)); }

            if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
            updateFormatString();
            fireUpdateEvent(RECALC_EVENT);
        } else {
            maxValue.set(VALUE);
        }
    }
    public DoubleProperty maxValueProperty() {
        if (null == maxValue) {
            maxValue = new DoublePropertyBase(_maxValue) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < getMinValue()) setMinValue(VALUE);
                    setRange(VALUE - getMinValue());
                    if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = VALUE;
                    if (Double.compare(originalThreshold, getThreshold()) > 0) { setThreshold(Helper.clamp(getMinValue(), VALUE, originalThreshold)); }
                    updateFormatString();
                    fireUpdateEvent(RECALC_EVENT);
                    if (!valueProperty().isBound()) Gauge.this.setValue(Helper.clamp(getMinValue(), getMaxValue(), Gauge.this.getValue()));
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "maxValue"; }
            };
        }
        return maxValue;
    }

    /**
     * Always returns the range of the gauge scale (maxValue - minValue).
     * This value will be automatically calculated each time
     * the min- or maxValue will change.
     *
     * @return the range of the gauge scale
     */
    public double getRange() { return null == range ? _range : range.get(); }
    /**
     * This is a private method that sets the range to the given value
     * which is always (maxValue - minValue).
     *
     * @param RANGE
     */
    private void setRange(final double RANGE) {
        if (null == range) {
            _range = RANGE;
            setAngleStep(getAngleRange() / RANGE);
        } else {
            range.set(RANGE);
        }
    }
    public ReadOnlyDoubleProperty rangeProperty() {
        if (null == range) {
            range = new DoublePropertyBase((getMaxValue() - getMinValue())) {
                @Override protected void invalidated() { setAngleStep(getAngleRange() / get()); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "range"; }
            };
        }
        return range;
    }

    /**
     * Returns the threshold value that can be used to visualize a
     * threshold value on the scale. There are also events that will
     * be fired if the threshold was exceeded or underrun.
     * The value will be clamped to range of the gauge.
     *
     * @return the threshold value of the gauge
     */
    public double getThreshold() { return null == threshold ? _threshold : threshold.get(); }
    /**
     * Sets the threshold of the gauge to the given value. The value
     * will be clamped to the range of the gauge.
     *
     * @param THRESHOLD
     */
    public void setThreshold(final double THRESHOLD) {
        originalThreshold = THRESHOLD;
        if (null == threshold) {
            _threshold = Helper.clamp(getMinValue(), getMaxValue(), THRESHOLD);
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            threshold.set(THRESHOLD);
        }
    }
    public DoubleProperty tresholdProperty() {
        if (null == threshold) {
            threshold = new DoublePropertyBase(_threshold) {
                @Override protected void invalidated() {
                    final double THRESHOLD = get();
                    if (THRESHOLD < getMinValue() || THRESHOLD > getMaxValue()) set(Helper.clamp(getMinValue(), getMaxValue(), THRESHOLD));
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "threshold"; }
            };
        }
        return threshold;
    }

    /**
     * Returns the title of the gauge. This title will usually
     * only be visible if it is not empty.
     *
     * @return the title of the gauge
     */
    public String getTitle() { return null == title ? _title : title.get(); }
    /**
     * Sets the title of the gauge. This title will only be visible
     * if it is not empty.
     *
     * @param TITLE
     */
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
            fireUpdateEvent(VISIBILITY_EVENT);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            title.set(TITLE);
        }
    }
    public StringProperty titleProperty() {
        if (null == title) {
            title  = new StringPropertyBase(_title) {
                @Override protected void invalidated() {
                    fireUpdateEvent(VISIBILITY_EVENT);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "title"; }
            };
            _title = null;
        }
        return title;
    }

    /**
     * Returns the subtitle of the gauge. This subtitle will usually
     * only be visible if it is not empty.
     *
     * @return the subtitle of the gauge
     */
    public String getSubTitle() { return null == subTitle ? _subTitle : subTitle.get(); }
    /**
     * Sets the subtitle of the gauge. This subtitle will usually
     * only be visible if it is not empty.
     *
     * @param SUBTITLE
     */
    public void setSubTitle(final String SUBTITLE) {
        if (null == subTitle) {
            _subTitle = SUBTITLE;
            fireUpdateEvent(VISIBILITY_EVENT);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            subTitle.set(SUBTITLE);
        }
    }
    public StringProperty subTitleProperty() {
        if (null == subTitle) {
            subTitle  = new StringPropertyBase(_subTitle) {
                @Override protected void invalidated() {
                    fireUpdateEvent(VISIBILITY_EVENT);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "subTitle"; }
            };
            _subTitle = null;
        }
        return subTitle;
    }

    /**
     * Returns the unit of the gauge. This unit will usually only
     * be visible if it is not empty.
     *
     * @return the unit of the gauge
     */
    public String getUnit() { return null == unit ? _unit : unit.get(); }
    /**
     * Sets the unit of the gauge. This unit will usually only be
     * visible if it is not empty.
     *
     * @param UNIT
     */
    public void setUnit(final String UNIT) {
        if (null == unit) {
            _unit = UNIT;
            fireUpdateEvent(VISIBILITY_EVENT);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            unit.set(UNIT);
        }
    }
    public StringProperty unitProperty() {
        if (null == unit) {
            unit  = new StringPropertyBase(_unit) {
                @Override protected void invalidated() { 
                    fireUpdateEvent(VISIBILITY_EVENT);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "unit"; }
            };
            _unit = null;
        }
        return unit;
    }

    /**
     * Returns true if the averaging functionality is enabled.
     * @return true if the averaging functionality is enabled
     */
    public boolean isAveragingEnabled() { return null == averagingEnabled ? _averagingEnabled : averagingEnabled.get(); }
    /**
     * Defines if the averaging functionality will be enabled.
     */
    public void setAveragingEnabled(final boolean ENABLED) {
        if (null == averagingEnabled) {
            _averagingEnabled = ENABLED;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            averagingEnabled.set(ENABLED);
        }
    }
    public BooleanProperty averagingEnabledProperty() {
        if (null == averagingEnabled) {
            averagingEnabled = new BooleanPropertyBase(_averagingEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "averagingEnabled"; }
            };
        }
        return averagingEnabled;
    }

    /**
     * Returns the number of values that should be used for
     * the averaging of values. The value must be in the
     * range of 1 - 1000.
     * @return the number of values used for averaging
     */
    public int getAveragingPeriod() { return null == averagingPeriod ? _averagingPeriod : averagingPeriod.get(); }
    /**
     * Defines the number values that should be used for
     * the averaging of values. The value must be in the
     * range of 1 - 1000.
     * @param PERIOD
     */
    public void setAveragingPeriod(final int PERIOD) {
        if (null == averagingPeriod) {
            _averagingPeriod = PERIOD;
            movingAverage    = new MovingAverage(PERIOD); // MAX 1000 values
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            averagingPeriod.set(PERIOD);
        }
    }
    public IntegerProperty averagingPeriodProperty() {
        if (null == averagingPeriod) {
            averagingPeriod = new IntegerPropertyBase(_averagingPeriod) {
                @Override protected void invalidated() {
                    movingAverage = new MovingAverage(get());
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "averagingPeriod"; }
            };
        }
        return averagingPeriod;
    }

    /**
     * Returns the current list of Data objects that will
     * be used to calculate the moving average.
     * @return the current list of Data objects used for the moving average
     */
    public Queue<Data> getAveragingWindow() { return movingAverage.getWindow(); }

    /**
     * Returns the moving average over the number of values
     * defined by averagingPeriod.
     * @return the moving the average over the number of values defined by averagingPeriod
     */
    public double getAverage() { return movingAverage.getAverage(); }
    /**
     * Returns the moving average over the given duration.
     * @param DURATION
     * @return the moving average over the given duration
     */
    public double getTimeBasedAverageOf(final java.time.Duration DURATION) { return movingAverage.getTimeBasedAverageOf(DURATION); }

    /**
     * Returns an observable list of Section objects. The sections
     * will be used to colorize areas with a special meaning such
     * as the red area in a rpm gauge. Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getSections() { return sections; }
    /**
     * Sets the sections to the given list of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTIONS
     */
    public void setSections(final List<Section> SECTIONS) {
        sections.setAll(SECTIONS);
        Collections.sort(sections, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     *
     * @param SECTIONS
     */
    public void setSections(final Section... SECTIONS) { setSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given Section to the list of sections.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTION
     */
    public void addSection(final Section SECTION) {
        if (null == SECTION) return;
        sections.add(SECTION);
        Collections.sort(sections, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Removes the given Section from the list of sections.
     * Sections in the Medusa library
     * usually are less eye-catching than Areas.
     *
     * @param SECTION
     */
    public void removeSection(final Section SECTION) {
        if (null == SECTION) return;
        sections.remove(SECTION);
        Collections.sort(sections, new SectionComparator());
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
     * Returns an observable list of Section objects. The sections
     * will be used to colorize areas with a special meaning such
     * as the red area in a rpm gauge. Areas in the Medusa library
     * usually are more eye-catching than Sections.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getAreas() { return areas; }
    /**
     * Sets the sections to the given list of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     * Areas in the Medusa library usually are more
     * eye-catching than Sections.
     *
     * @param AREAS
     */
    public void setAreas(final List<Section> AREAS) {
        areas.setAll(AREAS);
        Collections.sort(areas, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Sets the sections to the given array of Section objects. The
     * sections will be used to colorize areas with a special
     * meaning such as the red area in a rpm gauge.
     * Areas in the Medusa library usually are more
     * eye-catching than Sections.
     *
     * @param AREAS
     */
    public void setAreas(final Section... AREAS) { setAreas(Arrays.asList(AREAS)); }
    /**
     * Adds the given Section to the list of areas.
     * Areas in the Medusa library usually are more
     * eye-catching than Sections.
     *
     * @param AREA
     */
    public void addArea(final Section AREA) {
        if (null == AREA) return;
        areas.add(AREA);
        Collections.sort(areas, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Removes the given Section from the list of areas.
     * Areas in the Medusa library usually are more
     * eye-catching than Sections.
     *
     * @param AREA
     */
    public void removeArea(final Section AREA) {
        if (null == AREA) return;
        areas.remove(AREA);
        Collections.sort(areas, new SectionComparator());
        fireUpdateEvent(SECTION_EVENT);
    }
    /**
     * Clears the list of areas.
     */
    public void clearAreas() {
        areas.clear();
        fireUpdateEvent(SECTION_EVENT);
    }

    /**
     * Returns an observable list of Section objects.
     * The sections will be used to colorize tickmarks with a
     * special meaning such as the red area in a rpm gauge.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getTickMarkSections() { return tickMarkSections; }
    /**
     * Sets the tickmark sections to the given list of Section objects.
     *
     * @param SECTIONS
     */
    public void setTickMarkSections(final List<Section> SECTIONS) {
        tickMarkSections.setAll(SECTIONS);
        Collections.sort(tickMarkSections, new SectionComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Sets the tickmark sections to the given array of Section objects.
     *
     * @param SECTIONS
     */
    public void setTickMarkSections(final Section... SECTIONS) { setTickMarkSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given Section to the list of tickmark sections.
     *
     * @param SECTION
     */
    public void addTickMarkSection(final Section SECTION) {
        if (null == SECTION) return;
        tickMarkSections.add(SECTION);
        Collections.sort(tickMarkSections, new SectionComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Removes the given Section from the list of tickmark sections.
     *
     * @param SECTION
     */
    public void removeTickMarkSection(final Section SECTION) {
        if (null == SECTION) return;
        tickMarkSections.remove(SECTION);
        Collections.sort(tickMarkSections, new SectionComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Clears the list of tickmark sections.
     */
    public void clearTickMarkSections() {
        tickMarkSections.clear();
        fireUpdateEvent(REDRAW_EVENT);
    }

    /**
     * Returns an observable list of Section objects.
     * The sections will be used to colorize ticklabels with
     * a special meaning such as the red area in a rpm gauge.
     *
     * @return an observable list of Section objects
     */
    public ObservableList<Section> getTickLabelSections() { return tickLabelSections; }
    /**
     * Sets the ticklabel sections to the given list of Section objects.
     *
     * @param SECTIONS
     */
    public void setTickLabelSections(final List<Section> SECTIONS) {
        tickLabelSections.setAll(SECTIONS);
        Collections.sort(tickLabelSections, new SectionComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Sets the ticklabel sections to the given array of Section objects.
     *
     * @param SECTIONS
     */
    public void setTickLabelSections(final Section... SECTIONS) { setTickLabelSections(Arrays.asList(SECTIONS)); }
    /**
     * Adds the given Section to the list of ticklabel sections.
     *
     * @param SECTION
     */
    public void addTickLabelSection(final Section SECTION) {
        if (null == SECTION) return;
        tickLabelSections.add(SECTION);
        Collections.sort(tickLabelSections, new SectionComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Removes the given Section from the list of ticklabel sections.
     *
     * @param SECTION
     */
    public void removeTickLabelSection(final Section SECTION) {
        if (null == SECTION) return;
        tickLabelSections.remove(SECTION);
        Collections.sort(tickLabelSections, new SectionComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Clears the list of ticklabel sections.
     */
    public void clearTickLabelSections() {
        tickLabelSections.clear();
        fireUpdateEvent(REDRAW_EVENT);
    }

    /**
     * Returns an observable list of Marker objects.
     * Like the threshold the markers are used to visualize
     * specific values. The markers will be visualized using
     * nodes with mouse event support (pressed, released) and
     * tooltip.
     *
     * @return an observable list of Marker objects
     */
    public ObservableList<Marker> getMarkers() { return markers; }
    /**
     * Sets the list of markers to the given list of Marker objects.
     * The markers will be visualized using nodes with mouse event
     * support (pressed, released) and tooltip.
     *
     * @param MARKERS
     */
    public void setMarkers(final List<Marker> MARKERS) {
        markers.setAll(MARKERS);
        Collections.sort(markers, new MarkerComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Sets the list of markers to the given array of Marker objects.
     * The markers will be visualuzed using nodes with mouse event
     * support (pressed, released) and tooltip.
     *
     * @param MARKERS
     */
    public void setMarkers(final Marker... MARKERS) { setMarkers(Arrays.asList(MARKERS)); }
    /**
     * Adds the given Marker to the list of markers.
     *
     * @param MARKER
     */
    public void addMarker(final Marker MARKER) {
        if (null == MARKER) return;
        markers.add(MARKER);
        Collections.sort(markers, new MarkerComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Removes the given Marker from the list of markers.
     *
     * @param MARKER
     */
    public void removeMarker(final Marker MARKER) {
        if (null == MARKER) return;
        markers.remove(MARKER);
        Collections.sort(markers, new MarkerComparator());
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Clears the list of markers.
     */
    public void clearMarkers() {
        markers.clear();
        fireUpdateEvent(REDRAW_EVENT);
    }


    // ******************** UI related methods ********************************
    /**
     * A convenient method to set the color of foreground elements like
     * title, subTitle, unit, value, tickLabel and tickMark to the given
     * Color.
     *
     * @param COLOR
     */
    public void setForegroundBaseColor(final Color COLOR) {
        if (null == titleColor) { _titleColor = COLOR; } else { titleColor.set(COLOR); }
        if (null == subTitleColor) { _subTitleColor = COLOR; } else { subTitleColor.set(COLOR); }
        if (null == unitColor) { _unitColor = COLOR; } else { unitColor.set(COLOR); }
        if (null == valueColor) { _valueColor = COLOR; } else { valueColor.set(COLOR); }
        if (null == tickLabelColor) { _tickLabelColor = COLOR; } else { tickLabelColor.set(COLOR); }
        if (null == zeroColor) { _zeroColor = COLOR; } else { zeroColor.set(COLOR); }
        if (null == tickMarkColor) { _tickMarkColor = COLOR; } else { tickMarkColor.set(COLOR); }
        if (null == majorTickMarkColor) { _majorTickMarkColor = COLOR; } else { majorTickMarkColor.set(COLOR); }
        if (null == mediumTickMarkColor) { _mediumTickMarkColor = COLOR; } else { mediumTickMarkColor.set(COLOR); }
        if (null == minorTickMarkColor) { _minorTickMarkColor = COLOR; } else { minorTickMarkColor.set(COLOR); }
        fireUpdateEvent(REDRAW_EVENT);
    }

    /**
     * Returns true if the visualization of the value should start from 0. This
     * is especially useful when you work for example with a gauge that has a
     * range with a negative minValue
     *
     * @return true if the visualization of the value should start from 0
     */
    public boolean isStartFromZero() { return null == startFromZero ? _startFromZero : startFromZero.get(); }
    /**
     * Defines the behavior of the visualization where the needle/bar should
     * start from 0 instead of the minValue. This is especially useful when
     * working with a gauge that has a range with a negative minValue
     *
     * @param IS_TRUE
     */
    public void setStartFromZero(final boolean IS_TRUE) {
        if (null == startFromZero) {
            _startFromZero = IS_TRUE;
            //setValue(IS_TRUE && getMinValue() < 0 ? 0 : getMinValue());
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            startFromZero.set(IS_TRUE);
        }
    }
    public BooleanProperty startFromZeroProperty() {
        if (null == startFromZero) {
            startFromZero = new BooleanPropertyBase(_startFromZero) {
                @Override protected void invalidated() {
                    //Gauge.this.setValue((get() && getMinValue() < 0) ? 0 : getMinValue());
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "startFromZero"; }
            };
        }
        return startFromZero;
    }

    /**
     * Returns true if the needle/bar should always return to zero. This setting
     * only makes sense if animated == true and the data rate is not too high.
     * Set to false when using real measured live data.
     *
     * @return true if the needle/bar should always return to zero.
     */
    public boolean isReturnToZero() { return null == returnToZero ? _returnToZero : returnToZero.get(); }
    /**
     * Defines the behavior of the visualization where the needle/bar should
     * always return to 0 after it reached the final value. This setting only makes
     * sense if animated == true and the data rate is not too high.
     * Set to false when using real measured live data.
     *
     * @param IS_TRUE
     */
    public void setReturnToZero(final boolean IS_TRUE) {
        if (null == returnToZero) {
            _returnToZero = Double.compare(getMinValue(), 0.0) <= 0 ? IS_TRUE : false;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            returnToZero.set(IS_TRUE);
        }
    }
    public BooleanProperty returnToZeroProperty() {
        if (null == returnToZero) {
            returnToZero = new BooleanPropertyBase(_returnToZero) {
                @Override protected void invalidated() {
                    if (Double.compare(getMaxValue(), 0.0) > 0) set(false);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "returnToZero"; }
            };
        }
        return returnToZero;
    }

    /**
     * Returns the color that will be used to colorize the 0 tickmark and ticklabel
     * when the gauge range has a negative min- and positive maxValue.
     *
     * @return the color that will used to visualize the 0 tickmark and ticklabel
     */
    public Color getZeroColor() { return null == zeroColor ? _zeroColor : zeroColor.get(); }
    /**
     * Defines the color that will be used to colorize the 0 tickmark and ticklabel
     * when the gauge range has a negative min- and positive maxValue.
     *
     * @param COLOR
     */
    public void setZeroColor(final Color COLOR) {
        if (null == zeroColor) {
            _zeroColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            zeroColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> zeroColorProperty() {
        if (null == zeroColor) {
            zeroColor  = new ObjectPropertyBase<Color>(_zeroColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "zeroColor"; }
            };
            _zeroColor = null;
        }
        return zeroColor;
    }

    /**
     * Returns the smallest value that was measured after the last reset.
     * The default value is the maxValue of the gauge.
     *
     * @return the smallest value that was measured after the last reset
     */
    public double getMinMeasuredValue() { return null == minMeasuredValue ? _minMeasuredValue : minMeasuredValue.get(); }
    /**
     * Sets the minMeasuredValue to the given value.
     *
     * @param MIN_MEASURED_VALUE
     */
    public void setMinMeasuredValue(final double MIN_MEASURED_VALUE) {
        if (null == minMeasuredValue) {
            _minMeasuredValue = MIN_MEASURED_VALUE;
        } else {
            minMeasuredValue.set(MIN_MEASURED_VALUE);
        }
    }
    public ReadOnlyDoubleProperty minMeasuredValueProperty() {
        if (null == minMeasuredValue) { minMeasuredValue = new SimpleDoubleProperty(this, "minMeasuredValue", _minMeasuredValue); }
        return minMeasuredValue;
    }

    /**
     * Returns the biggest value that was measured after the last reset.
     * The default value is the minValue of the gauge.
     *
     * @return the biggest value that was measured after the last reset
     */
    public double getMaxMeasuredValue() {
        return null == maxMeasuredValue ? _maxMeasuredValue : maxMeasuredValue.get();
    }
    /**
     * Sets the maxMeasuredVAlue to the given value.
     *
     * @param MAX_MEASURED_VALUE
     */
    public void setMaxMeasuredValue(final double MAX_MEASURED_VALUE) {
        if (null == maxMeasuredValue) {
            _maxMeasuredValue = MAX_MEASURED_VALUE;
        } else {
            maxMeasuredValue.set(MAX_MEASURED_VALUE);
        }
    }
    public ReadOnlyDoubleProperty maxMeasuredValueProperty() {
        if (null == maxMeasuredValue) { maxMeasuredValue = new SimpleDoubleProperty(this, "maxMeasuredValue", _maxMeasuredValue); }
        return maxMeasuredValue;
    }

    /**
     * Resets the min- and maxMeasuredValue to the value of the gauge.
     */
    public void resetMeasuredValues() {
        setMinMeasuredValue(getValue());
        setMaxMeasuredValue(getValue());
    }

    /**
     * Returns true if the indicator of the minMeasuredValue is visible.
     *
     * @return true if the indicator of the minMeasuredValue is visible
     */
    public boolean isMinMeasuredValueVisible() { return null == minMeasuredValueVisible ? _minMeasuredValueVisible : minMeasuredValueVisible.get(); }
    /**
     * Defines if the indicator of the minMeasuredValue should be visible.
     *
     * @param VISIBLE
     */
    public void setMinMeasuredValueVisible(final boolean VISIBLE) {
        if (null == minMeasuredValueVisible) {
            _minMeasuredValueVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            minMeasuredValueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty minMeasuredValueVisibleProperty() {
        if (null == minMeasuredValueVisible) {
            minMeasuredValueVisible = new BooleanPropertyBase(_minMeasuredValueVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minMeasuredValueVisible"; }
            };
        }
        return minMeasuredValueVisible;
    }

    /**
     * Returns true if the indicator of the maxMeasuredValue is visible.
     *
     * @return true if the indicator of the maxMeasuredValue is visible
     */
    public boolean isMaxMeasuredValueVisible() { return null == maxMeasuredValueVisible ? _maxMeasuredValueVisible : maxMeasuredValueVisible.get(); }
    /**
     * Defines if the indicator of the maxMeasuredValue should be visible.
     *
     * @param VISIBLE
     */
    public void setMaxMeasuredValueVisible(final boolean VISIBLE) {
        if (null == maxMeasuredValueVisible) {
            _maxMeasuredValueVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            maxMeasuredValueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty maxMeasuredValueVisibleProperty() {
        if (null == maxMeasuredValueVisible) {
            maxMeasuredValueVisible = new BooleanPropertyBase(_maxMeasuredValueVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "maxMeasuredValueVisible"; }
            };
        }
        return maxMeasuredValueVisible;
    }

    /**
     * Returns true if the old value of the gauge is visible (not implemented)
     *
     * @return true if the old value of the gauge is visible (not implemented)
     */
    public boolean isOldValueVisible() { return null == oldValueVisible ? _oldValueVisible : oldValueVisible.get(); }
    /**
     * Defines if the old value of the gauge should be visible (not implemented)
     *
     * @param VISIBLE
     */
    public void setOldValueVisible(final boolean VISIBLE) {
        if (null == oldValueVisible) {
            _oldValueVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            oldValueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty oldValueVisibleProperty() {
        if (null == oldValueVisible) {
            oldValueVisible = new BooleanPropertyBase(_oldValueVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "oldValueVisible"; }
            };
        }
        return oldValueVisible;
    }

    /**
     * Returns true if the visualization of the gauge value is visible.
     * Usually this is a Label or Text node.
     *
     * @return true if the visualization of the gauge value is visible
     */
    public boolean isValueVisible() { return null == valueVisible ? _valueVisible : valueVisible.get(); }
    /**
     * Defines if the visualization of the gauge value should be visible.
     *
     * @param VISIBLE
     */
    public void setValueVisible(final boolean VISIBLE) {
        if (null == valueVisible) {
            _valueVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            valueVisible.set(VISIBLE);
        }
    }
    public BooleanProperty valueVisibleProperty() {
        if (null == valueVisible) {
            valueVisible = new BooleanPropertyBase(_valueVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "valueVisible"; }
            };
        }
        return valueVisible;
    }

    /**
     * Returns the Paint object that will be used to fill the gauge background.
     * This is usally a Color object.
     *
     * @return the Paint object that will be used to fill the gauge background
     */
    public Paint getBackgroundPaint() { return null == backgroundPaint ? _backgroundPaint : backgroundPaint.get(); }
    /**
     * Defines the Paint object that will be used to fill the gauge background.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "backgroundPaint"; }
            };
            _backgroundPaint = null;
        }
        return backgroundPaint;
    }

    /**
     * Returns the Paint object that will be used to draw the border of the gauge.
     * Usually this is a Color object.
     *
     * @return the Paint object that will be used to draw the border of the gauge
     */
    public Paint getBorderPaint() { return null == borderPaint ? _borderPaint : borderPaint.get(); }
    /**
     * Defines the Paint object that will be used to draw the border of the gauge.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "borderPaint"; }
            };
            _borderPaint = null;
        }
        return borderPaint;
    }

    /**
     * Returns the width in pixels that will be used to draw the border of the gauge.
     * The value will be clamped between 0 and 50 pixels.
     *
     * @return the width in pixels that will be used to draw the border of the gauge
     */
    public double getBorderWidth() { return null == borderWidth ? _borderWidth : borderWidth.get(); }
    /**
     * Defines the width in pixels that will be used to draw the border of the gauge.
     * The value will be clamped between 0 and 50 pixels.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "borderWidth"; }
            };
        }
        return borderWidth;
    }

    /**
     * Returns the Paint object that will be used to fill the foreground of the gauge.
     * This could be used to visualize glass effects etc. and is only rarely used.
     *
     * @return the Paint object that will be used to fill the foreground of the gauge
     */
    public Paint getForegroundPaint() { return null == foregroundPaint ? _foregroundPaint : foregroundPaint.get(); }
    /**
     * Defines the Paint object that will be used to fill the foreground of the gauge.
     * This could be used to visualize glass effects etc. and is only rarely used.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "foregroundPaint"; }
            };
            _foregroundPaint = null;
        }
        return foregroundPaint;
    }

    /**
     * Returns the color that will be used to colorize the knob of
     * the radial gauges.
     *
     * @return the color that will be used to colorize the knob of the radial gauges
     */
    public Color getKnobColor() { return null == knobColor ? _knobColor : knobColor.get(); }
    /**
     * Defines the color that will be used to colorize the knob of
     * the radial gauges.
     *
     * @param COLOR
     */
    public void setKnobColor(final Color COLOR) {
        if (null == knobColor) {
            _knobColor = COLOR;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            knobColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> knobColorProperty() {
        if (null == knobColor) {
            knobColor  = new ObjectPropertyBase<Color>(_knobColor) {
                @Override protected void invalidated() { fireUpdateEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "knobColor"; }
            };
            _knobColor = null;
        }
        return knobColor;
    }

    /**
     * Returns the type of knob that will be used in the radial
     * gauges. The values are STANDARD, PLAIN, METAL and FLAT.
     *
     * @return the type of knob that will be used in the radial gauges
     */
    public KnobType getKnobType() { return null == knobType ? _knobType : knobType.get(); }
    /**
     * Defines the type of knob that will be used in the radial
     * gauges. The values are STANDARD, PLAIN, METAL and FLAT.
     *
     * @param TYPE
     */
    public void setKnobType(final KnobType TYPE) {
        if (null == knobType) {
            _knobType = null == TYPE ? KnobType.STANDARD : TYPE;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            knobType.set(TYPE);
        }
    }
    public ObjectProperty<KnobType> knobTypeProperty() {
        if (null == knobType) {
            knobType  = new ObjectPropertyBase<KnobType>(_knobType) {
                @Override protected void invalidated() {
                    if (null == get()) set(KnobType.STANDARD);
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "knobType"; }
            };
            _knobType = null;
        }
        return knobType;
    }

    /**
     * Returns the position of the knob in radial gauges. This
     * position also defines where the needle will be placed.
     * Dependent on the SkinType you can use the following values
     * GaugeSkin  : CENTER
     * HSkin      : TOP_CENTER, BOTTOM_CENTER
     * VSkin      : CENTER_LEFT, CENTER_RIGHT
     * QuarterSkin: TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT
     *
     * @return the position of the knob in the radial gauges
     */
    public Pos getKnobPosition() { return null == knobPosition ? _knobPosition : knobPosition.get(); }
    /**
     * Defines the position of the knob in radial gauges. This
     * position also defines where the needle will be placed.
     * Dependent on the SkinType you can use the following values
     * GaugeSkin  : CENTER
     * HSkin      : TOP_CENTER, BOTTOM_CENTER
     * VSkin      : CENTER_LEFT, CENTER_RIGHT
     * QuarterSkin: TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT
     *
     * @param POSITION
     */
    public void setKnobPosition(final Pos POSITION) {
        if (null == knobPosition) {
            _knobPosition = POSITION;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            knobPosition.set(POSITION);
        }
    }
    public ObjectProperty<Pos> knobPositionProperty() {
        if (null == knobPosition) {
            knobPosition  = new ObjectPropertyBase<Pos>(_knobPosition) {
                @Override protected void invalidated() {
                    final Pos POSITION = get();
                    if (null == POSITION) {
                        switch (skinType) {
                            case HORIZONTAL: super.set(Pos.CENTER_RIGHT); break;
                            case VERTICAL  : super.set(Pos.BOTTOM_CENTER); break;
                            case QUARTER   : super.set(Pos.BOTTOM_RIGHT); break;
                            default        : super.set(Pos.CENTER);
                        }
                    }
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "knobPosition"; }
            };
            _knobPosition = null;
        }
        return knobPosition;
    }

    /**
     * Returns true if the knob is visible.
     *
     * @return true if the knob is visible
     */
    public boolean isKnobVisible() { return null == knobVisible ? _knobVisible : knobVisible.get(); }
    /**
     * Defines if the knob is visible.
     *
     * @param VISIBLE
     */
    public void setKnobVisible(final boolean VISIBLE) {
        if (null == knobVisible) {
            _knobVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            knobVisible.set(VISIBLE);
        }
    }
    public BooleanProperty knobVisibleProperty() {
        if (null == knobVisible) {
            knobVisible = new BooleanPropertyBase(_knobVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "knobVisible"; }
            };
        }
        return knobVisible;
    }

    /**
     * Returns true if setting the value of the gauge will be animated
     * using the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
     * @return true if setting the value of the gauge will be animated
     */
    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    /**
     * Defines if setting the value of the gauge should be animated using
     * the duration defined in animationDuration [ms].
     * Keep in mind that it only makes sense to animate the setting if
     * the data rate is low (more than 1 value per second). If you use real
     * live measured data you should set animated to false.
     *
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
        if (null == animated) { animated = new SimpleBooleanProperty(Gauge.this, "animated", _animated); }
        return animated;
    }

    /**
     * Returns the duration in milliseconds that will be used to animate
     * the needle/bar of the gauge from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @return the duration in ms that will be used to animate the needle/bar
     */
    public long getAnimationDuration() { return animationDuration; }
    /**
     * Defines the duration in milliseconds that will be used to animate
     * the needle/bar of the gauge from the last value to the next value.
     * This will only be used if animated == true. This value will be
     * clamped in the range of 10ms - 10s.
     *
     * @param ANIMATION_DURATION
     */
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = Helper.clamp(10, 10000, ANIMATION_DURATION); }

    /**
     * Returns the angle in degree that defines the start of the scale with
     * it's minValue in a radial gauge. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the gauge height the startAngle should be set to 270 degrees.
     *
     * @return the angle in degree that defines the start of the scale
     */
    public double getStartAngle() { return null == startAngle ? _startAngle : startAngle.get(); }
    /**
     * Defines the angle in degree that defines the start of the scale with
     * it's minValue in a radial gauge. If set to 0 the scale will start at
     * the bottom center and the direction of counting is mathematical correct
     * counter-clockwise.
     * Means if you would like to start the scale on the left side in the
     * middle of the gauge height the startAngle should be set to 270 degrees.
     *
     * @param ANGLE
     */
    public void setStartAngle(final double ANGLE) {
        if (null == startAngle) {
            _startAngle = Helper.clamp(0.0, 360.0, ANGLE);
            fireUpdateEvent(RECALC_EVENT);
        } else {
            startAngle.set(ANGLE);
        }
    }
    public DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new DoublePropertyBase(_startAngle) {
                @Override protected void invalidated() {
                    final double ANGLE = get();
                    if (ANGLE < 0 || ANGLE > 360 ) set(Helper.clamp(0.0, 360.0, ANGLE));
                    fireUpdateEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    /**
     * Returns the angle range in degree that will be used to draw the scale
     * of the radial gauge. The given range will be clamped in the range of
     * 0 - 360 degrees and will be drawn in the direction dependent on the
     * scaleDirection.
     *
     * @return the angle range in degree that will be used to draw the scale
     */
    public double getAngleRange() { return null == angleRange ? _angleRange : angleRange.get(); }
    /**
     * Defines the angle range in degree that will be used to draw the scale
     * of the radial gauge. The given range will be clamped in the range of
     * 0 - 360 degrees. The range will start at the startAngle and will be
     * drawn in the direction dependent on the scaleDirection.
     *
     * @param RANGE
     */
    public void setAngleRange(final double RANGE) {
        double tmpAngleRange = Helper.clamp(0.0, 360.0, RANGE);
        if (null == angleRange) {
            _angleRange = tmpAngleRange;
            setAngleStep(tmpAngleRange / getRange());
            if (isAutoScale()) { calcAutoScale(); }
            fireUpdateEvent(RECALC_EVENT);
        } else {
            angleRange.set(tmpAngleRange);
        }
    }
    public DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new DoublePropertyBase(_angleRange) {
                @Override protected void invalidated() {
                    final double ANGLE_RANGE = get();
                    set(Helper.clamp(0.0, 360.0, ANGLE_RANGE));
                    setAngleStep(get() / getRange());
                    if (isAutoScale()) { calcAutoScale(); }
                    fireUpdateEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "angleRange"; }
            };
        }
        return angleRange;
    }

    /**
     * Returns the value that is calculated by dividing the angleRange
     * by the range. The angleStep will always be recalculated when changing
     * the min-, maxValue or angleRange.
     * E.g. angleRange = 180 degrees, range = 0 - 100 will lead to angleStep = 180/100 = 1.8
     *
     * @return the value that is calculated by dividing the angleRange by the range
     */
    public double getAngleStep() { return null == angleStep ? _angleStep : angleStep.get(); }
    /**
     * Private method that will be used to set the angleStep
     *
     * @param STEP
     */
    private void setAngleStep(final double STEP) {
        if (null == angleStep) {
            _angleStep = STEP;
        } else {
            angleStep.set(STEP);
        }
    }
    public ReadOnlyDoubleProperty angleStepProperty() {
        if (null == angleStep) { angleStep = new SimpleDoubleProperty(Gauge.this, "angleStep", _angleStep); }
        return angleStep;
    }

    /**
     * Returns true if the scale will be calculated automatically based
     * on the defined values for min- and maxValue.
     * The autoscaling is on per default because otherwise you will
     * run into problems when having very large or very small scales like
     * 0 - 10000 or 0 - 1.
     *
     * @return true if the scale will be calculated automatically
     */
    public boolean isAutoScale() { return null == autoScale ? _autoScale : autoScale.get(); }
    /**
     * Defines if the scale should be calculated automatically based on
     * the defined values for min- and maxValue.
     * The autoscaling is on per default because otherwise you will
     * run into problems when having very large or very small scales like
     * 0 - 10000 or 0 - 1.
     *
     * @param AUTO_SCALE
     */
    public void setAutoScale(final boolean AUTO_SCALE) {
        if (null == autoScale) {
            _autoScale = AUTO_SCALE;
            if (_autoScale) {
                originalMinValue = getMinValue();
                originalMaxValue = getMaxValue();
                calcAutoScale();
            } else {
                setMinValue(Double.compare(-Double.MAX_VALUE, originalMinValue) == 0 ? getMinValue() : originalMinValue);
                setMaxValue(Double.compare(Double.MAX_VALUE, originalMaxValue) == 0 ? getMaxValue() : originalMaxValue);
            }
            fireUpdateEvent(RECALC_EVENT);
        } else {
            autoScale.set(AUTO_SCALE);
        }
    }
    public BooleanProperty autoScaleProperty() {
        if (null == autoScale) {
            autoScale = new BooleanPropertyBase(_autoScale) {
                @Override protected void invalidated() {
                    if (get()) {
                        calcAutoScale();
                    } else {
                        setMinValue(originalMinValue);
                        setMaxValue(originalMaxValue);
                    }
                    fireUpdateEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "autoScale"; }
            };
        }
        return autoScale;
    }

    /**
     * Returns true if effects like shadows will be drawn.
     * In some gauges inner- and dropshadows will be used which will be
     * switched on/off by setting the shadowsEnabled property.
     *
     * @return true if effects like shadows will be drawn
     */
    public boolean isShadowsEnabled() { return null == shadowsEnabled ? _shadowsEnabled : shadowsEnabled.get(); }
    /**
     * Defines if effects like shadows should be drawn.
     * In some gauges inner- and dropshadows will be used which will be
     * switched on/off by setting the shadowsEnabled property.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "shadowsEnabled"; }
            };
        }
        return shadowsEnabled;
    }

    /**
     * Returns true if the highlight effect on the gauges like the
     * LinearSkin bar will be drawn. If you would like to have a
     * more flat style you should set this to false.
     *
     * @return true if the highlight effect on a bar will be drawn
     */
    public boolean isBarEffectEnabled() { return null == barEffectEnabled ? _barEffectEnabled : barEffectEnabled.get(); }
    /**
     * Defines if the the highlight effect on the gauges like the
     * LinearSkin bar will be drawn. If you would like to have a
     * more flat style you should set this to false.
     *
     * @param ENABLED
     */
    public void setBarEffectEnabled(final boolean ENABLED) {
        if (null == barEffectEnabled) {
            _barEffectEnabled = ENABLED;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            barEffectEnabled.set(ENABLED);
        }
    }
    public BooleanProperty barEffectEnabledProperty() {
        if (null == barEffectEnabled) {
            barEffectEnabled = new BooleanPropertyBase(_barEffectEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "barEffectEnabled"; }
            };
        }
        return barEffectEnabled;
    }

    /**
     * Returns the direction of the scale. The values are
     * CLOCKWISE and COUNTER_CLOCKWISE. This property is needed
     * to realize gauges like in QuarterSkin where the needle
     * and knob should be placed on the upper right corner and
     * the scale should start at the bottom. Here you need a
     * counter-clockwise direction of the scale.
     *
     * @return the direction of the scale
     */
    public ScaleDirection getScaleDirection() { return null == scaleDirection ? _scaleDirection : scaleDirection.get(); }
    /**
     * Defines the direction of the scale. The values are
     * CLOCKWISE and COUNTER_CLOCKWISE. This property is needed
     * to realize gauges like in QuarterSkin where the needle
     * and knob should be placed on the upper right corner and
     * the scale should start at the bottom. Here you need a
     * counter-clockwise direction of the scale.
     *
     * @param DIRECTION
     */
    public void setScaleDirection(final ScaleDirection DIRECTION) {
        if (null == scaleDirection) {
            _scaleDirection = null == DIRECTION ? ScaleDirection.CLOCKWISE : DIRECTION;
            fireUpdateEvent(RECALC_EVENT);
        } else {
            scaleDirection.set(DIRECTION);
        }
    }
    public ObjectProperty<ScaleDirection> scaleDirectionProperty() {
        if (null == scaleDirection) {
            scaleDirection  = new ObjectPropertyBase<ScaleDirection>(_scaleDirection) {
                @Override protected void invalidated() {
                    if (null == get()) set(ScaleDirection.CLOCKWISE);
                    fireUpdateEvent(RECALC_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "scaleDirection"; }
            };
            _scaleDirection = null;
        }
        return scaleDirection;
    }

    /**
     * Returns the location of the ticklabels. The values are
     * INSIDE and OUTSIDE. The location of the ticklabels has an
     * influence on the size of the tickmarks and length of the needle.
     *
     * @return the location of the ticklabels
     */
    public TickLabelLocation getTickLabelLocation() { return null == tickLabelLocation ? _tickLabelLocation : tickLabelLocation.get(); }
    /**
     * Defines the location of the ticklabels. The values are
     * INSIDE and OUTSIDE. The location of the ticklabels has an
     * influence on the size of the tickmarks and length of the needle.
     *
     * @param LOCATION
     */
    public void setTickLabelLocation(final TickLabelLocation LOCATION) {
        if (null == tickLabelLocation) {
            _tickLabelLocation = null == LOCATION ? TickLabelLocation.INSIDE : LOCATION;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelLocation.set(LOCATION);
        }
    }
    public ObjectProperty<TickLabelLocation> tickLabelLocationProperty() {
        if (null == tickLabelLocation) {
            tickLabelLocation  = new ObjectPropertyBase<TickLabelLocation>(_tickLabelLocation) {
                @Override protected void invalidated() {
                    if(null == get()) set(TickLabelLocation.INSIDE);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelLocation"; }
            };
            _tickLabelLocation = null;
        }
        return tickLabelLocation;
    }

    /**
     * Returns the orientation of the ticklabels. The values are
     * HORIZONTAL, ORTHOGONAL and TANGENT. Especially the ORTHOGONAL
     * setting can be useful when using scales with big numbers.
     *
     * @return the orientation of the ticklabels
     */
    public TickLabelOrientation getTickLabelOrientation() { return null == tickLabelOrientation ? _tickLabelOrientation : tickLabelOrientation.get(); }
    /**
     * Defines the orientation of the ticklabels. The values are
     * HORIZONTAL, ORTHOGONAL and TANGENT. Especially the ORTHOGONAL
     * setting can be useful when using scales with big numbers.
     *
     * @param ORIENTATION
     */
    public void setTickLabelOrientation(final TickLabelOrientation ORIENTATION) {
        if (null == tickLabelOrientation) {
            _tickLabelOrientation = null == ORIENTATION ? TickLabelOrientation.HORIZONTAL : ORIENTATION;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelOrientation.set(ORIENTATION);
        }
    }
    public ObjectProperty<TickLabelOrientation> tickLabelOrientationProperty() {
        if (null == tickLabelOrientation) {
            tickLabelOrientation  = new ObjectPropertyBase<TickLabelOrientation>(_tickLabelOrientation) {
                @Override protected void invalidated() {
                    if(null == get()) set(TickLabelOrientation.HORIZONTAL);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelOrientation"; }
            };
            _tickLabelOrientation = null;
        }
        return tickLabelOrientation;
    }

    /**
     * Returns the color that will be used to colorize the ticklabels. This color
     * will only be used if no ticklabel section defines a different color.
     *
     * @return the color that will be used to colorize the ticklabels
     */
    public Color getTickLabelColor() { return null == tickLabelColor ? _tickLabelColor : tickLabelColor.get(); }
    /**
     * Defines the color that will be used to colorize the ticklabels. This color
     * will only be used if no ticklabel section defines a different color.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelColor"; }
            };
            _tickLabelColor = null;
        }
        return tickLabelColor;
    }

    /**
     * Returns the color that will be used to colorize the tickmarks. This color
     * will only be used if no tickmark section or major-, medium- and minorTickMarkColor
     * is defined at the position of the tickmark.
     *
     * @return the color that will be used to colorize the tickmarks
     */
    public Color getTickMarkColor() { return null == tickMarkColor ? _tickMarkColor : tickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the tickmarks. This color
     * will only be used if no tickmark section or major-, medium- and minorTickMarkColor
     * is defined at the position of the tickmark.
     *
     * @param COLOR
     */
    public void setTickMarkColor(final Color COLOR) {
        if (null == tickMarkColor) {
            _tickMarkColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> tickMarkColorProperty() {
        if (null == tickMarkColor) {
            tickMarkColor  = new ObjectPropertyBase<Color>(_tickMarkColor) {
                @Override protected void invalidated() { fireUpdateEvent( REDRAW_EVENT ); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickMarkColor"; }
            };
            _tickMarkColor = null;
        }
        return tickMarkColor;
    }

    /**
     * Returns the color that will be used to colorize the major
     * tickmarks. This color will only be used if no tickmark
     * section is defined at the position of the tickmark.
     *
     * @return the color that will be used to colorize the major tickmarks
     */
    public Color getMajorTickMarkColor() { return null == majorTickMarkColor ? _majorTickMarkColor : majorTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the major
     * tickmarks. This color will only be used if no tickmark
     * section is defined at the position of the tickmark.
     *
     * @param COLOR
     */
    public void setMajorTickMarkColor(final Color COLOR) {
        if (null == majorTickMarkColor) {
            _majorTickMarkColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            majorTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> majorTickMarkColorProperty() {
        if (null == majorTickMarkColor) {
            majorTickMarkColor  = new ObjectPropertyBase<Color>(_majorTickMarkColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "majorTickMarkColor"; }
            };
            _majorTickMarkColor = null;
        }
        return majorTickMarkColor;
    }

    /**
     * Returns the factor that defines the length of the major tick mark.
     * The value can be in the range from 0 - 1;
     *
     * @return the factor that defines the length of the major tick mark
     */
    public double getMajorTickMarkLengthFactor() { return null == majorTickMarkLengthFactor ? _majorTickMarkLengthFactor : majorTickMarkLengthFactor.get(); }
    /**
     * The factor defines the length of the major tick mark.
     * It can be in the range from 0 - 1.
     *
     * @param FACTOR
     */
    public void setMajorTickMarkLengthFactor(final double FACTOR) {
        if (null == majorTickMarkLengthFactor) {
            _majorTickMarkLengthFactor = Helper.clamp(0.0, 1.0, FACTOR);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            majorTickMarkLengthFactor.set(FACTOR);
        }
    }
    public DoubleProperty majorTickMarkLengthFactorProperty() {
        if (null == majorTickMarkLengthFactor) {
            majorTickMarkLengthFactor = new DoublePropertyBase(_majorTickMarkLengthFactor) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < 0 || VALUE > 1) set(Helper.clamp(0.0, 1.0, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "majorTickMarkLengthFactor"; }
            };
        }
        return majorTickMarkLengthFactor;
    }

    /**
     * Returns the factor that defines the width of the major tick mark.
     * The value can be in the range from 0 - 1.
     *
     * @return the factor that defines the width of the major tick mark
     */
    public double getMajorTickMarkWidthFactor() { return null == majorTickMarkWidthFactor ? _majorTickMarkWidthFactor : majorTickMarkWidthFactor.get(); }
    /**
     * The factor defines the width of the major tick mark.
     * It can be in the range from 0 - 1.
     *
     * @param FACTOR
     */
    public void setMajorTickMarkWidthFactor(final double FACTOR) {
        if (null == majorTickMarkWidthFactor) {
            _majorTickMarkWidthFactor = Helper.clamp(0.0, 1.0, FACTOR);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            majorTickMarkWidthFactor.set(FACTOR);
        }
    }
    public DoubleProperty majorTickMarkWidthFactorProperty() {
        if (null == majorTickMarkWidthFactor) {
            majorTickMarkWidthFactor = new DoublePropertyBase(_majorTickMarkWidthFactor) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < 0 || VALUE > 1) set(Helper.clamp(0.0, 1.0, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "majorTickMarkWidthFactor"; }
            };
        }
        return majorTickMarkWidthFactor;
    }

    /**
     * Returns the color that will be used to colorize the medium
     * tickmarks. This color will only be used if no tickmark
     * section is defined at the position of the tickmark.
     *
     * @return the color that will be used to colorize the medium tickmark
     */
    public Color getMediumTickMarkColor() { return null == mediumTickMarkColor ? _mediumTickMarkColor : mediumTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the medium
     * tickmarks. This color will only be used if no tickmark
     * section is defined at the position of the tickmark.
     *
     * @param COLOR
     */
    public void setMediumTickMarkColor(final Color COLOR) {
        if (null == mediumTickMarkColor) {
            _mediumTickMarkColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            mediumTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> mediumTickMarkColorProperty() {
        if (null == mediumTickMarkColor) {
            mediumTickMarkColor  = new ObjectPropertyBase<Color>(_mediumTickMarkColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "mediumTickMarkColor"; }
            };
            _mediumTickMarkColor = null;
        }
        return mediumTickMarkColor;
    }

    /**
     * Returns the factor that defines the length of the medium tick mark.
     * The value can be in the range from 0 - 1;
     *
     * @return the factor that defines the length of the medium tick mark
     */
    public double getMediumTickMarkLengthFactor() { return null == mediumTickMarkLengthFactor ? _mediumTickMarkLengthFactor : mediumTickMarkLengthFactor.get(); }
    /**
     * The factor defines the length of the medium tick mark.
     * It can be in the range from 0 - 1.
     *
     * @param FACTOR
     */
    public void setMediumTickMarkLengthFactor(final double FACTOR) {
        if (null == mediumTickMarkLengthFactor) {
            _mediumTickMarkLengthFactor = Helper.clamp(0.0, 1.0, FACTOR);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            mediumTickMarkLengthFactor.set(FACTOR);
        }
    }
    public DoubleProperty mediumTickMarkLengthFactorProperty() {
        if (null == mediumTickMarkLengthFactor) {
            mediumTickMarkLengthFactor = new DoublePropertyBase(_mediumTickMarkLengthFactor) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < 0 || VALUE > 1) set(Helper.clamp(0.0, 1.0, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "mediumTickMarkLengthFactor"; }
            };
        }
        return mediumTickMarkLengthFactor;
    }

    /**
     * Returns the factor that defines the width of the medium tick mark.
     * The value can be in the range from 0 - 1.
     *
     * @return the factor that defines the width of the medium tick mark
     */
    public double getMediumTickMarkWidthFactor() { return null == mediumTickMarkWidthFactor ? _mediumTickMarkWidthFactor : mediumTickMarkWidthFactor.get(); }
    /**
     * The factor defines the width of the medium tick mark.
     * It can be in the range from 0 - 1.
     *
     * @param FACTOR
     */
    public void setMediumTickMarkWidthFactor(final double FACTOR) {
        if (null == mediumTickMarkWidthFactor) {
            _mediumTickMarkWidthFactor = Helper.clamp(0.0, 1.0, FACTOR);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            mediumTickMarkWidthFactor.set(FACTOR);
        }
    }
    public DoubleProperty mediumTickMarkWidthFactorProperty() {
        if (null == mediumTickMarkWidthFactor) {
            mediumTickMarkWidthFactor = new DoublePropertyBase(_mediumTickMarkWidthFactor) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < 0 || VALUE > 1) set(Helper.clamp(0.0, 1.0, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "mediumTickMarkWidthFactor"; }
            };
        }
        return mediumTickMarkWidthFactor;
    }

    /**
     * Returns the color that will be used to colorize the minor
     * tickmarks. This color will only be used if no tickmark
     * section is defined at the position of the tickmark.
     *
     * @return the color that will be used to colorize the minor tickmark
     */
    public Color getMinorTickMarkColor() { return null == minorTickMarkColor ? _minorTickMarkColor : minorTickMarkColor.get(); }
    /**
     * Defines the color that will be used to colorize the minor
     * tickmarks. This color will only be used if no tickmark
     * section is defined at the position of the tickmark.
     *
     * @param COLOR
     */
    public void setMinorTickMarkColor(final Color COLOR) {
        if (null == minorTickMarkColor) {
            _minorTickMarkColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minorTickMarkColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> minorTickMarkColorProperty() {
        if (null == minorTickMarkColor) {
            minorTickMarkColor  = new ObjectPropertyBase<Color>(_minorTickMarkColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minorTickMarkColor"; }
            };
            _minorTickMarkColor = null;
        }
        return minorTickMarkColor;
    }

    /**
     * Returns the factor that defines the length of the minor tick mark.
     * The value can be in the range from 0 - 1;
     *
     * @return the factor that defines the length of the minor tick mark
     */
    public double getMinorTickMarkLengthFactor() { return null == minorTickMarkLengthFactor ? _minorTickMarkLengthFactor : minorTickMarkLengthFactor.get(); }
    /**
     * The factor defines the length of the minor tick mark.
     * It can be in the range from 0 - 1.
     *
     * @param FACTOR
     */
    public void setMinorTickMarkLengthFactor(final double FACTOR) {
        if (null == minorTickMarkLengthFactor) {
            _minorTickMarkLengthFactor = Helper.clamp(0.0, 1.0, FACTOR);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minorTickMarkLengthFactor.set(FACTOR);
        }
    }
    public DoubleProperty minorTickMarkLengthFactorProperty() {
        if (null == minorTickMarkLengthFactor) {
            minorTickMarkLengthFactor = new DoublePropertyBase(_minorTickMarkLengthFactor) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < 0 || VALUE > 1) set(Helper.clamp(0.0, 1.0, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minorTickMarkLengthFactor"; }
            };
        }
        return minorTickMarkLengthFactor;
    }

    /**
     * Returns the factor that defines the width of the minor tick mark.
     * The value can be in the range from 0 - 1.
     *
     * @return the factor that defines the width of the minor tick mark
     */
    public double getMinorTickMarkWidthFactor() { return null == minorTickMarkWidthFactor ? _minorTickMarkWidthFactor : minorTickMarkWidthFactor.get(); }
    /**
     * The factor defines the width of the minor tick mark.
     * It can be in the range from 0 - 1.
     *
     * @param FACTOR
     */
    public void setMinorTickMarkWidthFactor(final double FACTOR) {
        if (null == minorTickMarkWidthFactor) {
            _minorTickMarkWidthFactor = Helper.clamp(0.0, 1.0, FACTOR);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minorTickMarkWidthFactor.set(FACTOR);
        }
    }
    public DoubleProperty minorTickMarkWidthFactorProperty() {
        if (null == minorTickMarkWidthFactor) {
            minorTickMarkWidthFactor = new DoublePropertyBase(_minorTickMarkWidthFactor) {
                @Override protected void invalidated() {
                    final double VALUE = get();
                    if (VALUE < 0 || VALUE > 1) set(Helper.clamp(0.0, 1.0, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minorTickMarkWidthFactor"; }
            };
        }
        return minorTickMarkWidthFactor;
    }

    /**
     * Returns the shape that will be used to visualize the major tickmark.
     * Values are LINE, DOT, TRAPEZOID, BOX, TICK_LABEL and PILL
     *
     * @return the shape that will be used to visualize the major tickmark
     */
    public TickMarkType getMajorTickMarkType() { return null == majorTickMarkType ? _majorTickMarkType : majorTickMarkType.get(); }
    /**
     * Defines the shape that will be used to visualize the major tickmark.
     * Values are LINE, DOT, TRAPEZOID, BOX, TICK_LABEL and PILL
     *
     * @param TYPE
     */
    public void setMajorTickMarkType(final TickMarkType TYPE) {
        if (null == majorTickMarkType) {
            _majorTickMarkType = null == TYPE ? TickMarkType.LINE : TYPE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            majorTickMarkType.set(TYPE);
        }
    }
    public ObjectProperty<TickMarkType> majorTickMarkTypeProperty() {
        if (null == majorTickMarkType) {
            majorTickMarkType  = new ObjectPropertyBase<TickMarkType>(_majorTickMarkType) {
                @Override protected void invalidated() {
                    if (null == get()) set(TickMarkType.LINE);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "majorTickMarkType"; }
            };
            _majorTickMarkType = null;
        }
        return majorTickMarkType;
    }

    /**
     * Returns the shape that will be used to visualize the medium tickmark.
     * Values are LINE, DOT, TRAPEZOID, BOX and PILL
     *
     * @return the shape that will be used to visualize the medium tickmark
     */
    public TickMarkType getMediumTickMarkType() { return null == mediumTickMarkType ? _mediumTickMarkType : mediumTickMarkType.get(); }
    /**
     * Defines the shape that will be used to visualize the medium tickmark.
     * Values are LINE, DOT, TRAPEZOID, BOX and PILL
     *
     * @param TYPE
     */
    public void setMediumTickMarkType(final TickMarkType TYPE) {
        if (null == mediumTickMarkType) {
            _mediumTickMarkType = null == TYPE ? TickMarkType.LINE : TYPE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            mediumTickMarkType.set(TYPE);
        }
    }
    public ObjectProperty<TickMarkType> mediumTickMarkTypeProperty() {
        if (null == mediumTickMarkType) {
            mediumTickMarkType  = new ObjectPropertyBase<TickMarkType>(_mediumTickMarkType) {
                @Override protected void invalidated() {
                    if (null == get()) set(TickMarkType.LINE);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "mediumTickMarkType"; }
            };
            _mediumTickMarkType = null;
        }
        return mediumTickMarkType;
    }

    /**
     * Returns the shape that will be used to visualize the minor tickmark.
     * Values are LINE, DOT, TRAPEZOID, BOX and PILL
     *
     * @return the shape that will be used to visualize the minor tickmark
     */
    public TickMarkType getMinorTickMarkType() { return null == minorTickMarkType ? _minorTickMarkType : minorTickMarkType.get(); }
    /**
     * Defines the shape that will be used to visualize the minor tickmark.
     * Values are LINE, DOT, TRAPEZOID, BOX and PILL
     *
     * @param TYPE
     */
    public void setMinorTickMarkType(final TickMarkType TYPE) {
        if (null == minorTickMarkType) {
            _minorTickMarkType = null == TYPE ? TickMarkType.LINE : TYPE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minorTickMarkType.set(TYPE);
        }
    }
    public ObjectProperty<TickMarkType> minorTickMarkTypeProperty() {
        if (null == minorTickMarkType) {
            minorTickMarkType  = new ObjectPropertyBase<TickMarkType>(_minorTickMarkType) {
                @Override protected void invalidated() {
                    if (null == get()) set(TickMarkType.LINE);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minorTickMarkType"; }
            };
            _minorTickMarkType = null;
        }
        return minorTickMarkType;
    }

    public Locale getLocale() { return null == locale ? _locale : locale.get(); }
    public void setLocale(final Locale LOCALE) {
        if (null == locale) {
            _locale = null == LOCALE ? Locale.US : LOCALE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            locale.set(LOCALE);
        }
    }
    public ObjectProperty<Locale> localeProperty() {
        if (null == locale) {
            locale  = new ObjectPropertyBase<Locale>(_locale) {
                @Override protected void invalidated() {
                    if (null == get()) set(Locale.US);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "locale"; }
            };
            _locale = null;
        }
        return locale;
    }

    /**
     * Returns the number of decimals that will be used to format the
     * value of the gauge. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @return the number of decimals that will be used to format the value
     */
    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    /**
     * Defines the number of decimals that will be used to format the
     * value of the gauge. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @param DECIMALS
     */
    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = Helper.clamp(0, MAX_NO_OF_DECIMALS, DECIMALS);
            updateFormatString();
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            decimals.set(DECIMALS);
        }
    }
    public IntegerProperty decimalsProperty() {
        if (null == decimals) {
            decimals = new IntegerPropertyBase(_decimals) {
                @Override protected void invalidated() {
                    final int VALUE = get();
                    if (VALUE < 0 || VALUE > MAX_NO_OF_DECIMALS) set(Helper.clamp(0, MAX_NO_OF_DECIMALS, VALUE));
                    updateFormatString();
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "decimals"; }
            };
        }
        return decimals;
    }

    /**
     * Returns the number of decimals that will be used to format the
     * value of the ticklabels. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @return
     */
    public int getTickLabelDecimals() { return null == tickLabelDecimals ? _tickLabelDecimals : tickLabelDecimals.get(); }
    /**
     * Defines the number of decimals that will be used to format the
     * value of the gauge. The number of decimals will be clamped to
     * a value between 0-3.
     *
     * @param DECIMALS
     */
    public void setTickLabelDecimals(final int DECIMALS) {
        if (null == tickLabelDecimals) {
            _tickLabelDecimals = Helper.clamp(0, MAX_NO_OF_DECIMALS, DECIMALS);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelDecimals.set(DECIMALS);
        }
    }
    public IntegerProperty tickLabelDecimalsProperty() {
        if (null == tickLabelDecimals) {
            tickLabelDecimals = new IntegerPropertyBase(_tickLabelDecimals) {
                @Override protected void invalidated() {
                    final int VALUE = get();
                    if (VALUE < 0 || VALUE > MAX_NO_OF_DECIMALS) set(Helper.clamp(0, MAX_NO_OF_DECIMALS, VALUE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelDecimals"; }
            };
        }
        return tickLabelDecimals;
    }

    /**
     * Returns the shape of the needle that will be used. This is
     * dependent on the used SkinType. Values are
     * GaugeSkin  : STANDARD, FAT
     * HSkin      : STANDARD
     * VSkin      : STANDARD
     * QuarterSkin: STANDARD
     *
     * @return the shape of the needle that will be used
     */
    public NeedleType getNeedleType() { return null == needleType ? _needleType : needleType.get(); }
    /**
     * Defines the shape of the needle that will be used. This is
     * dependent on the used SkinType. Values are
     * GaugeSkin  : STANDARD, FAT
     * HSkin      : STANDARD
     * VSkin      : STANDARD
     * QuarterSkin: STANDARD
     *
     * @param TYPE
     */
    public void setNeedleType(final NeedleType TYPE) {
        if (null == needleType) {
            _needleType = TYPE == null ? NeedleType.STANDARD : TYPE;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            needleType.set(TYPE);
        }
    }
    public ObjectProperty<NeedleType> needleTypeProperty() {
        if (null == needleType) {
            needleType  = new ObjectPropertyBase<NeedleType>(_needleType) {
                @Override protected void invalidated() {
                    if (null == get()) set(NeedleType.STANDARD);
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "needleType"; }
            };
            _needleType = null;
        }
        return needleType;
    }

    /**
     * Returns the graphical representation of the needle that will be used.
     * Values are ANGLED, ROUND and FLAT
     * In principle it defines how the needle will be filled (gradient, color)
     *
     * @return the graphical representation of the needle
     */
    public NeedleShape getNeedleShape() { return null == needleShape ? _needleShape : needleShape.get(); }
    /**
     * Defines the graphical representation of the needle that will be used.
     * Values are ANGLED, ROUND and FLAT
     * In principle it defines how the needle will be filled (gradient, color)
     *
     * @param SHAPE
     */
    public void setNeedleShape(final NeedleShape SHAPE) {
        if (null == needleShape) {
            _needleShape = null == SHAPE ? NeedleShape.ANGLED : SHAPE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            needleShape.set(SHAPE);
        }
    }
    public ObjectProperty<NeedleShape> needleShapeProperty() {
        if (null == needleShape) {
            needleShape  = new ObjectPropertyBase<NeedleShape>(_needleShape) {
                @Override protected void invalidated() {
                    if (null == get()) set(NeedleShape.ANGLED);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "needleShape"; }
            };
            _needleShape = null;
        }
        return needleShape;
    }

    /**
     * Returns the thickness of the needle.
     * The values are THIN, STANDARD and THICK
     *
     * @return the thickness of the needle
     */
    public NeedleSize getNeedleSize() { return null == needleSize ? _needleSize : needleSize.get(); }
    /**
     * Defines the thickness of the needle.
     * The values are THIN, STANDARD and THICK
     *
     * @param SIZE
     */
    public void setNeedleSize(final NeedleSize SIZE) {
        if (null == needleSize) {
            _needleSize = null == SIZE ? NeedleSize.STANDARD : SIZE;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            needleSize.set(SIZE);
        }
    }
    public ObjectProperty<NeedleSize> needleSizeProperty() {
        if (null == needleSize) {
            needleSize  = new ObjectPropertyBase<NeedleSize>(_needleSize) {
                @Override protected void invalidated() {
                    if(null == get()) set(NeedleSize.STANDARD);
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "needleSize"; }
            };
            _needleSize = null;
        }
        return needleSize;
    }

    /**
     * Returns the behavior of the needle movement.
     * The values are STANDARD and OPTIMIZED
     * This is an experimental feature that only makes sense in
     * gauges that use an angleRange of 360 degrees and where the
     * needle should be able to use the shortest way to the target
     * value. As an example one can think of a compass. If the value
     * in a compass changes from 20 degrees to 290 degrees the needle
     * will take the shortest way to the value, in this case this
     * means it will rotate counter-clockwise.
     *
     * @return the behavior of the needle movement (EXPERIMENTAL)
     */
    public NeedleBehavior getNeedleBehavior() { return null == needleBehavior ? _needleBehavior : needleBehavior.get(); }
    /**
     * Defines the behavior of the needle movement.
     * The values are STANDARD and OPTIMIZED
     * This is an experimental feature that only makes sense in
     * gauges that use an angleRange of 360 degrees and where the
     * needle should be able to use the shortest way to the target
     * value. As an example one can think of a compass. If the value
     * in a compass changes from 20 degrees to 290 degrees the needle
     * will take the shortest way to the value, in this case this
     * means it will rotate counter-clockwise.
     *
     * @param BEHAVIOR
     */
    public void setNeedleBehavior(final NeedleBehavior BEHAVIOR) {
        if (null == needleBehavior) {
            _needleBehavior = null == BEHAVIOR ? NeedleBehavior.STANDARD : BEHAVIOR;
        } else {
            needleBehavior.set(BEHAVIOR);
        }
    }
    public ObjectProperty<NeedleBehavior> needleBehaviorProperty() {
        if (null == needleBehavior) {
            needleBehavior  = new ObjectPropertyBase<NeedleBehavior>(_needleBehavior) {
                @Override protected void invalidated() { if(null == get()) set(NeedleBehavior.STANDARD); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "needleBehavior"; }
            };
            _needleBehavior = null;
        }
        return needleBehavior;
    }

    /**
     * Returns the color that will be used to colorize the needle of
     * the radial gauges.
     *
     * @return the color that wil be used to colorize the needle
     */
    public Color getNeedleColor() { return null == needleColor ? _needleColor : needleColor.get(); }
    /**
     * Defines the color that will be used to colorize the needle of
     * the radial gauges.
     *
     * @param COLOR
     */
    public void setNeedleColor(final Color COLOR) {
        if (null == needleColor) {
            _needleColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            needleColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> needleColorProperty() {
        if (null == needleColor) {
            needleColor  = new ObjectPropertyBase<Color>(_needleColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "needleColor"; }
            };
            _needleColor = null;
        }
        return needleColor;
    }

    /**
     * Returns the color that is used to colorize the border of the needle.
     *
     * @return the color that is used to colorize the border of the needle
     */
    public Color getNeedleBorderColor() { return null == needleBorderColor ? _needleBorderColor : needleBorderColor.get(); }
    /**
     * Defines the color that will be used to colorize the border of the needle.
     *
     * @param COLOR
     */
    public void setNeedleBorderColor(final Color COLOR) {
        if (null == needleBorderColor) {
            _needleBorderColor = null == COLOR ? Color.TRANSPARENT : COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            needleBorderColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> needleBorderColorProperty() {
        if (null == needleBorderColor) {
            needleBorderColor  = new ObjectPropertyBase<Color>(_needleBorderColor) {
                @Override protected void invalidated() {
                    if (null == get()) set(Color.TRANSPARENT);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "needleBorderColor"; }
            };
            _needleBorderColor = null;
        }
        return needleBorderColor;
    }

    /**
     * Returns the color that will be used to colorize the bar of
     * the gauge (if it has a bar).
     *
     * @return the color that will be used to colorized the bar (if available)
     */
    public Color getBarColor() { return null == barColor ? _barColor : barColor.get(); }
    /**
     * Defines the color that will be used to colorize the bar of
     * the gauge (if it has a bar).
     *
     * @param COLOR
     */
    public void setBarColor(final Color COLOR) {
        if (null == barColor) {
            _barColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            barColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> barColorProperty() {
        if (null == barColor) {
            barColor  = new ObjectPropertyBase<Color>(_barColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "barColor"; }
            };
            _barColor = null;
        }
        return barColor;
    }

    /**
     * Returns the color that is used to colorize the border of the bar.
     *
     * @return the color that is used to colorize the border of the bar
     */
    public Color getBarBorderColor() { return null == barBorderColor ? _barBorderColor : barBorderColor.get(); }
    /**
     * Defines the color that will be used to colorize the border of the bar.
     *
     * @param COLOR
     */
    public void setBarBorderColor(final Color COLOR) {
        if (null == barBorderColor) {
            _barBorderColor = null == COLOR ? Color.TRANSPARENT : COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            barBorderColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> barBorderColorProperty() {
        if (null == barBorderColor) {
            barBorderColor  = new ObjectPropertyBase<Color>(_barBorderColor) {
                @Override protected void invalidated() {
                    if(null == get()) set(Color.TRANSPARENT);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "barBorderColor"; }
            };
            _barBorderColor = null;
        }
        return barBorderColor;
    }

    /**
     * Returns the color that will be used to colorize the bar background of
     * the gauge (if it has a bar).
     *
     * @return the color that will be used to colorize the bar background
     */
    public Color getBarBackgroundColor() { return null == barBackgroundColor ? _barBackgroundColor : barBackgroundColor.get(); }
    /**
     * Returns the color that will be used to colorize the bar background of
     * the gauge (if it has a bar).
     *
     * @param COLOR
     */
    public void setBarBackgroundColor(final Color COLOR) {
        if (null == barBackgroundColor) {
            _barBackgroundColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            barBackgroundColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> barBackgroundColorProperty() {
        if (null == barBackgroundColor) {
            barBackgroundColor  = new ObjectPropertyBase<Color>(_barBackgroundColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "barBackgroundColor"; }
            };
            _barBackgroundColor = null;
        }
        return barBackgroundColor;
    }

    /**
     * Returns the design that will be used to visualize the LCD display
     * of the gauge (if it has one). The values are
     * BEIGE, BLACK, BLUE, ORANGE, RED, YELLOW, WHITE, GRAY,
     * BLACK, GREEN, GREEN_DARKGREEN, BLUE2, BLUE_BLACK,
     * BLUE_DARKBLUE, BLUE_LIGHTBLUE, BLUE_GRAY, STANDARD,
     * LIGHTGREEN, STANDARD_GREEN, BLUE_BLUE, RED_DARKRED,
     * DARKBLUE, PURPLE, BLACK_RED, DARKGREEN, AMBER,
     * LIGHTBLUE, GREEN_BLACK, YELLOW_BLACK, BLACK_YELLOW,
     * LIGHTGREEN_BLACK, DARKPURPLE, DARKAMBER, BLUE_LIGHTBLUE2,
     * GRAY_PURPLE, YOCTOPUCE, SECTIONS, FLAT_CUSTOM
     *
     * @return the design that will be used to visualize the LCD display (if available)
     */
    public LcdDesign getLcdDesign() { return null == lcdDesign ? _lcdDesign : lcdDesign.get(); }
    /**
     * Defines the design that will be used to visualize the LCD display
     * of the gauge (if it has one). The values are
     * BEIGE, BLACK, BLUE, ORANGE, RED, YELLOW, WHITE, GRAY,
     * BLACK, GREEN, GREEN_DARKGREEN, BLUE2, BLUE_BLACK,
     * BLUE_DARKBLUE, BLUE_LIGHTBLUE, BLUE_GRAY, STANDARD,
     * LIGHTGREEN, STANDARD_GREEN, BLUE_BLUE, RED_DARKRED,
     * DARKBLUE, PURPLE, BLACK_RED, DARKGREEN, AMBER,
     * LIGHTBLUE, GREEN_BLACK, YELLOW_BLACK, BLACK_YELLOW,
     * LIGHTGREEN_BLACK, DARKPURPLE, DARKAMBER, BLUE_LIGHTBLUE2,
     * GRAY_PURPLE, YOCTOPUCE, SECTIONS, FLAT_CUSTOM
     *
     * @param DESIGN
     */
    public void setLcdDesign(final LcdDesign DESIGN) {
        if (null == lcdDesign) {
            _lcdDesign = null == DESIGN ? LcdDesign.STANDARD : DESIGN;
            fireUpdateEvent(LCD_EVENT);
        } else {
            lcdDesign.set(DESIGN);
        }
    }
    public ObjectProperty<LcdDesign> lcdDesignProperty() {
        if (null == lcdDesign) {
            lcdDesign  = new ObjectPropertyBase<LcdDesign>(_lcdDesign) {
                @Override protected void invalidated() {
                    if(null == get()) set(LcdDesign.STANDARD);
                    fireUpdateEvent(LCD_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "lcdDesign"; }
            };
            _lcdDesign = null;
        }
        return lcdDesign;
    }

    /**
     * Returns the font that will be used to visualize the LCD value
     * if the gauge has a LCD display.
     * The values are STANDARD, LCD, SLIM, DIGITAL_BOLD, ELEKTRA
     *
     * @return the font that will be used to visualize the LCD value
     */
    public LcdFont getLcdFont() { return null == lcdFont ? _lcdFont : lcdFont.get(); }
    /**
     * Defines the font that will be used to visualize the LCD value
     * if the gauge has a LCD display.
     * The values are STANDARD, LCD, SLIM, DIGITAL_BOLD, ELEKTRA
     *
     * @param FONT
     */
    public void setLcdFont(final LcdFont FONT) {
        if (null == lcdFont) {
            _lcdFont = null == FONT ? LcdFont.DIGITAL_BOLD : FONT;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            lcdFont.set(FONT);
        }
    }
    public ObjectProperty<LcdFont> lcdFontProperty() {
        if (null == lcdFont) {
            lcdFont  = new ObjectPropertyBase<LcdFont>(_lcdFont) {
                @Override protected void invalidated() {
                    if(null == get()) set(LcdFont.DIGITAL_BOLD);
                    fireUpdateEvent(RESIZE_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "lcdFont"; }
            };
            _lcdFont = null;
        }
        return lcdFont;
    }

    /**
     * Returns the color that will be used to visualize the LED of the
     * gauge if it has one.
     *
     * @return the color that will be used to visualize the LED
     */
    public Color getLedColor() { return null == ledColor ? _ledColor : ledColor.get(); }
    /**
     * Defines the color that will be used to visualize the LED of the
     * gauge if it has one.
     *
     * @param COLOR
     */
    public void setLedColor(final Color COLOR) {
        if (null == ledColor) {
            _ledColor = null == COLOR ? Color.RED : COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            ledColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> ledColorProperty() {
        if (null == ledColor) {
            ledColor  = new ObjectPropertyBase<Color>(_ledColor) {
                @Override protected void invalidated() {
                    if (null == get()) set(Color.RED);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "ledColor"; }
            };
            _ledColor = null;
        }
        return ledColor;
    }

    /**
     * Returns the graphical representation of the LED.
     * The values are STANDARD and FLAT
     * In principle this represents how the LED will be filled (gradient or color).
     *
     * @return the graphical representation of the LED
     */
    public LedType getLedType() { return null == ledType ? _ledType : ledType.get(); }
    /**
     * Defines the graphical representation of the LED.
     * The values are STANDARD and FLAT
     * In principle this represents how the LED will be filled (gradient or color).
     *
     * @param TYPE
     */
    public void setLedType(final LedType TYPE) {
        if (null == ledType) {
            _ledType = null == TYPE ? LedType.STANDARD : TYPE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            ledType.set(TYPE);
        }
    }
    public ObjectProperty<LedType> ledTypeProperty() {
        if (null == ledType) {
            ledType  = new ObjectPropertyBase<LedType>(_ledType) {
                @Override protected void invalidated() {
                    if(null == get()) set(LedType.STANDARD);
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "ledType"; }
            };
            _ledType = null;
        }
        return ledType;
    }

    /**
     * Returns the color that will be used to colorize the title
     * of the gauge.
     *
     * @return the color that will be used to colorize the title
     */
    public Color getTitleColor() { return null == titleColor ? _titleColor : titleColor.get(); }
    /**
     * Defines the color that will be used to colorize the title
     * of the gauge.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "titleColor"; }
            };
            _titleColor = null;
        }
        return titleColor;
    }

    /**
     * Returns the color that will be used to colorize the subTitle
     * of the gauge.
     *
     * @return the color that will be used to colorize the subTitle
     */
    public Color getSubTitleColor() { return null == subTitleColor ? _subTitleColor : subTitleColor.get(); }
    /**
     * Defines the color that will be used to colorize the subTitle
     * of the gauge.
     *
     * @param COLOR
     */
    public void setSubTitleColor(final Color COLOR) {
        if (null == subTitleColor) {
            _subTitleColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            subTitleColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> subTitleColorProperty() {
        if (null == subTitleColor) {
            subTitleColor  = new ObjectPropertyBase<Color>(_subTitleColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "subTitleColor"; }
            };
            _subTitleColor = null;
        }
        return subTitleColor;
    }

    /**
     * Returns the color that will be used to colorize the unit
     * of the gauge.
     *
     * @return the color that will be used to colorize the unit
     */
    public Color getUnitColor() { return null == unitColor ? _unitColor : unitColor.get(); }
    /**
     * Defines the color that will be used to colorize the unit
     * of the gauge.
     *
     * @param COLOR
     */
    public void setUnitColor(final Color COLOR) {
        if (null == unitColor) {
            _unitColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            unitColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> unitColorProperty() {
        if (null == unitColor) {
            unitColor  = new ObjectPropertyBase<Color>(_unitColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "unitColor"; }
            };
            _unitColor = null;
        }
        return unitColor;
    }

    /**
     * Returns the color that will be used to colorize the value
     * of the gauge.
     *
     * @return the color that will be used to colorize the value
     */
    public Color getValueColor() { return null == valueColor ? _valueColor : valueColor.get(); }
    /**
     * Defines the color that will be used to colorize the value
     * of the gauge.
     *
     * @param COLOR
     */
    public void setValueColor(final Color COLOR) {
        if (null == valueColor) {
            _valueColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            valueColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> valueColorProperty() {
        if (null == valueColor) {
            valueColor  = new ObjectPropertyBase<Color>(_valueColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "valueColor"; }
            };
            _valueColor = null;
        }
        return valueColor;
    }

    /**
     * Returns the color that will be used to colorize the threshold
     * indicator of the gauge.
     *
     * @return the color that will be used to colorize the threshold indicator
     */
    public Color getThresholdColor() { return null == thresholdColor ? _thresholdColor : thresholdColor.get(); }
    /**
     * Defines the color that will be used to colorize the threshold
     * indicator of the gauge.
     *
     * @param COLOR
     */
    public void setThresholdColor(final Color COLOR) {
        if (null == thresholdColor) {
            _thresholdColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            thresholdColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> thresholdColorProperty() {
        if (null == thresholdColor) {
            thresholdColor  = new ObjectPropertyBase<Color>(_thresholdColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "thresholdColor"; }
            };
            _thresholdColor = null;
        }
        return thresholdColor;
    }

    /**
     * Returns the color that will be used to colorize the average
     * indicator of the gauge.
     *
     * @return the color that will be used to colorize the average indicator
     */
    public Color getAverageColor() { return null == averageColor ? _averageColor : averageColor.get(); }
    /**
     * Defines the color that will be used to colorize the average
     * indicator of the gauge.
     *
     * @param COLOR
     */
    public void setAverageColor(final Color COLOR) {
        if (null == averageColor) {
            _averageColor = COLOR;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            averageColor.set(COLOR);
        }
    }
    public ObjectProperty<Color> averageColorProperty() {
        if (null == averageColor) {
            averageColor  = new ObjectPropertyBase<Color>(_averageColor) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "averageColor"; }
            };
            _averageColor = null;
        }
        return averageColor;
    }
    
    /**
     * Returns true if the value of the gauge should be checked against
     * all sections (if sections not empty). If a value enters a section
     * or leaves a section it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the gauge should be checked against all sections
     */
    public boolean getCheckSectionsForValue() { return null == checkSectionsForValue ? _checkSectionsForValue : checkSectionsForValue.get(); }
    /**
     * Defines if the value of the gauge should be checked against
     * all sections (if sections not empty). If a value enters a section
     * or leaves a section it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckSectionsForValue(final boolean CHECK) {
        if (null == checkSectionsForValue) { _checkSectionsForValue = CHECK; } else { checkSectionsForValue.set(CHECK); }
    }
    public BooleanProperty checkSectionsForValueProperty() {
        if (null == checkSectionsForValue) { checkSectionsForValue = new SimpleBooleanProperty(Gauge.this, "checkSectionsForValue", _checkSectionsForValue); }
        return checkSectionsForValue;
    }

    /**
     * Returns true if the value of the gauge should be checked against
     * all areas (if areas not empty). If a value enters an area
     * or leaves an area it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the the value of the gauge should be checked against all areas
     */
    public boolean getCheckAreasForValue() { return null == checkAreasForValue ? _checkAreasForValue : checkAreasForValue.get(); }
    /**
     * Defines if the value of the gauge should be checked against
     * all areas (if areas not empty). If a value enters an area
     * or leaves an area it will fire an event. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckAreasForValue(final boolean CHECK) {
        if (null == checkAreasForValue) { _checkAreasForValue = CHECK; } else { checkAreasForValue.set(CHECK); }
    }
    public BooleanProperty checkAreasForValueProperty() {
        if (null == checkAreasForValue) { checkAreasForValue = new SimpleBooleanProperty(Gauge.this, "checkAreasForValue", _checkAreasForValue); }
        return checkAreasForValue;
    }

    /**
     * Returns true if the value of the gauge should be checked against
     * the threshold. If a value crosses the threshold it will fire an
     * event (EXCEEDED and UNDERRUN. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @return true if the value of the gauge should be checked against the threshold
     */
    public boolean isCheckThreshold() { return null == checkThreshold ? _checkThreshold : checkThreshold.get(); }
    /**
     * Defines if the value of the gauge should be checked against
     * the threshold. If a value crosses the threshold it will fire an
     * event (EXCEEDED and UNDERRUN. The check will be performed
     * after the animation is finished (if animated == true).
     *
     * @param CHECK
     */
    public void setCheckThreshold(final boolean CHECK) {
        if (null == checkThreshold) {
            _checkThreshold = CHECK;
        } else {
            checkThreshold.set(CHECK);
        }
    }
    public BooleanProperty checkThresholdProperty() {
        if (null == checkThreshold) { checkThreshold = new SimpleBooleanProperty(Gauge.this, "checkThreshold", _checkThreshold); }
        return checkThreshold;
    }

    /**
     * Returns true if an inner shadow should be drawn on the gauge
     * background.
     *
     * @return true if an inner shadow should be drawn on the gauge background
     */
    public boolean isInnerShadowEnabled() { return null == innerShadowEnabled ? _innerShadowEnabled : innerShadowEnabled.get(); }
    /**
     * Defines if an inner shadow should be drawn on the gauge
     * background.
     *
     * @param ENABLED
     */
    public void setInnerShadowEnabled(final boolean ENABLED) {
        if (null == innerShadowEnabled) {
            _innerShadowEnabled = ENABLED;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            innerShadowEnabled.set(ENABLED);
        }
    }
    public BooleanProperty innerShadowEnabledProperty() {
        if (null == innerShadowEnabled) {
            innerShadowEnabled = new BooleanPropertyBase(_innerShadowEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "innerShadowEnabled"; }
            };
        }
        return innerShadowEnabled;
    }

    /**
     * Returns true if the threshold indicator should be drawn.
     *
     * @return true if the threshold indicator should be drawn
     */
    public boolean isThresholdVisible() { return null == thresholdVisible ? _thresholdVisible : thresholdVisible.get(); }
    /**
     * Defines if the threshold indicator should be drawn
     *
     * @param VISIBLE
     */
    public void setThresholdVisible(final boolean VISIBLE) {
        if (null == thresholdVisible) {
            _thresholdVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            thresholdVisible.set(VISIBLE);
        }
    }
    public BooleanProperty thresholdVisibleProperty() {
        if (null == thresholdVisible) {
            thresholdVisible = new BooleanPropertyBase(_thresholdVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "thresholdVisible"; }
            };
        }
        return thresholdVisible;
    }

    /**
     * Returns true if the average indicator should be drawn.
     *
     * @return true if the average indicator should be drawn
     */
    public boolean isAverageVisible() { return null == averageVisible ? _averageVisible : averageVisible.get(); }
    /**
     * Defines if the average indicator should be drawn
     *
     * @param VISIBLE
     */
    public void setAverageVisible(final boolean VISIBLE) {
        if (null == averageVisible) {
            _averageVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            averageVisible.set(VISIBLE);
        }
    }
    public BooleanProperty averageVisibleProperty() {
        if (null == averageVisible) {
            averageVisible = new BooleanPropertyBase() {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "averageVisible"; }
            };
        }
        return averageVisible;
    }

    /**
     * Returns true if the sections will be drawn
     *
     * @return true if the sections will be drawn
     */
    public boolean getSectionsVisible() { return null == sectionsVisible ? _sectionsVisible : sectionsVisible.get(); }
    /**
     * Defines if the sections will be drawn
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "sectionsVisible"; }
            };
        }
        return sectionsVisible;
    }

    /**
     * Returns true if the sections in the IndicatorSkin
     * will always be visible
     * @return
     */
    public boolean getSectionsAlwaysVisible() { return null == sectionsAlwaysVisible ? _sectionsAlwaysVisible : sectionsAlwaysVisible.get(); }
    /**
     * Defines if the sections will always be visible.
     * This is currently only used in the IndicatorSkin
     * @param VISIBLE
     */
    public void setSectionsAlwaysVisible(final boolean VISIBLE) {
        if (null == sectionsAlwaysVisible) {
            _sectionsAlwaysVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            sectionsAlwaysVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionsAlwaysVisibleProperty() {
        if (null == sectionsAlwaysVisible) {
            sectionsAlwaysVisible = new BooleanPropertyBase(_sectionsAlwaysVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "sectionsAlwaysVisible"; }
            };
        }
        return sectionsAlwaysVisible;
    }

    /**
     * Returns true if the text of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @return true if the text of the sections should be drawn
     */
    public boolean isSectionTextVisible() { return null == sectionTextVisible ? _sectionTextVisible : sectionTextVisible.get(); }
    /**
     * Defines if the text of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @param VISIBLE
     */
    public void setSectionTextVisible(final boolean VISIBLE) {
        if (null == sectionTextVisible) {
            _sectionTextVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            sectionTextVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionTextVisibleProperty() {
        if (null == sectionTextVisible) {
            sectionTextVisible = new BooleanPropertyBase(_sectionTextVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "sectionTextVisible"; }
            };
        }
        return sectionTextVisible;
    }

    /**
     * Returns true if the icon of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @return true if the icon of the sections should be drawn
     */
    public boolean getSectionIconsVisible() { return null == sectionIconsVisible ? _sectionIconsVisible : sectionIconsVisible.get(); }
    /**
     * Defines if the icon of the sections should be drawn inside
     * the sections. This is currently only used in the SimpleSkin.
     *
     * @param VISIBLE
     */
    public void setSectionIconsVisible(final boolean VISIBLE) {
        if (null == sectionIconsVisible) {
            _sectionIconsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            sectionIconsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty sectionIconsVisibleProperty() {
        if (null == sectionIconsVisible) {
            sectionIconsVisible = new BooleanPropertyBase(_sectionIconsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "sectionIconsVisible"; }
            };
        }
        return sectionIconsVisible;
    }

    /**
     * Returns true if sections should be highlighted in case they
     * contain the current value.
     *
     * @return true if sections should be highlighted
     */
    public boolean isHighlightSections() { return null == highlightSections ? _highlightSections : highlightSections.get(); }
    /**
     * Defines if sections should be highlighted in case they
     * contain the current value
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "highlightSections"; }
            };
        }
        return highlightSections;
    }

    /**
     * Returns true if the areas should be drawn
     *
     * @return true if the areas should be drawn
     */
    public boolean getAreasVisible() { return null == areasVisible ? _areasVisible : areasVisible.get(); }
    /**
     * Defines if the areas should be drawn
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "areasVisible"; }
            };
        }
        return areasVisible;
    }

    /**
     * Returns true if the text of the areas should be drawn inside
     * the areas. This is currently only used in the SimpleSkin.
     *
     * @return true if the text of the areas should be drawn
     */
    public boolean isAreaTextVisible() { return null == areaTextVisible ? _areaTextVisible : areaTextVisible.get(); }
    /**
     * Defines if the text of the areas should be drawn inside
     * the areas.
     *
     * @param VISIBLE
     */
    public void setAreaTextVisible(final boolean VISIBLE) {
        if (null == areaTextVisible) {
            _areaTextVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            areaTextVisible.set(VISIBLE);
        }
    }
    public BooleanProperty areaTextVisibleProperty() {
        if (null == areaTextVisible) {
            areaTextVisible = new BooleanPropertyBase(_areaTextVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "areaTextVisible"; }
            };
        }
        return areaTextVisible;
    }

    /**
     * Returns true if the icon of the areas should be drawn inside
     * the areas.
     *
     * @return true if the icon of the areas should be drawn
     */
    public boolean getAreaIconsVisible() { return null == areaIconsVisible ? _areaIconsVisible : areaIconsVisible.get(); }
    /**
     * Defines if the icon of the areas should be drawn inside
     * the areas.
     *
     * @param VISIBLE
     */
    public void setAreaIconsVisible(final boolean VISIBLE) {
        if (null == areaIconsVisible) {
            _areaIconsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            areaIconsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty areaIconsVisibleProperty() {
        if (null == areaIconsVisible) {
            areaIconsVisible = new BooleanPropertyBase(_areaIconsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "areaIconsVisible"; }
            };
        }
        return areaIconsVisible;
    }

    /**
     * Returns true if areas should be highlighted in case they
     * contain the current value.
     *
     * @return true if areas should be highlighted
     */
    public boolean isHighlightAreas() { return null == highlightAreas ? _highlightAreas : highlightAreas.get(); }
    /**
     * Defines if areas should be highlighted in case they
     * contain the current value
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "highlightAreas"; }
            };
        }
        return highlightAreas;
    }

    /**
     * Returns true if the tickmark sections should be used to
     * colorize the tickmarks.
     *
     * @return true if the tickmark sections should be used
     */
    public boolean getTickMarkSectionsVisible() { return null == tickMarkSectionsVisible ? _tickMarkSectionsVisible : tickMarkSectionsVisible.get(); }
    /**
     * Defines if the tickmark sections should be used to
     * colorize the tickmarks.
     *
     * @param VISIBLE
     */
    public void setTickMarkSectionsVisible(final boolean VISIBLE) {
        if (null == tickMarkSectionsVisible) {
            _tickMarkSectionsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickMarkSectionsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty tickMarkSectionsVisibleProperty() {
        if (null == tickMarkSectionsVisible) {
            tickMarkSectionsVisible = new BooleanPropertyBase(_tickMarkSectionsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickMarkSectionsVisible"; }
            };
        }
        return tickMarkSectionsVisible;
    }

    /**
     * Returns true if the ticklabel sections should be used
     * to colorize the ticklabels.
     *
     * @return true if the ticklabel sections should be used
     */
    public boolean getTickLabelSectionsVisible() { return null == tickLabelSectionsVisible ? _tickLabelSectionsVisible : tickLabelSectionsVisible.get(); }
    /**
     * Defines if the ticklabel sections should be used to
     * colorize the ticklabels.
     *
     * @param VISIBLE
     */
    public void setTickLabelSectionsVisible(final boolean VISIBLE) {
        if (null == tickLabelSectionsVisible) {
            _tickLabelSectionsVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickLabelSectionsVisible.set(VISIBLE);
        }
    }
    public BooleanProperty tickLabelSectionsVisibleProperty() {
        if (null == tickLabelSectionsVisible) {
            tickLabelSectionsVisible = new BooleanPropertyBase(_tickLabelSectionsVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelSectionsVisible"; }
            };
        }
        return tickLabelSectionsVisible;
    }

    /**
     * Returns true if the markers should be drawn
     *
     * @return true if the markser should be drawn
     */
    public boolean getMarkersVisible() { return null == markersVisible ? _markersVisible : markersVisible.get(); }
    /**
     * Defines if the markers should be drawn
     *
     * @param VISIBLE
     */
    public void setMarkersVisible(final boolean VISIBLE) {
        if (null == markersVisible) {
            _markersVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            markersVisible.set(VISIBLE);
        }
    }
    public BooleanProperty markersVisibleProperty() {
        if (null == markersVisible) {
            markersVisible = new BooleanPropertyBase(_markersVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "markersVisible"; }
            };
        }
        return markersVisible;
    }

    /**
     * Returns true if the ticklabels should be drawn
     *
     * @return true if the ticklabels should be drawn
     */
    public boolean getTickLabelsVisible() { return null == tickLabelsVisible ? _tickLabelsVisible : tickLabelsVisible.get(); }
    /**
     * Defines if the ticklabels should be drawn
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickLabelsVisible"; }
            };
        }
        return tickLabelsVisible;
    }

    /**
     * Returns true if only the first and the last ticklabel
     * will be drawn. Sometimes this could be useful if a gauge
     * should for example only should show 0 and 1000.
     *
     * @return true if only the first and last ticklabel will be drawn
     */
    public boolean isOnlyFirstAndLastTickLabelVisible() {
        return null == onlyFirstAndLastTickLabelVisible ? _onlyFirstAndLastTickLabelVisible : onlyFirstAndLastTickLabelVisible.get();
    }
    /**
     * Defines if only the first and the last ticklabel
     * will be drawn. Sometimes this could be useful if a gauge
     * should for example only should show 0 and 1000.
     *
     * @param VISIBLE
     */
    public void setOnlyFirstAndLastTickLabelVisible(final boolean VISIBLE) {
        if (null == onlyFirstAndLastTickLabelVisible) {
            _onlyFirstAndLastTickLabelVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            onlyFirstAndLastTickLabelVisible.set(VISIBLE);
        }
    }
    public BooleanProperty onlyFirstAndLastTickLabelVisibleProperty() {
        if (null == onlyFirstAndLastTickLabelVisible) {
            onlyFirstAndLastTickLabelVisible = new BooleanPropertyBase(_onlyFirstAndLastTickLabelVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "onlyFirstAndLastTickLabelVisible"; }
            };
        }
        return onlyFirstAndLastTickLabelVisible;
    }

    /**
     * Returns true if the major tickmarks should be drawn
     * If set to false and minorTickmarks == true, a minor tickmark
     * will be drawn instead of the major tickmark.
     *
     * @return true if the major tickmarks should be drawn
     */
    public boolean getMajorTickMarksVisible() { return null == majorTickMarksVisible ? _majorTickMarksVisible : majorTickMarksVisible.get(); }
    /**
     * Defines if the major tickmarks should be drawn
     * If set to false and minorTickmarks == true, a minor tickmark
     * will be drawn instead of the major tickmark.
     *
     * @param VISIBLE
     */
    public void setMajorTickMarksVisible(final boolean VISIBLE) {
        if (null == majorTickMarksVisible) {
            _majorTickMarksVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            majorTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty majorTickMarksVisibleProperty() {
        if (null == majorTickMarksVisible) {
            majorTickMarksVisible = new BooleanPropertyBase(_majorTickMarksVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "majorTickMarksVisible"; }
            };
        }
        return majorTickMarksVisible;
    }

    /**
     * Returns true if the medium tickmarks should be drawn
     * If set to false and minorTickmarks == true, a minor tickmark
     * will be drawn instead of the medium tickmark.
     *
     * @return true if the medium tickmarks should be drawn
     */
    public boolean getMediumTickMarksVisible() { return null == mediumTickMarksVisible ? _mediumTickMarksVisible : mediumTickMarksVisible.get(); }
    public void setMediumTickMarksVisible(final boolean VISIBLE) {
        if (null == mediumTickMarksVisible) {
            _mediumTickMarksVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            mediumTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty mediumTickMarksVisibleProperty() {
        if (null == mediumTickMarksVisible) {
            mediumTickMarksVisible = new BooleanPropertyBase(_mediumTickMarksVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "mediumTickMarksVisible"; }
            };
        }
        return mediumTickMarksVisible;
    }

    /**
     * Returns true if the minor tickmarks should be drawn
     *
     * @return true if the minor tickmarks should be drawn
     */
    public boolean getMinorTickMarksVisible() { return null == minorTickMarksVisible ? _minorTickMarksVisible : minorTickMarksVisible.get(); }
    /**
     * Defines if the minor tickmarks should be drawn
     *
     * @param VISIBLE
     */
    public void setMinorTickMarksVisible(final boolean VISIBLE) {
        if (null == minorTickMarksVisible) {
            _minorTickMarksVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            minorTickMarksVisible.set(VISIBLE);
        }
    }
    public BooleanProperty minorTickMarksVisibleProperty() {
        if (null == minorTickMarksVisible) {
            minorTickMarksVisible = new BooleanPropertyBase(_minorTickMarksVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minorTickMarksVisible"; }
            };
        }
        return minorTickMarksVisible;
    }

    /**
     * Returns true when an additional ring will be drawn that "connects" the
     * tick marks.
     *
     * @return true when an additional ring will be drawn that "connects" the tick marks
     */
    public boolean isTickMarkRingVisible() { return null == tickMarkRingVisible ? _tickMarkRingVisible : tickMarkRingVisible.get(); }
    /**
     * Defines if an additional ring should be drawn that "connects" the tick marks.
     *
     * @param VISIBLE
     */
    public void setTickMarkRingVisible(final boolean VISIBLE) {
        if (null == tickMarkRingVisible) {
            _tickMarkRingVisible = VISIBLE;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            tickMarkRingVisible.set(VISIBLE);
        }
    }
    public BooleanProperty tickMarkRingVisibleProperty() {
        if (null == tickMarkRingVisible) {
            tickMarkRingVisible = new BooleanPropertyBase(_tickMarkRingVisible) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "tickMarkRingVisible"; }
            };
        }
        return tickMarkRingVisible;
    }

    /**
     * Returns the value that represents the space between major
     * tickmarks. This value will be automatically set by the
     * autoscale property. Be careful when changing it manually.
     *
     * @return the value that represents the space between major tickmarks
     */
    public double getMajorTickSpace() { return null == majorTickSpace ? _majorTickSpace : majorTickSpace.get(); }
    /**
     * Defines the value that represents the space between major
     * tickmarks. This value will be automatically set by the
     * autoscale property. Be careful when changing it manually.
     *
     * @param SPACE
     */
    public void setMajorTickSpace(final double SPACE) {
        if (null == majorTickSpace) {
            _majorTickSpace = SPACE;
            fireUpdateEvent(RECALC_EVENT);
        } else {
            majorTickSpace.set(SPACE);
        }
    }
    public DoubleProperty majorTickSpaceProperty() {
        if (null == majorTickSpace) {
            majorTickSpace = new DoublePropertyBase(_majorTickSpace) {
                @Override protected void invalidated() { fireUpdateEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "majorTickSpace"; }
            };
        }
        return majorTickSpace;
    }

    /**
     * Returns the value that represents the space between minor
     * tickmarks. This value will be automatically set by the
     * autoscale property. Be careful when changing it manually.
     *
     * @return the value that represents the space between minor tickmarks
     */
    public double getMinorTickSpace() { return null == minorTickSpace ? _minorTickSpace : minorTickSpace.get(); }
    /**
     * Defines the value that represents the space between major
     * tickmarks. This value will be automatically set by the
     * autoscale property. Be careful when changing it manually.
     *
     * @param SPACE
     */
    public void setMinorTickSpace(final double SPACE) {
        if (null == minorTickSpace) {
            _minorTickSpace = SPACE;
            fireUpdateEvent(RECALC_EVENT);
        } else {
            minorTickSpace.set(SPACE);
        }
    }
    public DoubleProperty minorTickSpaceProperty() {
        if (null == minorTickSpace) {
            minorTickSpace = new DoublePropertyBase(_minorTickSpace) {
                @Override protected void invalidated() { fireUpdateEvent(RECALC_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minorTickSpace"; }
            };
        }
        return minorTickSpace;
    }

    /**
     * Returns true if the LCD display is visible (if available)
     * The LCD display won't be visible if valueVisible == false.
     *
     * @return true if the LCD display is visible
     */
    public boolean isLcdVisible() { return null == lcdVisible ? _lcdVisible : lcdVisible.get(); }
    /**
     * Defines if the LCD display is visible (if available)
     * The LCD display won't be visible if valueVisible == false.
     *
     * @param VISIBLE
     */
    public void setLcdVisible(final boolean VISIBLE) {
        if (null == lcdVisible) {
            _lcdVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            lcdVisible.set(VISIBLE);
        }
    }
    public BooleanProperty lcdVisibleProperty() {
        if (null == lcdVisible) {
            lcdVisible = new BooleanPropertyBase(_lcdVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "lcdVisible"; }
            };
        }
        return lcdVisible;
    }

    /**
     * Returns true if the crystal effect of the LCD display will be drawn.
     * This feature could decrease the performance if you run it on
     * embedded devices because it will calculate a bitmap image where
     * each pixel will be calculated.
     *
     * @return true if the crystal effect of the LCD display will be drawn
     */
    public boolean isLcdCrystalEnabled() { return null == lcdCrystalEnabled ? _lcdCrystalEnabled : lcdCrystalEnabled.get(); }
    /**
     * Defines if the crystal effect of the LCD display will be drawn.
     * This feature could decrease the performance if you run it on
     * embedded devices because it will calculate a bitmap image where
     * each pixel will be calculated.
     *
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "lcdCrystalEnabled"; }
            };
        }
        return lcdCrystalEnabled;
    }

    /**
     * Returns true if the LED will be drawn (if available)
     *
     * @return true if the LED will be drawn
     */
    public boolean isLedVisible() { return null == ledVisible ? _ledVisible : ledVisible.get(); }
    /**
     * Defines if the LED will be drawn (if available)
     *
     * @param VISIBLE
     */
    public void setLedVisible(final boolean VISIBLE) {
        if (null == ledVisible) {
            _ledVisible = VISIBLE;
            fireUpdateEvent(VISIBILITY_EVENT);
        } else {
            ledVisible.set(VISIBLE);
        }
    }
    public BooleanProperty ledVisibleProperty() {
        if (null == ledVisible) {
            ledVisible = new BooleanPropertyBase(_ledVisible) {
                @Override protected void invalidated() { fireUpdateEvent(VISIBILITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "ledVisible"; }
            };
        }
        return ledVisible;
    }

    /**
     * Returns true if the LED is on (if available)
     *
     * @return true if the LED is on
     */
    public boolean isLedOn() { return null == ledOn ? _ledOn : ledOn.get(); }
    /**
     * Defines if the LED is on (if available)
     *
     * @param ON
     */
    public void setLedOn(final boolean ON) {
        if (null == ledOn) {
            _ledOn = ON;
            fireUpdateEvent(LED_EVENT);
        } else {
            ledOn.set(ON);
        }
    }
    public BooleanProperty ledOnProperty() {
        if (null == ledOn) {
            ledOn = new BooleanPropertyBase(_ledOn) {
                @Override protected void invalidated() { fireUpdateEvent(LED_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "ledOn"; }
            };
        }
        return ledOn;
    }

    /**
     * Returns true if the LED is blinking (if available)
     *
     * @return true if the LED is blinking
     */
    public boolean isLedBlinking() { return null == ledBlinking ? _ledBlinking : ledBlinking.get(); }
    /**
     * Defines if the LED is blinking (if available)
     *
     * @param BLINKING
     */
    public void setLedBlinking(final boolean BLINKING) {
        if (null == ledBlinking) {
            _ledBlinking = BLINKING;
            if (_ledBlinking) {
                startBlinkExecutorService();
            } else {
                if (null != blinkFuture) blinkFuture.cancel(true);
                setLedOn(false);
            }
        } else {
            ledBlinking.set(BLINKING);
        }
    }
    public BooleanProperty ledBlinkingProperty() {
        if (null == ledBlinking) {
            ledBlinking = new BooleanPropertyBase(_ledBlinking) {
                @Override protected void invalidated() {
                    if (get()) {
                        startBlinkExecutorService();
                    } else {
                        if (null != blinkFuture) blinkFuture.cancel(true);
                        setLedOn(false);
                    }
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "ledBlinking"; }
            };
        }
        return ledBlinking;
    }

    /**
     * Returns the orientation of the control. This feature
     * will only be used in the BulletChartSkin and LinearSkin.
     * Values are HORIZONTAL and VERTICAL
     *
     * @return the orientation of the control
     */
    public Orientation getOrientation() { return null == orientation ? _orientation : orientation.get(); }
    /**
     * Defines the orientation of the control. This feature
     * will only be used in the BulletChartSkin and LinearSkin.
     * Values are HORIZONTAL and VERTICAL
     *
     * @param ORIENTATION
     */
    public void setOrientation(final Orientation ORIENTATION) {
        if (null == orientation) {
            _orientation = ORIENTATION;
            fireUpdateEvent(RESIZE_EVENT);
        } else {
            orientation.set(ORIENTATION);
        }
    }
    public ObjectProperty<Orientation> orientationProperty() {
        if (null == orientation) {
            orientation  = new ObjectPropertyBase<Orientation>(_orientation) {
                @Override protected void invalidated() { fireUpdateEvent(RESIZE_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "orientation"; }
            };
            _orientation = null;
        }
        return orientation;
    }

    /**
     * Returns true if the gradient defined by the gradient lookup
     * will be used to visualize the bar (if available).
     *
     * @return true if the gradient defined by the gradient lookup will be used to visualize the bar
     */
    public boolean isGradientBarEnabled() { return null == gradientBarEnabled ? _gradientBarEnabled : gradientBarEnabled.get(); }
    /**
     * Defines if the gradient defined by the gradient lookup
     * will be used to visualize the bar (if available).
     *
     * @param ENABLED
     */
    public void setGradientBarEnabled(final boolean ENABLED) {
        if (null == gradientBarEnabled) {
            _gradientBarEnabled = ENABLED;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            gradientBarEnabled.set(ENABLED);
        }
    }
    public BooleanProperty gradientBarEnabledProperty() {
        if (null == gradientBarEnabled) {
            gradientBarEnabled = new BooleanPropertyBase(_gradientBarEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "gradientBarEnabled"; }
            };
        }
        return gradientBarEnabled;
    }

    /**
     * Returns the GradientLookup that is used to colorize the bar
     * of the gauge (if avaiable)
     *
     * @return the GradientLookup that is used to colorize the bar
     */
    public GradientLookup getGradientLookup() {
        if (null == gradientLookup) { gradientLookup = new GradientLookup(); }
        return gradientLookup;
    }
    /**
     * Defines the GradientLookup that is used to colorize the bar
     * of the gauge (if avaiable)
     *
     * @param GRADIENT_LOOKUP
     */
    public void setGradientLookup(final GradientLookup GRADIENT_LOOKUP) {
        gradientLookup = GRADIENT_LOOKUP;
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Returns a list of Stops that will be used to calculate the gradient
     * in the GradientLookup.
     *
     * @return a list of Stops that will be used to calculate the gradient in the GradientLookup
     */
    public List<Stop> getGradientBarStops() { return getGradientLookup().getStops(); }
    /**
     * Defines a list of Stops that will be used to calculate the gradient
     * in the GradientLookup.
     *
     * @param STOPS
     */
    public void setGradientBarStops(final Stop... STOPS) { setGradientBarStops(Arrays.asList(STOPS)); }
    /**
     * Defines a list of Stops that will be used to calculate the gradient
     * in the GradientLookup.
     *
     * @param STOPS
     */
    public void setGradientBarStops(final List<Stop> STOPS) {
        getGradientLookup().setStops(STOPS);
        fireUpdateEvent(REDRAW_EVENT);
    }

    /**
     * Returns true if custom ticklabels should be used instead of the
     * automatically calculated ones. This could be useful for gauges
     * like a compass where you need "N", "E", "S" and "W" instead of
     * numbers.
     *
     * @return true if custom ticklabels should be used
     */
    public boolean getCustomTickLabelsEnabled() { return null == customTickLabelsEnabled ? _customTickLabelsEnabled : customTickLabelsEnabled.get(); }
    /**
     * Defines if custom ticklabels should be used instead of the
     * automatically calculated ones. This could be useful for gauges
     * like a compass where you need "N", "E", "S" and "W" instead of
     * numbers.
     *
     * @param ENABLED
     */
    public void setCustomTickLabelsEnabled(final boolean ENABLED) {
        if (null == customTickLabelsEnabled) {
            _customTickLabelsEnabled = ENABLED;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            customTickLabelsEnabled.set(ENABLED);
        }
    }
    public BooleanProperty getCustomTickLabelsEnabledProperty() {
        if (null == customTickLabelsEnabled) {
            customTickLabelsEnabled = new BooleanPropertyBase(_customTickLabelsEnabled) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "customTickLabelsEnabled"; }
            };
        }
        return customTickLabelsEnabled;
    }

    /**
     * Returns a list of Strings that represent the ticklabels that
     * will be used for the scale.
     *
     * @return a list of Strings that represent the ticklabels
     */
    public List<String> getCustomTickLabels() { return customTickLabels; }
    /**
     * Defines a list of Strings that represent the ticklabels that
     * will be used for the scale.
     *
     * @param TICK_LABELS
     */
    public void setCustomTickLabels(final List<String> TICK_LABELS) {
        customTickLabels.setAll(TICK_LABELS);
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Defines a list of Strings that represent the ticklabels that
     * will be used for the scale.
     *
     * @param TICK_LABELS
     */
    public void setCustomTickLabels(final String... TICK_LABELS) { setCustomTickLabels(Arrays.asList(TICK_LABELS)); }
    /**
     * Adds the given String to the list of custom ticklabels
     *
     * @param TICK_LABEL
     */
    public void addCustomTickLabel(final String TICK_LABEL) {
        if (null == TICK_LABEL) return;
        if (!customTickLabels.contains(TICK_LABEL)) customTickLabels.add(TICK_LABEL);
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Removes the given String from the list of custom ticklabels
     *
     * @param TICK_LABEL
     */
    public void removeCustomTickLabel(final String TICK_LABEL) {
        if (null == TICK_LABEL) return;
        if (customTickLabels.contains(TICK_LABEL)) customTickLabels.remove(TICK_LABEL);
        fireUpdateEvent(REDRAW_EVENT);
    }
    /**
     * Clears the list of custom ticklabels
     */
    public void clearCustomTickLabels() {
        customTickLabels.clear();
        fireUpdateEvent(REDRAW_EVENT);
    }

    /**
     * Returns the custom font size. The default font size is 18px at
     * a size of 250px. This value will be used to calculate the current
     * font size for the ticklabels when scaling.
     *
     * @return the custom font size
     */
    public double getCustomTickLabelFontSize() { return null == customTickLabelFontSize ? _customTickLabelFontSize : customTickLabelFontSize.get(); }
    /**
     * Defines the custom font size. The default font size is 18px at
     * a size of 250px. This value will be used to calculate the current
     * font size for the ticklabels when scaling.
     *
     * @param SIZE
     */
    public void setCustomTickLabelFontSize(final double SIZE) {
        if (null == customTickLabelFontSize) {
            _customTickLabelFontSize = Helper.clamp(0.0, 72.0, SIZE);
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            customTickLabelFontSize.set(SIZE);
        }
    }
    public DoubleProperty customTickLabelFontSizeProperty() {
        if (null == customTickLabelFontSize) {
            customTickLabelFontSize = new DoublePropertyBase(_customTickLabelFontSize) {
                @Override protected void invalidated() {
                    final double SIZE = get();
                    if (SIZE < 0 || SIZE > 72) set(Helper.clamp(0.0, 72.0, SIZE));
                    fireUpdateEvent(REDRAW_EVENT);
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "customTickLabelFontSize";}
            };
        }
        return customTickLabelFontSize;
    }

    /**
     * Returns true if the gauge is in interactive mode. This is currently
     * implemented in the radial gauges that have a knob. If interactive == true
     * the knob can be pressed to trigger something.
     *
     * @return true if the gauge is in interactive mode
     */
    public boolean isInteractive() { return null == interactive ? _interactive : interactive.get(); }
    /**
     * Defines if the gauge is in interactive mode. This is currently
     * implemented in the radial gauges that have a knob. If interactive == true
     * the knob can be pressed to trigger something.
     *
     * @param INTERACTIVE
     */
    public void setInteractive(final boolean INTERACTIVE) {
        if (null == interactive) {
            _interactive = INTERACTIVE;
            fireUpdateEvent(INTERACTIVITY_EVENT);
        } else {
            interactive.set(INTERACTIVE);
        }
    }
    public BooleanProperty interactiveProperty() {
        if (null == interactive) {
            interactive = new BooleanPropertyBase(_interactive) {
                @Override protected void invalidated() { fireUpdateEvent(INTERACTIVITY_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "interactive"; }
            };
        }
        return interactive;
    }

    /**
     * Returns the text that will be shown in the button tooltip. The
     * knob in the radial gauges acts as button if interactive == true.
     *
     * @return the text that will be shown in the button tooltip
     */
    public String getButtonTooltipText() { return null == buttonTooltipText ? _buttonTooltipText : buttonTooltipText.get(); }
    /**
     * Defines the text that will be shown in the button tooltip. The
     * knob in the radial gauges acts as button if interactive == true.
     *
     * @param TEXT
     */
    public void setButtonTooltipText(final String TEXT) {
        if (null == buttonTooltipText) {
            _buttonTooltipText = TEXT;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            buttonTooltipText.set(TEXT);
        }
    }
    public StringProperty buttonTooltipTextProperty() {
        if (null == buttonTooltipText) {
            buttonTooltipText  = new StringPropertyBase(_buttonTooltipText) {
                @Override protected  void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "buttonTooltipText"; }
            };
            _buttonTooltipText = null;
        }
        return buttonTooltipText;
    }

    /**
     * Returns true if the control should keep it's aspect. This is
     * in principle only needed if the control has different width and
     * height.
     *
     * @return true if the control should keep it's aspect
     */
    public boolean isKeepAspect() { return null == keepAspect ? _keepAspect : keepAspect.get(); }
    /**
     * Defines if the control should keep it's aspect. This is
     * in principle only needed if the control has different width and
     * height.
     *
     * @param KEEP
     */
    public void setKeepAspect(final boolean KEEP) {
        if (null == keepAspect) {
            _keepAspect = KEEP;
        } else {
            keepAspect.set(KEEP);
        }
    }
    public BooleanProperty keepAspectProperty() {
        if (null == keepAspect) { keepAspect = new SimpleBooleanProperty(Gauge.this, "keepAspect", _keepAspect); }
        return keepAspect;
    }

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
                @Override public Object getBean() { return Gauge.this; }
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
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "customFont"; }
            };
            _customFont = null;
        }
        return customFont;
    }

    /**
     * Returns true if the alert property was set.
     * This property can be used to visualize an alert
     * situation in a skin.
     * @return true if the alert property was set
     */
    public boolean isAlert() { return null == alert ? _alert : alert.get(); }
    /**
     * Defines if the alert property should be set. This
     * property can be used to visualize an alert situation
     * in the skin.
     * @param ALERT
     */
    public void setAlert(final boolean ALERT) {
        if (null == alert) {
            _alert = ALERT;
            fireUpdateEvent(ALERT_EVENT);
        } else {
            alert.set(ALERT);
        }
    }
    public BooleanProperty alertProperty() {
        if (null == alert) {
            alert = new BooleanPropertyBase(_alert) {
                @Override protected void invalidated() { fireUpdateEvent(ALERT_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "alert"; }
            };
        }
        return alert;
    }

    /**
     * Returns the alert message text that could be used in a tooltip
     * in case of an alert.
     * @return the alert message text
     */
    public String getAlertMessage() { return null == alertMessage ? _alertMessage : alertMessage.get(); }
    /**
     * Defines the text that could be used in a tooltip as an
     * alert message.
     * @param MESSAGE
     */
    public void setAlertMessage(final String MESSAGE) {
        if (null == alertMessage) {
            _alertMessage = MESSAGE;
            fireUpdateEvent(ALERT_EVENT);
        } else {
            alertMessage.set(MESSAGE);
        }
    }
    public StringProperty alertMessageProperty() {
        if (null == alertMessage) {
            alertMessage = new StringPropertyBase(_alertMessage) {
                @Override protected void invalidated() { fireUpdateEvent(ALERT_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "alertMessage"; }
            };
            _alertMessage = null;
        }
        return alertMessage;
    }

    /**
     * Returns true when smoothing is enabled. This property is only used
     * in the TileSparklineSkin to smooth the path. In a custom skin it
     * could be also used for other things.
     * @return true when smoothing is enabled
     */
    public boolean isSmoothing() { return null == smoothing ? _smoothing : smoothing.get(); }
    /**
     * Defines if the smoothing property should be enabled/disabled.
     * At the moment this is only used in the TileSparklineSkin.
     * @param SMOOTHING
     */
    public void setSmoothing(final boolean SMOOTHING) {
        if (null == smoothing) {
            _smoothing = SMOOTHING;
            fireUpdateEvent(REDRAW_EVENT);
        } else {
            smoothing.set(SMOOTHING);
        }
    }
    public BooleanProperty smoothingProperty() {
        if (null == smoothing) {
            smoothing = new BooleanPropertyBase(_smoothing) {
                @Override protected void invalidated() { fireUpdateEvent(REDRAW_EVENT); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "smoothing"; }
            };
        }
        return smoothing;
    }

    public String getFormatString() { return formatString; }
    private void updateFormatString() {
        StringBuilder formatBuilder = new StringBuilder("%.").append(getDecimals()).append("f");
        String        format        = formatBuilder.toString();
        int           minLength     = String.format(Locale.US, format, getMinValue()).length();
        int           maxLength     = String.format(Locale.US, format, getMaxValue()).length();
        int           length        = Math.max(minLength, maxLength);
        formatBuilder.setLength(0);
        formatBuilder.append("%").append(length).append(".").append(getDecimals()).append("f");
        formatString = formatBuilder.toString();
        fireUpdateEvent(RESIZE_EVENT);
    }

    /**
     * Calling this method will lead to a recalculation of the scale
     */
    public void calcAutoScale() {
        double maxNoOfMajorTicks = 10;
        double maxNoOfMinorTicks = 10;
        double niceRange         = (Helper.calcNiceNumber(getRange(), false));
        setMajorTickSpace(Helper.calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true));
        double niceMinValue = (Math.floor(getMinValue() / getMajorTickSpace()) * getMajorTickSpace());
        double niceMaxValue = (Math.ceil(getMaxValue() / getMajorTickSpace()) * getMajorTickSpace());
        setMinorTickSpace(Helper.calcNiceNumber(getMajorTickSpace() / (maxNoOfMinorTicks - 1), true));
        setMinValue(niceMinValue);
        setMaxValue(niceMaxValue);
    }


    // ******************** Misc **********************************************
    private synchronized void createBlinkTask() {
        blinkTask = new Callable<Void>() {
            @Override public Void call() throws Exception {
                try {
                    Platform.runLater(() -> setLedOn(!isLedOn()));
                } finally {
                    if (!Thread.currentThread().isInterrupted()) {
                        // Schedule the same Callable with the current updateInterval
                        blinkFuture = blinkService.schedule(this, LED_BLINK_INTERVAL, TimeUnit.MILLISECONDS);
                    }
                }
                return null;
            }
        };
    }
    private synchronized void startBlinkExecutorService() {
        if (null == blinkTask) { createBlinkTask(); }
        if (null == blinkService) { blinkService = new ScheduledThreadPoolExecutor(1, Helper.getThreadFactory("BlinkTask", true)); }
        blinkFuture = blinkService.schedule(blinkTask, LED_BLINK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Calling this method will stop all threads. This is needed when using
     * JavaFX on mobile devices when the device goes to sleep mode.
     */
    public void stop() {
        setLedOn(false);
        if (null != blinkFuture) { blinkFuture.cancel(true); }
        if (null != blinkService) { blinkService.shutdownNow(); }
    }

    private void createShutdownHook() { Runtime.getRuntime().addShutdownHook(new Thread(() -> stop())); }

    @Override public String toString() {
        return new StringBuilder("{")
            .append("\"title\":").append("\"").append(getTitle()).append("\",")
            .append("\"subTitle\":").append("\"").append(getTitle()).append("\",")
            .append("\"unit\":").append("\"").append(getUnit()).append("\",")
            .append("\"value\":").append(getValue()).append(",")
            .append("\"minValue\":").append(getMinValue()).append(",")
            .append("\"maxValue\":").append(getMaxValue()).append(",")
            .append("\"threshold\":").append(getThreshold()).append(",")
            .append("\"minMeasuredValue\":").append(getMinMeasuredValue()).append(",")
            .append("\"maxMeasuredValue\":").append(getMaxMeasuredValue())
            .append("}").toString();
    }


    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() {
        switch (skinType) {
            case AMP            : return new AmpSkin(Gauge.this);
            case BULLET_CHART   : return new BulletChartSkin(Gauge.this);
            case DASHBOARD      : return new DashboardSkin(Gauge.this);
            case FLAT           : return new FlatSkin(Gauge.this);
            case INDICATOR      : return new IndicatorSkin(Gauge.this);
            case KPI            : return new KpiSkin(Gauge.this);
            case MODERN         : return new ModernSkin(Gauge.this);
            case SIMPLE         : return new SimpleSkin(Gauge.this);
            case SLIM           : return new SlimSkin(Gauge.this);
            case SPACE_X        : return new SpaceXSkin(Gauge.this);
            case QUARTER        : return new QuarterSkin(Gauge.this);
            case HORIZONTAL     : return new HSkin(Gauge.this);
            case VERTICAL       : return new VSkin(Gauge.this);
            case LCD            : return new LcdSkin(Gauge.this);
            case TINY           : return new TinySkin(Gauge.this);
            case BATTERY        : return new BatterySkin(Gauge.this);
            case LEVEL          : return new LevelSkin(Gauge.this);
            case LINEAR         : return new LinearSkin(Gauge.this);
            case DIGITAL        : return new DigitalSkin(Gauge.this);
            case SIMPLE_DIGITAL : return new SimpleDigitalSkin(Gauge.this);
            case SECTION        : return new SectionSkin(Gauge.this);
            case BAR            : return new BarSkin(Gauge.this);
            case WHITE          : return new WhiteSkin(Gauge.this);
            case CHARGE         : return new ChargeSkin(Gauge.this);
            case SIMPLE_SECTION : return new SimpleSectionSkin(Gauge.this);
            case TILE_KPI       : return new TileKpiSkin(Gauge.this);
            case TILE_TEXT_KPI  : return new TileTextKpiSkin(Gauge.this);
            case TILE_SPARK_LINE: return new TileSparklineSkin(Gauge.this);
            case GAUGE          :
            default             : return new GaugeSkin(Gauge.this);
        }
    }

    @Override public String getUserAgentStylesheet() { return getClass().getResource("gauge.css").toExternalForm(); }

    public SkinType getSkinType() { return skinType; }
    public void setSkinType(final SkinType SKIN_TYPE) {
        skinType = SKIN_TYPE;
        switch (SKIN_TYPE) {
            case AMP:
                setKnobPosition(Pos.BOTTOM_CENTER);
                setTitleColor(Color.WHITE);
                setLedVisible(true);
                setBackgroundPaint(Color.WHITE);
                setForegroundPaint(Color.BLACK);
                setLcdVisible(true);
                setShadowsEnabled(true);
                super.setSkin(new AmpSkin(Gauge.this));
                break;
            case BULLET_CHART:
                setKnobPosition(Pos.CENTER);
                setBarColor(Color.BLACK);
                setThresholdColor(Color.BLACK);
                super.setSkin(new BulletChartSkin(Gauge.this));
                break;
            case DASHBOARD:
                setKnobPosition(Pos.BOTTOM_CENTER);
                setDecimals(0);
                setBarBackgroundColor(Color.LIGHTGRAY);
                setBarColor(Color.rgb(93, 190, 205));
                super.setSkin(new DashboardSkin(Gauge.this));
                break;
            case FLAT:
                setKnobPosition(Pos.CENTER);
                setBarColor(Color.CYAN);
                setBackgroundPaint(Color.TRANSPARENT);
                setTitleColor(Gauge.DARK_COLOR);
                setValueColor(Gauge.DARK_COLOR);
                setUnitColor(Gauge.DARK_COLOR);
                setBorderPaint(Color.rgb(208, 208, 208));
                setDecimals(0);
                super.setSkin(new FlatSkin(Gauge.this));
                break;
            case INDICATOR:
                setKnobPosition(Pos.BOTTOM_CENTER);
                setValueVisible(false);
                setGradientBarEnabled(false);
                setGradientBarStops(new Stop(0.0, Color.rgb(34, 180, 11)),
                                    new Stop(0.5, Color.rgb(255, 146, 0)),
                                    new Stop(1.0, Color.rgb(255, 0, 39)));
                setTickLabelsVisible(false);
                setNeedleColor(Color.rgb(71, 71, 71));
                setBarBackgroundColor(Color.rgb(232, 231, 223));
                setBarColor(Color.rgb(255, 0, 39));
                setAngleRange(180);
                super.setSkin(new IndicatorSkin(Gauge.this));
                break;
            case KPI:
                setKnobPosition(Pos.BOTTOM_CENTER);
                setDecimals(0);
                setForegroundBaseColor(Color.rgb(126, 126, 127));
                setBarColor(Color.rgb(168, 204, 254));
                setThresholdVisible(true);
                setThresholdColor(Color.rgb(45, 86, 184));
                setNeedleColor(Color.rgb(74, 74, 74));
                setAngleRange(128);
                super.setSkin(new KpiSkin(Gauge.this));
                break;
            case MODERN:
                setKnobPosition(Pos.CENTER);
                setDecimals(0);
                setValueColor(Color.WHITE);
                setTitleColor(Color.WHITE);
                setSubTitleColor(Color.WHITE);
                setUnitColor(Color.WHITE);
                setBarColor(Color.rgb(0, 214, 215));
                setNeedleColor(Color.WHITE);
                setThresholdColor(Color.rgb(204, 0, 0));
                setTickLabelColor(Color.rgb(151, 151, 151));
                setTickMarkColor(Color.BLACK);
                setTickLabelOrientation(TickLabelOrientation.ORTHOGONAL);
                super.setSkin(new ModernSkin(Gauge.this));
                break;
            case SIMPLE:
                setKnobPosition(Pos.CENTER);
                setBorderPaint(Color.WHITE);
                setNeedleBorderColor(Color.WHITE);
                setBackgroundPaint(Color.DARKGRAY);
                setDecimals(0);
                setTickLabelColor(Color.WHITE);
                setNeedleColor(Color.web("#5a615f"));
                setValueColor(Color.WHITE);
                setTitleColor(Color.WHITE);
                setSubTitleColor(Color.WHITE);
                setSectionsVisible(true);
                super.setSkin(new SimpleSkin(Gauge.this));
                break;
            case SLIM:
                setKnobPosition(Pos.CENTER);
                setDecimals(2);
                setBarBackgroundColor(Color.rgb(62, 67, 73));
                setBarColor(Color.rgb(93, 190, 205));
                setTitleColor(Color.rgb(142, 147, 151));
                setValueColor(Color.rgb(228, 231, 238));
                setUnitColor(Color.rgb(142, 147, 151));
                super.setSkin(new SlimSkin(Gauge.this));
                break;
            case SPACE_X:
                setKnobPosition(Pos.CENTER);
                setDecimals(0);
                setThresholdColor(Color.rgb(180, 0, 0));
                setBarBackgroundColor(Color.rgb(169, 169, 169, 0.25));
                setBarColor(Color.rgb(169, 169, 169));
                setTitleColor(Color.WHITE);
                setValueColor(Color.WHITE);
                setUnitColor(Color.WHITE);
                super.setSkin(new SpaceXSkin(Gauge.this));
                break;
            case QUARTER:
                setKnobPosition(Pos.BOTTOM_RIGHT);
                setAngleRange(90);
                super.setSkin(new QuarterSkin(Gauge.this));
                break;
            case HORIZONTAL:
                setKnobPosition(Pos.BOTTOM_CENTER);
                setAngleRange(180);
                super.setSkin(new HSkin(Gauge.this));
                break;
            case VERTICAL:
                setKnobPosition(Pos.CENTER_RIGHT);
                setAngleRange(180);
                super.setSkin(new VSkin(Gauge.this));
                break;
            case LCD:
                setDecimals(1);
                setTickLabelDecimals(1);
                setMinMeasuredValueVisible(true);
                setMaxMeasuredValueVisible(true);
                setOldValueVisible(true);
                setBorderPaint(Color.WHITE);
                setForegroundPaint(Color.WHITE);
                super.setSkin(new LcdSkin(Gauge.this));
                break;
            case TINY:
                setBorderWidth(24);
                setBackgroundPaint(Color.rgb(216, 216, 216));
                setBorderPaint(Color.rgb(76, 76, 76));
                setBarBackgroundColor(Color.rgb(76, 76, 76, 0.2));
                setNeedleColor(Color.rgb(76, 76, 76));
                setSectionsVisible(true);
                setMajorTickMarksVisible(true);
                setMajorTickMarkColor(Color.WHITE);
                super.setSkin(new TinySkin(Gauge.this));
                break;
            case BATTERY:
                setBarBackgroundColor(Color.BLACK);
                setBarColor(Color.BLACK);
                setValueColor(Color.WHITE);
                super.setSkin(new BatterySkin(Gauge.this));
                break;
            case LEVEL:
                setValueColor(Color.WHITE);
                setBarColor(Color.CYAN);
                super.setSkin(new LevelSkin(Gauge.this));
                break;
            case LINEAR:
                setOrientation(Orientation.VERTICAL);
                setBarColor(DARK_COLOR);
                setBarEffectEnabled(true);
                super.setSkin(new LinearSkin(Gauge.this));
                break;
            case DIGITAL:
                setBarColor(DARK_COLOR);
                super.setSkin(new DigitalSkin(Gauge.this));
                break;
            case SIMPLE_DIGITAL:
                setBarColor(DARK_COLOR);
                super.setSkin(new SimpleDigitalSkin(Gauge.this));
                break;
            case SECTION:
                setBackgroundPaint(Gauge.DARK_COLOR);
                setAutoScale(false);
                setValueVisible(false);
                setKnobColor(Color.rgb(82, 82, 84));
                setSectionsVisible(true);
                setSectionTextVisible(true);
                super.setSkin(new SectionSkin(Gauge.this));
                break;
            case BAR:
                Color barColor = getBarColor();
                setAnimated(true);
                setAnimationDuration(1000);
                setMinValue(0);
                setMaxValue(100);
                setGradientBarEnabled(true);
                setGradientBarStops(new Stop(0.0, barColor),
                                    new Stop(0.01, barColor),
                                    new Stop(0.75, barColor.deriveColor(-10, 1, 1, 1)),
                                    new Stop(1.0, barColor.deriveColor(-20, 1, 1, 1)));
                super.setSkin(new BarSkin(Gauge.this));
                break;
            case WHITE:
                setAnimated(true);
                setAnimationDuration(1000);
                setAngleRange(360);
                setMinValue(0);
                setMaxValue(100);
                setBarColor(Color.WHITE);
                setValueColor(Color.WHITE);
                setUnitColor(Color.WHITE);
                super.setSkin(new WhiteSkin(Gauge.this));
                break;
            case CHARGE:
                setAnimated(true);
                setMinValue(0.0);
                setMaxValue(1.0);
                super.setSkin(new ChargeSkin(Gauge.this));
                break;
            case SIMPLE_SECTION:
                setAnimated(true);
                setStartAngle(150);
                setAngleRange(300);
                setSectionsVisible(true);
                setBarBackgroundColor(Color.rgb(150, 150, 150, 0.25));
                setBarColor(Color.rgb(69, 106, 207));
                setTitleColor(Color.rgb(90, 90, 90));
                setUnitColor(Color.rgb(90, 90, 90));
                setValueColor(Color.rgb(90, 90, 90));
                super.setSkin(new SimpleSectionSkin(Gauge.this));
                break;
            case TILE_KPI:
                setKnobPosition(Pos.BOTTOM_CENTER);
                setDecimals(0);
                setValueColor(Color.rgb(238, 238, 238));
                setBackgroundPaint(Color.rgb(42,42,42));
                setForegroundBaseColor(Color.rgb(238,238,238));
                setBarColor(Color.rgb(238,238,238));
                setThresholdVisible(false);
                setThresholdColor(Color.rgb(41,177,255));
                setNeedleColor(Color.rgb(238,238,238));
                setAngleRange(180);
                super.setSkin(new TileKpiSkin(Gauge.this));
                break;
            case TILE_TEXT_KPI:
                setDecimals(0);
                setBackgroundPaint(Color.rgb(42,42,42));
                setForegroundBaseColor(Color.rgb(238,238,238));
                setBarColor(Color.rgb(41,177,255));
                setValueColor(Color.rgb(238, 238, 238));
                setUnitColor(Color.rgb(238, 238, 238));
                setThresholdVisible(false);
                setThresholdColor(Color.rgb(139,144,146));
                super.setSkin(new TileTextKpiSkin(Gauge.this));
                break;
            case TILE_SPARK_LINE:
                setDecimals(0);
                setBackgroundPaint(Color.rgb(42,42,42));
                setForegroundBaseColor(Color.rgb(238,238,238));
                setBarColor(Color.rgb(41,177,255));
                setValueColor(Color.rgb(238, 238, 238));
                setUnitColor(Color.rgb(238, 238, 238));
                setAveragingEnabled(true);
                setAveragingPeriod(10);
                setAverageColor(Color.rgb(238, 238, 238, 0.5));
                setAnimated(false);
                super.setSkin(new TileSparklineSkin(Gauge.this));
                break;
            case GAUGE:
                setStartAngle(320);
                setAngleRange(280);
            default:
                super.setSkin(new GaugeSkin(Gauge.this));
                break;
        }
        fireUpdateEvent(RESIZE_EVENT);
    }


    // ******************** Event handling ************************************
    public void setOnUpdate(final UpdateEventListener LISTENER) { addUpdateEventListener(LISTENER); }
    public void addUpdateEventListener(final UpdateEventListener LISTENER) { if (!listenerList.contains(LISTENER)) listenerList.add(LISTENER); }
    public void removeUpdateEventListener(final UpdateEventListener LISTENER) { if (listenerList.contains(LISTENER)) listenerList.remove(LISTENER); }

    public void fireUpdateEvent(final UpdateEvent EVENT) {
        int listSize = listenerList.size();
        for (int i = 0; i < listSize; i++) { listenerList.get(i).onUpdateEvent(EVENT); }
    }


    public void setOnButtonPressed(final EventHandler<ButtonEvent> HANDLER) { addEventHandler(ButtonEvent.BTN_PRESSED, HANDLER); }
    public void removeOnButtonPressed(final EventHandler<ButtonEvent> HANDLER) { removeEventHandler(ButtonEvent.BTN_PRESSED, HANDLER); }

    public void setOnButtonReleased(final EventHandler<ButtonEvent> HANDLER) { addEventHandler(ButtonEvent.BTN_RELEASED, HANDLER); }
    public void removeOnButtonReleased(final EventHandler<ButtonEvent> HANDLER) { removeEventHandler(ButtonEvent.BTN_RELEASED, HANDLER); }


    public void setOnThresholdExceeded(final EventHandler<ThresholdEvent> HANDLER) { addEventHandler(ThresholdEvent.THRESHOLD_EXCEEDED, HANDLER); }
    public void removeOnThresholdExceeded(final EventHandler<ThresholdEvent> HANDLER) { removeEventHandler(ThresholdEvent.THRESHOLD_EXCEEDED, HANDLER); }

    public void setOnThresholdUnderrun(final EventHandler<ThresholdEvent> HANDLER) { addEventHandler(ThresholdEvent.THRESHOLD_UNDERRUN, HANDLER); }
    public void removeOnThresholdUnderrun(final EventHandler<ThresholdEvent> HANDLER) { removeEventHandler(ThresholdEvent.THRESHOLD_UNDERRUN, HANDLER); }


    // ******************** Inner Classes *************************************
    public static class ButtonEvent extends Event {
        public static final EventType<ButtonEvent> BTN_PRESSED  = new EventType<>(ANY, "BTN_PRESSED");
        public static final EventType<ButtonEvent> BTN_RELEASED = new EventType<>(ANY, "BTN_RELEASED");


        // ******************** Constructors **************************************
        public ButtonEvent(final EventType<ButtonEvent> TYPE) { super(TYPE); }
        public ButtonEvent(final Object SOURCE, final EventTarget TARGET, EventType<ButtonEvent> TYPE) { super(SOURCE, TARGET, TYPE); }
    }

    public static class ThresholdEvent extends Event {
        public static final EventType<ThresholdEvent> THRESHOLD_EXCEEDED = new EventType<>(ANY, "THRESHOLD_EXCEEDED");
        public static final EventType<ThresholdEvent> THRESHOLD_UNDERRUN = new EventType<>(ANY, "THRESHOLD_UNDERRUN");


        // ******************** Constructors **************************************
        public ThresholdEvent(final EventType<ThresholdEvent> TYPE) { super(TYPE); }
        public ThresholdEvent(final Object SOURCE, final EventTarget TARGET, EventType<ThresholdEvent> TYPE) { super(SOURCE, TARGET, TYPE); }
    }
}
