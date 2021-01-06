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
import eu.hansolo.medusa.tools.AngleConicalGradient;
import eu.hansolo.medusa.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


public class NasaSkin extends GaugeSkinBase {
    private static final double  ANGLE_RANGE = 216;
    private double               size;
    private double               oldSize;
    private Arc                  lowerFrame;
    private Arc                  barBackground;
    private Circle               outerCircle;
    private Circle               innerCircle;
    private Line                 separator;
    private Arc                  bar;
    private Canvas               canvas;
    private GraphicsContext      ctx;
    private Text                 titleText;
    private Text                 valueText;
    private Text                 unitText;
    private Pane                 pane;
    private AngleConicalGradient gradient;
    private Rectangle            gradientRect;
    private boolean              gradientNeedsRefresh;
    private InvalidationListener decimalListener;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public NasaSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        decimalListener      = o -> handleEvents("DECIMALS");
        currentValueListener = o -> setBar(gauge.getCurrentValue());
        gradientNeedsRefresh = true;

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

        outerCircle = new Circle();
        outerCircle.setFill(null);

        innerCircle = new Circle();
        innerCircle.setFill(null);

        lowerFrame = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, gauge.getStartAngle() + 90, 144);
        lowerFrame.setType(ArcType.OPEN);
        lowerFrame.setStroke(gauge.getValueColor());
        lowerFrame.setStrokeWidth(PREFERRED_WIDTH * 0.06043956);
        lowerFrame.setStrokeLineCap(StrokeLineCap.BUTT);
        lowerFrame.setFill(null);

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, gauge.getStartAngle() + 90, -ANGLE_RANGE);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(gauge.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.06043956);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        separator = new Line();
        separator.setStroke(gauge.getValueColor());
        separator.setFill(gauge.getValueColor());
        separator.setStrokeWidth(Helper.clamp(1, Double.MAX_VALUE, 0.00549451 * PREFERRED_WIDTH));

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, gauge.getStartAngle() + 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(gauge.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.06043956);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx    = canvas.getGraphicsContext2D();

        titleText = new Text(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        valueText = new Text();
        valueText.setStroke(null);
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

        unitText = new Text();
        unitText.setStroke(null);
        unitText.setFill(gauge.getUnitColor());
        Helper.enableNode(unitText, gauge.isValueVisible() && !gauge.getUnit().isEmpty());

        if (gauge.isGradientBarEnabled()) { setupGradient(); }

        pane = new Pane(outerCircle, innerCircle, lowerFrame, barBackground, separator, titleText, valueText, unitText, bar, canvas);

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.decimalsProperty().addListener(decimalListener);
        gauge.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(unitText, gauge.isValueVisible() && !gauge.getUnit().isEmpty());
        }else if ("RECALC".equals(EVENT_TYPE)) {
            redraw();
            setBar(gauge.getCurrentValue());
        }
    }

    @Override public void dispose() {
        gauge.decimalsProperty().removeListener(decimalListener);
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Canvas ********************************************
    private void setBar( final double VALUE ) {
        double barLength    = 0;
        double barStart     = 0;
        double min          = gauge.getMinValue();
        double max          = gauge.getMaxValue();
        double step         = gauge.getAngleStep();
        double clampedValue = Helper.clamp(min, max, VALUE);

        if (gauge.isStartFromZero()) {
            if ( ( VALUE > min || min < 0 ) && ( VALUE < max || max > 0 ) ) {
                if ( max < 0 ) {
                    barStart = gauge.getStartAngle() + 90 - gauge.getAngleRange();
                    barLength = ( max - clampedValue ) * step;
                } else if ( min > 0 ) {
                    barStart = gauge.getStartAngle() + 90;
                    barLength = ( min - clampedValue ) * step;
                } else {
                    barStart = gauge.getStartAngle() + 90 + min * step;
                    barLength = - clampedValue * step;
                }
            }
        } else {
            barStart = gauge.getStartAngle() + 90;
            barLength = ( min - clampedValue ) * step;
        }

        bar.setStartAngle(barStart);
        bar.setLength(barLength);

        if (gauge.isGradientBarEnabled() && gauge.getGradientBarStops().size() > 1) {
            bar.setStroke(gauge.getGradientLookup().getColorAt((VALUE - gauge.getMinValue()) / gauge.getRange()));
            bar.setStroke(gradient.getImagePattern(gradientRect));
        } else {
            bar.setStroke(gauge.getBarColor());
        }

        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        valueText.setLayoutX((size - valueText.getLayoutBounds().getWidth()) * 0.5);

    }

    private void drawTicks() {
        double stepSize = ANGLE_RANGE / 100;
        ctx.clearRect(0, 0, size, size);
        Helper.drawRadialTickMarks(gauge, ctx, gauge.getMinValue(), gauge.getMaxValue(), -72, ANGLE_RANGE, stepSize, size * 0.5, size * 0.5, size * 1.045);
    }

    private void drawBackground() {
        outerCircle.setStroke(gauge.getValueColor());
        innerCircle.setStroke(gauge.getValueColor());
        lowerFrame.setStroke(gauge.getValueColor());
        barBackground.setStroke(gauge.getBarBackgroundColor());
        separator.setStroke(gauge.getValueColor());
    }


    // ******************** Resizing ******************************************
    private void resizeValueText() {
        double maxWidth = size * 0.71;
        double fontSize = size * 0.21;
        valueText.setFont(Fonts.estrictaMedium(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.275);
    }
    private void resizeStaticText() {
        double maxWidth = size * 0.67;
        double fontSize = size * 0.13;
        titleText.setFont(Fonts.estrictaRegularItalic(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.64);
        titleText.setFill(Color.RED);
        fontSize = size * 0.08;
        unitText.setFont(Fonts.estrictaRegularItalic(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.52);
    }

    private void setupGradient() {
        List<Stop>         stops        = gauge.getGradientBarStops();
        Map<Double, Color> stopAngleMap = new HashMap<>(stops.size());
        for (Stop stop : stops) { stopAngleMap.put(stop.getOffset() * 216, stop.getColor()); }
        gradient     = new AngleConicalGradient(size * 0.5, size * 0.5, 252, stopAngleMap, gauge.getScaleDirection());
        gradientRect = new Rectangle(0, 0, size, size);

        gradientNeedsRefresh = false;
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((gauge.getWidth() - size) * 0.5, (gauge.getHeight() - size) * 0.5);

            if (oldSize != size) { gradientNeedsRefresh = true; }
            if (gauge.isGradientBarEnabled() && gradientNeedsRefresh) { setupGradient(); }

            double centerX = size * 0.5;
            double centerY = size * 0.5;

            outerCircle.setCenterX(centerX);
            outerCircle.setCenterY(centerY);
            outerCircle.setRadius(size * 0.5);
            outerCircle.setStrokeWidth(1);

            innerCircle.setCenterX(centerX);
            innerCircle.setCenterY(centerY);
            innerCircle.setRadius(size * 0.43956044);
            innerCircle.setStrokeWidth(1);

            lowerFrame.setCenterX(centerX);
            lowerFrame.setCenterY(centerY);
            lowerFrame.setRadiusX(size * 0.46978022);
            lowerFrame.setRadiusY(size * 0.46978022);
            lowerFrame.setStrokeWidth(size * 0.06043956);

            barBackground.setCenterX(centerX);
            barBackground.setCenterY(centerY);
            barBackground.setRadiusX(size * 0.46978022);
            barBackground.setRadiusY(size * 0.46978022);
            barBackground.setStrokeWidth(size * 0.06043956);

            separator.setStartX(size * 0.16483516);
            separator.setStartY(size * 0.6456044);
            separator.setEndX(size * 0.83516484);
            separator.setEndY(size * 0.6456044);
            separator.setStrokeWidth(Helper.clamp(1, Double.MAX_VALUE, size * 0.00549451));

            bar.setCenterX(centerX);
            bar.setCenterY(centerY);
            bar.setRadiusX(size * 0.46978022);
            bar.setRadiusY(size * 0.46978022);
            bar.setStrokeWidth(size * 0.06043956);

            canvas.setWidth(size);
            canvas.setHeight(size);

            redraw();
        }

        oldSize = size;
    }

    @Override protected void redraw() {
        if (gauge.isGradientBarEnabled() && gradientNeedsRefresh) { setupGradient(); }

        drawBackground();
        setBar(gauge.getCurrentValue());

        drawTicks();

        resizeValueText();

        titleText.setText(gauge.getTitle());
        unitText.setText(gauge.getUnit());
        resizeStaticText();

        titleText.setFill(gauge.getTitleColor());
        valueText.setFill(gauge.getValueColor());
        unitText.setFill(gauge.getUnitColor());
    }
}
