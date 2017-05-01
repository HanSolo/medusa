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

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.NeedleType;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Needle;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.medusa.tools.AngleConicalGradient;
import eu.hansolo.medusa.tools.Helper;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 18.01.16.
 */
public class QuarterSkin extends GaugeSkinBase {
    private static final double        ANGLE_RANGE = 90;
    private Map<Marker, Shape>         markerMap   = new ConcurrentHashMap<>();
    private double                     oldValue;
    private double                     size;
    private double                     centerX;
    private double                     centerY;
    private Pane                       pane;
    private InnerShadow                backgroundInnerShadow;
    private Canvas                     sectionsAndAreasCanvas;
    private GraphicsContext            sectionsAndAreasCtx;
    private Canvas                     tickMarkCanvas;
    private GraphicsContext            tickMarkCtx;
    private double                     ledSize;
    private InnerShadow                ledOnShadow;
    private InnerShadow                ledOffShadow;
    private Paint                      ledFramePaint;
    private Paint                      ledOnPaint;
    private Paint                      ledOffPaint;
    private Paint                      ledHighlightPaint;
    private Canvas                     ledCanvas;
    private GraphicsContext            ledCtx;
    private Pane                       markerPane;
    private Path                       threshold;
    private Path                       average;
    private Rectangle                  lcd;
    private Path                       needle;
    private Rotate                     needleRotate;
    private Paint                      needlePaint;
    private Canvas                     knobCanvas;
    private GraphicsContext            knobCtx;
    private Group                      shadowGroup;
    private DropShadow                 dropShadow;
    private Text                       titleText;
    private Text                       subTitleText;
    private Text                       unitText;
    private Text                       valueText;
    private double                     startAngle;
    private double                     angleStep;
    private EventHandler<MouseEvent>   mouseHandler;
    private Tooltip                    buttonTooltip;
    private Tooltip                    thresholdTooltip;
    private String                     formatString;
    private Locale                     locale;
    private double                     minValue;
    private double                     maxValue;
    private List<Section>              sections;
    private boolean                    highlightSections;
    private boolean                    sectionsVisible;
    private List<Section>              areas;
    private boolean                    highlightAreas;
    private boolean                    areasVisible;
    private TickLabelLocation          tickLabelLocation;
    private ScaleDirection             scaleDirection;
    private ListChangeListener<Marker> markerListener;
    private InvalidationListener       currentValueListener;


    // ******************** Constructors **************************************
    public QuarterSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        startAngle        = getStartAngle();
        angleStep         = ANGLE_RANGE / gauge.getRange();
        oldValue          = gauge.getValue();
        minValue          = gauge.getMinValue();
        maxValue          = gauge.getMaxValue();
        formatString      = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale            = gauge.getLocale();
        sections          = gauge.getSections();
        highlightSections = gauge.isHighlightSections();
        sectionsVisible   = gauge.getSectionsVisible();
        areas             = gauge.getAreas();
        highlightAreas    = gauge.isHighlightAreas();
        areasVisible      = gauge.getAreasVisible();
        tickLabelLocation = gauge.getTickLabelLocation();
        scaleDirection    = gauge.getScaleDirection();
        mouseHandler      = e -> handleMouseEvent(e);
        markerListener    = c -> {
            updateMarkers();
            redraw();
        };
        currentValueListener = o -> rotateNeedle(gauge.getCurrentValue());
        updateMarkers();

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // Set initial size
        if (Double.compare(gauge.getPrefWidth(), 0.0) <= 0 || Double.compare(gauge.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(gauge.getWidth(), 0.0) <= 0 || Double.compare(gauge.getHeight(), 0.0) <= 0) {
            if (gauge.getPrefWidth() > 0 && gauge.getPrefHeight() > 0) {
                gauge.setPrefSize(gauge.getPrefWidth(), gauge.getPrefHeight());
            } else {
                gauge.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        backgroundInnerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(10, 10, 10, 0.45), 8, 0.0, 8.0, 0.0);

        sectionsAndAreasCanvas = new Canvas();
        sectionsAndAreasCtx    = sectionsAndAreasCanvas.getGraphicsContext2D();

        tickMarkCanvas = new Canvas();
        tickMarkCtx    = tickMarkCanvas.getGraphicsContext2D();

        ledCanvas = new Canvas();
        ledCtx    = ledCanvas.getGraphicsContext2D();
        Helper.enableNode(ledCanvas, gauge.isLedVisible());

        thresholdTooltip = new Tooltip("Threshold\n(" + String.format(locale, formatString, gauge.getThreshold()) + ")");
        thresholdTooltip.setTextAlignment(TextAlignment.CENTER);

        threshold = new Path();
        Helper.enableNode(threshold, gauge.isThresholdVisible());
        Tooltip.install(threshold, thresholdTooltip);

        average = new Path();
        Helper.enableNode(average, gauge.isAverageVisible());

        markerPane = new Pane();

        lcd = new Rectangle(0.3 * PREFERRED_WIDTH, 0.014 * PREFERRED_HEIGHT);
        lcd.setArcWidth(0.0125 * PREFERRED_HEIGHT);
        lcd.setArcHeight(0.0125 * PREFERRED_HEIGHT);
        lcd.relocate((PREFERRED_WIDTH - lcd.getWidth()) * 0.5, 0.44 * PREFERRED_HEIGHT);
        Helper.enableNode(lcd, gauge.isLcdVisible() && gauge.isValueVisible());

        needleRotate = new Rotate(180 - startAngle);
        needleRotate.setAngle(needleRotate.getAngle() + (gauge.getValue() - oldValue - minValue) * angleStep);
        needle = new Path();
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setStrokeType(StrokeType.INSIDE);
        needle.setStroke(Color.TRANSPARENT);

        buttonTooltip    = new Tooltip();
        buttonTooltip.setTextAlignment(TextAlignment.CENTER);

        knobCanvas = new Canvas();
        knobCtx = knobCanvas.getGraphicsContext2D();
        knobCanvas.setPickOnBounds(false);
        Helper.enableNode(knobCanvas, gauge.isKnobVisible());

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle, knobCanvas);
        shadowGroup.setEffect(gauge.isShadowsEnabled() ? dropShadow : null);

        titleText = new Text(gauge.getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setMouseTransparent(true);
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        subTitleText = new Text(gauge.getSubTitle());
        subTitleText.setTextOrigin(VPos.CENTER);
        subTitleText.setMouseTransparent(true);
        Helper.enableNode(subTitleText, !gauge.getSubTitle().isEmpty());

        unitText = new Text(gauge.getUnit());
        unitText.setMouseTransparent(true);
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setMouseTransparent(true);
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()));
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setMouseTransparent(true);
        Helper.enableNode(valueText, gauge.isValueVisible());

        // Set initial value
        double targetAngle = 180 - startAngle + (gauge.getCurrentValue() - minValue) * angleStep;
        targetAngle        = Helper.clamp(180 - startAngle, 180 - startAngle + ANGLE_RANGE, targetAngle);
        needleRotate.setAngle(targetAngle);

        // Add all nodes
        pane = new Pane(sectionsAndAreasCanvas,
                        tickMarkCanvas,
                        markerPane,
                        ledCanvas,
                        lcd,
                        titleText,
                        subTitleText,
                        unitText,
                        valueText,
                        shadowGroup);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.getMarkers().addListener(markerListener);
        gauge.currentValueProperty().addListener(currentValueListener);

