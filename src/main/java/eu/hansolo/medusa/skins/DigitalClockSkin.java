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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * Created by hansolo on 11.08.16.
 */
public class DigitalClockSkin extends ClockSkinBase {
    protected static final double          PREFERRED_WIDTH       = 250;
    protected static final double          PREFERRED_HEIGHT      = 100;
    protected static final double          MINIMUM_WIDTH         = 50;
    protected static final double          MINIMUM_HEIGHT        = 20;
    protected static final double          MAXIMUM_WIDTH         = 1024;
    protected static final double          MAXIMUM_HEIGHT        = 1024;
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

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    @Override protected void initGraphics() {
        // Set initial size
        if (Double.compare(clock.getPrefWidth(), 0.0) <= 0 || Double.compare(clock.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(clock.getWidth(), 0.0) <= 0 || Double.compare(clock.getHeight(), 0.0) <= 0) {
            if (clock.getPrefWidth() > 0 && clock.getPrefHeight() > 0) {
                clock.setPrefSize(clock.getPrefWidth(), clock.getPrefHeight());
            } else {
                clock.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        canvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ctx = canvas.getGraphicsContext2D();

        pane = new Pane(canvas);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {

        } else if ("SECTION".equals(EVENT_TYPE)) {
            redraw();
        }
    }


    // ******************** Canvas ********************************************
    private void drawTime(final ZonedDateTime TIME) {
        ctx.clearRect(0, 0, width, height);

        // draw the time
        if (clock.isTextVisible()) {
            ctx.setFill(textColor);
            ctx.setTextBaseline(VPos.CENTER);
            ctx.setTextAlign(TextAlignment.CENTER);
            if (Locale.US == clock.getLocale()) {
                ctx.setFont(Fonts.digital(0.5 * height));
                ctx.fillText(clock.isSecondsVisible() ? AMPM_HHMMSS_FORMATTER.format(TIME) : AMPM_HHMM_FORMATTER.format(TIME), centerX, clock.isDateVisible() ? height * 0.35 : centerY);
            } else {
                ctx.setFont(Fonts.digital(0.6 * height));
                ctx.fillText(clock.isSecondsVisible() ? HHMMSS_FORMATTER.format(TIME) : HHMM_FORMATTER.format(TIME), centerX, clock.isDateVisible() ? height * 0.3 : centerY);
            }
        }

        // draw the date
        if (clock.isDateVisible()) {
            ctx.setFill(dateColor);
            ctx.setFont(Fonts.digital(0.34 * height));
            ctx.fillText(dateFormat.format(TIME), centerX, height * 0.77);
        }

        // draw the alarmOn icon
        if (clock.isAlarmsEnabled() && clock.getAlarms().size() > 0) { ctx.setFill(alarmColor); }

    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        drawTime(TIME);
    }

    @Override public void updateAlarms() {}


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        centerX = width * 0.5;
        centerY = height * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((clock.getWidth() - height) * 0.5, (clock.getHeight() - height) * 0.5);

            canvas.setWidth(width);
            canvas.setHeight(height);
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * height))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        ZonedDateTime time = clock.getTime();

        textColor  = clock.getTextColor();
        dateColor  = clock.getDateColor();
        alarmColor = clock.getAlarmColor();

        drawTime(time);
    }
}