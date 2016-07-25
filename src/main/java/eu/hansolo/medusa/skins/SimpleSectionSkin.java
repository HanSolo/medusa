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
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

import java.util.List;


/**
 * Created by hansolo on 25.07.16.
 */
public class SimpleSectionSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ANGLE_RANGE      = 300;
    private double          size;
    private Canvas          sectionCanvas;
    private GraphicsContext sectionCtx;
    private Arc             barBackground;
    private Arc             bar;
    private Text            titleText;
    private Text            valueText;
    private Text            unitText;
    private Pane            pane;
    private Paint           backgroundPaint;
    private Paint           borderPaint;
    private double          borderWidth;
    private List<Section>   sections;
    private String          formatString;


    // ******************** Constructors **************************************
    public SimpleSectionSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        sections     = gauge.getSections();
        init();
        initGraphics();
        registerListeners();

        setBar(gauge.getCurrentValue());
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
        sectionCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionCtx    = sectionCanvas.getGraphicsContext2D();

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, getSkinnable().getStartAngle() + 150, ANGLE_RANGE);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(getSkinnable().getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.125);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.4, PREFERRED_HEIGHT * 0.4, getSkinnable().getStartAngle() + 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(getSkinnable().getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.125);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text();
        valueText.setStroke(null);
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text();
        unitText.setStroke(null);
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, getSkinnable().isValueVisible() && !getSkinnable().getUnit().isEmpty());

        pane = new Pane(barBackground, sectionCanvas, titleText, valueText, unitText, bar);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().decimalsProperty().addListener(o -> handleEvents("DECIMALS"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> setBar(getSkinnable().getCurrentValue()));
        getSkinnable().decimalsProperty().addListener(o -> handleEvents("DECIMALS"));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(unitText, getSkinnable().isValueVisible() && !getSkinnable().getUnit().isEmpty());
        } else if ("SECTIONS".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
        } else if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            redraw();
        } else if ("DECIMALS".equals(EVENT_TYPE)) {
            formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        }
    }


    // ******************** Canvas ********************************************
    private void setBar(final double VALUE) {
        if (getSkinnable().getMinValue() > 0) {
            bar.setLength((getSkinnable().getMinValue() - VALUE) * getSkinnable().getAngleStep());
        } else {
            bar.setLength(-VALUE * getSkinnable().getAngleStep());
        }
        if (getSkinnable().getSectionsVisible() && !sections.isEmpty()) {
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }

        valueText.setText(String.format(getSkinnable().getLocale(), formatString, VALUE));
        valueText.setLayoutX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
    }

    private void drawBackground() {
        sectionCanvas.setCache(false);
        sectionCtx.setLineCap(StrokeLineCap.BUTT);
        sectionCtx.clearRect(0, 0, size, size);

        if (getSkinnable().getSectionsVisible() && !sections.isEmpty()) {
            double xy        = 0.012 * size;
            double wh        = size * 0.976;
            double minValue  = getSkinnable().getMinValue();
            double maxValue  = getSkinnable().getMaxValue();
            double angleStep = getSkinnable().getAngleStep();

            sectionCtx.setLineWidth(size * 0.025);
            sectionCtx.setLineCap(StrokeLineCap.BUTT);
            for (int i = 0; i < sections.size(); i++) {
                Section section = sections.get(i);
                double  sectionStartAngle;
                if (Double.compare(section.getStart(), maxValue) <= 0 && Double.compare(section.getStop(), minValue) >= 0) {
                    if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                        sectionStartAngle = 0;
                    } else {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection() ? (section.getStart() - minValue) * angleStep : -(section.getStart() - minValue) * angleStep;
                    }
                    double sectionAngleExtend;
                    if (Double.compare(section.getStop(), maxValue) > 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection() ? (maxValue - section.getStart()) * angleStep : -(maxValue - section.getStart()) * angleStep;
                    } else if (Double.compare(section.getStart(), minValue) < 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection() ? (section.getStop() - minValue) * getSkinnable().getAngleStep() : -(section.getStop() - minValue) * angleStep;
                    } else {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection() ? (section.getStop() - section.getStart()) * angleStep : -(section.getStop() - section.getStart()) * angleStep;
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
    }


    // ******************** Resizing ******************************************
    private void resizeValueText() {
        double maxWidth = size * 0.86466165;
        double fontSize = size * 0.2556391;
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

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

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

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth / PREFERRED_WIDTH * size))));

        drawBackground();
        setBar(getSkinnable().getCurrentValue());

        titleText.setText(getSkinnable().getTitle());
        unitText.setText(getSkinnable().getUnit());
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
    }
}
