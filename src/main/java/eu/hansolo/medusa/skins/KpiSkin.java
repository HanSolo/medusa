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
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.util.Locale;


/**
 * Created by hansolo on 15.01.16.
 */
public class KpiSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double       size;
    private double       oldValue;
    private Arc          barBackground;
    private Arc          thresholdBar;
    private Path         needle;
    private Rotate       needleRotate;
    private Text         titleText;
    private Text         valueText;
    private Text         minValueText;
    private Text         maxValueText;
    private Text         thresholdText;
    private Pane         pane;
    private double       angleRange;
    private double       minValue;
    private double       range;
    private double       angleStep;
    private String       formatString;
    private Locale       locale;


    // ******************** Constructors **************************************
    public KpiSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleRange   = Helper.clamp(90.0, 180.0, gauge.getAngleRange());
        oldValue     = gauge.getValue();
        minValue     = gauge.getMinValue();
        range        = gauge.getRange();
        angleStep    = angleRange / range;
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale       = gauge.getLocale();

        init();
        initGraphics();
        registerListeners();

        rotateNeedle(gauge.getCurrentValue());
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
        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, -angleRange);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(getSkinnable().getBarColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        thresholdBar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, -angleRange * 0.5 + 90, 0);
        thresholdBar.setType(ArcType.OPEN);
        thresholdBar.setStroke(getSkinnable().getThresholdColor());
        thresholdBar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        thresholdBar.setStrokeLineCap(StrokeLineCap.BUTT);
        thresholdBar.setFill(null);

        needleRotate = new Rotate((getSkinnable().getValue() - oldValue - minValue) * angleStep);

        needle = new Path();
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setFill(getSkinnable().getNeedleColor());
        needle.setStrokeWidth(0);
        needle.setStroke(Color.TRANSPARENT);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getCurrentValue()));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        minValueText = new Text(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMinValue()));
        minValueText.setFill(getSkinnable().getTitleColor());

        maxValueText = new Text(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMaxValue()));
        maxValueText.setFill(getSkinnable().getTitleColor());

        thresholdText = new Text(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getThreshold()));
        thresholdText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(thresholdText, Double.compare(getSkinnable().getThreshold(), getSkinnable().getMinValue()) != 0 && Double.compare(getSkinnable().getThreshold(), getSkinnable().getMaxValue()) != 0);

        pane = new Pane(barBackground, thresholdBar, needle, titleText, valueText, minValueText, maxValueText, thresholdText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> rotateNeedle(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            angleRange = Helper.clamp(90.0, 180.0, getSkinnable().getAngleRange());
            minValue   = getSkinnable().getMinValue();
            range      = getSkinnable().getRange();
            angleStep  = angleRange / range;
            redraw();
        }
    }

    private void rotateNeedle(final double VALUE) {
        double needleStartAngle = angleRange * 0.5;
        double targetAngle = (VALUE - minValue) * angleStep - needleStartAngle;
        targetAngle = Helper.clamp(-needleStartAngle, -needleStartAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeValueText();
    }

    private void drawNeedle() {
        double needleWidth  = size * 0.064;
        double needleHeight = size * 0.44;
        needle.setCache(false);
        needle.getElements().clear();
        needle.getElements().add(new MoveTo(0.1875 * needleWidth, 0.0));
        needle.getElements().add(new CubicCurveTo(0.1875 * needleWidth, 0.0,
                                                0.1875 * needleWidth, 0.8727272727272727 * needleHeight,
                                                0.1875 * needleWidth, 0.8727272727272727 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.0625 * needleWidth, 0.8818181818181818 * needleHeight,
                                                0.0, 0.9 * needleHeight,
                                                0.0, 0.9272727272727272 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.0, 0.9636363636363636 * needleHeight,
                                                0.25 * needleWidth, needleHeight,
                                                0.5 * needleWidth, needleHeight));
        needle.getElements().add(new CubicCurveTo(0.75 * needleWidth, needleHeight,
                                                needleWidth, 0.9636363636363636 * needleHeight,
                                                needleWidth, 0.9272727272727272 * needleHeight));
        needle.getElements().add(new CubicCurveTo(needleWidth, 0.9 * needleHeight,
                                                0.9375 * needleWidth, 0.8818181818181818 * needleHeight,
                                                0.8125 * needleWidth, 0.8727272727272727 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.8125 * needleWidth, 0.8727272727272727 * needleHeight,
                                                0.8125 * needleWidth, 0.0,
                                                0.8125 * needleWidth, 0.0));
        needle.getElements().add(new LineTo(0.1875 * needleWidth, 0.0));
        needle.getElements().add(new ClosePath());
        needle.setCache(true);
        needle.setCacheHint(CacheHint.ROTATE);
    }


    // ******************** Resizing ******************************************
    private void resizeValueText() {
        double maxWidth = 0.86466165 * size;
        double fontSize = 0.192 * size;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size * 0.744));
    }
    private void resizeStaticText() {
        double maxWidth   = 0.98 * size;
        double fontSize   = size * 0.104;
        double textRadius;
        double textAngle;
        double sinValue;
        double cosValue;
        double textX;
        double textY;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.02, size * 0.02);

        maxWidth = size * 0.144;
        fontSize = size * 0.048;
        maxValueText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        textRadius = size * 0.45;
        sinValue  = Math.sin(Math.toRadians(90 + (180 - angleRange) * 0.5));
        cosValue  = Math.cos(Math.toRadians(90 + (180 - angleRange) * 0.5));
        textX     = size * 0.5 + textRadius * sinValue;
        textY     = size * 0.672 + textRadius * cosValue;
        maxValueText.setTranslateX(-maxValueText.getLayoutBounds().getWidth() * 0.5);
        maxValueText.setTranslateY(-maxValueText.getLayoutBounds().getHeight() * 0.5);
        maxValueText.relocate(textX, textY);

        minValueText.setFont(Fonts.latoRegular(maxValueText.getFont().getSize()));
        if (minValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minValueText, maxWidth, fontSize); }
        sinValue  = Math.sin(Math.toRadians(-90 - (180 - angleRange) * 0.5));
        cosValue  = Math.cos(Math.toRadians(-90 - (180 - angleRange) * 0.5));
        textX     = size * 0.5 + textRadius * sinValue;
        textY     = size * 0.672 + textRadius * cosValue;
        minValueText.setTranslateX(-minValueText.getLayoutBounds().getWidth() * 0.5);
        minValueText.setTranslateY(-minValueText.getLayoutBounds().getHeight() * 0.5);
        minValueText.relocate(textX, textY);

        fontSize = size * 0.08;
        thresholdText.setFont(Fonts.latoRegular(fontSize));
        if (thresholdText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(thresholdText, maxWidth, fontSize); }
        textRadius = size * 0.5;
        textAngle  = (getSkinnable().getThreshold() - minValue) * angleStep;
        sinValue   = Math.sin(Math.toRadians(180 + angleRange * 0.5 - textAngle));
        cosValue   = Math.cos(Math.toRadians(180 + angleRange * 0.5 - textAngle));
        textX      = size * 0.5 + textRadius * sinValue;
        textY      = size * 0.72 + textRadius * cosValue;
        thresholdText.setTranslateX(-thresholdText.getLayoutBounds().getWidth() * 0.5);
        thresholdText.setTranslateY(-thresholdText.getLayoutBounds().getHeight() * 0.5);
        thresholdText.relocate(textX, textY);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            double centerX   = size * 0.5;
            double centerY   = size * 0.696;
            double barRadius = size * 0.275;
            double barWidth  = size * 0.216;

            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            barBackground.setCenterX(centerX);
            barBackground.setCenterY(centerY - size * 0.008);
            barBackground.setRadiusX(barRadius);
            barBackground.setRadiusY(barRadius);
            barBackground.setStrokeWidth(barWidth);
            barBackground.setStartAngle(angleRange * 0.5 + 90);
            barBackground.setLength(-angleRange);

            thresholdBar.setCenterX(centerX);
            thresholdBar.setCenterY(centerY - size * 0.008);
            thresholdBar.setRadiusX(barRadius);
            thresholdBar.setRadiusY(barRadius);
            thresholdBar.setStrokeWidth(barWidth);
            thresholdBar.setStartAngle(90 - angleRange * 0.5);
            thresholdBar.setLength((getSkinnable().getMaxValue() - getSkinnable().getThreshold()) * angleStep);

            drawNeedle();

            needle.relocate((size - needle.getLayoutBounds().getWidth()) * 0.5, centerY - needle.getLayoutBounds().getHeight() + needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight() - needle.getLayoutBounds().getWidth() * 0.5);

            resizeStaticText();
            resizeValueText();
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();

        titleText.setText(getSkinnable().getTitle());
        minValueText.setText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMinValue()));
        maxValueText.setText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMaxValue()));
        thresholdText.setText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getThreshold()));
        resizeStaticText();

        barBackground.setStroke(getSkinnable().getBarColor());
        thresholdBar.setStroke(getSkinnable().getThresholdColor());
        needle.setFill(getSkinnable().getNeedleColor());
        titleText.setFill(getSkinnable().getTitleColor());
        minValueText.setFill(getSkinnable().getTitleColor());
        maxValueText.setFill(getSkinnable().getTitleColor());
        thresholdText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());

        thresholdText.setVisible(Double.compare(getSkinnable().getThreshold(), getSkinnable().getMinValue()) != 0 && Double.compare(getSkinnable().getThreshold(), getSkinnable().getMaxValue()) != 0);
    }
}
