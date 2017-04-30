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

import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.NeedleBehavior;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.NeedleType;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.tools.GradientLookup;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;


/**
 * Created by hansolo on 13.12.15.
 */
public class GaugeBuilder<B extends GaugeBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected GaugeBuilder() {}


    // ******************** Methods *******************************************
    public static final GaugeBuilder create() {
        return new GaugeBuilder();
    }

    public final B skinType(final SkinType TYPE) {
        properties.put("skinType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B minValue(final double VALUE) {
        properties.put("minValue", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B maxValue(final double VALUE) {
        properties.put("maxValue", new SimpleDoubleProperty(VALUE));
        return (B) this;
    }

    public final B threshold(final double VALUE) {
        properties.put("threshold", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B decimals(final int DECIMALS) {
        properties.put("decimals", new SimpleIntegerProperty(DECIMALS));
        return (B) this;
    }

    public final B tickLabelDecimals(final int DECIMALS) {
        properties.put("tickLabelDecimals", new SimpleIntegerProperty(DECIMALS));
        return (B)this;
    }

    public final B title(final String TITLE) {
        properties.put("title", new SimpleStringProperty(TITLE));
        return (B)this;
    }

    public final B subTitle(final String SUBTITLE) {
        properties.put("subTitle", new SimpleStringProperty(SUBTITLE));
        return (B)this;
    }

    public final B unit(final String UNIT) {
        properties.put("unit", new SimpleStringProperty(UNIT));
        return (B)this;
    }

    public final B averagingEnabled(final boolean ENABLED) {
        properties.put("averagingEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B averagingPeriod(final int PERIOD) {
        properties.put("averagingPeriod", new SimpleIntegerProperty(PERIOD));
        return (B)this;
    }

    public final B foregroundBaseColor(final Color COLOR) {
        properties.put("foregroundBaseColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B startFromZero(final boolean START) {
        properties.put("startFromZero", new SimpleBooleanProperty(START));
        return (B)this;
    }

    public final B returnToZero(final boolean RETURN) {
        properties.put("returnToZero", new SimpleBooleanProperty(RETURN));
        return (B)this;
    }

    public final B zeroColor(final Color COLOR) {
        properties.put("zeroColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B minMeasuredValueVisible(final boolean VISIBLE) {
        properties.put("minMeasuredValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B maxMeasuredValueVisible(final boolean VISIBLE) {
        properties.put("maxMeasuredValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B oldValueVisible(final boolean VISIBLE) {
        properties.put("oldValueVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B valueVisible(final boolean VISIBLE) {
        properties.put("valueVisible", new SimpleBooleanProperty(VISIBLE));
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

    public final B knobColor(final Color COLOR) {
        properties.put("knobColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B knobType(final KnobType TYPE) {
        properties.put("knobType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B knobVisible(final boolean VISIBLE) {
        properties.put("knobVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B knobPosition(final Pos POSITION) {
        properties.put("knobPosition", new SimpleObjectProperty<>(POSITION));
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

    public final B startAngle(final double ANGLE) {
        properties.put("startAngle", new SimpleDoubleProperty(ANGLE));
        return (B)this;
    }

    public final B angleRange(final double RANGE) {
        properties.put("angleRange", new SimpleDoubleProperty(RANGE));
        return (B)this;
    }

    public final B autoScale(final boolean AUTO_SCALE) {
        properties.put("autoScale", new SimpleBooleanProperty(AUTO_SCALE));
        return (B)this;
    }

    public final B needleType(final NeedleType TYPE) {
        properties.put("needleType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B needleShape(final NeedleShape SHAPE) {
        properties.put("needleShape", new SimpleObjectProperty<>(SHAPE));
        return (B)this;
    }

    public final B needleSize(final NeedleSize SIZE) {
        properties.put("needleSize", new SimpleObjectProperty<>(SIZE));
        return (B)this;
    }

    public final B needleBehavior(final NeedleBehavior BEHAVIOR) {
        properties.put("needleBehavior", new SimpleObjectProperty<>(BEHAVIOR));
        return (B)this;
    }

    public final B needleColor(final Color COLOR) {
        properties.put("needleColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B needleBorderColor(final Color COLOR) {
        properties.put("needleBorderColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barColor(final Color COLOR) {
        properties.put("barColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barBorderColor(final Color COLOR) {
        properties.put("barBorderColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B barBackgroundColor(final Color COLOR) {
        properties.put("barBackgroundColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B tickLabelOrientation(final TickLabelOrientation ORIENTATION) {
        properties.put("tickLabelOrientation", new SimpleObjectProperty<>(ORIENTATION));
        return (B)this;
    }

    public final B tickLabelLocation(final TickLabelLocation LOCATION) {
        properties.put("tickLabelLocation", new SimpleObjectProperty<>(LOCATION));
        return (B)this;
    }

    public final B locale(final Locale LOCALE) {
        properties.put("locale", new SimpleObjectProperty<>(LOCALE));
        return (B)this;
    }

    public final B majorTickSpace(final double SPACE) {
        properties.put("majorTickSpace", new SimpleDoubleProperty(SPACE));
        return (B)this;
    }

    public final B minorTickSpace(final double SPACE) {
        properties.put("minorTickSpace", new SimpleDoubleProperty(SPACE));
        return (B)this;
    }

    public final B shadowsEnabled(final boolean ENABLED) {
        properties.put("shadowsEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B barEffectEnabled(final boolean ENABLED) {
        properties.put("barEffectEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B scaleDirection(final ScaleDirection DIRECTION) {
        properties.put("scaleDirection", new SimpleObjectProperty<>(DIRECTION));
        return (B)this;
    }

    public final B tickLabelColor(final Color COLOR) {
        properties.put("tickLabelColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B tickMarkColor(final Color COLOR) {
        properties.put("tickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B majorTickMarkColor(final Color COLOR) {
        properties.put("majorTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B majorTickMarkLengthFactor(final double FACTOR) {
        properties.put("majorTickMarkLengthFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }
    
    public final B majorTickMarkWidthFactor(final double FACTOR) {
        properties.put("majorTickMarkWidthFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }
    
    public final B mediumTickMarkColor(final Color COLOR) {
        properties.put("mediumTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B mediumTickMarkLengthFactor(final double FACTOR) {
        properties.put("mediumTickMarkLengthFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }

    public final B mediumTickMarkWidthFactor(final double FACTOR) {
        properties.put("mediumTickMarkWidthFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }
    
    public final B minorTickMarkColor(final Color COLOR) {
        properties.put("minorTickMarkColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B minorTickMarkLengthFactor(final double FACTOR) {
        properties.put("minorTickMarkLengthFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }

    public final B minorTickMarkWidthFactor(final double FACTOR) {
        properties.put("minorTickMarkWidthFactor", new SimpleDoubleProperty(FACTOR));
        return (B)this;
    }
    
    public final B majorTickMarkType(final TickMarkType TYPE) {
        properties.put("majorTickMarkType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B mediumTickMarkType(final TickMarkType TYPE) {
        properties.put("mediumTickMarkType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B minorTickMarkType(final TickMarkType TYPE) {
        properties.put("minorTickMarkType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B style(final String STYLE) {
        properties.put("style", new SimpleStringProperty(STYLE));
        return (B)this;
    }

    public final B styleClass(final String... STYLES) {
        properties.put("styleClass", new SimpleObjectProperty<>(STYLES));
        return (B)this;
    }

    public final B ledColor(final Color COLOR) {
        properties.put("ledColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B ledType(final LedType TYPE) {
        properties.put("ledType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B ledVisible(final boolean VISIBLE) {
        properties.put("ledVisible", new SimpleBooleanProperty(VISIBLE));
        return (B) this;
    }

    public final B lcdVisible(final boolean VISIBLE) {
        properties.put("lcdVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B lcdCrystalEnabled(final boolean ENABLED) {
        properties.put("lcdCrystalEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B lcdDesign(final LcdDesign DESIGN) {
        properties.put("lcdDesign", new SimpleObjectProperty<>(DESIGN));
        return (B)this;
    }

    public final B lcdFont(final LcdFont FONT) {
        properties.put("lcdFont", new SimpleObjectProperty<>(FONT));
        return (B)this;
    }

    public final B sections(final Section... SECTIONS) {
        properties.put("sectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B sections(final List<Section> SECTIONS) {
        properties.put("sectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B areas(final Section... AREAS) {
        properties.put("areasArray", new SimpleObjectProperty<>(AREAS));
        return (B)this;
    }

    public final B areas(final List<Section> AREAS) {
        properties.put("areasList", new SimpleObjectProperty<>(AREAS));
        return (B)this;
    }

    public final B tickMarkSections(final Section... SECTIONS) {
        properties.put("tickMarkSectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B tickMarkSections(final List<Section> SECTIONS) {
        properties.put("tickMarkSectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B tickLabelSections(final Section... SECTIONS) {
        properties.put("tickLabelSectionsArray", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B tickLabelSections(final List<Section> SECTIONS) {
        properties.put("tickLabelSectionsList", new SimpleObjectProperty<>(SECTIONS));
        return (B)this;
    }

    public final B markers(final Marker... MARKERS) {
        properties.put("markersArray", new SimpleObjectProperty<>(MARKERS));
        return (B)this;
    }

    public final B markers(final List<Marker> MARKERS) {
        properties.put("markersList", new SimpleObjectProperty<>(MARKERS));
        return (B)this;
    }

    public final B titleColor(final Color COLOR) {
        properties.put("titleColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B subTitleColor(final Color COLOR) {
        properties.put("subTitleColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B unitColor(final Color COLOR) {
        properties.put("unitColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B valueColor(final Color COLOR) {
        properties.put("valueColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B thresholdColor(final Color COLOR) {
        properties.put("thresholdColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public B averageColor(final Color COLOR) {
        properties.put("averageColor", new SimpleObjectProperty<>(COLOR));
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

    public final B checkThreshold(final boolean CHECK) {
        properties.put("checkThreshold", new SimpleBooleanProperty(CHECK));
        return (B)this;
    }

    public final B innerShadowEnabled(final boolean ENABLED) {
        properties.put("innerShadowEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B thresholdVisible(final boolean VISIBLE) {
        properties.put("thresholdVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B averageVisible(final boolean VISIBLE) {
        properties.put("averageVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionsVisible(final boolean VISIBLE) {
        properties.put("sectionsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionsAlwaysVisible(final boolean VISIBLE) {
        properties.put("sectionsAlwaysVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionTextVisible(final boolean VISIBLE) {
        properties.put("sectionTextVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B sectionIconsVisible(final boolean VISIBLE) {
        properties.put("sectionIconsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B highlightSections(final boolean HIGHLIGHT) {
        properties.put("highlightSections", new SimpleBooleanProperty(HIGHLIGHT));
        return (B)this;
    }

    public final B areasVisible(final boolean VISIBLE) {
        properties.put("areasVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B areaTextVisible(final boolean VISIBLE) {
        properties.put("areaTextVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B areaIconsVisible(final boolean VISIBLE) {
        properties.put("areaIconsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B highlightAreas(final boolean HIGHLIGHT) {
        properties.put("highlightAreas", new SimpleBooleanProperty(HIGHLIGHT));
        return (B)this;
    }

    public final B tickMarkSectionsVisible(final boolean VISIBLE) {
        properties.put("tickMarkSectionsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B tickLabelSectionsVisible(final boolean VISIBLE) {
        properties.put("tickLabelSectionsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B markersVisible(final boolean VISIBLE) {
        properties.put("markersVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B tickLabelsVisible(final boolean VISIBLE) {
        properties.put("tickLabelsVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B onlyFirstAndLastTickLabelVisible(final boolean VISIBLE) {
        properties.put("onlyFirstAndLastTickLabelVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B majorTickMarksVisible(final boolean VISIBLE) {
        properties.put("majorTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B mediumTickMarksVisible(final boolean VISIBLE) {
        properties.put("mediumTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B minorTickMarksVisible(final boolean VISIBLE) {
        properties.put("minorTickMarksVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B tickMarkRingVisible(final boolean VISIBLE) {
        properties.put("tickMarkRingVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B ledOn(final boolean ON) {
        properties.put("ledOn", new SimpleBooleanProperty(ON));
        return (B)this;
    }

    public final B ledBlinking(final boolean BLINKING) {
        properties.put("ledBlinking", new SimpleBooleanProperty(BLINKING));
        return (B)this;
    }

    public final B orientation(final Orientation ORIENTATION) {
        properties.put("orientation", new SimpleObjectProperty<>(ORIENTATION));
        return (B)this;
    }

    public final B gradientBarEnabled(final boolean ENABLED) {
        properties.put("gradientBarEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B gradientLookup(final GradientLookup GRADIENT_LOOKUP) {
        properties.put("gradientLookup", new SimpleObjectProperty<>(GRADIENT_LOOKUP));
        return (B)this;
    }

    public final B gradientBarStops(final Stop... STOPS) {
        properties.put("gradientBarStopsArray", new SimpleObjectProperty<>(STOPS));
        return (B)this;
    }

    public final B gradientBarStops(final List<Stop> STOPS) {
        properties.put("gradientBarStopsList", new SimpleObjectProperty<>(STOPS));
        return (B)this;
    }

    public final B customTickLabelsEnabled(final boolean ENABLED) {
        properties.put("customTickLabelsEnabled", new SimpleBooleanProperty(ENABLED));
        return (B)this;
    }

    public final B customTickLabels(final String... TICK_LABELS) {
        properties.put("customTickLabelsArray", new SimpleObjectProperty<>(TICK_LABELS));
        return (B)this;
    }

    public final B customTickLabels(final List<String> TICK_LABELS) {
        properties.put("customTickLabelsList", new SimpleObjectProperty<>(TICK_LABELS));
        return (B)this;
    }

    public final B customTickLabelFontSize(final double SIZE) {
        properties.put("customTickLabelFontSize", new SimpleDoubleProperty(SIZE));
        return (B)this;
    }

    public final B interactive(final boolean INTERACTIVE) {
        properties.put("interactive", new SimpleBooleanProperty(INTERACTIVE));
        return (B)this;
    }

    public final B buttonTooltipText(final String TEXT) {
        properties.put("buttonTooltipText", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B keepAspect(final boolean KEEP) {
        properties.put("keepAspect", new SimpleBooleanProperty(KEEP));
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

    public final B alertMessage(final String MESSAGE) {
        properties.put("alertMessage", new SimpleStringProperty(MESSAGE));
        return (B)this;
    }

    public final B smoothing(final boolean SMOOTHING) {
        properties.put("smoothing", new SimpleBooleanProperty(SMOOTHING));
        return (B)this;
    }

    public final B onValueChanged(final InvalidationListener LISTENER) {
        properties.put("onValueChanged", new SimpleObjectProperty<>(LISTENER));
        return (B)this;
    }

    public final B onButtonPressed(final EventHandler<Gauge.ButtonEvent> HANDLER) {
        properties.put("onButtonPressed", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onButtonReleased(final EventHandler<Gauge.ButtonEvent> HANDLER) {
        properties.put("onButtonReleased", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onThresholdExceeded(final EventHandler<Gauge.ThresholdEvent> HANDLER) {
        properties.put("onThresholdExceeded", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onThresholdUnderrun(final EventHandler<Gauge.ThresholdEvent> HANDLER) {
        properties.put("onThresholdUnderrun", new SimpleObjectProperty<>(HANDLER));
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

    public final Gauge build() {
        final Gauge CONTROL;
        if (properties.containsKey("skinType")) {
            SkinType skinType = ((ObjectProperty<SkinType>) properties.get("skinType")).get();
            CONTROL = new Gauge(skinType);
            switch(skinType) {
                case AMP         :
                    CONTROL.setKnobPosition(Pos.BOTTOM_CENTER);
                    CONTROL.setTitleColor(Color.WHITE);
                    CONTROL.setLedVisible(true);
                    CONTROL.setBackgroundPaint(Color.WHITE);
                    CONTROL.setForegroundPaint(Color.BLACK);
                    CONTROL.setLcdVisible(true);
                    CONTROL.setShadowsEnabled(true);
                    break;
                case BULLET_CHART:
                    CONTROL.setKnobPosition(Pos.CENTER);
                    CONTROL.setBarColor(Color.BLACK);
                    CONTROL.setThresholdColor(Color.BLACK);
                    break;
                case DASHBOARD   :
                    CONTROL.setKnobPosition(Pos.BOTTOM_CENTER);
                    CONTROL.setDecimals(0);
                    CONTROL.setBarBackgroundColor(Color.LIGHTGRAY);
                    CONTROL.setBarColor(Color.rgb(93,190,205));
                    CONTROL.setStartFromZero(false);
                    break;
                case FLAT        :
                    CONTROL.setKnobPosition(Pos.CENTER);
                    CONTROL.setBarColor(Color.CYAN);
                    CONTROL.setBackgroundPaint(Color.TRANSPARENT);
                    CONTROL.setTitleColor(Gauge.DARK_COLOR);
                    CONTROL.setValueColor(Gauge.DARK_COLOR);
                    CONTROL.setUnitColor(Gauge.DARK_COLOR);
                    CONTROL.setBorderPaint(Color.rgb(208, 208, 208));
                    CONTROL.setDecimals(0);
                    CONTROL.setStartFromZero(true);
                    break;
                case INDICATOR   :
                    CONTROL.setKnobPosition(Pos.BOTTOM_CENTER);
                    CONTROL.setValueVisible(false);
                    CONTROL.setGradientBarEnabled(false);
                    CONTROL.setGradientBarStops(new Stop(0.0, Color.rgb(34,180,11)),
                                        new Stop(0.5, Color.rgb(255,146,0)),
                                        new Stop(1.0, Color.rgb(255,0,39)));
                    CONTROL.setTickLabelsVisible(false);
                    CONTROL.setNeedleColor(Color.rgb(71,71,71));
                    CONTROL.setBarBackgroundColor(Color.rgb(232,231,223));
                    CONTROL.setBarColor(Color.rgb(255,0,39));
                    CONTROL.setAngleRange(180);
                    break;
                case KPI         :
                    CONTROL.setKnobPosition(Pos.BOTTOM_CENTER);
                    CONTROL.setDecimals(0);
                    CONTROL.setForegroundBaseColor(Color.rgb(126,126,127));
                    CONTROL.setBarColor(Color.rgb(168,204,254));
                    CONTROL.setThresholdVisible(true);
                    CONTROL.setThresholdColor(Color.rgb(45,86,184));
                    CONTROL.setNeedleColor(Color.rgb(74,74,74));
                    CONTROL.setAngleRange(128);
                    break;
                case MODERN      :
                    CONTROL.setKnobPosition(Pos.CENTER);
                    CONTROL.setDecimals(0);
                    CONTROL.setValueColor(Color.WHITE);
                    CONTROL.setTitleColor(Color.WHITE);
                    CONTROL.setSubTitleColor(Color.WHITE);
                    CONTROL.setUnitColor(Color.WHITE);
                    CONTROL.setBarColor(Color.rgb(0, 214, 215));
                    CONTROL.setNeedleColor(Color.WHITE);
                    CONTROL.setThresholdColor(Color.rgb(204, 0, 0));
                    CONTROL.setTickLabelColor(Color.rgb(151, 151, 151));
                    CONTROL.setTickMarkColor(Color.BLACK);
                    CONTROL.setTickLabelOrientation(TickLabelOrientation.ORTHOGONAL);
                    break;
                case SIMPLE      :
                    CONTROL.setKnobPosition(Pos.CENTER);
                    CONTROL.setBorderPaint(Color.WHITE);
                    CONTROL.setNeedleBorderColor(Color.WHITE);
                    CONTROL.setBackgroundPaint(Color.DARKGRAY);
                    CONTROL.setDecimals(0);
                    CONTROL.setTickLabelColor(Color.WHITE);
                    CONTROL.setNeedleColor(Color.web("#5a615f"));
                    CONTROL.setValueColor(Color.WHITE);
                    CONTROL.setTitleColor(Color.WHITE);
                    CONTROL.setSubTitleColor(Color.WHITE);
                    CONTROL.setSectionsVisible(true);
                    break;
                case SLIM        :
                    CONTROL.setKnobPosition(Pos.CENTER);
                    CONTROL.setDecimals(2);
                    CONTROL.setStartFromZero(true);
                    CONTROL.setBarBackgroundColor(Color.rgb(62, 67, 73));
                    CONTROL.setBarColor(Color.rgb(93,190,205));
                    CONTROL.setTitleColor(Color.rgb(142,147,151));
                    CONTROL.setValueColor(Color.rgb(228,231,238));
                    CONTROL.setUnitColor(Color.rgb(142,147,151));
                    break;
                case SPACE_X     :
                    CONTROL.setKnobPosition(Pos.CENTER);
                    CONTROL.setDecimals(0);
                    CONTROL.setThresholdColor(Color.rgb(180, 0, 0));
                    CONTROL.setBarBackgroundColor(Color.rgb(169, 169, 169, 0.25));
                    CONTROL.setBarColor(Color.rgb(169, 169, 169));
                    CONTROL.setTitleColor(Color.WHITE);
                    CONTROL.setValueColor(Color.WHITE);
                    CONTROL.setUnitColor(Color.WHITE);
                    break;
                case QUARTER     :
                    CONTROL.setKnobPosition(Pos.BOTTOM_RIGHT);
                    CONTROL.setAngleRange(90);
                    break;
                case HORIZONTAL:
                    CONTROL.setKnobPosition(Pos.BOTTOM_CENTER);
                    CONTROL.setAngleRange(180);
                    break;
                case VERTICAL:
                    CONTROL.setKnobPosition(Pos.CENTER_RIGHT);
                    CONTROL.setAngleRange(180);
                    break;
                case LCD:
                    CONTROL.setDecimals(1);
                    CONTROL.setTickLabelDecimals(1);
                    CONTROL.setMinMeasuredValueVisible(true);
                    CONTROL.setMaxMeasuredValueVisible(true);
                    CONTROL.setOldValueVisible(true);
                    CONTROL.setBorderPaint(Color.WHITE);
                    CONTROL.setForegroundPaint(Color.WHITE);
                    break;
                case TINY:
                    CONTROL.setBorderWidth(24);
                    CONTROL.setBackgroundPaint(Color.rgb(216,216,216));
                    CONTROL.setBorderPaint(Color.rgb(76,76,76));
                    CONTROL.setBarBackgroundColor(Color.rgb(76, 76, 76, 0.2));
                    CONTROL.setNeedleColor(Color.rgb(76, 76, 76));
                    CONTROL.setSectionsVisible(true);
                    CONTROL.setMajorTickMarksVisible(true);
                    CONTROL.setMajorTickMarkColor(Color.WHITE);
                    break;
                case BATTERY:
                    CONTROL.setBarBackgroundColor(Color.BLACK);
                    CONTROL.setBarColor(Color.BLACK);
                    CONTROL.setValueColor(Color.WHITE);
                    break;
                case LEVEL:
                    CONTROL.setValueColor(Color.WHITE);
                    CONTROL.setBarColor(Color.CYAN);
                    break;
                case LINEAR:
                    CONTROL.setOrientation(Orientation.VERTICAL);
                    CONTROL.setBarColor(Gauge.DARK_COLOR);
                    CONTROL.setBarEffectEnabled(true);
                    break;
                case DIGITAL:
                    CONTROL.setBarColor(Gauge.DARK_COLOR);
                    break;
                case SIMPLE_DIGITAL:
                    CONTROL.setBarColor(Gauge.DARK_COLOR);
                    break;
                case SECTION:
                    CONTROL.setBackgroundPaint(Gauge.DARK_COLOR);
                    CONTROL.setAutoScale(false);
                    CONTROL.setValueVisible(false);
                    CONTROL.setKnobColor(Color.rgb(82,82,84));
                    CONTROL.setSectionsVisible(true);
                    CONTROL.setSectionTextVisible(true);
                    break;
                case BAR:
                    Color barColor = CONTROL.getBarColor();
                    CONTROL.setAnimated(true);
                    CONTROL.setAnimationDuration(1000);
                    CONTROL.setMinValue(0);
                    CONTROL.setMaxValue(100);
                    CONTROL.setGradientBarEnabled(true);
                    CONTROL.setGradientBarStops(new Stop(0.0, barColor),
                                                new Stop(0.01, barColor),
                                                new Stop(0.75, barColor.deriveColor(-10, 1, 1, 1)),
                                                new Stop(1.0, barColor.deriveColor(-20, 1, 1, 1)));
                    CONTROL.setBarColor(barColor);
                    CONTROL.setBarEffectEnabled(true);
                    break;
                case WHITE:
                    CONTROL.setAnimated(true);
                    CONTROL.setAnimationDuration(1000);
                    CONTROL.setAngleRange(360);
                    CONTROL.setMinValue(0);
                    CONTROL.setMaxValue(100);
                    CONTROL.setBarColor(Color.WHITE);
                    CONTROL.setValueColor(Color.WHITE);
                    CONTROL.setUnitColor(Color.WHITE);
                    break;
                case CHARGE:
                    CONTROL.setMinValue(0);
                    CONTROL.setMaxValue(1.0);
                    CONTROL.setAnimated(true);
                    break;
                case SIMPLE_SECTION:
                    CONTROL.setAnimated(true);
                    CONTROL.setStartAngle(150);
                    CONTROL.setAngleRange(300);
                    CONTROL.setStartFromZero(true);
                    CONTROL.setSectionsVisible(true);
                    CONTROL.setBarBackgroundColor(Color.rgb(150, 150, 150, 0.25));
                    CONTROL.setBarColor(Color.rgb(69, 106, 207));
                    CONTROL.setTitleColor(Color.rgb(90, 90, 90));
                    CONTROL.setUnitColor(Color.rgb(90, 90, 90));
                    CONTROL.setValueColor(Color.rgb(90, 90, 90));
                    break;
                case TILE_KPI:
                    CONTROL.setKnobPosition(Pos.BOTTOM_CENTER);
                    CONTROL.setDecimals(0);
                    CONTROL.setBackgroundPaint(Color.rgb(42,42,42));
                    CONTROL.setForegroundBaseColor(Color.rgb(238,238,238));
                    CONTROL.setBarColor(Color.rgb(238,238,238));
                    CONTROL.setThresholdVisible(false);
                    CONTROL.setThresholdColor(Color.rgb(41,177,255));
                    CONTROL.setNeedleColor(Color.rgb(238,238,238));
                    CONTROL.setAngleRange(180);
                    break;
                case TILE_TEXT_KPI:
                    CONTROL.setDecimals(0);
                    CONTROL.setBackgroundPaint(Color.rgb(42,42,42));
                    CONTROL.setForegroundBaseColor(Color.rgb(238,238,238));
                    CONTROL.setBarColor(Color.rgb(41,177,255));
                    CONTROL.setValueColor(Color.rgb(238, 238, 238));
                    CONTROL.setUnitColor(Color.rgb(238, 238, 238));
                    CONTROL.setThresholdVisible(false);
                    CONTROL.setThresholdColor(Color.rgb(139,144,146));
                    break;
                case TILE_SPARK_LINE:
                    CONTROL.setDecimals(0);
                    CONTROL.setBackgroundPaint(Color.rgb(42,42,42));
                    CONTROL.setForegroundBaseColor(Color.rgb(238,238,238));
                    CONTROL.setBarColor(Color.rgb(41,177,255));
                    CONTROL.setValueColor(Color.rgb(238, 238, 238));
                    CONTROL.setUnitColor(Color.rgb(238, 238, 238));
                    CONTROL.setAveragingEnabled(true);
                    CONTROL.setAveragingPeriod(10);
                    CONTROL.setAverageColor(Color.rgb(238, 238, 238, 0.5));
                    CONTROL.setAnimated(false);
                    break;
                case GAUGE:
                    CONTROL.setStartAngle(320);
                    CONTROL.setAngleRange(280);
                    break;
            }
        } else {
            CONTROL = new Gauge();
        }

        // Make sure that sections, areas and markers will be added first
        if (properties.keySet().contains("sectionsArray")) {
            CONTROL.setSections(((ObjectProperty<Section[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            CONTROL.setSections(((ObjectProperty<List<Section>>) properties.get("sectionsList")).get());
        }

        if (properties.keySet().contains("areasArray")) {
            CONTROL.setAreas(((ObjectProperty<Section[]>) properties.get("areasArray")).get());
        }
        if(properties.keySet().contains("areasList")) {
            CONTROL.setAreas(((ObjectProperty<List<Section>>) properties.get("areasList")).get());
        }

        if (properties.keySet().contains("tickMarkSectionsArray")) {
            CONTROL.setTickMarkSections(((ObjectProperty<Section[]>) properties.get("tickMarkSectionsArray")).get());
        }
        if(properties.keySet().contains("tickMarkSectionsList")) {
            CONTROL.setTickMarkSections(((ObjectProperty<List<Section>>) properties.get("tickMarkSectionsList")).get());
        }

        if (properties.keySet().contains("tickLabelSectionsArray")) {
            CONTROL.setTickLabelSections(((ObjectProperty<Section[]>) properties.get("tickLabelSectionsArray")).get());
        }
        if(properties.keySet().contains("tickLabelSectionsList")) {
            CONTROL.setTickLabelSections(((ObjectProperty<List<Section>>) properties.get("tickLabelSectionsList")).get());
        }

        if (properties.keySet().contains("markersArray")) {
            CONTROL.setMarkers(((ObjectProperty<Marker[]>) properties.get("markersArray")).get());
        }
        if (properties.keySet().contains("markersList")) {
            CONTROL.setMarkers(((ObjectProperty<List<Marker>>) properties.get("markersList")).get());
        }

        if (properties.keySet().contains("gradientBarStopsArray")) {
            CONTROL.setGradientBarStops(((ObjectProperty<Stop[]>) properties.get("gradientBarStopsArray")).get());
        }
        if (properties.keySet().contains("gradientBarStopsList")) {
            CONTROL.setGradientBarStops(((ObjectProperty<List<Stop>>) properties.get("gradientBarStopsList")).get());
        }

        if (properties.keySet().contains("customTickLabelsArray")) {
            CONTROL.setCustomTickLabels(((ObjectProperty<String[]>) properties.get("customTickLabelsArray")).get());
        }
        if (properties.keySet().contains("customTickLabelsList")) {
            CONTROL.setCustomTickLabels(((ObjectProperty<List<String>>) properties.get("customTickLabelsList")).get());
        }

        if(properties.keySet().contains("foregroundBaseColor")) {
            CONTROL.setForegroundBaseColor(((ObjectProperty<Color>) properties.get("foregroundBaseColor")).get());
        }

        if (properties.keySet().contains("minValue")) {
            CONTROL.setMinValue(((DoubleProperty) properties.get("minValue")).get());
        }
        if (properties.keySet().contains("maxValue")) {
            CONTROL.setMaxValue(((DoubleProperty) properties.get("maxValue")).get());
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
            } else if("styleClass".equals(key)) {
                CONTROL.getStyleClass().setAll("gauge");
                CONTROL.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
            } else if ("autoScale".equals(key)) {
                CONTROL.setAutoScale(((BooleanProperty) properties.get(key)).get());
            } else if("value".equals(key)) {
                CONTROL.setValue(((DoubleProperty) properties.get(key)).get());
            } else if("decimals".equals(key)) {
                CONTROL.setDecimals(((IntegerProperty) properties.get(key)).get());
            } else if ("tickLabelDecimals".equals(key)) {
                CONTROL.setTickLabelDecimals(((IntegerProperty) properties.get(key)).get());
            } else if("title".equals(key)) {
                CONTROL.setTitle(((StringProperty) properties.get(key)).get());
            } else if("subTitle".equals(key)) {
                CONTROL.setSubTitle(((StringProperty) properties.get(key)).get());
            } else if("unit".equals(key)) {
                CONTROL.setUnit(((StringProperty) properties.get(key)).get());
            } else if("averagingEnabled".equals(key)) {
                CONTROL.setAveragingEnabled(((BooleanProperty) properties.get(key)).get());
            } else if("averagingPeriod".equals(key)) {
                CONTROL.setAveragingPeriod(((IntegerProperty) properties.get(key)).get());
            } else if("startFromZero".equals(key)) {
                CONTROL.setStartFromZero(((BooleanProperty) properties.get(key)).get());
            } else if("returnToZero".equals(key)) {
                CONTROL.setReturnToZero(((BooleanProperty) properties.get(key)).get());
            } else if("zeroColor".equals(key)) {
                CONTROL.setZeroColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minMeasuredValueVisible".equals(key)) {
                CONTROL.setMinMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("maxMeasuredValueVisible".equals(key)) {
                CONTROL.setMaxMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("oldValueVisible".equals(key)) {
                CONTROL.setOldValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("valueVisible".equals(key)) {
                CONTROL.setValueVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("backgroundPaint".equals(key)) {
                CONTROL.setBackgroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
            } else if ("borderPaint".equals(key)) {
                CONTROL.setBorderPaint(((ObjectProperty<Paint>) properties.get(key)).get());
            } else if ("borderWidth".equals(key)) {
                CONTROL.setBorderWidth(((DoubleProperty) properties.get(key)).get());
            } else if ("foregroundPaint".equals(key)) {
                CONTROL.setForegroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
            } else if ("knobColor".equals(key)) {
                CONTROL.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("knobType".equals(key)) {
                CONTROL.setKnobType(((ObjectProperty<KnobType>) properties.get(key)).get());
            } else if ("knobPosition".equals(key)) {
                CONTROL.setKnobPosition(((ObjectProperty<Pos>) properties.get(key)).get());
            } else if ("knobVisible".equals(key)) {
                CONTROL.setKnobVisible(((BooleanProperty) properties.get(key)).get());
            } else if("animated".equals(key)) {
                CONTROL.setAnimated(((BooleanProperty) properties.get(key)).get());
            } else if("animationDuration".equals(key)) {
                CONTROL.setAnimationDuration(((LongProperty) properties.get(key)).get());
            } else if("startAngle".equals(key)) {
                CONTROL.setStartAngle(((DoubleProperty) properties.get(key)).get());
            } else if("angleRange".equals(key)) {
                CONTROL.setAngleRange(((DoubleProperty) properties.get(key)).get());
            } else if("needleType".equals(key)) {
                CONTROL.setNeedleType(((ObjectProperty<NeedleType>) properties.get(key)).get());
            } else if("needleShape".equals(key)) {
                CONTROL.setNeedleShape(((ObjectProperty<NeedleShape>) properties.get(key)).get());
            } else if("needleSize".equals(key)) {
                CONTROL.setNeedleSize(((ObjectProperty<NeedleSize>) properties.get(key)).get());
            } else if("needleBehavior".equals(key)) {
                CONTROL.setNeedleBehavior(((ObjectProperty<NeedleBehavior>) properties.get(key)).get());
            } else if("needleColor".equals(key)) {
                CONTROL.setNeedleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("needleBorderColor".equals(key)) {
                CONTROL.setNeedleBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("barColor".equals(key)) {
                CONTROL.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("barBorderColor".equals(key)) {
                CONTROL.setBarBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("barBackgroundColor".equals(key)) {
                CONTROL.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("tickLabelOrientation".equals(key)) {
                CONTROL.setTickLabelOrientation(((ObjectProperty<TickLabelOrientation>) properties.get(key)).get());
            } else if("tickLabelLocation".equals(key)) {
                CONTROL.setTickLabelLocation(((ObjectProperty<TickLabelLocation>) properties.get(key)).get());
            } else if("locale".equals(key)) {
                CONTROL.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
            } else if("majorTickSpace".equals(key)) {
                CONTROL.setMajorTickSpace(((DoubleProperty) properties.get(key)).get());
            } else if("minorTickSpace".equals(key)) {
                CONTROL.setMinorTickSpace(((DoubleProperty) properties.get(key)).get());
            } else if("shadowsEnabled".equals(key)) {
                CONTROL.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("barEffectEnabled".equals(key)) {
                CONTROL.setBarEffectEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("scaleDirection".equals(key)) {
                CONTROL.setScaleDirection(((ObjectProperty<ScaleDirection>) properties.get(key)).get());
            } else if("tickLabelColor".equals(key)) {
                CONTROL.setTickLabelColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("tickMarkColor".equals(key)) {
                CONTROL.setTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("majorTickMarkColor".equals(key)) {
                CONTROL.setMajorTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("majorTickMarkLengthFactor".equals(key)) {
                CONTROL.setMajorTickMarkLengthFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("majorTickMarkWidthFactor".equals(key)) {
                CONTROL.setMajorTickMarkWidthFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("mediumTickMarkColor".equals(key)) {
                CONTROL.setMediumTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("mediumTickMarkLengthFactor".equals(key)) {
                CONTROL.setMediumTickMarkLengthFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("mediumTickMarkWidthFactor".equals(key)) {
                CONTROL.setMediumTickMarkWidthFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("minorTickMarkColor".equals(key)) {
                CONTROL.setMinorTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("minorTickMarkLengthFactor".equals(key)) {
                CONTROL.setMinorTickMarkLengthFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("minorTickMarkWidthFactor".equals(key)) {
                CONTROL.setMinorTickMarkWidthFactor(((DoubleProperty) properties.get(key)).get());
            } else if ("style".equals(key)) {
                CONTROL.setStyle(((StringProperty) properties.get(key)).get());
            } else if("ledColor".equals(key)) {
                CONTROL.setLedColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if("ledType".equals(key)) {
                CONTROL.setLedType(((ObjectProperty<LedType>) properties.get(key)).get());
            } else if ("ledVisible".equals(key)) {
                CONTROL.setLedVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("lcdVisible".equals(key)) {
                CONTROL.setLcdVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("lcdCrystalEnabled".equals(key)) {
                CONTROL.setLcdCrystalEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("lcdDesign".equals(key)) {
                CONTROL.setLcdDesign(((ObjectProperty<LcdDesign>) properties.get(key)).get());
            } else if ("lcdFont".equals(key)) {
                CONTROL.setLcdFont(((ObjectProperty<LcdFont>) properties.get(key)).get());
            } else if ("innerShadowEnabled".equals(key)) {
                CONTROL.setInnerShadowEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("thresholdVisible".equals(key)) {
                CONTROL.setThresholdVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("averageVisible".equals(key)) {
                CONTROL.setAverageVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsVisible".equals(key)) {
                CONTROL.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionsAlwaysVisible".equals(key)) {
                CONTROL.setSectionsAlwaysVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionTextVisible".equals(key)) {
                CONTROL.setSectionTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("sectionIconsVisible".equals(key)) {
                CONTROL.setSectionIconsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("highlightSections".equals(key)) {
                CONTROL.setHighlightSections(((BooleanProperty) properties.get(key)).get());
            } else if ("areasVisible".equals(key)) {
                CONTROL.setAreasVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("areaTextVisible".equals(key)) {
                CONTROL.setAreaTextVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("areaIconsVisible".equals(key)) {
                CONTROL.setAreaIconsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("highlightAreas".equals(key)) {
                CONTROL.setHighlightAreas(((BooleanProperty) properties.get(key)).get());
            } else if ("tickMarkSectionsVisible".equals(key)) {
                CONTROL.setTickMarkSectionsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("tickLabelSectionsVisible".equals(key)) {
                CONTROL.setTickLabelSectionsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("markersVisible".equals(key)) {
                CONTROL.setMarkersVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("majorTickMarkType".equals(key)) {
                CONTROL.setMajorTickMarkType(((ObjectProperty<TickMarkType>) properties.get(key)).get());
            } else if ("mediumTickMarkType".equals(key)) {
                CONTROL.setMediumTickMarkType(((ObjectProperty<TickMarkType>) properties.get(key)).get());
            } else if ("minorTickMarkType".equals(key)) {
                CONTROL.setMinorTickMarkType(((ObjectProperty<TickMarkType>) properties.get(key)).get());
            } else if ("titleColor".equals(key)) {
                CONTROL.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("subTitleColor".equals(key)) {
                CONTROL.setSubTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("unitColor".equals(key)) {
                CONTROL.setUnitColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("valueColor".equals(key)) {
                CONTROL.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("thresholdColor".equals(key)) {
                CONTROL.setThresholdColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("averageColor".equals(key)) {
                CONTROL.setAverageColor(((ObjectProperty<Color>) properties.get(key)).get());
            } else if ("tickLabelsVisible".equals(key)) {
                CONTROL.setTickLabelsVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("onlyFirstAndLastTickLabelVisible".equals(key)) {
                CONTROL.setOnlyFirstAndLastTickLabelVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("majorTickMarksVisible".equals(key)) {
                CONTROL.setMajorTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("mediumTickMarksVisible".equals(key)) {
                CONTROL.setMediumTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("minorTickMarksVisible".equals(key)) {
                CONTROL.setMinorTickMarksVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("tickMarkRingVisible".equals(key)) {
                CONTROL.setTickMarkRingVisible(((BooleanProperty) properties.get(key)).get());
            } else if ("ledBlinking".equals(key)) {
                CONTROL.setLedBlinking(((BooleanProperty) properties.get(key)).get());
            } else if ("ledOn".equals(key)) {
                CONTROL.setLedOn(((BooleanProperty) properties.get(key)).get());
            } else if ("orientation".equals(key)) {
                CONTROL.setOrientation(((ObjectProperty<Orientation>) properties.get(key)).get());
            } else if("gradientBarEnabled".equals(key)) {
                CONTROL.setGradientBarEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("gradientLookup".equals(key)) {
                CONTROL.setGradientLookup(((ObjectProperty<GradientLookup>) properties.get(key)).get());
            } else if ("customTickLabelsEnabled".equals(key)) {
                CONTROL.setCustomTickLabelsEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("customTickLabelFontSize".equals(key)) {
                CONTROL.setCustomTickLabelFontSize(((DoubleProperty) properties.get(key)).get());
            } else if ("interactive".equals(key)) {
                CONTROL.setInteractive(((BooleanProperty) properties.get(key)).get());
            } else if ("checkSectionsForValue".equals(key)) {
                CONTROL.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
            } else if ("checkAreasForValue".equals(key)) {
                CONTROL.setCheckAreasForValue(((BooleanProperty) properties.get(key)).get());
            } else if ("checkThreshold".equals(key)) {
                CONTROL.setCheckThreshold(((BooleanProperty) properties.get(key)).get());
            } else if ("onValueChanged".equals(key)) {
                CONTROL.currentValueProperty().addListener(((ObjectProperty<InvalidationListener>) properties.get(key)).get());
            } else if ("onButtonPressed".equals(key)) {
                CONTROL.setOnButtonPressed(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("onButtonReleased".equals(key)) {
                CONTROL.setOnButtonReleased(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("onThresholdExceeded".equals(key)) {
                CONTROL.setOnThresholdExceeded(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("onThresholdUnderrun".equals(key)) {
                CONTROL.setOnThresholdUnderrun(((ObjectProperty<EventHandler>) properties.get(key)).get());
            } else if ("buttonTooltipText".equals(key)) {
                CONTROL.setButtonTooltipText(((StringProperty) properties.get(key)).get());
            } else if ("keepAspect".equals(key)) {
                CONTROL.setKeepAspect(((BooleanProperty) properties.get(key)).get());
            } else if ("threshold".equals(key)) {
                CONTROL.setThreshold(((DoubleProperty) properties.get(key)).get());
            } else if ("customFontEnabled".equals(key)) {
                CONTROL.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
            } else if ("customFont".equals(key)) {
                CONTROL.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
            } else if ("alertMessage".equals(key)) {
                CONTROL.setAlertMessage(((StringProperty) properties.get(key)).get());
            } else if ("smoothing".equals(key)) {
                CONTROL.setSmoothing(((BooleanProperty) properties.get(key)).get());
            }
        }

        // Adjust tick mark colors
        if (properties.containsKey("tickMarkColor")) {
            Color tickMarkColor = ((ObjectProperty<Color>) properties.get("tickMarkColor")).get();
            if (!properties.containsKey("majorTickMarkColor")) CONTROL.setMajorTickMarkColor(tickMarkColor);
            if (!properties.containsKey("mediumTickMarkColor")) CONTROL.setMediumTickMarkColor(tickMarkColor);
            if (!properties.containsKey("minorTickMarkColor")) CONTROL.setMinorTickMarkColor(tickMarkColor);
        }

        return CONTROL;
    }
}
