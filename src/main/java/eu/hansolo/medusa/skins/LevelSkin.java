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
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 25.01.16.
 */
public class LevelSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 216;
    private static final double PREFERRED_HEIGHT = 249;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ASPECT_RATIO     = 1.15384615;
    private double        width;
    private double        height;
    private Pane          pane;
    private Path          tube;
    private Ellipse       tubeTop;
    private Ellipse       tubeBottom;
    private Path          fluidBody;
    private CubicCurveTo  fluidUpperLeft;
    private CubicCurveTo  fluidUpperCenter;
    private CubicCurveTo  fluidUpperRight;
    private Ellipse       fluidTop;
    private Text          valueText;
    private Text          titleText;
    private Tooltip       barTooltip;
    private String        formatString;
    private Locale        locale;
    private List<Section> sections;



    // ******************** Constructors **************************************
    public LevelSkin(Gauge gauge) {
        super(gauge);
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale       = gauge.getLocale();
        sections     = gauge.getSections();
        barTooltip   = new Tooltip();
        barTooltip.setTextAlignment(TextAlignment.CENTER);

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
        tube = new Path();
        tube.setFillRule(FillRule.EVEN_ODD);
        tube.setStroke(null);
        Tooltip.install(tube, barTooltip);

        tubeTop = new Ellipse();
        tubeTop.setStroke(Color.rgb(255, 255, 255, 0.5));
        tubeTop.setStrokeType(StrokeType.INSIDE);
        tubeTop.setStrokeWidth(1);

        tubeBottom = new Ellipse();
        tubeBottom.setStroke(null);

        fluidUpperLeft   = new CubicCurveTo(0.21794871794871795 * PREFERRED_WIDTH, 0.24444444444444444 * PREFERRED_HEIGHT,
                                            0.0, 0.18888888888888888 * PREFERRED_HEIGHT,
                                            0.0, 0.12222222222222222 * PREFERRED_HEIGHT);
        fluidUpperCenter = new CubicCurveTo(PREFERRED_WIDTH, 0.18888888888888888 * PREFERRED_HEIGHT,
                                            0.782051282051282 * PREFERRED_WIDTH, 0.24444444444444444 * PREFERRED_HEIGHT,
                                            0.5 * PREFERRED_WIDTH, 0.24444444444444444 * PREFERRED_HEIGHT);
        fluidUpperRight  = new CubicCurveTo(PREFERRED_WIDTH, 0.7111111111111111 * PREFERRED_HEIGHT,
                                            PREFERRED_WIDTH, 0.12222222222222222 * PREFERRED_HEIGHT,
                                            PREFERRED_WIDTH, 0.12222222222222222 * PREFERRED_HEIGHT);

        fluidBody = new Path();
        fluidBody.getElements().add(new MoveTo(0.0, 0.7111111111111111 * PREFERRED_HEIGHT));
        fluidBody.getElements().add(new CubicCurveTo(0.0, 0.7777777777777778 * PREFERRED_HEIGHT,
                                                     0.21794871794871795 * PREFERRED_WIDTH, 0.8333333333333334 * PREFERRED_HEIGHT,
                                                     0.5 * PREFERRED_WIDTH, 0.8333333333333334 * PREFERRED_HEIGHT));
        fluidBody.getElements().add(new CubicCurveTo(0.782051282051282 * PREFERRED_WIDTH, 0.8333333333333334 * PREFERRED_HEIGHT,
                                                     PREFERRED_WIDTH, 0.7777777777777778 * PREFERRED_HEIGHT,
                                                     PREFERRED_WIDTH, 0.7111111111111111 * PREFERRED_HEIGHT));
        fluidBody.getElements().add(fluidUpperRight);
        fluidBody.getElements().add(fluidUpperCenter);
        fluidBody.getElements().add(fluidUpperLeft);
        fluidBody.getElements().add(new CubicCurveTo(0.0, 0.12222222222222222 * PREFERRED_HEIGHT,
                                                     0.0, 0.7111111111111111 * PREFERRED_HEIGHT,
                                                     0.0, 0.7111111111111111 * PREFERRED_HEIGHT));
        fluidBody.getElements().add(new ClosePath());
        fluidBody.setFillRule(FillRule.EVEN_ODD);
        fluidBody.setStroke(null);

        fluidTop = new Ellipse();
        fluidTop.setStroke(null);

        valueText = new Text(String.format(locale, "%.0f%%", getSkinnable().getCurrentValue()));
        valueText.setMouseTransparent(true);
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        titleText = new Text(getSkinnable().getTitle());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        // Add all nodes
        pane = new Pane(tubeBottom, fluidBody, fluidTop, tube, tubeTop, valueText, titleText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));

        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(e -> setBar(getSkinnable().getCurrentValue()));

        handleEvents("VISIBILITY");
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            if (getSkinnable().isAutoScale()) getSkinnable().calcAutoScale();
            setBar(getSkinnable().getCurrentValue());
            resize();
            redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
            resize();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(titleText, !getSkinnable().getUnit().isEmpty());
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            StringBuilder content = new StringBuilder(String.format(locale, formatString, getSkinnable().getValue()))
                .append("\n(").append(valueText.getText()).append(")");
            barTooltip.setText(content.toString());
        }
    }


    // ******************** Private Methods ***********************************
    private void setBar(final double VALUE) {
        double factor  = VALUE / getSkinnable().getRange();

        if (getSkinnable().isGradientBarEnabled() && !getSkinnable().getGradientBarStops().isEmpty()) {
            Color color = getSkinnable().getGradientLookup().getColorAt(factor);
            fluidBody.setFill(color);
            fluidTop.setFill(color.darker());
        } else if (getSkinnable().getSectionsVisible() && !sections.isEmpty()) {
            int listSize = sections.size();
            for (int i = 0 ; i < listSize ; i++) {
                if (sections.get(i).contains(VALUE)) {
                    Color color = sections.get(i).getColor();
                    fluidBody.setFill(color);
                    fluidTop.setFill(color.darker());
                    break;
                }
            }
        }

        double centerY = height * 0.71111111 - factor * 0.58888889 * height;
        fluidTop.setCenterY(centerY);

        fluidUpperRight.setControlY1(centerY + 0.58888889 * height);
        fluidUpperRight.setControlY2(centerY);
        fluidUpperRight.setY(centerY);

        fluidUpperCenter.setControlY1(centerY + 0.06666667 * height);
        fluidUpperCenter.setControlY2(centerY + 0.12222222 * height);
        fluidUpperCenter.setY(centerY + 0.12222222 * height);

        fluidUpperLeft.setControlY1(centerY + 0.12222222 * height);
        fluidUpperLeft.setControlY2(centerY + 0.06666667 * height);
        fluidUpperLeft.setY(centerY);

        valueText.setText(String.format(locale, "%.0f%%", factor * 100));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, (height - valueText.getLayoutBounds().getHeight()) * 0.5);
    }

    private void resizeText() {
        double fontSize = 0.175 * width;
        valueText.setFont(Fonts.latoRegular(fontSize));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, (width - valueText.getLayoutBounds().getHeight()) * 0.5);

        fontSize = 0.15384615 * width;
        titleText.setFont(Fonts.robotoRegular(fontSize));
        titleText.setText(getSkinnable().getTitle());
        Helper.adjustTextSize(titleText, 0.9 * width, fontSize);
        titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.83333333 * height);
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();

        if (width > 0 && height > 0) {
            if (ASPECT_RATIO * width > height) {
                width = 1 / (ASPECT_RATIO / height);
            } else if (1 / (ASPECT_RATIO / height) > width) {
                height = ASPECT_RATIO * width;
            }

            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            tubeBottom.setCenterX(width * 0.5);
            tubeBottom.setCenterY(height * 0.71111111);
            tubeBottom.setRadiusX(width * 0.5);
            tubeBottom.setRadiusY(height * 0.12222222);
            tubeBottom.setFill(new LinearGradient(0, 0, width, 0,
                                                  false, CycleMethod.NO_CYCLE,
                                                  new Stop(0.0, Color.rgb(0, 0, 0, 0.2)),
                                                  new Stop(0.45, Color.rgb(255, 255, 255, 0.2)),
                                                  new Stop(1.0, Color.rgb(0, 0, 0, 0.2))));

            fluidUpperRight  = new CubicCurveTo(width, 0.7111111111111111 * height,
                                                width, 0.12222222222222222 * height,
                                                width, 0.12222222222222222 * height);
            fluidUpperCenter = new CubicCurveTo(width, 0.18888888888888888 * height,
                                                0.782051282051282 * width, 0.24444444444444444 * height,
                                                0.5 * width, 0.24444444444444444 * height);
            fluidUpperLeft   = new CubicCurveTo(0.21794871794871795 * width, 0.24444444444444444 * height,
                                                0.0, 0.18888888888888888 * height,
                                                0.0, 0.12222222222222222 * height);


            fluidBody.getElements().clear();
            fluidBody.getElements().add(new MoveTo(0.0, 0.7111111111111111 * height));
            fluidBody.getElements().add(new CubicCurveTo(0.0, 0.7777777777777778 * height,
                                                    0.21794871794871795 * width, 0.8333333333333334 * height,
                                                    0.5 * width, 0.8333333333333334 * height));
            fluidBody.getElements().add(new CubicCurveTo(0.782051282051282 * width, 0.8333333333333334 * height,
                                                    width, 0.7777777777777778 * height,
                                                    width, 0.7111111111111111 * height));
            fluidBody.getElements().add(fluidUpperRight);
            fluidBody.getElements().add(fluidUpperCenter);
            fluidBody.getElements().add(fluidUpperLeft);
            fluidBody.getElements().add(new CubicCurveTo(0.0, 0.12222222222222222 * height,
                                                    0.0, 0.7111111111111111 * height,
                                                    0.0, 0.7111111111111111 * height));
            fluidBody.getElements().add(new ClosePath());
            
            fluidTop.setCenterX(width * 0.5);
            fluidTop.setCenterY(height * 0.71111111);
            fluidTop.setRadiusX(width * 0.5);
            fluidTop.setRadiusY(height * 0.12222222);

            tube.getElements().clear();
            tube.getElements().add(new MoveTo(0.0, 0.7111111111111111 * height));
            tube.getElements().add(new CubicCurveTo(0.0, 0.7777777777777778 * height,
                                                    0.21794871794871795 * width, 0.8333333333333334 * height,
                                                    0.5 * width, 0.8333333333333334 * height));
            tube.getElements().add(new CubicCurveTo(0.782051282051282 * width, 0.8333333333333334 * height,
                                                    width, 0.7777777777777778 * height,
                                                    width, 0.7111111111111111 * height));
            tube.getElements().add(new CubicCurveTo(width, 0.7111111111111111 * height,
                                                    width, 0.12222222222222222 * height,
                                                    width, 0.12222222222222222 * height));
            tube.getElements().add(new CubicCurveTo(width, 0.18888888888888888 * height,
                                                    0.782051282051282 * width, 0.24444444444444444 * height,
                                                    0.5 * width, 0.24444444444444444 * height));
            tube.getElements().add(new CubicCurveTo(0.21794871794871795 * width, 0.24444444444444444 * height,
                                                    0.0, 0.18888888888888888 * height,
                                                    0.0, 0.12222222222222222 * height));
            tube.getElements().add(new CubicCurveTo(0.0, 0.12222222222222222 * height,
                                                    0.0, 0.7111111111111111 * height,
                                                    0.0, 0.7111111111111111 * height));
            tube.getElements().add(new ClosePath());
            tube.setFill(new LinearGradient(0, 0, width, 0,
                                            false, CycleMethod.NO_CYCLE,
                                            new Stop(0.00, Color.rgb(0, 0, 0, 0.35)),
                                            new Stop(0.20, Color.rgb(255, 255, 255, 0.15)),
                                            new Stop(0.26, Color.rgb(255, 255, 255, 0.2)),
                                            new Stop(0.33, Color.rgb(0, 0, 0, 0.22)),
                                            new Stop(0.81, Color.rgb(255, 255, 255, 0.2)),
                                            new Stop(1.00, Color.rgb(0, 0, 0, 0.35))));

            tubeTop.setCenterX(width * 0.5);
            tubeTop.setCenterY(height * 0.12222222);
            tubeTop.setRadiusX(width * 0.5);
            tubeTop.setRadiusY(height * 0.12222222);
            tubeTop.setFill(new LinearGradient(0, 0, width, 0,
                                               false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, Color.rgb(0, 0, 0, 0.25)),
                                               new Stop(0.52, Color.rgb(200, 200, 200, 0.25)),
                                               new Stop(1.0, Color.rgb(0, 0, 0, 0.25))));

            resizeText();

            redraw();
        }
    }

    private void redraw() {
        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();

        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * width))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        fluidBody.setFill(getSkinnable().getBarColor());
        fluidTop.setFill(getSkinnable().getBarColor().darker());

        valueText.setFill(getSkinnable().getValueColor());
        titleText.setFill(getSkinnable().getTitleColor());
        resizeText();

        setBar(getSkinnable().getCurrentValue());
    }
}
