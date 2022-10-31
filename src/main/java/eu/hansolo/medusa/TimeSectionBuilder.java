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

import eu.hansolo.medusa.TimeSection.TimeSectionEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.time.LocalTime;
import java.util.HashMap;


/**
 * Created by hansolo on 31.01.16.
 */
public class TimeSectionBuilder<B extends TimeSectionBuilder<B>> {
    private HashMap<String, Property> properties = new HashMap<>();


    // ******************** Constructors **************************************
    protected TimeSectionBuilder() {}


    // ******************** Methods *******************************************
    public static final TimeSectionBuilder create() {
        return new TimeSectionBuilder();
    }

    public final B start(final LocalTime VALUE) {
        properties.put("start", new SimpleObjectProperty<>(VALUE));
        return (B)this;
    }

    public final B stop(final LocalTime VALUE) {
        properties.put("stop", new SimpleObjectProperty<>(VALUE));
        return (B)this;
    }

    public final B text(final String TEXT) {
        properties.put("text", new SimpleStringProperty(TEXT));
        return (B)this;
    }

    public final B icon(final Image IMAGE) {
        properties.put("icon", new SimpleObjectProperty<>(IMAGE));
        return (B)this;
    }

    public final B color(final Color COLOR) {
        properties.put("color", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B highlightColor(final Color COLOR) {
        properties.put("highlightColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B textColor(final Color COLOR) {
        properties.put("textColor", new SimpleObjectProperty<>(COLOR));
        return (B)this;
    }

    public final B onTimeSectionEntered(final EventHandler<TimeSectionEvent> HANDLER) {
        properties.put("onTimeSectionEntered", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final B onTimeSectionLeft(final EventHandler<TimeSectionEvent> HANDLER) {
        properties.put("onTimeSectionLeft", new SimpleObjectProperty<>(HANDLER));
        return (B)this;
    }

    public final TimeSection build() {
        final TimeSection section = new TimeSection();
        for (String key : properties.keySet()) {
            switch (key) {
                case "start"                -> section.setStart(((ObjectProperty<LocalTime>) properties.get(key)).get());
                case "stop"                 -> section.setStop(((ObjectProperty<LocalTime>) properties.get(key)).get());
                case "text"                 -> section.setText(((StringProperty) properties.get(key)).get());
                case "icon"                 -> section.setIcon(((ObjectProperty<Image>) properties.get(key)).get());
                case "color"                -> section.setColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "highlightColor"       -> section.setHighlightColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "textColor"            -> section.setTextColor(((ObjectProperty<Color>) properties.get(key)).get());
                case "onTimeSectionEntered" -> section.setOnTimeSectionEntered(((ObjectProperty<EventHandler>) properties.get(key)).get());
                case "onTimeSectionLeft"    -> section.setOnTimeSectionLeft(((ObjectProperty<EventHandler>) properties.get(key)).get());
            }
        }
        return section;
    }
}

