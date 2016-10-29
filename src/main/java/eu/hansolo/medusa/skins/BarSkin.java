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
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.tools.ConicalGradient;
import eu.hansolo.medusa.tools.Helper;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 11.04.16.
 */
public class BarSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double          PREFERRED_WIDTH  = 250;
    private static final double          PREFERRED_HEIGHT = 250;
    private static final double          MINIMUM_WIDTH    = 50;
    private static final double          MINIMUM_HEIGHT   = 50;
    private static final double          MAXIMUM_WIDTH    = 1024;
    private static final double          MAXIMUM_HEIGHT   = 1024;
    private static final double          ANGLE_RANGE      = 360;
    private              double          size;
    private              Text            titleText;
    private              Text            valueText;
    private              Text            unitText;
    private              Circle          dot;
    private              Circle          fakeDot;
    private              Arc             arc;
    private              Circle          circle;
    private              Pane            pane;
    private              DropShadow      shadow;
    private              ConicalGradient gradient;
    private              double          center;
    private              double          range;
    private              double          angleStep;
    private              String          formatString;
    private              Locale          locale;


    // ******************** Constructors **************************************
    public BarSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        range        = gauge.getRange();
        angleStep    = -ANGLE_RANGE / range;
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale       = gauge.getLocale();

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
        Color barColor = getSkinnable().getBarColor();
        getSkinnable().setGradientBarStops(new Stop(0.0, barColor),
                                           new Stop(0.01, barColor),
                                           new Stop(0.75, barColor.deriveColor(-10, 1, 1, 1)),
                                           new Stop(1.0, barColor.deriveColor(-20, 1, 1, 1)));

        shadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.45), 0.01 * PREFERRED_WIDTH, 0, 0.01 * PREFERRED_WIDTH, 0);

        circle = new Circle();
        circle.setFill(null);

        arc = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.96, PREFERRED_WIDTH * 0.48, 90, 0);
        arc.setStrokeWidth(PREFERRED_WIDTH * 0.008);
        arc.setStrokeType(StrokeType.CENTERED);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.setFill(null);

        fakeDot = new Circle();
        fakeDot.setStroke(null);

        dot = new Circle();
        dot.setStroke(null);
        dot.setVisible(false);
        dot.setEffect(shadow);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFont(Fonts.robotoLight(PREFERRED_WIDTH * 0.5));
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getCurrentValue()));
        valueText.setFont(Fonts.robotoRegular(PREFERRED_WIDTH * 0.27333));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFont(Fonts.robotoLight(PREFERRED_WIDTH * 0.08));
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        pane = new Pane(circle, arc, fakeDot, dot, titleText, valueText, unitText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> redraw());
        getSkinnable().barColorProperty().addListener(o -> {
            Color barColor = getSkinnable().getBarColor();
            getSkinnable().setGradientBarStops(new Stop(0.0, barColor),
                                               new Stop(0.01, barColor),
                                               new Stop(0.75, barColor.deriveColor(-10, 1, 1, 1)),
                                               new Stop(1.0, barColor.deriveColor(-20, 1, 1, 1)));
            resize();
        });
        getSkinnable().titleProperty().addListener(o -> {
            titleText.setText(getSkinnable().getTitle());
            resizeTitleText();
        });
        getSkinnable().unitProperty().addListener(o -> {
            unitText.setText(getSkinnable().getUnit());
            resizeUnitText();
        });
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            range     = getSkinnable().getRange();
            angleStep = -ANGLE_RANGE / range;
            redraw();
        }
    }


    // ******************** Resizing ******************************************
    private void resizeTitleText() {
        double maxWidth = 0.48 * size;
        double fontSize = 0.08 * size;
        titleText.setFont(Fonts.robotoLight(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.25);
    }
    private void resizeValueText() {
        double maxWidth = 0.5 * size;
        double fontSize = 0.3 * size;
        valueText.setFont(Fonts.robotoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void resizeUnitText() {
        double maxWidth = 0.56667 * size;
        double fontSize = 0.08 * size;
        unitText.setFont(Fonts.robotoLight(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.68);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            locale       = getSkinnable().getLocale();
            formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();

            center = size * 0.5;

            circle.setCenterX(center);
            circle.setCenterY(center);
            circle.setRadius(size * 0.44);
            circle.setStrokeWidth(size * 0.11);

            arc.setCenterX(center);
            arc.setCenterY(center);
            arc.setRadiusX(size * 0.44);
            arc.setRadiusY(size * 0.44);
            arc.setStrokeWidth(size * 0.11);

            shadow.setRadius(0.03 * size);
            shadow.setOffsetX(0.03 * size);

            Color      barColor         = getSkinnable().getBarColor();
            double     currentValue     = getSkinnable().getCurrentValue();
            List<Stop> gradientBarStops = getSkinnable().getGradientBarStops();

            circle.setStroke(Color.color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 0.13));

            Rectangle bounds = new Rectangle(0, 0, size, size);

            gradient = new ConicalGradient(center, center, ScaleDirection.CLOCKWISE, gradientBarStops);
            arc.setStroke(gradient.getImagePattern(bounds));
            arc.setLength(currentValue * angleStep);

            fakeDot.setRadius(size * 0.055);
            fakeDot.setFill(gradientBarStops.get(0).getColor());

            dot.setRadius(size * 0.055);
            dot.setFill(gradientBarStops.get(3).getColor());

            dot.setCenterX(center + arc.getRadiusX() * Math.sin(Math.toRadians(180 - currentValue * angleStep)));
            dot.setCenterY(center + arc.getRadiusY() * Math.cos(Math.toRadians(180 - currentValue * angleStep)));

            titleText.setFill(barColor);

            resizeTitleText();
            resizeValueText();
            resizeUnitText();

            redraw();
        }
    }

    private void redraw() {
        double currentValue   = getSkinnable().getCurrentValue();
        double angle   = currentValue * angleStep;
        double rotate  = angle  < -360 ? angle  + 360 : 0;

        arc.setRotate(-rotate);
        arc.setLength(Helper.clamp(-360.0, 0.0, angle));

        dot.setRotate(-angle);
        dot.setVisible(angle   < -345 ? true : false);

        if (angle < -360) {
            fakeDot.setCenterX(center + arc.getRadiusX() * Math.sin(Math.toRadians(180 + angle)));
            fakeDot.setCenterY(center + arc.getRadiusY() * Math.cos(Math.toRadians(180 + angle)));
        } else {
            fakeDot.setCenterX(center + arc.getRadiusX() * Math.sin(Math.toRadians(180)));
            fakeDot.setCenterY(center + arc.getRadiusY() * Math.cos(Math.toRadians(180)));
        }

        dot.setCenterX(center + arc.getRadiusX() * Math.sin(Math.toRadians(180 + angle)));
        dot.setCenterY(center + arc.getRadiusY() * Math.cos(Math.toRadians(180 + angle)));

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());

        valueText.setText(String.format(getSkinnable().getLocale(), formatString, currentValue));
        resizeValueText();
    }
}
