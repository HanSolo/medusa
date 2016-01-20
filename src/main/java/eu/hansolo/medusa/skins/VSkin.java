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
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.TickLabelLocation;
import eu.hansolo.medusa.Gauge.TickLabelOrientation;
import eu.hansolo.medusa.Gauge.TickMarkType;
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.AngleConicalGradient;
import eu.hansolo.medusa.tools.Helper;
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
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;


/**
 * Created by hansolo on 19.01.16.
 */
public class VSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double             PREFERRED_WIDTH  = 125;
    private static final double             PREFERRED_HEIGHT = 250;
    private static final double             MINIMUM_WIDTH    = 50;
    private static final double             MINIMUM_HEIGHT   = 50;
    private static final double             MAXIMUM_WIDTH    = 1024;
    private static final double             MAXIMUM_HEIGHT   = 1024;
    private static final double             ASPECT_RATIO     = 2;
    private              Map<Marker, Shape> markerMap        = new ConcurrentHashMap<>();
    private double                   oldValue;
    private double                   width;
    private double                   height;
    private Pane                     pane;
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
    private Text                     unitText;
    private Text                     valueText;
    private double                   startAngle;
    private double                   angleRange;
    private double                   angleStep;
    private String                   limitString;
    private EventHandler<MouseEvent> mouseHandler;
    private Tooltip                  buttonTooltip;
    private Tooltip                  thresholdTooltip;
    private String                   formatString;
    private double                   minValue;
    private double                   maxValue;


    // ******************** Constructors **************************************
    public VSkin(Gauge gauge) {
        super(gauge);
        angleRange   = Helper.clamp(90d, 180d, gauge.getAngleRange());
        startAngle   = getStartAngle();
        angleStep    = angleRange / gauge.getRange();
        oldValue     = gauge.getValue();
        minValue     = gauge.getMinValue();
        maxValue     = gauge.getMaxValue();
        limitString  = "";
        formatString = String.join("", "%.", Integer.toString(gauge.getDecimals()), "f");
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

        ticksAndSectionsCanvas = new Canvas();
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        ledCanvas = new Canvas();
        led = ledCanvas.getGraphicsContext2D();

        thresholdTooltip = new Tooltip("Threshold\n(" + String.format(Locale.US, formatString, getSkinnable().getThreshold()) + ")");
        thresholdTooltip.setTextAlignment(TextAlignment.CENTER);

        threshold = new Path();
        Tooltip.install(threshold, thresholdTooltip);

        markerPane = new Pane();

        needleRotate = new Rotate(180 - startAngle);
        needleRotate.setAngle(needleRotate.getAngle() + (getSkinnable().getValue() - oldValue - minValue) * angleStep);
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
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setMouseTransparent(true);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setMouseTransparent(true);
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setMouseTransparent(true);

        valueText = new Text(String.format(Locale.US, formatString, getSkinnable().getValue()));
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setMouseTransparent(true);

        // Set initial value
        double targetAngle = 180 - startAngle + (getSkinnable().getCurrentValue() - minValue) * angleStep;
        targetAngle        = Helper.clamp(180 - startAngle, 180 - startAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);

        // Add all nodes
        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.getChildren().setAll(ticksAndSectionsCanvas,
                                  markerPane,
                                  ledCanvas,
                                  titleText,
                                  unitText,
                                  valueText,
                                  shadowGroup);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().getMarkers().addListener((ListChangeListener<Marker>) c -> {
            updateMarkers();
            redraw();
        });

        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle(getSkinnable().getCurrentValue()));

        handleEvents("INTERACTIVITY");
        handleEvents("VISIBILITY");
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            double currentValue = getSkinnable().getCurrentValue();
            // Check min- and maxMeasuredValue
            if (currentValue < getSkinnable().getMinMeasuredValue()) {
                getSkinnable().setMinMeasuredValue(currentValue);
            }
            if (currentValue > getSkinnable().getMaxMeasuredValue()) {
                getSkinnable().setMaxMeasuredValue(currentValue);
            }

            // Check sections for value and fire section events
            if (getSkinnable().getCheckSectionsForValue()) {
                List<Section> sections = getSkinnable().getSections();
                int           listSize = sections.size();
                for (int i = sections.size() ; i > listSize ; i--) {
                    sections.get(i).checkForValue(currentValue);
                }
            }

            // Check areas for value and fire section events
            if (getSkinnable().getCheckAreasForValue()) {
                List<Section> areas = getSkinnable().getSections();
                int listSize = areas.size();
                for (int i = areas.size() ; i > listSize ; i--) {
                    areas.get(i).checkForValue(currentValue);
                }
            }
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            ledCanvas.setManaged(getSkinnable().isLedVisible());
            ledCanvas.setVisible(getSkinnable().isLedVisible());

            titleText.setVisible(!getSkinnable().getTitle().isEmpty());
            titleText.setManaged(!getSkinnable().getTitle().isEmpty());

            unitText.setVisible(!getSkinnable().getUnit().isEmpty());
            unitText.setManaged(!getSkinnable().getUnit().isEmpty());

            valueText.setManaged(getSkinnable().isValueVisible());
            valueText.setVisible(getSkinnable().isValueVisible());

            markerMap.values().forEach(shape -> {
                shape.setManaged(getSkinnable().getMarkersVisible());
                shape.setVisible(getSkinnable().getMarkersVisible());
            });

            threshold.setManaged(getSkinnable().isThresholdVisible());
            threshold.setVisible(getSkinnable().isThresholdVisible());
        } else if ("LED".equals(EVENT_TYPE)) {
            if (getSkinnable().isLedVisible()) { drawLed(); }
        } else if ("RECALC".equals(EVENT_TYPE)) {
            angleRange = Helper.clamp(90d, 180d, getSkinnable().getAngleRange());
            startAngle = getStartAngle();
            if (getSkinnable().isAutoScale()) getSkinnable().calcAutoScale();
            minValue   = getSkinnable().getMinValue();
            maxValue   = getSkinnable().getMaxValue();
            angleStep  = angleRange / getSkinnable().getRange();
            needleRotate.setAngle((180 - startAngle) + (getSkinnable().getValue() - minValue) * angleStep);
            if (getSkinnable().getValue() < minValue) {
                getSkinnable().setValue(minValue);
                oldValue = minValue;
            }
            if (getSkinnable().getValue() > maxValue) {
                getSkinnable().setValue(maxValue);
                oldValue = maxValue;
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
    private double getStartAngle() {
        ScaleDirection scaleDirection = getSkinnable().getScaleDirection();
        Pos            knobPosition   = getSkinnable().getKnobPosition();
        switch(knobPosition) {
            case CENTER_LEFT : return ScaleDirection.CLOCKWISE == scaleDirection ? angleRange * 0.5 + 90 : 90 - angleRange * 0.5;
            case CENTER_RIGHT:
            default          : return ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection() ? angleRange * 0.5 - 90 : 270 -  angleRange * 0.5;
        }
    }

    private void rotateNeedle(final double VALUE) {
        double startOffsetAngle = 180 - startAngle;
        double targetAngle;
        if (ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection()) {
            targetAngle = startOffsetAngle + (VALUE - minValue) * angleStep;
            targetAngle = Helper.clamp(startOffsetAngle, startOffsetAngle + angleRange, targetAngle);
        } else {
            targetAngle = startOffsetAngle - (VALUE - minValue) * angleStep;
            targetAngle = Helper.clamp(startOffsetAngle - angleRange, startOffsetAngle, targetAngle);
        }
        needleRotate.setAngle(targetAngle);
        valueText.setText(limitString + String.format(Locale.US, formatString, VALUE));
        valueText.setTranslateX(Pos.CENTER_LEFT == getSkinnable().getKnobPosition() ? width * 0.6 - valueText.getLayoutBounds().getWidth() : width * 0.9 - valueText.getLayoutBounds().getWidth());
    }

    private void drawTickMarks() {
        ticksAndSections.setLineCap(StrokeLineCap.BUTT);
        double               sinValue;
        double               cosValue;
        double               scaledHeight          = height * 0.9;
        double               centerX               = width * 0.9;
        double               centerY               = height * 0.5;
        int                  tickLabelDecimals     = getSkinnable().getTickLabelDecimals();
        String               tickLabelFormatString = "%." + tickLabelDecimals + "f";
        double               minorTickSpace        = getSkinnable().getMinorTickSpace();
        double               tmpAngleStep          = angleStep * minorTickSpace;
        TickLabelOrientation tickLabelOrientation  = getSkinnable().getTickLabelOrientation();
        TickLabelLocation    tickLabelLocation     = getSkinnable().getTickLabelLocation();
        BigDecimal           minorTickSpaceBD      = BigDecimal.valueOf(minorTickSpace);
        BigDecimal           majorTickSpaceBD      = BigDecimal.valueOf(getSkinnable().getMajorTickSpace());
        BigDecimal           mediumCheck2          = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal           mediumCheck5          = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal           counterBD             = BigDecimal.valueOf(minValue);
        double               counter               = minValue;

        List<Section> tickMarkSections             = getSkinnable().getTickMarkSections();
        List<Section> tickLabelSections            = getSkinnable().getTickLabelSections();
        Color         tickMarkColor                = getSkinnable().getTickMarkColor();
        Color         majorTickMarkColor           = getSkinnable().getMajorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : getSkinnable().getMajorTickMarkColor();
        Color         mediumTickMarkColor          = getSkinnable().getMediumTickMarkColor().equals(tickMarkColor) ? tickMarkColor : getSkinnable().getMediumTickMarkColor();
        Color         minorTickMarkColor           = getSkinnable().getMinorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : getSkinnable().getMinorTickMarkColor();
        Color         tickLabelColor               = getSkinnable().getTickLabelColor();
        Color         zeroColor                    = getSkinnable().getZeroColor();
        boolean       isNotZero                    = true;
        TickMarkType  majorTickMarkType            = getSkinnable().getMajorTickMarkType();
        TickMarkType  mediumTickMarkType           = getSkinnable().getMediumTickMarkType();
        TickMarkType  minorTickMarkType            = getSkinnable().getMinorTickMarkType();
        boolean       tickMarkSectionsVisible      = getSkinnable().getTickMarkSectionsVisible();
        boolean       tickLabelSectionsVisible     = getSkinnable().getTickLabelSectionsVisible();
        boolean       majorTickMarksVisible        = getSkinnable().getMajorTickMarksVisible();
        boolean       mediumTickMarksVisible       = getSkinnable().getMediumTickMarksVisible();
        boolean       minorTickMarksVisible        = getSkinnable().getMinorTickMarksVisible();
        boolean       tickLabelsVisible            = getSkinnable().getTickLabelsVisible();
        boolean       onlyFirstAndLastLabelVisible = getSkinnable().isOnlyFirstAndLastTickLabelVisible();
        boolean       customTickLabelsEnabled      = getSkinnable().getCustomTickLabelsEnabled();
        List<String>  customTickLabels             = customTickLabelsEnabled ? getSkinnable().getCustomTickLabels() : null;
        double        textDisplacementFactor       = majorTickMarkType == TickMarkType.DOT ? (TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.95 : 1.05) : 1.0;
        double        majorDotSize;
        double        majorHalfDotSize;
        double        mediumDotSize;
        double        mediumHalfDotSize;
        double        minorDotSize;
        double        minorHalfDotSize;

        double orthTextFactor;
        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
            orthTextFactor    = Gauge.TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.45 * textDisplacementFactor : 0.45 * textDisplacementFactor;
            majorDotSize      = 0.02 * scaledHeight;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01375 * scaledHeight;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0075 * scaledHeight;
            minorHalfDotSize  = minorDotSize * 0.5;
        } else {
            orthTextFactor    = Gauge.TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.38 * textDisplacementFactor : 0.37 * textDisplacementFactor;
            majorDotSize      = 0.025 * scaledHeight;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01875 * scaledHeight;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0125 * scaledHeight;
            minorHalfDotSize  = minorDotSize * 0.5;
        };

        boolean fullRange                  = (minValue < 0 && maxValue > 0);
        double  tickLabelFontSize          = tickLabelDecimals == 0 ? 0.054 * scaledHeight : 0.051 * scaledHeight;
        double  tickMarkFontSize           = tickLabelDecimals == 0 ? 0.047 * scaledHeight : 0.044 * scaledHeight;
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
        BigDecimal     tmpStepBD      = new BigDecimal(tmpAngleStep);
        tmpStepBD                     = tmpStepBD.setScale(3, BigDecimal.ROUND_HALF_UP);
        double tmpStep                = tmpStepBD.doubleValue();
        double angle                  = 0;
        int    customTickLabelCounter = 0;
        for (double i = 0; Double.compare(-angleRange - tmpStep, i) <= 0 ; i -= tmpStep) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            switch(tickLabelLocation) {
                case OUTSIDE:
                    innerPointX                = centerX + scaledHeight * 0.3585 * sinValue;
                    innerPointY                = centerY + scaledHeight * 0.3585 * cosValue;
                    innerMediumPointX          = centerX + scaledHeight * 0.3585 * sinValue;
                    innerMediumPointY          = centerY + scaledHeight * 0.3585 * cosValue;
                    innerMinorPointX           = centerX + scaledHeight * 0.3585 * sinValue;
                    innerMinorPointY           = centerY + scaledHeight * 0.3585 * cosValue;
                    outerPointX                = centerX + scaledHeight * 0.4105 * sinValue;
                    outerPointY                = centerY + scaledHeight * 0.4105 * cosValue;
                    outerMediumPointX          = centerX + scaledHeight * 0.4045 * sinValue;
                    outerMediumPointY          = centerY + scaledHeight * 0.4045 * cosValue;
                    outerMinorPointX           = centerX + scaledHeight * 0.3975 * sinValue;
                    outerMinorPointY           = centerY + scaledHeight * 0.3975 * cosValue;
                    textPointX                 = centerX + scaledHeight * orthTextFactor * sinValue;
                    textPointY                 = centerY + scaledHeight * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + scaledHeight * 0.3685 * sinValue;
                    dotCenterY                 = centerY + scaledHeight * 0.3685 * cosValue;
                    dotMediumCenterX           = centerX + scaledHeight * 0.365375 * sinValue;
                    dotMediumCenterY           = centerY + scaledHeight * 0.365375 * cosValue;
                    dotMinorCenterX            = centerX + scaledHeight * 0.36225 * sinValue;
                    dotMinorCenterY            = centerY + scaledHeight * 0.36225 * cosValue;
                    tickLabelTickMarkX         = centerX + scaledHeight * 0.3805 * sinValue;
                    tickLabelTickMarkY         = centerY + scaledHeight * 0.3805 * cosValue;

                    triangleMajorInnerAngle1   = Math.toRadians(angle - 1.2 + startAngle);
                    triangleMajorInnerAngle2   = Math.toRadians(angle + 1.2 + startAngle);
                    triangleMajorOuterAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMajorOuterAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMajorInnerPoint1X  = centerX + scaledHeight * 0.3585 * Math.sin(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint1Y  = centerY + scaledHeight * 0.3585 * Math.cos(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint2X  = centerX + scaledHeight * 0.3585 * Math.sin(triangleMajorInnerAngle2);
                    triangleMajorInnerPoint2Y  = centerY + scaledHeight * 0.3585 * Math.cos(triangleMajorInnerAngle2);
                    triangleMajorOuterPoint1X  = centerX + scaledHeight * 0.4105 * Math.sin(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint1Y  = centerY + scaledHeight * 0.4105 * Math.cos(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint2X  = centerX + scaledHeight * 0.4105 * Math.sin(triangleMajorOuterAngle2);
                    triangleMajorOuterPoint2Y  = centerY + scaledHeight * 0.4105 * Math.cos(triangleMajorOuterAngle2);

                    triangleMediumInnerAngle1  = Math.toRadians(angle - 1.0 + startAngle);
                    triangleMediumInnerAngle2  = Math.toRadians(angle + 1.0 + startAngle);
                    triangleMediumOuterAngle1  = Math.toRadians(angle - 0.7 + startAngle);
                    triangleMediumOuterAngle2  = Math.toRadians(angle + 0.7 + startAngle);
                    triangleMediumInnerPoint1X = centerX + scaledHeight * 0.3585 * Math.sin(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint1Y = centerY + scaledHeight * 0.3585 * Math.cos(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint2X = centerX + scaledHeight * 0.3585 * Math.sin(triangleMediumInnerAngle2);
                    triangleMediumInnerPoint2Y = centerY + scaledHeight * 0.3585 * Math.cos(triangleMediumInnerAngle2);
                    triangleMediumOuterPoint1X = centerX + scaledHeight * 0.3985 * Math.sin(triangleMajorOuterAngle1);
                    triangleMediumOuterPoint1Y = centerY + scaledHeight * 0.3985 * Math.cos(triangleMediumOuterAngle1);
                    triangleMediumOuterPoint2X = centerX + scaledHeight * 0.3985 * Math.sin(triangleMediumOuterAngle2);
                    triangleMediumOuterPoint2Y = centerY + scaledHeight * 0.3985 * Math.cos(triangleMediumOuterAngle2);

                    triangleMinorInnerAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMinorInnerAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMinorOuterAngle1   = Math.toRadians(angle - 0.6 + startAngle);
                    triangleMinorOuterAngle2   = Math.toRadians(angle + 0.6 + startAngle);
                    triangleMinorInnerPoint1X  = centerX + scaledHeight * 0.3585 * Math.sin(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint1Y  = centerY + scaledHeight * 0.3585 * Math.cos(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint2X  = centerX + scaledHeight * 0.3585 * Math.sin(triangleMinorInnerAngle2);
                    triangleMinorInnerPoint2Y  = centerY + scaledHeight * 0.3585 * Math.cos(triangleMinorInnerAngle2);
                    triangleMinorOuterPoint1X  = centerX + scaledHeight * 0.3975 * Math.sin(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint1Y  = centerY + scaledHeight * 0.3975 * Math.cos(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint2X  = centerX + scaledHeight * 0.3975 * Math.sin(triangleMinorOuterAngle2);
                    triangleMinorOuterPoint2Y  = centerY + scaledHeight * 0.3975 * Math.cos(triangleMinorOuterAngle2);
                    break;
                case INSIDE:
                default:
                    innerPointX                = centerX + scaledHeight * 0.423 * sinValue;
                    innerPointY                = centerY + scaledHeight * 0.423 * cosValue;
                    innerMediumPointX          = centerX + scaledHeight * 0.43 * sinValue;
                    innerMediumPointY          = centerY + scaledHeight * 0.43 * cosValue;
                    innerMinorPointX           = centerX + scaledHeight * 0.436 * sinValue;
                    innerMinorPointY           = centerY + scaledHeight * 0.436 * cosValue;
                    outerPointX                = centerX + scaledHeight * 0.475 * sinValue;
                    outerPointY                = centerY + scaledHeight * 0.475 * cosValue;
                    outerMediumPointX          = centerX + scaledHeight * 0.475 * sinValue;
                    outerMediumPointY          = centerY + scaledHeight * 0.475 * cosValue;
                    outerMinorPointX           = centerX + scaledHeight * 0.475 * sinValue;
                    outerMinorPointY           = centerY + scaledHeight * 0.475 * cosValue;
                    textPointX                 = centerX + scaledHeight * orthTextFactor * sinValue;
                    textPointY                 = centerY + scaledHeight * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + scaledHeight * 0.4625 * sinValue;
                    dotCenterY                 = centerY + scaledHeight * 0.4625 * cosValue;
                    dotMediumCenterX           = centerX + scaledHeight * 0.465625 * sinValue;
                    dotMediumCenterY           = centerY + scaledHeight * 0.465625 * cosValue;
                    dotMinorCenterX            = centerX + scaledHeight * 0.46875 * sinValue;
                    dotMinorCenterY            = centerY + scaledHeight * 0.46875 * cosValue;
                    tickLabelTickMarkX         = centerX + scaledHeight * 0.445 * sinValue;
                    tickLabelTickMarkY         = centerY + scaledHeight * 0.445 * cosValue;

                    triangleMajorInnerAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMajorInnerAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMajorOuterAngle1   = Math.toRadians(angle - 1.2 + startAngle);
                    triangleMajorOuterAngle2   = Math.toRadians(angle + 1.2 + startAngle);
                    triangleMajorInnerPoint1X  = centerX + scaledHeight * 0.423 * Math.sin(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint1Y  = centerY + scaledHeight * 0.423 * Math.cos(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint2X  = centerX + scaledHeight * 0.423 * Math.sin(triangleMajorInnerAngle2);
                    triangleMajorInnerPoint2Y  = centerY + scaledHeight * 0.423 * Math.cos(triangleMajorInnerAngle2);
                    triangleMajorOuterPoint1X  = centerX + scaledHeight * 0.475 * Math.sin(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint1Y  = centerY + scaledHeight * 0.475 * Math.cos(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint2X  = centerX + scaledHeight * 0.475 * Math.sin(triangleMajorOuterAngle2);
                    triangleMajorOuterPoint2Y  = centerY + scaledHeight * 0.475 * Math.cos(triangleMajorOuterAngle2);

                    triangleMediumInnerAngle1  = Math.toRadians(angle - 0.7 + startAngle);
                    triangleMediumInnerAngle2  = Math.toRadians(angle + 0.7 + startAngle);
                    triangleMediumOuterAngle1  = Math.toRadians(angle - 1.0 + startAngle);
                    triangleMediumOuterAngle2  = Math.toRadians(angle + 1.0 + startAngle);
                    triangleMediumInnerPoint1X = centerX + scaledHeight * 0.435 * Math.sin(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint1Y = centerY + scaledHeight * 0.435 * Math.cos(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint2X = centerX + scaledHeight * 0.435 * Math.sin(triangleMediumInnerAngle2);
                    triangleMediumInnerPoint2Y = centerY + scaledHeight * 0.435 * Math.cos(triangleMediumInnerAngle2);
                    triangleMediumOuterPoint1X = centerX + scaledHeight * 0.475 * Math.sin(triangleMajorOuterAngle1);
                    triangleMediumOuterPoint1Y = centerY + scaledHeight * 0.475 * Math.cos(triangleMediumOuterAngle1);
                    triangleMediumOuterPoint2X = centerX + scaledHeight * 0.475 * Math.sin(triangleMediumOuterAngle2);
                    triangleMediumOuterPoint2Y = centerY + scaledHeight * 0.475 * Math.cos(triangleMediumOuterAngle2);

                    triangleMinorInnerAngle1   = Math.toRadians(angle - 0.6 + startAngle);
                    triangleMinorInnerAngle2   = Math.toRadians(angle + 0.6 + startAngle);
                    triangleMinorOuterAngle1   = Math.toRadians(angle - 0.8 + startAngle);
                    triangleMinorOuterAngle2   = Math.toRadians(angle + 0.8 + startAngle);
                    triangleMinorInnerPoint1X  = centerX + scaledHeight * 0.440 * Math.sin(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint1Y  = centerY + scaledHeight * 0.440 * Math.cos(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint2X  = centerX + scaledHeight * 0.440 * Math.sin(triangleMinorInnerAngle2);
                    triangleMinorInnerPoint2Y  = centerY + scaledHeight * 0.440 * Math.cos(triangleMinorInnerAngle2);
                    triangleMinorOuterPoint1X  = centerX + scaledHeight * 0.475 * Math.sin(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint1Y  = centerY + scaledHeight * 0.475 * Math.cos(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint2X  = centerX + scaledHeight * 0.475 * Math.sin(triangleMinorOuterAngle2);
                    triangleMinorOuterPoint2Y  = centerY + scaledHeight * 0.475 * Math.cos(triangleMinorOuterAngle2);
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
                    tickMarkType = majorTickMarkType;
                    ticksAndSections.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    ticksAndSections.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    ticksAndSections.setLineWidth(scaledHeight * (TickMarkType.BOX == tickMarkType ? 0.016 : 0.0055));
                } else if (minorTickMarksVisible) {
                    tickMarkType = minorTickMarkType;
                    ticksAndSections.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    ticksAndSections.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    ticksAndSections.setLineWidth(scaledHeight * (TickMarkType.BOX == tickMarkType ? 0.007 : 0.00225));
                }
                if (fullRange && !isNotZero) {
                    ticksAndSections.setFill(zeroColor);
                    ticksAndSections.setStroke(zeroColor);
                }

                switch (tickMarkType) {
                    case TRIANGLE:
                        if (majorTickMarksVisible) {
                            Helper.drawTriangle(ticksAndSections, triangleMajorInnerPoint1X, triangleMajorInnerPoint1Y, triangleMajorInnerPoint2X, triangleMajorInnerPoint2Y,
                                                triangleMajorOuterPoint1X, triangleMajorOuterPoint1Y, triangleMajorOuterPoint2X, triangleMajorOuterPoint2Y);
                        } else if (minorTickMarksVisible) {
                            Helper.drawTriangle(ticksAndSections, triangleMinorInnerPoint1X, triangleMinorInnerPoint1Y, triangleMinorInnerPoint2X, triangleMinorInnerPoint2Y,
                                                triangleMinorOuterPoint1X, triangleMinorOuterPoint1Y, triangleMinorOuterPoint2X, triangleMinorOuterPoint2Y);
                        }
                        break;
                    case DOT:
                        if (majorTickMarksVisible) {
                            Helper.drawDot(ticksAndSections, dotCenterX - majorHalfDotSize, dotCenterY - majorHalfDotSize, majorDotSize);
                        } else if (minorTickMarksVisible) {
                            Helper.drawDot(ticksAndSections, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
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
                            ticksAndSections.fillText(String.format(Locale.US, tickLabelFormatString, counter), 0, 0);
                            ticksAndSections.restore();
                        }
                        break;
                    case BOX:
                    case LINE:
                    default:
                        if (majorTickMarksVisible) {
                            Helper.drawLine(ticksAndSections, innerPointX, innerPointY, outerPointX, outerPointY);
                        } else if (minorTickMarksVisible) {
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(ticksAndSections, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(ticksAndSections, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
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

                    if (!onlyFirstAndLastLabelVisible) {
                        if (isNotZero) {
                            ticksAndSections.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                        } else {
                            ticksAndSections.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : fullRange ? zeroColor : tickLabelColor);
                        }
                    } else {
                        if ((Double.compare(counter, minValue) == 0 || Double.compare(counter, maxValue) == 0)) {
                            if (isNotZero) {
                                ticksAndSections.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                            } else {
                                ticksAndSections.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : fullRange ? zeroColor : tickLabelColor);
                            }
                        } else {
                            ticksAndSections.setFill(Color.TRANSPARENT);
                        }
                    }

                    if (customTickLabelsEnabled) {
                        if (customTickLabelCounter >= 0) {
                            ticksAndSections.fillText(customTickLabels.get(customTickLabelCounter), 0, 0);
                            customTickLabelCounter++;
                        }
                        if (customTickLabelCounter > customTickLabels.size() - 1) customTickLabelCounter = -1;
                    } else {
                        ticksAndSections.fillText(String.format(Locale.US, tickLabelFormatString, counter), 0, 0);
                    }
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
                        Helper.drawTriangle(ticksAndSections, triangleMediumInnerPoint1X, triangleMediumInnerPoint1Y, triangleMediumInnerPoint2X, triangleMediumInnerPoint2Y,
                                            triangleMediumOuterPoint1X, triangleMediumOuterPoint1Y, triangleMediumOuterPoint2X, triangleMediumOuterPoint2Y);
                        break;
                    case DOT:
                        Helper.drawDot(ticksAndSections, dotMediumCenterX - mediumHalfDotSize, dotMediumCenterY - mediumHalfDotSize, mediumDotSize);
                        break;
                    case BOX:
                        ticksAndSections.setLineWidth(scaledHeight * 0.009);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(ticksAndSections, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(ticksAndSections, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                    case LINE:
                    default:
                        ticksAndSections.setLineWidth(scaledHeight * 0.0035);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(ticksAndSections, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(ticksAndSections, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
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
                            Helper.drawTriangle(ticksAndSections, triangleMinorInnerPoint1X, triangleMinorInnerPoint1Y, triangleMinorInnerPoint2X, triangleMinorInnerPoint2Y,
                                                triangleMinorOuterPoint1X, triangleMinorOuterPoint1Y, triangleMinorOuterPoint2X, triangleMinorOuterPoint2Y);
                            break;
                        case DOT:
                            Helper.drawDot(ticksAndSections, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                            break;
                        case BOX:
                            ticksAndSections.setLineWidth(scaledHeight * 0.007);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(ticksAndSections, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(ticksAndSections, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                        case LINE:
                        default:
                            ticksAndSections.setLineWidth(scaledHeight * 0.00225);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(ticksAndSections, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(ticksAndSections, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
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
        TickLabelLocation  tickLabelLocation     = getSkinnable().getTickLabelLocation();
        double             scaledHeight          = height * 0.9;
        double             xy                    = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.1705 * scaledHeight : 0.107 * scaledHeight;
        double             wh                    = TickLabelLocation.OUTSIDE == tickLabelLocation ? scaledHeight * 0.77 : scaledHeight * 0.897;
        double             offset                = 90 - startAngle;
        double             offsetX               = -0.1 * width;
        double             knobPositionOffsetCW  = Pos.CENTER_LEFT == getSkinnable().getKnobPosition() ? 90 : 270;
        double             knobPositionOffsetCCW = Pos.CENTER_LEFT == getSkinnable().getKnobPosition() ? 180 : 0;
        ScaleDirection     scaleDirection        = getSkinnable().getScaleDirection();
        List<Stop>         stops                 = getSkinnable().getGradientBarStops();
        Map<Double, Color> stopAngleMap          = new HashMap<>(stops.size());

        stops.forEach(stop -> stopAngleMap.put(stop.getOffset() * angleRange, stop.getColor()));
        double               offsetFactor = ScaleDirection.CLOCKWISE == scaleDirection ? knobPositionOffsetCW - angleRange * 0.5 : angleRange - (angleRange / 180 * angleRange) + knobPositionOffsetCCW;
        AngleConicalGradient gradient     = new AngleConicalGradient(width * 0.5, width * 0.5, offsetFactor, stopAngleMap, getSkinnable().getScaleDirection());

        double barStartAngle  = ScaleDirection.CLOCKWISE == scaleDirection ? -minValue * angleStep : minValue * angleStep;
        double barAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? getSkinnable().getRange() * angleStep : -getSkinnable().getRange() * angleStep;
        ticksAndSections.save();
        ticksAndSections.setStroke(gradient.getImagePattern(new Rectangle(xy - 0.026 * height + offsetX, xy - 0.026 * height, wh + 0.052 * height, wh + 0.052 * height)));
        ticksAndSections.setLineWidth(scaledHeight * 0.052);
        ticksAndSections.setLineCap(StrokeLineCap.BUTT);
        ticksAndSections.strokeArc(xy + offsetX, xy, wh, wh, -(offset + barStartAngle), -barAngleExtend, ArcType.OPEN);
        ticksAndSections.restore();
    }

    private void drawSections() {
        if (getSkinnable().getSections().isEmpty()) return;
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double            scaledHeight      = height * 0.9;
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.1705 * scaledHeight : 0.107 * scaledHeight;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? scaledHeight * 0.77 : scaledHeight * 0.897;
        double            offsetX           = -0.1 * width;
        double            offset            = 90 - startAngle;
        ScaleDirection    scaleDirection    = getSkinnable().getScaleDirection();
        IntStream.range(0, getSkinnable().getSections().size()).parallel().forEachOrdered(
            i -> {
                Section section = getSkinnable().getSections().get(i);
                double sectionStartAngle;
                if (Double.compare(section.getStart(), maxValue) <= 0 && Double.compare(section.getStop(), minValue) >= 0) {
                    if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? minValue * angleStep : -minValue * angleStep;
                    } else {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStart() - minValue) * angleStep : -(section.getStart() - minValue) * angleStep;
                    }
                    double sectionAngleExtend;
                    if (Double.compare(section.getStop(), maxValue) > 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - section.getStart()) * angleStep : -(maxValue - section.getStart()) * angleStep;
                    } else {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStop() - section.getStart()) * angleStep : -(section.getStop() - section.getStart()) * angleStep;
                    }
                    ticksAndSections.save();
                    ticksAndSections.setStroke(section.getColor());
                    ticksAndSections.setLineWidth(scaledHeight * 0.052);
                    ticksAndSections.setLineCap(StrokeLineCap.BUTT);
                    ticksAndSections.strokeArc(xy + offsetX, xy, wh, wh, -(offset + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
                    ticksAndSections.restore();
                }
            }
                                                                                         );
    }

    private void drawAreas() {
        if (getSkinnable().getAreas().isEmpty()) return;
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double            scaledHeight      = height * 0.9;
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.0895 * scaledHeight : 0.025 * scaledHeight;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? scaledHeight * 0.821 : scaledHeight * 0.95;
        double            offsetY           = 0.1 * width;
        double            offset            = 90 - startAngle;
        ScaleDirection    scaleDirection    = getSkinnable().getScaleDirection();

        IntStream.range(0, getSkinnable().getAreas().size()).parallel().forEachOrdered(
            i -> {
                Section area = getSkinnable().getAreas().get(i);
                double areaStartAngle;
                if (Double.compare(area.getStart(), maxValue) <= 0 && Double.compare(area.getStop(), minValue) >= 0) {
                    if (area.getStart() < minValue && area.getStop() < maxValue) {
                        areaStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? minValue * angleStep : -minValue * angleStep;
                    } else {
                        areaStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStart() - minValue) * angleStep : -(area.getStart() - minValue) * angleStep;
                    }
                    double areaAngleExtend;
                    if (area.getStop() > maxValue) {
                        areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - area.getStart()) * angleStep : -(maxValue - area.getStart()) * angleStep;
                    } else {
                        areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStop() - area.getStart()) * angleStep : -(area.getStop() - area.getStart()) * angleStep;
                    }
                    ticksAndSections.save();
                    ticksAndSections.setFill(area.getColor());
                    ticksAndSections.fillArc(xy, xy + offsetY, wh, wh, -(offset + areaStartAngle), - areaAngleExtend, ArcType.ROUND);
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
            double lineWidth = 0.0037037 * width;
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
        double            markerSize        = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.0125 * height : 0.015 * height;
        double            pathHalf          = markerSize * 0.3;
        double            scaledHeight      = height * 0.9;
        double            centerX           = Pos.CENTER_LEFT == getSkinnable().getKnobPosition() ? width * 0.1 : width * 0.9;
        double            centerY           = height * 0.5;
        ScaleDirection    scaleDirection    = getSkinnable().getScaleDirection();
        if (getSkinnable().getMarkersVisible()) {
            markerMap.keySet().forEach(marker -> {
                Shape  shape = markerMap.get(marker);
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
                        Path triangle = (Path) shape;
                        triangle.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                triangle.getElements().add(new MoveTo(centerX + scaledHeight * 0.38 * sinValue, centerY + scaledHeight * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                triangle.getElements().add(new LineTo(centerX + scaledHeight * 0.4075 * sinValue, centerY + scaledHeight * 0.4075 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                triangle.getElements().add(new LineTo(centerX + scaledHeight * 0.4075 * sinValue, centerY + scaledHeight * 0.4075 * cosValue));
                                triangle.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                triangle.getElements().add(new MoveTo(centerX + scaledHeight * 0.465 * sinValue, centerY + scaledHeight * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                triangle.getElements().add(new LineTo(centerX + scaledHeight * 0.436 * sinValue, centerY + scaledHeight * 0.436 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                triangle.getElements().add(new LineTo(centerX + scaledHeight * 0.436 * sinValue, centerY + scaledHeight * 0.436 * cosValue));
                                triangle.getElements().add(new ClosePath());
                                break;
                        }
                        break;
                    case DOT:
                        Circle dot = (Circle) shape;
                        dot.setRadius(markerSize);
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                dot.setCenterX(centerX + scaledHeight * 0.3945 * sinValue);
                                dot.setCenterY(centerY + scaledHeight * 0.3945 * cosValue);
                                break;
                            default:
                                dot.setCenterX(centerX + scaledHeight * 0.449 * sinValue);
                                dot.setCenterY(centerY + scaledHeight * 0.449 * cosValue);
                                break;
                        }
                        break;
                    case STANDARD:
                    default:
                        Path standard = (Path) shape;
                        standard.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                standard.getElements().add(new MoveTo(centerX + scaledHeight * 0.38 * sinValue, centerY + scaledHeight * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.4075 * sinValue, centerY + scaledHeight * 0.4075 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.4575 * sinValue, centerY + scaledHeight * 0.4575 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.4575 * sinValue, centerY + scaledHeight * 0.4575 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.4075 * sinValue, centerY + scaledHeight * 0.4075 * cosValue));
                                standard.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                standard.getElements().add(new MoveTo(centerX + scaledHeight * 0.465 * sinValue, centerY + scaledHeight * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.436 * sinValue, centerY + scaledHeight * 0.436 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.386 * sinValue, centerY + scaledHeight * 0.386 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.386 * sinValue, centerY + scaledHeight * 0.386 * cosValue));
                                standard.getElements().add(new LineTo(centerX + scaledHeight * 0.436 * sinValue, centerY + scaledHeight * 0.436 * cosValue));
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
                thresholdAngle = startAngle - (getSkinnable().getThreshold() - minValue) * angleStep;
            } else {
                thresholdAngle = startAngle + (getSkinnable().getThreshold() - minValue) * angleStep;
            }
            double thresholdSize = Helper.clamp(3d, 3.5, 0.01 * height);
            double sinValue      = Math.sin(Math.toRadians(thresholdAngle));
            double cosValue      = Math.cos(Math.toRadians(thresholdAngle));
            switch (tickLabelLocation) {
                case OUTSIDE:
                    threshold.getElements().add(new MoveTo(centerX + height * 0.38 * sinValue, centerY + height * 0.38 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + height * 0.34 * sinValue, centerY + height * 0.34 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + height * 0.34 * sinValue, centerY + height * 0.34 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
                case INSIDE:
                default:
                    threshold.getElements().add(new MoveTo(centerX + height * 0.465 * sinValue, centerY + height * 0.465 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + height * 0.425 * sinValue, centerY + height * 0.425 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + height * 0.425 * sinValue, centerY + height * 0.425 * cosValue));
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
                case TRIANGLE: markerMap.put(marker, new Path()); break;
                case DOT     : markerMap.put(marker, new Circle()); break;
                case STANDARD:
                default:       markerMap.put(marker, new Path()); break;
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
        double brg       = Color.BLACK.equals(knobColor) ? 0.2 : knobColor.getBrightness();
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
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.9 : brg * 1.0, alp)),
                                                new Stop(0.01, Color.hsb(hue, sat, PRESSED ? brg * 0.75 : brg * 0.85, alp)),
                                                new Stop(0.5, Color.hsb(hue, sat, PRESSED ? brg * 0.4 : brg * 0.5, alp)),
                                                new Stop(0.51, Color.hsb(hue, sat, PRESSED ? brg * 0.35 :brg *  0.45, alp)),
                                                new Stop(1.0, Color.hsb(hue, sat, PRESSED ? brg * 0.7 : brg * 0.8, alp))
                                            ));
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
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.7 : brg * 0.9, alp)),
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.3 : brg * 0.5, alp))));
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
                double lineWidth = 0.00740741 * width;
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
                gradTop = PRESSED ? h - width * 0.01 : width * 0.005;
                gradBot = PRESSED ? width * 0.005 : h - width * 0.01;
                knob.setFill(new LinearGradient(0,gradTop, 0, gradBot,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.hsb(hue, sat, brg * 0.85, alp)),
                                                new Stop(0.45, Color.hsb(hue, sat, brg * 0.65, alp)),
                                                new Stop(1.0, Color.hsb(hue, sat, brg * 0.4, alp))));
                knob.fillOval(width * 0.005, width * 0.005, w - width * 0.01, h - width * 0.01);
                break;
        }
        knobCanvas.setCache(true);
        knobCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private void resizeText() {
        double scaledHeight = height * 0.9;
        Pos    knobPosition = getSkinnable().getKnobPosition();
        double maxWidth     = 0.4 * width;
        double fontSize     = 0.06 * scaledHeight;

        titleText.setFont(Fonts.robotoMedium(fontSize));
        titleText.setText(getSkinnable().getTitle());
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(Pos.CENTER_LEFT == knobPosition ? width * 0.6 - titleText.getLayoutBounds().getWidth() : width * 0.4, (height - titleText.getLayoutBounds().getHeight()) * 0.5);

        fontSize = 0.04 * scaledHeight;
        unitText.setFont(Fonts.robotoRegular(fontSize));
        unitText.setText(getSkinnable().getUnit());
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(Pos.CENTER_LEFT == knobPosition ? width * 0.6 - unitText.getLayoutBounds().getWidth() : width * 0.4, (height - unitText.getLayoutBounds().getHeight()) * 0.38);
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            Pos    knobPosition = getSkinnable().getKnobPosition();
            double centerX      = Pos.CENTER_LEFT == knobPosition ? width * 0.1 : width * 0.9;
            double centerY      = height * 0.5;
            double scaledHeight = height * 0.9;

            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            dropShadow.setRadius(0.008 * scaledHeight);
            dropShadow.setOffsetY(0.008 * scaledHeight);

            backgroundInnerShadow.setOffsetX(0);
            backgroundInnerShadow.setOffsetY(scaledHeight * 0.03);
            backgroundInnerShadow.setRadius(scaledHeight * 0.04);

            pane.setEffect(getSkinnable().isInnerShadowEnabled() ? backgroundInnerShadow : null);

            ticksAndSectionsCanvas.setWidth(height);
            ticksAndSectionsCanvas.setHeight(height);
            ticksAndSectionsCanvas.relocate(Pos.CENTER_LEFT == getSkinnable().getKnobPosition() ? -width * 0.8 : 0, 0);

            markerPane.setPrefSize(height, height);

            boolean isFlatLed = LedType.FLAT == getSkinnable().getLedType();
            ledSize = isFlatLed ? 0.05 * scaledHeight : 0.06 * scaledHeight;
            ledCanvas.setWidth(ledSize);
            ledCanvas.setHeight(ledSize);
            ledCanvas.relocate(Pos.CENTER_LEFT == knobPosition ? 0.025 * height : 0.425 * height, 0.35 * height);
            ledOffShadow = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            ledOnShadow  = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            if (!isFlatLed) ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, getSkinnable().getLedColor(), 0.36 * ledSize, 0, 0, 0));

            resizeText();

            valueText.setFont(Fonts.robotoMedium(scaledHeight * 0.1));
            valueText.setTranslateX(Pos.CENTER_LEFT == getSkinnable().getKnobPosition() ? width * 0.6 - valueText.getLayoutBounds().getWidth() : width * 0.9 - valueText.getLayoutBounds().getWidth());
            valueText.setTranslateY(height * 0.6);

            double needleWidth  = scaledHeight * getSkinnable().getNeedleSize().FACTOR;
            double needleHeight = TickLabelLocation.OUTSIDE == getSkinnable().getTickLabelLocation() ? scaledHeight * 0.3965 : scaledHeight * 0.455;

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

            needle.relocate(centerX - needle.getLayoutBounds().getWidth() * 0.5, centerY - needle.getLayoutBounds().getHeight());
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight());

            knobCanvas.setWidth(height * 0.1);
            knobCanvas.setHeight(height * 0.1);
            knobCanvas.relocate(centerX - height * 0.05, centerY - height * 0.05);

            buttonTooltip.setText(getSkinnable().getButtonTooltipText());
        }
    }

    private void redraw() {
        formatString = String.join("", "%.", Integer.toString(getSkinnable().getDecimals()), "f");
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        // Areas, Sections and Tick Marks
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, height, height);
        if (getSkinnable().getAreasVisible()) drawAreas();
        if (getSkinnable().isGradientBarEnabled() && getSkinnable().getGradientLookup() != null) {
            drawGradientBar();
        } else if (getSkinnable().getSectionsVisible()) {
            drawSections();
        }
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

        // Text
        titleText.setFill(getSkinnable().getTitleColor());
        unitText.setFill(getSkinnable().getUnitColor());
        valueText.setFill(getSkinnable().getValueColor());
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
                needle.setStrokeWidth(0.0037037 * width);
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
        thresholdTooltip.setText("Threshold\n(" + String.format(Locale.US, formatString, getSkinnable().getThreshold()) + ")");
    }
}
