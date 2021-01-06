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
package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.tools.Helper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 08.02.16.
 */
public class DigitalSkin extends GaugeSkinBase {
    private static final double  START_ANGLE = -30;
    private static final double  ANGLE_RANGE = 300;
    private double               size;
    private double               center;
    private Pane                 pane;
    private Canvas               backgroundCanvas;
    private GraphicsContext      backgroundCtx;
    private Canvas               barCanvas;
    private GraphicsContext      barCtx;
    private Text                 valueBkgText;
    private Text                 valueText;
    private Color                barColor;
    private Color                valueColor;
    private Color                titleColor;
    private Color                subTitleColor;
    private Color                unitColor;
    private double               minValue;
    private double               maxValue;
    private double               range;
    private double               angleStep;
    private boolean              isStartFromZero;
    private double               barWidth;
    private Locale               locale;
    private boolean              sectionsVisible;
    private List<Section>        sections;
    private boolean              thresholdVisible;
    private Color                thresholdColor;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public DigitalSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        minValue             = gauge.getMinValue();
        maxValue             = gauge.getMaxValue();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        locale               = gauge.getLocale();
        barColor             = gauge.getBarColor();
        valueColor           = gauge.getValueColor();
        titleColor           = gauge.getTitleColor();
        subTitleColor        = gauge.getSubTitleColor();
        unitColor            = gauge.getUnitColor();
        isStartFromZero      = gauge.isStartFromZero();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        thresholdVisible     = gauge.isThresholdVisible();
        thresholdColor       = gauge.getThresholdColor();
        currentValueListener = o -> setBar(gauge.getCurrentValue());

        initGraphics();
        registerListeners();

        setBar(gauge.getCurrentValue());
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

        backgroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        backgroundCtx    = backgroundCanvas.getGraphicsContext2D();
        
        barCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        barCtx    = barCanvas.getGraphicsContext2D();

        valueBkgText = new Text();
        valueBkgText.setStroke(null);
        valueBkgText.setFill(Helper.getTranslucentColorFrom(valueColor, 0.1));

        valueText = new Text();
        valueText.setStroke(null);
        valueText.setFill(valueColor);
        Helper.enableNode(valueText, gauge.isValueVisible());

