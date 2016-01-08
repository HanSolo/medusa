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

package eu.hansolo.medusa;

import javafx.geometry.Insets;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;


/**
 * Created by hansolo on 08.01.16.
 */
public class FGauge extends Region {
    public static final double PREFERRED_WIDTH  = 270;
    public static final double PREFERRED_HEIGHT = 270;
    public static final double MINIMUM_WIDTH    = 50;
    public static final double MINIMUM_HEIGHT   = 50;
    public static final double MAXIMUM_WIDTH    = 1024;
    public static final double MAXIMUM_HEIGHT   = 1024;

    // Model related
    private Gauge              gauge;

    // View related
    private double             size;
    private Region             frame;
    private Circle             background;
    private Circle             foreground;
    private GaugeDesign        design;
    private InnerShadow        innerShadow;


    // ******************** Constructors **************************************
    public FGauge(final Gauge GAUGE, final GaugeDesign DESIGN) {
        getStylesheets().add(getClass().getResource("framed-gauge.css").toExternalForm());
        getStyleClass().setAll("framed-gauge");

        gauge  = GAUGE;
        design = DESIGN;

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getWidth(), 0) <= 0 || Double.compare(getHeight(), 0) <= 0 ||
            Double.compare(getPrefWidth(), 0) <= 0 || Double.compare(getPrefHeight(), 0) <= 0) {
            setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        }
        if (Double.compare(getMinWidth(), 0) <= 0 || Double.compare(getMinHeight(), 0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }
        if (Double.compare(getMaxWidth(), 0) <= 0 || Double.compare(getMaxHeight(), 0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.35), 20, 0.0, 0, 20);

        frame = new Region();

        background = new Circle();
        background.setEffect(innerShadow);

        foreground = new Circle();

        getChildren().setAll(frame, background, gauge, foreground);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> redraw());
        heightProperty().addListener(o -> redraw());
    }


    // ******************** Methods *******************************************
    public GaugeDesign getDesign() { return design; }
    public void setDesign(final GaugeDesign DESIGN) {
        design = DESIGN;
        redraw();
    }

    public Gauge getGauge() { return gauge; }
    public void setGauge(final Gauge GAUGE) { gauge = GAUGE; }


    // ******************** Resizing ******************************************
    private void redraw() {
        double width  = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            if (getWidth() > getHeight()) {
                setTranslateX(0.5 * (getWidth() - size));
            } else if (getHeight() > getWidth()) {
                setTranslateY(0.5 * (getHeight() - size));
            }

            frame.setPrefSize(size, size);
            frame.setBorder(new Border(design.getBorderStrokes(size)));

            background.setCenterX(size * 0.5);
            background.setCenterY(size * 0.5);
            background.setRadius(size * 0.4375);

            switch(design) {
                case STEEL_SERIES:
                    background.setFill(new LinearGradient(0, background.getLayoutBounds().getMinY(), 0, background.getLayoutBounds().getMaxY(), false, CycleMethod.NO_CYCLE,
                                                          new Stop(0, Color.BLACK), new Stop(0.39, Color.rgb(50,50,50)), new Stop(0.40, Color.rgb(51, 51, 51)), new Stop(1.0, Color.rgb(153,153,153))));
                    innerShadow.setColor(Color.rgb(0, 0, 0, 0.65));
                    innerShadow.setRadius(0.08 * size);
                    innerShadow.setOffsetX(0);
                    innerShadow.setOffsetY(0.0 * size);
                    foreground.setFill(new RadialGradient(0, 0, size * 0.5, size * design.FRAME_FACTOR * 0.5, size * 0.4, false, CycleMethod.NO_CYCLE,
                                                          new Stop(0, Color.rgb(255, 255, 255, 0.6)),
                                                          new Stop(1, Color.rgb(255, 255, 255, 0))));
                    foreground.setStroke(null);
                    break;
                case ENZO:
                    background.setFill(Color.rgb(240, 240, 240));
                    innerShadow.setRadius(0.07407407 * size);
                    innerShadow.setOffsetX(0);
                    innerShadow.setOffsetY(0.07407407 * size);
                    innerShadow.setColor(Color.rgb(0, 0, 0, 0.35));
                    foreground.setFill(Color.TRANSPARENT);
                    foreground.setStroke(null);
                    break;
            }

            gauge.setPrefSize(size * (1d - design.FRAME_FACTOR * 2d), size * (1d - design.FRAME_FACTOR * 2d));
            gauge.relocate(design.FRAME_FACTOR * size, design.FRAME_FACTOR * size);

            foreground.setCenterX(size * 0.5);
            foreground.setCenterY(size * 0.5);
            foreground.setRadius(size * 0.42);
        }
    }
}
