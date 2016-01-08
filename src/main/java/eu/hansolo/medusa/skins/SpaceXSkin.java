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
import eu.hansolo.medusa.Gauge;
import javafx.geometry.VPos;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

import java.util.Locale;


/**
 * Created by hansolo on 29.12.15.
 */
public class SpaceXSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double         PREFERRED_WIDTH  = 250;
    private static final double         PREFERRED_HEIGHT = 290;
    private static final double         MINIMUM_WIDTH    = 50;
    private static final double         MINIMUM_HEIGHT   = 50;
    private static final double         MAXIMUM_WIDTH    = 1024;
    private static final double         MAXIMUM_HEIGHT   = 1024;
    private static final double         ASPECT_RATIO     = 1.1625;
    private              double         size;
    private              double         width;
    private              double         height;
    private              double         centerX;
    private              double         centerY;
    private              double         range;
    private              double         angleStep;
    private              double         currentValueAngle;
    private              double         thresholdAngle;
    private              double         barWidth;
    private              Pane           pane;
    private              Text           unitText;
    private              Text           titleText;
    private              Text           valueText;
    private              Path           barBackground;
    private              MoveTo         barBackgroundStart;
    private              ArcTo          barBackgroundOuterArc;
    private              LineTo         barBackgroundLineToInnerArc;
    private              ArcTo          barBackgroundInnerArc;
    private              Path           thresholdBar;
    private              MoveTo         thresholdBarStart;
    private              ArcTo          thresholdBarOuterArc;
    private              LineTo         thresholdBarLineToInnerArc;
    private              ArcTo          thresholdBarInnerArc;
    private              Path           dataBar;
    private              MoveTo         dataBarStart;
    private              ArcTo          dataBarOuterArc;
    private              LineTo         dataBarLineToInnerArc;
    private              ArcTo          dataBarInnerArc;
    private              Path           dataBarThreshold;
    private              MoveTo         dataBarThresholdStart;
    private              ArcTo          dataBarThresholdOuterArc;
    private              LineTo         dataBarThresholdLineToInnerArc;
    private              ArcTo          dataBarThresholdInnerArc;
    private              Color          barColor;
    private              Color          thresholdColor;
    private              Color          barBackgroundColor;
    private              Color          thresholdBackgroundColor;


    // ******************** Constructors **************************************
    public SpaceXSkin(Gauge gauge) {
        super(gauge);
        range             = getSkinnable().getRange();
        angleStep         = 270d / range;
        currentValueAngle = 0;

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
        barColor                 = getSkinnable().getBarColor();
        thresholdColor           = getSkinnable().getThresholdColor();
        barBackgroundColor       = Color.color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 0.25);
        thresholdBackgroundColor = Color.color(thresholdColor.getRed(), thresholdColor.getGreen(), thresholdColor.getBlue(), 0.25);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(getSkinnable().getUnitColor());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());

        barBackgroundStart          = new MoveTo();
        barBackgroundOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, true, true);
        barBackgroundLineToInnerArc = new LineTo();
        barBackgroundInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, true, false);

        barBackground = new Path();
        barBackground.setFillRule(FillRule.EVEN_ODD);
        barBackground.getElements().add(barBackgroundStart);
        barBackground.getElements().add(barBackgroundOuterArc);
        barBackground.getElements().add(barBackgroundLineToInnerArc);
        barBackground.getElements().add(barBackgroundInnerArc);
        barBackground.getElements().add(new ClosePath());
        barBackground.setFill(barBackgroundColor);
        barBackground.setStroke(Color.TRANSPARENT);

        thresholdBarStart = new MoveTo();
        thresholdBarOuterArc = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, false, true);
        thresholdBarLineToInnerArc = new LineTo();
        thresholdBarInnerArc = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, false, false);

        thresholdBar = new Path();
        thresholdBar.setFillRule(FillRule.EVEN_ODD);
        thresholdBar.getElements().add(thresholdBarStart);
        thresholdBar.getElements().add(thresholdBarOuterArc);
        thresholdBar.getElements().add(thresholdBarLineToInnerArc);
        thresholdBar.getElements().add(thresholdBarInnerArc);
        thresholdBar.getElements().add(new ClosePath());
        thresholdBar.setFill(thresholdBackgroundColor);
        thresholdBar.setStroke(Color.TRANSPARENT);
        
        dataBarStart          = new MoveTo();
        dataBarOuterArc       = new ArcTo(PREFERRED_WIDTH, 0.5 * PREFERRED_HEIGHT, 0, 0, 0, true, true);
        dataBarLineToInnerArc = new LineTo();
        dataBarInnerArc       = new ArcTo(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT - PREFERRED_WIDTH * 0.0625, 0, 0, 0, true, false);

        dataBar = new Path();
        dataBar.setFillRule(FillRule.EVEN_ODD);
        dataBar.getElements().add(dataBarStart);
        dataBar.getElements().add(dataBarOuterArc);
        dataBar.getElements().add(dataBarLineToInnerArc);
        dataBar.getElements().add(dataBarInnerArc);
        dataBar.getElements().add(new ClosePath());
        dataBar.setFill(barColor);
        dataBar.setStroke(getSkinnable().getBorderPaint());

        dataBarThresholdStart          = new MoveTo();
        dataBarThresholdOuterArc       = new ArcTo(PREFERRED_WIDTH, 0.5 * PREFERRED_HEIGHT, 0, 0, 0, false, true);
        dataBarThresholdLineToInnerArc = new LineTo();
        dataBarThresholdInnerArc       = new ArcTo(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT - PREFERRED_WIDTH * 0.0625, 0, 0, 0, false, false);

        dataBarThreshold = new Path();
        dataBarThreshold.setFillRule(FillRule.EVEN_ODD);
        dataBarThreshold.getElements().add(dataBarThresholdStart);
        dataBarThreshold.getElements().add(dataBarThresholdOuterArc);
        dataBarThreshold.getElements().add(dataBarThresholdLineToInnerArc);
        dataBarThreshold.getElements().add(dataBarThresholdInnerArc);
        dataBarThreshold.getElements().add(new ClosePath());
        dataBarThreshold.setFill(thresholdColor);
        dataBarThreshold.setStroke(getSkinnable().getBorderPaint());

        pane = new Pane();
        pane.getChildren().setAll(titleText,
                                  valueText,
                                  unitText,
                                  barBackground,
                                  thresholdBar,
                                  dataBar,
                                  dataBarThreshold);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> resize());
        getSkinnable().heightProperty().addListener(o -> resize());
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> updateBar());
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            range     = getSkinnable().getRange();
            angleStep = 270d / range;
            //currentValue.set(getSkinnable().getMinValue());
            resize();
        } else if ("ANGLE".equals(EVENT_TYPE)) {
            double currentValue = dataBarOuterArc.getXAxisRotation() / angleStep + getSkinnable().getMinValue();
            valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
            valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        }
    }


    // ******************** Private Methods ***********************************
    private void updateBar() {
        thresholdAngle    = (getSkinnable().getThreshold() + Math.abs(getSkinnable().getMinValue())) * angleStep;
        currentValueAngle = (getSkinnable().getCurrentValue() + Math.abs(getSkinnable().getMinValue())) * angleStep;
        double valueAngle = currentValueAngle > thresholdAngle ? thresholdAngle : currentValueAngle;

        dataBarOuterArc.setLargeArcFlag(valueAngle > 180);
        dataBarInnerArc.setLargeArcFlag(valueAngle > 180);

        dataBarOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(valueAngle)));
        dataBarOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(valueAngle)));
        dataBarLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(valueAngle)));
        dataBarLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(valueAngle)));

        double dataBarThresholdAngle = getSkinnable().getCurrentValue() > getSkinnable().getThreshold() ? currentValueAngle : thresholdAngle;

        dataBarThresholdOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(thresholdAngle)));
        dataBarThresholdInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(thresholdAngle)));

        valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getCurrentValue()));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);
    }

    private void redraw() {
        barColor                 = getSkinnable().getBarColor();
        thresholdColor           = getSkinnable().getThresholdColor();
        barBackgroundColor       = Color.color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 0.25);
        thresholdBackgroundColor = Color.color(thresholdColor.getRed(), thresholdColor.getGreen(), thresholdColor.getBlue(), 0.25);
        barBackground.setFill(barBackgroundColor);
        thresholdBar.setFill(thresholdBackgroundColor);
        dataBar.setFill(barColor);
        dataBarThreshold.setFill(thresholdColor);

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size   = width < height ? width : height;

        if (ASPECT_RATIO * width > height) {
            width  = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            centerX  = 0.5 * width;
            centerY  = 0.56989247 * height;
            barWidth = 0.125 * width;

            titleText.setFont(Fonts.robotoMedium(0.13 * width));
            titleText.relocate(0, 0);

            valueText.setFont(Fonts.robotoRegular(0.21 * width));
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);

            unitText.setFont(Fonts.robotoLight(0.11 * width));
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()), 0.8 * height);

            thresholdAngle    = (getSkinnable().getThreshold() + Math.abs(getSkinnable().getMinValue())) * angleStep;
            currentValueAngle = (getSkinnable().getCurrentValue() + Math.abs(getSkinnable().getMinValue())) * angleStep;
            currentValueAngle = currentValueAngle > thresholdAngle ? thresholdAngle : currentValueAngle;

            barBackgroundStart.setX(centerX);
            barBackgroundStart.setY(height);
            barBackgroundOuterArc.setRadiusX(centerX);
            barBackgroundOuterArc.setRadiusY(centerX);
            barBackgroundOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(thresholdAngle)));
            barBackgroundOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(thresholdAngle)));
            barBackgroundLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(thresholdAngle)));
            barBackgroundLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(thresholdAngle)));
            barBackgroundInnerArc.setRadiusX(0.375 * width);
            barBackgroundInnerArc.setRadiusY(0.375 * width);
            barBackgroundInnerArc.setX(centerX);
            barBackgroundInnerArc.setY(height - barWidth);

            thresholdBarStart.setX(centerX + centerX * Math.sin(-Math.toRadians(thresholdAngle)));
            thresholdBarStart.setY(centerY + centerX * Math.cos(-Math.toRadians(thresholdAngle)));
            thresholdBarOuterArc.setRadiusX(centerX);
            thresholdBarOuterArc.setRadiusY(centerX);
            thresholdBarOuterArc.setX(width);
            thresholdBarOuterArc.setY(centerY);
            thresholdBarLineToInnerArc.setX(width - barWidth);
            thresholdBarLineToInnerArc.setY(centerY);
            thresholdBarInnerArc.setRadiusX(0.375 * width);
            thresholdBarInnerArc.setRadiusY(0.375 * width);
            thresholdBarInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(thresholdAngle)));
            thresholdBarInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(thresholdAngle)));

            dataBarOuterArc.setLargeArcFlag(currentValueAngle > 180);
            dataBarInnerArc.setLargeArcFlag(currentValueAngle > 180);

            dataBarStart.setX(centerX);
            dataBarStart.setY(height);
            dataBarOuterArc.setRadiusX(centerX);
            dataBarOuterArc.setRadiusY(centerX);
            dataBarOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarInnerArc.setRadiusX(0.375 * width);
            dataBarInnerArc.setRadiusY(0.375 * width);
            dataBarInnerArc.setX(centerX);
            dataBarInnerArc.setY((height - barWidth));

            double dataBarThresholdAngle = getSkinnable().getCurrentValue() > getSkinnable().getThreshold() ? currentValueAngle : thresholdAngle;

            dataBarThresholdStart.setX(centerX + centerX * Math.sin(-Math.toRadians(thresholdAngle)));
            dataBarThresholdStart.setY(centerY + centerX * Math.cos(-Math.toRadians(thresholdAngle)));
            dataBarThresholdOuterArc.setRadiusX(centerX);
            dataBarThresholdOuterArc.setRadiusY(centerX);
            dataBarThresholdOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
            dataBarThresholdOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
            dataBarThresholdLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
            dataBarThresholdLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
            dataBarThresholdInnerArc.setRadiusX(0.375 * width);
            dataBarThresholdInnerArc.setRadiusY(0.375 * width);
            dataBarThresholdInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(thresholdAngle)));
            dataBarThresholdInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(thresholdAngle)));
        }
    }
}
