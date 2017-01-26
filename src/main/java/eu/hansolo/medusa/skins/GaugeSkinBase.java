/*
 * Copyright (c) 2017 by Gerrit Grunwald
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

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.events.UpdateEventListener;
import javafx.beans.InvalidationListener;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;


/**
 * Created by hansolo on 20.01.17.
 */
public abstract class GaugeSkinBase extends SkinBase<Gauge> implements Skin<Gauge> {
    protected static final double             PREFERRED_WIDTH  = 250;
    protected static final double             PREFERRED_HEIGHT = 250;
    protected static final double             MINIMUM_WIDTH    = 50;
    protected static final double             MINIMUM_HEIGHT   = 50;
    protected static final double             MAXIMUM_WIDTH    = 1024;
    protected static final double             MAXIMUM_HEIGHT   = 1024;
    protected Gauge                gauge;
    protected InvalidationListener sizeListener;
    protected UpdateEventListener  updateEventListener;


    protected GaugeSkinBase(final Gauge GAUGE) {
        super(GAUGE);
        gauge               = GAUGE;
        sizeListener        = o -> handleEvents("RESIZE");
        updateEventListener = e -> handleEvents(e.eventType.name());
    }

    protected void registerListeners() {
        getSkinnable().widthProperty().addListener(sizeListener);
        getSkinnable().heightProperty().addListener(sizeListener);
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
    }

    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        }
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefWidth(HEIGHT, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computePrefHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefHeight(WIDTH, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computeMaxWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_HEIGHT; }
    
    @Override public void dispose() {
        gauge.widthProperty().removeListener(sizeListener);
        gauge.heightProperty().removeListener(sizeListener);
        gauge.removeUpdateEventListener(updateEventListener);
    }

    protected void resize() {}

    protected void redraw() {}
}
