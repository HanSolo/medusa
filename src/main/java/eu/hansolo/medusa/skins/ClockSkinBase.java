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

import eu.hansolo.medusa.Alarm;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.events.UpdateEventListener;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * Created by hansolo on 20.01.17.
 */
public abstract class ClockSkinBase extends SkinBase<Clock> implements Skin<Clock> {
    protected static final double       PREFERRED_WIDTH     = 250;
    protected static final double       PREFERRED_HEIGHT    = 250;
    protected static final double       MINIMUM_WIDTH       = 50;
    protected static final double       MINIMUM_HEIGHT      = 50;
    protected static final double       MAXIMUM_WIDTH       = 1024;
    protected static final double       MAXIMUM_HEIGHT      = 1024;

    protected Clock                     clock;
    protected InvalidationListener      sizeListener;
    protected UpdateEventListener       updateEventListener;
    protected InvalidationListener      currentTimeListener;
    protected InvalidationListener      timeListener;
    protected ListChangeListener<Alarm> alarmListener;


    // ******************** Constructors **************************************
    public ClockSkinBase(final Clock CLOCK) {
        super(CLOCK);

        clock               = CLOCK;
        sizeListener        = o -> handleEvents("RESIZE");
        updateEventListener = e -> handleEvents(e.eventType.name());
        currentTimeListener = o -> updateTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(clock.getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId())));
        timeListener        = o -> updateTime(clock.getTime());
        alarmListener       = c -> {
            updateAlarms();
            redraw();
        };
    }


    // ******************** Initialization ************************************
    protected abstract void initGraphics();

    protected void registerListeners() {
        clock.widthProperty().addListener(sizeListener);
        clock.heightProperty().addListener(sizeListener);
        clock.setOnUpdate(updateEventListener);
        if (clock.isAnimated()) {
            clock.currentTimeProperty().addListener(currentTimeListener);
        } else {
            clock.timeProperty().addListener(timeListener);
        }
        clock.getAlarms().addListener(alarmListener);
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefWidth(HEIGHT, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computePrefHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefHeight(WIDTH, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computeMaxWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_HEIGHT; }

    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        }
    }

    @Override public void dispose() {
        clock.widthProperty().removeListener(sizeListener);
        clock.heightProperty().removeListener(sizeListener);
        clock.removeUpdateEventListener(updateEventListener);
        if (clock.isAnimated()) {
            clock.currentTimeProperty().removeListener(currentTimeListener);
        } else {
            clock.timeProperty().removeListener(timeListener);
        }
        clock.getAlarms().removeListener(alarmListener);
        clock = null;
    }


    // ******************** Graphics ******************************************
    public abstract void updateTime(final ZonedDateTime TIME);

    public abstract void updateAlarms();


    // ******************** Resizing ******************************************
    protected abstract void resize();

    protected abstract void redraw();
}
