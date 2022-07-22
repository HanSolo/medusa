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

import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.NeedleBehavior;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.NeedleType;
import eu.hansolo.medusa.Gauge.SkinType;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import eu.hansolo.medusa.tools.Helper;
import eu.hansolo.toolboxfx.GradientLookup;
import eu.hansolo.toolboxfx.ScaleDirection;
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

    public final B arcExtend(final double ARC_EXTEND) {
        properties.put("arcExtend", new SimpleDoubleProperty(ARC_EXTEND));
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

    public final B customTickLabelFontSizeEnabled(final boolean ENABLED) {
        properties.put("customTickLabelFontSizeEnabled", new SimpleBooleanProperty(ENABLED));
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
        final Gauge gauge;
        if (properties.containsKey("skinType")) {
            SkinType skinType = ((ObjectProperty<SkinType>) properties.get("skinType")).get();
            gauge = new Gauge(skinType);
            switch(skinType) {
                case AMP:
                    gauge.setKnobPosition(Pos.BOTTOM_CENTER);
                    gauge.setTitleColor(Color.WHITE);
                    gauge.setLedVisible(true);
                    gauge.setBackgroundPaint(Color.WHITE);
                    gauge.setForegroundPaint(Color.BLACK);
                    gauge.setLcdVisible(true);
                    gauge.setShadowsEnabled(true);
                    break;
                case PLAIN_AMP:
                    gauge.setKnobPosition(Pos.BOTTOM_CENTER);
                    gauge.setTitleColor(Color.WHITE);
                    gauge.setLedVisible(true);
                    gauge.setBackgroundPaint(Color.WHITE);
                    gauge.setForegroundPaint(Color.BLACK);
                    gauge.setLcdVisible(true);
                    gauge.setShadowsEnabled(true);
                    break;
                case BULLET_CHART:
                    gauge.setKnobPosition(Pos.CENTER);
                    gauge.setBarColor(Color.BLACK);
                    gauge.setThresholdColor(Color.BLACK);
                    break;
                case DASHBOARD:
                    gauge.setKnobPosition(Pos.BOTTOM_CENTER);
                    gauge.setDecimals(0);
                    gauge.setBarBackgroundColor(Color.LIGHTGRAY);
                    gauge.setBarColor(Color.rgb(93, 190, 205));
                    gauge.setStartFromZero(false);
                    break;
                case FLAT:
                    gauge.setKnobPosition(Pos.CENTER);
                    gauge.setBarColor(Color.CYAN);
                    gauge.setBackgroundPaint(Color.TRANSPARENT);
                    gauge.setTitleColor(Gauge.DARK_COLOR);
                    gauge.setValueColor(Gauge.DARK_COLOR);
                    gauge.setUnitColor(Gauge.DARK_COLOR);
                    gauge.setBorderPaint(Color.rgb(208, 208, 208));
                    gauge.setDecimals(0);
                    gauge.setStartFromZero(true);
                    break;
                case INDICATOR:
                    gauge.setKnobPosition(Pos.BOTTOM_CENTER);
                    gauge.setValueVisible(false);
                    gauge.setGradientBarEnabled(false);
                    gauge.setGradientBarStops(new Stop(0.0, Color.rgb(34, 180, 11)),
                                              new Stop(0.5, Color.rgb(255,146,0)),
                                              new Stop(1.0, Color.rgb(255,0,39)));
                    gauge.setTickLabelsVisible(false);
                    gauge.setNeedleColor(Color.rgb(71, 71, 71));
                    gauge.setBarBackgroundColor(Color.rgb(232, 231, 223));
                    gauge.setBarColor(Color.rgb(255, 0, 39));
                    gauge.setAngleRange(180);
                    break;
                case KPI:
                    gauge.setKnobPosition(Pos.BOTTOM_CENTER);
                    gauge.setDecimals(0);
                    gauge.setForegroundBaseColor(Color.rgb(126, 126, 127));
                    gauge.setBarColor(Color.rgb(168, 204, 254));
                    gauge.setThresholdVisible(true);
                    gauge.setThresholdColor(Color.rgb(45, 86, 184));
                    gauge.setNeedleColor(Color.rgb(74, 74, 74));
                    gauge.setAngleRange(128);
                    break;
                case MODERN:
                    gauge.setKnobPosition(Pos.CENTER);
                    gauge.setDecimals(0);
                    gauge.setValueColor(Color.WHITE);
                    gauge.setTitleColor(Color.WHITE);
                    gauge.setSubTitleColor(Color.WHITE);
                    gauge.setUnitColor(Color.WHITE);
                    gauge.setBarColor(Color.rgb(0, 214, 215));
                    gauge.setNeedleColor(Color.WHITE);
                    gauge.setThresholdColor(Color.rgb(204, 0, 0));
                    gauge.setTickLabelColor(Color.rgb(151, 151, 151));
                    gauge.setTickMarkColor(Color.BLACK);
                    gauge.setTickLabelOrientation(TickLabelOrientation.ORTHOGONAL);
                    break;
                case SIMPLE:
                    gauge.setKnobPosition(Pos.CENTER);
                    gauge.setBorderPaint(Color.WHITE);
                    gauge.setNeedleBorderColor(Color.WHITE);
                    gauge.setBackgroundPaint(Color.DARKGRAY);
                    gauge.setDecimals(0);
                    gauge.setTickLabelColor(Color.WHITE);
                    gauge.setNeedleColor(Color.web("#5a615f"));
                    gauge.setValueColor(Color.WHITE);
                    gauge.setTitleColor(Color.WHITE);
                    gauge.setSubTitleColor(Color.WHITE);
                    gauge.setSectionsVisible(true);
                    break;
                case SLIM:
                    gauge.setKnobPosition(Pos.CENTER);
                    gauge.setDecimals(2);
                    gauge.setStartFromZero(true);
                    gauge.setBarBackgroundColor(Color.rgb(62, 67, 73));
                    gauge.setBarColor(Color.rgb(93, 190, 205));
                    gauge.setTitleColor(Color.rgb(142, 147, 151));
                    gauge.setValueColor(Color.rgb(228, 231, 238));
                    gauge.setUnitColor(Color.rgb(142, 147, 151));
                    break;
                case SPACE_X:
                    gauge.setKnobPosition(Pos.CENTER);
                    gauge.setDecimals(0);
                    gauge.setThresholdColor(Color.rgb(180, 0, 0));
                    gauge.setBarBackgroundColor(Color.rgb(169, 169, 169, 0.25));
                    gauge.setBarColor(Color.rgb(169, 169, 169));
                    gauge.setTitleColor(Color.WHITE);
                    gauge.setValueColor(Color.WHITE);
                    gauge.setUnitColor(Color.WHITE);
                    break;
                case QUARTER:
                    gauge.setKnobPosition(Pos.BOTTOM_RIGHT);
                    gauge.setAngleRange(90);
                    break;
                case HORIZONTAL:
                    gauge.setKnobPosition(Pos.BOTTOM_CENTER);
                    gauge.setAngleRange(180);
                    break;
                case VERTICAL:
                    gauge.setKnobPosition(Pos.CENTER_RIGHT);
                    gauge.setAngleRange(180);
                    break;
                case LCD:
                    gauge.setDecimals(1);
                    gauge.setTickLabelDecimals(1);
                    gauge.setMinMeasuredValueVisible(true);
                    gauge.setMaxMeasuredValueVisible(true);
                    gauge.setOldValueVisible(true);
                    gauge.setBorderPaint(Color.WHITE);
                    gauge.setForegroundPaint(Color.WHITE);
                    break;
                case TINY:
                    gauge.setBorderWidth(24);
                    gauge.setBackgroundPaint(Color.rgb(216, 216, 216));
                    gauge.setBorderPaint(Color.rgb(76, 76, 76));
                    gauge.setBarBackgroundColor(Color.rgb(76, 76, 76, 0.2));
                    gauge.setNeedleColor(Color.rgb(76, 76, 76));
                    gauge.setSectionsVisible(true);
                    gauge.setMajorTickMarksVisible(true);
                    gauge.setMajorTickMarkColor(Color.WHITE);
                    break;
                case BATTERY:
                    gauge.setBarBackgroundColor(Color.BLACK);
                    gauge.setBarColor(Color.BLACK);
                    gauge.setValueColor(Color.WHITE);
                    break;
                case LEVEL:
                    gauge.setValueColor(Color.WHITE);
                    gauge.setBarColor(Color.CYAN);
                    gauge.setUnit("%");
                    break;
                case LINEAR:
                    gauge.setOrientation(Orientation.VERTICAL);
                    gauge.setBarColor(Gauge.DARK_COLOR);
                    gauge.setBarEffectEnabled(true);
                    break;
                case DIGITAL:
                    gauge.setBarColor(Gauge.DARK_COLOR);
                    gauge.setShadowsEnabled(true);
                    break;
                case SIMPLE_DIGITAL:
                    gauge.setBarBackgroundColor(Helper.getTranslucentColorFrom(Gauge.DARK_COLOR, 0.1));
                    gauge.setBarColor(Gauge.DARK_COLOR);
                    break;
                case SECTION:
                    gauge.setBackgroundPaint(Gauge.DARK_COLOR);
                    gauge.setAutoScale(false);
                    gauge.setValueVisible(false);
                    gauge.setKnobColor(Color.rgb(82, 82, 84));
                    gauge.setSectionsVisible(true);
                    gauge.setSectionTextVisible(true);
                    break;
                case BAR:
                    Color barColor = gauge.getBarColor();
                    gauge.setAnimated(true);
                    gauge.setAnimationDuration(1000);
                    gauge.setMinValue(0);
                    gauge.setMaxValue(100);
                    gauge.setGradientBarEnabled(true);
                    gauge.setGradientBarStops(new Stop(0.0, barColor),
                                              new Stop(0.01, barColor),
                                              new Stop(0.75, barColor.deriveColor(-10, 1, 1, 1)),
                                              new Stop(1.0, barColor.deriveColor(-20, 1, 1, 1)));
                    gauge.setBarColor(barColor);
                    gauge.setBarEffectEnabled(true);
                    break;
                case WHITE:
                    gauge.setAnimated(true);
                    gauge.setAnimationDuration(1000);
                    gauge.setAngleRange(360);
                    gauge.setMinValue(0);
                    gauge.setMaxValue(100);
                    gauge.setBarColor(Color.WHITE);
                    gauge.setValueColor(Color.WHITE);
                    gauge.setUnitColor(Color.WHITE);
                    break;
                case CHARGE:
                    gauge.setMinValue(0);
                    gauge.setMaxValue(1.0);
                    gauge.setAnimated(true);
                    break;
                case SIMPLE_SECTION:
                    gauge.setAnimated(true);
                    gauge.setStartAngle(150);
                    gauge.setAngleRange(300);
                    gauge.setStartFromZero(true);
                    gauge.setSectionsVisible(true);
                    gauge.setBarBackgroundColor(Color.rgb(150, 150, 150, 0.25));
                    gauge.setBarColor(Color.rgb(69, 106, 207));
                    gauge.setTitleColor(Color.rgb(90, 90, 90));
                    gauge.setUnitColor(Color.rgb(90, 90, 90));
                    gauge.setValueColor(Color.rgb(90, 90, 90));
                    break;
                case NASA:
                    gauge.setBarBackgroundColor(Color.TRANSPARENT);
                    gauge.setForegroundBaseColor(Color.WHITE);
                    gauge.setStartAngle(108);
                    gauge.setAngleRange(216);
                    gauge.setTickLabelsVisible(false);
                    gauge.setMediumTickMarksVisible(false);
                    gauge.setMajorTickMarksVisible(false);
                    break;
                case GAUGE:
                    gauge.setStartAngle(320);
                    gauge.setAngleRange(280);
                    break;
            }
        } else {
            gauge = new Gauge();
        }

        // Make sure that sections, areas and markers will be added first
        if (properties.keySet().contains("sectionsArray")) {
            gauge.setSections(((ObjectProperty<Section[]>) properties.get("sectionsArray")).get());
        }
        if(properties.keySet().contains("sectionsList")) {
            gauge.setSections(((ObjectProperty<List<Section>>) properties.get("sectionsList")).get());
        }

        if (properties.keySet().contains("areasArray")) {
            gauge.setAreas(((ObjectProperty<Section[]>) properties.get("areasArray")).get());
        }
        if(properties.keySet().contains("areasList")) {
            gauge.setAreas(((ObjectProperty<List<Section>>) properties.get("areasList")).get());
        }

        if (properties.keySet().contains("tickMarkSectionsArray")) {
            gauge.setTickMarkSections(((ObjectProperty<Section[]>) properties.get("tickMarkSectionsArray")).get());
        }
        if(properties.keySet().contains("tickMarkSectionsList")) {
            gauge.setTickMarkSections(((ObjectProperty<List<Section>>) properties.get("tickMarkSectionsList")).get());
        }

        if (properties.keySet().contains("tickLabelSectionsArray")) {
            gauge.setTickLabelSections(((ObjectProperty<Section[]>) properties.get("tickLabelSectionsArray")).get());
        }
        if(properties.keySet().contains("tickLabelSectionsList")) {
            gauge.setTickLabelSections(((ObjectProperty<List<Section>>) properties.get("tickLabelSectionsList")).get());
        }

        if (properties.keySet().contains("markersArray")) {
            gauge.setMarkers(((ObjectProperty<Marker[]>) properties.get("markersArray")).get());
        }
        if (properties.keySet().contains("markersList")) {
            gauge.setMarkers(((ObjectProperty<List<Marker>>) properties.get("markersList")).get());
        }

        if (properties.keySet().contains("gradientBarStopsArray")) {
            gauge.setGradientBarStops(((ObjectProperty<Stop[]>) properties.get("gradientBarStopsArray")).get());
        }
        if (properties.keySet().contains("gradientBarStopsList")) {
            gauge.setGradientBarStops(((ObjectProperty<List<Stop>>) properties.get("gradientBarStopsList")).get());
        }

        if (properties.keySet().contains("customTickLabelsArray")) {
            gauge.setCustomTickLabels(((ObjectProperty<String[]>) properties.get("customTickLabelsArray")).get());
        }
        if (properties.keySet().contains("customTickLabelsList")) {
            gauge.setCustomTickLabels(((ObjectProperty<List<String>>) properties.get("customTickLabelsList")).get());
        }
        if(properties.keySet().contains("foregroundBaseColor")) {
            gauge.setForegroundBaseColor(((ObjectProperty<Color>) properties.get("foregroundBaseColor")).get());
        }
        if (properties.keySet().contains("autoScale")) {
            gauge.setAutoScale(((BooleanProperty) properties.get("autoScale")).get());
        }

        setMinMaxValues(gauge);

        for (String key : properties.keySet()) {
            switch (key) {
                case "prefSize"                         -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    gauge.setPrefSize(dim.getWidth(), dim.getHeight());
                }
                case "minSize"                          -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    gauge.setMinSize(dim.getWidth(), dim.getHeight());
                }
                case "maxSize"                          -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    gauge.setMaxSize(dim.getWidth(), dim.getHeight());
                }
                case "prefWidth"                        -> gauge.setPrefWidth(((DoubleProperty) properties.get(key)).get());
                case "prefHeight"                       -> gauge.setPrefHeight(((DoubleProperty) properties.get(key)).get());
                case "minWidth"                         -> gauge.setMinWidth(((DoubleProperty) properties.get(key)).get());
                case "minHeight"                        -> gauge.setMinHeight(((DoubleProperty) properties.get(key)).get());
                case "maxWidth"                         -> gauge.setMaxWidth(((DoubleProperty) properties.get(key)).get());
                case "maxHeight"                        -> gauge.setMaxHeight(((DoubleProperty) properties.get(key)).get());
                case "scaleX"                           -> gauge.setScaleX(((DoubleProperty) properties.get(key)).get());
                case "scaleY"                           -> gauge.setScaleY(((DoubleProperty) properties.get(key)).get());
                case "layoutX"                          -> gauge.setLayoutX(((DoubleProperty) properties.get(key)).get());
                case "layoutY"                          -> gauge.setLayoutY(((DoubleProperty) properties.get(key)).get());
                case "translateX"                       -> gauge.setTranslateX(((DoubleProperty) properties.get(key)).get());
                case "translateY"                       -> gauge.setTranslateY(((DoubleProperty) properties.get(key)).get());
                case "padding"                          -> gauge.setPadding(((ObjectProperty<Insets>) properties.get(key)).get());
                case "styleClass"                       -> {
                    gauge.getStyleClass().setAll("gauge");
                    gauge.getStyleClass().addAll(((ObjectProperty<String[]>) properties.get(key)).get());
                }
                case "decimals"                         -> gauge.setDecimals(((IntegerProperty) properties.get(key)).get());
                case "tickLabelDecimals"                -> gauge.setTickLabelDecimals(((IntegerProperty) properties.get(key)).get());
                case "title"                            -> gauge.setTitle(((StringProperty) properties.get(key)).get());
                case "subTitle"                         -> gauge.setSubTitle(((StringProperty) properties.get(key)).get());
                case "unit"                             -> gauge.setUnit(((StringProperty) properties.get(key)).get());
                case "averagingEnabled"                 -> gauge.setAveragingEnabled(((BooleanProperty) properties.get(key)).get());
                case "averagingPeriod"                  -> gauge.setAveragingPeriod(((IntegerProperty) properties.get(key)).get());
                case "startFromZero"                    -> gauge.setStartFromZero(((BooleanProperty) properties.get(key)).get());
                case "returnToZero"                     -> gauge.setReturnToZero(((BooleanProperty) properties.get(key)).get());
                case "zeroColor"                        -> gauge.setZeroColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "minMeasuredValueVisible"          -> gauge.setMinMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
                case "maxMeasuredValueVisible"          -> gauge.setMaxMeasuredValueVisible(((BooleanProperty) properties.get(key)).get());
                case "oldValueVisible"                  -> gauge.setOldValueVisible(((BooleanProperty) properties.get(key)).get());
                case "valueVisible"                     -> gauge.setValueVisible(((BooleanProperty) properties.get(key)).get());
                case "backgroundPaint"                  -> gauge.setBackgroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
                case "borderPaint"                      -> gauge.setBorderPaint(((ObjectProperty<Paint>) properties.get(key)).get());
                case "borderWidth"                      -> gauge.setBorderWidth(((DoubleProperty) properties.get(key)).get());
                case "foregroundPaint"                  -> gauge.setForegroundPaint(((ObjectProperty<Paint>) properties.get(key)).get());
                case "knobColor"                        -> gauge.setKnobColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "knobType"                         -> gauge.setKnobType(((ObjectProperty<KnobType>) properties.get(key)).get());
                case "knobPosition"                     -> gauge.setKnobPosition(((ObjectProperty<Pos>) properties.get(key)).get());
                case "knobVisible"                      -> gauge.setKnobVisible(((BooleanProperty) properties.get(key)).get());
                case "animated"                         -> gauge.setAnimated(((BooleanProperty) properties.get(key)).get());
                case "animationDuration"                -> gauge.setAnimationDuration(((LongProperty) properties.get(key)).get());
                case "startAngle"                       -> gauge.setStartAngle(((DoubleProperty) properties.get(key)).get());
                case "angleRange"                       -> gauge.setAngleRange(((DoubleProperty) properties.get(key)).get());
                case "arcExtend"                        -> gauge.setArcExtend(((DoubleProperty) properties.get(key)).get());
                case "needleType"                       -> gauge.setNeedleType(((ObjectProperty<NeedleType>) properties.get(key)).get());
                case "needleShape"                      -> gauge.setNeedleShape(((ObjectProperty<NeedleShape>) properties.get(key)).get());
                case "needleSize"                       -> gauge.setNeedleSize(((ObjectProperty<NeedleSize>) properties.get(key)).get());
                case "needleBehavior"                   -> gauge.setNeedleBehavior(((ObjectProperty<NeedleBehavior>) properties.get(key)).get());
                case "needleColor"                      -> gauge.setNeedleColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "needleBorderColor"                -> gauge.setNeedleBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "barColor"                         -> gauge.setBarColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "barBorderColor"                   -> gauge.setBarBorderColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "barBackgroundColor"               -> gauge.setBarBackgroundColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "tickLabelOrientation"             -> gauge.setTickLabelOrientation(((ObjectProperty<TickLabelOrientation>) properties.get(key)).get());
                case "tickLabelLocation"                -> gauge.setTickLabelLocation(((ObjectProperty<TickLabelLocation>) properties.get(key)).get());
                case "locale"                           -> gauge.setLocale(((ObjectProperty<Locale>) properties.get(key)).get());
                case "majorTickSpace"                   -> gauge.setMajorTickSpace(((DoubleProperty) properties.get(key)).get());
                case "minorTickSpace"                   -> gauge.setMinorTickSpace(((DoubleProperty) properties.get(key)).get());
                case "shadowsEnabled"                   -> gauge.setShadowsEnabled(((BooleanProperty) properties.get(key)).get());
                case "barEffectEnabled"                 -> gauge.setBarEffectEnabled(((BooleanProperty) properties.get(key)).get());
                case "scaleDirection"                   -> gauge.setScaleDirection(((ObjectProperty<ScaleDirection>) properties.get(key)).get());
                case "tickLabelColor"                   -> gauge.setTickLabelColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "tickMarkColor"                    -> gauge.setTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "majorTickMarkColor"               -> gauge.setMajorTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "majorTickMarkLengthFactor"        -> gauge.setMajorTickMarkLengthFactor(((DoubleProperty) properties.get(key)).get());
                case "majorTickMarkWidthFactor"         -> gauge.setMajorTickMarkWidthFactor(((DoubleProperty) properties.get(key)).get());
                case "mediumTickMarkColor"              -> gauge.setMediumTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "mediumTickMarkLengthFactor"       -> gauge.setMediumTickMarkLengthFactor(((DoubleProperty) properties.get(key)).get());
                case "mediumTickMarkWidthFactor"        -> gauge.setMediumTickMarkWidthFactor(((DoubleProperty) properties.get(key)).get());
                case "minorTickMarkColor"               -> gauge.setMinorTickMarkColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "minorTickMarkLengthFactor"        -> gauge.setMinorTickMarkLengthFactor(((DoubleProperty) properties.get(key)).get());
                case "minorTickMarkWidthFactor"         -> gauge.setMinorTickMarkWidthFactor(((DoubleProperty) properties.get(key)).get());
                case "style"                            -> gauge.setStyle(((StringProperty) properties.get(key)).get());
                case "ledColor"                         -> gauge.setLedColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "ledType"                          -> gauge.setLedType(((ObjectProperty<LedType>) properties.get(key)).get());
                case "ledVisible"                       -> gauge.setLedVisible(((BooleanProperty) properties.get(key)).get());
                case "lcdVisible"                       -> gauge.setLcdVisible(((BooleanProperty) properties.get(key)).get());
                case "lcdCrystalEnabled"                -> gauge.setLcdCrystalEnabled(((BooleanProperty) properties.get(key)).get());
                case "lcdDesign"                        -> gauge.setLcdDesign(((ObjectProperty<LcdDesign>) properties.get(key)).get());
                case "lcdFont"                          -> gauge.setLcdFont(((ObjectProperty<LcdFont>) properties.get(key)).get());
                case "innerShadowEnabled"               -> gauge.setInnerShadowEnabled(((BooleanProperty) properties.get(key)).get());
                case "thresholdVisible"                 -> gauge.setThresholdVisible(((BooleanProperty) properties.get(key)).get());
                case "averageVisible"                   -> gauge.setAverageVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionsVisible"                  -> gauge.setSectionsVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionsAlwaysVisible"            -> gauge.setSectionsAlwaysVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionTextVisible"               -> gauge.setSectionTextVisible(((BooleanProperty) properties.get(key)).get());
                case "sectionIconsVisible"              -> gauge.setSectionIconsVisible(((BooleanProperty) properties.get(key)).get());
                case "highlightSections"                -> gauge.setHighlightSections(((BooleanProperty) properties.get(key)).get());
                case "areasVisible"                     -> gauge.setAreasVisible(((BooleanProperty) properties.get(key)).get());
                case "areaTextVisible"                  -> gauge.setAreaTextVisible(((BooleanProperty) properties.get(key)).get());
                case "areaIconsVisible"                 -> gauge.setAreaIconsVisible(((BooleanProperty) properties.get(key)).get());
                case "highlightAreas"                   -> gauge.setHighlightAreas(((BooleanProperty) properties.get(key)).get());
                case "tickMarkSectionsVisible"          -> gauge.setTickMarkSectionsVisible(((BooleanProperty) properties.get(key)).get());
                case "tickLabelSectionsVisible"         -> gauge.setTickLabelSectionsVisible(((BooleanProperty) properties.get(key)).get());
                case "markersVisible"                   -> gauge.setMarkersVisible(((BooleanProperty) properties.get(key)).get());
                case "majorTickMarkType"                -> gauge.setMajorTickMarkType(((ObjectProperty<TickMarkType>) properties.get(key)).get());
                case "mediumTickMarkType"               -> gauge.setMediumTickMarkType(((ObjectProperty<TickMarkType>) properties.get(key)).get());
                case "minorTickMarkType"                -> gauge.setMinorTickMarkType(((ObjectProperty<TickMarkType>) properties.get(key)).get());
                case "titleColor"                       -> gauge.setTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "subTitleColor"                    -> gauge.setSubTitleColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "unitColor"                        -> gauge.setUnitColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "valueColor"                       -> gauge.setValueColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "thresholdColor"                   -> gauge.setThresholdColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "averageColor"                     -> gauge.setAverageColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "tickLabelsVisible"                -> gauge.setTickLabelsVisible(((BooleanProperty) properties.get(key)).get());
                case "onlyFirstAndLastTickLabelVisible" -> gauge.setOnlyFirstAndLastTickLabelVisible(((BooleanProperty) properties.get(key)).get());
                case "majorTickMarksVisible"            -> gauge.setMajorTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "mediumTickMarksVisible"           -> gauge.setMediumTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "minorTickMarksVisible"            -> gauge.setMinorTickMarksVisible(((BooleanProperty) properties.get(key)).get());
                case "tickMarkRingVisible"              -> gauge.setTickMarkRingVisible(((BooleanProperty) properties.get(key)).get());
                case "ledBlinking"                      -> gauge.setLedBlinking(((BooleanProperty) properties.get(key)).get());
                case "ledOn"                            -> gauge.setLedOn(((BooleanProperty) properties.get(key)).get());
                case "orientation"                      -> gauge.setOrientation(((ObjectProperty<Orientation>) properties.get(key)).get());
                case "gradientBarEnabled"               -> gauge.setGradientBarEnabled(((BooleanProperty) properties.get(key)).get());
                case "gradientLookup"                   -> gauge.setGradientLookup(((ObjectProperty<GradientLookup>) properties.get(key)).get());
                case "customTickLabelsEnabled"          -> gauge.setCustomTickLabelsEnabled(((BooleanProperty) properties.get(key)).get());
                case "customTickLabelFontSize"          -> gauge.setCustomTickLabelFontSize(((DoubleProperty) properties.get(key)).get());
                case "customTickLabelFontSizeEnabled"   -> gauge.setCustomTickLabelFontSizeEnabled(((BooleanProperty) properties.get(key)).get());
                case "interactive"                      -> gauge.setInteractive(((BooleanProperty) properties.get(key)).get());
                case "checkSectionsForValue"            -> gauge.setCheckSectionsForValue(((BooleanProperty) properties.get(key)).get());
                case "checkAreasForValue"               -> gauge.setCheckAreasForValue(((BooleanProperty) properties.get(key)).get());
                case "checkThreshold"                   -> gauge.setCheckThreshold(((BooleanProperty) properties.get(key)).get());
                case "onValueChanged"                   -> gauge.currentValueProperty().addListener(((ObjectProperty<InvalidationListener>) properties.get(key)).get());
                case "onButtonPressed"                  -> gauge.setOnButtonPressed(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onButtonReleased"                 -> gauge.setOnButtonReleased(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onThresholdExceeded"              -> gauge.setOnThresholdExceeded(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onThresholdUnderrun"              -> gauge.setOnThresholdUnderrun(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "buttonTooltipText"                -> gauge.setButtonTooltipText(((StringProperty) properties.get(key)).get());
                case "keepAspect"                       -> gauge.setKeepAspect(((BooleanProperty) properties.get(key)).get());
                case "threshold"                        -> gauge.setThreshold(((DoubleProperty) properties.get(key)).get());
                case "customFontEnabled"                -> gauge.setCustomFontEnabled(((BooleanProperty) properties.get(key)).get());
                case "customFont"                       -> gauge.setCustomFont(((ObjectProperty<Font>) properties.get(key)).get());
                case "alertMessage"                     -> gauge.setAlertMessage(((StringProperty) properties.get(key)).get());
                case "smoothing"                        -> gauge.setSmoothing(((BooleanProperty) properties.get(key)).get());
                case "value"                            -> gauge.setValue(((DoubleProperty) properties.get(key)).get());
            }
        }

        // Adjust tick mark colors
        if (properties.containsKey("tickMarkColor")) {
            Color tickMarkColor = ((ObjectProperty<Color>) properties.get("tickMarkColor")).get();
            if (!properties.containsKey("majorTickMarkColor")) gauge.setMajorTickMarkColor(tickMarkColor);
            if (!properties.containsKey("mediumTickMarkColor")) gauge.setMediumTickMarkColor(tickMarkColor);
            if (!properties.containsKey("minorTickMarkColor")) gauge.setMinorTickMarkColor(tickMarkColor);
        }

        return gauge;
    }

    private void setMinMaxValues(final Gauge CONTROL) {
        if (properties.keySet().contains("minValue")) { CONTROL.setMinValue(((DoubleProperty) properties.get("minValue")).get()); }
        if (properties.keySet().contains("maxValue")) { CONTROL.setMaxValue(((DoubleProperty) properties.get("maxValue")).get()); }
    }
}
