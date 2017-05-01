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
import eu.hansolo.medusa.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
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

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 29.12.15.
 */
public class SpaceXSkin extends GaugeSkinBase {
    protected static final double             PREFERRED_WIDTH  = 250;
    protected static final double             PREFERRED_HEIGHT = 290;
    protected static final double             MINIMUM_WIDTH    = 50;
    protected static final double             MINIMUM_HEIGHT   = 50;
    protected static final double             MAXIMUM_WIDTH    = 1024;
    protected static final double             MAXIMUM_HEIGHT   = 1024;
    private static final double               ASPECT_RATIO     = 1.1625;
    private static final double               ANGLE_RANGE      = 270;
    private              double               size;
    private              double               width;
    private              double               height;
    private              double               centerX;
    private              double               centerY;
    private              double               range;
    private              double               angleStep;
    private              double               currentValueAngle;
    private              double               thresholdAngle;
    private              double               barWidth;
    private              Pane                 pane;
    private              Text                 unitText;
    private              Text                 titleText;
    private              Text                 valueText;
    private              Path                 barBackground;
    private              MoveTo               barBackgroundStart;
    private              ArcTo                barBackgroundOuterArc;
    private              LineTo               barBackgroundLineToInnerArc;
    private              ArcTo                barBackgroundInnerArc;
    private              Path                 thresholdBar;
    private              MoveTo               thresholdBarStart;
    private              ArcTo                thresholdBarOuterArc;
    private              LineTo               thresholdBarLineToInnerArc;
    private              ArcTo                thresholdBarInnerArc;
    private              Path                 dataBar;
    private              MoveTo               dataBarStart;
    private              ArcTo                dataBarOuterArc;
    private              LineTo               dataBarLineToInnerArc;
    private              ArcTo                dataBarInnerArc;
    private              Path                 dataBarThreshold;
    private              MoveTo               dataBarThresholdStart;
    private              ArcTo                dataBarThresholdOuterArc;
    private              LineTo               dataBarThresholdLineToInnerArc;
    private              ArcTo                dataBarThresholdInnerArc;
    private              Color                barColor;
    private              Color                thresholdColor;
    private              Color                barBackgroundColor;
    private              Color                thresholdBackgroundColor;
    private              double               minValue;
    private              InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public SpaceXSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        minValue             = gauge.getMinValue();
        currentValueAngle    = 0;
        currentValueListener = o -> setBar(gauge.getCurrentValue());

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

        barColor                 = gauge.getBarColor();
        thresholdColor           = gauge.getThresholdColor();
        barBackgroundColor       = gauge.getBarBackgroundColor();
        thresholdBackgroundColor = Color.color(thresholdColor.getRed(), thresholdColor.getGreen(), thresholdColor.getBlue(), 0.25);

        unitText = new Text(gauge.getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(gauge.getUnitColor());
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        titleText = new Text(gauge.getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

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
        dataBar.setStroke(gauge.getBorderPaint());

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
        dataBarThreshold.setStroke(gauge.getBorderPaint());

        pane = new Pane(titleText,
                        valueText,
                        unitText,
                        barBackground,
                        thresholdBar,
                        dataBar,
                        dataBarThreshold);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

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
            range     = gauge.getRange();
            angleStep = ANGLE_RANGE / range;
            minValue  = gauge.getMinValue();
            resize();
            redraw();
            setBar(gauge.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void setBar(final double VALUE) {
        currentValueAngle = (VALUE - minValue) * angleStep;
        thresholdAngle    = (gauge.getThreshold() - minValue) * angleStep;
        double valueAngle = currentValueAngle > thresholdAngle ? thresholdAngle : currentValueAngle;
        dataBarOuterArc.setLargeArcFlag(valueAngle > 180);
        dataBarInnerArc.setLargeArcFlag(valueAngle > 180);

        dataBarOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(valueAngle)));
        dataBarOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(valueAngle)));
        dataBarLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(valueAngle)));
        dataBarLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(valueAngle)));

        double dataBarThresholdAngle = VALUE > gauge.getThreshold() ? currentValueAngle : thresholdAngle;
        dataBarThresholdOuterArc.setLargeArcFlag(dataBarThresholdAngle > 180 + thresholdAngle);
        dataBarThresholdInnerArc.setLargeArcFlag(dataBarThresholdAngle > 180 + thresholdAngle);

        dataBarThresholdOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(thresholdAngle)));
        dataBarThresholdInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(thresholdAngle)));

        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        if (valueText.getLayoutBounds().getWidth() > 0.64 * width) Helper.adjustTextSize(valueText, width, 0.21 * width);
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);
    }

    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size   = width < height ? width : height;

        if (ASPECT_RATIO * width > height) {
            width  = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

            centerX  = 0.5 * width;
            centerY  = 0.56989247 * height;
            barWidth = 0.125 * width;

            titleText.setFont(Fonts.robotoMedium(0.13 * width));
            if (titleText.getLayoutBounds().getWidth() > width) Helper.adjustTextSize(titleText, width, 0.13 * width);
            titleText.relocate(0, 0);

            valueText.setFont(Fonts.robotoRegular(0.21 * width));
            if (valueText.getLayoutBounds().getWidth() > 0.64 * width) Helper.adjustTextSize(valueText, width, 0.21 * width);
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);

            unitText.setFont(Fonts.robotoLight(0.11 * width));
            if (unitText.getLayoutBounds().getWidth() > 0.4 * width) Helper.adjustTextSize(unitText, width, 0.11 * width);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()), 0.79 * height);

            thresholdAngle    = (gauge.getThreshold() - minValue) * angleStep;
            currentValueAngle = (gauge.getCurrentValue() - minValue) * angleStep;

            barBackgroundOuterArc.setLargeArcFlag(thresholdAngle > 180);
            barBackgroundInnerArc.setLargeArcFlag(thresholdAngle > 180);

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

            thresholdBarOuterArc.setLargeArcFlag(thresholdAngle < 180);
            thresholdBarInnerArc.setLargeArcFlag(thresholdAngle < 180);

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

            double dataBarThresholdAngle = gauge.getCurrentValue() > gauge.getThreshold() ? currentValueAngle : thresholdAngle;

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

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * width))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        barColor                 = gauge.getBarColor();
        thresholdColor           = gauge.getThresholdColor();
        barBackgroundColor       = gauge.getBarBackgroundColor();
        thresholdBackgroundColor = Color.color(thresholdColor.getRed(), thresholdColor.getGreen(), thresholdColor.getBlue(), 0.25);
        barBackground.setFill(barBackgroundColor);
        thresholdBar.setFill(thresholdBackgroundColor);
        dataBar.setFill(barColor);
        dataBarThreshold.setFill(thresholdColor);

        titleText.setFill(gauge.getTitleColor());
        titleText.setText(gauge.getTitle());

        valueText.setFill(gauge.getValueColor());
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);

        unitText.setFill(gauge.getUnitColor());
        unitText.setText(gauge.getUnit());
        unitText.relocate((width - unitText.getLayoutBounds().getWidth()), 0.79 * height);
    }
}
