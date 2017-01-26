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

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.AngleConicalGradient;
import eu.hansolo.medusa.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by hansolo on 21.01.16.
 */
public class TinySkin extends GaugeSkinBase {
    private static final double  ANGLE_RANGE      = 270;
    private double               size;
    private double               oldValue;
    private Arc                  barBackground;
    private Canvas               sectionCanvas;
    private GraphicsContext      sectionCtx;
    private Path                 needle;
    private MoveTo               needleMoveTo1;
    private CubicCurveTo         needleCubicCurveTo2;
    private CubicCurveTo         needleCubicCurveTo3;
    private CubicCurveTo         needleCubicCurveTo4;
    private CubicCurveTo         needleCubicCurveTo5;
    private ClosePath            needleClosePath6;
    private MoveTo               needleMoveTo7;
    private CubicCurveTo         needleCubicCurveTo8;
    private CubicCurveTo         needleCubicCurveTo9;
    private CubicCurveTo         needleCubicCurveTo10;
    private CubicCurveTo         needleCubicCurveTo11;
    private ClosePath            needleClosePath12;
    private Rotate               needleRotate;
    private Pane                 pane;
    private double               minValue;
    private double               maxValue;
    private double               range;
    private double               angleStep;
    private boolean              colorGradientEnabled;
    private int                  noOfGradientStops;
    private List<Section>        sections;
    private Tooltip              needleTooltip;
    private String               formatString;
    private Locale               locale;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public TinySkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        oldValue             = gauge.getValue();
        minValue             = gauge.getMinValue();
        maxValue             = gauge.getMaxValue();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sections             = gauge.getSections();
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale               = gauge.getLocale();
        currentValueListener = o -> rotateNeedle(gauge.getCurrentValue());

        initGraphics();
        registerListeners();

        rotateNeedle(gauge.getCurrentValue());

