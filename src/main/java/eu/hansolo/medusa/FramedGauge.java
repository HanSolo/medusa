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

package eu.hansolo.medusa;

import javafx.scene.layout.Region;


/**
 * Created by hansolo on 18.12.15.
 */
public class FramedGauge extends Region {
    public static final double PREFERRED_WIDTH  = 270;
    public static final double PREFERRED_HEIGHT = 270;
    public static final double MINIMUM_WIDTH    = 50;
    public static final double MINIMUM_HEIGHT   = 50;
    public static final double MAXIMUM_WIDTH    = 1024;
    public static final double MAXIMUM_HEIGHT   = 1024;

    // Model related
    private Gauge               gauge;

    // View related
    private double              size;
    private Region              frame;
    private GaugeDesign         design;


    // ******************** Constructors **************************************
    public FramedGauge(final Gauge GAUGE, final GaugeDesign DESIGN) {
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
        frame = new Region();
        frame.getStyleClass().add(design.FRAME_STYLE);

        getChildren().setAll(frame, gauge);
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
            gauge.setPrefSize(size * (1d - design.FRAME_FACTOR * 2d), size * (1d - design.FRAME_FACTOR * 2d));
            gauge.relocate(design.FRAME_FACTOR * size, design.FRAME_FACTOR * size);

            frame.setStyle(design.getInsets(size));
        }
    }
}
