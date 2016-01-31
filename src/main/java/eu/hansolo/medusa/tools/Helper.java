/*
 * Copyright (c) 2015 by Gerrit Grunwald
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

package eu.hansolo.medusa.tools;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TimeSection;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;


/**
 * Created by hansolo on 11.12.15.
 */
public class Helper {

    public static final <T extends Number> T clamp(final T MIN, final T MAX, final T VALUE) {
        if (VALUE.doubleValue() < MIN.doubleValue()) return MIN;
        if (VALUE.doubleValue() > MAX.doubleValue()) return MAX;
        return VALUE;
    }

    public static final double[] calcAutoScale(final double MIN_VALUE, final double MAX_VALUE) {
        double maxNoOfMajorTicks = 10;
        double maxNoOfMinorTicks = 10;
        double niceMinValue;
        double niceMaxValue;
        double niceRange;
        double majorTickSpace;
        double minorTickSpace;
        niceRange      = (calcNiceNumber((MAX_VALUE - MIN_VALUE), false));
        majorTickSpace = calcNiceNumber(niceRange / (maxNoOfMajorTicks - 1), true);
        niceMinValue   = (Math.floor(MIN_VALUE / majorTickSpace) * majorTickSpace);
        niceMaxValue   = (Math.ceil(MAX_VALUE / majorTickSpace) * majorTickSpace);
        minorTickSpace = calcNiceNumber(majorTickSpace / (maxNoOfMinorTicks - 1), true);
        return new double[]{ niceMinValue, niceMaxValue, majorTickSpace, minorTickSpace };
    }

