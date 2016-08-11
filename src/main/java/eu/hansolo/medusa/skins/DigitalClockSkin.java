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

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * Created by hansolo on 11.08.16.
 */
public class DigitalClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH       = 250;
    private static final double            PREFERRED_HEIGHT      = 100;
    private static final double            MINIMUM_WIDTH         = 50;
    private static final double            MINIMUM_HEIGHT        = 20;
    private static final double            MAXIMUM_WIDTH         = 1024;
    private static final double            MAXIMUM_HEIGHT        = 1024;
    private static final DateTimeFormatter HHMM_FORMATTER        = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HHMMSS_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter AMPM_HHMM_FORMATTER   = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter AMPM_HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private              double            aspectRatio           = 0.4;
    private              double            width;
    private              double            height;
    private              double            centerX;
    private              double            centerY;
    private              DateTimeFormatter dateFormat;
    private              Pane              pane;
    private              Canvas            canvas;
    private              GraphicsContext   ctx;
    private              Color             textColor;
    private              Color             dateColor;
    private              Color             alarmColor;


    // ******************** Constructors **************************************
    public DigitalClockSkin(Clock clock) {
        super(clock);
        textColor  = clock.getTextColor();
        dateColor  = clock.getDateColor();
        alarmColor = clock.getAlarmColor();
        dateFormat = Helper.getDateFormat(clock.getLocale());

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
        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        pane = new Pane(canvas);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        if (getSkinnable().isAnimated()) {
            getSkinnable().currentTimeProperty().addListener(o -> updateTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(getSkinnable().getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId()))));
        } else {
            getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
        }
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {

        } else if ("SECTION".equals(EVENT_TYPE)) {
            redraw();
        }
    }


    // ******************** Canvas ********************************************
    private void drawTime(final ZonedDateTime TIME) {
        ctx.clearRect(0, 0, width, height);

        // draw the time
        if (getSkinnable().isTextVisible()) {
            ctx.setFill(textColor);
            ctx.setTextBaseline(VPos.CENTER);
            ctx.setTextAlign(TextAlignment.CENTER);
            if (Locale.US == getSkinnable().getLocale()) {
                ctx.setFont(Fonts.digital(0.5 * height));
                ctx.fillText(getSkinnable().isSecondsVisible() ? AMPM_HHMMSS_FORMATTER.format(TIME) : AMPM_HHMM_FORMATTER.format(TIME), centerX, getSkinnable().isDateVisible() ? height * 0.35 : centerY);
            } else {
                ctx.setFont(Fonts.digital(0.6 * height));
                ctx.fillText(getSkinnable().isSecondsVisible() ? HHMMSS_FORMATTER.format(TIME) : HHMM_FORMATTER.format(TIME), centerX, getSkinnable().isDateVisible() ? height * 0.3 : centerY);
            }
        }

        // draw the date
        if (getSkinnable().isDateVisible()) {
            ctx.setFill(dateColor);
            ctx.setFont(Fonts.digital(0.34 * height));
            ctx.fillText(dateFormat.format(TIME), centerX, height * 0.77);
        }

        // draw the alarmOn icon
        if (getSkinnable().isAlarmsEnabled() && getSkinnable().getAlarms().size() > 0) { ctx.setFill(alarmColor); }

    }

    private void updateTime(final ZonedDateTime TIME) {
        drawTime(TIME);
    }


    // ******************** Resizing ******************************************
    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        centerX = width * 0.5;
        centerY = height * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - height) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            canvas.setWidth(width);
            canvas.setHeight(height);
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * height))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        ZonedDateTime time = getSkinnable().getTime();

        textColor       = getSkinnable().getTextColor();
        dateColor       = getSkinnable().getDateColor();
        alarmColor      = getSkinnable().getAlarmColor();

        drawTime(time);
    }
}