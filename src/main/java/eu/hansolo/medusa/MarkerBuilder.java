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

import eu.hansolo.medusa.Marker.MarkerType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;

import java.util.HashMap;


/**
 * Created by hansolo on 05.01.16.
 */
public class MarkerBuilder<B extends MarkerBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected MarkerBuilder() {}


    // ******************** Methods *******************************************
    public static final MarkerBuilder create() {
        return new MarkerBuilder();
    }

    public final B value(final double VALUE) {
        properties.put("value", new SimpleDoubleProperty(VALUE));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B markerType(final MarkerType TYPE) {
        properties.put("markerType", new SimpleObjectProperty<>(TYPE));
        return (B)this;
    }

    public final B styleClass(final String STYLE_CLASS) {
        properties.put("styleClass", new SimpleStringProperty(STYLE_CLASS));
        return (B)this;
    }

    public final B onMarkerPressed(final EventHandler<Marker.MarkerEvent> HANDLER) {
        properties.put("onMarkerPressed", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onMarkerReleased(final EventHandler<Marker.MarkerEvent> HANDLER) {
        properties.put("onMarkerReleased", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onMarkerExceeded(final EventHandler<Marker.MarkerEvent> HANDLER) {
        properties.put("onMarkerExceeded", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onMarkerUnderrun(final EventHandler<Marker.MarkerEvent> HANDLER) {
        properties.put("onMarkerUnderrun", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final Marker build() {
        final Marker marker = new Marker();
        for (String key : properties.keySet()) {
            switch (key) {
                case "value"            -> marker.setValue(((DoubleProperty) properties.get(key)).get());
                case "text"             -> marker.setText(((StringProperty) properties.get(key)).get());
                case "color"            -> marker.setColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "markerType"       -> marker.setMarkerType(((ObjectProperty<MarkerType>) properties.get(key)).get());
                case "onMarkerPressed"  -> marker.setOnMarkerPressed(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onMarkerReleased" -> marker.setOnMarkerReleased(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onMarkerExceeded" -> marker.setOnMarkerExceeded(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onMarkerUnderrun" -> marker.setOnMarkerUnderrun(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "styleClass"       -> marker.setStyleClass(((StringProperty) properties.get(key)).get());
            }
        }
        return marker;
    }
}