        redraw();
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

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, ANGLE_RANGE * 0.5 + 90, -ANGLE_RANGE);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(gauge.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        sectionCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionCtx    = sectionCanvas.getGraphicsContext2D();

        needleRotate = new Rotate((gauge.getValue() - oldValue - minValue) * angleStep);

        needleMoveTo1        = new MoveTo();
        needleCubicCurveTo2  = new CubicCurveTo();
        needleCubicCurveTo3  = new CubicCurveTo();
        needleCubicCurveTo4  = new CubicCurveTo();
        needleCubicCurveTo5  = new CubicCurveTo();
        needleClosePath6     = new ClosePath();
        needleMoveTo7        = new MoveTo();
        needleCubicCurveTo8  = new CubicCurveTo();
        needleCubicCurveTo9  = new CubicCurveTo();
        needleCubicCurveTo10 = new CubicCurveTo();
        needleCubicCurveTo11 = new CubicCurveTo();
        needleClosePath12    = new ClosePath();
        needle = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4, needleCubicCurveTo5, needleClosePath6,
                          needleMoveTo7, needleCubicCurveTo8, needleCubicCurveTo9, needleCubicCurveTo10, needleCubicCurveTo11, needleClosePath12);
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setFill(gauge.getNeedleColor());
        needle.setStrokeType(StrokeType.INSIDE);
        needle.setStrokeWidth(1);
        needle.setStroke(gauge.getBackgroundPaint());

        needleTooltip = new Tooltip(String.format(locale, formatString, gauge.getValue()));
        needleTooltip.setTextAlignment(TextAlignment.CENTER);
        Tooltip.install(needle, needleTooltip);

        pane = new Pane(barBackground, sectionCanvas, needle);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(o -> rotateNeedle(gauge.getCurrentValue()));
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = gauge.getMinValue();
            maxValue  = gauge.getMaxValue();
            range     = gauge.getRange();
            sections  = gauge.getSections();
            angleStep = ANGLE_RANGE / range;
            redraw();
            rotateNeedle(gauge.getCurrentValue());
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            needleTooltip.setText(String.format(locale, formatString, gauge.getValue()));
        }
    }
    
    private void rotateNeedle(final double VALUE) {
        double needleStartAngle = ANGLE_RANGE * 0.5;
        double targetAngle      = (VALUE - minValue) * angleStep - needleStartAngle;
        targetAngle = Helper.clamp(-needleStartAngle, -needleStartAngle + ANGLE_RANGE, targetAngle);
        needleRotate.setAngle(targetAngle);
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(o -> rotateNeedle(gauge.getCurrentValue()));
        super.dispose();
    }


    // ******************** Drawing *******************************************
    private void drawSections() {
        if (sections.isEmpty()) return;
        sectionCtx.clearRect(0, 0, size, size);
        double xy       = size * 0.1875;
        double wh       = size * 0.625;
        double offset   = -ANGLE_RANGE * 0.5 - 90;
        int    listSize = sections.size();
        for (int i = 0 ; i < listSize ; i++) {
            Section section = sections.get(i);
            double sectionStartAngle;
            if (Double.compare(section.getStart(), maxValue) <= 0 && Double.compare(section.getStop(), minValue) >= 0) {
                if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                    sectionStartAngle = 0;
                } else {
                    sectionStartAngle = (section.getStart() - minValue) * angleStep;
                }
                double sectionAngleExtend;
                if (Double.compare(section.getStop(), maxValue) > 0) {
                    sectionAngleExtend = (maxValue - section.getStart()) * angleStep;
                } else if (Double.compare(section.getStart(), minValue) < 0) {
                    sectionAngleExtend = (section.getStop() - minValue) * angleStep;
                } else {
                    sectionAngleExtend = (section.getStop() - section.getStart()) * angleStep;
                }
                sectionCtx.save();
                sectionCtx.setStroke(section.getColor());
                sectionCtx.setLineWidth(size * 0.18382353);
                sectionCtx.setLineCap(StrokeLineCap.BUTT);
                sectionCtx.strokeArc(xy, xy, wh, wh, -(offset + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
                sectionCtx.restore();
            }
        }
    }
    
    private void drawGradientBar() {
        double             xy           = size * 0.1875;
        double             wh           = size * 0.625;
        double             offset       = -ANGLE_RANGE * 0.5 - 90;
        double             startAngle   = 315;
        List<Stop>         stops        = gauge.getGradientBarStops();
        Map<Double, Color> stopAngleMap = new HashMap<>(stops.size());
        for (Stop stop : stops) { stopAngleMap.put(stop.getOffset() * ANGLE_RANGE, stop.getColor()); }
        double               offsetFactor = startAngle - 90;
        AngleConicalGradient gradient     = new AngleConicalGradient(size * 0.5, size * 0.5, offsetFactor, stopAngleMap, ScaleDirection.CLOCKWISE);

        double barStartAngle  = 0;
        double barAngleExtend = 270;
        sectionCtx.save();
        sectionCtx.setStroke(gradient.getImagePattern(new Rectangle(xy - 0.09191176 * size, xy - 0.09191176 * size, wh + 0.18382353 * size, wh + 0.18382353 * size)));
        sectionCtx.setLineWidth(size * 0.18382353);
        sectionCtx.setLineCap(StrokeLineCap.BUTT);
        sectionCtx.strokeArc(xy, xy, wh, wh, -(offset + barStartAngle), -barAngleExtend, ArcType.OPEN);
        sectionCtx.restore();    
    }

    private void drawTickMarks() {
        double     sinValue;
        double     cosValue;
        double     centerX               = size * 0.5;
        double     centerY               = size * 0.5;
        double     minorTickSpace        = gauge.getMinorTickSpace();
        double     tmpAngleStep          = angleStep * minorTickSpace;
        BigDecimal minorTickSpaceBD      = BigDecimal.valueOf(minorTickSpace);
        BigDecimal majorTickSpaceBD      = BigDecimal.valueOf(gauge.getMajorTickSpace());
        BigDecimal counterBD             = BigDecimal.valueOf(minValue);
        double     counter               = minValue;
        Color      tickMarkColor         = gauge.getTickMarkColor();
        Color      majorTickMarkColor    = gauge.getMajorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : gauge.getMajorTickMarkColor();
        double     majorDotSize          = 0.025 * size;
        double     majorHalfDotSize      = majorDotSize * 0.5;
        double     dotCenterX;
        double     dotCenterY;

        // Main loop
        BigDecimal     tmpStepBD      = new BigDecimal(tmpAngleStep);
        tmpStepBD                     = tmpStepBD.setScale(3, BigDecimal.ROUND_HALF_UP);
        double tmpStep                = tmpStepBD.doubleValue();
        double angle                  = 0;
        double startAngle             = -45;
        for (double i = 0 ; Double.compare(-ANGLE_RANGE - tmpStep, i) <= 0 ; i -= tmpStep) {
            sinValue    = Math.sin(Math.toRadians(angle + startAngle));
            cosValue    = Math.cos(Math.toRadians(angle + startAngle));
            dotCenterX  = centerX + size * 0.3125 * sinValue;
            dotCenterY  = centerY + size * 0.3125 * cosValue;
            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                if ((Double.compare(counter, minValue) == 0 || Double.compare(counter, maxValue) == 0)) {
                    sectionCtx.setFill(Color.TRANSPARENT);
                } else {
                    sectionCtx.setFill(majorTickMarkColor);
                }
                Helper.drawDot(sectionCtx, dotCenterX - majorHalfDotSize, dotCenterY - majorHalfDotSize, majorDotSize);
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
            if (counter > maxValue) break;
            angle -= tmpAngleStep;
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size = width < height ? width : height;

        if (size > 0 ) {
            double centerX   = size * 0.5;
            double centerY   = size * 0.5;
            double barRadius = size * 0.3125;
            double barWidth  = size * 0.18382353;

            pane.setMaxSize(size, size);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

            barBackground.setCenterX(centerX);
            barBackground.setCenterY(centerY);
            barBackground.setRadiusX(barRadius);
            barBackground.setRadiusY(barRadius);
            barBackground.setStrokeWidth(barWidth);
            barBackground.setStartAngle(ANGLE_RANGE * 0.5 + 90);
            barBackground.setLength(-ANGLE_RANGE);

            sectionCanvas.setWidth(size);
            sectionCanvas.setHeight(size);

            // Areas, Sections and Tick Marks
            sectionCanvas.setCache(false);
            sectionCtx.clearRect(0, 0, size, size);
            if (gauge.isGradientBarEnabled() && gauge.getGradientLookup() != null) {
                drawGradientBar();
                if (gauge.getMajorTickMarksVisible()) drawTickMarks();
            } else if (gauge.getSectionsVisible()) {
                drawSections();
                if (gauge.getMajorTickMarksVisible()) drawTickMarks();
            }
            sectionCanvas.setCache(true);
            sectionCanvas.setCacheHint(CacheHint.QUALITY);

            double needleWidth  = size * 0.26470588;
            double needleHeight = size * 0.47426471;

            needle.setCache(false);

            needleMoveTo1.setX(0.277777777777778 * needleWidth); needleMoveTo1.setY(0.720930232558139 * needleHeight);

            needleCubicCurveTo2.setControlX1(0.277777777777778 * needleWidth); needleCubicCurveTo2.setControlY1(0.652428682170543 * needleHeight);
            needleCubicCurveTo2.setControlX2(0.377268055555556 * needleWidth); needleCubicCurveTo2.setControlY2(0.596899224806202 * needleHeight);
            needleCubicCurveTo2.setX(0.5 * needleWidth); needleCubicCurveTo2.setY(0.596899224806202 * needleHeight);

            needleCubicCurveTo3.setControlX1(0.622731944444444 * needleWidth); needleCubicCurveTo3.setControlY1(0.596899224806202 * needleHeight);
            needleCubicCurveTo3.setControlX2(0.722222222222222 * needleWidth); needleCubicCurveTo3.setControlY2(0.652428682170543 * needleHeight);
            needleCubicCurveTo3.setX(0.722222222222222 * needleWidth); needleCubicCurveTo3.setY(0.720930232558139 * needleHeight);

            needleCubicCurveTo4.setControlX1(0.722222222222222 * needleWidth); needleCubicCurveTo4.setControlY1(0.789431782945736 * needleHeight);
            needleCubicCurveTo4.setControlX2(0.622731944444444 * needleWidth); needleCubicCurveTo4.setControlY2(0.844961240310077 * needleHeight);
            needleCubicCurveTo4.setX(0.5 * needleWidth); needleCubicCurveTo4.setY(0.844961240310077 * needleHeight);

            needleCubicCurveTo5.setControlX1(0.377268055555556 * needleWidth); needleCubicCurveTo5.setControlY1(0.844961240310077 * needleHeight);
            needleCubicCurveTo5.setControlX2(0.277777777777778 * needleWidth); needleCubicCurveTo5.setControlY2(0.789431782945736 * needleHeight);
            needleCubicCurveTo5.setX(0.277777777777778 * needleWidth); needleCubicCurveTo5.setY(0.720930232558139 * needleHeight);

            needleMoveTo7.setX(0); needleMoveTo7.setY(0.720930232558139 * needleHeight);
            
            needleCubicCurveTo8.setControlX1(0); needleCubicCurveTo8.setControlY1(0.875058139534884 * needleHeight);
            needleCubicCurveTo8.setControlX2(0.223854166666667 * needleWidth); needleCubicCurveTo8.setControlY2(needleHeight);
            needleCubicCurveTo8.setX(0.5 * needleWidth); needleCubicCurveTo8.setY(needleHeight);

            needleCubicCurveTo9.setControlX1(0.776145833333333 * needleWidth); needleCubicCurveTo9.setControlY1(needleHeight);
            needleCubicCurveTo9.setControlX2(needleWidth); needleCubicCurveTo9.setControlY2(0.875058139534884 * needleHeight);
            needleCubicCurveTo9.setX(needleWidth); needleCubicCurveTo9.setY(0.720930232558139 * needleHeight);

            needleCubicCurveTo10.setControlX1(needleWidth); needleCubicCurveTo10.setControlY1(0.566860465116279 * needleHeight);
            needleCubicCurveTo10.setControlX2(0.5 * needleWidth); needleCubicCurveTo10.setControlY2(0);
            needleCubicCurveTo10.setX(0.5 * needleWidth); needleCubicCurveTo10.setY(0);

            needleCubicCurveTo11.setControlX1(0.5 * needleWidth); needleCubicCurveTo11.setControlY1(0);
            needleCubicCurveTo11.setControlX2(0); needleCubicCurveTo11.setControlY2(0.566860465116279 * needleHeight);
            needleCubicCurveTo11.setX(0); needleCubicCurveTo11.setY(0.720930232558139 * needleHeight);

            needle.setCache(true);
            needle.setCacheHint(CacheHint.ROTATE);

            needle.relocate((size - needle.getLayoutBounds().getWidth()) * 0.5, centerY - needle.getLayoutBounds().getHeight() + needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight() - needle.getLayoutBounds().getWidth() * 0.5);
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        locale               = gauge.getLocale();
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();

        barBackground.setStroke(gauge.getBarBackgroundColor());

        // Areas, Sections and Tick Marks
        sectionCanvas.setCache(false);
        sectionCtx.clearRect(0, 0, size, size);
        if (gauge.isGradientBarEnabled() && gauge.getGradientLookup() != null) {
            drawGradientBar();
            if (gauge.getMajorTickMarksVisible()) drawTickMarks();
        } else if (gauge.getSectionsVisible()) {
            drawSections();
            if (gauge.getMajorTickMarksVisible()) drawTickMarks();
        }
        sectionCanvas.setCache(true);
        sectionCanvas.setCacheHint(CacheHint.QUALITY);

        needle.setFill(gauge.getNeedleColor());
    }
}
