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
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
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
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 01.01.16.
 */
public class ModernSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double              START_ANGLE      = 300;
    private double              ANGLE_RANGE      = 240;
    private double              BAR_START_ANGLE  = -150;
    private double                   size;
    private Pane                     pane;
    private Circle                   background;
    private Path                     mask;
    private Circle                   centerKnob;
    private Path                     needle;
    private MoveTo                   needleMoveTo1;
    private CubicCurveTo             needleCubicCurveTo2;
    private CubicCurveTo             needleCubicCurveTo3;
    private CubicCurveTo             needleCubicCurveTo4;
    private LineTo                   needleLineTo5;
    private LineTo                   needleLineTo6;
    private LineTo                   needleLineTo7;
    private LineTo                   needleLineTo8;
    private ClosePath                needleClosePath9;
    private Rotate                   needleRotate;
    private Canvas                   mainCanvas;
    private GraphicsContext          mainCtx;
    private Canvas                   tickMarkCanvas;
    private GraphicsContext          tickMarkCtx;
    private InnerShadow              innerShadow0;
    private InnerShadow              innerShadow1;
    private InnerShadow              innerShadow2;
    private InnerShadow              innerShadow3;
    private DropShadow               dropShadow4;
    private DropShadow               glow1;
    private DropShadow               glow2;
    private DropShadow               bigGlow;
    private Text                     valueText;
    private Text                     titleText;
    private Text                     subTitleText;
    private Text                     unitText;
    private double                   angleStep;
    private EventHandler<MouseEvent> mouseHandler;
    private Tooltip                  buttonTooltip;
    private String                   formatString;
    private Locale                   locale;
    private boolean                  sectionsVisible;
    private List<Section>            sections;
    private Color                    barColor;
    private Color                    thresholdColor;


    // ******************** Constructors **************************************
    public ModernSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleStep       = ANGLE_RANGE / (gauge.getRange());
        mouseHandler    = event -> handleMouseEvent(event);
        buttonTooltip   = new Tooltip();
        formatString    = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale          = gauge.getLocale();
        sectionsVisible = gauge.getSectionsVisible();
        sections        = gauge.getSections();
        barColor        = gauge.getBarColor();
        thresholdColor  = gauge.getThresholdColor();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
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
        innerShadow0 = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 1, 0, 0, 1);

        innerShadow1 = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.65), 1, 0, 0, -1);
        innerShadow1.setInput(innerShadow0);

        background = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.5);
        background.setFill(Color.rgb(32, 32, 32));
        background.setStroke(null);
        background.setEffect(innerShadow1);

        innerShadow2 = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.65), 1, 0, 0, 1);

        innerShadow3 = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 1, 0, 0, -1);
        innerShadow3.setInput(innerShadow2);

        dropShadow4 = new DropShadow();
        dropShadow4.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow4.setColor(Color.rgb(0, 0, 0, 0.65));
        dropShadow4.setOffsetX(0);
        dropShadow4.setInput(innerShadow3);

        mask = new Path();
        mask.setFillRule(FillRule.EVEN_ODD);
        mask.setFill(Color.rgb(32, 32, 32));
        mask.setStroke(null);
        mask.setEffect(dropShadow4);

        needleRotate = new Rotate(180 - START_ANGLE);

        double targetAngle = 180 - START_ANGLE + (getSkinnable().getCurrentValue() - getSkinnable().getMinValue()) * angleStep;
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle));

        glow1   = new DropShadow(BlurType.TWO_PASS_BOX, barColor, 0.085 * PREFERRED_WIDTH, 0, 0, 0);
        glow2   = new DropShadow(BlurType.TWO_PASS_BOX, barColor, 0.085 * PREFERRED_WIDTH, 0, 0, 0);
        bigGlow = new DropShadow(BlurType.TWO_PASS_BOX, barColor, 0.25 * PREFERRED_WIDTH, 0, 0, 0);

        needleMoveTo1       = new MoveTo();
        needleCubicCurveTo2 = new CubicCurveTo();
        needleCubicCurveTo3 = new CubicCurveTo();
        needleCubicCurveTo4 = new CubicCurveTo();
        needleLineTo5       = new LineTo();
        needleLineTo6       = new LineTo();
        needleLineTo7       = new LineTo();
        needleLineTo8       = new LineTo();
        needleClosePath9    = new ClosePath();
        needle              = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4,
                                       needleLineTo5, needleLineTo6, needleLineTo7, needleLineTo8, needleClosePath9);
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.setEffect(glow1);

        needle.getTransforms().setAll(needleRotate);
        needle.setFill(getSkinnable().getNeedleColor());
        needle.setStroke(null);
        needle.setStrokeLineCap(StrokeLineCap.ROUND);
        needle.setStrokeLineJoin(StrokeLineJoin.BEVEL);

        mainCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        mainCtx    = mainCanvas.getGraphicsContext2D();

        tickMarkCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        tickMarkCtx    = tickMarkCanvas.getGraphicsContext2D();

        centerKnob = new Circle(0.5 * PREFERRED_WIDTH, 0.5 * PREFERRED_HEIGHT, 0.22916667 * PREFERRED_WIDTH);
        centerKnob.setPickOnBounds(false);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());
        titleText.setEffect(glow1);
        titleText.setMouseTransparent(true);
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        subTitleText = new Text(getSkinnable().getSubTitle());
        subTitleText.setTextOrigin(VPos.CENTER);
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        subTitleText.setEffect(glow1);
        subTitleText.setMouseTransparent(true);
        Helper.enableNode(subTitleText, !getSkinnable().getSubTitle().isEmpty());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(getSkinnable().getUnitColor());
        unitText.setEffect(glow1);
        unitText.setMouseTransparent(true);
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getMinValue()) + getSkinnable().getUnit());
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());
        valueText.setEffect(bigGlow);
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        // Add all nodes
        pane = new Pane(background,
                        mainCanvas,
                        tickMarkCanvas,
                        mask,
                        needle,
                        centerKnob,
                        titleText,
                        subTitleText,
                        unitText,
                        valueText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().animatedProperty().addListener(o -> handleEvents("ANIMATED"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle(getSkinnable().getCurrentValue()));

        handleEvents("INTERACTIVITY");
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(subTitleText, !getSkinnable().getSubTitle().isEmpty());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            sectionsVisible = getSkinnable().getSectionsVisible();
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            angleStep = ANGLE_RANGE / getSkinnable().getRange();
            needleRotate.setAngle((180 - START_ANGLE) + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
            redraw();
        } else if ("INTERACTIVITY".equals(EVENT_TYPE)) {
            if (getSkinnable().isInteractive()) {
                centerKnob.setOnMousePressed(mouseHandler);
                centerKnob.setOnMouseReleased(mouseHandler);
                buttonTooltip.setText(getSkinnable().getButtonTooltipText());
                Tooltip.install(centerKnob, buttonTooltip);
            } else {
                centerKnob.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);
                centerKnob.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseHandler);
                Tooltip.uninstall(centerKnob, buttonTooltip);
            }
        } else if ("SECTIONS".equals(EVENT_TYPE)) {
            sectionsVisible = getSkinnable().getSectionsVisible();
            sections        = getSkinnable().getSections();
        }
    }

    public void handleMouseEvent(final MouseEvent EVENT) {
        if (getSkinnable().isDisabled()) return;
        final EventType TYPE = EVENT.getEventType();
        if (MouseEvent.MOUSE_PRESSED.equals(TYPE)) {
            getSkinnable().fireEvent(getSkinnable().BTN_PRESSED_EVENT);
            centerKnob.setFill(new LinearGradient(0.5 * size, 0.2708333333333333 * size,
                                                  0.5 * size, 0.7291666666666666 * size,
                                                  false, CycleMethod.NO_CYCLE,
                                                  new Stop(0.0, Color.rgb(31, 31, 31)),
                                                  new Stop(1.0, Color.rgb(69, 70, 73))));
            valueText.setTranslateY(size * 0.501);
            subTitleText.setTranslateY(size * 0.3525);
            unitText.setTranslateY(size * 0.6675);
        } else if (MouseEvent.MOUSE_RELEASED.equals(TYPE)) {
            getSkinnable().fireEvent(getSkinnable().BTN_RELEASED_EVENT);
            centerKnob.setFill(new LinearGradient(0.5 * size, 0.2708333333333333 * size,
                                                  0.5 * size, 0.7291666666666666 * size,
                                                  false, CycleMethod.NO_CYCLE,
                                                  new Stop(0.0, Color.rgb(69, 70, 73)),
                                                  new Stop(1.0, Color.rgb(31, 31, 31))));
            valueText.setTranslateY(size * 0.5);
            subTitleText.setTranslateY(size * 0.35);
            unitText.setTranslateY(size * 0.67);
        }
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle(final double VALUE) {
        angleStep          = ANGLE_RANGE / getSkinnable().getRange();
        double targetAngle = 180 - START_ANGLE + (VALUE - getSkinnable().getMinValue()) * angleStep;
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle));
        valueText.setText(String.format(locale, formatString, VALUE));
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        if (valueText.getLayoutBounds().getWidth() > 0.395 * size) {
            resizeText();
            placeTextVerticaly();
        }

        if (VALUE > getSkinnable().getThreshold()) {
            glow2.setColor(thresholdColor);
            bigGlow.setColor(thresholdColor);
        } else {
            glow2.setColor(barColor);
            bigGlow.setColor(barColor);
        }
        highlightValue(tickMarkCtx, VALUE);
    }

    private void highlightValue(final GraphicsContext CTX, final double CURRENT_VALUE) {
        CTX.clearRect(0, 0, size, size);

        // highlight tickmarks
        double     sinValue;
        double     cosValue;
        double     centerX                = size * 0.5;
        double     centerY                = size * 0.5;
        double     minorTickSpace         = getSkinnable().getMinorTickSpace();
        double     minValue               = getSkinnable().getMinValue();
        double     tmpAngleStep           = angleStep * minorTickSpace;
        BigDecimal minorTickSpaceBD       = BigDecimal.valueOf(minorTickSpace);
        BigDecimal majorTickSpaceBD       = BigDecimal.valueOf(getSkinnable().getMajorTickSpace());
        BigDecimal mediumCheck2           = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal mediumCheck5           = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal counterBD              = BigDecimal.valueOf(minValue);
        double     counter                = minValue;
        boolean    majorTickMarksVisible  = getSkinnable().getMajorTickMarksVisible();
        boolean    mediumTickMarksVisible = getSkinnable().getMediumTickMarksVisible();
        double     threshold              = getSkinnable().getThreshold();
        Color      tickMarkColor          = Color.TRANSPARENT;
        Color      highlightColor         = CURRENT_VALUE < getSkinnable().getThreshold() ? barColor : thresholdColor;

        double innerPointX;
        double innerPointY;
        double outerPointX;
        double outerPointY;
        double innerMediumPointX;
        double innerMediumPointY;
        double outerMediumPointX;
        double outerMediumPointY;

        CTX.setLineCap(StrokeLineCap.BUTT);
        CTX.setLineWidth(size * 0.0035);
        for (double angle = 0 ; Double.compare(-ANGLE_RANGE - tmpAngleStep, angle) < 0 ; angle -= tmpAngleStep) {
            sinValue = Math.sin(Math.toRadians(angle + START_ANGLE));
            cosValue = Math.cos(Math.toRadians(angle + START_ANGLE));

            innerPointX       = centerX + size * 0.375 * sinValue;
            innerPointY       = centerY + size * 0.375 * cosValue;
            outerPointX       = centerX + size * 0.425 * sinValue;
            outerPointY       = centerY + size * 0.425 * cosValue;
            innerMediumPointX = centerX + size * 0.35 * sinValue;
            innerMediumPointY = centerY + size * 0.35 * cosValue;
            outerMediumPointX = centerX + size * 0.4 * sinValue;
            outerMediumPointY = centerY + size * 0.4 * cosValue;

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw major tick mark
                if (majorTickMarksVisible) {
                    CTX.setStroke(counter < CURRENT_VALUE ? highlightColor : tickMarkColor);
                    CTX.strokeLine(innerPointX, innerPointY, outerPointX, outerPointY);
                }
            } else if (mediumTickMarksVisible &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0.0) != 0.0 &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0.0) == 0.0) {
                // Draw medium tick mark
                CTX.setStroke(counter < CURRENT_VALUE ? highlightColor : tickMarkColor);
                CTX.strokeLine(innerMediumPointX, innerMediumPointY, outerMediumPointX, outerMediumPointY);
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
        }

        // Draw black bar overlay
        double blackBarXY = (size - 0.75 * size) * 0.5;
        double blackBarWH = size * 0.75;
        CTX.save();
        CTX.setStroke(Color.rgb(23, 23, 23));
        CTX.setLineWidth(size * 0.01666667);
        CTX.setLineCap(StrokeLineCap.BUTT);
        CTX.strokeArc(blackBarXY, blackBarXY, blackBarWH, blackBarWH, BAR_START_ANGLE, -ANGLE_RANGE, ArcType.OPEN);
        CTX.restore();

        // highlight bar
        double barXY          = (size - 0.68 * size) * 0.5;
        double barWH          = size * 0.68;
        double barAngleExtend = (CURRENT_VALUE - minValue) * angleStep;
        CTX.save();
        CTX.setEffect(glow2);
        CTX.setStroke(CURRENT_VALUE < threshold ? barColor : thresholdColor);
        CTX.setLineWidth(size * 0.01666667);
        CTX.setLineCap(StrokeLineCap.BUTT);
        CTX.strokeArc(barXY, barXY, barWH, barWH, BAR_START_ANGLE, -barAngleExtend, ArcType.OPEN);
        CTX.restore();
    }

    private void drawMainCanvas() {
        mainCtx.clearRect(0, 0, size, size);
        mainCtx.setFillRule(FillRule.EVEN_ODD);

        // Draw sections if available
        final double sectionsXY = (size - 0.75 * size) * 0.5;
        final double sectionsWH = size * 0.75;
        double minValue         = getSkinnable().getMinValue();
        double maxValue         = getSkinnable().getMaxValue();
        double offset           = 90 - START_ANGLE;
        double sectionWidth     = size * 0.05;
        if (sectionsVisible) {
            int listSize = sections.size();
            for (int i = 0; i < listSize; i++) {
                final Section SECTION = sections.get(i);
                final double  SECTION_START_ANGLE;
                if (Double.compare(SECTION.getStart(), maxValue) <= 0 && Double.compare(SECTION.getStop(), minValue) >= 0) {
                    if (Double.compare(SECTION.getStart(), minValue) < 0 && Double.compare(SECTION.getStop(), maxValue) < 0) {
                        SECTION_START_ANGLE = 0;
                    } else {
                        SECTION_START_ANGLE = (SECTION.getStart() - minValue) * angleStep;
                    }
                    final double SECTION_ANGLE_EXTEND;
                    if (Double.compare(SECTION.getStop(), maxValue) > 0) {
                        SECTION_ANGLE_EXTEND = (maxValue - SECTION.getStart()) * angleStep;
                    } else {
                        SECTION_ANGLE_EXTEND = (SECTION.getStop() - SECTION.getStart()) * angleStep;
                    }
                    mainCtx.save();
                    mainCtx.setStroke(SECTION.getColor());
                    mainCtx.setLineWidth(sectionWidth);
                    mainCtx.setLineCap(StrokeLineCap.BUTT);
                    mainCtx.strokeArc(sectionsXY, sectionsXY, sectionsWH, sectionsWH, -(offset + SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.OPEN);
                    mainCtx.restore();
                }
            }
        }

        // Draw tickmarks
        mainCtx.save();
        drawTickMarks(mainCtx);
        mainCtx.restore();

        // Draw black bar overlay
        mainCtx.save();
        mainCtx.setStroke(Color.rgb(23, 23, 23));
        mainCtx.setLineWidth(size * 0.01666667);
        mainCtx.setLineCap(StrokeLineCap.BUTT);
        mainCtx.strokeArc(sectionsXY, sectionsXY, sectionsWH, sectionsWH, BAR_START_ANGLE, -ANGLE_RANGE, ArcType.OPEN);
        mainCtx.restore();

        // Draw databar background
        double barXY = (size - 0.68 * size) * 0.5;
        double barWH = size * 0.68;
        mainCtx.save();
        mainCtx.setStroke(Color.rgb(57, 57, 57, 0.75));
        mainCtx.setLineWidth(size * 0.01666667);
        mainCtx.setLineCap(StrokeLineCap.BUTT);
        mainCtx.strokeArc(barXY, barXY, barWH, barWH, BAR_START_ANGLE, -ANGLE_RANGE, ArcType.OPEN);
        mainCtx.restore();

        // Draw threshold
        if (getSkinnable().isThresholdVisible()) {
            mainCtx.save();
            mainCtx.translate(size * 0.5, size * 0.5);
            mainCtx.rotate((getSkinnable().getThreshold() * angleStep) - 120);
            mainCtx.beginPath();
            mainCtx.moveTo(0, -size * 0.33);
            mainCtx.lineTo(-size * 0.0125, -size * 0.30833333);
            mainCtx.lineTo(size * 0.0125, -size * 0.30833333);
            mainCtx.closePath();
            mainCtx.setFill(getSkinnable().getNeedleColor());
            mainCtx.fill();
            mainCtx.restore();
        }
    }

    private void drawTickMarks(final GraphicsContext CTX) {
        double               sinValue;
        double               cosValue;
        double               centerX                = size * 0.5;
        double               centerY                = size * 0.5;
        double               minorTickSpace         = getSkinnable().getMinorTickSpace();
        double               minValue               = getSkinnable().getMinValue();
        double               maxValue               = getSkinnable().getMaxValue();
        double               tmpAngleStep           = angleStep * minorTickSpace;
        int                  decimals               = getSkinnable().getTickLabelDecimals();
        BigDecimal           minorTickSpaceBD       = BigDecimal.valueOf(minorTickSpace);
        BigDecimal           majorTickSpaceBD       = BigDecimal.valueOf(getSkinnable().getMajorTickSpace());
        BigDecimal           mediumCheck2           = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal           mediumCheck5           = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal           counterBD              = BigDecimal.valueOf(minValue);
        double               counter                = minValue;
        boolean              majorTickMarksVisible  = getSkinnable().getMajorTickMarksVisible();
        boolean              mediumTickMarksVisible = getSkinnable().getMediumTickMarksVisible();
        boolean              tickLabelsVisible      = getSkinnable().getTickLabelsVisible();
        TickLabelOrientation tickLabelOrientation   = getSkinnable().getTickLabelOrientation();
        Color                tickMarkColor          = getSkinnable().getTickMarkColor();
        Color                majorTickMarkColor     = tickMarkColor;
        Color                mediumTickMarkColor    = tickMarkColor;
        Color                tickLabelColor         = getSkinnable().getTickLabelColor();

        double innerPointX;
        double innerPointY;
        double outerPointX;
        double outerPointY;
        double innerMediumPointX;
        double innerMediumPointY;
        double outerMediumPointX;
        double outerMediumPointY;
        double textPointX;
        double textPointY;

        double orthTextFactor = TickLabelOrientation.ORTHOGONAL == getSkinnable().getTickLabelOrientation() ? 0.46 : 0.46;

        Font tickLabelFont = Fonts.robotoCondensedLight((decimals == 0 ? 0.047 : 0.040) * size);
        CTX.setFont(tickLabelFont);
        CTX.setTextAlign(TextAlignment.CENTER);
        CTX.setTextBaseline(VPos.CENTER);

        CTX.setLineCap(StrokeLineCap.BUTT);
        CTX.setLineWidth(size * 0.0035);
        for (double angle = 0 ; Double.compare(-ANGLE_RANGE - tmpAngleStep, angle) < 0 ; angle -= tmpAngleStep) {
            sinValue = Math.sin(Math.toRadians(angle + START_ANGLE));
            cosValue = Math.cos(Math.toRadians(angle + START_ANGLE));

            innerPointX       = centerX + size * 0.375 * sinValue;
            innerPointY       = centerY + size * 0.375 * cosValue;
            outerPointX       = centerX + size * 0.425 * sinValue;
            outerPointY       = centerY + size * 0.425 * cosValue;
            innerMediumPointX = centerX + size * 0.35 * sinValue;
            innerMediumPointY = centerY + size * 0.35 * cosValue;
            outerMediumPointX = centerX + size * 0.4 * sinValue;
            outerMediumPointY = centerY + size * 0.4 * cosValue;
            textPointX        = centerX + size * orthTextFactor * sinValue;
            textPointY        = centerY + size * orthTextFactor * cosValue;

            // Set the general tickmark color
            CTX.setStroke(tickMarkColor);

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw major tick mark
                if (majorTickMarksVisible) {
                    CTX.setFill(majorTickMarkColor);
                    CTX.setStroke(majorTickMarkColor);
                    CTX.strokeLine(innerPointX, innerPointY, outerPointX, outerPointY);
                }
                // Draw tick label text
                if (tickLabelsVisible) {
                    CTX.save();
                    CTX.translate(textPointX, textPointY);

                    Helper.rotateContextForText(CTX, START_ANGLE, angle, tickLabelOrientation);

                    CTX.setFill(tickLabelColor);
                    if (TickLabelOrientation.HORIZONTAL == tickLabelOrientation &&
                        (Double.compare(counter, minValue) == 0 ||
                        Double.compare(counter, maxValue) == 0)) {
                            CTX.setFill(Color.TRANSPARENT);
                    }
                    CTX.fillText(String.format(locale, "%." + decimals + "f", counter), 0, 0);
                    CTX.restore();
                }
            } else if (mediumTickMarksVisible &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0.0) != 0.0 &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0.0) == 0.0) {
                // Draw medium tick mark
                CTX.setFill(mediumTickMarkColor);
                CTX.setStroke(mediumTickMarkColor);
                CTX.strokeLine(innerMediumPointX, innerMediumPointY, outerMediumPointX, outerMediumPointY);
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
        }
    }

    private void resizeText() {
        double maxWidth = 0.405 * size;
        double currentValue = (needleRotate.getAngle() + START_ANGLE - 180) / angleStep + getSkinnable().getMinValue();

        valueText.setFont(Fonts.latoRegular(size * 0.22));
        valueText.setText(String.format(locale, "%." + getSkinnable().getDecimals() + "f", currentValue));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, size * 0.22); }
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);

        titleText.setFont(Fonts.robotoCondensedRegular(size * 0.062));
        titleText.setText(getSkinnable().getTitle());
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, size * 0.062); }
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);

        maxWidth = 0.28 * size;
        subTitleText.setFont(Fonts.robotoCondensedLight(size * 0.047));
        subTitleText.setText(getSkinnable().getSubTitle());
        if (subTitleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(subTitleText, maxWidth, size * 0.047); }
        subTitleText.setTranslateX((size - subTitleText.getLayoutBounds().getWidth()) * 0.5);

        unitText.setFont(Fonts.robotoCondensedRegular(size * 0.047));
        unitText.setText(getSkinnable().getUnit());
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, size * 0.047); }
        unitText.setTranslateX((size - unitText.getLayoutBounds().getWidth()) * 0.5);
    }

    private void placeTextVerticaly() {
        valueText.setTranslateY(size * 0.5);

        titleText.setTranslateY(size * 0.83);

        subTitleText.setTranslateY(size * 0.35);

        unitText.setTranslateY(size * 0.67);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            background.setRadius(size * 0.5);
            background.setCenterX(size * 0.5);
            background.setCenterY(size * 0.5);

            mask.getElements().clear();
            mask.getElements().add(new MoveTo(0.23333333333333334 * size, 0.5 * size));
            mask.getElements().add(new CubicCurveTo(0.23333333333333334 * size, 0.3525 * size,
                                                    0.3525 * size, 0.23333333333333334 * size,
                                                    0.5 * size, 0.23333333333333334 * size));
            mask.getElements().add(new CubicCurveTo(0.6475 * size, 0.23333333333333334 * size,
                                                    0.7666666666666667 * size, 0.3525 * size,
                                                    0.7666666666666667 * size, 0.5 * size));
            mask.getElements().add(new CubicCurveTo(0.7666666666666667 * size, 0.6475 * size,
                                                    0.6475 * size, 0.7666666666666667 * size,
                                                    0.5 * size, 0.7666666666666667 * size));
            mask.getElements().add(new CubicCurveTo(0.3525 * size, 0.7666666666666667 * size,
                                                    0.23333333333333334 * size, 0.6475 * size,
                                                    0.23333333333333334 * size, 0.5 * size));
            mask.getElements().add(new ClosePath());
            mask.getElements().add(new MoveTo(0.2 * size, 0.5 * size));
            mask.getElements().add(new CubicCurveTo(0.2 * size, 0.555 * size,
                                                    0.215 * size, 0.6058333333333333 * size,
                                                    0.24 * size, 0.65 * size));
            mask.getElements().add(new CubicCurveTo(0.24 * size, 0.65 * size,
                                                    0.12583333333333332 * size, 0.7166666666666667 * size,
                                                    0.12583333333333332 * size, 0.7166666666666667 * size));
            mask.getElements().add(new CubicCurveTo(0.205 * size, 0.8541666666666666 * size,
                                                    0.3408333333333333 * size, 0.9333333333333333 * size,
                                                    0.5 * size, 0.9333333333333333 * size));
            mask.getElements().add(new CubicCurveTo(0.6591666666666667 * size, 0.9333333333333333 * size,
                                                    0.795 * size, 0.8541666666666666 * size,
                                                    0.8741666666666666 * size, 0.7166666666666667 * size));
            mask.getElements().add(new CubicCurveTo(0.8741666666666666 * size, 0.7166666666666667 * size,
                                                    0.76 * size, 0.65 * size,
                                                    0.76 * size, 0.65 * size));
            mask.getElements().add(new CubicCurveTo(0.785 * size, 0.6058333333333333 * size,
                                                    0.8 * size, 0.555 * size,
                                                    0.8 * size, 0.5 * size));
            mask.getElements().add(new CubicCurveTo(0.8 * size, 0.33416666666666667 * size,
                                                    0.6658333333333334 * size, 0.2 * size,
                                                    0.5 * size, 0.2 * size));
            mask.getElements().add(new CubicCurveTo(0.33416666666666667 * size, 0.2 * size,
                                                    0.2 * size, 0.33416666666666667 * size,
                                                    0.2 * size, 0.5 * size));
            mask.getElements().add(new ClosePath());

            dropShadow4.setOffsetY(0.014 * size);
            dropShadow4.setRadius(0.014 * size);

            needleMoveTo1.setX(0.5008333333333334 * size); needleMoveTo1.setY(0.0775 * size);

            needleCubicCurveTo2.setControlX1(0.5008333333333334 * size); needleCubicCurveTo2.setControlY1(0.0775 * size);
            needleCubicCurveTo2.setControlX2(0.5416666666666666 * size); needleCubicCurveTo2.setControlY2(0.0025 * size);
            needleCubicCurveTo2.setX(0.5416666666666666 * size); needleCubicCurveTo2.setY(0.0025 * size);

            needleCubicCurveTo3.setControlX1(0.5125 * size); needleCubicCurveTo3.setControlY1(0);
            needleCubicCurveTo3.setControlX2(0.4875 * size); needleCubicCurveTo3.setControlY2(0);
            needleCubicCurveTo3.setX(0.4583333333333333 * size); needleCubicCurveTo3.setY(0.0025 * size);

            needleCubicCurveTo4.setControlX1(0.4583333333333333 * size); needleCubicCurveTo4.setControlY1(0.0025 * size);
            needleCubicCurveTo4.setControlX2(0.49833333333333335 * size); needleCubicCurveTo4.setControlY2(0.0775 * size);
            needleCubicCurveTo4.setX(0.49833333333333335 * size); needleCubicCurveTo4.setY(0.0775 * size);

            needleLineTo5.setX(0.49833333333333335 * size); needleLineTo5.setY(0.0775 * size);

            needleLineTo6.setX(0.49833333333333335 * size); needleLineTo6.setY(0.17916666666666667 * size);

            needleLineTo7.setX(0.5008333333333334 * size); needleLineTo7.setY(0.17916666666666667 * size);

            needleLineTo8.setX(0.5008333333333334 * size); needleLineTo8.setY(0.0775 * size);

            needle.relocate(needle.getLayoutBounds().getMinX(), needle.getLayoutBounds().getMinY());
            needleRotate.setPivotX(size * 0.5);
            needleRotate.setPivotY(size * 0.5);

            mainCanvas.setCache(false);
            mainCanvas.setWidth(size);
            mainCanvas.setHeight(size);
            drawMainCanvas();
            mainCanvas.setCache(true);
            mainCanvas.setCacheHint(CacheHint.QUALITY);

            tickMarkCanvas.setWidth(size);
            tickMarkCanvas.setHeight(size);
            highlightValue(tickMarkCtx, getSkinnable().getValue());

            centerKnob.setRadius(0.22916667 * size);
            centerKnob.setCenterX(0.5 * size);
            centerKnob.setCenterY(0.5 * size);
            centerKnob.setFill(new LinearGradient(0.5 * size, 0.2708333333333333 * size,
                                                  0.5 * size, 0.7291666666666666 * size,
                                                  false, CycleMethod.NO_CYCLE,
                                                  new Stop(0.0, Color.rgb(69,70,73)),
                                                  new Stop(1.0, Color.rgb(31,31,31))));
            centerKnob.setEffect(dropShadow4);

            glow1.setRadius(0.085 * size);
            glow2.setRadius(0.085 * size);
            bigGlow.setRadius(0.25 * size);

            resizeText();
            placeTextVerticaly();
        }
    }

    private void redraw() {
        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        needle.setFill(getSkinnable().getNeedleColor());
        titleText.setFill(getSkinnable().getTitleColor());
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        unitText.setFill(getSkinnable().getUnitColor());
        valueText.setFill(getSkinnable().getValueColor());
        buttonTooltip.setText(getSkinnable().getButtonTooltipText());
        barColor       = getSkinnable().getBarColor();
        thresholdColor = getSkinnable().getThresholdColor();
        resizeText();
    }
}
