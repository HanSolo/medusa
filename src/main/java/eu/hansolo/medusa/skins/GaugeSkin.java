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

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.TickLabelLocation;
import eu.hansolo.medusa.Gauge.TickLabelOrientation;
import eu.hansolo.medusa.Gauge.TickMarkType;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
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
import javafx.scene.shape.CubicCurveTo;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;


/**
 * Created by hansolo on 11.12.15.
 */
public class GaugeSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double             PREFERRED_WIDTH  = 250;
    private static final double             PREFERRED_HEIGHT = 250;
    private static final double             MINIMUM_WIDTH    = 50;
    private static final double             MINIMUM_HEIGHT   = 50;
    private static final double             MAXIMUM_WIDTH    = 1024;
    private static final double             MAXIMUM_HEIGHT   = 1024;
    private              Map<Marker, Shape> markerMap        = new ConcurrentHashMap<>();
    private double                   oldValue;
    private double                   size;
    private Pane                     pane;
    private Circle                   background;
    private InnerShadow              backgroundInnerShadow;
    private Canvas                   ticksAndSectionsCanvas;
    private GraphicsContext          ticksAndSections;
    private double                   ledSize;
    private InnerShadow              ledOnShadow;
    private InnerShadow              ledOffShadow;
    private Paint                    ledFramePaint;
    private Paint                    ledOnPaint;
    private Paint                    ledOffPaint;
    private Paint                    ledHighlightPaint;
    private Canvas                   ledCanvas;
    private GraphicsContext          led;
    private Pane                     markerPane;
    private Path                     threshold;
    private Rectangle                lcd;
    private Path                     needle;
    private MoveTo                   needleMoveTo1;
    private CubicCurveTo             needleCubicCurveTo2;
    private CubicCurveTo             needleCubicCurveTo3;
    private CubicCurveTo             needleCubicCurveTo4;
    private LineTo                   needleLineTo5;
    private CubicCurveTo             needleCubicCurveTo6;
    private ClosePath                needleClosePath7;
    private Rotate                   needleRotate;
    private Paint                    needlePaint;
    private Canvas                   knobCanvas;
    private GraphicsContext          knob;
    private Group                    shadowGroup;
    private DropShadow               dropShadow;
    private Text                     titleText;
    private Text                     subTitleText;
    private Text                     unitText;
    private Text                     valueText;
    private double                   angleStep;
    private String                   limitString;
    private EventHandler<MouseEvent> mouseHandler;
    private Tooltip                  buttonTooltip;
    private Tooltip                  thresholdTooltip;


    // ******************** Constructors **************************************
    public GaugeSkin(Gauge gauge) {
        super(gauge);
        angleStep    = gauge.getAngleRange() / (gauge.getMaxValue() - gauge.getMinValue());
        oldValue     = gauge.getValue();
        limitString  = "";
        mouseHandler = event -> handleMouseEvent(event);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        updateMarkers();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() < 0 && getSkinnable().getPrefHeight() < 0) {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        backgroundInnerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(10, 10, 10, 0.45), 8, 0d, 8d, 0d);
        background = new Circle();

        ticksAndSectionsCanvas = new Canvas();
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        ledCanvas = new Canvas();
        led = ledCanvas.getGraphicsContext2D();

        thresholdTooltip = new Tooltip("Threshold\n(" + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getThreshold()) + ")");
        thresholdTooltip.setTextAlignment(TextAlignment.CENTER);

        threshold = new Path();
        Tooltip.install(threshold, thresholdTooltip);

        markerPane = new Pane();

        lcd = new Rectangle(0.3 * PREFERRED_WIDTH, 0.014 * PREFERRED_HEIGHT);
        lcd.setArcWidth(0.0125 * PREFERRED_HEIGHT);
        lcd.setArcHeight(0.0125 * PREFERRED_HEIGHT);
        lcd.relocate((PREFERRED_WIDTH - lcd.getWidth()) * 0.5, 0.44 * PREFERRED_HEIGHT);
        lcd.setManaged(getSkinnable().isLcdVisible());
        lcd.setVisible(getSkinnable().isLcdVisible());

        needleRotate = new Rotate(180 - getSkinnable().getStartAngle());
        needleRotate.setAngle(needleRotate.getAngle() + (getSkinnable().getValue() - oldValue - getSkinnable().getMinValue()) * angleStep);
        needleMoveTo1       = new MoveTo();
        needleCubicCurveTo2 = new CubicCurveTo();
        needleCubicCurveTo3 = new CubicCurveTo();
        needleCubicCurveTo4 = new CubicCurveTo();
        needleLineTo5       = new LineTo();
        needleCubicCurveTo6 = new CubicCurveTo();
        needleClosePath7    = new ClosePath();
        needle              = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4, needleLineTo5, needleCubicCurveTo6, needleClosePath7);
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setStrokeType(StrokeType.INSIDE);
        needle.setStroke(Color.TRANSPARENT);

        buttonTooltip    = new Tooltip();
        buttonTooltip.setTextAlignment(TextAlignment.CENTER);

        knobCanvas = new Canvas();
        knob = knobCanvas.getGraphicsContext2D();
        knobCanvas.setPickOnBounds(false);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle, knobCanvas);
        shadowGroup.setEffect(getSkinnable().areShadowsEnabled() ? dropShadow : null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setMouseTransparent(true);

        subTitleText = new Text(getSkinnable().getSubTitle());
        subTitleText.setTextOrigin(VPos.CENTER);
        subTitleText.setMouseTransparent(true);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setMouseTransparent(true);
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setMouseTransparent(true);

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setMouseTransparent(true);

        // Set initial value
        angleStep          = getSkinnable().getAngleStep();
        double targetAngle = 180 - getSkinnable().getStartAngle() + (getSkinnable().getCurrentValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = clamp(180 - getSkinnable().getStartAngle(), 180 - getSkinnable().getStartAngle() + getSkinnable().getAngleRange(), targetAngle);
        needleRotate.setAngle(targetAngle);

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(background,
                                  ticksAndSectionsCanvas,
                                  markerPane,
                                  ledCanvas,
                                  lcd,
                                  titleText,
                                  subTitleText,
                                  unitText,
                                  valueText,
                                  shadowGroup);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) c -> redraw());
        getSkinnable().getAreas().addListener((ListChangeListener<Section>) c -> redraw());
        getSkinnable().getTickMarkSections().addListener((ListChangeListener<Section>) c -> redraw());
        getSkinnable().getTickLabelSections().addListener((ListChangeListener<Section>) c -> redraw());
        getSkinnable().getMarkers().addListener((ListChangeListener<Marker>) c -> {
            updateMarkers();
            redraw();
        });

        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));

        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle());

        handleEvents("INTERACTIVITY");

        handleEvents("VISIBILITY");

        needleRotate.angleProperty().addListener(observable -> handleEvents("ANGLE"));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("ANGLE".equals(EVENT_TYPE)) {
            double currentValue;
            if (ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection()) {
                currentValue = (needleRotate.getAngle() + getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();
            } else {
                currentValue = -(needleRotate.getAngle() - getSkinnable().getStartAngle() - 180) / angleStep + getSkinnable().getMinValue();
            }

            valueText.setText(limitString + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            if (getSkinnable().isLcdVisible()) {
                valueText.setTranslateX((0.691 * size - valueText.getLayoutBounds().getWidth()));
            } else {
                valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
            }

            // Check min- and maxMeasuredValue
            if (currentValue < getSkinnable().getMinMeasuredValue()) {
                getSkinnable().setMinMeasuredValue(currentValue);
            }
            if (currentValue > getSkinnable().getMaxMeasuredValue()) {
                getSkinnable().setMaxMeasuredValue(currentValue);
            }

            // Check sections for value and fire section events
            if (getSkinnable().getCheckSectionsForValue()) {
                for (Section section : getSkinnable().getSections()) { section.checkForValue(currentValue); }
            }

            // Check areas for value and fire section events
            if (getSkinnable().getCheckAreasForValue()) {
                for (Section area : getSkinnable().getAreas()) { area.checkForValue(currentValue); }
            }
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            ledCanvas.setManaged(getSkinnable().isLedVisible());
            ledCanvas.setVisible(getSkinnable().isLedVisible());

            titleText.setVisible(!getSkinnable().getTitle().isEmpty());
            titleText.setManaged(!getSkinnable().getTitle().isEmpty());

            subTitleText.setVisible(!getSkinnable().getSubTitle().isEmpty());
            subTitleText.setManaged(!getSkinnable().getSubTitle().isEmpty());

            unitText.setVisible(!getSkinnable().getUnit().isEmpty());
            unitText.setManaged(!getSkinnable().getUnit().isEmpty());

            valueText.setManaged(getSkinnable().isValueVisible());
            valueText.setVisible(getSkinnable().isValueVisible());

            lcd.setManaged(getSkinnable().isLcdVisible() && getSkinnable().isValueVisible());
            lcd.setVisible(getSkinnable().isLcdVisible() && getSkinnable().isValueVisible());

            markerMap.values().forEach(shape -> {
                shape.setManaged(getSkinnable().areMarkersVisible());
                shape.setVisible(getSkinnable().areMarkersVisible());
            });

            threshold.setManaged(getSkinnable().isThresholdVisible());
            threshold.setVisible(getSkinnable().isThresholdVisible());
        } else if ("LED_BLINK".equals(EVENT_TYPE)) {
            if (getSkinnable().isLedVisible()) { drawLed(); }
        } else if ("RECALC".equals(EVENT_TYPE)) {
            if (getSkinnable().isAutoScale()) getSkinnable().calcAutoScale();
            angleStep = getSkinnable().getAngleStep();
            needleRotate.setAngle((180 - getSkinnable().getStartAngle()) + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
            if (getSkinnable().getValue() < getSkinnable().getMinValue()) {
                getSkinnable().setValue(getSkinnable().getMinValue());
                oldValue = getSkinnable().getMinValue();
            }
            if (getSkinnable().getValue() > getSkinnable().getMaxValue()) {
                getSkinnable().setValue(getSkinnable().getMaxValue());
                oldValue = getSkinnable().getMaxValue();
            }
            resize();
            redraw();
        } else if ("INTERACTIVITY".equals(EVENT_TYPE)) {
            if (getSkinnable().isInteractive()) {
                knobCanvas.setOnMousePressed(mouseHandler);
                knobCanvas.setOnMouseReleased(mouseHandler);
                if (!getSkinnable().getButtonTooltipText().isEmpty()) {
                    buttonTooltip.setText(getSkinnable().getButtonTooltipText());
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
        final EventType TYPE = EVENT.getEventType();
        if (MouseEvent.MOUSE_PRESSED == TYPE) {
            getSkinnable().fireButtonEvent(getSkinnable().BUTTON_PRESSED_EVENT);
            drawKnob(true);
        } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
            getSkinnable().fireButtonEvent(getSkinnable().BUTTON_RELEASED_EVENT);
            drawKnob(false);
        }
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle() {
        angleStep          = getSkinnable().getAngleStep();
        double startAngle  = 180 - getSkinnable().getStartAngle();
        double targetAngle;
        if (ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection()) {
            targetAngle = startAngle + (getSkinnable().getCurrentValue() - getSkinnable().getMinValue()) * angleStep;
            targetAngle = clamp(startAngle, startAngle + getSkinnable().getAngleRange(), targetAngle);
        } else {
            targetAngle = startAngle - (getSkinnable().getCurrentValue() - getSkinnable().getMinValue()) * angleStep;
            targetAngle = clamp(startAngle - getSkinnable().getAngleRange(), startAngle, targetAngle);
        }
        needleRotate.setAngle(targetAngle);
    }

    private void drawTickMarks() {
        ticksAndSections.setLineCap(StrokeLineCap.BUTT);
        double               sinValue;
        double               cosValue;
        double               startAngle           = getSkinnable().getStartAngle();
        double               angleRange           = getSkinnable().getAngleRange();
        double               centerX              = size * 0.5;
        double               centerY              = size * 0.5;
        int                  decimals             = Double.compare(Math.abs(getSkinnable().getRange()), 10d) < 0 ? 1 : 0;
        double               minValue             = getSkinnable().getMinValue();
        double               minorTickSpace       = getSkinnable().getMinorTickSpace();
        double               tmpAngleStep         = angleStep * minorTickSpace;
        TickLabelOrientation tickLabelOrientation = getSkinnable().getTickLabelOrientation();
        TickLabelLocation    tickLabelLocation    = getSkinnable().getTickLabelLocation();
        BigDecimal           minorTickSpaceBD     = BigDecimal.valueOf(minorTickSpace);
        BigDecimal           majorTickSpaceBD     = BigDecimal.valueOf(getSkinnable().getMajorTickSpace());
        BigDecimal           mediumCheck2         = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal           mediumCheck5         = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal           counterBD            = BigDecimal.valueOf(minValue);
        double               counter              = minValue;

        List<Section> tickMarkSections         = getSkinnable().getTickMarkSections();
        List<Section> tickLabelSections        = getSkinnable().getTickLabelSections();
        Color         tickMarkColor            = getSkinnable().getTickMarkColor();
        Color         majorTickMarkColor       = getSkinnable().getMajorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : getSkinnable().getMajorTickMarkColor();
        Color         mediumTickMarkColor      = getSkinnable().getMediumTickMarkColor().equals(tickMarkColor) ? tickMarkColor : getSkinnable().getMediumTickMarkColor();
        Color         minorTickMarkColor       = getSkinnable().getMinorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : getSkinnable().getMinorTickMarkColor();
        Color         tickLabelColor           = getSkinnable().getTickLabelColor();
        Color         zeroColor                = getSkinnable().getZeroColor();
        boolean       isNotZero                = true;
        TickMarkType  majorTickMarkType        = getSkinnable().getMajorTickMarkType();
        TickMarkType  mediumTickMarkType       = getSkinnable().getMediumTickMarkType();
        TickMarkType  minorTickMarkType        = getSkinnable().getMinorTickMarkType();
        boolean       tickMarkSectionsVisible  = getSkinnable().areTickMarkSectionsVisible();
        boolean       tickLabelSectionsVisible = getSkinnable().areTickLabelSectionsVisible();
        boolean       majorTickMarksVisible    = getSkinnable().areMajorTickMarksVisible();
        boolean       mediumTickMarksVisible   = getSkinnable().areMediumTickMarksVisible();
        boolean       minorTickMarksVisible    = getSkinnable().areMinorTickMarksVisible();
        boolean       tickLabelsVisible        = getSkinnable().areTickLabelsVisible();
        double        textDisplacementFactor   = majorTickMarkType == TickMarkType.DOT ? (TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.95 : 1.05) : 1.0;
        double        majorDotSize;
        double        majorHalfDotSize;
        double        mediumDotSize;
        double        mediumHalfDotSize;
        double        minorDotSize;
        double        minorHalfDotSize;

        double orthTextFactor;
        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
            orthTextFactor    = Gauge.TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.45 * textDisplacementFactor : 0.45 * textDisplacementFactor;
            majorDotSize      = 0.02 * size;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01375 * size;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0075 * size;
            minorHalfDotSize  = minorDotSize * 0.5;
        } else {
            orthTextFactor    = Gauge.TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.38 * textDisplacementFactor : 0.37 * textDisplacementFactor;
            majorDotSize      = 0.025 * size;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01875 * size;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0125 * size;
            minorHalfDotSize  = minorDotSize * 0.5;
        };

        boolean fullRange  = (minValue < 0 && getSkinnable().getMaxValue() > 0);
        double  tickLabelFontSize = decimals == 0 ? 0.054 * size : 0.051 * size;
        double  tickMarkFontSize  = decimals == 0 ? 0.047 * size: 0.044 * size;
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

        double triangleMajorInnerAngle1;
        double triangleMajorInnerAngle2;
        double triangleMajorOuterAngle1;
        double triangleMajorOuterAngle2;
        double triangleMajorInnerPoint1X;
        double triangleMajorInnerPoint1Y;
        double triangleMajorInnerPoint2X;
        double triangleMajorInnerPoint2Y;
        double triangleMajorOuterPoint1X;
        double triangleMajorOuterPoint1Y;
        double triangleMajorOuterPoint2X;
        double triangleMajorOuterPoint2Y;

        double triangleMediumInnerAngle1;
        double triangleMediumInnerAngle2;
        double triangleMediumOuterAngle1;
        double triangleMediumOuterAngle2;
        double triangleMediumInnerPoint1X;
        double triangleMediumInnerPoint1Y;
        double triangleMediumInnerPoint2X;
        double triangleMediumInnerPoint2Y;
        double triangleMediumOuterPoint1X;
        double triangleMediumOuterPoint1Y;
        double triangleMediumOuterPoint2X;
        double triangleMediumOuterPoint2Y;

        double triangleMinorInnerAngle1;
        double triangleMinorInnerAngle2;
        double triangleMinorOuterAngle1;
        double triangleMinorOuterAngle2;
        double triangleMinorInnerPoint1X;
        double triangleMinorInnerPoint1Y;
        double triangleMinorInnerPoint2X;
        double triangleMinorInnerPoint2Y;
        double triangleMinorOuterPoint1X;
        double triangleMinorOuterPoint1Y;
        double triangleMinorOuterPoint2X;
        double triangleMinorOuterPoint2Y;


        // Main loop
        ScaleDirection scaleDirection = getSkinnable().getScaleDirection();
        BigDecimal tmpStepBD = new BigDecimal(tmpAngleStep);
        tmpStepBD = tmpStepBD.setScale(3, BigDecimal.ROUND_HALF_UP);
        double tmpStep = tmpStepBD.doubleValue();
        double angle   = 0;
        for (double i = 0 ; Double.compare(-angleRange - tmpStep, i) <= 0 ; i -= tmpStep) {
            switch (scaleDirection) {
                case CLOCKWISE        : if (Double.compare(-angleRange - tmpAngleStep, angle) < 0) break;
                    break;
                case COUNTER_CLOCKWISE: if (Double.compare(angleRange + tmpAngleStep, angle) > 0) break;
                    break;
            }

            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            switch(tickLabelLocation) {
                case OUTSIDE:
                    innerPointX                = centerX + size * 0.3585 * sinValue;
                    innerPointY                = centerY + size * 0.3585 * cosValue;
                    innerMediumPointX          = centerX + size * 0.3585 * sinValue;
                    innerMediumPointY          = centerY + size * 0.3585 * cosValue;
                    innerMinorPointX           = centerX + size * 0.3585 * sinValue;
                    innerMinorPointY           = centerY + size * 0.3585 * cosValue;
                    outerPointX                = centerX + size * 0.4105 * sinValue;
                    outerPointY                = centerY + size * 0.4105 * cosValue;
                    outerMediumPointX          = centerX + size * 0.4045 * sinValue;
                    outerMediumPointY          = centerY + size * 0.4045 * cosValue;
                    outerMinorPointX           = centerX + size * 0.3975 * sinValue;
                    outerMinorPointY           = centerY + size * 0.3975 * cosValue;
                    textPointX                 = centerX + size * orthTextFactor * sinValue;
                    textPointY                 = centerY + size * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + size * 0.3685 * sinValue;
                    dotCenterY                 = centerY + size * 0.3685 * cosValue;
                    dotMediumCenterX           = centerX + size * 0.365375 * sinValue;
                    dotMediumCenterY           = centerY + size * 0.365375 * cosValue;
                    dotMinorCenterX            = centerX + size * 0.36225 * sinValue;
                    dotMinorCenterY            = centerY + size * 0.36225 * cosValue;
                    tickLabelTickMarkX         = centerX + size * 0.3805 * sinValue;
                    tickLabelTickMarkY         = centerY + size * 0.3805 * cosValue;

                    triangleMajorInnerAngle1   = Math.toRadians(angle - 1.2 + startAngle);
                    triangleMajorInnerAngle2   = Math.toRadians(angle + 1.2 + startAngle);
                    triangleMajorOuterAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMajorOuterAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMajorInnerPoint1X  = centerX + size * 0.3585 * Math.sin(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint1Y  = centerY + size * 0.3585 * Math.cos(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint2X  = centerX + size * 0.3585 * Math.sin(triangleMajorInnerAngle2);
                    triangleMajorInnerPoint2Y  = centerY + size * 0.3585 * Math.cos(triangleMajorInnerAngle2);
                    triangleMajorOuterPoint1X  = centerX + size * 0.4105 * Math.sin(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint1Y  = centerY + size * 0.4105 * Math.cos(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint2X  = centerX + size * 0.4105 * Math.sin(triangleMajorOuterAngle2);
                    triangleMajorOuterPoint2Y  = centerY + size * 0.4105 * Math.cos(triangleMajorOuterAngle2);

                    triangleMediumInnerAngle1  = Math.toRadians(angle - 1.0 + startAngle);
                    triangleMediumInnerAngle2  = Math.toRadians(angle + 1.0 + startAngle);
                    triangleMediumOuterAngle1  = Math.toRadians(angle - 0.7 + startAngle);
                    triangleMediumOuterAngle2  = Math.toRadians(angle + 0.7 + startAngle);
                    triangleMediumInnerPoint1X = centerX + size * 0.3585 * Math.sin(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint1Y = centerY + size * 0.3585 * Math.cos(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint2X = centerX + size * 0.3585 * Math.sin(triangleMediumInnerAngle2);
                    triangleMediumInnerPoint2Y = centerY + size * 0.3585 * Math.cos(triangleMediumInnerAngle2);
                    triangleMediumOuterPoint1X = centerX + size * 0.3985 * Math.sin(triangleMajorOuterAngle1);
                    triangleMediumOuterPoint1Y = centerY + size * 0.3985 * Math.cos(triangleMediumOuterAngle1);
                    triangleMediumOuterPoint2X = centerX + size * 0.3985 * Math.sin(triangleMediumOuterAngle2);
                    triangleMediumOuterPoint2Y = centerY + size * 0.3985 * Math.cos(triangleMediumOuterAngle2);

                    triangleMinorInnerAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMinorInnerAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMinorOuterAngle1   = Math.toRadians(angle - 0.6 + startAngle);
                    triangleMinorOuterAngle2   = Math.toRadians(angle + 0.6 + startAngle);
                    triangleMinorInnerPoint1X  = centerX + size * 0.3585 * Math.sin(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint1Y  = centerY + size * 0.3585 * Math.cos(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint2X  = centerX + size * 0.3585 * Math.sin(triangleMinorInnerAngle2);
                    triangleMinorInnerPoint2Y  = centerY + size * 0.3585 * Math.cos(triangleMinorInnerAngle2);
                    triangleMinorOuterPoint1X  = centerX + size * 0.3975 * Math.sin(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint1Y  = centerY + size * 0.3975 * Math.cos(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint2X  = centerX + size * 0.3975 * Math.sin(triangleMinorOuterAngle2);
                    triangleMinorOuterPoint2Y  = centerY + size * 0.3975 * Math.cos(triangleMinorOuterAngle2);
                    break;
                case INSIDE:
                default:
                    innerPointX                = centerX + size * 0.423 * sinValue;
                    innerPointY                = centerY + size * 0.423 * cosValue;
                    innerMediumPointX          = centerX + size * 0.43 * sinValue;
                    innerMediumPointY          = centerY + size * 0.43 * cosValue;
                    innerMinorPointX           = centerX + size * 0.436 * sinValue;
                    innerMinorPointY           = centerY + size * 0.436 * cosValue;
                    outerPointX                = centerX + size * 0.475 * sinValue;
                    outerPointY                = centerY + size * 0.475 * cosValue;
                    outerMediumPointX          = centerX + size * 0.475 * sinValue;
                    outerMediumPointY          = centerY + size * 0.475 * cosValue;
                    outerMinorPointX           = centerX + size * 0.475 * sinValue;
                    outerMinorPointY           = centerY + size * 0.475 * cosValue;
                    textPointX                 = centerX + size * orthTextFactor * sinValue;
                    textPointY                 = centerY + size * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + size * 0.4625 * sinValue;
                    dotCenterY                 = centerY + size * 0.4625 * cosValue;
                    dotMediumCenterX           = centerX + size * 0.465625 * sinValue;
                    dotMediumCenterY           = centerY + size * 0.465625 * cosValue;
                    dotMinorCenterX            = centerX + size * 0.46875 * sinValue;
                    dotMinorCenterY            = centerY + size * 0.46875 * cosValue;
                    tickLabelTickMarkX         = centerX + size * 0.445 * sinValue;
                    tickLabelTickMarkY         = centerY + size * 0.445 * cosValue;

                    triangleMajorInnerAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMajorInnerAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMajorOuterAngle1   = Math.toRadians(angle - 1.2 + startAngle);
                    triangleMajorOuterAngle2   = Math.toRadians(angle + 1.2 + startAngle);
                    triangleMajorInnerPoint1X  = centerX + size * 0.423 * Math.sin(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint1Y  = centerY + size * 0.423 * Math.cos(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint2X  = centerX + size * 0.423 * Math.sin(triangleMajorInnerAngle2);
                    triangleMajorInnerPoint2Y  = centerY + size * 0.423 * Math.cos(triangleMajorInnerAngle2);
                    triangleMajorOuterPoint1X  = centerX + size * 0.475 * Math.sin(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint1Y  = centerY + size * 0.475 * Math.cos(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint2X  = centerX + size * 0.475 * Math.sin(triangleMajorOuterAngle2);
                    triangleMajorOuterPoint2Y  = centerY + size * 0.475 * Math.cos(triangleMajorOuterAngle2);

                    triangleMediumInnerAngle1  = Math.toRadians(angle - 0.7 + startAngle);
                    triangleMediumInnerAngle2  = Math.toRadians(angle + 0.7 + startAngle);
                    triangleMediumOuterAngle1  = Math.toRadians(angle - 1.0 + startAngle);
                    triangleMediumOuterAngle2  = Math.toRadians(angle + 1.0 + startAngle);
                    triangleMediumInnerPoint1X = centerX + size * 0.435 * Math.sin(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint1Y = centerY + size * 0.435 * Math.cos(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint2X = centerX + size * 0.435 * Math.sin(triangleMediumInnerAngle2);
                    triangleMediumInnerPoint2Y = centerY + size * 0.435 * Math.cos(triangleMediumInnerAngle2);
                    triangleMediumOuterPoint1X = centerX + size * 0.475 * Math.sin(triangleMajorOuterAngle1);
                    triangleMediumOuterPoint1Y = centerY + size * 0.475 * Math.cos(triangleMediumOuterAngle1);
                    triangleMediumOuterPoint2X = centerX + size * 0.475 * Math.sin(triangleMediumOuterAngle2);
                    triangleMediumOuterPoint2Y = centerY + size * 0.475 * Math.cos(triangleMediumOuterAngle2);

                    triangleMinorInnerAngle1   = Math.toRadians(angle - 0.6 + startAngle);
                    triangleMinorInnerAngle2   = Math.toRadians(angle + 0.6 + startAngle);
                    triangleMinorOuterAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMinorOuterAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMinorInnerPoint1X  = centerX + size * 0.440 * Math.sin(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint1Y  = centerY + size * 0.440 * Math.cos(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint2X  = centerX + size * 0.440 * Math.sin(triangleMinorInnerAngle2);
                    triangleMinorInnerPoint2Y  = centerY + size * 0.440 * Math.cos(triangleMinorInnerAngle2);
                    triangleMinorOuterPoint1X  = centerX + size * 0.475 * Math.sin(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint1Y  = centerY + size * 0.475 * Math.cos(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint2X  = centerX + size * 0.475 * Math.sin(triangleMinorOuterAngle2);
                    triangleMinorOuterPoint2Y  = centerY + size * 0.475 * Math.cos(triangleMinorOuterAngle2);
                    break;
            }

            // Set the general tickmark color
            ticksAndSections.setStroke(tickMarkColor);
            ticksAndSections.setFill(tickMarkColor);

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0d) == 0) {
                // Draw major tick mark
                isNotZero = Double.compare(0d, counter) != 0;
                TickMarkType tickMarkType = null;
                if (majorTickMarksVisible) {
                    ticksAndSections.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    ticksAndSections.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    ticksAndSections.setLineWidth(size * 0.0055);
                    tickMarkType = majorTickMarkType;
                } else if (minorTickMarksVisible) {
                    ticksAndSections.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    ticksAndSections.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    ticksAndSections.setLineWidth(size * 0.00225);
                    tickMarkType = minorTickMarkType;
                }
                if (fullRange && !isNotZero) {
                    ticksAndSections.setFill(zeroColor);
                    ticksAndSections.setStroke(zeroColor);
                }

                switch (tickMarkType) {
                    case TRIANGLE:
                        if (majorTickMarksVisible) {
                            drawTriangle(ticksAndSections, triangleMajorInnerPoint1X, triangleMajorInnerPoint1Y, triangleMajorInnerPoint2X, triangleMajorInnerPoint2Y,
                                         triangleMajorOuterPoint1X, triangleMajorOuterPoint1Y, triangleMajorOuterPoint2X, triangleMajorOuterPoint2Y);
                        } else if (minorTickMarksVisible) {
                            drawTriangle(ticksAndSections, triangleMinorInnerPoint1X, triangleMinorInnerPoint1Y, triangleMinorInnerPoint2X, triangleMinorInnerPoint2Y,
                                         triangleMinorOuterPoint1X, triangleMinorOuterPoint1Y, triangleMinorOuterPoint2X, triangleMinorOuterPoint2Y);
                        }
                        break;
                    case DOT:
                        if (majorTickMarksVisible) {
                            drawDot(ticksAndSections, dotCenterX - majorHalfDotSize, dotCenterY - majorHalfDotSize, majorDotSize);
                        } else if (minorTickMarksVisible) {
                            drawDot(ticksAndSections, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                        }
                        break;
                    case TICK_LABEL:
                        if (majorTickMarksVisible) {
                            ticksAndSections.save();
                            ticksAndSections.translate(tickLabelTickMarkX, tickLabelTickMarkY);

                            Helper.rotateContextForText(ticksAndSections, startAngle, angle, tickLabelOrientation);

                            ticksAndSections.setFont(isNotZero ? tickMarkFont : tickMarkZeroFont);
                            ticksAndSections.setTextAlign(TextAlignment.CENTER);
                            ticksAndSections.setTextBaseline(VPos.CENTER);
                            ticksAndSections.fillText(String.format(Locale.US, "%." + decimals + "f", counter), 0, 0);
                            ticksAndSections.restore();
                        }
                        break;
                    case LINE:
                    default:
                        if (majorTickMarksVisible) {
                            drawLine(ticksAndSections, innerPointX, innerPointY, outerPointX, outerPointY);
                        } else if (minorTickMarksVisible) {
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                drawLine(ticksAndSections, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                drawLine(ticksAndSections, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                        }
                        break;
                }

                // Draw tick label text
                if (tickLabelsVisible) {
                    ticksAndSections.save();
                    ticksAndSections.translate(textPointX, textPointY);

                    Helper.rotateContextForText(ticksAndSections, startAngle, angle, tickLabelOrientation);
                    ticksAndSections.setFont(isNotZero ? tickLabelFont : tickLabelZeroFont);
                    ticksAndSections.setTextAlign(TextAlignment.CENTER);
                    ticksAndSections.setTextBaseline(VPos.CENTER);
                    if (isNotZero) {
                        ticksAndSections.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                    } else {
                        if (fullRange) ticksAndSections.setFill(zeroColor);
                    }
                    ticksAndSections.fillText(String.format(Locale.US, "%." + decimals + "f", counter), 0, 0);
                    ticksAndSections.restore();
                }
            } else if (mediumTickMarksVisible &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0d) != 0d &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0d) == 0d) {
                // Draw medium tick mark
                ticksAndSections.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                ticksAndSections.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                switch(mediumTickMarkType) {
                    case TRIANGLE:
                        drawTriangle(ticksAndSections, triangleMediumInnerPoint1X, triangleMediumInnerPoint1Y, triangleMediumInnerPoint2X, triangleMediumInnerPoint2Y,
                                     triangleMediumOuterPoint1X, triangleMediumOuterPoint1Y, triangleMediumOuterPoint2X, triangleMediumOuterPoint2Y);
                        break;
                    case DOT:
                        drawDot(ticksAndSections, dotMediumCenterX - mediumHalfDotSize, dotMediumCenterY - mediumHalfDotSize, mediumDotSize);
                        break;
                    case LINE:
                    default:
                        ticksAndSections.setLineWidth(size * 0.0035);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            drawLine(ticksAndSections, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            drawLine(ticksAndSections, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                    break;
                }
            } else if (minorTickMarksVisible && Double.compare(counterBD.remainder(minorTickSpaceBD).doubleValue(), 0d) == 0) {
                // Draw minor tick mark
                if (TickMarkType.TICK_LABEL != majorTickMarkType) {
                    ticksAndSections.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    ticksAndSections.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    switch (minorTickMarkType) {
                        case TRIANGLE:
                            drawTriangle(ticksAndSections, triangleMinorInnerPoint1X, triangleMinorInnerPoint1Y, triangleMinorInnerPoint2X, triangleMinorInnerPoint2Y,
                                         triangleMinorOuterPoint1X, triangleMinorOuterPoint1Y, triangleMinorOuterPoint2X, triangleMinorOuterPoint2Y);
                            break;
                        case DOT:
                            drawDot(ticksAndSections, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                            break;
                        case LINE:
                        default:
                            ticksAndSections.setLineWidth(size * 0.00225);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                drawLine(ticksAndSections, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                drawLine(ticksAndSections, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                    }
                }
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
            angle     = ScaleDirection.CLOCKWISE == scaleDirection ? (angle - tmpAngleStep) : (angle + tmpAngleStep);
        }
    }

    private void drawTriangle(final GraphicsContext CTX,
                              final double PI1X, final double PI1Y, final double PI2X, final double PI2Y,
                              final double PO1X, final double PO1Y, final double PO2X, final double PO2Y) {
        CTX.beginPath();
        CTX.moveTo(PI2X, PI2Y);
        CTX.lineTo(PI1X, PI1Y);
        CTX.lineTo(PO1X, PO1Y);
        CTX.lineTo(PO2X, PO2Y);
        CTX.closePath();
        CTX.fill();
    }
    private void drawDot(final GraphicsContext CTX, final double CENTER_X, final double CENTER_Y, final double SIZE) {
        CTX.fillOval(CENTER_X, CENTER_Y, SIZE, SIZE);
    }
    private void drawLine(final GraphicsContext CTX, final double P1X, final double P1Y, final double P2X, final double P2Y) {
        CTX.strokeLine(P1X, P1Y, P2X, P2Y);
    }

    private void drawSections() {
        if (getSkinnable().getSections().isEmpty()) return;
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        final double         xy              = TickLabelLocation.OUTSIDE == tickLabelLocation ? (size - 0.77 * size) * 0.5 : (size - 0.897 * size) * 0.5;
        final double         wh              = TickLabelLocation.OUTSIDE == tickLabelLocation ? size * 0.77 : size * 0.897;
        final double         MIN_VALUE       = getSkinnable().getMinValue();
        final double         MAX_VALUE       = getSkinnable().getMaxValue();
        final double         OFFSET          = 90 - getSkinnable().getStartAngle();
        final ScaleDirection SCALE_DIRECTION = getSkinnable().getScaleDirection();
        IntStream.range(0, getSkinnable().getSections().size()).parallel().forEachOrdered(
            i -> {
                final Section SECTION = getSkinnable().getSections().get(i);
                final double SECTION_START_ANGLE;
                if (Double.compare(SECTION.getStart(), MAX_VALUE) <= 0 && Double.compare(SECTION.getStop(), MIN_VALUE) >= 0) {
                    if (Double.compare(SECTION.getStart(), MIN_VALUE) < 0 && Double.compare(SECTION.getStop(), MAX_VALUE) < 0) {
                        SECTION_START_ANGLE = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? MIN_VALUE * angleStep : -MIN_VALUE * angleStep;
                    } else {
                        SECTION_START_ANGLE = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? (SECTION.getStart() - MIN_VALUE) * angleStep : -(SECTION.getStart() - MIN_VALUE) * angleStep;
                    }
                    final double SECTION_ANGLE_EXTEND;
                    if (Double.compare(SECTION.getStop(), MAX_VALUE) > 0) {
                        SECTION_ANGLE_EXTEND = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? (MAX_VALUE - SECTION.getStart()) * angleStep : -(MAX_VALUE - SECTION.getStart()) * angleStep;
                    } else {
                        SECTION_ANGLE_EXTEND = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? (SECTION.getStop() - SECTION.getStart()) * angleStep : -(SECTION.getStop() - SECTION.getStart()) * angleStep;
                    }
                    ticksAndSections.save();
                    ticksAndSections.setStroke(SECTION.getColor());
                    ticksAndSections.setLineWidth(size * 0.052);
                    ticksAndSections.setLineCap(StrokeLineCap.BUTT);
                    ticksAndSections.strokeArc(xy, xy, wh, wh, -(OFFSET + SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.OPEN);
                    ticksAndSections.restore();
                }
            }
        );
    }

    private void drawAreas() {
        if (getSkinnable().getAreas().isEmpty()) return;
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        final double         xy              = TickLabelLocation.OUTSIDE == tickLabelLocation ? (size - 0.821 * size) * 0.5 : (size - 0.95 * size) * 0.5;
        final double         wh              = TickLabelLocation.OUTSIDE == tickLabelLocation ? size * 0.821 : size * 0.95;
        final double         MIN_VALUE       = getSkinnable().getMinValue();
        final double         MAX_VALUE       = getSkinnable().getMaxValue();
        final double         OFFSET          = 90 - getSkinnable().getStartAngle();
        final ScaleDirection SCALE_DIRECTION = getSkinnable().getScaleDirection();

        IntStream.range(0, getSkinnable().getAreas().size()).parallel().forEachOrdered(
            i -> {
                final Section AREA = getSkinnable().getAreas().get(i);
                final double AREA_START_ANGLE;
                if (Double.compare(AREA.getStart(), MAX_VALUE) <= 0 && Double.compare(AREA.getStop(), MIN_VALUE) >= 0) {
                    if (AREA.getStart() < MIN_VALUE && AREA.getStop() < MAX_VALUE) {
                        AREA_START_ANGLE = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? MIN_VALUE * angleStep : -MIN_VALUE * angleStep;
                    } else {
                        AREA_START_ANGLE = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? (AREA.getStart() - MIN_VALUE) * angleStep : -(AREA.getStart() - MIN_VALUE) * angleStep;
                    }
                    final double AREA_ANGLE_EXTEND;
                    if (AREA.getStop() > MAX_VALUE) {
                        AREA_ANGLE_EXTEND = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? (MAX_VALUE - AREA.getStart()) * angleStep : -(MAX_VALUE - AREA.getStart()) * angleStep;
                    } else {
                        AREA_ANGLE_EXTEND = ScaleDirection.CLOCKWISE == SCALE_DIRECTION ? (AREA.getStop() - AREA.getStart()) * angleStep : -(AREA.getStop() - AREA.getStart()) * angleStep;
                    }
                    ticksAndSections.save();
                    ticksAndSections.setFill(AREA.getColor());
                    ticksAndSections.fillArc(xy, xy, wh, wh, -(OFFSET + AREA_START_ANGLE), - AREA_ANGLE_EXTEND, ArcType.ROUND);
                    ticksAndSections.restore();
                }
            }
                                                                                      );
    }

    private void drawLed() {
        led.clearRect(0, 0, ledSize, ledSize);

        boolean isFlatLed = LedType.FLAT == getSkinnable().getLedType();

        if (!isFlatLed) {
            led.setFill(ledFramePaint);
            led.fillOval(0, 0, ledSize, ledSize);
        } else {
            double lineWidth = 0.0037037 * size;
            led.setStroke(ledFramePaint);
            led.setLineWidth(lineWidth);
            led.strokeOval(lineWidth, lineWidth, ledSize - 2 * lineWidth, ledSize - 2 * lineWidth);
        }

        led.save();
        if (getSkinnable().isLedOn()) {
            led.setEffect(ledOnShadow);
            led.setFill(ledOnPaint);
        } else {
            led.setEffect(ledOffShadow);
            led.setFill(ledOffPaint);
        }
        if (isFlatLed) {
            led.fillOval(0.2 * ledSize, 0.2 * ledSize, 0.6 * ledSize, 0.6 * ledSize);
        } else {
            led.fillOval(0.14 * ledSize, 0.14 * ledSize, 0.72 * ledSize, 0.72 * ledSize);
        }
        led.restore();

        led.setFill(ledHighlightPaint);
        led.fillOval(0.21 * ledSize, 0.21 * ledSize, 0.58 * ledSize, 0.58 * ledSize);
    }

    private void drawMarkers() {
        markerPane.getChildren().setAll(markerMap.values());
        markerPane.getChildren().add(threshold);
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double         markerSize     = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.0125 * size : 0.015 * size;
        double         pathHalf       = markerSize * 0.3;
        double         startAngle     = getSkinnable().getStartAngle();
        double         centerX        = size * 0.5;
        double         centerY        = size * 0.5;
        ScaleDirection scaleDirection = getSkinnable().getScaleDirection();
        if (getSkinnable().areMarkersVisible()) {
            markerMap.keySet().forEach(marker -> {
                Shape  shape = markerMap.get(marker);
                double valueAngle;
                if (ScaleDirection.CLOCKWISE == scaleDirection) {
                    valueAngle = startAngle - (marker.getValue() - getSkinnable().getMinValue()) * angleStep;
                } else {
                    valueAngle = startAngle + (marker.getValue() - getSkinnable().getMinValue()) * angleStep;
                }
                double sinValue = Math.sin(Math.toRadians(valueAngle));
                double cosValue = Math.cos(Math.toRadians(valueAngle));
                switch (marker.getMarkerType()) {
                    case TRIANGLE:
                        Path triangle = (Path) shape;
                        triangle.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                triangle.getElements().add(new MoveTo(centerX + size * 0.38 * sinValue, centerY + size * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                triangle.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                triangle.getElements().add(new MoveTo(centerX + size * 0.465 * sinValue, centerY + size * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                triangle.getElements().add(new ClosePath());
                                break;
                        }
                        break;
                    case DOT:
                        Circle dot = (Circle) shape;
                        dot.setRadius(markerSize);
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                dot.setCenterX(centerX + size * 0.3945 * sinValue);
                                dot.setCenterY(centerY + size * 0.3945 * cosValue);
                                break;
                            default:
                                dot.setCenterX(centerX + size * 0.449 * sinValue);
                                dot.setCenterY(centerY + size * 0.449 * cosValue);
                                break;
                        }
                        break;
                    case STANDARD:
                    default:
                        Path standard = (Path) shape;
                        standard.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                standard.getElements().add(new MoveTo(centerX + size * 0.38 * sinValue, centerY + size * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.4575 * sinValue, centerY + size * 0.4575 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.4575 * sinValue, centerY + size * 0.4575 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                standard.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                standard.getElements().add(new MoveTo(centerX + size * 0.465 * sinValue, centerY + size * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.386 * sinValue, centerY + size * 0.386 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.386 * sinValue, centerY + size * 0.386 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
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
                    markerTooltip = new Tooltip(String.join("", marker.getText(), "\n(", Double.toString(marker.getValue()), ")"));
                }
                markerTooltip.setTextAlignment(TextAlignment.CENTER);
                Tooltip.install(shape, markerTooltip);
                shape.setOnMousePressed(e -> marker.fireMarkerEvent(marker.MARKER_PRESSED_EVENT));
                shape.setOnMouseReleased(e -> marker.fireMarkerEvent(marker.MARKER_RELEASED_EVENT));
            });
        }

        if (getSkinnable().isThresholdVisible()) {
            // Draw threshold
            threshold.getElements().clear();
            double thresholdAngle;
            if (ScaleDirection.CLOCKWISE == scaleDirection) {
                thresholdAngle = startAngle - (getSkinnable().getThreshold() - getSkinnable().getMinValue()) * angleStep;
            } else {
                thresholdAngle = startAngle + (getSkinnable().getThreshold() - getSkinnable().getMinValue()) * angleStep;
            }
            double thresholdSize = clamp(3, 3.5, 0.01 * size);
            double sinValue      = Math.sin(Math.toRadians(thresholdAngle));
            double cosValue      = Math.cos(Math.toRadians(thresholdAngle));
            switch (tickLabelLocation) {
                case OUTSIDE:
                    threshold.getElements().add(new MoveTo(centerX + size * 0.38 * sinValue, centerY + size * 0.38 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.34 * sinValue, centerY + size * 0.34 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.34 * sinValue, centerY + size * 0.34 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
                case INSIDE:
                default:
                    threshold.getElements().add(new MoveTo(centerX + size * 0.465 * sinValue, centerY + size * 0.465 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.425 * sinValue, centerY + size * 0.425 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.425 * sinValue, centerY + size * 0.425 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
            }
            threshold.setFill(getSkinnable().getThresholdColor());
            threshold.setStroke(getSkinnable().getTickMarkColor());
        }
    }

    private void updateMarkers() {
        markerMap.clear();
        getSkinnable().getMarkers().forEach(marker -> {
            switch(marker.getMarkerType()) {
                case TRIANGLE:
                    markerMap.put(marker, new Path());
                    break;
                case DOT:
                    markerMap.put(marker, new Circle());
                    break;
                case STANDARD:
                default:
                    markerMap.put(marker, new Path());
                    break;
            }
        });
    }

    private void drawKnob(final boolean PRESSED) {
        knobCanvas.setCache(false);
        double w = knobCanvas.getWidth();
        double h = knobCanvas.getHeight();
        knob.clearRect(0, 0, w, h);

        Color  knobColor = getSkinnable().getKnobColor();
        double hue       = knobColor.getHue();
        double sat       = knobColor.getSaturation();
        double alp       = knobColor.getOpacity();
        double gradTop;
        double gradBot;

        switch (getSkinnable().getKnobType()) {
            case PLAIN:
                knob.setFill(new LinearGradient(0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, Color.rgb(180,180,180)),
                                               new Stop(0.46, Color.rgb(63,63,63)),
                                               new Stop(1.0, Color.rgb(40,40,40))));
                knob.fillOval(0, 0, w, h);

                knob.setFill(new LinearGradient(0, 0.11764706 * h, 0, 0.76470588 * h, false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? 0.9 : 1.0, alp)),
                                                new Stop(0.01, Color.hsb(hue, sat, PRESSED ? 0.75 : 0.85, alp)),
                                                new Stop(0.5, Color.hsb(hue, sat, PRESSED ? 0.4 : 0.5, alp)),
                                                new Stop(0.51, Color.hsb(hue, sat, PRESSED ? 0.35 : 0.45, alp)),
                                                new Stop(1.0, Color.hsb(hue, sat, PRESSED ? 0.7 : 0.8, alp))));
                knob.fillOval(w * 0.11764706, h * 0.11764706, w - w * 0.23529412, h - h * 0.23529412);

                knob.setFill(new RadialGradient(0, 0, 0.5 * w, 0.47 * h, w * 0.38, false, CycleMethod.NO_CYCLE,
                                               new Stop(0, Color.TRANSPARENT),
                                               new Stop(0.76, Color.TRANSPARENT),
                                               new Stop(1.0, Color.rgb(0, 0, 0, PRESSED ? 0.5 : 0.2))));
                knob.fillOval(w * 0.11764706, h * 0.11764706, w - w * 0.23529412, h - h * 0.23529412);
                break;
            case METAL:
                knob.setFill(new LinearGradient(0, 0, 0, h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(92,95,101)),
                                                new Stop(0.47, Color.rgb(46,49,53)),
                                                new Stop(1.0, Color.rgb(22,23,26))));
                knob.fillOval(0, 0, w, h);

                knob.setFill(new LinearGradient(0, 0.058823529411764705 * h, 0, 0.9411764705882353 * h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? 0.7 : 0.9, alp)),
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? 0.3 : 0.5, alp))));
                knob.fillOval(0.05882353 * w, 0.05882353 * h, w * 0.88235294, h * 0.88235294);

                knob.beginPath();
                knob.moveTo(0.17647058823529413 * w, 0.8235294117647058 * h);
                knob.bezierCurveTo(0.29411764705882354 * w, 0.8823529411764706 * h, 0.35294117647058826 * w, 0.9411764705882353 * h, 0.5294117647058824 * w, 0.9411764705882353 * h);
                knob.bezierCurveTo(0.6470588235294118 * w, 0.9411764705882353 * h, 0.7058823529411765 * w, 0.8823529411764706 * h, 0.8235294117647058 * w, 0.8235294117647058 * h);
                knob.bezierCurveTo(0.7647058823529411 * w, 0.7058823529411765 * h, 0.6470588235294118 * w, 0.5882352941176471 * h, 0.5294117647058824 * w, 0.5882352941176471 * h);
                knob.bezierCurveTo(0.35294117647058826 * w, 0.5882352941176471 * h, 0.23529411764705882 * w, 0.7058823529411765 * h, 0.17647058823529413 * w, 0.8235294117647058 * h);
                knob.closePath();
                knob.setFill(new RadialGradient(0, 0,
                                                0.47058823529411764 * w, 0.8823529411764706 * h,
                                                0.3235294117647059 * w,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(255, 255, 255, PRESSED ? 0.3 : 0.6)),
                                                new Stop(1.0, Color.TRANSPARENT)));
                knob.fill();

                knob.beginPath();
                knob.moveTo(0.058823529411764705 * w, 0.29411764705882354 * h);
                knob.bezierCurveTo(0.17647058823529413 * w, 0.35294117647058826 * h, 0.35294117647058826 * w, 0.35294117647058826 * h, 0.5294117647058824 * w, 0.35294117647058826 * h);
                knob.bezierCurveTo(0.6470588235294118 * w, 0.35294117647058826 * h, 0.8235294117647058 * w, 0.35294117647058826 * h, 0.9411764705882353 * w, 0.29411764705882354 * h);
                knob.bezierCurveTo(0.8823529411764706 * w, 0.11764705882352941 * h, 0.7058823529411765 * w, 0.0 * h, 0.5294117647058824 * w, 0.0 * h);
                knob.bezierCurveTo(0.29411764705882354 * w, 0.0 * h, 0.11764705882352941 * w, 0.11764705882352941 * h, 0.058823529411764705 * w, 0.29411764705882354 * h);
                knob.closePath();
                knob.setFill(new RadialGradient(0, 0,
                                                0.47058823529411764 * w, 0.0,
                                                0.4411764705882353 * w,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(255, 255, 255, PRESSED ? 0.45 : 0.75)),
                                                new Stop(1.0, Color.TRANSPARENT)));
                knob.fill();

                knob.setFill(new LinearGradient(0.5294117647058824 * w, 0.23529411764705882 * h,
                                                0.5294117647058824 * w, 0.7647058823529411 * h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.BLACK),
                                                new Stop(1.0, Color.rgb(204, 204, 204))));
                knob.fillOval(0.23529412 * w, 0.23529412 * h, 0.52941176 * w, 0.52941176 * h);

                knob.setFill(new LinearGradient(0.5294117647058824 * w, 0.29411764705882354 * h,
                                                0.5294117647058824 * w, 0.7058823529411765 * h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(1,6,11)),
                                                new Stop(1.0, Color.rgb(50,52,56))));
                knob.fillOval(0.29411765 * w, 0.29411765 * h, 0.41176471 * w, 0.41176471 * h);
                break;
            case FLAT:
                double lineWidth = 0.00740741 * size;
                double knobSize  = w - 2 * lineWidth;
                knob.setFill(PRESSED ? knobColor.darker() : knobColor);
                knob.setStroke(PRESSED ? Color.WHITE.darker() : Color.WHITE);
                knob.setLineWidth(lineWidth);
                knob.fillOval(lineWidth, lineWidth, knobSize, knobSize);
                knob.strokeOval(lineWidth, lineWidth, knobSize, knobSize);
                break;
            case STANDARD:
            default:
                knob.setFill(new LinearGradient(0, 0, 0, h,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.rgb(133, 133, 133).brighter().brighter()),
                                                       new Stop(0.52, Color.rgb(133, 133, 133)),
                                                       new Stop(1.0, Color.rgb(133, 133, 133).darker().darker())));
                knob.fillOval(0, 0, w, h);
                gradTop = PRESSED ? h - size * 0.01 : size * 0.005;
                gradBot = PRESSED ? size * 0.005 : h - size * 0.01;
                knob.setFill(new LinearGradient(0,gradTop, 0, gradBot,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.hsb(hue, sat, 0.85, alp)),
                                                       new Stop(0.45, Color.hsb(hue, sat, 0.65, alp)),
                                                       new Stop(1.0, Color.hsb(hue, sat, 0.4, alp))));
                knob.fillOval(size * 0.005, size * 0.005, w - size * 0.01, h - size * 0.01);
                break;
        }
        knobCanvas.setCache(true);
        knobCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }

    private void resizeText() {
        double maxWidth = 0.3 * size;

        titleText.setFont(Fonts.robotoMedium(size * 0.06));
        titleText.setText(getSkinnable().getTitle());
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, 0.065, size); }
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(size * 0.26);

        unitText.setFont(Fonts.robotoRegular(size * 0.05));
        unitText.setText(getSkinnable().getUnit());
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, 0.055, size); }
        unitText.setTranslateX((size - unitText.getLayoutBounds().getWidth()) * 0.5);
        unitText.setTranslateY(size * 0.35);

        maxWidth = 0.25 * size;
        subTitleText.setFont(Fonts.robotoRegular(size * 0.05));
        subTitleText.setText(getSkinnable().getSubTitle());
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, 0.055, size); }
        subTitleText.setTranslateX((size - subTitleText.getLayoutBounds().getWidth()) * 0.5);
        subTitleText.setTranslateY(size * 0.76);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            backgroundInnerShadow.setOffsetX(0);
            backgroundInnerShadow.setOffsetY(size * 0.03);
            backgroundInnerShadow.setRadius(size * 0.04);

            background.setCenterX(center);
            background.setCenterY(center);
            background.setRadius(center);
            background.setStrokeWidth(1);
            background.setEffect(getSkinnable().isInnerShadowEnabled() ? backgroundInnerShadow : null);

            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);

            markerPane.setPrefSize(size, size);

            boolean isFlatLed = LedType.FLAT == getSkinnable().getLedType();
            ledSize = isFlatLed ? 0.05 * size : 0.06 * size;
            ledCanvas.setWidth(ledSize);
            ledCanvas.setHeight(ledSize);
            ledCanvas.relocate(0.65 * size, 0.47 * size);
            ledOffShadow = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            ledOnShadow  = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            if (!isFlatLed) ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, getSkinnable().getLedColor(), 0.36 * ledSize, 0, 0, 0));

            resizeText();

            if (getSkinnable().isLcdVisible()) {
                lcd.setWidth(0.4 * size);
                lcd.setHeight(0.114 * size);
                lcd.setArcWidth(0.0125 * size);
                lcd.setArcHeight(0.0125 * size);
                lcd.relocate((size - lcd.getWidth()) * 0.5, 0.583 * size);

                switch(getSkinnable().getLcdFont()) {
                    case LCD:
                        valueText.setFont(Fonts.digital(0.108 * size));
                        valueText.setTranslateY(0.64 * size);
                        break;
                    case DIGITAL:
                        valueText.setFont(Fonts.digitalReadout(0.105 * size));
                        valueText.setTranslateY(0.65 * size);
                        break;
                    case DIGITAL_BOLD:
                        valueText.setFont(Fonts.digitalReadoutBold(0.105 * size));
                        valueText.setTranslateY(0.65 * size);
                        break;
                    case ELEKTRA:
                        valueText.setFont(Fonts.elektra(0.1116 * size));
                        valueText.setTranslateY(0.645 * size);
                        break;
                    case STANDARD:
                    default:
                        valueText.setFont(Fonts.robotoMedium(0.09 * size));
                        valueText.setTranslateY(0.64 * size);
                        break;
                }
                valueText.setTranslateX((0.691 * size - valueText.getLayoutBounds().getWidth()));
            } else {
                valueText.setFont(Fonts.robotoMedium(size * 0.1));
                valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
                valueText.setTranslateY(size * 0.65);
            }
            
            double needleWidth  = size * getSkinnable().getNeedleSize().FACTOR;
            double needleHeight = TickLabelLocation.OUTSIDE == getSkinnable().getTickLabelLocation() ? size * 0.3965 : size * 0.455;

            needleMoveTo1.setX(0.25 * needleWidth); needleMoveTo1.setY(0.025423728813559324 * needleHeight);

            needleCubicCurveTo2.setControlX1(0.25 * needleWidth); needleCubicCurveTo2.setControlY1(0.00847457627118644 * needleHeight);
            needleCubicCurveTo2.setControlX2(0.375 * needleWidth); needleCubicCurveTo2.setControlY2(0);
            needleCubicCurveTo2.setX(0.5 * needleWidth); needleCubicCurveTo2.setY(0);

            needleCubicCurveTo3.setControlX1(0.625 * needleWidth); needleCubicCurveTo3.setControlY1(0);
            needleCubicCurveTo3.setControlX2(0.75 * needleWidth); needleCubicCurveTo3.setControlY2(0.00847457627118644 * needleHeight);
            needleCubicCurveTo3.setX(0.75 * needleWidth); needleCubicCurveTo3.setY(0.025423728813559324 * needleHeight);

            needleCubicCurveTo4.setControlX1(0.75 * needleWidth); needleCubicCurveTo4.setControlY1(0.025423728813559324 * needleHeight);
            needleCubicCurveTo4.setControlX2(needleWidth); needleCubicCurveTo4.setControlY2(needleHeight);
            needleCubicCurveTo4.setX(needleWidth); needleCubicCurveTo4.setY(needleHeight);

            needleLineTo5.setX(0); needleLineTo5.setY(needleHeight);

            needleCubicCurveTo6.setControlX1(0); needleCubicCurveTo6.setControlY1(needleHeight);
            needleCubicCurveTo6.setControlX2(0.25 * needleWidth); needleCubicCurveTo6.setControlY2(0.025423728813559324 * needleHeight);
            needleCubicCurveTo6.setX(0.25 * needleWidth); needleCubicCurveTo6.setY(0.025423728813559324 * needleHeight);

            needle.relocate((size - needle.getLayoutBounds().getWidth()) * 0.5, center - needle.getLayoutBounds().getHeight());
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight());

            knobCanvas.setWidth(size * 0.1);
            knobCanvas.setHeight(size * 0.1);
            knobCanvas.relocate(center - size * 0.05, center - size * 0.05);

            buttonTooltip.setText(getSkinnable().getButtonTooltipText());
        }
    }

    private void redraw() {
        shadowGroup.setEffect(getSkinnable().areShadowsEnabled() ? dropShadow : null);

        // Background stroke and fill
        background.setStroke(getSkinnable().getBorderPaint());
        background.setFill(getSkinnable().getBackgroundPaint());

        // Areas, Sections and Tick Marks
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, size, size);
        if (getSkinnable().areAreasVisible()) drawAreas();
        if (getSkinnable().areSectionsVisible()) drawSections();
        drawTickMarks();
        ticksAndSectionsCanvas.setCache(true);
        ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

        // LED
        if (getSkinnable().isLedVisible()) {
            final Color LED_COLOR = getSkinnable().getLedColor();
            switch(getSkinnable().getLedType()) {
                case FLAT:
                    ledFramePaint = Color.WHITE;
                    ledOnPaint = new LinearGradient(0, 0.25 * ledSize,
                                                    0, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR),
                                                    new Stop(1.0, LED_COLOR.deriveColor(0d, 1d, 0.5, 1d)));
                    ledOffPaint = new LinearGradient(0, 0.25 * ledSize,
                                                     0, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0d, 1d, 0.5, 1d)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0d, 1d, 0.13, 1d)));
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
                                                    new Stop(0.0, LED_COLOR.deriveColor(0d, 1d, 0.77, 1d)),
                                                    new Stop(0.49, LED_COLOR.deriveColor(0d, 1d, 0.5, 1d)),
                                                    new Stop(1.0, LED_COLOR));
                    ledOffPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                        0.74 * ledSize, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0d, 1d, 0.20, 1d)),
                                                     new Stop(0.49, LED_COLOR.deriveColor(0d, 1d, 0.13, 1d)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0d, 1d, 0.2, 1d)));
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
        if (getSkinnable().isLcdVisible() && getSkinnable().isValueVisible()) {
            LcdDesign lcdDesign = getSkinnable().getLcdDesign();
            LinearGradient lcdGradient = new LinearGradient(0, 1, 0, lcd.getHeight() - 1,
                                                            false, CycleMethod.NO_CYCLE,
                                                            new Stop(0, lcdDesign.BG0),
                                                            new Stop(0.03, lcdDesign.BG1),
                                                            new Stop(0.5, lcdDesign.BG2),
                                                            new Stop(0.5, lcdDesign.BG3),
                                                            new Stop(1.0, lcdDesign.BG4));
            Paint lcdFramePaint;
            if (lcdDesign.name().startsWith("FLAT")) {
                lcdFramePaint = Color.WHITE;
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
        titleText.setFill(getSkinnable().getTitleColor());
        unitText.setFill(getSkinnable().getUnitColor());
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        valueText.setFill(getSkinnable().isLcdVisible() ? getSkinnable().getLcdDesign().FG : getSkinnable().getValueColor());
        resizeText();

        // Needle
        Color needleColor = getSkinnable().getNeedleColor();
        switch(getSkinnable().getNeedleShape()) {
            case ROUND:
                needlePaint = new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                                 needle.getLayoutBounds().getMaxX(), 0,
                                                 false, CycleMethod.NO_CYCLE,
                                                 new Stop(0.0, needleColor.darker()),
                                                 new Stop(0.5, needleColor.brighter().brighter()),
                                                 new Stop(1.0, needleColor.darker()));
                needle.setStrokeWidth(0);
                needle.setStroke(Color.TRANSPARENT);
                break;
            case FLAT:
                needlePaint = needleColor;
                needle.setStrokeWidth(0.0037037 * size);
                needle.setStroke(Color.WHITE);
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
                needle.setStroke(Color.TRANSPARENT);
                break;
        }
        needle.setFill(needlePaint);

        // Knob
        drawKnob(false);

        // Markers
        drawMarkers();
        thresholdTooltip.setText("Threshold\n(" + String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getThreshold()) + ")");
    }
}
