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
package eu.hansolo.medusa;

import eu.hansolo.medusa.GaugeDesign.GaugeBackground;
import eu.hansolo.medusa.skins.GaugeSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.control.Skin;

import java.util.HashMap;


/**
 * Created by hansolo on 13.01.16.
 */
public class FGaugeBuilder<B extends FGaugeBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected FGaugeBuilder() {}


    // ******************** Methods *******************************************
    public static final FGaugeBuilder create() {
        return new FGaugeBuilder();
    }

    public final B gauge(final Gauge GAUGE) {
        properties.put("gauge", new SimpleObjectProperty<>(GAUGE));
        return (B)this;
    }

    public final B gaugeDesign(final GaugeDesign DESIGN) {
        properties.put("gaugeDesign", new SimpleObjectProperty<>(DESIGN));
        return (B)this;
    }

    public final B gaugeBackground(final GaugeBackground BACKGROUND) {
        properties.put("gaugeBackground", new SimpleObjectProperty<>(BACKGROUND));
        return (B)this;
    }

    public final B foregroundVisible(final boolean VISIBLE) {
        properties.put("foregroundVisible", new SimpleBooleanProperty(VISIBLE));
        return (B)this;
    }

    public final B prefSize(final double WIDTH, final double HEIGHT) {
        properties.put("prefSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B minSize(final double WIDTH, final double HEIGHT) {
        properties.put("minSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }
    public final B maxSize(final double WIDTH, final double HEIGHT) {
        properties.put("maxSize", new SimpleObjectProperty<>(new Dimension2D(WIDTH, HEIGHT)));
        return (B)this;
    }

    public final B prefWidth(final double PREF_WIDTH) {
        properties.put("prefWidth", new SimpleDoubleProperty(PREF_WIDTH));
        return (B)this;
    }
    public final B prefHeight(final double PREF_HEIGHT) {
        properties.put("prefHeight", new SimpleDoubleProperty(PREF_HEIGHT));
        return (B)this;
    }

    public final B minWidth(final double MIN_WIDTH) {
        properties.put("minWidth", new SimpleDoubleProperty(MIN_WIDTH));
        return (B)this;
    }
    public final B minHeight(final double MIN_HEIGHT) {
        properties.put("minHeight", new SimpleDoubleProperty(MIN_HEIGHT));
        return (B)this;
    }

    public final B maxWidth(final double MAX_WIDTH) {
        properties.put("maxWidth", new SimpleDoubleProperty(MAX_WIDTH));
        return (B)this;
    }
    public final B maxHeight(final double MAX_HEIGHT) {
        properties.put("maxHeight", new SimpleDoubleProperty(MAX_HEIGHT));
        return (B)this;
    }

    public final B scaleX(final double SCALE_X) {
        properties.put("scaleX", new SimpleDoubleProperty(SCALE_X));
        return (B)this;
    }
    public final B scaleY(final double SCALE_Y) {
        properties.put("scaleY", new SimpleDoubleProperty(SCALE_Y));
        return (B)this;
    }

    public final B layoutX(final double LAYOUT_X) {
        properties.put("layoutX", new SimpleDoubleProperty(LAYOUT_X));
        return (B)this;
    }
    public final B layoutY(final double LAYOUT_Y) {
        properties.put("layoutY", new SimpleDoubleProperty(LAYOUT_Y));
        return (B)this;
    }

    public final B translateX(final double TRANSLATE_X) {
        properties.put("translateX", new SimpleDoubleProperty(TRANSLATE_X));
        return (B)this;
    }
    public final B translateY(final double TRANSLATE_Y) {
        properties.put("translateY", new SimpleDoubleProperty(TRANSLATE_Y));
        return (B)this;
    }

    public final FGauge build() {
        Gauge           gauge      = null == properties.get("gauge") ? new Gauge() : ((ObjectProperty<Gauge>) properties.get("gauge")).get();
        GaugeDesign     design     = null == properties.get("gaugeDesign") ? GaugeDesign.METAL : ((ObjectProperty<GaugeDesign>) properties.get("gaugeDesign")).get();
        GaugeBackground background = null == properties.get("gaugeBackground") ? GaugeBackground.DARK_GRAY : ((ObjectProperty<GaugeBackground>) properties.get("gaugeBackground")).get();

        Skin skin = gauge.getSkin();
        if (null != skin && gauge.getSkin().getClass() != GaugeSkin.class) {
            throw new RuntimeException("Please change Skin to GaugeSkin.");
        }

        final FGauge fGauge = new FGauge(gauge, design, background);
        for (String key : properties.keySet()) {
            switch (key) {
                case "prefSize"          -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    fGauge.setPrefSize(dim.getWidth(), dim.getHeight());
                }
                case "minSize"           -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    fGauge.setPrefSize(dim.getWidth(), dim.getHeight());
                }
                case "maxSize"           -> {
                    Dimension2D dim = ((ObjectProperty<Dimension2D>) properties.get(key)).get();
                    fGauge.setPrefSize(dim.getWidth(), dim.getHeight());
                }
                case "prefWidth"         -> fGauge.setPrefWidth(((DoubleProperty) properties.get(key)).get());
                case "prefHeight"        -> fGauge.setPrefHeight(((DoubleProperty) properties.get(key)).get());
                case "minWidth"          -> fGauge.setMinWidth(((DoubleProperty) properties.get(key)).get());
                case "minHeight"         -> fGauge.setMinHeight(((DoubleProperty) properties.get(key)).get());
                case "maxWidth"          -> fGauge.setMaxWidth(((DoubleProperty) properties.get(key)).get());
                case "maxHeight"         -> fGauge.setMaxHeight(((DoubleProperty) properties.get(key)).get());
                case "scaleX"            -> fGauge.setScaleX(((DoubleProperty) properties.get(key)).get());
                case "scaleY"            -> fGauge.setScaleY(((DoubleProperty) properties.get(key)).get());
                case "layoutX"           -> fGauge.setLayoutX(((DoubleProperty) properties.get(key)).get());
                case "layoutY"           -> fGauge.setLayoutY(((DoubleProperty) properties.get(key)).get());
                case "translateX"        -> fGauge.setTranslateX(((DoubleProperty) properties.get(key)).get());
                case "translateY"        -> fGauge.setTranslateY(((DoubleProperty) properties.get(key)).get());
                case "foregroundVisible" -> fGauge.setForegroundVisible(((BooleanProperty) properties.get(key)).get());
            }
        }
        return fGauge;
    }
}