    /**
     * Returns a "niceScaling" number approximately equal to the range.
     * Rounds the number if ROUND == true.
     * Takes the ceiling if ROUND = false.
     *
     * @param RANGE the value range (maxValue - minValue)
     * @param ROUND whether to round the result or ceil
     * @return a "niceScaling" number to be used for the value range
     */
    public static final double calcNiceNumber(final double RANGE, final boolean ROUND) {
        double niceFraction;
        double exponent = Math.floor(Math.log10(RANGE));   // exponent of range
        double fraction = RANGE / Math.pow(10, exponent);  // fractional part of range

        if (ROUND) {
            if (Double.compare(fraction, 1.5) < 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 3)  < 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 7) < 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        } else {
            if (Double.compare(fraction, 1) <= 0) {
                niceFraction = 1;
            } else if (Double.compare(fraction, 2) <= 0) {
                niceFraction = 2;
            } else if (Double.compare(fraction, 5) <= 0) {
                niceFraction = 5;
            } else {
                niceFraction = 10;
            }
        }
        return niceFraction * Math.pow(10, exponent);
    }

    public static final Color getColorOfSection(final List<Section> SECTIONS, final double VALUE, final Color DEFAULT_COLOR) {
        for (Section section : SECTIONS) {
            if (section.contains(VALUE)) return section.getColor();
        }
        return DEFAULT_COLOR;
    }

    public static final void rotateContextForText(final GraphicsContext CTX, final double START_ANGLE, final double ANGLE, final TickLabelOrientation ORIENTATION) {
        switch (ORIENTATION) {
            case ORTHOGONAL:
                if ((360 - START_ANGLE - ANGLE) % 360 > 90 && (360 - START_ANGLE - ANGLE) % 360 < 270) {
                    CTX.rotate((180 - START_ANGLE - ANGLE) % 360);
                } else {
                    CTX.rotate((360 - START_ANGLE - ANGLE) % 360);
                }
                break;
            case TANGENT:
                if ((360 - START_ANGLE - ANGLE - 90) % 360 > 90 && (360 - START_ANGLE - ANGLE - 90) % 360 < 270) {
                    CTX.rotate((90 - START_ANGLE - ANGLE) % 360);
                } else {
                    CTX.rotate((270 - START_ANGLE - ANGLE) % 360);
                }
                break;
            case HORIZONTAL:
            default:
                break;
        }
    }

    public static final void adjustTextSize(final Text TEXT, final double MAX_WIDTH, double fontSize) {
        final String FONT_NAME = TEXT.getFont().getName();
        while (TEXT.getLayoutBounds().getWidth() > MAX_WIDTH && fontSize > 0) {
            fontSize -= 0.005;
            TEXT.setFont(new Font(FONT_NAME, fontSize));
        }
    }

    public static final String colorToCss(final Color COLOR) {
        return COLOR.toString().replace("0x", "#");
    }

    public static final ThreadFactory getThreadFactory(final String THREAD_NAME, final boolean IS_DAEMON) {
        return runnable -> {
            Thread thread = new Thread(runnable, THREAD_NAME);
            thread.setDaemon(IS_DAEMON);
            return thread;
        };
    }

    public static final void stopTask(ScheduledFuture<?> task) {
        if (null == task) return;
        task.cancel(true);
        task = null;
    }

    public static final ImagePattern createCarbonPattern() {
        final double          SIZE   = 12;
        final Canvas          CANVAS = new Canvas(SIZE, SIZE);
        final GraphicsContext CTX    = CANVAS.getGraphicsContext2D();

        CTX.setFill(new LinearGradient(0, 0, 0, 0.5 * SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(35, 35, 35)),
                                       new Stop(1, Color.rgb(23, 23, 23))));
        CTX.fillRect(0, 0, SIZE * 0.5, SIZE * 0.5);

        CTX.setFill(new LinearGradient(0, 0, 0, 0.416666 * SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(38, 38, 38)),
                                       new Stop(1, Color.rgb(30, 30, 30))));
        CTX.fillRect(SIZE * 0.083333, 0, SIZE * 0.333333, SIZE * 0.416666);

        CTX.setFill(new LinearGradient(0, 0.5 * SIZE, 0, SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(35, 35, 35)),
                                       new Stop(1, Color.rgb(23, 23, 23))));
        CTX.fillRect(SIZE * 0.5, SIZE * 0.5, SIZE * 0.5, SIZE * 0.5);

        CTX.setFill(new LinearGradient(0, 0.5 * SIZE, 0, 0.916666 * SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(38, 38, 38)),
                                       new Stop(1, Color.rgb(30, 30, 30))));
        CTX.fillRect(SIZE * 0.583333, SIZE * 0.5, SIZE * 0.333333, SIZE * 0.416666);

        CTX.setFill(new LinearGradient(0, 0, 0, 0.5 * SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(48, 48, 48)),
                                       new Stop(1, Color.rgb(40, 40, 40))));
        CTX.fillRect(SIZE * 0.5, 0, SIZE * 0.5, SIZE * 0.5);

        CTX.setFill(new LinearGradient(0, 0.083333 * SIZE, 0, 0.5 * SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(53, 53, 53)),
                                       new Stop(1, Color.rgb(45, 45, 45))));
        CTX.fillRect(SIZE * 0.583333, SIZE * 0.083333, SIZE * 0.333333, SIZE * 0.416666);

        CTX.setFill(new LinearGradient(0, 0.5 * SIZE, 0, SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(48, 48, 48)),
                                       new Stop(1, Color.rgb(40, 40, 40))));
        CTX.fillRect(0, SIZE * 0.5, SIZE * 0.5, SIZE * 0.5);

        CTX.setFill(new LinearGradient(0, 0.583333 * SIZE, 0, SIZE,
                                       false, CycleMethod.NO_CYCLE,
                                       new Stop(0, Color.rgb(53, 53, 53)),
                                       new Stop(1, Color.rgb(45, 45, 45))));
        CTX.fillRect(SIZE * 0.083333, SIZE * 0.583333, SIZE * 0.333333, SIZE * 0.416666);

        final Image        PATTERN_IMAGE = CANVAS.snapshot(new SnapshotParameters(), null);
        final ImagePattern PATTERN       = new ImagePattern(PATTERN_IMAGE, 0, 0, SIZE, SIZE, false);

        return PATTERN;
    }

    public static void drawTriangle(final GraphicsContext CTX,
                              final double PI1X, final double PI1Y, final double PI2X, final double PI2Y,
                              final double PO1X, final double PO1Y, final double PO2X, final double PO2Y) {
        CTX.beginPath();
        CTX.moveTo(PI2X, PI2Y);
        CTX.lineTo(PI1X, PI1Y);
        CTX.lineTo(PO1X, PO1Y);
        CTX.lineTo(PO2X, PO2Y);
        CTX.closePath();
        CTX.fill();
    }
    public static void drawDot(final GraphicsContext CTX, final double CENTER_X, final double CENTER_Y, final double SIZE) {
        CTX.fillOval(CENTER_X, CENTER_Y, SIZE, SIZE);
    }
    public static void drawLine(final GraphicsContext CTX, final double P1X, final double P1Y, final double P2X, final double P2Y) {
        CTX.strokeLine(P1X, P1Y, P2X, P2Y);
    }

    public static boolean isMonochrome(final Color COLOR) {
        return Double.compare(COLOR.getRed(), COLOR.getGreen()) == 0 && Double.compare(COLOR.getGreen(), COLOR.getBlue()) == 0;
    }

    public static double colorDistance(final Color COLOR_1, final Color COLOR_2) {
        final double DELTA_R = (COLOR_2.getRed() - COLOR_1.getRed());
        final double DELTA_G = (COLOR_2.getGreen() - COLOR_1.getGreen());
        final double DELTA_B = (COLOR_2.getBlue() - COLOR_1.getBlue());

        return Math.sqrt(DELTA_R * DELTA_R + DELTA_G * DELTA_G + DELTA_B * DELTA_B);
    }

    public static boolean isBright(final Color COLOR) { return !isDark(COLOR); }
    public static boolean isDark(final Color COLOR) {
        final double DISTANCE_TO_WHITE = colorDistance(COLOR, Color.WHITE);
        final double DISTANCE_TO_BLACK = colorDistance(COLOR, Color.BLACK);
        return DISTANCE_TO_BLACK < DISTANCE_TO_WHITE;
    }

    public static void drawRadialTickMarks(final Gauge GAUGE, final GraphicsContext CTX, final double MIN_VALUE, final double MAX_VALUE,
                                           final double START_ANGLE, final double ANGLE_RANGE, final double ANGLE_STEP, final double CENTER_X, final double CENTER_Y, final double SIZE) {
        CTX.setLineCap(StrokeLineCap.BUTT);
        double               sinValue;
        double               cosValue;
        double               centerX               = CENTER_X;
        double               centerY               = CENTER_Y;
        int                  tickLabelDecimals     = GAUGE.getTickLabelDecimals();
        String               tickLabelFormatString = "%." + tickLabelDecimals + "f";
        double               minorTickSpace        = GAUGE.getMinorTickSpace();
        double               tmpAngleStep          = ANGLE_STEP * minorTickSpace;
        TickLabelOrientation tickLabelOrientation  = GAUGE.getTickLabelOrientation();
        TickLabelLocation    tickLabelLocation     = GAUGE.getTickLabelLocation();
        BigDecimal           minorTickSpaceBD      = BigDecimal.valueOf(minorTickSpace);
        BigDecimal           majorTickSpaceBD      = BigDecimal.valueOf(GAUGE.getMajorTickSpace());
        BigDecimal           mediumCheck2          = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal           mediumCheck5          = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal           counterBD             = BigDecimal.valueOf(MIN_VALUE);
        double               counter               = MIN_VALUE;

        List<Section> tickMarkSections             = GAUGE.getTickMarkSections();
        List<Section> tickLabelSections            = GAUGE.getTickLabelSections();
        Color         tickMarkColor                = GAUGE.getTickMarkColor();
        Color         majorTickMarkColor           = GAUGE.getMajorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : GAUGE.getMajorTickMarkColor();
        Color         mediumTickMarkColor          = GAUGE.getMediumTickMarkColor().equals(tickMarkColor) ? tickMarkColor : GAUGE.getMediumTickMarkColor();
        Color         minorTickMarkColor           = GAUGE.getMinorTickMarkColor().equals(tickMarkColor) ? tickMarkColor : GAUGE.getMinorTickMarkColor();
        Color         tickLabelColor               = GAUGE.getTickLabelColor();
        Color         zeroColor                    = GAUGE.getZeroColor();
        boolean       isNotZero                    = true;
        TickMarkType  majorTickMarkType            = GAUGE.getMajorTickMarkType();
        TickMarkType  mediumTickMarkType           = GAUGE.getMediumTickMarkType();
        TickMarkType  minorTickMarkType            = GAUGE.getMinorTickMarkType();
        boolean       tickMarkSectionsVisible      = GAUGE.getTickMarkSectionsVisible();
        boolean       tickLabelSectionsVisible     = GAUGE.getTickLabelSectionsVisible();
        boolean       majorTickMarksVisible        = GAUGE.getMajorTickMarksVisible();
        boolean       mediumTickMarksVisible       = GAUGE.getMediumTickMarksVisible();
        boolean       minorTickMarksVisible        = GAUGE.getMinorTickMarksVisible();
        boolean       tickLabelsVisible            = GAUGE.getTickLabelsVisible();
        boolean       onlyFirstAndLastLabelVisible = GAUGE.isOnlyFirstAndLastTickLabelVisible();
        boolean       customTickLabelsEnabled      = GAUGE.getCustomTickLabelsEnabled();
        List<String>  customTickLabels             = customTickLabelsEnabled ? GAUGE.getCustomTickLabels() : null;
        double        textDisplacementFactor       = majorTickMarkType == TickMarkType.DOT ? (TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.95 : 1.05) : 1.0;
        double        majorDotSize;
        double        majorHalfDotSize;
        double        mediumDotSize;
        double        mediumHalfDotSize;
        double        minorDotSize;
        double        minorHalfDotSize;

        double orthTextFactor;
        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
            orthTextFactor    = TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.45 * textDisplacementFactor : 0.45 * textDisplacementFactor;
            majorDotSize      = 0.02 * SIZE;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01375 * SIZE;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0075 * SIZE;
            minorHalfDotSize  = minorDotSize * 0.5;
        } else {
            orthTextFactor    = TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.38 * textDisplacementFactor : 0.37 * textDisplacementFactor;
            majorDotSize      = 0.025 * SIZE;
            majorHalfDotSize  = majorDotSize * 0.5;
            mediumDotSize     = 0.01875 * SIZE;
            mediumHalfDotSize = mediumDotSize * 0.5;
            minorDotSize      = 0.0125 * SIZE;
            minorHalfDotSize  = minorDotSize * 0.5;
        };

        boolean fullRange                  = (MIN_VALUE < 0 && MAX_VALUE > 0);
        double  tickLabelFontSize          = tickLabelDecimals == 0 ? 0.054 * SIZE : 0.051 * SIZE;
        double  tickMarkFontSize           = tickLabelDecimals == 0 ? 0.047 * SIZE: 0.044 * SIZE;
        double  tickLabelOrientationFactor = TickLabelOrientation.HORIZONTAL == tickLabelOrientation ? 0.9 : 1.0;

        Font tickLabelFont     = Fonts.robotoCondensedRegular(tickLabelFontSize * tickLabelOrientationFactor);
        Font tickMarkFont      = Fonts.robotoCondensedRegular(tickMarkFontSize * tickLabelOrientationFactor);
        Font tickLabelZeroFont = fullRange ? Fonts.robotoCondensedBold(tickLabelFontSize * tickLabelOrientationFactor) : tickLabelFont;
        Font tickMarkZeroFont  = fullRange ? Fonts.robotoCondensedBold(tickMarkFontSize * tickLabelOrientationFactor) : tickMarkFont;

        // Variables needed for tickmarks
        double innerPointX;
        double innerPointY;
        double innerMediumPointX;
        double innerMediumPointY;
        double innerMinorPointX;
        double innerMinorPointY;
        double outerPointX;
        double outerPointY;
        double outerMediumPointX;
        double outerMediumPointY;
        double outerMinorPointX;
        double outerMinorPointY;
        double textPointX;
        double textPointY;
        double dotCenterX;
        double dotCenterY;
        double dotMediumCenterX;
        double dotMediumCenterY;
        double dotMinorCenterX;
        double dotMinorCenterY;
        double tickLabelTickMarkX;
        double tickLabelTickMarkY;

        double triangleMajorInnerAngle1;
        double triangleMajorInnerAngle2;
        double triangleMajorOuterAngle1;
        double triangleMajorOuterAngle2;
        double triangleMajorInnerPoint1X;
        double triangleMajorInnerPoint1Y;
        double triangleMajorInnerPoint2X;
        double triangleMajorInnerPoint2Y;
        double triangleMajorOuterPoint1X;
        double triangleMajorOuterPoint1Y;
        double triangleMajorOuterPoint2X;
        double triangleMajorOuterPoint2Y;

        double triangleMediumInnerAngle1;
        double triangleMediumInnerAngle2;
        double triangleMediumOuterAngle1;
        double triangleMediumOuterAngle2;
        double triangleMediumInnerPoint1X;
        double triangleMediumInnerPoint1Y;
        double triangleMediumInnerPoint2X;
        double triangleMediumInnerPoint2Y;
        double triangleMediumOuterPoint1X;
        double triangleMediumOuterPoint1Y;
        double triangleMediumOuterPoint2X;
        double triangleMediumOuterPoint2Y;

        double triangleMinorInnerAngle1;
        double triangleMinorInnerAngle2;
        double triangleMinorOuterAngle1;
        double triangleMinorOuterAngle2;
        double triangleMinorInnerPoint1X;
        double triangleMinorInnerPoint1Y;
        double triangleMinorInnerPoint2X;
        double triangleMinorInnerPoint2Y;
        double triangleMinorOuterPoint1X;
        double triangleMinorOuterPoint1Y;
        double triangleMinorOuterPoint2X;
        double triangleMinorOuterPoint2Y;

        // Main loop
        ScaleDirection scaleDirection = GAUGE.getScaleDirection();
        BigDecimal     tmpStepBD      = new BigDecimal(tmpAngleStep);
        tmpStepBD                     = tmpStepBD.setScale(3, BigDecimal.ROUND_HALF_UP);
        double tmpStep                = tmpStepBD.doubleValue();
        double angle                  = 0;
        int    customTickLabelCounter = 0;
        for (double i = 0 ; Double.compare(-ANGLE_RANGE - tmpStep, i) <= 0 ; i -= tmpStep) {
            sinValue = Math.sin(Math.toRadians(angle + START_ANGLE));
            cosValue = Math.cos(Math.toRadians(angle + START_ANGLE));

            switch(tickLabelLocation) {
                case OUTSIDE:
                    innerPointX                = centerX + SIZE * 0.3585 * sinValue;
                    innerPointY                = centerY + SIZE * 0.3585 * cosValue;
                    innerMediumPointX          = centerX + SIZE * 0.3585 * sinValue;
                    innerMediumPointY          = centerY + SIZE * 0.3585 * cosValue;
                    innerMinorPointX           = centerX + SIZE * 0.3585 * sinValue;
                    innerMinorPointY           = centerY + SIZE * 0.3585 * cosValue;
                    outerPointX                = centerX + SIZE * 0.4105 * sinValue;
                    outerPointY                = centerY + SIZE * 0.4105 * cosValue;
                    outerMediumPointX          = centerX + SIZE * 0.4045 * sinValue;
                    outerMediumPointY          = centerY + SIZE * 0.4045 * cosValue;
                    outerMinorPointX           = centerX + SIZE * 0.3975 * sinValue;
                    outerMinorPointY           = centerY + SIZE * 0.3975 * cosValue;
                    textPointX                 = centerX + SIZE * orthTextFactor * sinValue;
                    textPointY                 = centerY + SIZE * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + SIZE * 0.3685 * sinValue;
                    dotCenterY                 = centerY + SIZE * 0.3685 * cosValue;
                    dotMediumCenterX           = centerX + SIZE * 0.365375 * sinValue;
                    dotMediumCenterY           = centerY + SIZE * 0.365375 * cosValue;
                    dotMinorCenterX            = centerX + SIZE * 0.36225 * sinValue;
                    dotMinorCenterY            = centerY + SIZE * 0.36225 * cosValue;
                    tickLabelTickMarkX         = centerX + SIZE * 0.3805 * sinValue;
                    tickLabelTickMarkY         = centerY + SIZE * 0.3805 * cosValue;

                    triangleMajorInnerAngle1   = Math.toRadians(angle - 1.2 + START_ANGLE);
                    triangleMajorInnerAngle2   = Math.toRadians(angle + 1.2 + START_ANGLE);
                    triangleMajorOuterAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    triangleMajorOuterAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    triangleMajorInnerPoint1X  = centerX + SIZE * 0.3585 * Math.sin(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint1Y  = centerY + SIZE * 0.3585 * Math.cos(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint2X  = centerX + SIZE * 0.3585 * Math.sin(triangleMajorInnerAngle2);
                    triangleMajorInnerPoint2Y  = centerY + SIZE * 0.3585 * Math.cos(triangleMajorInnerAngle2);
                    triangleMajorOuterPoint1X  = centerX + SIZE * 0.4105 * Math.sin(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint1Y  = centerY + SIZE * 0.4105 * Math.cos(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint2X  = centerX + SIZE * 0.4105 * Math.sin(triangleMajorOuterAngle2);
                    triangleMajorOuterPoint2Y  = centerY + SIZE * 0.4105 * Math.cos(triangleMajorOuterAngle2);

                    triangleMediumInnerAngle1  = Math.toRadians(angle - 1.0 + START_ANGLE);
                    triangleMediumInnerAngle2  = Math.toRadians(angle + 1.0 + START_ANGLE);
                    triangleMediumOuterAngle1  = Math.toRadians(angle - 0.7 + START_ANGLE);
                    triangleMediumOuterAngle2  = Math.toRadians(angle + 0.7 + START_ANGLE);
                    triangleMediumInnerPoint1X = centerX + SIZE * 0.3585 * Math.sin(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint1Y = centerY + SIZE * 0.3585 * Math.cos(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint2X = centerX + SIZE * 0.3585 * Math.sin(triangleMediumInnerAngle2);
                    triangleMediumInnerPoint2Y = centerY + SIZE * 0.3585 * Math.cos(triangleMediumInnerAngle2);
                    triangleMediumOuterPoint1X = centerX + SIZE * 0.3985 * Math.sin(triangleMajorOuterAngle1);
                    triangleMediumOuterPoint1Y = centerY + SIZE * 0.3985 * Math.cos(triangleMediumOuterAngle1);
                    triangleMediumOuterPoint2X = centerX + SIZE * 0.3985 * Math.sin(triangleMediumOuterAngle2);
                    triangleMediumOuterPoint2Y = centerY + SIZE * 0.3985 * Math.cos(triangleMediumOuterAngle2);

                    triangleMinorInnerAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    triangleMinorInnerAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    triangleMinorOuterAngle1   = Math.toRadians(angle - 0.6 + START_ANGLE);
                    triangleMinorOuterAngle2   = Math.toRadians(angle + 0.6 + START_ANGLE);
                    triangleMinorInnerPoint1X  = centerX + SIZE * 0.3585 * Math.sin(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint1Y  = centerY + SIZE * 0.3585 * Math.cos(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint2X  = centerX + SIZE * 0.3585 * Math.sin(triangleMinorInnerAngle2);
                    triangleMinorInnerPoint2Y  = centerY + SIZE * 0.3585 * Math.cos(triangleMinorInnerAngle2);
                    triangleMinorOuterPoint1X  = centerX + SIZE * 0.3975 * Math.sin(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint1Y  = centerY + SIZE * 0.3975 * Math.cos(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint2X  = centerX + SIZE * 0.3975 * Math.sin(triangleMinorOuterAngle2);
                    triangleMinorOuterPoint2Y  = centerY + SIZE * 0.3975 * Math.cos(triangleMinorOuterAngle2);
                    break;
                case INSIDE:
                default:
                    innerPointX                = centerX + SIZE * 0.423 * sinValue;
                    innerPointY                = centerY + SIZE * 0.423 * cosValue;
                    innerMediumPointX          = centerX + SIZE * 0.43 * sinValue;
                    innerMediumPointY          = centerY + SIZE * 0.43 * cosValue;
                    innerMinorPointX           = centerX + SIZE * 0.436 * sinValue;
                    innerMinorPointY           = centerY + SIZE * 0.436 * cosValue;
                    outerPointX                = centerX + SIZE * 0.475 * sinValue;
                    outerPointY                = centerY + SIZE * 0.475 * cosValue;
                    outerMediumPointX          = centerX + SIZE * 0.475 * sinValue;
                    outerMediumPointY          = centerY + SIZE * 0.475 * cosValue;
                    outerMinorPointX           = centerX + SIZE * 0.475 * sinValue;
                    outerMinorPointY           = centerY + SIZE * 0.475 * cosValue;
                    textPointX                 = centerX + SIZE * orthTextFactor * sinValue;
                    textPointY                 = centerY + SIZE * orthTextFactor * cosValue;
                    dotCenterX                 = centerX + SIZE * 0.4625 * sinValue;
                    dotCenterY                 = centerY + SIZE * 0.4625 * cosValue;
                    dotMediumCenterX           = centerX + SIZE * 0.465625 * sinValue;
                    dotMediumCenterY           = centerY + SIZE * 0.465625 * cosValue;
                    dotMinorCenterX            = centerX + SIZE * 0.46875 * sinValue;
                    dotMinorCenterY            = centerY + SIZE * 0.46875 * cosValue;
                    tickLabelTickMarkX         = centerX + SIZE * 0.445 * sinValue;
                    tickLabelTickMarkY         = centerY + SIZE * 0.445 * cosValue;

                    triangleMajorInnerAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    triangleMajorInnerAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    triangleMajorOuterAngle1   = Math.toRadians(angle - 1.2 + START_ANGLE);
                    triangleMajorOuterAngle2   = Math.toRadians(angle + 1.2 + START_ANGLE);
                    triangleMajorInnerPoint1X  = centerX + SIZE * 0.423 * Math.sin(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint1Y  = centerY + SIZE * 0.423 * Math.cos(triangleMajorInnerAngle1);
                    triangleMajorInnerPoint2X  = centerX + SIZE * 0.423 * Math.sin(triangleMajorInnerAngle2);
                    triangleMajorInnerPoint2Y  = centerY + SIZE * 0.423 * Math.cos(triangleMajorInnerAngle2);
                    triangleMajorOuterPoint1X  = centerX + SIZE * 0.475 * Math.sin(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint1Y  = centerY + SIZE * 0.475 * Math.cos(triangleMajorOuterAngle1);
                    triangleMajorOuterPoint2X  = centerX + SIZE * 0.475 * Math.sin(triangleMajorOuterAngle2);
                    triangleMajorOuterPoint2Y  = centerY + SIZE * 0.475 * Math.cos(triangleMajorOuterAngle2);

                    triangleMediumInnerAngle1  = Math.toRadians(angle - 0.7 + START_ANGLE);
                    triangleMediumInnerAngle2  = Math.toRadians(angle + 0.7 + START_ANGLE);
                    triangleMediumOuterAngle1  = Math.toRadians(angle - 1.0 + START_ANGLE);
                    triangleMediumOuterAngle2  = Math.toRadians(angle + 1.0 + START_ANGLE);
                    triangleMediumInnerPoint1X = centerX + SIZE * 0.435 * Math.sin(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint1Y = centerY + SIZE * 0.435 * Math.cos(triangleMediumInnerAngle1);
                    triangleMediumInnerPoint2X = centerX + SIZE * 0.435 * Math.sin(triangleMediumInnerAngle2);
                    triangleMediumInnerPoint2Y = centerY + SIZE * 0.435 * Math.cos(triangleMediumInnerAngle2);
                    triangleMediumOuterPoint1X = centerX + SIZE * 0.475 * Math.sin(triangleMajorOuterAngle1);
                    triangleMediumOuterPoint1Y = centerY + SIZE * 0.475 * Math.cos(triangleMediumOuterAngle1);
                    triangleMediumOuterPoint2X = centerX + SIZE * 0.475 * Math.sin(triangleMediumOuterAngle2);
                    triangleMediumOuterPoint2Y = centerY + SIZE * 0.475 * Math.cos(triangleMediumOuterAngle2);

                    triangleMinorInnerAngle1   = Math.toRadians(angle - 0.6 + START_ANGLE);
                    triangleMinorInnerAngle2   = Math.toRadians(angle + 0.6 + START_ANGLE);
                    triangleMinorOuterAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    triangleMinorOuterAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    triangleMinorInnerPoint1X  = centerX + SIZE * 0.440 * Math.sin(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint1Y  = centerY + SIZE * 0.440 * Math.cos(triangleMinorInnerAngle1);
                    triangleMinorInnerPoint2X  = centerX + SIZE * 0.440 * Math.sin(triangleMinorInnerAngle2);
                    triangleMinorInnerPoint2Y  = centerY + SIZE * 0.440 * Math.cos(triangleMinorInnerAngle2);
                    triangleMinorOuterPoint1X  = centerX + SIZE * 0.475 * Math.sin(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint1Y  = centerY + SIZE * 0.475 * Math.cos(triangleMinorOuterAngle1);
                    triangleMinorOuterPoint2X  = centerX + SIZE * 0.475 * Math.sin(triangleMinorOuterAngle2);
                    triangleMinorOuterPoint2Y  = centerY + SIZE * 0.475 * Math.cos(triangleMinorOuterAngle2);
                    break;
            }

            // Set the general tickmark color
            CTX.setStroke(tickMarkColor);
            CTX.setFill(tickMarkColor);

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0d) == 0) {
                // Draw major tick mark
                isNotZero = Double.compare(0d, counter) != 0;
                TickMarkType tickMarkType = null;
                if (majorTickMarksVisible) {
                    tickMarkType = majorTickMarkType;
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    CTX.setLineWidth(SIZE * (TickMarkType.BOX == tickMarkType ? 0.016 : 0.0055));
                } else if (minorTickMarksVisible) {
                    tickMarkType = minorTickMarkType;
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    CTX.setLineWidth(SIZE * (TickMarkType.BOX == tickMarkType ? 0.007 : 0.00225));
                }
                if (fullRange && !isNotZero) {
                    CTX.setFill(zeroColor);
                    CTX.setStroke(zeroColor);
                }
                if (null != tickMarkType) {
                    switch (tickMarkType) {
                        case TRIANGLE:
                            if (majorTickMarksVisible) {
                                Helper.drawTriangle(CTX, triangleMajorInnerPoint1X, triangleMajorInnerPoint1Y, triangleMajorInnerPoint2X, triangleMajorInnerPoint2Y,
                                                    triangleMajorOuterPoint1X, triangleMajorOuterPoint1Y, triangleMajorOuterPoint2X, triangleMajorOuterPoint2Y);
                            } else if (minorTickMarksVisible) {
                                Helper.drawTriangle(CTX, triangleMinorInnerPoint1X, triangleMinorInnerPoint1Y, triangleMinorInnerPoint2X, triangleMinorInnerPoint2Y,
                                                    triangleMinorOuterPoint1X, triangleMinorOuterPoint1Y, triangleMinorOuterPoint2X, triangleMinorOuterPoint2Y);
                            }
                            break;
                        case DOT:
                            if (majorTickMarksVisible) {
                                Helper.drawDot(CTX, dotCenterX - majorHalfDotSize, dotCenterY - majorHalfDotSize, majorDotSize);
                            } else if (minorTickMarksVisible) {
                                Helper.drawDot(CTX, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                            }
                            break;
                        case TICK_LABEL:
                            if (majorTickMarksVisible) {
                                CTX.save();
                                CTX.translate(tickLabelTickMarkX, tickLabelTickMarkY);

                                Helper.rotateContextForText(CTX, START_ANGLE, angle, tickLabelOrientation);

                                CTX.setFont(isNotZero ? tickMarkFont : tickMarkZeroFont);
                                CTX.setTextAlign(TextAlignment.CENTER);
                                CTX.setTextBaseline(VPos.CENTER);
                                CTX.fillText(String.format(Locale.US, tickLabelFormatString, counter), 0, 0);
                                CTX.restore();
                            }
                            break;
                        case LINE:
                        default:
                            if (majorTickMarksVisible) {
                                Helper.drawLine(CTX, innerPointX, innerPointY, outerPointX, outerPointY);
                            } else if (minorTickMarksVisible) {
                                if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                    Helper.drawLine(CTX, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                                } else {
                                    Helper.drawLine(CTX, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                                }
                            }
                            break;
                    }
                }

                // Draw tick label text
                if (tickLabelsVisible) {
                    CTX.save();
                    CTX.translate(textPointX, textPointY);

                    Helper.rotateContextForText(CTX, START_ANGLE, angle, tickLabelOrientation);
                    CTX.setFont(isNotZero ? tickLabelFont : tickLabelZeroFont);
                    CTX.setTextAlign(TextAlignment.CENTER);
                    CTX.setTextBaseline(VPos.CENTER);

                    if (!onlyFirstAndLastLabelVisible) {
                        if (isNotZero) {
                            CTX.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                        } else {
                            CTX.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : fullRange ? zeroColor : tickLabelColor);
                        }
                    } else {
                        if ((Double.compare(counter, MIN_VALUE) == 0 || Double.compare(counter, MAX_VALUE) == 0)) {
                            if (isNotZero) {
                                CTX.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                            } else {
                                CTX.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : fullRange ? zeroColor : tickLabelColor);
                            }
                        } else {
                            CTX.setFill(Color.TRANSPARENT);
                        }
                    }

                    if (customTickLabelsEnabled) {
                        if (customTickLabelCounter >= 0) {
                            CTX.fillText(customTickLabels.get(customTickLabelCounter), 0, 0);
                            customTickLabelCounter++;
                        }
                        if (customTickLabelCounter > customTickLabels.size() - 1) customTickLabelCounter = -1;
                    } else {
                        CTX.fillText(String.format(Locale.US, tickLabelFormatString, counter), 0, 0);
                    }
                    CTX.restore();
                }
            } else if (mediumTickMarksVisible &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0d) != 0d &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0d) == 0d) {
                // Draw medium tick mark
                CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                switch(mediumTickMarkType) {
                    case TRIANGLE:
                        Helper.drawTriangle(CTX, triangleMediumInnerPoint1X, triangleMediumInnerPoint1Y, triangleMediumInnerPoint2X, triangleMediumInnerPoint2Y,
                                            triangleMediumOuterPoint1X, triangleMediumOuterPoint1Y, triangleMediumOuterPoint2X, triangleMediumOuterPoint2Y);
                        break;
                    case DOT:
                        Helper.drawDot(CTX, dotMediumCenterX - mediumHalfDotSize, dotMediumCenterY - mediumHalfDotSize, mediumDotSize);
                        break;
                    case BOX:
                        CTX.setLineWidth(SIZE * 0.009);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(CTX, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(CTX, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                    case LINE:
                    default:
                        CTX.setLineWidth(SIZE * 0.0035);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(CTX, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(CTX, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                }
            } else if (minorTickMarksVisible && Double.compare(counterBD.remainder(minorTickSpaceBD).doubleValue(), 0d) == 0) {
                // Draw minor tick mark
                if (TickMarkType.TICK_LABEL != majorTickMarkType) {
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    switch (minorTickMarkType) {
                        case TRIANGLE:
                            Helper.drawTriangle(CTX, triangleMinorInnerPoint1X, triangleMinorInnerPoint1Y, triangleMinorInnerPoint2X, triangleMinorInnerPoint2Y,
                                                triangleMinorOuterPoint1X, triangleMinorOuterPoint1Y, triangleMinorOuterPoint2X, triangleMinorOuterPoint2Y);
                            break;
                        case DOT:
                            Helper.drawDot(CTX, dotMinorCenterX - minorHalfDotSize, dotMinorCenterY - minorHalfDotSize, minorDotSize);
                            break;
                        case BOX:
                            CTX.setLineWidth(SIZE * 0.007);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(CTX, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(CTX, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                        case LINE:
                        default:
                            CTX.setLineWidth(SIZE * 0.00225);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(CTX, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(CTX, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                    }
                }
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
            if (counter > MAX_VALUE) break;
            angle     = ScaleDirection.CLOCKWISE == scaleDirection ? (angle - tmpAngleStep) : (angle + tmpAngleStep);
        }
    }

    public static Image createNoiseImage(final double WIDTH, final double HEIGHT, final Color DARK_COLOR, final Color BRIGHT_COLOR, final double ALPHA_VARIATION_IN_PERCENT) {
        if (Double.compare(WIDTH, 0) <= 0 || Double.compare(HEIGHT, 0) <= 0) return null;
        int                 width                   = (int) WIDTH;
        int                 height                  = (int) HEIGHT;
        double              alphaVariationInPercent = Helper.clamp(0d, 100d, ALPHA_VARIATION_IN_PERCENT);
        final WritableImage IMAGE                   = new WritableImage(width, height);
        final PixelWriter   PIXEL_WRITER            = IMAGE.getPixelWriter();
        final Random        BW_RND                  = new Random();
        final Random        ALPHA_RND               = new Random();
        final double        ALPHA_START             = alphaVariationInPercent / 100 / 2;
        final double        ALPHA_VARIATION         = alphaVariationInPercent / 100;
        for (int y = 0 ; y < height ; y++) {
            for (int x = 0 ; x < width ; x++) {
                final Color  NOISE_COLOR = BW_RND.nextBoolean() == true ? BRIGHT_COLOR : DARK_COLOR;
                final double NOISE_ALPHA = Helper.clamp(0d, 1d, ALPHA_START + ALPHA_RND.nextDouble() * ALPHA_VARIATION);
                PIXEL_WRITER.setColor(x, y, Color.color(NOISE_COLOR.getRed(), NOISE_COLOR.getGreen(), NOISE_COLOR.getBlue(), NOISE_ALPHA));
            }
        }
        return IMAGE;
    }

    public static void drawTimeSections(final Clock CLOCK, final GraphicsContext CTX, final List<TimeSection> SECTIONS, final double SIZE,
                                        final double XY_INSIDE, final double XY_OUTSIDE, final double WH_INSIDE, final double WH_OUTSIDE,
                                        final double LINE_WIDTH) {
        if (SECTIONS.isEmpty()) return;
        TickLabelLocation tickLabelLocation = CLOCK.getTickLabelLocation();
        double            xy                = TickLabelLocation.INSIDE == tickLabelLocation ? XY_INSIDE * SIZE : XY_OUTSIDE * SIZE;
        double            wh                = TickLabelLocation.INSIDE == tickLabelLocation ? WH_INSIDE * SIZE : WH_OUTSIDE * SIZE;
        double            offset            = 90;
        int               listSize          = SECTIONS.size();
        double            angleStep         = 360d / 60d;
        for (int i = 0 ; i < listSize ; i++) {
            TimeSection section           = SECTIONS.get(i);
            LocalTime   start             = section.getStart();
            LocalTime   stop              = section.getStop();
            double      sectionStartAngle = (start.getHour() % 12 * 5d + start.getMinute() / 12d + start.getSecond() / 300d) * angleStep + 180;
            double      sectionAngleExtend;
            sectionAngleExtend = ((stop.getHour() - start.getHour()) % 12 * 5d + (stop.getMinute() - start.getMinute()) / 12d + (stop.getSecond() - start.getSecond()) / 300d) * angleStep;
            CTX.save();
            CTX.setStroke(section.getColor());
            CTX.setLineWidth(SIZE * LINE_WIDTH);
            CTX.setLineCap(StrokeLineCap.BUTT);
            CTX.strokeArc(xy, xy, wh, wh, -(offset + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
            CTX.restore();
        }
    }

    public static void drawTimeAreas(final Clock CLOCK, final GraphicsContext CTX, final List<TimeSection> AREAS, final double SIZE,
                                     final double XY_INSIDE, final double XY_OUTSIDE, final double WH_INSIDE, final double WH_OUTSIDE) {
        if (AREAS.isEmpty()) return;
        TickLabelLocation tickLabelLocation = CLOCK.getTickLabelLocation();
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? XY_OUTSIDE * SIZE : XY_INSIDE * SIZE;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? WH_OUTSIDE * SIZE : WH_INSIDE * SIZE;
        double            offset            = 90;
        double            angleStep         = 360d / 60d;
        int listSize = AREAS.size();
        for (int i = 0; i < listSize ; i++) {
            TimeSection area           = AREAS.get(i);
            LocalTime   start          = area.getStart();
            LocalTime   stop           = area.getStop();
            double      areaStartAngle = (start.getHour() % 12 * 5d + start.getMinute() / 12d + start.getSecond() / 300d) * angleStep + 180;
            double      areaAngleExtend;
            areaAngleExtend = ((stop.getHour() - start.getHour()) % 12 * 5d + (stop.getMinute() - start.getMinute()) / 12d + (stop.getSecond() - start.getSecond()) / 300d) * angleStep;
            CTX.save();
            CTX.setFill(area.getColor());
            CTX.fillArc(xy, xy, wh, wh, -(offset + areaStartAngle), - areaAngleExtend, ArcType.ROUND);
            CTX.restore();
        }
    }
}