        handleEvents("INTERACTIVITY");
        handleEvents("VISIBILITY");
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("FINISHED".equals(EVENT_TYPE)) {
            double currentValue = gauge.getCurrentValue();
            // eu.hansolo.medusa.Check sections for value and fire section events
            if (gauge.getCheckSectionsForValue()) {
                int listSize = sections.size();
                for (int i = 0 ; i < listSize ; i++) { sections.get(i).checkForValue(currentValue); }
            }

            // eu.hansolo.medusa.Check areas for value and fire section events
            if (gauge.getCheckAreasForValue()) {
                int listSize = areas.size();
                for (int i = 0 ; i < listSize ; i++) { areas.get(i).checkForValue(currentValue); }
            }

            // Highlight Sections and/or Areas if enabled
            if (highlightSections | highlightAreas) {
                sectionsAndAreasCtx.clearRect(0, 0, size * 1.9, size * 1.9);
                drawAreasAndSections(sectionsAndAreasCtx);
            }
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(ledCanvas, gauge.isLedVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(subTitleText, !gauge.getSubTitle().isEmpty());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(lcd, gauge.isLcdVisible() && gauge.isValueVisible());
            Helper.enableNode(knobCanvas, gauge.isKnobVisible());
            Helper.enableNode(threshold, gauge.isThresholdVisible());
            Helper.enableNode(average, gauge.isAverageVisible());
            boolean markersVisible = gauge.getMarkersVisible();
            for (Shape shape : markerMap.values()) { Helper.enableNode(shape, markersVisible); }
            resize();
            redraw();
        } else if ("LED".equals(EVENT_TYPE)) {
            if (gauge.isLedVisible()) { drawLed(); }
        } else if ("LCD".equals(EVENT_TYPE)) {
            if (gauge.isLcdVisible()) redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            startAngle = getStartAngle();
            minValue  = gauge.getMinValue();
            maxValue  = gauge.getMaxValue();
            angleStep = ANGLE_RANGE / gauge.getRange();
            if (gauge.getValue() < minValue) { oldValue = minValue; }
            if (gauge.getValue() > maxValue) { oldValue = maxValue; }
            resize();
            redraw();
            rotateNeedle(gauge.getCurrentValue());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = gauge.getSections();
            highlightSections = gauge.isHighlightSections();
            sectionsVisible   = gauge.getSectionsVisible();
            areas             = gauge.getAreas();
            highlightAreas    = gauge.isHighlightAreas();
            areasVisible      = gauge.getAreasVisible();
            resize();
            redraw();
        } else if ("INTERACTIVITY".equals(EVENT_TYPE)) {
            if (gauge.isInteractive()) {
                knobCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);
                knobCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseHandler);
                if (!gauge.getButtonTooltipText().isEmpty()) {
                    buttonTooltip.setText(gauge.getButtonTooltipText());
                    Tooltip.install(knobCanvas, buttonTooltip);
                }
            } else {
                knobCanvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);
                knobCanvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseHandler);
                Tooltip.uninstall(knobCanvas, buttonTooltip);
            }
        }
    }

    public void handleMouseEvent(final MouseEvent EVENT) {
        if (gauge.isDisabled()) return;
        final EventType TYPE = EVENT.getEventType();
        if (MouseEvent.MOUSE_PRESSED == TYPE) {
            gauge.fireEvent(gauge.BTN_PRESSED_EVENT);
            drawKnob(true);
        } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
            gauge.fireEvent(gauge.BTN_RELEASED_EVENT);
            drawKnob(false);
        }
    }

    @Override public void dispose() {
        gauge.getMarkers().removeListener(markerListener);
        gauge.currentValueProperty().removeListener(currentValueListener);
        if (gauge.isInteractive()) {
            knobCanvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);
            knobCanvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseHandler);
        }
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private double getStartAngle() {
        ScaleDirection scaleDirection = gauge.getScaleDirection();
        Pos            knobPosition   = gauge.getKnobPosition();
        switch(knobPosition) {
            case BOTTOM_LEFT : return ScaleDirection.CLOCKWISE == scaleDirection ? 180 : 90;
            case TOP_LEFT    : return ScaleDirection.CLOCKWISE == scaleDirection ?  90 : 0;
            case TOP_RIGHT   : return ScaleDirection.CLOCKWISE == scaleDirection ?   0 : 270;
            case BOTTOM_RIGHT:
            default          : return ScaleDirection.CLOCKWISE == scaleDirection ? 270 : 180;
        }
    }

    private void rotateNeedle(final double VALUE) {
        double startOffsetAngle  = 180 - startAngle;
        double targetAngle;
        if (ScaleDirection.CLOCKWISE == gauge.getScaleDirection()) {
            targetAngle = startOffsetAngle + (VALUE - minValue) * angleStep;
            targetAngle = Helper.clamp(startOffsetAngle, startOffsetAngle + ANGLE_RANGE, targetAngle);
        } else {
            targetAngle = startOffsetAngle - (VALUE - minValue) * angleStep;
            targetAngle = Helper.clamp(startOffsetAngle - ANGLE_RANGE, startOffsetAngle, targetAngle);
        }
        needleRotate.setAngle(targetAngle);
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        resizeValueText();
        if (gauge.isAverageVisible()) drawAverage();
    }

    private void drawTickMarks() {
        tickMarkCtx.setLineCap(StrokeLineCap.BUTT);
        double               sinValue;
        double               cosValue;
        double               scaledSize            = size * 1.95;
        int                  tickLabelDecimals     = gauge.getTickLabelDecimals();
        String               tickLabelFormatString = "%." + tickLabelDecimals + "f";
        double               minorTickSpace        = gauge.getMinorTickSpace();
        double               tmpAngleStep          = angleStep * minorTickSpace;
        TickLabelOrientation tickLabelOrientation  = gauge.getTickLabelOrientation();
        TickLabelLocation    tickLabelLocation     = gauge.getTickLabelLocation();
        BigDecimal           minorTickSpaceBD      = BigDecimal.valueOf(minorTickSpace);
        BigDecimal           majorTickSpaceBD      = BigDecimal.valueOf(gauge.getMajorTickSpace());
        BigDecimal           mediumCheck2          = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal           mediumCheck5          = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal           counterBD             = BigDecimal.valueOf(minValue);
        double               counter               = minValue;

        List<Section> tickMarkSections             = gauge.getTickMarkSections();
        List<Section> tickLabelSections            = gauge.getTickLabelSections();
        Color         tickMarkColor                = gauge.getTickMarkColor();
        Color         majorTickMarkColor           = gauge.getMajorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : gauge.getMajorTickMarkColor();
        Color         mediumTickMarkColor          = gauge.getMediumTickMarkColor().equals(tickMarkColor) ? tickMarkColor : gauge.getMediumTickMarkColor();
        Color         minorTickMarkColor           = gauge.getMinorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : gauge.getMinorTickMarkColor();
        Color         tickLabelColor               = gauge.getTickLabelColor();
        Color         zeroColor                    = gauge.getZeroColor();
        boolean       isNotZero                    = true;
        TickMarkType  majorTickMarkType            = gauge.getMajorTickMarkType();
        TickMarkType  mediumTickMarkType           = gauge.getMediumTickMarkType();
        TickMarkType  minorTickMarkType            = gauge.getMinorTickMarkType();
        boolean       tickMarkSectionsVisible      = gauge.getTickMarkSectionsVisible();
        boolean       tickLabelSectionsVisible     = gauge.getTickLabelSectionsVisible();
        boolean       majorTickMarksVisible        = gauge.getMajorTickMarksVisible();
        boolean       mediumTickMarksVisible       = gauge.getMediumTickMarksVisible();
        boolean       minorTickMarksVisible        = gauge.getMinorTickMarksVisible();
        boolean       tickLabelsVisible            = gauge.getTickLabelsVisible();
        boolean       onlyFirstAndLastLabelVisible = gauge.isOnlyFirstAndLastTickLabelVisible();
        boolean       customTickLabelsEnabled      = gauge.getCustomTickLabelsEnabled();
        List<String>  customTickLabels             = customTickLabelsEnabled ? gauge.getCustomTickLabels() : null;
        double        textDisplacementFactor       = majorTickMarkType == TickMarkType.DOT ? (TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.95 : 1.05) : 1.0;
        double        majorDotSize;
        double        majorHalfDotSize;
        double        mediumDotSize;
        double        mediumHalfDotSize;
        double        minorDotSize;
        double        minorHalfDotSize;

        double orthTextFactor;
        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
            orthTextFactor    = 0.45 * textDisplacementFactor;// TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.45 * textDisplacementFactor : 0.45 * textDisplacementFactor;
            majorDotSize      = 0.02 * size;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01375 * size;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0075 * size;
            minorHalfDotSize  = minorDotSize * 0.5;
        } else {
            orthTextFactor    = TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.40 * textDisplacementFactor : 0.39 * textDisplacementFactor;
            majorDotSize      = 0.025 * size;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01875 * size;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0125 * size;
            minorHalfDotSize  = minorDotSize * 0.5;
        };

        boolean fullRange                  = (minValue < 0 && maxValue > 0);
        double  tickLabelFontSize          = tickLabelDecimals == 0 ? 0.074 * size : 0.071 * size;
        double  tickMarkFontSize           = tickLabelDecimals == 0 ? 0.067 * size: 0.064 * size;
        double  tickLabelOrientationFactor = TickLabelOrientation.HORIZONTAL == tickLabelOrientation ? 0.9 : 1.0;

        Font tickLabelFont     = Fonts.robotoCondensedRegular(tickLabelFontSize * tickLabelOrientationFactor);
        Font tickMarkFont      = Fonts.robotoCondensedRegular(tickMarkFontSize * tickLabelOrientationFactor);
        Font tickLabelZeroFont = fullRange ? Fonts.robotoCondensedBold(tickLabelFontSize * tickLabelOrientationFactor) : tickLabelFont;
        Font tickMarkZeroFont  = fullRange ? Fonts.robotoCondensedBold(tickMarkFontSize * tickLabelOrientationFactor) : tickMarkFont;

        // Variables needed for tickmarks
        double innerPointX;
        double innerPointY;
        double innerMediumPointX;
        double innerMediumPointY;
        double innerMinorPointX;
        double innerMinorPointY;
        double outerPointX;
        double outerPointY;
        double outerMediumPointX;
        double outerMediumPointY;
        double outerMinorPointX;
        double outerMinorPointY;
        double textPointX;
        double textPointY;
        double dotCenterX;
        double dotCenterY;
        double dotMediumCenterX;
        double dotMediumCenterY;
        double dotMinorCenterX;
        double dotMinorCenterY;
        double tickLabelTickMarkX;
        double tickLabelTickMarkY;

        double trapezoidMajorInnerAngle1;
        double trapezoidMajorInnerAngle2;
        double trapezoidMajorOuterAngle1;
        double trapezoidMajorOuterAngle2;
        double trapezoidMajorInnerPoint1X;
        double trapezoidMajorInnerPoint1Y;
        double trapezoidMajorInnerPoint2X;
        double trapezoidMajorInnerPoint2Y;
        double trapezoidMajorOuterPoint1X;
        double trapezoidMajorOuterPoint1Y;
        double trapezoidMajorOuterPoint2X;
        double trapezoidMajorOuterPoint2Y;

        double trapezoidMediumInnerAngle1;
        double trapezoidMediumInnerAngle2;
        double trapezoidMediumOuterAngle1;
        double trapezoidMediumOuterAngle2;
        double trapezoidMediumInnerPoint1X;
        double trapezoidMediumInnerPoint1Y;
        double trapezoidMediumInnerPoint2X;
        double trapezoidMediumInnerPoint2Y;
        double trapezoidMediumOuterPoint1X;
        double trapezoidMediumOuterPoint1Y;
        double trapezoidMediumOuterPoint2X;
        double trapezoidMediumOuterPoint2Y;

        double trapezoidMinorInnerAngle1;
        double trapezoidMinorInnerAngle2;
        double trapezoidMinorOuterAngle1;
        double trapezoidMinorOuterAngle2;
        double trapezoidMinorInnerPoint1X;
        double trapezoidMinorInnerPoint1Y;
        double trapezoidMinorInnerPoint2X;
        double trapezoidMinorInnerPoint2Y;
        double trapezoidMinorOuterPoint1X;
        double trapezoidMinorOuterPoint1Y;
        double trapezoidMinorOuterPoint2X;
        double trapezoidMinorOuterPoint2Y;

        ScaleDirection scaleDirection = gauge.getScaleDirection();
        
        // Draw tickmark ring
        if (gauge.isTickMarkRingVisible()) {
            Pos    knobPosition = gauge.getKnobPosition();
            double xy           = TickLabelLocation.INSIDE == tickLabelLocation ? scaledSize * 0.0125 : scaledSize * 0.1285;
            double wh           = TickLabelLocation.INSIDE == tickLabelLocation ? scaledSize * 0.948 : scaledSize * 0.716;
            double offset       = -90 + startAngle;
            tickMarkCtx.setLineWidth(scaledSize * 0.004);
            tickMarkCtx.setLineCap(StrokeLineCap.SQUARE);
            tickMarkCtx.save();
            tickMarkCtx.setStroke(tickMarkColor);
            switch(knobPosition) {
                case BOTTOM_LEFT: tickMarkCtx.strokeArc((-scaledSize * 0.46) + xy, xy, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
                case TOP_LEFT   : tickMarkCtx.strokeArc((-scaledSize * 0.46) + xy, (-scaledSize * 0.46) + xy, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
                case TOP_RIGHT  : tickMarkCtx.strokeArc(xy, (-scaledSize * 0.46) + xy, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
                default         : tickMarkCtx.strokeArc(xy, xy, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
            }
            tickMarkCtx.restore();
            if (tickMarkSections.size() > 0) {
                tickMarkCtx.setLineCap(StrokeLineCap.BUTT);
                int    listSize = tickMarkSections.size();
                double sectionStartAngle;
                for (int i = 0; i < listSize; i++) {
                    Section section = tickMarkSections.get(i);
                    if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                        sectionStartAngle = 0;
                    } else {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStart() - minValue) * angleStep : -(section.getStart() - minValue) * angleStep;
                    }
                    double sectionAngleExtend;
                    if (Double.compare(section.getStop(), maxValue) > 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - section.getStart()) * angleStep : -(maxValue - section.getStart()) * angleStep;
                    } else {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStop() - section.getStart()) * angleStep : -(section.getStop() - section.getStart()) * angleStep;
                    }
                    tickMarkCtx.save();
                    tickMarkCtx.setStroke(section.getColor());
                    switch(knobPosition) {
                        case BOTTOM_LEFT: tickMarkCtx.strokeArc((-scaledSize * 0.46) + xy, xy, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                        case TOP_LEFT   : tickMarkCtx.strokeArc((-scaledSize * 0.46) + xy, (-scaledSize * 0.46) + xy, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                        case TOP_RIGHT  : tickMarkCtx.strokeArc(xy, (-scaledSize * 0.46) + xy, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                        default         : tickMarkCtx.strokeArc(xy, xy, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                    }
                    tickMarkCtx.restore();
                }
            }
        }

        // Main loop
        BigDecimal tmpStepBD              = new BigDecimal(tmpAngleStep);
        tmpStepBD                         = tmpStepBD.setScale(3, BigDecimal.ROUND_HALF_UP);
        double     tmpStep                = tmpStepBD.doubleValue();
        double     angle                  = 0;
        int        customTickLabelCounter = 0;
        for (double i = 0 ; Double.compare(-ANGLE_RANGE - tmpStep, i) <= 0 ; i -= tmpStep) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            switch(tickLabelLocation) {
                case OUTSIDE:
                    innerPointX                = centerX + scaledSize * 0.3585 * sinValue;
                    innerPointY                = centerY + scaledSize * 0.3585 * cosValue;
                    innerMediumPointX          = centerX + scaledSize * 0.3585 * sinValue;
                    innerMediumPointY          = centerY + scaledSize * 0.3585 * cosValue;
                    innerMinorPointX           = centerX + scaledSize * 0.3585 * sinValue;
                    innerMinorPointY           = centerY + scaledSize * 0.3585 * cosValue;
                    outerPointX                = centerX + scaledSize * 0.4105 * sinValue;
                    outerPointY                = centerY + scaledSize * 0.4105 * cosValue;
                    outerMediumPointX          = centerX + scaledSize * 0.4045 * sinValue;
                    outerMediumPointY          = centerY + scaledSize * 0.4045 * cosValue;
                    outerMinorPointX           = centerX + scaledSize * 0.3975 * sinValue;
                    outerMinorPointY           = centerY + scaledSize * 0.3975 * cosValue;
                    textPointX                 = centerX + scaledSize * orthTextFactor * sinValue;
                    textPointY                 = centerY + scaledSize * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + scaledSize * 0.3685 * sinValue;
                    dotCenterY                 = centerY + scaledSize * 0.3685 * cosValue;
                    dotMediumCenterX           = centerX + scaledSize * 0.365375 * sinValue;
                    dotMediumCenterY           = centerY + scaledSize * 0.365375 * cosValue;
                    dotMinorCenterX            = centerX + scaledSize * 0.36225 * sinValue;
                    dotMinorCenterY            = centerY + scaledSize * 0.36225 * cosValue;
                    tickLabelTickMarkX         = centerX + scaledSize * 0.3805 * sinValue;
                    tickLabelTickMarkY         = centerY + scaledSize * 0.3805 * cosValue;

                    trapezoidMajorInnerAngle1   = Math.toRadians(angle - 1.2 + startAngle);
                    trapezoidMajorInnerAngle2   = Math.toRadians(angle + 1.2 + startAngle);
                    trapezoidMajorOuterAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    trapezoidMajorOuterAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    trapezoidMajorInnerPoint1X  = centerX + scaledSize * 0.3585 * Math.sin(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint1Y  = centerY + scaledSize * 0.3585 * Math.cos(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint2X  = centerX + scaledSize * 0.3585 * Math.sin(trapezoidMajorInnerAngle2);
                    trapezoidMajorInnerPoint2Y  = centerY + scaledSize * 0.3585 * Math.cos(trapezoidMajorInnerAngle2);
                    trapezoidMajorOuterPoint1X  = centerX + scaledSize * 0.4105 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint1Y  = centerY + scaledSize * 0.4105 * Math.cos(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint2X  = centerX + scaledSize * 0.4105 * Math.sin(trapezoidMajorOuterAngle2);
                    trapezoidMajorOuterPoint2Y  = centerY + scaledSize * 0.4105 * Math.cos(trapezoidMajorOuterAngle2);

                    trapezoidMediumInnerAngle1  = Math.toRadians(angle - 1.0 + startAngle);
                    trapezoidMediumInnerAngle2  = Math.toRadians(angle + 1.0 + startAngle);
                    trapezoidMediumOuterAngle1  = Math.toRadians(angle - 0.7 + startAngle);
                    trapezoidMediumOuterAngle2  = Math.toRadians(angle + 0.7 + startAngle);
                    trapezoidMediumInnerPoint1X = centerX + scaledSize * 0.3585 * Math.sin(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint1Y = centerY + scaledSize * 0.3585 * Math.cos(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint2X = centerX + scaledSize * 0.3585 * Math.sin(trapezoidMediumInnerAngle2);
                    trapezoidMediumInnerPoint2Y = centerY + scaledSize * 0.3585 * Math.cos(trapezoidMediumInnerAngle2);
                    trapezoidMediumOuterPoint1X = centerX + scaledSize * 0.3985 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMediumOuterPoint1Y = centerY + scaledSize * 0.3985 * Math.cos(trapezoidMediumOuterAngle1);
                    trapezoidMediumOuterPoint2X = centerX + scaledSize * 0.3985 * Math.sin(trapezoidMediumOuterAngle2);
                    trapezoidMediumOuterPoint2Y = centerY + scaledSize * 0.3985 * Math.cos(trapezoidMediumOuterAngle2);

                    trapezoidMinorInnerAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    trapezoidMinorInnerAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    trapezoidMinorOuterAngle1   = Math.toRadians(angle - 0.6 + startAngle);
                    trapezoidMinorOuterAngle2   = Math.toRadians(angle + 0.6 + startAngle);
                    trapezoidMinorInnerPoint1X  = centerX + scaledSize * 0.3585 * Math.sin(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint1Y  = centerY + scaledSize * 0.3585 * Math.cos(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint2X  = centerX + scaledSize * 0.3585 * Math.sin(trapezoidMinorInnerAngle2);
                    trapezoidMinorInnerPoint2Y  = centerY + scaledSize * 0.3585 * Math.cos(trapezoidMinorInnerAngle2);
                    trapezoidMinorOuterPoint1X  = centerX + scaledSize * 0.3975 * Math.sin(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint1Y  = centerY + scaledSize * 0.3975 * Math.cos(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint2X  = centerX + scaledSize * 0.3975 * Math.sin(trapezoidMinorOuterAngle2);
                    trapezoidMinorOuterPoint2Y  = centerY + scaledSize * 0.3975 * Math.cos(trapezoidMinorOuterAngle2);
                    break;
                case INSIDE:
                default:
                    innerPointX                = centerX + scaledSize * 0.423 * sinValue;
                    innerPointY                = centerY + scaledSize * 0.423 * cosValue;
                    innerMediumPointX          = centerX + scaledSize * 0.43 * sinValue;
                    innerMediumPointY          = centerY + scaledSize * 0.43 * cosValue;
                    innerMinorPointX           = centerX + scaledSize * 0.436 * sinValue;
                    innerMinorPointY           = centerY + scaledSize * 0.436 * cosValue;
                    outerPointX                = centerX + scaledSize * 0.475 * sinValue;
                    outerPointY                = centerY + scaledSize * 0.475 * cosValue;
                    outerMediumPointX          = centerX + scaledSize * 0.475 * sinValue;
                    outerMediumPointY          = centerY + scaledSize * 0.475 * cosValue;
                    outerMinorPointX           = centerX + scaledSize * 0.475 * sinValue;
                    outerMinorPointY           = centerY + scaledSize * 0.475 * cosValue;
                    textPointX                 = centerX + scaledSize * orthTextFactor * sinValue;
                    textPointY                 = centerY + scaledSize * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + scaledSize * 0.4625 * sinValue;
                    dotCenterY                 = centerY + scaledSize * 0.4625 * cosValue;
                    dotMediumCenterX           = centerX + scaledSize * 0.465625 * sinValue;
                    dotMediumCenterY           = centerY + scaledSize * 0.465625 * cosValue;
                    dotMinorCenterX            = centerX + scaledSize * 0.46875 * sinValue;
                    dotMinorCenterY            = centerY + scaledSize * 0.46875 * cosValue;
                    tickLabelTickMarkX         = centerX + scaledSize * 0.445 * sinValue;
                    tickLabelTickMarkY         = centerY + scaledSize * 0.445 * cosValue;

                    trapezoidMajorInnerAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    trapezoidMajorInnerAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    trapezoidMajorOuterAngle1   = Math.toRadians(angle - 1.2 + startAngle);
                    trapezoidMajorOuterAngle2   = Math.toRadians(angle + 1.2 + startAngle);
                    trapezoidMajorInnerPoint1X  = centerX + scaledSize * 0.423 * Math.sin(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint1Y  = centerY + scaledSize * 0.423 * Math.cos(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint2X  = centerX + scaledSize * 0.423 * Math.sin(trapezoidMajorInnerAngle2);
                    trapezoidMajorInnerPoint2Y  = centerY + scaledSize * 0.423 * Math.cos(trapezoidMajorInnerAngle2);
                    trapezoidMajorOuterPoint1X  = centerX + scaledSize * 0.475 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint1Y  = centerY + scaledSize * 0.475 * Math.cos(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint2X  = centerX + scaledSize * 0.475 * Math.sin(trapezoidMajorOuterAngle2);
                    trapezoidMajorOuterPoint2Y  = centerY + scaledSize * 0.475 * Math.cos(trapezoidMajorOuterAngle2);

                    trapezoidMediumInnerAngle1  = Math.toRadians(angle - 0.7 + startAngle);
                    trapezoidMediumInnerAngle2  = Math.toRadians(angle + 0.7 + startAngle);
                    trapezoidMediumOuterAngle1  = Math.toRadians(angle - 1.0 + startAngle);
                    trapezoidMediumOuterAngle2  = Math.toRadians(angle + 1.0 + startAngle);
                    trapezoidMediumInnerPoint1X = centerX + scaledSize * 0.435 * Math.sin(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint1Y = centerY + scaledSize * 0.435 * Math.cos(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint2X = centerX + scaledSize * 0.435 * Math.sin(trapezoidMediumInnerAngle2);
                    trapezoidMediumInnerPoint2Y = centerY + scaledSize * 0.435 * Math.cos(trapezoidMediumInnerAngle2);
                    trapezoidMediumOuterPoint1X = centerX + scaledSize * 0.475 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMediumOuterPoint1Y = centerY + scaledSize * 0.475 * Math.cos(trapezoidMediumOuterAngle1);
                    trapezoidMediumOuterPoint2X = centerX + scaledSize * 0.475 * Math.sin(trapezoidMediumOuterAngle2);
                    trapezoidMediumOuterPoint2Y = centerY + scaledSize * 0.475 * Math.cos(trapezoidMediumOuterAngle2);

                    trapezoidMinorInnerAngle1   = Math.toRadians(angle - 0.6 + startAngle);
                    trapezoidMinorInnerAngle2   = Math.toRadians(angle + 0.6 + startAngle);
                    trapezoidMinorOuterAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    trapezoidMinorOuterAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    trapezoidMinorInnerPoint1X  = centerX + scaledSize * 0.440 * Math.sin(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint1Y  = centerY + scaledSize * 0.440 * Math.cos(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint2X  = centerX + scaledSize * 0.440 * Math.sin(trapezoidMinorInnerAngle2);
                    trapezoidMinorInnerPoint2Y  = centerY + scaledSize * 0.440 * Math.cos(trapezoidMinorInnerAngle2);
                    trapezoidMinorOuterPoint1X  = centerX + scaledSize * 0.475 * Math.sin(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint1Y  = centerY + scaledSize * 0.475 * Math.cos(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint2X  = centerX + scaledSize * 0.475 * Math.sin(trapezoidMinorOuterAngle2);
                    trapezoidMinorOuterPoint2Y  = centerY + scaledSize * 0.475 * Math.cos(trapezoidMinorOuterAngle2);
                    break;
            }

            // Set the general tickmark color
            tickMarkCtx.setStroke(tickMarkColor);
            tickMarkCtx.setFill(tickMarkColor);

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw major tick mark
                isNotZero = Double.compare(0.0, counter) != 0;
                TickMarkType tickMarkType = TickMarkType.LINE;
                if (majorTickMarksVisible) {
                    tickMarkType = majorTickMarkType;
                    tickMarkCtx.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    tickMarkCtx.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    tickMarkCtx.setLineWidth(size * (TickMarkType.BOX == tickMarkType || TickMarkType.PILL == tickMarkType ? 0.016 : 0.0055));
                    tickMarkCtx.setLineCap(TickMarkType.PILL == tickMarkType ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
                } else if (minorTickMarksVisible) {
                    tickMarkType = minorTickMarkType;
                    tickMarkCtx.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    tickMarkCtx.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    tickMarkCtx.setLineWidth(size * (TickMarkType.BOX == tickMarkType || TickMarkType.PILL == tickMarkType ? 0.007 : 0.00225));
                    tickMarkCtx.setLineCap(TickMarkType.PILL == tickMarkType ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
                }
                if (fullRange && !isNotZero) {
                    tickMarkCtx.setFill(zeroColor);
                    tickMarkCtx.setStroke(zeroColor);
                }

                switch (tickMarkType) {
                    case TRAPEZOID:
                        if (majorTickMarksVisible) {
                            Helper.drawTrapezoid(tickMarkCtx, trapezoidMajorInnerPoint1X, trapezoidMajorInnerPoint1Y, trapezoidMajorInnerPoint2X, trapezoidMajorInnerPoint2Y,
                                                 trapezoidMajorOuterPoint1X, trapezoidMajorOuterPoint1Y, trapezoidMajorOuterPoint2X, trapezoidMajorOuterPoint2Y);
                        } else if (minorTickMarksVisible) {
                            Helper.drawTrapezoid(tickMarkCtx, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y,
                                                 trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                        }
                        break;
                    case TRIANGLE:
                        if (majorTickMarksVisible) {
                            if (TickLabelLocation.INSIDE == tickLabelLocation) {
                                Helper.drawTriangle(tickMarkCtx, innerPointX, innerPointY, trapezoidMajorOuterPoint1X, trapezoidMajorOuterPoint1Y, trapezoidMajorOuterPoint2X, trapezoidMajorOuterPoint2Y);
                            } else {
                                Helper.drawTriangle(tickMarkCtx, outerPointX, outerPointY, trapezoidMajorInnerPoint1X, trapezoidMajorInnerPoint1Y, trapezoidMajorInnerPoint2X, trapezoidMajorInnerPoint2Y);
                            }
                        } else if (minorTickMarksVisible) {
                            if (TickLabelLocation.INSIDE == tickLabelLocation) {
                                Helper.drawTriangle(tickMarkCtx, innerMinorPointX, innerMinorPointY, trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                            } else {
                                Helper.drawTriangle(tickMarkCtx, outerMinorPointX, outerMinorPointY, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y);
                            }
                        }
                        break;
                    case DOT:
                        if (majorTickMarksVisible) {
                            Helper.drawDot(tickMarkCtx, dotCenterX - majorHalfDotSize, dotCenterY - majorHalfDotSize, majorDotSize);
                        } else if (minorTickMarksVisible) {
                            Helper.drawDot(tickMarkCtx, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                        }
                        break;
                    case TICK_LABEL:
                        if (majorTickMarksVisible) {
                            tickMarkCtx.save();
                            tickMarkCtx.translate(tickLabelTickMarkX, tickLabelTickMarkY);

                            Helper.rotateContextForText(tickMarkCtx, startAngle, angle, tickLabelOrientation);

                            tickMarkCtx.setFont(isNotZero ? tickMarkFont : tickMarkZeroFont);
                            tickMarkCtx.setTextAlign(TextAlignment.CENTER);
                            tickMarkCtx.setTextBaseline(VPos.CENTER);
                            tickMarkCtx.fillText(String.format(locale, tickLabelFormatString, counter), 0, 0);
                            tickMarkCtx.restore();
                        }
                        break;
                    case LINE:
                    default:
                        if (majorTickMarksVisible) {
                            Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerPointX, outerPointY);
                        } else if (minorTickMarksVisible) {
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(tickMarkCtx, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                        }
                        break;
                }

                // Draw tick label text
                if (tickLabelsVisible) {
                    tickMarkCtx.save();
                    tickMarkCtx.translate(textPointX, textPointY);

                    Helper.rotateContextForText(tickMarkCtx, startAngle, angle, tickLabelOrientation);
                    tickMarkCtx.setFont(isNotZero ? tickLabelFont : tickLabelZeroFont);
                    tickMarkCtx.setTextAlign(TextAlignment.CENTER);
                    tickMarkCtx.setTextBaseline(VPos.CENTER);

                    if (!onlyFirstAndLastLabelVisible) {
                        if (isNotZero) {
                            tickMarkCtx.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                        } else {
                            tickMarkCtx.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : fullRange ? zeroColor : tickLabelColor);
                        }
                    } else {
                        if ((Double.compare(counter, minValue) == 0 || Double.compare(counter, maxValue) == 0)) {
                            if (isNotZero) {
                                tickMarkCtx.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                            } else {
                                tickMarkCtx.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : fullRange ? zeroColor : tickLabelColor);
                            }
                        } else {
                            tickMarkCtx.setFill(Color.TRANSPARENT);
                        }
                    }

                    if (customTickLabelsEnabled) {
                        if (customTickLabelCounter >= 0) {
                            tickMarkCtx.fillText(customTickLabels.get(customTickLabelCounter), 0, 0);
                            customTickLabelCounter++;
                        }
                        if (customTickLabelCounter > customTickLabels.size() - 1) customTickLabelCounter = -1;
                    } else {
                        tickMarkCtx.fillText(String.format(locale, tickLabelFormatString, counter), 0, 0);
                    }
                    tickMarkCtx.restore();
                }
            } else if (mediumTickMarksVisible &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0.0) != 0.0 &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0.0) == 0.0) {
                // Draw medium tick mark
                tickMarkCtx.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                tickMarkCtx.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                switch(mediumTickMarkType) {
                    case TRAPEZOID:
                        Helper.drawTrapezoid(tickMarkCtx, trapezoidMediumInnerPoint1X, trapezoidMediumInnerPoint1Y, trapezoidMediumInnerPoint2X, trapezoidMediumInnerPoint2Y,
                                             trapezoidMediumOuterPoint1X, trapezoidMediumOuterPoint1Y, trapezoidMediumOuterPoint2X, trapezoidMediumOuterPoint2Y);
                        break;
                    case TRIANGLE:
                        if (TickLabelLocation.INSIDE == tickLabelLocation) {
                            Helper.drawTriangle(tickMarkCtx, innerMediumPointX, innerMediumPointY, trapezoidMediumOuterPoint1X, trapezoidMediumOuterPoint1Y, trapezoidMediumOuterPoint2X, trapezoidMediumOuterPoint2Y);
                        } else {
                            Helper.drawTriangle(tickMarkCtx, outerMediumPointX, outerMediumPointY, trapezoidMediumInnerPoint1X, trapezoidMediumInnerPoint1Y, trapezoidMediumInnerPoint2X, trapezoidMediumInnerPoint2Y);
                        }
                        break;
                    case DOT:
                        Helper.drawDot(tickMarkCtx, dotMediumCenterX - mediumHalfDotSize, dotMediumCenterY - mediumHalfDotSize, mediumDotSize);
                        break;
                    case BOX:
                        tickMarkCtx.setLineWidth(size * 0.009);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(tickMarkCtx, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                    case PILL:
                        tickMarkCtx.setLineCap(StrokeLineCap.ROUND);
                        tickMarkCtx.setLineWidth(size * 0.009);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(tickMarkCtx, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                    case LINE:
                    default:
                        tickMarkCtx.setLineWidth(size * 0.0035);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(tickMarkCtx, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                }
            } else if (minorTickMarksVisible && Double.compare(counterBD.remainder(minorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw minor tick mark
                if (TickMarkType.TICK_LABEL != majorTickMarkType) {
                    tickMarkCtx.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    tickMarkCtx.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    switch (minorTickMarkType) {
                        case TRAPEZOID:
                            Helper.drawTrapezoid(tickMarkCtx, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y,
                                                 trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                            break;
                        case TRIANGLE:
                            if (TickLabelLocation.INSIDE == tickLabelLocation) {
                                Helper.drawTriangle(tickMarkCtx, innerMinorPointX, innerMinorPointY, trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                            } else {
                                Helper.drawTriangle(tickMarkCtx, outerMinorPointX, outerMinorPointY, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y);
                            }
                            break;
                        case DOT:
                            Helper.drawDot(tickMarkCtx, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                            break;
                        case BOX:
                            tickMarkCtx.setLineWidth(size * 0.007);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(tickMarkCtx, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                        case PILL:
                            tickMarkCtx.setLineCap(StrokeLineCap.ROUND);
                            tickMarkCtx.setLineWidth(size * 0.007);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(tickMarkCtx, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                        case LINE:
                        default:
                            tickMarkCtx.setLineWidth(size * 0.00225);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(tickMarkCtx, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(tickMarkCtx, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                    }
                }
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
            if (counter > maxValue) break;
            angle     = ScaleDirection.CLOCKWISE == scaleDirection ? (angle - tmpAngleStep) : (angle + tmpAngleStep);
        }
    }

    private void drawGradientBar() {
        Pos                knobPosition      = gauge.getKnobPosition();
        TickLabelLocation  tickLabelLocation = gauge.getTickLabelLocation();
        double             scaledSize        = size * 1.9;
        double             xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.105 * scaledSize : 0.03875 * scaledSize;
        double             wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? scaledSize * 0.79 : scaledSize * 0.925;
        double             offsetX           = Pos.TOP_LEFT == knobPosition || Pos.BOTTOM_LEFT == knobPosition ? -scaledSize * 0.475 : 0;
        double             offsetY           = Pos.TOP_LEFT == knobPosition || Pos.TOP_RIGHT == knobPosition ? -scaledSize * 0.475 : 0;
        double             offset            = 90 - startAngle;
        ScaleDirection     scaleDirection    = gauge.getScaleDirection();
        List<Stop>         stops             = gauge.getGradientBarStops();
        Map<Double, Color> stopAngleMap      = new HashMap<>(stops.size());
        for (Stop stop : stops) { stopAngleMap.put(stop.getOffset() * ANGLE_RANGE, stop.getColor()); }
        double               offsetFactor = ScaleDirection.CLOCKWISE == scaleDirection ? (Pos.TOP_LEFT == knobPosition || Pos.BOTTOM_RIGHT == knobPosition ? startAngle : 180 - startAngle) : (startAngle + 180);
        AngleConicalGradient gradient     = new AngleConicalGradient(scaledSize * 0.5, scaledSize * 0.5, offsetFactor, stopAngleMap, gauge.getScaleDirection());

        double barStartAngle  = ScaleDirection.CLOCKWISE == scaleDirection ? -minValue * angleStep : minValue * angleStep;
        double barAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? gauge.getRange() * angleStep : -gauge.getRange() * angleStep;
        tickMarkCtx.save();
        tickMarkCtx.setStroke(gradient.getImagePattern(new Rectangle(xy - 0.026 * scaledSize + offsetX, xy - 0.026 * scaledSize + offsetY, wh + 0.052 * scaledSize, wh + 0.052 * scaledSize)));
        tickMarkCtx.setLineWidth(scaledSize * 0.052);
        tickMarkCtx.setLineCap(StrokeLineCap.BUTT);
        tickMarkCtx.strokeArc(xy + offsetX, xy + offsetY, wh, wh, -(offset + barStartAngle), -barAngleExtend, ArcType.OPEN);
        tickMarkCtx.restore();
    }

    private void drawAreasAndSections(final GraphicsContext CTX) {
        if (areas.isEmpty() && sections.isEmpty()) return;
        double value        = gauge.getCurrentValue();
        Pos    knobPosition = gauge.getKnobPosition();
        double scaledSize   = size * 1.9;
        double offset       = 90 - startAngle;
        double offsetX;
        double offsetY;
        double xy;
        double wh;
        int    listSize;
        
        // Draw Areas
        if (areasVisible && !areas.isEmpty()) {
            xy       = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.078 * scaledSize : 0.0125 * scaledSize;
            wh       = TickLabelLocation.OUTSIDE == tickLabelLocation ? scaledSize * 0.846 : scaledSize * 0.97;
            offsetX  = Pos.BOTTOM_RIGHT == knobPosition || Pos.TOP_RIGHT == knobPosition ? 0 : -scaledSize * 0.475;
            offsetY  = Pos.TOP_LEFT == knobPosition || Pos.TOP_RIGHT == knobPosition ? -scaledSize * 0.475 : 0;
            listSize = areas.size();
            for (int i = 0 ; i < listSize ; i++) {
                Section area = areas.get(i);
                double areaStartAngle;
                if (Double.compare(area.getStart(), maxValue) <= 0 && Double.compare(area.getStop(), minValue) >= 0) {
                    if (area.getStart() < minValue && area.getStop() < maxValue) {
                        areaStartAngle = 0;
                    } else {
                        areaStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStart() - minValue) * angleStep : -(area.getStart() - minValue) * angleStep;
                    }
                    double areaAngleExtend;
                    if (area.getStop() > maxValue) {
                        areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - area.getStart()) * angleStep : -(maxValue - area.getStart()) * angleStep;
                    } else if (Double.compare(area.getStart(), minValue) < 0) {
                        areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStop() - minValue) * angleStep : -(area.getStop() - minValue) * angleStep;
                    } else {
                        areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStop() - area.getStart()) * angleStep : -(area.getStop() - area.getStart()) * angleStep;
                    }
                    CTX.save();
                    if (highlightAreas) {
                        CTX.setFill(area.contains(value) ? area.getHighlightColor() : area.getColor());
                    } else {
                        CTX.setFill(area.getColor());
                    }
                    CTX.fillArc(xy + offsetX, xy + offsetY, wh, wh, -(offset + areaStartAngle), - areaAngleExtend, ArcType.ROUND);
                    CTX.restore();
                }
            }    
        }
        
        // Draw Sections
        if (sectionsVisible && !sections.isEmpty()) {
            xy       = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.11675 * scaledSize : 0.03265 * scaledSize;
            wh       = TickLabelLocation.OUTSIDE == tickLabelLocation ? scaledSize * 0.7745 : scaledSize * 0.935;
            offsetX  = TickLabelLocation.OUTSIDE == tickLabelLocation 
                     ? ( Pos.BOTTOM_RIGHT == knobPosition || Pos.TOP_RIGHT == knobPosition ? -scaledSize * 0.0045 : -scaledSize * 0.4770 )
                     : ( Pos.BOTTOM_RIGHT == knobPosition || Pos.TOP_RIGHT == knobPosition ? 0 : -scaledSize * 0.4738 );
            offsetY  = TickLabelLocation.OUTSIDE == tickLabelLocation 
                     ? ( Pos.TOP_LEFT == knobPosition || Pos.TOP_RIGHT == knobPosition ? -scaledSize * 0.4770 : -scaledSize * 0.0045 )
                     : ( Pos.TOP_LEFT == knobPosition || Pos.TOP_RIGHT == knobPosition ? -scaledSize * 0.4738 : 0 );
            listSize = sections.size();
            CTX.setLineWidth(scaledSize * 0.04);
            CTX.setLineCap(StrokeLineCap.BUTT);
            for (int i = 0; i < listSize; i++) {
                Section section = sections.get(i);
                double  sectionStartAngle;
                if (Double.compare(section.getStart(), maxValue) <= 0 && Double.compare(section.getStop(), minValue) >= 0) {
                    if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                        sectionStartAngle = 0;
                    } else {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStart() - minValue) * angleStep : -(section.getStart() - minValue) * angleStep;
                    }
                    double sectionAngleExtend;
                    if (Double.compare(section.getStop(), maxValue) > 0) {
                        sectionAngleExtend =
                            ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - section.getStart()) * angleStep : -(maxValue - section.getStart()) * angleStep;
                    } else if (Double.compare(section.getStart(), minValue) < 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStop() - minValue) * angleStep : -(section.getStop() - minValue) * angleStep;
                    } else {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ?
                                             (section.getStop() - section.getStart()) * angleStep : -(section.getStop() - section.getStart()) * angleStep;
                    }
                    CTX.save();
                    if (highlightSections) {
                        CTX.setStroke(section.contains(value) ? section.getHighlightColor() : section.getColor());
                    } else {
                        CTX.setStroke(section.getColor());
                    }
                    CTX.strokeArc(xy + offsetX, xy + offsetY, wh, wh, -(offset + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
                    CTX.restore();
                }
            }
        }
    }
    
    private void drawLed() {
        ledCtx.clearRect(0, 0, ledSize, ledSize);

        boolean isFlatLed = LedType.FLAT == gauge.getLedType();

        if (!isFlatLed) {
            ledCtx.setFill(ledFramePaint);
            ledCtx.fillOval(0, 0, ledSize, ledSize);
        } else {
            double lineWidth = 0.0037037 * size;
            ledCtx.setStroke(ledFramePaint);
            ledCtx.setLineWidth(lineWidth);
            ledCtx.strokeOval(lineWidth, lineWidth, ledSize - 2 * lineWidth, ledSize - 2 * lineWidth);
        }

        ledCtx.save();
        if (gauge.isLedOn()) {
            ledCtx.setEffect(ledOnShadow);
            ledCtx.setFill(ledOnPaint);
        } else {
            ledCtx.setEffect(ledOffShadow);
            ledCtx.setFill(ledOffPaint);
        }
        if (isFlatLed) {
            ledCtx.fillOval(0.2 * ledSize, 0.2 * ledSize, 0.6 * ledSize, 0.6 * ledSize);
        } else {
            ledCtx.fillOval(0.14 * ledSize, 0.14 * ledSize, 0.72 * ledSize, 0.72 * ledSize);
        }
        ledCtx.restore();

        ledCtx.setFill(ledHighlightPaint);
        ledCtx.fillOval(0.21 * ledSize, 0.21 * ledSize, 0.58 * ledSize, 0.58 * ledSize);
    }

    private void drawMarkers() {
        markerPane.getChildren().setAll(markerMap.values());
        markerPane.getChildren().addAll(average, threshold);
        TickLabelLocation tickLabelLocation = gauge.getTickLabelLocation();
        double         scaledSize     = size * 1.95;
        double         markerSize     = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.0125 * size : 0.015 * size;
        double         pathHalf       = markerSize * 0.3;
        ScaleDirection scaleDirection = gauge.getScaleDirection();
        if (gauge.getMarkersVisible()) {
            for (Map.Entry<Marker, Shape> entry : markerMap.entrySet()) {
                Marker marker = entry.getKey();
                Shape  shape  = entry.getValue();
                double valueAngle;
                if (ScaleDirection.CLOCKWISE == scaleDirection) {
                    valueAngle = startAngle - (marker.getValue() - minValue) * angleStep;
                } else {
                    valueAngle = startAngle + (marker.getValue() - minValue) * angleStep;
                }
                double sinValue = Math.sin(Math.toRadians(valueAngle));
                double cosValue = Math.cos(Math.toRadians(valueAngle));
                switch (marker.getMarkerType()) {
                    case TRIANGLE:
                        Path trapezoid = (Path) shape;
                        trapezoid.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                trapezoid.getElements().add(new MoveTo(centerX + scaledSize * 0.38 * sinValue, centerY + scaledSize * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                trapezoid.getElements().add(new LineTo(centerX + scaledSize * 0.4075 * sinValue, centerY + scaledSize * 0.4075 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                trapezoid.getElements().add(new LineTo(centerX + scaledSize * 0.4075 * sinValue, centerY + scaledSize * 0.4075 * cosValue));
                                trapezoid.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                trapezoid.getElements().add(new MoveTo(centerX + scaledSize * 0.465 * sinValue, centerY + scaledSize * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                trapezoid.getElements().add(new LineTo(centerX + scaledSize * 0.436 * sinValue, centerY + scaledSize * 0.436 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                trapezoid.getElements().add(new LineTo(centerX + scaledSize * 0.436 * sinValue, centerY + scaledSize * 0.436 * cosValue));
                                trapezoid.getElements().add(new ClosePath());
                                break;
                        }
                        break;
                    case DOT:
                        Circle dot = (Circle) shape;
                        dot.setRadius(markerSize);
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                dot.setCenterX(centerX + scaledSize * 0.3945 * sinValue);
                                dot.setCenterY(centerY + scaledSize * 0.3945 * cosValue);
                                break;
                            default:
                                dot.setCenterX(centerX + scaledSize * 0.449 * sinValue);
                                dot.setCenterY(centerY + scaledSize * 0.449 * cosValue);
                                break;
                        }
                        break;
                    case STANDARD:
                    default:
                        Path standard = (Path) shape;
                        standard.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                standard.getElements().add(new MoveTo(centerX + scaledSize * 0.38 * sinValue, centerY + scaledSize * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.4075 * sinValue, centerY + scaledSize * 0.4075 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.4575 * sinValue, centerY + scaledSize * 0.4575 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.4575 * sinValue, centerY + scaledSize * 0.4575 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.4075 * sinValue, centerY + scaledSize * 0.4075 * cosValue));
                                standard.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                standard.getElements().add(new MoveTo(centerX + scaledSize * 0.465 * sinValue, centerY + scaledSize * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.436 * sinValue, centerY + scaledSize * 0.436 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.386 * sinValue, centerY + scaledSize * 0.386 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.386 * sinValue, centerY + scaledSize * 0.386 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledSize * 0.436 * sinValue, centerY + scaledSize * 0.436 * cosValue));
                                standard.getElements().add(new ClosePath());
                                break;
                        }
                        break;
                }
                Color markerColor = marker.getColor();
                shape.setFill(markerColor);
                shape.setStroke(markerColor.darker());
                shape.setPickOnBounds(false);
                Tooltip markerTooltip;
                if (marker.getText().isEmpty()) {
                    markerTooltip = new Tooltip(Double.toString(marker.getValue()));
                } else {
                    markerTooltip = new Tooltip(new StringBuilder(marker.getText()).append("\n(").append(Double.toString(marker.getValue())).append(")").toString());
                }
                markerTooltip.setTextAlignment(TextAlignment.CENTER);
                Tooltip.install(shape, markerTooltip);
                shape.setOnMousePressed(e -> marker.fireMarkerEvent(marker.MARKER_PRESSED_EVENT));
                shape.setOnMouseReleased(e -> marker.fireMarkerEvent(marker.MARKER_RELEASED_EVENT));
            }
        }

        if (gauge.isThresholdVisible()) {
            // Draw threshold
            threshold.getElements().clear();
            double thresholdAngle;
            if (ScaleDirection.CLOCKWISE == scaleDirection) {
                thresholdAngle = startAngle - (gauge.getThreshold() - minValue) * angleStep;
            } else {
                thresholdAngle = startAngle + (gauge.getThreshold() - minValue) * angleStep;
            }
            double thresholdSize = Helper.clamp(3.0, 3.5, 0.01 * size);
            double sinValue      = Math.sin(Math.toRadians(thresholdAngle));
            double cosValue      = Math.cos(Math.toRadians(thresholdAngle));
            switch (tickLabelLocation) {
                case OUTSIDE:
                    threshold.getElements().add(new MoveTo(centerX + scaledSize * 0.38 * sinValue, centerY + scaledSize * 0.38 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + scaledSize * 0.34 * sinValue, centerY + scaledSize * 0.34 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + scaledSize * 0.34 * sinValue, centerY + scaledSize * 0.34 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
                case INSIDE:
                default:
                    threshold.getElements().add(new MoveTo(centerX + scaledSize * 0.465 * sinValue, centerY + scaledSize * 0.465 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + scaledSize * 0.425 * sinValue, centerY + scaledSize * 0.425 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + scaledSize * 0.425 * sinValue, centerY + scaledSize * 0.425 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
            }
            threshold.setFill(gauge.getThresholdColor());
            threshold.setStroke(gauge.getTickMarkColor());
        }
    }

    private void drawAverage() {
        double scaledSize = size * 1.95;
        // Draw average
        average.getElements().clear();
        double averageAngle;
        if (ScaleDirection.CLOCKWISE == scaleDirection) {
            averageAngle = startAngle - (gauge.getAverage() - minValue) * angleStep;
        } else {
            averageAngle = startAngle + (gauge.getAverage() - minValue) * angleStep;
        }
        double averageSize = Helper.clamp(3.0, 3.5, 0.01 * size);
        double sinValue      = Math.sin(Math.toRadians(averageAngle));
        double cosValue      = Math.cos(Math.toRadians(averageAngle));
        switch (tickLabelLocation) {
            case OUTSIDE:
                average.getElements().add(new MoveTo(centerX + scaledSize * 0.38 * sinValue, centerY + scaledSize * 0.38 * cosValue));
                sinValue = Math.sin(Math.toRadians(averageAngle - averageSize));
                cosValue = Math.cos(Math.toRadians(averageAngle - averageSize));
                average.getElements().add(new LineTo(centerX + scaledSize * 0.34 * sinValue, centerY + scaledSize * 0.34 * cosValue));
                sinValue = Math.sin(Math.toRadians(averageAngle + averageSize));
                cosValue = Math.cos(Math.toRadians(averageAngle + averageSize));
                average.getElements().add(new LineTo(centerX + scaledSize * 0.34 * sinValue, centerY + scaledSize * 0.34 * cosValue));
                average.getElements().add(new ClosePath());
                break;
            case INSIDE:
            default:
                average.getElements().add(new MoveTo(centerX + scaledSize * 0.465 * sinValue, centerY + scaledSize * 0.465 * cosValue));
                sinValue = Math.sin(Math.toRadians(averageAngle - averageSize));
                cosValue = Math.cos(Math.toRadians(averageAngle - averageSize));
                average.getElements().add(new LineTo(centerX + scaledSize * 0.425 * sinValue, centerY + scaledSize * 0.425 * cosValue));
                sinValue = Math.sin(Math.toRadians(averageAngle + averageSize));
                cosValue = Math.cos(Math.toRadians(averageAngle + averageSize));
                average.getElements().add(new LineTo(centerX + scaledSize * 0.425 * sinValue, centerY + scaledSize * 0.425 * cosValue));
                average.getElements().add(new ClosePath());
                break;
        }
        average.setFill(gauge.getAverageColor());
        average.setStroke(gauge.getTickMarkColor());
    }

    private void updateMarkers() {
        markerMap.clear();
        for (Marker marker : gauge.getMarkers()) {
            switch(marker.getMarkerType()) {
                case TRIANGLE: markerMap.put(marker, new Path()); break;
                case DOT     : markerMap.put(marker, new Circle()); break;
                case STANDARD:
                default:       markerMap.put(marker, new Path()); break;
            }
        }
    }

    private void drawKnob(final boolean PRESSED) {
        knobCanvas.setCache(false);
        double w = knobCanvas.getWidth();
        double h = knobCanvas.getHeight();
        knobCtx.clearRect(0, 0, w, h);

        Color  knobColor = gauge.getKnobColor();
        double hue       = knobColor.getHue();
        double sat       = knobColor.getSaturation();
        double alp       = knobColor.getOpacity();
        double brg       = Color.BLACK.equals(knobColor) ? 0.2 : knobColor.getBrightness();
        double gradTop;
        double gradBot;

        switch (gauge.getKnobType()) {
            case PLAIN:
                knobCtx.setFill(new LinearGradient(0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(180,180,180)),
                                                   new Stop(0.46, Color.rgb(63,63,63)),
                                                   new Stop(1.0, Color.rgb(40,40,40))));
                knobCtx.fillOval(0, 0, w, h);

                knobCtx.setFill(new LinearGradient(0, 0.11764706 * h, 0, 0.76470588 * h, false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.9 : brg * 1.0, alp)),
                                                   new Stop(0.01, Color.hsb(hue, sat, PRESSED ? brg * 0.75 : brg * 0.85, alp)),
                                                   new Stop(0.5, Color.hsb(hue, sat, PRESSED ? brg * 0.4 : brg * 0.5, alp)),
                                                   new Stop(0.51, Color.hsb(hue, sat, PRESSED ? brg * 0.35 : brg * 0.45, alp)),
                                                   new Stop(1.0, Color.hsb(hue, sat, PRESSED ? brg * 0.7 : brg * 0.8, alp))));
                knobCtx.fillOval(w * 0.11764706, h * 0.11764706, w - w * 0.23529412, h - h * 0.23529412);

                knobCtx.setFill(new RadialGradient(0, 0, 0.5 * w, 0.47 * h, w * 0.38, false, CycleMethod.NO_CYCLE,
                                                   new Stop(0, Color.TRANSPARENT),
                                                   new Stop(0.76, Color.TRANSPARENT),
                                                   new Stop(1.0, Color.rgb(0, 0, 0, PRESSED ? 0.5 : 0.2))));
                knobCtx.fillOval(w * 0.11764706, h * 0.11764706, w - w * 0.23529412, h - h * 0.23529412);
                break;
            case METAL:
                knobCtx.setFill(new LinearGradient(0, 0, 0, h,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(92,95,101)),
                                                   new Stop(0.47, Color.rgb(46,49,53)),
                                                   new Stop(1.0, Color.rgb(22,23,26))));
                knobCtx.fillOval(0, 0, w, h);

                knobCtx.setFill(new LinearGradient(0, 0.058823529411764705 * h, 0, 0.9411764705882353 * h,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.7 : brg * 0.9, alp)),
                                                   new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.3 : brg * 0.5, alp))));
                knobCtx.fillOval(0.05882353 * w, 0.05882353 * h, w * 0.88235294, h * 0.88235294);

                knobCtx.beginPath();
                knobCtx.moveTo(0.17647058823529413 * w, 0.8235294117647058 * h);
                knobCtx.bezierCurveTo(0.29411764705882354 * w, 0.8823529411764706 * h, 0.35294117647058826 * w, 0.9411764705882353 * h, 0.5294117647058824 * w, 0.9411764705882353 * h);
                knobCtx.bezierCurveTo(0.6470588235294118 * w, 0.9411764705882353 * h, 0.7058823529411765 * w, 0.8823529411764706 * h, 0.8235294117647058 * w, 0.8235294117647058 * h);
                knobCtx.bezierCurveTo(0.7647058823529411 * w, 0.7058823529411765 * h, 0.6470588235294118 * w, 0.5882352941176471 * h, 0.5294117647058824 * w, 0.5882352941176471 * h);
                knobCtx.bezierCurveTo(0.35294117647058826 * w, 0.5882352941176471 * h, 0.23529411764705882 * w, 0.7058823529411765 * h, 0.17647058823529413 * w, 0.8235294117647058 * h);
                knobCtx.closePath();
                knobCtx.setFill(new RadialGradient(0, 0,
                                                0.47058823529411764 * w, 0.8823529411764706 * h,
                                                0.3235294117647059 * w,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(255, 255, 255, PRESSED ? 0.3 : 0.6)),
                                                   new Stop(1.0, Color.TRANSPARENT)));
                knobCtx.fill();

                knobCtx.beginPath();
                knobCtx.moveTo(0.058823529411764705 * w, 0.29411764705882354 * h);
                knobCtx.bezierCurveTo(0.17647058823529413 * w, 0.35294117647058826 * h, 0.35294117647058826 * w, 0.35294117647058826 * h, 0.5294117647058824 * w, 0.35294117647058826 * h);
                knobCtx.bezierCurveTo(0.6470588235294118 * w, 0.35294117647058826 * h, 0.8235294117647058 * w, 0.35294117647058826 * h, 0.9411764705882353 * w, 0.29411764705882354 * h);
                knobCtx.bezierCurveTo(0.8823529411764706 * w, 0.11764705882352941 * h, 0.7058823529411765 * w, 0.0 * h, 0.5294117647058824 * w, 0.0 * h);
                knobCtx.bezierCurveTo(0.29411764705882354 * w, 0.0 * h, 0.11764705882352941 * w, 0.11764705882352941 * h, 0.058823529411764705 * w, 0.29411764705882354 * h);
                knobCtx.closePath();
                knobCtx.setFill(new RadialGradient(0, 0,
                                                0.47058823529411764 * w, 0.0,
                                                0.4411764705882353 * w,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(255, 255, 255, PRESSED ? 0.45 : 0.75)),
                                                   new Stop(1.0, Color.TRANSPARENT)));
                knobCtx.fill();

                knobCtx.setFill(new LinearGradient(0.5294117647058824 * w, 0.23529411764705882 * h,
                                                0.5294117647058824 * w, 0.7647058823529411 * h,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.BLACK),
                                                   new Stop(1.0, Color.rgb(204, 204, 204))));
                knobCtx.fillOval(0.23529412 * w, 0.23529412 * h, 0.52941176 * w, 0.52941176 * h);

                knobCtx.setFill(new LinearGradient(0.5294117647058824 * w, 0.29411764705882354 * h,
                                                0.5294117647058824 * w, 0.7058823529411765 * h,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(1,6,11)),
                                                   new Stop(1.0, Color.rgb(50,52,56))));
                knobCtx.fillOval(0.29411765 * w, 0.29411765 * h, 0.41176471 * w, 0.41176471 * h);
                break;
            case FLAT:
                double lineWidth = 0.00740741 * size;
                double knobSize  = w - 2 * lineWidth;
                knobCtx.setFill(PRESSED ? knobColor.darker() : knobColor);
                knobCtx.setStroke(PRESSED ? Color.WHITE.darker() : Color.WHITE);
                knobCtx.setLineWidth(lineWidth);
                knobCtx.fillOval(lineWidth, lineWidth, knobSize, knobSize);
                knobCtx.strokeOval(lineWidth, lineWidth, knobSize, knobSize);
                break;
            case STANDARD:
            default:
                knobCtx.setFill(new LinearGradient(0, 0, 0, h,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(133, 133, 133).brighter().brighter()),
                                                   new Stop(0.52, Color.rgb(133, 133, 133)),
                                                   new Stop(1.0, Color.rgb(133, 133, 133).darker().darker())));
                knobCtx.fillOval(0, 0, w, h);
                gradTop = PRESSED ? h - size * 0.01 : size * 0.005;
                gradBot = PRESSED ? size * 0.005 : h - size * 0.01;
                knobCtx.setFill(new LinearGradient(0, gradTop, 0, gradBot,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.hsb(hue, sat, brg * 0.85, alp)),
                                                   new Stop(0.45, Color.hsb(hue, sat, brg * 0.65, alp)),
                                                   new Stop(1.0, Color.hsb(hue, sat, brg * 0.4, alp))));
                knobCtx.fillOval(size * 0.005, size * 0.005, w - size * 0.01, h - size * 0.01);
                break;
        }
        knobCanvas.setCache(true);
        knobCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private void resizeText() {
        double maxWidth = 0.4 * size;

        titleText.setFont(Fonts.robotoMedium(size * 0.06));
        titleText.setText(gauge.getTitle());
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, size * 0.06); }

        unitText.setFont(Fonts.robotoRegular(size * 0.05));
        unitText.setText(gauge.getUnit());
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, size * 0.05); }

        maxWidth = 0.25 * size;
        subTitleText.setFont(Fonts.robotoRegular(size * 0.05));
        subTitleText.setText(gauge.getSubTitle());
        if (subTitleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(subTitleText, maxWidth, size * 0.05); }

        double offset = size * 0.025;
        Pos knobPosition = gauge.getKnobPosition();
        switch(knobPosition) {
            case BOTTOM_LEFT :
                titleText.relocate(offset, size * 0.45);
                unitText.relocate(offset, size * 0.55);
                subTitleText.relocate(offset, size * 0.65);
                break;
            case TOP_LEFT    :
                titleText.relocate(offset, size * 0.3);
                unitText.relocate(offset, size * 0.4);
                subTitleText.relocate(offset, size * 0.5);
                break;
            case TOP_RIGHT   :
                titleText.relocate(size - titleText.getLayoutBounds().getWidth() - offset, size * 0.3);
                unitText.relocate(size - unitText.getLayoutBounds().getWidth() - offset, size * 0.4);
                subTitleText.relocate(size - subTitleText.getLayoutBounds().getWidth() - offset, size * 0.5);
                break;
            case BOTTOM_RIGHT:
            default          :
                titleText.relocate(size - titleText.getLayoutBounds().getWidth() - offset, size * 0.45);
                unitText.relocate(size - unitText.getLayoutBounds().getWidth() - offset, size * 0.55);
                subTitleText.relocate(size - subTitleText.getLayoutBounds().getWidth() - offset, size * 0.65);
                break;
        }
    }

    private void resizeValueText() {
        Pos knobPosition = gauge.getKnobPosition();
        if (gauge.isLcdVisible()) {
            switch(gauge.getLcdFont()) {
                case LCD:
                    valueText.setFont(Fonts.digital(0.108 * size));
                    switch(knobPosition) {
                        case BOTTOM_LEFT :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.865 * size);
                            break;
                        case TOP_LEFT    :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.125 * size);
                            break;
                        case TOP_RIGHT   :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.125 * size);
                            break;
                        case BOTTOM_RIGHT:
                        default          :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.865 * size);
                            break;
                    }
                    break;
                case DIGITAL:
                    valueText.setFont(Fonts.digitalReadout(0.105 * size));
                    switch(knobPosition) {
                        case BOTTOM_LEFT :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.875 * size);
                            break;
                        case TOP_LEFT    :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.136 * size);
                            break;
                        case TOP_RIGHT   :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.136 * size);
                            break;
                        case BOTTOM_RIGHT:
                        default          :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.875 * size);
                            break;
                    }
                    break;
                case DIGITAL_BOLD:
                    valueText.setFont(Fonts.digitalReadoutBold(0.105 * size));
                    switch(knobPosition) {
                        case BOTTOM_LEFT :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.875 * size);
                            break;
                        case TOP_LEFT    :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.136 * size);
                            break;
                        case TOP_RIGHT   :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.136 * size);
                            break;
                        case BOTTOM_RIGHT:
                        default          :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.875 * size);
                            break;
                    }
                    break;
                case ELEKTRA:
                    valueText.setFont(Fonts.elektra(0.1116 * size));
                    switch(knobPosition) {
                        case BOTTOM_LEFT :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.87 * size);
                            break;
                        case TOP_LEFT    :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.1267 * size);
                            break;
                        case TOP_RIGHT   :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.1267 * size);
                            break;
                        case BOTTOM_RIGHT:
                        default          :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.87 * size);
                            break;
                    }
                    break;
                case STANDARD:
                default:
                    valueText.setFont(Fonts.robotoMedium(0.09 * size));
                    switch(knobPosition) {
                        case BOTTOM_LEFT :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.865 * size);
                            break;
                        case TOP_LEFT    :
                            valueText.setTranslateX((0.545 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.125 * size);
                            break;
                        case TOP_RIGHT   :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.125 * size);
                            break;
                        case BOTTOM_RIGHT:
                        default          :
                            valueText.setTranslateX((0.845 * size - valueText.getLayoutBounds().getWidth()));
                            valueText.setTranslateY(0.865 * size);
                            break;
                    }
                    break;
            }
        } else {
            valueText.setFont(Fonts.robotoMedium(size * 0.1));
            switch(knobPosition) {
                case BOTTOM_LEFT :
                    valueText.setTranslateX((0.43 * size - valueText.getLayoutBounds().getWidth()));
                    valueText.setTranslateY(0.88 * size);
                    break;
                case TOP_LEFT    :
                    valueText.setTranslateX((0.43 * size - valueText.getLayoutBounds().getWidth()));
                    valueText.setTranslateY(0.12 * size);
                    break;
                case TOP_RIGHT   :
                    valueText.setTranslateX((0.78 * size - valueText.getLayoutBounds().getWidth()));
                    valueText.setTranslateY(0.12 * size);
                    break;
                case BOTTOM_RIGHT:
                default          :
                    valueText.setTranslateX((0.78 * size - valueText.getLayoutBounds().getWidth()));
                    valueText.setTranslateY(0.88 * size);
                    break;
            }
        }
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            Pos knobPosition = gauge.getKnobPosition();
            switch(knobPosition) {
                case BOTTOM_LEFT:
                    centerX = size * 0.05;
                    centerY = size * 0.95;
                    break;
                case TOP_LEFT:
                    centerX = size * 0.05;
                    centerY = size * 0.05;
                    break;
                case TOP_RIGHT:
                    centerX = size * 0.95;
                    centerY = size * 0.05;
                    break;
                case BOTTOM_RIGHT:
                default:
                    centerX = size * 0.95;
                    centerY = size * 0.95;
                    break;
            }

            startAngle = getStartAngle();
            rotateNeedle(gauge.getCurrentValue());

            pane.setMaxSize(size, size);
            pane.relocate((gauge.getWidth() - size) * 0.5, (gauge.getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            backgroundInnerShadow.setOffsetX(0);
            backgroundInnerShadow.setOffsetY(size * 0.03);
            backgroundInnerShadow.setRadius(size * 0.04);

            pane.setEffect(gauge.isInnerShadowEnabled() ? backgroundInnerShadow : null);

            sectionsAndAreasCanvas.setWidth(1.95 * size);
            sectionsAndAreasCanvas.setHeight(1.95 * size);
            
            tickMarkCanvas.setWidth(1.95 * size);
            tickMarkCanvas.setHeight(1.95 * size);

            markerPane.setPrefSize(0.9 * size, 0.9 * size);

            boolean isFlatLed = LedType.FLAT == gauge.getLedType();
            ledSize = 0.07 * size;
            ledCanvas.setWidth(ledSize);
            ledCanvas.setHeight(ledSize);
            ledOffShadow = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            ledOnShadow  = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            if (!isFlatLed) ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, gauge.getLedColor(), 0.36 * ledSize, 0, 0, 0));

            if (gauge.isLcdVisible()) {
                lcd.setWidth(0.4 * size);
                lcd.setHeight(0.114 * size);
                lcd.setArcWidth(0.0125 * size);
                lcd.setArcHeight(0.0125 * size);
            }
            resizeText();
            resizeValueText();

            double needleWidth;
            double needleHeight;
            needle.setCache(false);
            switch(gauge.getNeedleType()) {
                case BIG:
                    needleWidth  = 1.9 * 0.06 * size;
                    needleHeight = TickLabelLocation.INSIDE == tickLabelLocation ? 1.9 * 0.4975 * size : 1.9 * 0.415 * size;
                    Needle.INSTANCE.getPath(needle, needleWidth, needleHeight, NeedleType.BIG, tickLabelLocation);
                    needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - needle.getLayoutBounds().getHeight() * (TickLabelLocation.INSIDE == tickLabelLocation ? 0.93969849 : 0.92771084));
                    needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
                    needleRotate.setPivotY(needle.getLayoutBounds().getHeight() * (TickLabelLocation.INSIDE == tickLabelLocation ? 0.93969849 : 0.92771084));
                    break;
                case FAT:
                    needleWidth  = size * 0.3;
                    needleHeight = size * 0.505;
                    Needle.INSTANCE.getPath(needle, needleWidth, needleHeight, NeedleType.FAT, tickLabelLocation);
                    needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - needle.getLayoutBounds().getHeight() * 0.7029703);
                    needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
                    needleRotate.setPivotY(needle.getLayoutBounds().getHeight() * 0.7029703);
                    break;
                case SCIENTIFIC:
                    needleWidth  = 1.9 * 0.1 * size;
                    needleHeight = TickLabelLocation.INSIDE == tickLabelLocation ? 1.9 * 0.645 * size : 1.9 * 0.5625 * size;
                    Needle.INSTANCE.getPath(needle, needleWidth, needleHeight, NeedleType.SCIENTIFIC, tickLabelLocation);
                    needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - (TickLabelLocation.INSIDE == tickLabelLocation ? needle.getLayoutBounds().getHeight() * 0.7248062: needle.getLayoutBounds().getHeight() * 0.68444444));
                    needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
                    needleRotate.setPivotY(needle.getLayoutBounds().getHeight() * (TickLabelLocation.INSIDE == tickLabelLocation ? 0.7248062 : 0.68444444));
                    break;
                case AVIONIC:
                    needleWidth  = 1.9 * 0.06 * size;
                    needleHeight = TickLabelLocation.INSIDE == tickLabelLocation ? 1.9 * 0.5975 * size : 1.9 * 0.515 * size;
                    Needle.INSTANCE.getPath(needle, needleWidth, needleHeight, NeedleType.AVIONIC, tickLabelLocation);
                    needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - needle.getLayoutBounds().getHeight() * (TickLabelLocation.INSIDE == tickLabelLocation ? 0.78242678 : 0.74757282));
                    needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
                    needleRotate.setPivotY(needle.getLayoutBounds().getHeight() * (TickLabelLocation.INSIDE == tickLabelLocation ? 0.78242678 : 0.74757282));
                    break;
                case VARIOMETER:
                    needleWidth  = size * gauge.getNeedleSize().FACTOR;
                    needleHeight = TickLabelLocation.INSIDE == tickLabelLocation ? 1.9 * size * 0.4675 : 1.9 * size * 0.385;
                    Needle.INSTANCE.getPath(needle, needleWidth, needleHeight, NeedleType.VARIOMETER, tickLabelLocation);
                    needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - needle.getLayoutBounds().getHeight());
                    needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
                    needleRotate.setPivotY(needle.getLayoutBounds().getHeight());
                    break;
                case STANDARD:
                default      :
                    needleWidth  = size * gauge.getNeedleSize().FACTOR;
                    needleHeight = TickLabelLocation.INSIDE == tickLabelLocation ? size * 0.9085 : size * 0.75;
                    Needle.INSTANCE.getPath(needle, needleWidth, needleHeight, NeedleType.STANDARD, tickLabelLocation);
                    needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - needle.getLayoutBounds().getHeight());
                    needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
                    needleRotate.setPivotY(needle.getLayoutBounds().getHeight());
                    break;
            }
            needle.setCache(true);
            needle.setCacheHint(CacheHint.ROTATE);

            knobCanvas.setWidth(size * 0.1);
            knobCanvas.setHeight(size * 0.1);

            switch(knobPosition) {
                case BOTTOM_LEFT :
                    ledCanvas.relocate(size * 0.22, size * 0.72);
                    lcd.relocate(size * 0.15, 0.81 * size);
                    knobCanvas.relocate(0, size * 0.9);
                    break;
                case TOP_LEFT    :
                    ledCanvas.relocate(size * 0.22, size * 0.22);
                    lcd.relocate(size * 0.15, 0.07 * size);
                    knobCanvas.relocate(0, 0);
                    break;
                case TOP_RIGHT   :
                    ledCanvas.relocate(size * 0.72, size * 0.22);
                    lcd.relocate(size * 0.45, 0.07 * size);
                    knobCanvas.relocate(size * 0.9, 0);
                    break;
                case BOTTOM_RIGHT:
                default          :
                    ledCanvas.relocate(size * 0.72, size * 0.72);
                    lcd.relocate(size * 0.45, 0.81 * size);
                    knobCanvas.relocate(size * 0.9, size * 0.9);
                    break;
            }

            buttonTooltip.setText(gauge.getButtonTooltipText());
        }
    }

    @Override protected void redraw() {
        locale       = gauge.getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        shadowGroup.setEffect(gauge.isShadowsEnabled() ? dropShadow : null);

        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        // Areas, Sections and Tick Marks
        tickLabelLocation = gauge.getTickLabelLocation();
        scaleDirection    = gauge.getScaleDirection();
        areasVisible      = gauge.getAreasVisible();
        sectionsVisible   = gauge.getSectionsVisible();
        sectionsAndAreasCtx.clearRect(0, 0, size * 1.9, size * 1.9);
        drawAreasAndSections(sectionsAndAreasCtx);

        tickMarkCanvas.setCache(false);
        tickMarkCtx.clearRect(0, 0, size * 1.9, size * 1.9);
        if (gauge.isGradientBarEnabled() && gauge.getGradientLookup() != null) {
            drawGradientBar();
        }
        drawTickMarks();
        tickMarkCanvas.setCache(true);
        tickMarkCanvas.setCacheHint(CacheHint.QUALITY);

        // LED
        if (gauge.isLedVisible()) {
            final Color LED_COLOR = gauge.getLedColor();
            switch(gauge.getLedType()) {
                case FLAT:
                    ledFramePaint = Color.WHITE;
                    ledOnPaint = new LinearGradient(0, 0.25 * ledSize,
                                                    0, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR),
                                                    new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)));
                    ledOffPaint = new LinearGradient(0, 0.25 * ledSize,
                                                     0, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.13, 1.0)));
                    ledHighlightPaint = Color.TRANSPARENT;
                    break;
                case STANDARD:
                default:
                    ledFramePaint = new LinearGradient(0.14 * ledSize, 0.14 * ledSize,
                                                       0.84 * ledSize, 0.84 * ledSize,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.rgb(20, 20, 20, 0.65)),
                                                       new Stop(0.15, Color.rgb(20, 20, 20, 0.65)),
                                                       new Stop(0.26, Color.rgb(41, 41, 41, 0.65)),
                                                       new Stop(0.26, Color.rgb(41, 41, 41, 0.64)),
                                                       new Stop(0.85, Color.rgb(200, 200, 200, 0.41)),
                                                       new Stop(1.0, Color.rgb(200, 200, 200, 0.35)));
                    ledOnPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                    0.74 * ledSize, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.77, 1.0)),
                                                    new Stop(0.49, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)),
                                                    new Stop(1.0, LED_COLOR));
                    ledOffPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                     0.74 * ledSize, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.20, 1.0)),
                                                     new Stop(0.49, LED_COLOR.deriveColor(0.0, 1.0, 0.13, 1.0)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.2, 1.0)));
                    ledHighlightPaint = new RadialGradient(0, 0,
                                                           0.3 * ledSize, 0.3 * ledSize,
                                                           0.29 * ledSize,
                                                           false, CycleMethod.NO_CYCLE,
                                                           new Stop(0.0, Color.WHITE),
                                                           new Stop(1.0, Color.TRANSPARENT));
                    break;
            }
            drawLed();
        }

        // LCD
        LcdDesign lcdDesign = gauge.getLcdDesign();
        Color[]   lcdColors = lcdDesign.getColors();
        if (gauge.isLcdVisible() && gauge.isValueVisible()) {
            LinearGradient lcdGradient = new LinearGradient(0, 1, 0, lcd.getHeight() - 1,
                                                            false, CycleMethod.NO_CYCLE,
                                                            new Stop(0, lcdColors[0]),
                                                            new Stop(0.03, lcdColors[1]),
                                                            new Stop(0.5, lcdColors[2]),
                                                            new Stop(0.5, lcdColors[3]),
                                                            new Stop(1.0, lcdColors[4]));
            Paint lcdFramePaint;
            if (LcdDesign.FLAT_CUSTOM == lcdDesign) {
                lcdFramePaint = lcdDesign.lcdForegroundColor;
            } else {
                lcdFramePaint = new LinearGradient(0, 0, 0, lcd.getHeight(),
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(26, 26, 26)),
                                                   new Stop(0.01, Color.rgb(77, 77, 77)),
                                                   new Stop(0.99, Color.rgb(77, 77, 77)),
                                                   new Stop(1.0, Color.rgb(221, 221, 221)));
            }
            lcd.setFill(lcdGradient);
            lcd.setStroke(lcdFramePaint);
        }

        // Text
        titleText.setFill(gauge.getTitleColor());
        unitText.setFill(gauge.getUnitColor());
        subTitleText.setFill(gauge.getSubTitleColor());
        valueText.setFill(gauge.isLcdVisible() ? lcdColors[5] : gauge.getValueColor());
        resizeText();

        // Needle
        Color needleColor = gauge.getNeedleColor();
        switch(gauge.getNeedleShape()) {
            case ROUND:
                needlePaint = new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                                 needle.getLayoutBounds().getMaxX(), 0,
                                                 false, CycleMethod.NO_CYCLE,
                                                 new Stop(0.0, needleColor.darker()),
                                                 new Stop(0.5, needleColor.brighter().brighter()),
                                                 new Stop(1.0, needleColor.darker()));
                needle.setStrokeWidth(0);
                break;
            case FLAT:
                needlePaint = needleColor;
                needle.setStrokeWidth(0.0037037 * size);
                break;
            case ANGLED:
            default:
                needlePaint = new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                                 needle.getLayoutBounds().getMaxX(), 0,
                                                 false, CycleMethod.NO_CYCLE,
                                                 new Stop(0.0, needleColor.darker()),
                                                 new Stop(0.5, needleColor.darker()),
                                                 new Stop(0.5, needleColor.brighter()),
                                                 new Stop(1.0, needleColor.brighter()));
                needle.setStrokeWidth(0);
                break;
        }
        if (NeedleType.AVIONIC == gauge.getNeedleType()) {
            needlePaint = new LinearGradient(0, needle.getLayoutBounds().getMinY(),
                                             0, needle.getLayoutBounds().getMaxY(),
                                             false, CycleMethod.NO_CYCLE,
                                             new Stop(0.0, needleColor),
                                             new Stop(0.3, needleColor),
                                             new Stop(0.3, Color.BLACK),
                                             new Stop(1.0, Color.BLACK));
        }
        needle.setFill(needlePaint);
        needle.setStroke(gauge.getNeedleBorderColor());

        // Knob
        drawKnob(false);

        // Markers
        drawMarkers();
        thresholdTooltip.setText("Threshold\n(" + String.format(locale, formatString, gauge.getThreshold()) + ")");
    }
}
