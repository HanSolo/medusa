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
import javafx.geometry.Insets;
import javafx.geometry.VPos;
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
    private static final double          PREFERRED_WIDTH  = 250;
    private static final double          PREFERRED_HEIGHT = 290;
    private static final double          MINIMUM_WIDTH    = 50;
    private static final double          MINIMUM_HEIGHT   = 50;
    private static final double          MAXIMUM_WIDTH    = 1024;
    private static final double          MAXIMUM_HEIGHT   = 1024;
    private static final double          ASPECT_RATIO     = 1.1625;
    private static final double          ANGLE_RANGE      = 270;
    private              double          size;
    private              double          width;
    private              double          height;
    private              double          centerX;
    private              double          centerY;
    private              double          range;
    private              double          angleStep;
    private              double          currentValueAngle;
    private              double          thresholdAngle;
    private              double          barWidth;
    private              Pane            pane;
    private              Text            unitText;
    private              Text            titleText;
    private              Text            valueText;
    private              Path            barBackground;
    private              MoveTo          barBackgroundStart;
    private              ArcTo           barBackgroundOuterArc;
    private              LineTo          barBackgroundLineToInnerArc;
    private              ArcTo           barBackgroundInnerArc;
    private              Path            thresholdBar;
    private              MoveTo          thresholdBarStart;
    private              ArcTo           thresholdBarOuterArc;
    private              LineTo          thresholdBarLineToInnerArc;
    private              ArcTo           thresholdBarInnerArc;
    private              Path            dataBar;
    private              MoveTo          dataBarStart;
    private              ArcTo           dataBarOuterArc;
    private              LineTo          dataBarLineToInnerArc;
    private              ArcTo           dataBarInnerArc;
    private              Path            dataBarThreshold;
    private              MoveTo          dataBarThresholdStart;
    private              ArcTo           dataBarThresholdOuterArc;
    private              LineTo          dataBarThresholdLineToInnerArc;
    private              ArcTo           dataBarThresholdInnerArc;
    private              Color           barColor;
    private              Color           thresholdColor;
    private              Color           barBackgroundColor;
    private              Color           thresholdBackgroundColor;
    private              double          minValue;
    private              String          formatString;
    private              Locale          locale;


    // ******************** Constructors **************************************
    public SpaceXSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        range             = gauge.getRange();
        angleStep         = ANGLE_RANGE / range;
        minValue          = gauge.getMinValue();
        currentValueAngle = 0;
        formatString      = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale            = gauge.getLocale();
        
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
        barBackgroundColor       = getSkinnable().getBarBackgroundColor();
        thresholdBackgroundColor = Color.color(thresholdColor.getRed(), thresholdColor.getGreen(), thresholdColor.getBlue(), 0.25);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

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

        pane = new Pane(titleText,
                        valueText,
                        unitText,
                        barBackground,
                        thresholdBar,
                        dataBar,
                        dataBarThreshold);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> resize());
        getSkinnable().heightProperty().addListener(o -> resize());
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> setBar(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            range     = getSkinnable().getRange();
            angleStep = ANGLE_RANGE / range;
            minValue  = getSkinnable().getMinValue();
            resize();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
        }
    }


    // ******************** Private Methods ***********************************
    private void setBar(final double VALUE) {
        currentValueAngle = (VALUE - minValue) * angleStep;
        thresholdAngle    = (getSkinnable().getThreshold() - minValue) * angleStep;
        double valueAngle = currentValueAngle > thresholdAngle ? thresholdAngle : currentValueAngle;
        dataBarOuterArc.setLargeArcFlag(valueAngle > 180);
        dataBarInnerArc.setLargeArcFlag(valueAngle > 180);

        dataBarOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(valueAngle)));
        dataBarOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(valueAngle)));
        dataBarLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(valueAngle)));
        dataBarLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(valueAngle)));

        double dataBarThresholdAngle = VALUE > getSkinnable().getThreshold() ? currentValueAngle : thresholdAngle;
        dataBarThresholdOuterArc.setLargeArcFlag(dataBarThresholdAngle > 180 + thresholdAngle);
        dataBarThresholdInnerArc.setLargeArcFlag(dataBarThresholdAngle > 180 + thresholdAngle);

        dataBarThresholdOuterArc.setX(centerX + centerX * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdOuterArc.setY(centerY + centerX * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdLineToInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdLineToInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(dataBarThresholdAngle)));
        dataBarThresholdInnerArc.setX(centerX + (centerX - barWidth) * Math.sin(-Math.toRadians(thresholdAngle)));
        dataBarThresholdInnerArc.setY(centerY + (centerX - barWidth) * Math.cos(-Math.toRadians(thresholdAngle)));

        valueText.setText(String.format(locale, formatString, VALUE));
        if (valueText.getLayoutBounds().getWidth() > 0.64 * width) Helper.adjustTextSize(valueText, width, 0.21 * width);
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);
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
            if (titleText.getLayoutBounds().getWidth() > width) Helper.adjustTextSize(titleText, width, 0.13 * width);
            titleText.relocate(0, 0);

            valueText.setFont(Fonts.robotoRegular(0.21 * width));
            if (valueText.getLayoutBounds().getWidth() > 0.64 * width) Helper.adjustTextSize(valueText, width, 0.21 * width);
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);

            unitText.setFont(Fonts.robotoLight(0.11 * width));
            if (unitText.getLayoutBounds().getWidth() > 0.4 * width) Helper.adjustTextSize(unitText, width, 0.11 * width);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()), 0.79 * height);

            thresholdAngle    = (getSkinnable().getThreshold() - minValue) * angleStep;
            currentValueAngle = (getSkinnable().getCurrentValue() - minValue) * angleStep;

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

    private void redraw() {
        locale                   = getSkinnable().getLocale();
        formatString             = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * width))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        barColor                 = getSkinnable().getBarColor();
        thresholdColor           = getSkinnable().getThresholdColor();
        barBackgroundColor       = getSkinnable().getBarBackgroundColor();
        thresholdBackgroundColor = Color.color(thresholdColor.getRed(), thresholdColor.getGreen(), thresholdColor.getBlue(), 0.25);
        barBackground.setFill(barBackgroundColor);
        thresholdBar.setFill(thresholdBackgroundColor);
        dataBar.setFill(barColor);
        dataBarThreshold.setFill(thresholdColor);

        titleText.setFill(getSkinnable().getTitleColor());
        titleText.setText(getSkinnable().getTitle());

        valueText.setFill(getSkinnable().getValueColor());
        valueText.setText(String.format(locale, formatString, getSkinnable().getCurrentValue()));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()), 0.58064516 * height);

        unitText.setFill(getSkinnable().getUnitColor());
        unitText.setText(getSkinnable().getUnit());
        unitText.relocate((width - unitText.getLayoutBounds().getWidth()), 0.79 * height);
    }
}
