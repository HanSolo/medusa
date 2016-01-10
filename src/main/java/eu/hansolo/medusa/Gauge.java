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
import eu.hansolo.medusa.skins.GaugeSkin;
import eu.hansolo.medusa.tools.GradientLookup;
import eu.hansolo.medusa.tools.Helper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by hansolo on 11.12.15.
 */
public class Gauge extends Control {
    public enum NeedleType { STANDARD }
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
    public enum KnobType { STANDARD, PLAIN, METAL, FLAT }
    public enum LedType { STANDARD, FLAT }
    public enum TickLabelOrientation { ORTHOGONAL,  HORIZONTAL, TANGENT }
    public enum TickMarkType { LINE, DOT, TRIANGLE, DIAMOND, TICK_LABEL }
    public enum NumberFormat {
        AUTO("0"),
        STANDARD("0"),
        FRACTIONAL("0.0#"),
        SCIENTIFIC("0.##E0"),
        PERCENTAGE("##0.0%");

        private final DecimalFormat DF;

        NumberFormat(final String FORMAT_STRING) {
            Locale.setDefault(new Locale("en", "US"));

            DF = new DecimalFormat(FORMAT_STRING);
        }

        public String format(final Number NUMBER) { return DF.format(NUMBER); }
    }
    public enum TickLabelLocation { INSIDE, OUTSIDE }
    public enum LcdFont { STANDARD, LCD, DIGITAL, DIGITAL_BOLD, ELEKTRA }
    public enum ScaleDirection { CLOCKWISE, COUNTER_CLOCKWISE }

    public  static final Color                 DARK_COLOR           = Color.rgb(36, 36, 36);
    public  static final Color                 BRIGHT_COLOR         = Color.rgb(223, 223, 223);
    private static final long                  LED_BLINK_INTERVAL   = 500l;

    public         final ButtonEvent           BUTTON_PRESSED_EVENT = new ButtonEvent(Gauge.this, null, ButtonEvent.BUTTON_PRESSED);
    public         final ButtonEvent           BUTTON_RELEASED_EVENT= new ButtonEvent(Gauge.this, null, ButtonEvent.BUTTON_RELEASED);
    private        final ThresholdEvent        EXCEEDED_EVENT       = new ThresholdEvent(Gauge.this, null, ThresholdEvent.THRESHOLD_EXCEEDED);
    private        final ThresholdEvent        UNDERRUN_EVENT       = new ThresholdEvent(Gauge.this, null, ThresholdEvent.THRESHOLD_UNDERRUN);
    private        final UpdateEvent           RECALC_EVENT         = new UpdateEvent(Gauge.this, UpdateEvent.EventType.RECALC);
    private        final UpdateEvent           REDRAW_EVENT         = new UpdateEvent(Gauge.this, UpdateEvent.EventType.REDRAW);
    private        final UpdateEvent           RESIZE_EVENT         = new UpdateEvent(Gauge.this, UpdateEvent.EventType.RESIZE);
    private        final UpdateEvent           LED_BLINK_EVENT      = new UpdateEvent(Gauge.this, UpdateEvent.EventType.LED_BLINK);
    private        final UpdateEvent           VISIBILITY_EVENT     = new UpdateEvent(Gauge.this, UpdateEvent.EventType.VISIBILITY);
    private        final UpdateEvent           INTERACTIVITY_EVENT  = new UpdateEvent(Gauge.this, UpdateEvent.EventType.INTERACTIVITY);

    private static volatile Future             blinkFuture;
    private static ScheduledExecutorService    blinkService         = new ScheduledThreadPoolExecutor(1, Helper.getThreadFactory("BlinkTask", false));
    private static volatile Callable<Void>     blinkTask;

    // Update events
    private List<UpdateEventListener>          listenerList        = new CopyOnWriteArrayList();
    private List<EventHandler<ButtonEvent>>    pressedHandlerList  = new CopyOnWriteArrayList<>();
    private List<EventHandler<ButtonEvent>>    releasedHandlerList = new CopyOnWriteArrayList<>();
    private List<EventHandler<ThresholdEvent>> exceededHandlerList = new CopyOnWriteArrayList<>();
    private List<EventHandler<ThresholdEvent>> underrunHandlerList = new CopyOnWriteArrayList<>();

    // Data related
    private DoubleProperty                       value;
    private DoubleProperty                       currentValue;
    private DoubleProperty                       oldValue;
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
    private ObservableList<Section>              sections;
    private ObservableList<Section>              areas;
    private ObservableList<Section>              tickMarkSections;
    private ObservableList<Section>              tickLabelSections;
    private ObservableList<Marker>               markers;
    // UI related
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
    private boolean                              _valueVisible;
    private BooleanProperty                      valueVisible;
    private Paint                                _backgroundPaint;
    private ObjectProperty<Paint>                backgroundPaint;
    private Paint                                _borderPaint;
    private ObjectProperty<Paint>                borderPaint;
    private Paint                                _foregroundPaint;
    private ObjectProperty<Paint>                foregroundPaint;
    private Color                                _knobColor;
    private ObjectProperty<Color>                knobColor;
    private KnobType                             _knobType;
    private ObjectProperty<KnobType>             knobType;
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
    private Color                                _mediumTickMarkColor;
    private ObjectProperty<Color>                mediumTickMarkColor;
    private Color                                _minorTickMarkColor;
    private ObjectProperty<Color>                minorTickMarkColor;
    private TickMarkType                         _majorTickMarkType;
    private ObjectProperty<TickMarkType>         majorTickMarkType;
    private TickMarkType                         _mediumTickMarkType;
    private ObjectProperty<TickMarkType>         mediumTickMarkType;
    private TickMarkType                         _minorTickMarkType;
    private ObjectProperty<TickMarkType>         minorTickMarkType;
    private NumberFormat                         _numberFormat;
    private ObjectProperty<NumberFormat>         numberFormat;
    private int                                  _decimals;
    private IntegerProperty                      decimals;
    private NeedleType                           _needleType;
    private ObjectProperty<NeedleType>           needleType;
    private NeedleShape                          _needleShape;
    private ObjectProperty<NeedleShape>          needleShape;
    private NeedleSize                           _needleSize;
    private ObjectProperty<NeedleSize>           needleSize;
    private Color                                _needleColor;
    private ObjectProperty<Color>                needleColor;
    private Color                                _barColor;
    private ObjectProperty<Color>                barColor;
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
    private boolean                              _sectionsVisible;
    private BooleanProperty                      sectionsVisible;
    private boolean                              _sectionTextVisible;
    private BooleanProperty                      sectionTextVisible;
    private boolean                              _sectionIconsVisible;
    private BooleanProperty                      sectionIconsVisible;
    private boolean                              _areasVisible;
    private BooleanProperty                      areasVisible;
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
    private boolean                              _tickLabelsVisible;
    private BooleanProperty                      tickLabelsVisible;
    private double                               _majorTickSpace;
    private DoubleProperty                       majorTickSpace;
    private double                               _minorTickSpace;
    private DoubleProperty                       minorTickSpace;
    private boolean                              _lcdVisible;
    private BooleanProperty                      lcdVisible;
    private boolean                              _ledVisible;
    private BooleanProperty                      ledVisible;
    private boolean                              _ledOn;
    private BooleanProperty                      ledOn;
    private boolean                              _ledBlinking;
    private BooleanProperty                      ledBlinking;
    private Orientation                          _orientation;
    private ObjectProperty<Orientation>          orientation;
    private boolean                              _colorGradientEnabled;
    private BooleanProperty                      colorGradientEnabled;
    private GradientLookup                       gradientLookup;
    private boolean                              _customTickLabelsEnabled;
    private BooleanProperty                      customTickLabelsEnabled;
    private ObservableList<String>               customTickLabels;
    private boolean                              _interactive;
    private BooleanProperty                      interactive;
    private String                               _buttonTooltipText;
    private StringProperty                       buttonTooltipText;

    // others
    private double                               originalMinValue;
    private double                               originalMaxValue;
    private Timeline                             timeline;
    private Instant                              lastCall;
    private boolean                              withinSpeedLimit;


    // ******************** Constructors **************************************
    public Gauge() {
        getStyleClass().add("gauge");

        init();

        createBlinkTask();
    }


