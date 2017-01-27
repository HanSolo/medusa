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

import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeDesign.GaugeBackground;
import eu.hansolo.medusa.skins.GaugeSkin;
import javafx.scene.control.Skin;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;


/**
 * A Region that embeds a given Gauge control. Keep in mind that
 * this only works with the round Medusa Skins and not with
 * skins like BulletChartSkin and QuarterSkin.
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
    private Gauge           gauge;

    // View related
    private double          size;
    private Region          frame;
    private Circle          background;
    private Circle          foreground;
    private GaugeDesign     gaugeDesign;
    private GaugeBackground gaugeBackground;
    private InnerShadow     innerShadow;


    // ******************** Constructors **************************************
    public FGauge() {
        this(new Gauge(), GaugeDesign.BLACK_METAL, GaugeBackground.BEIGE);
    }
    public FGauge(final Gauge GAUGE, final GaugeDesign DESIGN) {
        this(GAUGE, DESIGN, GaugeBackground.DARK_GRAY);
    }
    public FGauge(final Gauge GAUGE, final GaugeDesign DESIGN, final GaugeBackground BACKGROUND) {
        getStylesheets().add(getClass().getResource("framed-gauge.css").toExternalForm());
        getStyleClass().setAll("framed-gauge");
        gauge           = GAUGE;
        gaugeDesign     = DESIGN;
        gaugeBackground = BACKGROUND;

        Skin skin = gauge.getSkin();
        if (null != skin && gauge.getSkin().getClass() != GaugeSkin.class) {
            throw new RuntimeException("Please change Skin to GaugeSkin.");
        }

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
        foreground.setMouseTransparent(true);

        getChildren().setAll(frame, background, gauge, foreground);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> redraw());
        heightProperty().addListener(o -> redraw());
    }


    // ******************** Methods *******************************************
    public GaugeDesign getGaugeDesign() { return gaugeDesign; }
    public void setGaugeDesign(final GaugeDesign DESIGN) {
        gaugeDesign = DESIGN;
        redraw();
    }

    public GaugeBackground getGaugeBackground() { return gaugeBackground; }
    public void setGaugeBackground(final GaugeBackground BACKGROUND) {
        gaugeBackground = BACKGROUND;
        redraw();
    }

    public boolean isForegroundVisible() { return foreground.isVisible(); }
    public void setForegroundVisible(final boolean VISIBLE) {
        foreground.setVisible(VISIBLE);
        foreground.setManaged(VISIBLE);
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
            frame.setBorder(gaugeDesign.getBorder(size));

            background.setCenterX(size * 0.5);
            background.setCenterY(size * 0.5);
            background.setRadius(size * 0.4375);

            background.setFill(gaugeBackground.getPaint(background.getLayoutBounds().getMinX(), background.getLayoutBounds().getMinY(),
                                                        background.getLayoutBounds().getMaxX(), background.getLayoutBounds().getMaxY()));

            switch(gaugeDesign) {
                case NONE:
                    frame.setVisible(false);
                    foreground.setVisible(false);
                    background.setVisible(false);
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
                default:
                    frame.setVisible(true);
                    foreground.setVisible(true);
                    background.setVisible(true);
                    innerShadow.setColor(Color.rgb(0, 0, 0, 0.65));
                    innerShadow.setRadius(0.08 * size);
                    innerShadow.setOffsetX(0);
                    innerShadow.setOffsetY(0.0 * size);
                    foreground.setFill(new RadialGradient(0, 0, size * 0.5, size * gaugeDesign.FRAME_FACTOR * 0.5, size, false, CycleMethod.NO_CYCLE,
                                                          new Stop(0, Color.rgb(255, 255, 255, 0.6)),
                                                          new Stop(0.4, Color.TRANSPARENT)));
                    foreground.setStroke(null);
                    break;
            }

            gauge.setPrefSize(size * (1.0 - gaugeDesign.FRAME_FACTOR * 2.0), size * (1.0 - gaugeDesign.FRAME_FACTOR * 2.0));
            gauge.relocate(gaugeDesign.FRAME_FACTOR * size, gaugeDesign.FRAME_FACTOR * size);

            foreground.setCenterX(size * 0.5);
            foreground.setCenterY(size * 0.5);
            foreground.setRadius(size * 0.42);
        }
    }
}