        pane = new Pane(backgroundCanvas, barCanvas, valueBkgText, valueText);
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth()))));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = gauge.getMinValue();
            maxValue  = gauge.getMaxValue();
            range     = gauge.getRange();
            angleStep = ANGLE_RANGE / range;
            redraw();
            setBar(gauge.getCurrentValue());
        } else if ("SECTIONS".equals(EVENT_TYPE)) {
            sections         = gauge.getSections();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            sectionsVisible  = gauge.getSectionsVisible();
            thresholdVisible = gauge.isThresholdVisible();
            thresholdVisible = gauge.isThresholdVisible();
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Canvas ********************************************
    private void setBar(final double VALUE) {
        barCtx.clearRect(0, 0, size, size);
        barCtx.setLineCap(StrokeLineCap.BUTT);
        barCtx.setStroke(barColor);
        barCtx.setLineWidth(barWidth);

        if (sectionsVisible) {
            int listSize = sections.size();
            for (int i = 0 ; i < listSize ;i++) {
                Section section = sections.get(i);
                if (section.contains(VALUE)) {
                    barCtx.setStroke(section.getColor());
                    break;
                }
            }
        }

        if (thresholdVisible && VALUE > gauge.getThreshold()) {
            barCtx.setStroke(thresholdColor);
        }

        double v             = (VALUE - minValue) * angleStep;
        int    minValueAngle = (int) (-minValue * angleStep);
        if (!isStartFromZero) {
            for (int i = 0; i < 300; i++) {
                if (i % 6 == 0 && i < v) {
                    barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.3, barWidth * 0.5 + barWidth * 0.3, size - barWidth - barWidth * 0.6, size - barWidth - barWidth * 0.6, (-i - 125), 4.6, ArcType.OPEN);
                }
            }
        } else {
            if (Double.compare(VALUE, 0) != 0) {
                if (VALUE < 0) {
                    for (int i = Math.min(minValueAngle, 300) - 1; i >= 0; i--) {
                        if (i % 6 == 0 && i > v - 6) {
                            barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.3, barWidth * 0.5 + barWidth * 0.3, size - barWidth - barWidth * 0.6, size - barWidth - barWidth * 0.6, (-i - 125), 4.6, ArcType.OPEN);
                        }
                    }
                } else {
                    for (int i = Math.max(minValueAngle, 0) - 3; i < 300; i++) {
                        if (i % 6 == 0 && i < v) {
                            barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.3, barWidth * 0.5 + barWidth * 0.3, size - barWidth - barWidth * 0.6, size - barWidth - barWidth * 0.6, (-i - 125), 4.6, ArcType.OPEN);
                        }
                    }
                }
            }
        }
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        valueText.setLayoutX(valueBkgText.getLayoutBounds().getMaxX() - valueText.getLayoutBounds().getWidth());
    }

    private void drawBackground() {
        backgroundCanvas.setCache(false);
        double outerBarWidth = size * 0.006;
        backgroundCtx.setLineCap(StrokeLineCap.BUTT);
        backgroundCtx.clearRect(0, 0, size, size);

        boolean shadowsEnabled = gauge.isShadowsEnabled();

        // draw translucent background
        backgroundCtx.setStroke(Color.rgb(0, 12, 6, 0.1));
        Color bColor = Helper.getTranslucentColorFrom(barColor, 0.1);
        for (int i = -60 ; i < 240 ; i++) {
            backgroundCtx.save();
            if (i % 6 == 0) {
                // draw value bar
                backgroundCtx.setStroke(bColor);
                backgroundCtx.setLineWidth(barWidth);
                backgroundCtx.strokeArc(barWidth * 0.5 + barWidth * 0.3, barWidth * 0.5 + barWidth * 0.3, size - barWidth - barWidth * 0.6, size - barWidth - barWidth * 0.6, i + 1, 4.6, ArcType.OPEN);

                // draw outer bar
                backgroundCtx.setStroke(barColor);
                backgroundCtx.setLineWidth(outerBarWidth);
                backgroundCtx.strokeArc(outerBarWidth, outerBarWidth, size - (2 * outerBarWidth), size - (2 * outerBarWidth), i + 1, 4.6, ArcType.OPEN);
            }
            backgroundCtx.restore();
        }

        // draw the title
        if (!gauge.getTitle().isEmpty()) {
            String title        = gauge.getTitle();
            char[] bkgTitleChrs = new char[title.length()];
            Arrays.fill(bkgTitleChrs, '8');
            backgroundCtx.setFill(shadowsEnabled ? Helper.getTranslucentColorFrom(titleColor, 0.1) : Color.TRANSPARENT);
            backgroundCtx.setFont(Fonts.digitalReadoutBold(0.09 * size));
            backgroundCtx.setTextBaseline(VPos.CENTER);
            backgroundCtx.setTextAlign(TextAlignment.CENTER);
            backgroundCtx.fillText(new String(bkgTitleChrs), center, size * 0.35, size * 0.55);
            backgroundCtx.setFill(titleColor);
            backgroundCtx.fillText(title, center, size * 0.35, size * 0.55);
        }

        // draw the subtitle
        if (!gauge.getSubTitle().isEmpty()) {
            String subTitle        = gauge.getSubTitle();
            char[] bkgSubTitleChrs = new char[subTitle.length()];
            Arrays.fill(bkgSubTitleChrs, '8');
            backgroundCtx.setFill(shadowsEnabled ? Helper.getTranslucentColorFrom(subTitleColor, 0.1) : Color.TRANSPARENT);
            backgroundCtx.setFont(Fonts.digital(0.09 * size));
            backgroundCtx.fillText(new String(bkgSubTitleChrs), center, size * 0.66);
            backgroundCtx.setFill(subTitleColor);
            backgroundCtx.fillText(subTitle, center, size * 0.66);
        }

        // draw the unit
        if (!gauge.getUnit().isEmpty()) {
            String unit        = gauge.getUnit();
            char[] bkgUnitChrs = new char[unit.length()];
            Arrays.fill(bkgUnitChrs, '8');
            backgroundCtx.setFill(shadowsEnabled ? Helper.getTranslucentColorFrom(unitColor, 0.1) : Color.TRANSPARENT);
            backgroundCtx.setFont(Fonts.digital(0.09 * size));
            backgroundCtx.fillText(new String(bkgUnitChrs), center, size * 0.88);
            backgroundCtx.setFill(unitColor);
            backgroundCtx.fillText(unit, center, size * 0.88);
        }

        // draw tick labels
        drawTickMarks();

        backgroundCanvas.setCache(true);
        backgroundCanvas.setCacheHint(CacheHint.QUALITY);

        // draw the value
        if (gauge.isValueVisible()) {
            StringBuilder valueBkg = new StringBuilder();
            int len = String.valueOf((int) gauge.getMaxValue()).length();
            if (gauge.getMinValue() < 0) { len++; }
            for (int i = 0 ; i < len ; i++) { valueBkg.append("8"); }
            if (gauge.getDecimals() > 0) {
                valueBkg.append(".");
                len = gauge.getDecimals();
                for (int i = 0 ; i < len ; i++) { valueBkg.append("8"); }
            }
            valueBkgText.setText(valueBkg.toString());
            valueBkgText.setX((size - valueBkgText.getLayoutBounds().getWidth()) * 0.5);
        }
    }
    
    private void drawTickMarks() {
        double        sinValue;
        double        cosValue;
        double        centerX                      = center;
        double        centerY                      = center;
        int           tickLabelDecimals            = gauge.getTickLabelDecimals();
        String        tickLabelFormatString        = "%." + tickLabelDecimals + "f";
        double        minorTickSpace               = gauge.getMinorTickSpace();
        double        tmpAngleStep                 = angleStep * minorTickSpace;
        BigDecimal    minorTickSpaceBD             = BigDecimal.valueOf(minorTickSpace);
        BigDecimal    majorTickSpaceBD             = BigDecimal.valueOf(gauge.getMajorTickSpace());
        BigDecimal    counterBD                    = BigDecimal.valueOf(minValue);
        double        counter                      = minValue;
        Color         tickMarkColor                = gauge.getTickMarkColor();
        Color         majorTickMarkColor           = gauge.getMajorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : gauge.getMajorTickMarkColor();
        Color         tickLabelColor               = gauge.getTickLabelColor();
        Color         zeroColor                    = gauge.getZeroColor();
        boolean       isNotZero                    = true;
        boolean       majorTickMarksVisible        = gauge.getMajorTickMarksVisible();
        boolean       tickLabelsVisible            = gauge.getTickLabelsVisible();
        boolean       onlyFirstAndLastLabelVisible = gauge.isOnlyFirstAndLastTickLabelVisible();
        double        customFontSizeFactor         = gauge.getCustomTickLabelFontSize() / 400;
        boolean       fullRange                    = (minValue < 0 && maxValue > 0);
        double        tickLabelOrientationFactor   = 0.8;
        double        tickLabelFontSize            = tickLabelDecimals == 0 ? 0.051 * size : 0.048 * size;
        tickLabelFontSize = gauge.getCustomTickLabelsEnabled() ? customFontSizeFactor * size : tickLabelFontSize;

        Font tickLabelFont     = Fonts.robotoCondensedRegular(tickLabelFontSize * tickLabelOrientationFactor);
        Font tickLabelZeroFont = fullRange ? Fonts.robotoCondensedBold(tickLabelFontSize * tickLabelOrientationFactor) : tickLabelFont;

        // Variables needed for tickmarks
        double innerPointX;
        double innerPointY;
        double triangleOuterAngle1;
        double triangleOuterAngle2;
        double triangleOuterPoint1X;
        double triangleOuterPoint1Y;
        double triangleOuterPoint2X;
        double triangleOuterPoint2Y;
        double textPointX;
        double textPointY;

        backgroundCtx.setStroke(tickMarkColor);
        backgroundCtx.strokeArc(0.0875 * size, 0.0875 * size, 0.825 * size, 0.825 * size, 300, 300, ArcType.OPEN);

        // Main loop
        BigDecimal tmpStepBD = new BigDecimal(tmpAngleStep);
        tmpStepBD = tmpStepBD.setScale(3, BigDecimal.ROUND_HALF_UP);
        double tmpStep = tmpStepBD.doubleValue();
        double angle   = 0;
        for (double i = 0 ; Double.compare(-ANGLE_RANGE - tmpStep, i) <= 0 ; i -= tmpStep) {
            sinValue = Math.sin(Math.toRadians(angle + START_ANGLE));
            cosValue = Math.cos(Math.toRadians(angle + START_ANGLE));

            innerPointX          = centerX + size * 0.395 * sinValue;
            innerPointY          = centerY + size * 0.395 * cosValue;
            triangleOuterAngle1  = Math.toRadians(angle - 1.2 + START_ANGLE);
            triangleOuterAngle2  = Math.toRadians(angle + 1.2 + START_ANGLE);
            triangleOuterPoint1X = centerX + size * 0.4125 * Math.sin(triangleOuterAngle1);
            triangleOuterPoint1Y = centerY + size * 0.4125 * Math.cos(triangleOuterAngle1);
            triangleOuterPoint2X = centerX + size * 0.4125 * Math.sin(triangleOuterAngle2);
            triangleOuterPoint2Y = centerY + size * 0.4125 * Math.cos(triangleOuterAngle2);
            textPointX           = centerX + size * 0.365 * sinValue;
            textPointY           = centerY + size * 0.365 * cosValue;

            // Set the general tickmark color
            backgroundCtx.setStroke(tickMarkColor);
            backgroundCtx.setFill(tickMarkColor);
            backgroundCtx.setLineCap(StrokeLineCap.BUTT);

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                isNotZero = Double.compare(0.0, counter) != 0;
                if (majorTickMarksVisible) {
                    backgroundCtx.setFill(majorTickMarkColor);
                    backgroundCtx.setStroke(majorTickMarkColor);
                    backgroundCtx.setLineWidth(size * 0.0055);
                    backgroundCtx.setLineCap(StrokeLineCap.BUTT);
                }
                if (fullRange && !isNotZero) {
                    backgroundCtx.setFill(zeroColor);
                    backgroundCtx.setStroke(zeroColor);
                }
                if (majorTickMarksVisible) {
                    Helper.drawTriangle(backgroundCtx, innerPointX, innerPointY, triangleOuterPoint1X, triangleOuterPoint1Y, triangleOuterPoint2X, triangleOuterPoint2Y);
                }

                // Draw tick label text
                if (tickLabelsVisible) {
                    backgroundCtx.save();
                    backgroundCtx.translate(textPointX, textPointY);

                    Helper.rotateContextForText(backgroundCtx, START_ANGLE, angle, TickLabelOrientation.HORIZONTAL);
                    backgroundCtx.setFont(isNotZero ? tickLabelFont : tickLabelZeroFont);
                    backgroundCtx.setTextAlign(TextAlignment.CENTER);
                    backgroundCtx.setTextBaseline(VPos.CENTER);

                    if (!onlyFirstAndLastLabelVisible) {
                        if (isNotZero) {
                            backgroundCtx.setFill(tickLabelColor);
                        } else {
                            backgroundCtx.setFill(fullRange ? zeroColor : tickLabelColor);
                        }
                    } else {
                        if ((Double.compare(counter, minValue) == 0 || Double.compare(counter, maxValue) == 0)) {
                            if (isNotZero) {
                                backgroundCtx.setFill(tickLabelColor);
                            } else {
                                backgroundCtx.setFill(fullRange ? zeroColor : tickLabelColor);
                            }
                        } else {
                            backgroundCtx.setFill(Color.TRANSPARENT);
                        }
                    }
                    backgroundCtx.fillText(String.format(locale, tickLabelFormatString, counter), 0, 0);
                    backgroundCtx.restore();
                }
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
            if (counter > maxValue) break;
            angle     = (angle - tmpAngleStep);
        }
    }


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            center   = size * 0.5;
            barWidth = size * 0.06;

            backgroundCanvas.setWidth(size);
            backgroundCanvas.setHeight(size);

            barCanvas.setWidth(size);
            barCanvas.setHeight(size);

            valueBkgText.setFont(Fonts.digital(0.18 * size));
            valueBkgText.setY(center + (valueBkgText.getLayoutBounds().getHeight() * 0.5));

            valueText.setFont(Fonts.digital(0.18 * size));
            valueText.setY(center + (valueText.getLayoutBounds().getHeight() * 0.5));

            drawBackground();
            setBar(gauge.getCurrentValue());
        }
    }

    @Override protected void redraw() {
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));
        
        locale          = gauge.getLocale();
        barColor        = gauge.getBarColor();
        valueColor      = gauge.getValueColor();
        titleColor      = gauge.getTitleColor();
        subTitleColor   = gauge.getSubTitleColor();
        unitColor       = gauge.getUnitColor();
        sectionsVisible = gauge.getSectionsVisible();
        drawBackground();

        setBar(gauge.getCurrentValue());

        valueBkgText.setFill(gauge.isShadowsEnabled() ? Helper.getTranslucentColorFrom(valueColor, 0.1) : Color.TRANSPARENT);

        valueText.setFill(valueColor);
    }
}
