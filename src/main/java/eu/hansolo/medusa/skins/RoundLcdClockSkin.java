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
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Locale;


/**
 * Created by hansolo on 02.02.16.
 */
public class RoundLcdClockSkin extends ClockSkinBase {
    private static final DateTimeFormatter TIME_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter AMPM_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private              double            size;
    private              double            center;
    private              DateTimeFormatter dateFormat;
    private              Pane              pane;
    private              Canvas            backgroundCanvas;
    private              GraphicsContext   backgroundCtx;
    private              Canvas            foregroundCanvas;
    private              GraphicsContext   foregroundCtx;
    private              Canvas            hoursCanvas;
    private              GraphicsContext   hoursCtx;
    private              Canvas            minutesCanvas;
    private              GraphicsContext   minutesCtx;
    private              Canvas            secondsCanvas;
    private              GraphicsContext   secondsCtx;
    private              Color             hourColor;
    private              Color             minuteColor;
    private              Color             fiveMinuteColor;
    private              Color             secondColor;
    private              Color             titleColor;
    private              Color             textColor;
    private              Color             dateColor;
    private              Color             alarmColor;


    // ******************** Constructors **************************************
    public RoundLcdClockSkin(Clock clock) {
        super(clock);
        hourColor       = clock.getHourColor();
        minuteColor     = clock.getMinuteColor();
        fiveMinuteColor = minuteColor.darker();
        secondColor     = clock.getSecondColor();
        titleColor      = clock.getTitleColor();
        textColor       = clock.getTextColor();
        dateColor       = clock.getDateColor();
        alarmColor      = clock.getAlarmColor();
        dateFormat      = Helper.getDateFormat(clock.getLocale());

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

        backgroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        backgroundCtx = backgroundCanvas.getGraphicsContext2D();

        foregroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        foregroundCtx = foregroundCanvas.getGraphicsContext2D();

        hoursCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        hoursCtx = hoursCanvas.getGraphicsContext2D();

        minutesCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        minutesCtx = minutesCanvas.getGraphicsContext2D();

        secondsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        secondsCtx = secondsCanvas.getGraphicsContext2D();

        pane = new Pane(backgroundCanvas, foregroundCanvas, hoursCanvas, minutesCanvas, secondsCanvas);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

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
    private void drawForeground(final ZonedDateTime TIME) {
        foregroundCtx.clearRect(0, 0, size, size);

        Locale locale = clock.getLocale();

        // draw the time
        if (clock.isTextVisible()) {
            foregroundCtx.setFill(textColor);
            foregroundCtx.setTextBaseline(VPos.CENTER);
            foregroundCtx.setTextAlign(TextAlignment.CENTER);
            if (Locale.US == locale) {
                foregroundCtx.setFont(Fonts.digital(0.17 * size));
                foregroundCtx.fillText(AMPM_TIME_FORMATTER.format(TIME), center, center);
            } else {
                foregroundCtx.setFont(Fonts.digital(0.2 * size));
                foregroundCtx.fillText(TIME_FORMATTER.format(TIME), center, center);
            }
        }

        // draw the date
        if (clock.isDateVisible()) {
            foregroundCtx.setFill(dateColor);
            foregroundCtx.setFont(Fonts.digital(0.09 * size));
            foregroundCtx.fillText(dateFormat.format(TIME), center, size * 0.65);
        }

        // draw the alarmOn icon
        if (clock.isAlarmsEnabled() && clock.getAlarms().size() > 0) {
            foregroundCtx.setFill(alarmColor);
            drawAlarmIcon(foregroundCtx, foregroundCtx.getFill());
        }
    }

    private void drawHours(final ZonedDateTime TIME) {
        int hourCounter = 1;
        int hour        = TIME.getHour();
        double strokeWidth = size * 0.06;
        hoursCtx.setLineCap(StrokeLineCap.BUTT);
        hoursCtx.clearRect(0, 0, size, size);
        for (int i = 450 ; i >= 90 ; i--) {
            hoursCtx.save();
            if (i % 30 == 0) {
                //draw hours
                hoursCtx.setStroke(hourColor);
                hoursCtx.setLineWidth(strokeWidth);
                if (hour == 0 || hour == 12) {
                    hoursCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1 - 30, 28, ArcType.OPEN);
                } else if (hourCounter <= (TIME.get(ChronoField.AMPM_OF_DAY) == 1 ? hour - 12 : hour)) {
                    hoursCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1 - 30, 28, ArcType.OPEN);
                    hourCounter++;
                }
            }
            hoursCtx.restore();
        }
    }

    private void drawMinutes(final ZonedDateTime TIME) {
        int minCounter  = 1;
        double strokeWidth = size * 0.06;
        minutesCtx.clearRect(0, 0, size, size);
        minutesCtx.setLineCap(StrokeLineCap.BUTT);
        for (int i = 450 ; i >= 90 ; i--) {
            minutesCtx.save();
            if (i % 6 == 0) {
                // draw minutes
                if (minCounter <= TIME.getMinute()) {
                    minutesCtx.setStroke(minCounter % 5 == 0 ? fiveMinuteColor : minuteColor);
                    minutesCtx.setLineWidth(strokeWidth);
                    minutesCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.1, strokeWidth * 0.5 + strokeWidth * 1.1, size - strokeWidth - strokeWidth * 2.2, size - strokeWidth - strokeWidth * 2.2, i + 1 - 6, 4, ArcType.OPEN);
                    minCounter++;
                }
            }
            minutesCtx.restore();
        }
    }

    private void drawSeconds(final ZonedDateTime TIME) {
        int secCounter  = 1;
        double strokeWidth = size * 0.06;
        secondsCtx.setLineCap(StrokeLineCap.BUTT);
        secondsCtx.clearRect(0, 0, size, size);
        for (int i = 450 ; i >= 90 ; i--) {
            secondsCtx.save();
            if (i % 6 == 0) {
                // draw seconds
                if (secCounter <= TIME.getSecond() + 1) {
                    secondsCtx.setStroke(secondColor);
                    secondsCtx.setLineWidth(strokeWidth * 0.25);
                    secondsCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.8, strokeWidth * 0.5 + strokeWidth * 1.8, size - strokeWidth - strokeWidth * 3.6, size - strokeWidth - strokeWidth * 3.6, i + 1 - 6, 4, ArcType.OPEN);
                    secCounter++;
                }
            }
            secondsCtx.restore();
        }
    }

    private void drawBackground() {
        double strokeWidth = size * 0.06;
        backgroundCtx.setLineCap(StrokeLineCap.BUTT);
        backgroundCtx.clearRect(0, 0, size, size);
        // draw translucent background
        backgroundCtx.setStroke(Color.rgb(0, 12, 6, 0.1));
        Color hColor = Helper.getTranslucentColorFrom(hourColor, 0.1);
        Color mColor = Helper.getTranslucentColorFrom(minuteColor, 0.1);
        Color sColor = Helper.getTranslucentColorFrom(secondColor, 0.1);
        for (int i = 0 ; i < 360 ; i++) {
            backgroundCtx.save();
            if (i % 6 == 0) {
                // draw minutes
                backgroundCtx.setStroke(mColor);
                backgroundCtx.setLineWidth(strokeWidth);
                backgroundCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.1, strokeWidth * 0.5 + strokeWidth * 1.1, size - strokeWidth - strokeWidth * 2.2, size - strokeWidth - strokeWidth * 2.2, i + 1, 4, ArcType.OPEN);

                // draw seconds
                backgroundCtx.setStroke(sColor);
                backgroundCtx.setLineWidth(strokeWidth * 0.25);
                backgroundCtx.strokeArc(strokeWidth * 0.5 + strokeWidth * 1.8, strokeWidth * 0.5 + strokeWidth * 1.8, size - strokeWidth - strokeWidth * 3.6, size - strokeWidth - strokeWidth * 3.6, i + 1, 4, ArcType.OPEN);
            }
            if (i % 30 == 0) {
                //draw hours
                backgroundCtx.setStroke(hColor);
                backgroundCtx.setLineWidth(strokeWidth);
                backgroundCtx.strokeArc(strokeWidth * 0.5, strokeWidth * 0.5, size - strokeWidth, size - strokeWidth, i + 1, 28, ArcType.OPEN);
            }
            backgroundCtx.restore();
        }

        // draw the title
        if (clock.isTitleVisible()) {
            String title = clock.getTitle();
            int    l     = title.length();
            char[] bkgChrs = new char[l];
            Arrays.fill(bkgChrs, '8');
            backgroundCtx.setFill(Helper.getTranslucentColorFrom(titleColor, 0.1));
            backgroundCtx.setFont(Fonts.digitalReadoutBold(0.09 * size));
            backgroundCtx.setTextBaseline(VPos.CENTER);
            backgroundCtx.setTextAlign(TextAlignment.CENTER);
            backgroundCtx.fillText(new String(bkgChrs), center, size * 0.35, size * 0.55);
            backgroundCtx.setFill(titleColor);
            backgroundCtx.fillText(title, center, size * 0.35, size * 0.55);
        }

        Locale locale = clock.getLocale();
        // draw the text
        if (clock.isTextVisible()) {
            backgroundCtx.setFill(Helper.getTranslucentColorFrom(textColor, 0.1));
            backgroundCtx.setTextBaseline(VPos.CENTER);
            backgroundCtx.setTextAlign(TextAlignment.CENTER);
            if (Locale.US == locale) {
                backgroundCtx.setFont(Fonts.digital(0.17 * size));
                backgroundCtx.fillText("88:88 88", center, center);
            } else {
                backgroundCtx.setFont(Fonts.digital(0.2 * size));
                backgroundCtx.fillText("88:88", center, center);
            }
        }

        // draw the date
        if (clock.isDateVisible()) {
            backgroundCtx.setFill(Helper.getTranslucentColorFrom(dateColor, 0.1));
            backgroundCtx.setFont(Fonts.digital(0.09 * size));
            if (Locale.US == locale) {
                backgroundCtx.fillText("88/88/8888", center, size * 0.65);
            } else if (Locale.CHINA == locale) {
                backgroundCtx.fillText("8888.88.88", center, size * 0.65);
            } else {
                backgroundCtx.fillText("88.88.8888", center, size * 0.65);
            }
        }

        // draw the alarmOn icon
        if (clock.isAlarmsEnabled() && clock.getAlarms().size() > 0) {
            backgroundCtx.setFill(Helper.getTranslucentColorFrom(alarmColor, 0.1));
            drawAlarmIcon(backgroundCtx, backgroundCtx.getFill());
        }
    }

    private void drawAlarmIcon(final GraphicsContext CTX, final Paint COLOR) {
        double iconSize = 0.1 * size;
        CTX.save();
        CTX.translate((size - iconSize) * 0.5, size * 0.25);
        CTX.beginPath();
        CTX.moveTo(0.6875 * iconSize, 0.875 * iconSize);
        CTX.bezierCurveTo(0.625 * iconSize, 0.9375 * iconSize, 0.5625 * iconSize, iconSize, 0.5 * iconSize, iconSize);
        CTX.bezierCurveTo(0.4375 * iconSize, iconSize, 0.375 * iconSize, 0.9375 * iconSize, 0.375 * iconSize, 0.875 * iconSize);
        CTX.bezierCurveTo(0.375 * iconSize, 0.875 * iconSize, 0.6875 * iconSize, 0.875 * iconSize, 0.6875 * iconSize, 0.875 * iconSize);
        CTX.closePath();
        CTX.moveTo(iconSize, 0.8125 * iconSize);
        CTX.bezierCurveTo(0.6875 * iconSize, 0.5625 * iconSize, 0.9375 * iconSize, 0.0, 0.5 * iconSize, 0.0);
        CTX.bezierCurveTo(0.5 * iconSize, 0.0, 0.5 * iconSize, 0.0, 0.5 * iconSize, 0.0);
        CTX.bezierCurveTo(0.5 * iconSize, 0.0, 0.5 * iconSize, 0.0, 0.5 * iconSize, 0.0);
        CTX.bezierCurveTo(0.125 * iconSize, 0.0, 0.375 * iconSize, 0.5625 * iconSize, 0.0, 0.8125 * iconSize);
        CTX.bezierCurveTo(0.0, 0.8125 * iconSize, 0.0, 0.8125 * iconSize, 0.0, 0.8125 * iconSize);
        CTX.bezierCurveTo(0.0, 0.8125 * iconSize, 0.0, 0.8125 * iconSize, 0.0, 0.8125 * iconSize);
        CTX.bezierCurveTo(0.0, 0.8125 * iconSize, 0.0, 0.8125 * iconSize, 0.0625 * iconSize, 0.8125 * iconSize);
        CTX.bezierCurveTo(0.0625 * iconSize, 0.8125 * iconSize, 0.5 * iconSize, 0.8125 * iconSize, 0.5 * iconSize, 0.8125 * iconSize);
        CTX.bezierCurveTo(0.5 * iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize);
        CTX.bezierCurveTo(iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize);
        CTX.bezierCurveTo(iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize);
        CTX.bezierCurveTo(iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize, iconSize, 0.8125 * iconSize);
        CTX.closePath();
        CTX.setFill(COLOR);
        CTX.fill();
        CTX.restore();
    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        drawForeground(TIME);
        drawHours(TIME);
        drawMinutes(TIME);
        drawSeconds(TIME);
    }

    @Override public void updateAlarms() {}


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        double width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        double height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();
        size          = width < height ? width : height;
        center        = size * 0.5;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((clock.getWidth() - size) * 0.5, (clock.getHeight() - size) * 0.5);

            backgroundCanvas.setWidth(size);
            backgroundCanvas.setHeight(size);
            foregroundCanvas.setWidth(size);
            foregroundCanvas.setHeight(size);
            hoursCanvas.setWidth(size);
            hoursCanvas.setHeight(size);
            minutesCanvas.setWidth(size);
            minutesCanvas.setHeight(size);
            secondsCanvas.setWidth(size);
            secondsCanvas.setHeight(size);
            drawBackground();
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        ZonedDateTime time = clock.getTime();

        hourColor       = clock.getHourColor();
        minuteColor     = clock.getMinuteColor();
        fiveMinuteColor = minuteColor.darker();
        secondColor     = clock.getSecondColor();
        titleColor      = clock.getTitleColor();
        textColor       = clock.getTextColor();
        dateColor       = clock.getDateColor();
        alarmColor      = clock.getAlarmColor();

        drawBackground();
        drawForeground(time);
        drawHours(time);
        drawMinutes(time);
        drawSeconds(time);
    }
}
