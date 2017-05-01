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
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
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

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 25.01.16.
 */
public class LevelSkin extends GaugeSkinBase {
    protected static final double PREFERRED_WIDTH  = 216;
    protected static final double PREFERRED_HEIGHT = 249;
    protected static final double MINIMUM_WIDTH    = 50;
    protected static final double MINIMUM_HEIGHT   = 50;
    protected static final double MAXIMUM_WIDTH    = 1024;
    protected static final double MAXIMUM_HEIGHT   = 1024;
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
    private Locale        locale;
    private List<Section> sections;
    private InvalidationListener currentValueListener;



    // ******************** Constructors **************************************
    public LevelSkin(Gauge gauge) {
        super(gauge);
        locale               = gauge.getLocale();
        sections             = gauge.getSections();
        barTooltip           = new Tooltip();
        currentValueListener = o -> setBar(gauge.getCurrentValue());
        barTooltip.setTextAlignment(TextAlignment.CENTER);

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

        valueText = new Text(String.format(locale, "%.0f%%", gauge.getCurrentValue()));
        valueText.setMouseTransparent(true);
        Helper.enableNode(valueText, gauge.isValueVisible());

        titleText = new Text(gauge.getTitle());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        // Add all nodes
        pane = new Pane(tubeBottom, fluidBody, fluidTop, tube, tubeTop, valueText, titleText);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);

        handleEvents("VISIBILITY");
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            resize();
            redraw();
            setBar(gauge.getCurrentValue());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = gauge.getSections();
            resize();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(titleText, !gauge.getUnit().isEmpty());
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            StringBuilder content = new StringBuilder(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getValue()))
                .append("\n(").append(valueText.getText()).append(")");
            barTooltip.setText(content.toString());
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void setBar(final double VALUE) {
        double factor  = VALUE / gauge.getRange();

        if (gauge.isGradientBarEnabled() && !gauge.getGradientBarStops().isEmpty()) {
            Color color = gauge.getGradientLookup().getColorAt(factor);
            fluidBody.setFill(color);
            fluidTop.setFill(color.darker());
        } else if (gauge.getSectionsVisible() && !sections.isEmpty()) {
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
        titleText.setText(gauge.getTitle());
        Helper.adjustTextSize(titleText, 0.9 * width, fontSize);
        titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.83333333 * height);
    }

    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();

        if (width > 0 && height > 0) {
            if (ASPECT_RATIO * width > height) {
                width = 1 / (ASPECT_RATIO / height);
            } else if (1 / (ASPECT_RATIO / height) > width) {
                height = ASPECT_RATIO * width;
            }

            pane.setMaxSize(width, height);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

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

    @Override protected void redraw() {
        locale = gauge.getLocale();

        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * width))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        fluidBody.setFill(gauge.getBarColor());
        fluidTop.setFill(gauge.getBarColor().darker());

        valueText.setFill(gauge.getValueColor());
        titleText.setFill(gauge.getTitleColor());
        resizeText();

        setBar(gauge.getCurrentValue());
    }
}
