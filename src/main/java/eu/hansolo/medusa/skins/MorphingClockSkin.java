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
package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.tools.Helper;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class MorphingClockSkin extends ClockSkinBase {
    protected static final double            PREFERRED_WIDTH       = 415;
    protected static final double            PREFERRED_HEIGHT      = 110;
    protected static final double            MINIMUM_WIDTH         = 50;
    protected static final double            MINIMUM_HEIGHT        = 20;
    protected static final double            MAXIMUM_WIDTH         = 4096;
    protected static final double            MAXIMUM_HEIGHT        = 4096;
    private   static final DateTimeFormatter HHMMSS_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm:ss");
    private   static final DateTimeFormatter AMPM_HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss");
    private   static final long              INTERVAL              = 40_000_000l;
    private                double            aspectRatio           = 0.26506024;
    private                double            width;
    private                double            height;
    private                Pane              pane;
    private                Canvas            canvas;
    private                GraphicsContext   ctx;
    private                double            dotSize;
    private                double            spacer;
    private                double            digitSpacer;
    private                double            digitWidth;
    private                double            digitHeight;
    private                Color             hourColor;
    private                Color             hourOffColor;
    private                Color             minuteColor;
    private                Color             minuteOffColor;
    private                Color             secondColor;
    private                Color             secondOffColor;
    private                RadialGradient    hourGradient;
    private                RadialGradient    hourOffGradient;
    private                RadialGradient    minuteGradient;
    private                RadialGradient    minuteOffGradient;
    private                RadialGradient    secondGradient;
    private                RadialGradient    secondOffGradient;
    private                int[][]           hl;
    private                int[][]           hr;
    private                int[][]           ml;
    private                int[][]           mr;
    private                int[][]           sl;
    private                int[][]           sr;
    private                int               oldHourLeft;
    private                int               oldHourRight;
    private                int               oldMinLeft;
    private                int               oldMinRight;
    private                int               oldSecLeft;
    private                int               oldSecRight;
    private                int               hourLeft;
    private                int               hourRight;
    private                int               minLeft;
    private                int               minRight;
    private                int               secLeft;
    private                int               secRight;
    private                int               step;
    private                long              lastTimerCall;
    private                AnimationTimer    timer;


    // ******************** Constructors **************************************
    public MorphingClockSkin(Clock clock) {
        super(clock);
        hourColor      = clock.getHourColor();
        hourOffColor   = Helper.getTranslucentColorFrom(hourColor, 0.15);
        minuteColor    = clock.getMinuteColor();
        minuteOffColor = Helper.getTranslucentColorFrom(minuteColor, 0.15);
        secondColor    = clock.getSecondColor();
        secondOffColor = Helper.getTranslucentColorFrom(secondColor, 0.15);
        hl             = new int[15][8]; // hour left digit
        hr             = new int[15][8]; // hour right digit
        ml             = new int[15][8]; // minute left digit
        mr             = new int[15][8]; // minute right digit
        sl             = new int[15][8]; // second left digit
        sr             = new int[15][8]; // second right digit
        hourLeft       = 0;
        hourRight      = 0;
        minLeft        = 0;
        minRight       = 0;
        secLeft        = 0;
        secRight       = 0;
        step           = 0;
        lastTimerCall  = System.nanoTime();
        timer          = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + INTERVAL) {
                    if (hourLeft  != oldHourLeft)  hl = animateArray(hourLeft, step);
                    if (hourRight != oldHourRight) hr = animateArray(hourRight, step);
                    if (minLeft   != oldMinLeft)   ml = animateArray(minLeft, step);
                    if (minRight  != oldMinRight)  mr = animateArray(minRight, step);
                    if (secLeft   != oldSecLeft)   sl = animateArray(secLeft, step);
                    if (secRight  != oldSecRight)  sr = animateArray(secRight, step);
                    drawTime();
                    step++;
                    if (step > 7) {
                        step = 0;
                        updateArrays();
                        this.stop();
                    }
                    lastTimerCall = now;
                }
            }
        };

        initGraphics();
        registerListeners();

        timer.start();
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
        ctx    = canvas.getGraphicsContext2D();
        ctx.setLineWidth(1);
        ctx.setStroke(null);

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
    private void drawTime() {
        ctx.clearRect(0, 0, width, height);

        // draw hours
        drawMatrix(0, 0, hl, hourGradient, hourOffGradient);
        drawMatrix(digitWidth + digitSpacer, 0, hr, hourGradient, hourOffGradient);

        // draw colon

        // draw minutes
        drawMatrix(2 * digitWidth + 3 * digitSpacer, 0, ml, minuteGradient, minuteOffGradient);
        drawMatrix(3 * digitWidth + 4 * digitSpacer, 0, mr, minuteGradient, minuteOffGradient);

        // draw colon

        // draw seconds
        drawMatrix(4 * digitWidth + 6 * digitSpacer, 0, sl, secondGradient, secondOffGradient);
        drawMatrix(5 * digitWidth + 7 * digitSpacer, 0, sr, secondGradient, secondOffGradient);
    }

    private void drawMatrix(final double X, final double Y, final int[][] MATRIX, final Paint ON_PAINT, final Paint OFF_PAINT) {
        double  x;
        double  y = Y;
        boolean fill;

        for (int row = 0 ; row < 15 ; row++) {
            x = X;
            for (int col = 0; col < 8; col++) {
                fill = MATRIX[row][col] == 1;
                ctx.setFill(fill ? ON_PAINT : OFF_PAINT);
                ctx.fillOval(x, y, dotSize, dotSize);
                x = X + ((col + 1) * (dotSize + spacer));
            }
            y = Y + ((row + 1) * (dotSize + spacer));
        }
    }

    private void updateArrays() {
        hl = updateArray(hourLeft);
        hr = updateArray(hourRight);
        ml = updateArray(minLeft);
        mr = updateArray(minRight);
        sl = updateArray(secLeft);
        sr = updateArray(secRight);
    }
    private int[][] updateArray(final int DIGIT) {
        final int[][] ARRAY;
        switch(DIGIT) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 8:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 9:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
        }
        return ARRAY;
    }

    private int[][] animateArray(final int DIGIT, final int STEP) {
        // Call every 125 ms to get 8 positions in 1000 ms
        switch(DIGIT) {
            case 0: return toZero(STEP);
            case 1: return toOne(STEP);
            case 2: return toTwo(STEP);
            case 3: return toThree(STEP);
            case 4: return toFour(STEP);
            case 5: return toFive(STEP);
            case 6: return toSix(STEP);
            case 7: return toSeven(STEP);
            case 8: return toEight(STEP);
            case 9: return toNine(STEP);
            default:
                return new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
        }
    }

    private int[][] toZero(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toOne(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 0, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 0, 0, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toTwo(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 0, 0, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 0, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toThree(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toFour(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toFive(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 0, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 0, 0, 0, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 0, 0, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 0, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toSix(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toSeven(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 0 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 0 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 0 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 0 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 0 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toEight(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }
    private int[][] toNine(final int STEP) {
        final int[][] ARRAY;
        switch(STEP) {
            case 0:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 1:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 2:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 0, 1, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 3:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 0, 0, 1, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 4:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 0, 0, 0, 1, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 5:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 0, 0, 0, 0, 1, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 6:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 0, 0, 0, 0, 0, 1, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            case 7:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
                break;
            default:
                ARRAY = new int[][] {
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 1, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 0, 0, 0, 0, 0, 0, 1 },
                    { 0, 1, 1, 1, 1, 1, 1, 0 }
                };
        }
        return ARRAY;
    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        String timeString = Locale.US == clock.getLocale() ? AMPM_HHMMSS_FORMATTER.format(TIME) : HHMMSS_FORMATTER.format(TIME);

        oldHourLeft  = hourLeft;
        oldHourRight = hourRight;
        oldMinLeft   = minLeft;
        oldMinRight  = minRight;
        oldSecLeft   = secLeft;
        oldSecRight  = secRight;

        hourLeft  = Integer.parseInt(timeString.substring(0, 1));
        hourRight = Integer.parseInt(timeString.substring(1, 2));
        minLeft   = Integer.parseInt(timeString.substring(3, 4));
        minRight  = Integer.parseInt(timeString.substring(4, 5));
        secLeft   = Integer.parseInt(timeString.substring(6, 7));
        secRight  = Integer.parseInt(timeString.substring(7, 8));

        timer.start();
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

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((clock.getWidth() - height) * 0.5, (clock.getHeight() - height) * 0.5);

            canvas.setWidth(width);
            canvas.setHeight(height);

            dotSize     = height * 0.045455;
            spacer      = height * 0.022727;
            digitWidth  = 8 * dotSize + 7 * spacer;
            digitHeight = 15 * dotSize + 14 * spacer;
            digitSpacer = height * 0.09090909;
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * height))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        hourColor      = clock.getHourColor();
        hourOffColor   = Helper.getTranslucentColorFrom(hourColor, 0.15);
        minuteColor    = clock.getMinuteColor();
        minuteOffColor = Helper.getTranslucentColorFrom(minuteColor, 0.15);
        secondColor    = clock.getSecondColor();
        secondOffColor = Helper.getTranslucentColorFrom(secondColor, 0.15);

        hourGradient = new RadialGradient(0, 0,
                                          0.5, 0.5, 1,
                                          true, CycleMethod.NO_CYCLE,
                                          new Stop(0.00, hourColor),
                                          new Stop(0.25, hourColor),
                                          new Stop(0.43, hourColor.darker()),
                                          new Stop(0.48, hourColor.darker().darker().darker()),
                                          new Stop(0.50, hourColor.darker().darker().darker().darker()));
        hourOffGradient = new RadialGradient(0, 0,
                                             0.5, 0.5, 1,
                                             true, CycleMethod.NO_CYCLE,
                                             new Stop(0.00, hourOffColor),
                                             new Stop(0.25, hourOffColor),
                                             new Stop(0.43, hourOffColor.darker()),
                                             new Stop(0.48, hourOffColor.darker().darker().darker()),
                                             new Stop(0.50, hourOffColor.darker().darker().darker().darker()));
        minuteGradient = new RadialGradient(0, 0,
                                            0.5, 0.5, 1,
                                            true, CycleMethod.NO_CYCLE,
                                            new Stop(0.00, minuteColor),
                                            new Stop(0.25, minuteColor),
                                            new Stop(0.43, minuteColor.darker()),
                                            new Stop(0.48, minuteColor.darker().darker().darker()),
                                            new Stop(0.50, minuteColor.darker().darker().darker().darker()));
        minuteOffGradient = new RadialGradient(0, 0,
                                             0.5, 0.5, 1,
                                             true, CycleMethod.NO_CYCLE,
                                             new Stop(0.00, minuteOffColor),
                                             new Stop(0.25, minuteOffColor),
                                             new Stop(0.43, minuteOffColor.darker()),
                                             new Stop(0.48, minuteOffColor.darker().darker().darker()),
                                             new Stop(0.50, minuteOffColor.darker().darker().darker().darker()));
        secondGradient = new RadialGradient(0, 0,
                                            0.5, 0.5, 1,
                                            true, CycleMethod.NO_CYCLE,
                                            new Stop(0.00, secondColor),
                                            new Stop(0.25, secondColor),
                                            new Stop(0.43, secondColor.darker()),
                                            new Stop(0.48, secondColor.darker().darker().darker()),
                                            new Stop(0.50, secondColor.darker().darker().darker().darker()));
        secondOffGradient = new RadialGradient(0, 0,
                                               0.5, 0.5, 1,
                                               true, CycleMethod.NO_CYCLE,
                                               new Stop(0.00, secondOffColor),
                                               new Stop(0.25, secondOffColor),
                                               new Stop(0.43, secondOffColor.darker()),
                                               new Stop(0.48, secondOffColor.darker().darker().darker()),
                                               new Stop(0.50, secondOffColor.darker().darker().darker().darker()));

        drawTime();
    }
}
