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
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 25.07.16.
 */
public class SimpleSectionSkin extends GaugeSkinBase {
    private static final double  ANGLE_RANGE = 300;
    private double               size;
    private Canvas               sectionCanvas;
    private GraphicsContext      sectionCtx;
    private Arc                  barBackground;
    private Arc                  bar;
    private Text                 titleText;
    private Text                 valueText;
    private Text                 unitText;
    private Pane                 pane;
    private List<Section>        sections;
    private InvalidationListener decimalListener;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public SimpleSectionSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        sections             = gauge.getSections();
        decimalListener      = o -> handleEvents("DECIMALS");
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

        sectionCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionCtx    = sectionCanvas.getGraphicsContext2D();

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, gauge.getStartAngle() + 150, ANGLE_RANGE);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(gauge.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.125);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, gauge.getStartAngle() + 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(gauge.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.125);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

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

        pane = new Pane(barBackground, sectionCanvas, titleText, valueText, unitText, bar);

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
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = gauge.getSections();
            resize();
        } else if ("RECALC".equals(EVENT_TYPE)) {
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

        double barLength = 0;
        double barStart = 0;
        double min = gauge.getMinValue();
        double max = gauge.getMaxValue();
        double step = gauge.getAngleStep();
        double clampedValue = Helper.clamp(min, max, VALUE);

        if ( gauge.isStartFromZero() ) {
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

        if ( gauge.getSectionsVisible() && !sections.isEmpty() ) {
            bar.setStroke(gauge.getBarColor());
            for ( Section section : sections ) {
                if ( section.contains(VALUE) ) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }

        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        valueText.setLayoutX((size - valueText.getLayoutBounds().getWidth()) * 0.5);

    }

    private void drawBackground() {
        sectionCanvas.setCache(false);
        sectionCtx.setLineCap(StrokeLineCap.BUTT);
        sectionCtx.clearRect(0, 0, size, size);

        if (gauge.getSectionsVisible() && !sections.isEmpty()) {
            double xy        = 0.012 * size;
            double wh        = size * 0.976;
            double minValue  = gauge.getMinValue();
            double maxValue  = gauge.getMaxValue();
            double angleStep = gauge.getAngleStep();

            sectionCtx.setLineWidth(size * 0.025);
            sectionCtx.setLineCap(StrokeLineCap.BUTT);
            for (int i = 0; i < sections.size(); i++) {
                Section section = sections.get(i);
                double  sectionStartAngle;
                if (Double.compare(section.getStart(), maxValue) <= 0 && Double.compare(section.getStop(), minValue) >= 0) {
                    if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                        sectionStartAngle = 0;
                    } else {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == gauge.getScaleDirection() ? (section.getStart() - minValue) * angleStep : -(section.getStart() - minValue) * angleStep;
                    }
                    double sectionAngleExtend;
                    if (Double.compare(section.getStop(), maxValue) > 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == gauge.getScaleDirection() ? (maxValue - section.getStart()) * angleStep : -(maxValue - section.getStart()) * angleStep;
                    } else if (Double.compare(section.getStart(), minValue) < 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == gauge.getScaleDirection() ? (section.getStop() - minValue) * gauge.getAngleStep() : -(section.getStop() - minValue) * angleStep;
                    } else {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == gauge.getScaleDirection() ? (section.getStop() - section.getStart()) * angleStep : -(section.getStop() - section.getStart()) * angleStep;
                    }
                    sectionCtx.save();

                    sectionCtx.setStroke(section.getColor());
                    sectionCtx.strokeArc(xy, xy, wh, wh, -(120 + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
                    sectionCtx.restore();
                }
            }
        }

        sectionCanvas.setCache(true);
        sectionCanvas.setCacheHint(CacheHint.QUALITY);
        barBackground.setStroke(gauge.getBarBackgroundColor());
    }


    // ******************** Resizing ******************************************
    private void resizeValueText() {
        double maxWidth = size * 0.86466165;
        double fontFactor = -0.035 * ( Math.max(1, gauge.getDecimals() ) - 1 ) + 0.2556391;
        double fontSize = size * fontFactor;
        valueText.setFont(Fonts.latoLight(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void resizeStaticText() {
        double maxWidth = size * 0.35;
        double fontSize = size * 0.08082707;
        titleText.setFont(Fonts.latoBold(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.22180451);
        titleText.setFill(Color.RED);
        unitText.setFont(Fonts.latoBold(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.68984962);
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((gauge.getWidth() - size) * 0.5, (gauge.getHeight() - size) * 0.5);

            sectionCanvas.setWidth(size);
            sectionCanvas.setHeight(size);

            barBackground.setCenterX(size * 0.5);
            barBackground.setCenterY(size * 0.5);
            barBackground.setRadiusX(size * 0.4);
            barBackground.setRadiusY(size * 0.4);
            barBackground.setStrokeWidth(size * 0.125);

            bar.setCenterX(size * 0.5);
            bar.setCenterY(size * 0.5);
            bar.setRadiusX(size * 0.4);
            bar.setRadiusY(size * 0.4);
            bar.setStrokeWidth(size * 0.125);

            resizeValueText();

            redraw();
        }
    }

    @Override protected void redraw() {
        drawBackground();
        setBar(gauge.getCurrentValue());

        titleText.setText(gauge.getTitle());
        unitText.setText(gauge.getUnit());
        resizeStaticText();

        titleText.setFill(gauge.getTitleColor());
        valueText.setFill(gauge.getValueColor());
        unitText.setFill(gauge.getUnitColor());
    }
}
