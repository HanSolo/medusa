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
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Locale;


/**
 * Created by hansolo on 28.12.15.
 */
public class DashboardSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 200;
    private static final double PREFERRED_HEIGHT = 148;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ASPECT_RATIO     = 0.74;
    private double      size;
    private double      width;
    private double      height;
    private double      centerX;
    private double      range;
    private double      angleStep;
    private double      currentValueAngle;
    private Pane        pane;
    private Text        unitText;
    private Text        titleText;
    private Text        valueText;
    private Text        minText;
    private Text        maxText;
    private Path        barBackground;
    private MoveTo      barBackgroundStart;
    private ArcTo       barBackgroundOuterArc;
    private LineTo      barBackgroundLineToInnerArc;
    private ArcTo       barBackgroundInnerArc;
    private Path        dataBar;
    private MoveTo      dataBarStart;
    private ArcTo       dataBarOuterArc;
    private LineTo      dataBarLineToInnerArc;
    private ArcTo       dataBarInnerArc;
    private InnerShadow innerShadow;
    private Font        smallFont;
    private Font        bigFont;


    // ******************** Constructors **************************************
    public DashboardSkin(Gauge gauge) {
        super(gauge);
        range             = getSkinnable().getRange();
        angleStep         = 180d / range;
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
        unitText = new Text(getSkinnable().getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(getSkinnable().getUnitColor());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());

        minText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinValue()));
        minText.setTextOrigin(VPos.CENTER);
        minText.setFill(getSkinnable().getValueColor());

        maxText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMaxValue()));
        maxText.setTextOrigin(VPos.CENTER);
        maxText.setFill(getSkinnable().getValueColor());

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.3), 30d, 0d, 0d, 10d);

        barBackgroundStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        barBackgroundOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, true, true);
        barBackgroundLineToInnerArc = new LineTo(0.72222 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        barBackgroundInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, false, false);

        barBackground = new Path();
        barBackground.setFillRule(FillRule.EVEN_ODD);
        barBackground.getElements().add(barBackgroundStart);
        barBackground.getElements().add(barBackgroundOuterArc);
        barBackground.getElements().add(barBackgroundLineToInnerArc);
        barBackground.getElements().add(barBackgroundInnerArc);
        barBackground.getElements().add(new ClosePath());
        barBackground.setFill(getSkinnable().getBarBackgroundColor());
        barBackground.setStroke(getSkinnable().getBorderPaint());
        barBackground.setEffect(getSkinnable().areShadowsEnabled() ? innerShadow : null);

        dataBarStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        dataBarOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, 0, 0, false, true);
        dataBarLineToInnerArc = new LineTo(0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        dataBarInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0, 0, false, false);

        dataBar = new Path();
        dataBar.setFillRule(FillRule.EVEN_ODD);
        dataBar.getElements().add(dataBarStart);
        dataBar.getElements().add(dataBarOuterArc);
        dataBar.getElements().add(dataBarLineToInnerArc);
        dataBar.getElements().add(dataBarInnerArc);
        dataBar.getElements().add(new ClosePath());
        dataBar.setFill(getSkinnable().getBarColor());
        dataBar.setStroke(getSkinnable().getBorderPaint());
        dataBar.setEffect(getSkinnable().areShadowsEnabled() ? innerShadow : null);

        pane = new Pane();
        pane.getChildren().setAll(unitText,
                                  titleText,
                                  valueText,
                                  minText,
                                  maxText,
                                  barBackground,
                                  dataBar);

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
            angleStep = 180d / range;
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
        double currentValue = getSkinnable().getCurrentValue();
        currentValueAngle = (currentValue + Math.abs(getSkinnable().getMinValue())) * angleStep + 90;
        dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
        dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
        dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
        dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
        if (getSkinnable().isColorGradientEnabled() && getSkinnable().getGradientLookup().getStops().size() > 1) dataBar.setFill(getSkinnable().getGradientLookup().getColorAt(currentValue / range));
        valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.62 * height);
    }

    private void redraw() {
        barBackground.setFill(getSkinnable().getBarBackgroundColor());
        barBackground.setEffect(getSkinnable().areShadowsEnabled() ? innerShadow : null);

        if (getSkinnable().isColorGradientEnabled() && !getSkinnable().getGradientLookup().getStops().isEmpty()) {
            dataBar.setFill(getSkinnable().getGradientLookup().getColorAt(getSkinnable().getCurrentValue() / range));
        } else {
            dataBar.setFill(getSkinnable().getBarColor());
        }

        titleText.setFill(getSkinnable().getTitleColor());

        valueText.setFill(getSkinnable().getValueColor());
        minText.setFill(getSkinnable().getValueColor());
        maxText.setFill(getSkinnable().getValueColor());

        unitText.setFill(getSkinnable().getUnitColor());

        dataBar.setEffect(getSkinnable().areShadowsEnabled() ? innerShadow : null);
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

            centerX   = width * 0.5;
            smallFont = Fonts.robotoThin(0.12 * height);
            bigFont   = Fonts.robotoRegular(0.24 * height);

            unitText.setFont(smallFont);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.5 * height);

            titleText.setFont(smallFont);
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.87 * height);

            valueText.setFont(bigFont);
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.62 * height);

            minText.setFont(smallFont);
            minText.relocate(((0.27778 * width) - minText.getLayoutBounds().getWidth()) * 0.5, 0.7 * height);

            maxText.setFont(smallFont);
            maxText.relocate(((0.27778 * width) - maxText.getLayoutBounds().getWidth()) * 0.5 + 0.72222 * width, 0.7 * height);

            if (getSkinnable().areShadowsEnabled()) {
                innerShadow.setRadius(0.075 * height);
                innerShadow.setOffsetY(0.025 * height);
            }

            barBackgroundStart.setX(0);
            barBackgroundStart.setY(0.675 * height);
            barBackgroundOuterArc.setRadiusX(0.675 * height);
            barBackgroundOuterArc.setRadiusY(0.675 * height);
            barBackgroundOuterArc.setX(width);
            barBackgroundOuterArc.setY(0.675 * height);
            barBackgroundLineToInnerArc.setX(0.72222 * width);
            barBackgroundLineToInnerArc.setY(0.675 * height);
            barBackgroundInnerArc.setRadiusX(0.3 * height);
            barBackgroundInnerArc.setRadiusY(0.3 * height);
            barBackgroundInnerArc.setX(0.27778 * width);
            barBackgroundInnerArc.setY(0.675 * height);

            currentValueAngle = (getSkinnable().getCurrentValue() + Math.abs(getSkinnable().getMinValue())) * angleStep + 90;
            dataBarStart.setX(0);
            dataBarStart.setY(0.675 * height);
            dataBarOuterArc.setRadiusX(0.675 * height);
            dataBarOuterArc.setRadiusY(0.675 * height);
            dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarInnerArc.setRadiusX(0.3 * height);
            dataBarInnerArc.setRadiusY(0.3 * height);
            dataBarInnerArc.setX(0.27778 * width);
            dataBarInnerArc.setY(0.675 * height);
        }
    }
}