    // ******************** Initialization ************************************
    private void init() {
        lastCall                       = Instant.now();
        _minValue                      = 0;
        _maxValue                      = 100;
        value                          = new DoublePropertyBase(0) {
            @Override public void set(final double VALUE) {
                oldValue.set(value.get());
                double nValue = Helper.clamp(getMinValue(), getMaxValue(), VALUE).doubleValue();
                super.set(nValue);
                withinSpeedLimit = !(Instant.now().minusMillis(getAnimationDuration()).isBefore(lastCall));
                lastCall         = Instant.now();
                if (withinSpeedLimit && isAnimated()) {
                    long animationDuration = isReturnToZero() ? (long) (0.5 * getAnimationDuration()) : getAnimationDuration();
                    timeline.stop();

                    final KeyValue KEY_VALUE = new KeyValue(currentValue, nValue, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                    final KeyFrame KEY_FRAME = new KeyFrame(Duration.millis(animationDuration), KEY_VALUE);
                    timeline.getKeyFrames().setAll(KEY_FRAME);
                    timeline.play();
                } else {
                    currentValue.set(nValue);
                }
            }
            @Override public Object getBean() { return Gauge.this; }
            @Override public String getName() { return "value"; }
        };
        currentValue                   = new DoublePropertyBase(value.get()) {
            @Override public void set(final double VALUE) {
                double formerValue = get();
                super.set(VALUE);
                if (isCheckThreshold()) {
                    double thrshld = getThreshold();
                    if (formerValue < thrshld && VALUE > thrshld) {
                        fireThresholdEvent(EXCEEDED_EVENT);
                    } else if (formerValue > thrshld && VALUE < thrshld ) {
                        fireThresholdEvent(UNDERRUN_EVENT);
                    }
                }
                if (VALUE < getMinMeasuredValue()) { setMinMeasuredValue(VALUE); }
                if (VALUE > getMaxMeasuredValue()) { setMaxMeasuredValue(VALUE); }
            }
            @Override public Object getBean() { return Gauge.this; }
            @Override public String getName() { return "currentValue";}
        };
        oldValue                       = new SimpleDoubleProperty(Gauge.this, "oldValue");
        _range                         = _maxValue - _minValue;
        _threshold                     = _maxValue;
        _title                         = "";
        _subTitle                      = "";
        _unit                          = "";
        sections                       = FXCollections.observableArrayList();
        areas                          = FXCollections.observableArrayList();
        tickMarkSections               = FXCollections.observableArrayList();
        tickLabelSections              = FXCollections.observableArrayList();
        markers                        = FXCollections.observableArrayList();

        _startFromZero                 = false;
        _returnToZero                  = false;
        _zeroColor                     = DARK_COLOR;
        _minMeasuredValue              = _maxValue;
        _maxMeasuredValue              = _minValue;
        _minMeasuredValueVisible       = false;
        _maxMeasuredValueVisible       = false;
        _valueVisible                  = true;
        _backgroundPaint               = Color.TRANSPARENT;
        _borderPaint                   = Color.TRANSPARENT;
        _foregroundPaint               = Color.TRANSPARENT;
        _knobColor                     = Color.rgb(204, 204, 204);
        _knobType                      = KnobType.STANDARD;
        _animated                      = false;
        animationDuration              = 800;
        _startAngle                    = 320;
        _angleRange                    = 280;
        _angleStep                     = _angleRange / _range;
        _autoScale                     = true;
        _shadowsEnabled                = false;
        _scaleDirection                = ScaleDirection.CLOCKWISE;
        _tickLabelLocation             = TickLabelLocation.INSIDE;
        _tickLabelOrientation          = TickLabelOrientation.HORIZONTAL;
        _tickLabelColor                = DARK_COLOR;
        _tickMarkColor                 = DARK_COLOR;
        _majorTickMarkColor            = DARK_COLOR;
        _mediumTickMarkColor           = DARK_COLOR;
        _minorTickMarkColor            = DARK_COLOR;
        _majorTickMarkType             = TickMarkType.LINE;
        _mediumTickMarkType            = TickMarkType.LINE;
        _minorTickMarkType             = TickMarkType.LINE;
        _numberFormat                  = NumberFormat.STANDARD;
        _decimals                      = 1;
        _needleType                    = NeedleType.STANDARD;
        _needleShape                   = NeedleShape.ANGLED;
        _needleSize                    = NeedleSize.STANDARD;
        _needleColor                   = Color.rgb(200, 0, 0);
        _barColor                      = DARK_COLOR;
        _lcdDesign                     = LcdDesign.STANDARD;
        _lcdFont                       = LcdFont.DIGITAL_BOLD;
        _ledColor                      = Color.RED;
        _ledType                       = LedType.STANDARD;
        _titleColor                    = DARK_COLOR;
        _subTitleColor                 = DARK_COLOR;
        _unitColor                     = DARK_COLOR;
        _valueColor                    = DARK_COLOR;
        _thresholdColor                = Color.CRIMSON;
        _checkSectionsForValue         = false;
        _checkAreasForValue            = false;
        _checkThreshold                = false;
        _innerShadowEnabled            = false;
        _thresholdVisible              = false;
        _sectionsVisible               = false;
        _sectionTextVisible            = false;
        _sectionIconsVisible           = false;
        _areasVisible                  = false;
        _tickMarkSectionsVisible       = false;
        _tickLabelSectionsVisible      = false;
        _markersVisible                = false;
        _tickLabelsVisible             = true;
        _majorTickMarksVisible         = true;
        _mediumTickMarksVisible        = true;
        _minorTickMarksVisible         = true;
        _majorTickSpace                = 10;
        _minorTickSpace                = 1;
        _lcdVisible                    = false;
        _ledVisible                    = false;
        _ledOn                         = false;
        _ledBlinking                   = false;
        _orientation                   = Orientation.HORIZONTAL;
        _colorGradientEnabled          = false;
        _customTickLabelsEnabled       = false;
        customTickLabels               = FXCollections.observableArrayList();
        _interactive                   = false;
        _buttonTooltipText             = "";

        originalMinValue               = -Double.MAX_VALUE;
        originalMaxValue               = Double.MAX_VALUE;
        timeline                       = new Timeline();
        timeline.setOnFinished(e -> {
            if (isReturnToZero() && Double.compare(currentValue.get(), 0d) != 0d) {
                final KeyValue KEY_VALUE2 = new KeyValue(value, 0, Interpolator.SPLINE(0.5, 0.4, 0.4, 1.0));
                final KeyFrame KEY_FRAME2 = new KeyFrame(Duration.millis(animationDuration), KEY_VALUE2);
                timeline.getKeyFrames().setAll(KEY_FRAME2);
                timeline.play();
            }
        });
    }


    // ******************** Data related methods ******************************
    public double getValue() { return value.get(); }
    public void setValue(final double VALUE) { value.set(VALUE); }
    public DoubleProperty valueProperty() { return value; }

    public double getCurrentValue() { return currentValue.get(); }
    public ReadOnlyDoubleProperty currentValueProperty() { return currentValue; }

    public double getOldValue() { return oldValue.get(); }
    public ReadOnlyDoubleProperty oldValueProperty() { return oldValue; }

    public double getMinValue() { return null == minValue ? _minValue : minValue.get(); }
    public void setMinValue(final double VALUE) {
        if (null == minValue) {
            _minValue = Helper.clamp(-Double.MAX_VALUE, getMaxValue(), VALUE).doubleValue();
            setRange(getMaxValue() - _minValue);
            if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = _minValue;
            setValue(isStartFromZero() ? 0 : _minValue);
        } else {
            minValue.set(Helper.clamp(-Double.MAX_VALUE, getMaxValue(), VALUE).doubleValue());
            setRange(getMaxValue() - getMinValue());
            if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = minValue.get();
            setValue(isStartFromZero() ? 0 : minValue.get());
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public DoubleProperty minValueProperty() {
        if (null == minValue) {
            minValue = new DoublePropertyBase(_minValue) {
                @Override public void set(final double VALUE) {
                    super.set(Helper.clamp(-Double.MAX_VALUE, getMaxValue(), VALUE).doubleValue());
                    setRange(getMaxValue() - get());
                    if (Double.compare(originalMinValue, -Double.MAX_VALUE) == 0) originalMinValue = get();
                    setValue(get());
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "minValue";}
            };
        }
        return minValue;
    }

    public double getMaxValue() { return null == maxValue ? _maxValue : maxValue.get(); }
    public void setMaxValue(final double VALUE) {
        if (null == maxValue) {
            _maxValue = Helper.clamp(getMinValue(), Double.MAX_VALUE, VALUE).doubleValue();
            setRange(_maxValue - getMinValue());
            if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = _maxValue;
        } else {
            maxValue.set(Helper.clamp(getMinValue(), Double.MAX_VALUE, VALUE).doubleValue());
            setRange(maxValue.get() - getMinValue());
            if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = maxValue.get();
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public DoubleProperty maxValueProperty() {
        if (null == maxValue) {
            maxValue = new DoublePropertyBase(_maxValue) {
                @Override public void set(final double VALUE) {
                    super.set(Helper.clamp(getMinValue(), Double.MAX_VALUE, VALUE).doubleValue());
                    setRange(get() - getMinValue());
                    if (Double.compare(originalMaxValue, Double.MAX_VALUE) == 0) originalMaxValue = get();
                }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "maxValue"; }
            };
        }
        return maxValue;
    }

    public double getRange() { return null == range ? _range : range.get(); }
    private void setRange(final double RANGE) {
        if (null == range) {
            _range = RANGE;
        } else {
            range.set(RANGE);
        }
        setAngleStep(getAngleRange() / RANGE);
    }
    public ReadOnlyDoubleProperty rangeProperty() {
        if (null == range) {
            range = new SimpleDoubleProperty(Gauge.this, "range", getMaxValue() - getMinValue());
        }
        return range;
    }

    public double getThreshold() { return null == threshold ? _threshold : threshold.get(); }
    public void setThreshold(final double THRESHOLD) {
        if (null == threshold) {
            _threshold = Helper.clamp(getMinValue(), getMaxValue(), THRESHOLD).doubleValue();
        } else {
            threshold.set(THRESHOLD);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public DoubleProperty tresholdProperty() {
        if (null == threshold) {
            threshold = new DoublePropertyBase(_threshold) {
                @Override public void set(final double VALUE) { super.set(Helper.clamp(getMinValue(), getMaxValue(), VALUE).doubleValue()); }
                @Override public Object getBean() { return Gauge.this; }
                @Override public String getName() { return "threshold"; }
            };
        }
        return threshold;
    }

    public String getTitle() { return null == title ? _title : title.get(); }
    public void setTitle(final String TITLE) {
        if (null == title) {
            _title = TITLE;
        } else {
            title.set(TITLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public StringProperty titleProperty() {
        if (null == title) { title = new SimpleStringProperty(Gauge.this, "title", _title); }
        return title;
    }

    public String getSubTitle() { return null == subTitle ? _subTitle : subTitle.get(); }
    public void setSubTitle(final String SUBTITLE) {
        if (null == subTitle) {
            _subTitle = SUBTITLE;
        } else {
            subTitle.set(SUBTITLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public StringProperty subTitleProperty() {
        if (null == subTitle) { subTitle = new SimpleStringProperty(Gauge.this, "subTitle", _subTitle); }
        return subTitle;
    }

    public String getUnit() { return null == unit ? _unit : unit.get(); }
    public void setUnit(final String UNIT) {
        if (null == unit) {
            _unit = UNIT;
        } else {
            unit.set(UNIT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public StringProperty unitProperty() {
        if (null == unit) { unit = new SimpleStringProperty(Gauge.this, "unit", _unit); }
        return unit;
    }

    public ObservableList<Section> getSections() { return sections; }
    public void setSections(final List<Section> SECTIONS) { sections.setAll(SECTIONS); }
    public void setSections(final Section... SECTIONS) { setSections(Arrays.asList(SECTIONS)); }
    public void addSection(final Section SECTION) { sections.add(SECTION); }
    public void removeSection(final Section SECTION) { sections.remove(SECTION); }
    public void clearSections() { sections.clear(); }

    public ObservableList<Section> getAreas() { return areas; }
    public void setAreas(final List<Section> AREAS) { areas.setAll(AREAS); }
    public void setAreas(final Section... AREAS) { setAreas(Arrays.asList(AREAS)); }
    public void addArea(final Section AREA) { areas.add(AREA); }
    public void removeArea(final Section AREA) { areas.remove(AREA); }
    public void clearAreas() { areas.clear(); }

    public ObservableList<Section> getTickMarkSections() { return tickMarkSections; }
    public void setTickMarkSections(final List<Section> SECTIONS) { tickMarkSections.setAll(SECTIONS); }
    public void setTickMarkSections(final Section... SECTIONS) { setTickMarkSections(Arrays.asList(SECTIONS)); }
    public void addTickMarkSection(final Section SECTION) { tickMarkSections.add(SECTION); }
    public void removeTickMarkSection(final Section SECTION) { tickMarkSections.remove(SECTION); }
    public void clearTickMarkSections() { tickMarkSections.clear(); }

    public ObservableList<Section> getTickLabelSections() { return tickLabelSections; }
    public void setTickLabelSections(final List<Section> SECTIONS) { tickLabelSections.setAll(SECTIONS); }
    public void setTickLabelSections(final Section... SECTIONS) { setTickLabelSections(Arrays.asList(SECTIONS)); }
    public void addTickLabelSection(final Section SECTION) { tickLabelSections.add(SECTION); }
    public void removeTickLabelSection(final Section SECTION) { tickLabelSections.remove(SECTION); }
    public void clearTickLabelSections() { tickLabelSections.clear(); }

    public ObservableList<Marker> getMarkers() { return markers; }
    public void setMarkers(final List<Marker> MARKERS) { markers.setAll(MARKERS); }
    public void setMarkers(final Marker... MARKERS) { setMarkers(Arrays.asList(MARKERS)); }
    public void addMarker(final Marker MARKER) { markers.add(MARKER); }
    public void removeMarker(final Marker MARKER) { markers.remove(MARKER); }
    public void clearMarkers() { markers.clear(); }


    // ******************** UI related methods ********************************
    public void setForegroundBaseColor(final Color COLOR) {
        if (null == titleColor)          { _titleColor     = COLOR;      } else { titleColor.set(COLOR); }
        if (null == unitColor)           { _unitColor      = COLOR;      } else { unitColor.set(COLOR); }
        if (null == valueColor)          { _valueColor     = COLOR;      } else { valueColor.set(COLOR); }
        if (null == tickLabelColor)      { _tickLabelColor = COLOR;      } else { tickLabelColor.set(COLOR); }
        if (null == tickMarkColor)       { _tickMarkColor  = COLOR;      } else { tickMarkColor.set(COLOR); }
        if (null == majorTickMarkColor)  { _majorTickMarkColor = COLOR;  } else { majorTickMarkColor.set(COLOR); }
        if (null == mediumTickMarkColor) { _mediumTickMarkColor = COLOR; } else { mediumTickMarkColor.set(COLOR); }
        if (null == minorTickMarkColor)  { _minorTickMarkColor = COLOR;  } else { minorTickMarkColor.set(COLOR); }
        fireUpdateEvent(REDRAW_EVENT);
    }

    public boolean isStartFromZero() { return null == startFromZero ? _startFromZero : startFromZero.get(); }
    public void setStartFromZero(final boolean START) {
        if (null == startFromZero) {
            _startFromZero = START;
        } else {
            startFromZero.set(START);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty startFromZeroProperty() {
        if (null == startFromZero) { startFromZero = new SimpleBooleanProperty(Gauge.this, "startFromZero", _startFromZero); }
        return startFromZero;
    }

    public boolean isReturnToZero() { return null == returnToZero ? _returnToZero : returnToZero.get(); }
    public void setReturnToZero(final boolean RETURN) {
        if (null == returnToZero) {
            _returnToZero = RETURN;
        } else {
            returnToZero.set(RETURN);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty returnToZeroProperty() {
        if (null == returnToZero) { returnToZero = new SimpleBooleanProperty(Gauge.this, "returnToZero", _returnToZero); }
        return returnToZero;
    }

    public Color getZeroColor() { return null == zeroColor ? _zeroColor : zeroColor.get(); }
    public void setZeroColor(final Color COLOR) {
        if (null == zeroColor) {
            _zeroColor = COLOR;
        } else {
            zeroColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> zeroColorProperty() {
        if (null == zeroColor) { zeroColor = new SimpleObjectProperty<>(Gauge.this, "zeroColor", _zeroColor); }
        return zeroColor;
    }

    public double getMinMeasuredValue() { return null == minMeasuredValue ? _minMeasuredValue : minMeasuredValue.get(); }
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

    public double getMaxMeasuredValue() {
        return null == maxMeasuredValue ? _maxMeasuredValue : maxMeasuredValue.get();
    }
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

    public boolean isMinMeasuredValueVisible() { return null == minMeasuredValueVisible ? _minMeasuredValueVisible : minMeasuredValueVisible.get(); }
    public void setMinMeasuredValueVisible(final boolean VISIBLE) {
        if (null == minMeasuredValueVisible) {
            _minMeasuredValueVisible = VISIBLE;
        } else {
            minMeasuredValueVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty minMeasuredValueVisibleProperty() {
        if (null == minMeasuredValueVisible) { minMeasuredValueVisible = new SimpleBooleanProperty(Gauge.this, "minMeasuredValueVisible", _minMeasuredValueVisible); }
        return minMeasuredValueVisible;
    }

    public boolean isMaxMeasuredValueVisible() { return null == maxMeasuredValueVisible ? _maxMeasuredValueVisible : maxMeasuredValueVisible.get(); }
    public void setMaxMeasuredValueVisible(final boolean VISIBLE) {
        if (null == maxMeasuredValueVisible) {
            _maxMeasuredValueVisible = VISIBLE;
        } else {
            maxMeasuredValueVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty maxMeasuredValueVisibleProperty() {
        if (null == maxMeasuredValueVisible) { maxMeasuredValueVisible = new SimpleBooleanProperty(Gauge.this, "maxMeasuredValueVisible", _maxMeasuredValueVisible); }
        return maxMeasuredValueVisible;
    }

    public void resetMeasuredValues() {
        setMinMeasuredValue(getValue());
        setMaxMeasuredValue(getValue());
    }

    public boolean isValueVisible() { return null == valueVisible ? _valueVisible : valueVisible.get(); }
    public void setValueVisible(final boolean VISIBLE) {
        if (null == valueVisible) {
            _valueVisible = VISIBLE;
        } else {
            valueVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty valueVisibleProperty() {
        if (null == valueVisible) { valueVisible = new SimpleBooleanProperty(Gauge.this, "valueVisible", _valueVisible); }
        return valueVisible;
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
        if (null == backgroundPaint) { backgroundPaint = new SimpleObjectProperty<>(Gauge.this, "backgroundPaint", _backgroundPaint); }
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
        if (null == borderPaint) { borderPaint = new SimpleObjectProperty<>(Gauge.this, "borderPaint", _borderPaint); }
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
        if (null == foregroundPaint) { foregroundPaint = new SimpleObjectProperty<>(Gauge.this, "foregroundPaint", _foregroundPaint); }
        return foregroundPaint;
    }

    public Color getKnobColor() { return null == knobColor ? _knobColor : knobColor.get(); }
    public void setKnobColor(final Color COLOR) {
        if (null == knobColor) {
            _knobColor = COLOR;
        } else {
            knobColor.set(COLOR);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<Color> knobColorProperty() {
        if (null == knobColor) { knobColor = new SimpleObjectProperty<>(Gauge.this, "knobColor", _knobColor); }
        return knobColor;
    }

    public KnobType getKnobType() { return null == knobType ? _knobType : knobType.get(); }
    public void setKnobType(final KnobType TYPE) {
        if (null == knobType) {
            _knobType = TYPE;
        } else {
            knobType.set(TYPE);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<KnobType> knobTypeProperty() {
        if (null == knobType) { knobType = new SimpleObjectProperty<>(Gauge.this, "knobType", _knobType); }
        return knobType;
    }
    
    public boolean isAnimated() { return null == animated ? _animated : animated.get(); }
    public void setAnimated(final boolean ANIMATED) {
        if (null == animated) {
            _animated = ANIMATED;
        } else {
            animated.set(ANIMATED);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty animatedProperty() {
        if (null == animated) { animated = new SimpleBooleanProperty(Gauge.this, "animated", _animated); }
        return animated;
    }

    public long getAnimationDuration() { return animationDuration; }
    public void setAnimationDuration(final long ANIMATION_DURATION) { animationDuration = Helper.clamp(20l, 5000l, ANIMATION_DURATION); }

    public double getStartAngle() { return null == startAngle ? _startAngle : startAngle.get(); }
    public void setStartAngle(final double ANGLE) {
        if (null == startAngle) {
            _startAngle = Helper.clamp(0d, 360d, ANGLE);
        } else {
            startAngle.set(ANGLE);
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public DoubleProperty startAngleProperty() {
        if (null == startAngle) {
            startAngle = new DoublePropertyBase(_startAngle) {
                @Override public void set(final double START_ANGLE) { super.set(Helper.clamp(0d, 360d, START_ANGLE)); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "startAngle"; }
            };
        }
        return startAngle;
    }

    public double getAngleRange() { return null == angleRange ? _angleRange : angleRange.get(); }
    public void setAngleRange(final double RANGE) {
        double tmpAngleRange = Helper.clamp(0d, 360d, RANGE);
        if (null == angleRange) {
            _angleRange = tmpAngleRange;
        } else {
            angleRange.set(tmpAngleRange);
        }
        setAngleStep(tmpAngleRange / getRange());
        fireUpdateEvent(RECALC_EVENT);
    }
    public DoubleProperty angleRangeProperty() {
        if (null == angleRange) {
            angleRange = new DoublePropertyBase(_angleRange) {
                @Override public void set(final double RANGE) { super.set(Helper.clamp(0d, 360d, RANGE)); }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "angleRange"; }
            };
        }
        return angleRange;
    }

    public double getAngleStep() { return null == angleStep ? _angleStep : angleStep.get(); }
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

    public boolean isAutoScale() { return null == autoScale ? _autoScale : autoScale.get(); }
    public void setAutoScale(final boolean AUTO_SCALE) {
        if (null == autoScale) {
            _autoScale = AUTO_SCALE;
            if (_autoScale) {
                calcAutoScale();
            } else {
                setMinValue(originalMinValue);
                setMaxValue(originalMaxValue);
            }
        } else {
            autoScale.set(AUTO_SCALE);
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public BooleanProperty autoScaleProperty() {
        if (null == autoScale) {
            autoScale = new BooleanPropertyBase(_autoScale) {
                @Override public void set(final boolean AUTO_SCALE) {
                    if (AUTO_SCALE) {
                        calcAutoScale();
                    } else {
                        setMinValue(originalMinValue);
                        setMaxValue(originalMaxValue);
                    }
                    super.set(AUTO_SCALE);
                }
                @Override public Object getBean() { return this; }
                @Override public String getName() { return "autoScale"; }
            };
        }
        return autoScale;
    }

    public boolean areShadowsEnabled() { return null == shadowsEnabled ? _shadowsEnabled : shadowsEnabled.get(); }
    public void setShadowsEnabled(final boolean ENABLED) {
        if (null == shadowsEnabled) {
            _shadowsEnabled = ENABLED;
        } else {
            shadowsEnabled.set(ENABLED);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty shadowsEnabledProperty() {
        if (null == shadowsEnabled) { shadowsEnabled = new SimpleBooleanProperty(Gauge.this, "shadowsEnabled", _shadowsEnabled); }
        return shadowsEnabled;
    }

    public ScaleDirection getScaleDirection() { return null == scaleDirection ? _scaleDirection : scaleDirection.get(); }
    public void setScaleDirection(final ScaleDirection DIRECTION) {
        if (null == scaleDirection) {
            _scaleDirection = DIRECTION;
        } else {
            scaleDirection.set(DIRECTION);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<ScaleDirection> scaleDirectionProperty() {
        if (null == scaleDirection) { scaleDirection = new SimpleObjectProperty<>(Gauge.this, "scaleDirection", _scaleDirection); }
        return scaleDirection;
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
        if (null == tickLabelLocation) { tickLabelLocation = new SimpleObjectProperty<>(Gauge.this, "tickLabelLocation", _tickLabelLocation); }
        return tickLabelLocation;
    }

    public TickLabelOrientation getTickLabelOrientation() { return null == tickLabelOrientation ? _tickLabelOrientation : tickLabelOrientation.get(); }
    public void setTickLabelOrientation(final TickLabelOrientation ORIENTATION) {
        if (null == tickLabelOrientation) {
            _tickLabelOrientation = ORIENTATION;
        } else {
            tickLabelOrientation.set(ORIENTATION);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<TickLabelOrientation> tickLabelOrientationProperty() {
        if (null == tickLabelOrientation) { tickLabelOrientation = new SimpleObjectProperty<>(Gauge.this, "tickLabelOrientation", _tickLabelOrientation); }
        return tickLabelOrientation;
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
        if (null == tickLabelColor) { tickLabelColor = new SimpleObjectProperty<>(Gauge.this, "tickLabelColor", _tickLabelColor); }
        return tickLabelColor;
    }

    public Color getTickMarkColor() { return null == tickMarkColor ? _tickMarkColor : tickMarkColor.get(); }
    public void setTickMarkColor(final Color COLOR) {
        if (null == tickMarkColor) {
            _tickMarkColor = COLOR;
        } else {
            tickMarkColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> tickMarkColorProperty() {
        if (null == tickMarkColor) { tickMarkColor = new SimpleObjectProperty<>(Gauge.this, "tickMarkColor", _tickMarkColor); }
        return tickMarkColor;
    }

    public Color getMajorTickMarkColor() { return null == majorTickMarkColor ? _majorTickMarkColor : majorTickMarkColor.get(); }
    public void setMajorTickMarkColor(final Color COLOR) {
        if (null == majorTickMarkColor) {
            _majorTickMarkColor = COLOR;
        } else {
            majorTickMarkColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> majorTickMarkColorProperty() {
        if (null == majorTickMarkColor) { majorTickMarkColor = new SimpleObjectProperty<>(Gauge.this, "majorTickMarkColor", _majorTickMarkColor); }
        return majorTickMarkColor;
    }

    public Color getMediumTickMarkColor() { return null == mediumTickMarkColor ? _mediumTickMarkColor : mediumTickMarkColor.get(); }
    public void setMediumTickMarkColor(final Color COLOR) {
        if (null == mediumTickMarkColor) {
            _mediumTickMarkColor = COLOR;
        } else {
            mediumTickMarkColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> mediumTickMarkColorProperty() {
        if (null == mediumTickMarkColor) { mediumTickMarkColor = new SimpleObjectProperty<>(Gauge.this, "mediumTickMarkColor", _mediumTickMarkColor); }
        return mediumTickMarkColor;
    }
    
    public Color getMinorTickMarkColor() { return null == minorTickMarkColor ? _minorTickMarkColor : minorTickMarkColor.get(); }
    public void setMinorTickMarkColor(final Color COLOR) {
        if (null == minorTickMarkColor) {
            _minorTickMarkColor = COLOR;
        } else {
            minorTickMarkColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> minorTickMarkColorProperty() {
        if (null == minorTickMarkColor) { minorTickMarkColor = new SimpleObjectProperty<>(Gauge.this, "minorTickMarkColor", _minorTickMarkColor); }
        return minorTickMarkColor;
    }

    public TickMarkType getMajorTickMarkType() { return null == majorTickMarkType ? _majorTickMarkType : majorTickMarkType.get(); }
    public void setMajorTickMarkType(final TickMarkType TYPE) {
        if (null == majorTickMarkType) {
            _majorTickMarkType = TYPE;
        } else {
            majorTickMarkType.set(TYPE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<TickMarkType> majorTickMarkTypeProperty() {
        if (null == majorTickMarkType) { majorTickMarkType = new SimpleObjectProperty<>(Gauge.this, "majorTickMarkType", _majorTickMarkType); }
        return majorTickMarkType;
    }

    public TickMarkType getMediumTickMarkType() { return null == mediumTickMarkType ? _mediumTickMarkType : mediumTickMarkType.get(); }
    public void setMediumTickMarkType(final TickMarkType TYPE) {
        if (null == mediumTickMarkType) {
            _mediumTickMarkType = TYPE;
        } else {
            mediumTickMarkType.set(TYPE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<TickMarkType> mediumTickMarkTypeProperty() {
        if (null == mediumTickMarkType) { mediumTickMarkType = new SimpleObjectProperty<>(Gauge.this, "mediumTickMarkType", _mediumTickMarkType); }
        return mediumTickMarkType;
    }
    
    public TickMarkType getMinorTickMarkType() { return null == minorTickMarkType ? _minorTickMarkType : minorTickMarkType.get(); }
    public void setMinorTickMarkType(final TickMarkType TYPE) {
        if (null == minorTickMarkType) {
            _minorTickMarkType = TYPE;
        } else {
            minorTickMarkType.set(TYPE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<TickMarkType> minorTickMarkTypeProperty() {
        if (null == minorTickMarkType) { minorTickMarkType = new SimpleObjectProperty<>(Gauge.this, "minorTickMarkType", _minorTickMarkType); }
        return minorTickMarkType;
    }

    public NumberFormat getNumberFormat() { return null == numberFormat ? _numberFormat : numberFormat.get(); }
    public void setNumberFormat(final NumberFormat FORMAT) {
        if (null == numberFormat) {
            _numberFormat = FORMAT;
        } else {
            numberFormat.set(FORMAT);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<NumberFormat> numberFormatProperty() {
        if (null == numberFormat) { numberFormat = new SimpleObjectProperty<>(Gauge.this, "numberFormat", _numberFormat); }
        return numberFormat;
    }

    public int getDecimals() { return null == decimals ? _decimals : decimals.get(); }
    public void setDecimals(final int DECIMALS) {
        if (null == decimals) {
            _decimals = DECIMALS;
        } else {
            decimals.set(DECIMALS);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public IntegerProperty decimalsProperty() {
        if (null == decimals) { decimals = new SimpleIntegerProperty(Gauge.this, "decimals", _decimals); }
        return decimals;
    }

    public NeedleType getNeedleType() { return null == needleType ? _needleType : needleType.get(); }
    public void setNeedleType(final NeedleType TYPE) {
        if (null == needleType) {
            _needleType = TYPE;
        } else {
            needleType.set(TYPE);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<NeedleType> needleTypeProperty() {
        if (null == needleType) { needleType = new SimpleObjectProperty<>(Gauge.this, "needleType", _needleType); }
        return needleType;
    }

    public NeedleShape getNeedleShape() { return null == needleShape ? _needleShape : needleShape.get(); }
    public void setNeedleShape(final NeedleShape SHAPE) {
        if (null == needleShape) {
            _needleShape = SHAPE;
        } else {
            needleShape.set(SHAPE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<NeedleShape> needleShapeProperty() {
        if (null == needleShape) { needleShape = new SimpleObjectProperty<>(Gauge.this, "needleShape", _needleShape); }
        return needleShape;
    }
    
    public NeedleSize getNeedleSize() { return null == needleSize ? _needleSize : needleSize.get(); }
    public void setNeedleSize(final NeedleSize SIZE) {
        if (null == needleSize) {
            _needleSize = SIZE;
        } else {
            needleSize.set(SIZE);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<NeedleSize> needleSizeProperty() {
        if (null == needleSize) { needleSize = new SimpleObjectProperty<>(Gauge.this, "needleSize", _needleSize); }
        return needleSize;
    }

    public Color getNeedleColor() { return null == needleColor ? _needleColor : needleColor.get(); }
    public void setNeedleColor(final Color COLOR) {
        if (null == needleColor) {
            _needleColor = COLOR;
        } else {
            needleColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> needleColorProperty() {
        if (null == needleColor) { needleColor = new SimpleObjectProperty<>(Gauge.this, "needleColor", _needleColor); }
        return needleColor;
    }

    public Color getBarColor() { return null == barColor ? _barColor : barColor.get(); }
    public void setBarColor(final Color COLOR) {
        if (null == barColor) {
            _barColor = COLOR;
        } else {
            barColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> barColorProperty() {
        if (null == barColor) { barColor = new SimpleObjectProperty<>(Gauge.this, "barColor", _barColor); }
        return barColor;
    }

    public LcdDesign getLcdDesign() { return null == lcdDesign ? _lcdDesign : lcdDesign.get(); }
    public void setLcdDesign(final LcdDesign DESIGN) {
        if (null == lcdDesign) {
            _lcdDesign = DESIGN;
        } else {
            lcdDesign.set(DESIGN);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<LcdDesign> lcdDesignProperty() {
        if (null == lcdDesign) { lcdDesign = new SimpleObjectProperty<>(Gauge.this, "lcdDesign", _lcdDesign); }
        return lcdDesign;
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
        if (null == lcdFont) { lcdFont = new SimpleObjectProperty<>(Gauge.this, "lcdFont", _lcdFont); }
        return lcdFont;
    }

    public Color getLedColor() { return null == ledColor ? _ledColor : ledColor.get(); }
    public void setLedColor(final Color COLOR) {
        if (null == ledColor) {
            _ledColor = COLOR;
        } else {
            ledColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> ledColorProperty() {
        if (null == ledColor) { ledColor = new SimpleObjectProperty<>(Gauge.this, "ledColor", _ledColor); }
        return ledColor;
    }

    public LedType getLedType() { return null == ledType ? _ledType : ledType.get(); }
    public void setLedType(final LedType TYPE) {
        if (null == ledType) {
            _ledType = TYPE;
        } else {
            ledType.set(TYPE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<LedType> ledTypeProperty() {
        if (null == ledType) { ledType = new SimpleObjectProperty<>(Gauge.this, "ledType", _ledType); }
        return ledType;
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
        if (null == titleColor) { titleColor = new SimpleObjectProperty<>(Gauge.this, "titleColor", _titleColor); }
        return titleColor;
    }

    public Color getSubTitleColor() { return null == subTitleColor ? _subTitleColor : subTitleColor.get(); }
    public void setSubTitleColor(final Color COLOR) {
        if (null == subTitleColor) {
            _subTitleColor = COLOR;
        } else {
            subTitleColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> subTitleColorProperty() {
        if (null == subTitleColor) { subTitleColor = new SimpleObjectProperty<>(Gauge.this, "subTitleColor", _subTitleColor); }
        return subTitleColor;
    }

    public Color getUnitColor() { return null == unitColor ? _unitColor : unitColor.get(); }
    public void setUnitColor(final Color COLOR) {
        if (null == unitColor) {
            _unitColor = COLOR;
        } else {
            unitColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> unitColorProperty() {
        if (null == unitColor) { unitColor = new SimpleObjectProperty<>(Gauge.this, "unitColor", _unitColor); }
        return unitColor;
    }

    public Color getValueColor() { return null == valueColor ? _valueColor : valueColor.get(); }
    public void setValueColor(final Color COLOR) {
        if (null == valueColor) {
            _valueColor = COLOR;
        } else {
            valueColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> valueColorProperty() {
        if (null == valueColor) { valueColor = new SimpleObjectProperty<>(Gauge.this, "valueColor", _valueColor); }
        return valueColor;
    }

    public Color getThresholdColor() { return null == thresholdColor ? _thresholdColor : thresholdColor.get(); }
    public void setThresholdColor(final Color COLOR) {
        if (null == thresholdColor) {
            _thresholdColor = COLOR;
        } else {
            thresholdColor.set(COLOR);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public ObjectProperty<Color> thresholdColorProperty() {
        if (null == thresholdColor) { thresholdColor = new SimpleObjectProperty<>(Gauge.this, "thresholdColor", _thresholdColor); }
        return thresholdColor;
    }

    public boolean getCheckSectionsForValue() { return null == checkSectionsForValue ? _checkSectionsForValue : checkSectionsForValue.get(); }
    public void setCheckSectionsForValue(final boolean CHECK) {
        if (null == checkSectionsForValue) { _checkSectionsForValue = CHECK; } else { checkSectionsForValue.set(CHECK); }
    }
    public BooleanProperty checkSectionsForValueProperty() {
        if (null == checkSectionsForValue) { checkSectionsForValue = new SimpleBooleanProperty(Gauge.this, "checkSectionsForValue", _checkSectionsForValue); }
        return checkSectionsForValue;
    }

    public boolean getCheckAreasForValue() { return null == checkAreasForValue ? _checkAreasForValue : checkAreasForValue.get(); }
    public void setCheckAreasForValue(final boolean CHECK) {
        if (null == checkAreasForValue) { _checkAreasForValue = CHECK; } else { checkAreasForValue.set(CHECK); }
    }
    public BooleanProperty checkAreasForValueProperty() {
        if (null == checkAreasForValue) { checkAreasForValue = new SimpleBooleanProperty(Gauge.this, "checkAreasForValue", _checkAreasForValue); }
        return checkAreasForValue;
    }

    public boolean isCheckThreshold() { return null == checkThreshold ? _checkThreshold : checkThreshold.get(); }
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

    public boolean isInnerShadowEnabled() { return null == innerShadowEnabled ? _innerShadowEnabled : innerShadowEnabled.get(); }
    public void setInnerShadowEnabled(final boolean ENABLED) {
        if (null == innerShadowEnabled) {
            _innerShadowEnabled = ENABLED;
        } else {
            innerShadowEnabled.set(ENABLED);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty innerShadowEnabledProperty() {
        if (null == innerShadowEnabled) { innerShadowEnabled = new SimpleBooleanProperty(Gauge.this, "innerShadowEnabled", _innerShadowEnabled); }
        return innerShadowEnabled;
    }

    public boolean isThresholdVisible() { return null == thresholdVisible ? _thresholdVisible : thresholdVisible.get(); }
    public void setThresholdVisible(final boolean VISIBLE) {
        if (null == thresholdVisible) {
            _thresholdVisible = VISIBLE;
        } else {
            thresholdVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty thresholdVisibleProperty() {
        if (null == thresholdVisible) { thresholdVisible = new SimpleBooleanProperty(Gauge.this, "thresholdVisible", _thresholdVisible); }
        return thresholdVisible;
    }

    public boolean areSectionsVisible() { return null == sectionsVisible ? _sectionsVisible : sectionsVisible.get(); }
    public void setSectionsVisible(final boolean VISIBLE) {
        if (null == sectionsVisible) {
            _sectionsVisible = VISIBLE;
        } else {
            sectionsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty sectionsVisibleProperty() {
        if (null == sectionsVisible) { sectionsVisible = new SimpleBooleanProperty(Gauge.this, "sectionsVisible", _sectionsVisible); }
        return sectionsVisible;
    }

    public boolean isSectionTextVisible() { return null == sectionTextVisible ? _sectionTextVisible : sectionTextVisible.get(); }
    public void setSectionTextVisible(final boolean VISIBLE) {
        if (null == sectionTextVisible) {
            _sectionTextVisible = VISIBLE;
        } else {
            sectionTextVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty sectionTextVisibleProperty() {
        if (null == sectionTextVisible) { sectionTextVisible = new SimpleBooleanProperty(Gauge.this, "sectionTextVisible", _sectionTextVisible); }
        return sectionTextVisible;
    }

    public boolean areSectionIconsVisible() { return null == sectionIconsVisible ? _sectionIconsVisible : sectionIconsVisible.get(); }
    public void setSectionIconsVisible(final boolean VISIBLE) {
        if (null == sectionIconsVisible) {
            _sectionIconsVisible = VISIBLE;
        } else {
            sectionIconsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty sectionIconsVisibleProperty() {
        if (null == sectionIconsVisible) { sectionIconsVisible = new SimpleBooleanProperty(Gauge.this, "sectionIconsVisible", _sectionIconsVisible); }
        return sectionIconsVisible;
    }

    public boolean areAreasVisible() { return null == areasVisible ? _areasVisible : areasVisible.get(); }
    public void setAreasVisible(final boolean VISIBLE) {
        if (null == areasVisible) {
            _areasVisible = VISIBLE;
        } else {
            areasVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty areasVisibleProperty() {
        if (null == areasVisible) { areasVisible = new SimpleBooleanProperty(Gauge.this, "areasVisible", _areasVisible); }
        return areasVisible;
    }

    public boolean areTickMarkSectionsVisible() { return null == tickMarkSectionsVisible ? _tickMarkSectionsVisible : tickMarkSectionsVisible.get(); }
    public void setTickMarkSectionsVisible(final boolean VISIBLE) {
        if (null == tickMarkSectionsVisible) {
            _tickMarkSectionsVisible = VISIBLE;
        } else {
            tickMarkSectionsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty TickMarkSectionsVisibleProperty() {
        if (null == tickMarkSectionsVisible) { tickMarkSectionsVisible = new SimpleBooleanProperty(Gauge.this, "tickMarkSectionsVisible", _tickMarkSectionsVisible); }
        return tickMarkSectionsVisible;
    }

    public boolean areTickLabelSectionsVisible() { return null == tickLabelSectionsVisible ? _tickLabelSectionsVisible : tickLabelSectionsVisible.get(); }
    public void setTickLabelSectionsVisible(final boolean VISIBLE) {
        if (null == tickLabelSectionsVisible) {
            _tickLabelSectionsVisible = VISIBLE;
        } else {
            tickLabelSectionsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty TickLabelSectionsVisibleProperty() {
        if (null == tickLabelSectionsVisible) { tickLabelSectionsVisible = new SimpleBooleanProperty(Gauge.this, "tickLabelSectionsVisible", _tickLabelSectionsVisible); }
        return tickLabelSectionsVisible;
    }

    public boolean areMarkersVisible() { return null == markersVisible ? _markersVisible : markersVisible.get() ; }
    public void setMarkersVisible(final boolean VISIBLE) {
        if (null == markersVisible) {
            _markersVisible = VISIBLE;
        } else {
            markersVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty markersVisibleProperty() {
        if (null == markersVisible) { markersVisible = new SimpleBooleanProperty(Gauge.this, "markersVisible", _markersVisible); }
        return markersVisible;
    }

    public boolean areTickLabelsVisible() { return null == tickLabelsVisible ? _tickLabelsVisible : tickLabelsVisible.get(); }
    public void setTickLabelsVisible(final boolean VISIBLE) {
        if (null == tickLabelsVisible) {
            _tickLabelsVisible = VISIBLE;
        } else {
            tickLabelsVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty tickLabelsVisibleProperty() {
        if (null == tickLabelsVisible) { tickLabelsVisible = new SimpleBooleanProperty(Gauge.this, "tickLabelsVisible", _tickLabelsVisible); }
        return tickLabelsVisible;
    }

    public boolean areMajorTickMarksVisible() { return null == majorTickMarksVisible ? _majorTickMarksVisible : majorTickMarksVisible.get(); }
    public void setMajorTickMarksVisible(final boolean VISIBLE) {
        if (null == majorTickMarksVisible) {
            _majorTickMarksVisible = VISIBLE;
        } else {
            majorTickMarksVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty majorTickMarksVisibleProperty() {
        if (null == majorTickMarksVisible) { majorTickMarksVisible = new SimpleBooleanProperty(Gauge.this, "majorTickMarksVisible", _majorTickMarksVisible); }
        return majorTickMarksVisible;
    }

    public boolean areMediumTickMarksVisible() { return null == mediumTickMarksVisible ? _mediumTickMarksVisible : mediumTickMarksVisible.get(); }
    public void setMediumTickMarksVisible(final boolean VISIBLE) {
        if (null == mediumTickMarksVisible) {
            _mediumTickMarksVisible = VISIBLE;
        } else {
            mediumTickMarksVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty mediumTickMarksVisibleProperty() {
        if (null == mediumTickMarksVisible) { mediumTickMarksVisible = new SimpleBooleanProperty(Gauge.this, "mediumTickMarksVisible", _mediumTickMarksVisible); }
        return mediumTickMarksVisible;
    }
    
    public boolean areMinorTickMarksVisible() { return null == minorTickMarksVisible ? _minorTickMarksVisible : minorTickMarksVisible.get(); }
    public void setMinorTickMarksVisible(final boolean VISIBLE) {
        if (null == minorTickMarksVisible) {
            _minorTickMarksVisible = VISIBLE;
        } else {
            minorTickMarksVisible.set(VISIBLE);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty MinorTickMarksVisibleProperty() {
        if (null == minorTickMarksVisible) { minorTickMarksVisible = new SimpleBooleanProperty(Gauge.this, "minorTickMarksVisible", _minorTickMarksVisible); }
        return minorTickMarksVisible;
    }

    public double getMajorTickSpace() { return null == majorTickSpace ? _majorTickSpace : majorTickSpace.get(); }
    public void setMajorTickSpace(final double SPACE) {
        if (null == majorTickSpace) {
            _majorTickSpace = SPACE;
        } else {
            majorTickSpace.set(SPACE);
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public DoubleProperty majorTickSpaceProperty() {
        if (null == majorTickSpace) { majorTickSpace = new SimpleDoubleProperty(Gauge.this, "majorTickSpace", _majorTickSpace); }
        return majorTickSpace;
    }

    public double getMinorTickSpace() { return null == minorTickSpace ? _minorTickSpace : minorTickSpace.get(); }
    public void setMinorTickSpace(final double SPACE) {
        if (null == minorTickSpace) {
            _minorTickSpace = SPACE;
        } else {
            minorTickSpace.set(SPACE);
        }
        fireUpdateEvent(RECALC_EVENT);
    }
    public DoubleProperty minorTickSpaceProperty() {
        if (null == minorTickSpace) { minorTickSpace = new SimpleDoubleProperty(Gauge.this, "minorTickSpace", _minorTickSpace); }
        return minorTickSpace;
    }

    public boolean isLcdVisible() { return null == lcdVisible ? _lcdVisible : lcdVisible.get(); }
    public void setLcdVisible(final boolean VISIBLE) {
        if (null == lcdVisible) {
            _lcdVisible = VISIBLE;
        } else {
            lcdVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty lcdVisibleProperty() {
        if (null == lcdVisible) { lcdVisible = new SimpleBooleanProperty(Gauge.this, "lcdVisible", _lcdVisible); }
        return lcdVisible;
    }

    public boolean isLedVisible() { return null == ledVisible ? _ledVisible : ledVisible.get(); }
    public void setLedVisible(final boolean VISIBLE) {
        if (null == ledVisible) {
            _ledVisible = VISIBLE;
        } else {
            ledVisible.set(VISIBLE);
        }
        fireUpdateEvent(VISIBILITY_EVENT);
    }
    public BooleanProperty ledVisibleProperty() {
        if (null == ledVisible) { ledVisible = new SimpleBooleanProperty(Gauge.this, "ledVisible", _ledVisible); }
        return ledVisible;
    }

    public boolean isLedOn() { return null == ledOn ? _ledOn : ledOn.get(); }
    public void setLedOn(final boolean ON) {
        if (null == ledOn) {
            _ledOn = ON;
        } else {
            ledOn.set(ON);
        }
        fireUpdateEvent(LED_BLINK_EVENT);
    }
    public BooleanProperty ledOnProperty() {
        if (null == ledOn) { ledOn = new SimpleBooleanProperty(Gauge.this, "ledOn", _ledOn); }
        return ledOn;
    }

    public boolean isLedBlinking() { return null == ledBlinking ? _ledBlinking : ledBlinking.get(); }
    public void setLedBlinking(final boolean BLINKING) {
        if (null == ledBlinking) {
            _ledBlinking = BLINKING;
        } else {
            ledBlinking.set(BLINKING);
        }
        if (BLINKING) {
            startBlinkExecutorService();
        } else {
            blinkFuture.cancel(true);
            setLedOn(false);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty ledBlinkingProperty() {
        if (null == ledBlinking) { ledBlinking = new SimpleBooleanProperty(Gauge.this, "ledBlinking", _ledBlinking); }
        return ledBlinking;
    }

    public Orientation getOrientation() { return null == orientation ? _orientation : orientation.get(); }
    public void setOrientation(final Orientation ORIENTATION) {
        if (null == orientation) {
            _orientation = ORIENTATION;
        } else {
            orientation.set(ORIENTATION);
        }
        fireUpdateEvent(RESIZE_EVENT);
    }
    public ObjectProperty<Orientation> orientationProperty() {
        if (null == orientation) { orientation = new SimpleObjectProperty<>(Gauge.this, "orientation", _orientation); }
        return orientation;
    }

    public boolean isColorGradientEnabled() { return null == colorGradientEnabled ? _colorGradientEnabled : colorGradientEnabled.get(); }
    public void setColorGradientEnabled(final boolean ENABLED) {
        if (null == colorGradientEnabled) {
            _colorGradientEnabled = ENABLED;
        } else {
            colorGradientEnabled.set(ENABLED);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty colorGradientEnabledProperty() {
        if (null == colorGradientEnabled) { colorGradientEnabled = new SimpleBooleanProperty(Gauge.this, "colorGradientEnabled", _colorGradientEnabled); }
        return colorGradientEnabled;
    }

    public GradientLookup getGradientLookup() {
        if (null == gradientLookup) { gradientLookup = new GradientLookup(); }
        return gradientLookup;
    }
    public void setGradientLookup(final GradientLookup GRADIENT_LOOKUP) { gradientLookup = GRADIENT_LOOKUP; }
    public List<Stop> getGradientLookupStops() { return getGradientLookup().getStops(); }
    public void setGradientLookupStops(final Stop... STOPS) { getGradientLookup().setStops(STOPS);}
    public void setGradientLookupStops(final List<Stop> STOPS) { getGradientLookup().setStops(STOPS);}

    public boolean areCustomTickLabelsEnabled() { return null == customTickLabelsEnabled ? _customTickLabelsEnabled : customTickLabelsEnabled.get(); }
    public void setCustomTickLabelsEnabled(final boolean ENABLED) {
        if (null == customTickLabelsEnabled) {
            _customTickLabelsEnabled = ENABLED;
        } else {
            customTickLabelsEnabled.set(ENABLED);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public BooleanProperty getCustomTickLabelsEnabledProperty() {
        if (null == customTickLabelsEnabled) { customTickLabelsEnabled = new SimpleBooleanProperty(Gauge.this, "customTickLabelsEnabled", _customTickLabelsEnabled); }
        return customTickLabelsEnabled;
    }

    public List<String> getCustomTickLabels() { return customTickLabels; }
    public void setCustomTickLabels(final List<String> TICK_LABELS) { customTickLabels.setAll(TICK_LABELS); }
    public void setCustomTickLabels(final String... TICK_LABELS) { customTickLabels.setAll(Arrays.asList(TICK_LABELS)); }
    public void addCustomTickLabel(final String TICK_LABEL) { if (!customTickLabels.contains(TICK_LABEL)) customTickLabels.add(TICK_LABEL); }
    public void removeCustomTickLabel(final String TICK_LABEL) { if (customTickLabels.contains(TICK_LABEL)) customTickLabels.remove(TICK_LABEL); }
    public void clearCustomTickLabels() { customTickLabels.clear(); }

    public boolean isInteractive() { return null == interactive ? _interactive : interactive.get(); }
    public void setInteractive(final boolean INTERACTIVE) {
        if (null == interactive) {
            _interactive = INTERACTIVE;
        } else {
            interactive.set(INTERACTIVE);
        }
        fireUpdateEvent(INTERACTIVITY_EVENT);
    }
    public BooleanProperty interactiveProperty() {
        if (null == interactive) { interactive = new SimpleBooleanProperty(Gauge.this, "interactive", _interactive); }
        return interactive;
    }

    public String getButtonTooltipText() { return null == buttonTooltipText ? _buttonTooltipText : buttonTooltipText.get(); }
    public void setButtonTooltipText(final String TEXT) {
        if (null == buttonTooltipText) {
            _buttonTooltipText = TEXT;
        } else {
            buttonTooltipText.set(TEXT);
        }
        fireUpdateEvent(REDRAW_EVENT);
    }
    public StringProperty buttonTooltipTextProperty() {
        if (null == buttonTooltipText) { buttonTooltipText = new SimpleStringProperty(Gauge.this, "buttonTooltipText", _buttonTooltipText); }
        return buttonTooltipText;
    }

    public void calcAutoScale() {
        double maxNoOfMajorTicks = 10;
        double maxNoOfMinorTicks = 10;
        double niceRange = (Helper.calcNiceNumber(getRange(), false));
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
                    setLedOn(!isLedOn());
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
    private synchronized static void startBlinkExecutorService() {
        if (null == blinkService) {
            blinkService = new ScheduledThreadPoolExecutor(1, Helper.getThreadFactory("BlinkTask", false));
        }
        blinkFuture = blinkService.schedule(blinkTask, LED_BLINK_INTERVAL, TimeUnit.MILLISECONDS);
    }


    // ******************** CSS Stylable Properties ***************************

    
    // ******************** Style related *************************************
    @Override protected Skin createDefaultSkin() { return new GaugeSkin(this); }

    @Override public String getUserAgentStylesheet() { return getClass().getResource("gauge.css").toExternalForm(); }


    // ******************** Event handling ************************************
    public void setOnUpdate(final UpdateEventListener LISTENER) { addUpdateEventListener(LISTENER); }
    public void addUpdateEventListener(final UpdateEventListener LISTENER) { listenerList.add(LISTENER); }
    public void removeUpdateEventListener(final UpdateEventListener LISTENER) { listenerList.remove(LISTENER); }

    public void fireUpdateEvent(final UpdateEvent EVENT) {
        listenerList.forEach(listener -> listener.onUpdateEvent(EVENT));
    }

    
    public void setOnButtonPressed(EventHandler<ButtonEvent> handler) { addButtonPressedHandler(handler); }
    public void addButtonPressedHandler(EventHandler<ButtonEvent> handler) { pressedHandlerList.add(handler); }
    public void removeButtonPressedHandler(EventHandler<ButtonEvent> handler) { pressedHandlerList.remove(handler); }

    public void setOnButtonReleased(EventHandler<ButtonEvent> handler) { addButtonReleasedHandler(handler); }
    public void addButtonReleasedHandler(EventHandler<ButtonEvent> handler) { releasedHandlerList.add(handler); }
    public void removeButtonReleasedHandler(EventHandler<ButtonEvent> handler) { releasedHandlerList.remove(handler); }

    public void fireButtonEvent(final ButtonEvent EVENT) {
        final EventType TYPE = EVENT.getEventType();
        if (ButtonEvent.BUTTON_PRESSED == TYPE) {
            pressedHandlerList.forEach(handler -> handler.handle(EVENT));
        } else if (ButtonEvent.BUTTON_RELEASED == TYPE) {
            releasedHandlerList.forEach(handler -> handler.handle(EVENT));
        }
    }


    public void setOnThresholdExceeded(EventHandler<ThresholdEvent> handler) { addThresholdExceededHandler(handler); }
    public void addThresholdExceededHandler(EventHandler<ThresholdEvent> handler) { exceededHandlerList.add(handler); }
    public void removeThresholdExceededHandler(EventHandler<ThresholdEvent> handler) { exceededHandlerList.remove(handler); }

    public void setOnThresholdUnderrun(EventHandler<ThresholdEvent> handler) { addThresholdUnderrunHandler(handler); }
    public void addThresholdUnderrunHandler(EventHandler<ThresholdEvent> handler) { underrunHandlerList.add(handler); }
    public void removeThresholdUnderrunHandler(EventHandler<ThresholdEvent> handler) { underrunHandlerList.remove(handler); }

    public void fireThresholdEvent(final ThresholdEvent EVENT) {
        final EventType TYPE = EVENT.getEventType();
        if (ThresholdEvent.THRESHOLD_EXCEEDED == TYPE) {
            exceededHandlerList.forEach(handler -> handler.handle(EVENT));
        } else if (ThresholdEvent.THRESHOLD_UNDERRUN == TYPE) {
            underrunHandlerList.forEach(handler -> handler.handle(EVENT));
        }
    }
    

    // ******************** Inner Classes *************************************
    public static class ButtonEvent extends Event {
        public static final EventType<ButtonEvent> BUTTON_PRESSED  = new EventType(ANY, "BUTTON_PRESSED");
        public static final EventType<ButtonEvent> BUTTON_RELEASED = new EventType(ANY, "BUTTON_RELEASED");

        // ******************** Constructors **************************************
        public ButtonEvent(final Object SOURCE, final EventTarget TARGET, EventType<ButtonEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
    
    public static class ThresholdEvent extends Event {
        public static final EventType<ThresholdEvent> THRESHOLD_EXCEEDED = new EventType(ANY, "THRESHOLD_EXCEEDED");
        public static final EventType<ThresholdEvent> THRESHOLD_UNDERRUN = new EventType(ANY, "THRESHOLD_UNDERRUN");

        // ******************** Constructors **************************************
        public ThresholdEvent(final Object SOURCE, final EventTarget TARGET, EventType<ThresholdEvent> TYPE) {
            super(SOURCE, TARGET, TYPE);
        }
    }
}
