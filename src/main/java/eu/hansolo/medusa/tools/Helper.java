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

import eu.hansolo.medusa.Alarm;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.medusa.TimeSection;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


/**
 * Created by hansolo on 11.12.15.
 */
public class Helper {
    public static final double MAX_TICK_MARK_LENGTH = 0.125;
    public static final double MAX_TICK_MARK_WIDTH  = 0.02;
    public static final Color  INACTIVE_ALARM_COLOR = Color.rgb(90, 90, 90, 0.5);


    public static final <T extends Number> T clamp(final T MIN, final T MAX, final T VALUE) {
        if (VALUE.doubleValue() < MIN.doubleValue()) return MIN;
        if (VALUE.doubleValue() > MAX.doubleValue()) return MAX;
        return VALUE;
    }

    public static final int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final long clamp(final long MIN, final long MAX, final long VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final double clamp(final double MIN, final double MAX, final double VALUE) {
        if (Double.compare(VALUE, MIN) < 0) return MIN;
        if (Double.compare(VALUE, MAX) > 0) return MAX;
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

    public static DateTimeFormatter getDateFormat(final Locale LOCALE) {
        if (Locale.US == LOCALE) {
            return DateTimeFormatter.ofPattern("MM/dd/YYYY");
        } else if (Locale.CHINA == LOCALE) {
            return DateTimeFormatter.ofPattern("YYYY.MM.dd");
        } else {
            return DateTimeFormatter.ofPattern("dd.MM.YYYY");
        }
    }
    public static DateTimeFormatter getLocalizedDateFormat(final Locale LOCALE) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);
    }

    public static void enableNode(final Node NODE, final boolean ENABLE) {
        NODE.setManaged(ENABLE);
        NODE.setVisible(ENABLE);
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

    public static void drawTrapezoid(final GraphicsContext CTX,
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
    public static void drawTriangle(final GraphicsContext CTX,
                                    final double PIX, final double PIY, final double PO1X, final double PO1Y, final double PO2X, final double PO2Y) {
        CTX.beginPath();
        CTX.moveTo(PIX, PIY);
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

    public static Color getTranslucentColorFrom(final Color COLOR, final double FACTOR) {
        return Color.color(COLOR.getRed(), COLOR.getGreen(), COLOR.getBlue(), Helper.clamp(0.0, 1.0, FACTOR));
    }

    public static void drawRadialTickMarks(final Gauge GAUGE, final GraphicsContext CTX,
                                           final double MIN_VALUE, final double MAX_VALUE,
                                           final double START_ANGLE, final double ANGLE_RANGE, final double ANGLE_STEP,
                                           final double CENTER_X, final double CENTER_Y, final double SIZE) {
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
        double        majorTickMarkLengthFactor    = GAUGE.getMajorTickMarkLengthFactor();
        double        majorTickMarkWidthFactor     = GAUGE.getMajorTickMarkWidthFactor();
        double        mediumTickMarkLengthFactor   = GAUGE.getMediumTickMarkLengthFactor();
        double        mediumTickMarkWidthFactor    = GAUGE.getMediumTickMarkWidthFactor();
        double        minorTickMarkLengthFactor    = GAUGE.getMinorTickMarkLengthFactor();
        double        minorTickMarkWidthFactor     = GAUGE.getMinorTickMarkWidthFactor();
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
        Locale        locale                       = GAUGE.getLocale();
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
            orthTextFactor    = 0.45 * textDisplacementFactor;//TickLabelOrientation.ORTHOGONAL == tickLabelOrientation ? 0.45 * textDisplacementFactor : 0.45 * textDisplacementFactor;
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

        double  customFontSizeFactor       = GAUGE.getCustomTickLabelFontSize() / 400;
        boolean fullRange                  = (MIN_VALUE < 0 && MAX_VALUE > 0);
        double  tickLabelFontSize          = tickLabelDecimals == 0 ? 0.054 * SIZE : 0.051 * SIZE;
        tickLabelFontSize                  = GAUGE.getCustomTickLabelsEnabled() ? customFontSizeFactor * SIZE : tickLabelFontSize;
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

        double trapezoidMajorInnerAngle1;
        double trapezoidMajorInnerAngle2;
        double trapezoidMajorOuterAngle1;
        double trapezoidMajorOuterAngle2;
        double trapezoidMajorInnerPoint1X;
        double trapezoidMajorInnerPoint1Y;
        double trapezoidMajorInnerPoint2X;
        double trapezoidMajorInnerPoint2Y;
        double trapezoidMajorOuterPoint1X;
        double trapezoidMajorOuterPoint1Y;
        double trapezoidMajorOuterPoint2X;
        double trapezoidMajorOuterPoint2Y;

        double trapezoidMediumInnerAngle1;
        double trapezoidMediumInnerAngle2;
        double trapezoidMediumOuterAngle1;
        double trapezoidMediumOuterAngle2;
        double trapezoidMediumInnerPoint1X;
        double trapezoidMediumInnerPoint1Y;
        double trapezoidMediumInnerPoint2X;
        double trapezoidMediumInnerPoint2Y;
        double trapezoidMediumOuterPoint1X;
        double trapezoidMediumOuterPoint1Y;
        double trapezoidMediumOuterPoint2X;
        double trapezoidMediumOuterPoint2Y;

        double trapezoidMinorInnerAngle1;
        double trapezoidMinorInnerAngle2;
        double trapezoidMinorOuterAngle1;
        double trapezoidMinorOuterAngle2;
        double trapezoidMinorInnerPoint1X;
        double trapezoidMinorInnerPoint1Y;
        double trapezoidMinorInnerPoint2X;
        double trapezoidMinorInnerPoint2Y;
        double trapezoidMinorOuterPoint1X;
        double trapezoidMinorOuterPoint1Y;
        double trapezoidMinorOuterPoint2X;
        double trapezoidMinorOuterPoint2Y;

        double triangleMajorInnerPointX;
        double triangleMajorInnerPointY;
        double triangleMajorOuterPointX;
        double triangleMajorOuterPointY;

        double triangleMediumInnerPointX;
        double triangleMediumInnerPointY;
        double triangleMediumOuterPointX;
        double triangleMediumOuterPointY;

        double triangleMinorInnerPointX;
        double triangleMinorInnerPointY;
        double triangleMinorOuterPointX;
        double triangleMinorOuterPointY;

        ScaleDirection scaleDirection = GAUGE.getScaleDirection();

        // Draw tickmark ring
        if (GAUGE.isTickMarkRingVisible()) {
            SkinType skinType     = GAUGE.getSkinType();
            double   xy           = TickLabelLocation.INSIDE == tickLabelLocation ? SIZE * 0.026 : SIZE * 0.14;
            double   wh           = TickLabelLocation.INSIDE == tickLabelLocation ? SIZE * 0.948 : SIZE * 0.72;
            double   offset       = -90 + START_ANGLE;
            double   horVerOffset = SIZE * 0.055555555;
            CTX.setLineWidth(SIZE * 0.004);
            CTX.setLineCap(StrokeLineCap.SQUARE);
            CTX.save();
            CTX.setStroke(tickMarkColor);
            switch(skinType) {
                case HORIZONTAL: CTX.strokeArc(xy + horVerOffset, xy, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
                case VERTICAL  : CTX.strokeArc(xy, xy + horVerOffset, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
                default        : CTX.strokeArc(xy, xy, wh, wh, offset, -ANGLE_RANGE, ArcType.OPEN); break;
            }

            CTX.restore();
            if (tickMarkSections.size() > 0) {
                int    listSize = tickMarkSections.size();
                double sectionStartAngle;
                for (int i = 0; i < listSize; i++) {
                    Section section = tickMarkSections.get(i);
                    if (Double.compare(section.getStart(), MIN_VALUE) < 0 && Double.compare(section.getStop(), MAX_VALUE) < 0) {
                        sectionStartAngle = 0;
                    } else {
                        sectionStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStart() - MIN_VALUE) * ANGLE_STEP : -(section.getStart() - MIN_VALUE) * ANGLE_STEP;
                    }
                    double sectionAngleExtend;
                    if (Double.compare(section.getStop(), MAX_VALUE) > 0) {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (MAX_VALUE - section.getStart()) * ANGLE_STEP : -(MAX_VALUE - section.getStart()) * ANGLE_STEP;
                    } else {
                        sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStop() - section.getStart()) * ANGLE_STEP : -(section.getStop() - section.getStart()) * ANGLE_STEP;
                    }
                    CTX.save();
                    CTX.setStroke(section.getColor());
                    switch(skinType) {
                        case HORIZONTAL: CTX.strokeArc(xy + horVerOffset, xy, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                        case VERTICAL  : CTX.strokeArc(xy, xy + horVerOffset, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                        default        : CTX.strokeArc(xy, xy, wh, wh, offset - sectionStartAngle, -sectionAngleExtend, ArcType.OPEN); break;
                    }
                    CTX.restore();
                }
            }
        }

        // Main loop
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
                    innerPointX                 = centerX + SIZE * 0.3585 * sinValue;
                    innerPointY                 = centerY + SIZE * 0.3585 * cosValue;
                    innerMediumPointX           = innerPointX;
                    innerMediumPointY           = innerPointY;
                    innerMinorPointX            = innerPointX;
                    innerMinorPointY            = innerPointY;
                    outerPointX                 = centerX + SIZE * (0.3585 + MAX_TICK_MARK_LENGTH * majorTickMarkLengthFactor) * sinValue;
                    outerPointY                 = centerY + SIZE * (0.3585 + MAX_TICK_MARK_LENGTH * majorTickMarkLengthFactor) * cosValue;
                    outerMediumPointX           = centerX + SIZE * (0.3585 + MAX_TICK_MARK_LENGTH * mediumTickMarkLengthFactor) * sinValue;
                    outerMediumPointY           = centerY + SIZE * (0.3585 + MAX_TICK_MARK_LENGTH * mediumTickMarkLengthFactor) * cosValue;
                    outerMinorPointX            = centerX + SIZE * (0.3585 + MAX_TICK_MARK_LENGTH * minorTickMarkLengthFactor) * sinValue;
                    outerMinorPointY            = centerY + SIZE * (0.3585 + MAX_TICK_MARK_LENGTH * minorTickMarkLengthFactor) * cosValue;
                    textPointX                  = centerX + SIZE * orthTextFactor * sinValue;
                    textPointY                  = centerY + SIZE * orthTextFactor * cosValue;
                    dotCenterX                  = centerX + SIZE * 0.3685 * sinValue;
                    dotCenterY                  = centerY + SIZE * 0.3685 * cosValue;
                    dotMediumCenterX            = centerX + SIZE * 0.365375 * sinValue;
                    dotMediumCenterY            = centerY + SIZE * 0.365375 * cosValue;
                    dotMinorCenterX             = centerX + SIZE * 0.36225 * sinValue;
                    dotMinorCenterY             = centerY + SIZE * 0.36225 * cosValue;
                    tickLabelTickMarkX          = centerX + SIZE * 0.3805 * sinValue;
                    tickLabelTickMarkY          = centerY + SIZE * 0.3805 * cosValue;

                    trapezoidMajorInnerAngle1   = Math.toRadians(angle - 1.2 + START_ANGLE);
                    trapezoidMajorInnerAngle2   = Math.toRadians(angle + 1.2 + START_ANGLE);
                    trapezoidMajorOuterAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    trapezoidMajorOuterAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    trapezoidMajorInnerPoint1X  = centerX + SIZE * 0.3585 * Math.sin(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint1Y  = centerY + SIZE * 0.3585 * Math.cos(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint2X  = centerX + SIZE * 0.3585 * Math.sin(trapezoidMajorInnerAngle2);
                    trapezoidMajorInnerPoint2Y  = centerY + SIZE * 0.3585 * Math.cos(trapezoidMajorInnerAngle2);
                    trapezoidMajorOuterPoint1X  = centerX + SIZE * 0.4105 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint1Y  = centerY + SIZE * 0.4105 * Math.cos(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint2X  = centerX + SIZE * 0.4105 * Math.sin(trapezoidMajorOuterAngle2);
                    trapezoidMajorOuterPoint2Y  = centerY + SIZE * 0.4105 * Math.cos(trapezoidMajorOuterAngle2);

                    trapezoidMediumInnerAngle1  = Math.toRadians(angle - 1.0 + START_ANGLE);
                    trapezoidMediumInnerAngle2  = Math.toRadians(angle + 1.0 + START_ANGLE);
                    trapezoidMediumOuterAngle1  = Math.toRadians(angle - 0.7 + START_ANGLE);
                    trapezoidMediumOuterAngle2  = Math.toRadians(angle + 0.7 + START_ANGLE);
                    trapezoidMediumInnerPoint1X = centerX + SIZE * 0.3585 * Math.sin(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint1Y = centerY + SIZE * 0.3585 * Math.cos(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint2X = centerX + SIZE * 0.3585 * Math.sin(trapezoidMediumInnerAngle2);
                    trapezoidMediumInnerPoint2Y = centerY + SIZE * 0.3585 * Math.cos(trapezoidMediumInnerAngle2);
                    trapezoidMediumOuterPoint1X = centerX + SIZE * 0.3985 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMediumOuterPoint1Y = centerY + SIZE * 0.3985 * Math.cos(trapezoidMediumOuterAngle1);
                    trapezoidMediumOuterPoint2X = centerX + SIZE * 0.3985 * Math.sin(trapezoidMediumOuterAngle2);
                    trapezoidMediumOuterPoint2Y = centerY + SIZE * 0.3985 * Math.cos(trapezoidMediumOuterAngle2);

                    trapezoidMinorInnerAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    trapezoidMinorInnerAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    trapezoidMinorOuterAngle1   = Math.toRadians(angle - 0.6 + START_ANGLE);
                    trapezoidMinorOuterAngle2   = Math.toRadians(angle + 0.6 + START_ANGLE);
                    trapezoidMinorInnerPoint1X  = centerX + SIZE * 0.3585 * Math.sin(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint1Y  = centerY + SIZE * 0.3585 * Math.cos(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint2X  = centerX + SIZE * 0.3585 * Math.sin(trapezoidMinorInnerAngle2);
                    trapezoidMinorInnerPoint2Y  = centerY + SIZE * 0.3585 * Math.cos(trapezoidMinorInnerAngle2);
                    trapezoidMinorOuterPoint1X  = centerX + SIZE * 0.3975 * Math.sin(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint1Y  = centerY + SIZE * 0.3975 * Math.cos(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint2X  = centerX + SIZE * 0.3975 * Math.sin(trapezoidMinorOuterAngle2);
                    trapezoidMinorOuterPoint2Y  = centerY + SIZE * 0.3975 * Math.cos(trapezoidMinorOuterAngle2);

                    triangleMajorInnerPointX    = centerX + SIZE * 0.3585 * sinValue;
                    triangleMajorInnerPointY    = centerY + SIZE * 0.3585 * cosValue;
                    triangleMajorOuterPointX    = centerX + SIZE * 0.4105 * sinValue;
                    triangleMajorOuterPointY    = centerY + SIZE * 0.4105 * cosValue;

                    triangleMediumInnerPointX   = triangleMajorInnerPointX;
                    triangleMediumInnerPointY   = triangleMajorInnerPointY;
                    triangleMediumOuterPointX   = centerX + SIZE * 0.4045 * sinValue;
                    triangleMediumOuterPointY   = centerY + SIZE * 0.4045 * cosValue;

                    triangleMinorInnerPointX    = triangleMajorInnerPointX;
                    triangleMinorInnerPointY    = triangleMajorInnerPointY;
                    triangleMinorOuterPointX    = centerX + SIZE * 0.3975 * sinValue;
                    triangleMinorOuterPointY    = centerY + SIZE * 0.3975 * cosValue;
                    break;
                case INSIDE:
                default:
                    innerPointX                 = centerX + SIZE * (0.475 - MAX_TICK_MARK_LENGTH * majorTickMarkLengthFactor) * sinValue;
                    innerPointY                 = centerY + SIZE * (0.475 - MAX_TICK_MARK_LENGTH * majorTickMarkLengthFactor) * cosValue;
                    innerMediumPointX           = centerX + SIZE * (0.475 - MAX_TICK_MARK_LENGTH * mediumTickMarkLengthFactor) * sinValue;
                    innerMediumPointY           = centerY + SIZE * (0.475 - MAX_TICK_MARK_LENGTH * mediumTickMarkLengthFactor) * cosValue;
                    innerMinorPointX            = centerX + SIZE * (0.475 - MAX_TICK_MARK_LENGTH * minorTickMarkLengthFactor) * sinValue;
                    innerMinorPointY            = centerY + SIZE * (0.475 - MAX_TICK_MARK_LENGTH * minorTickMarkLengthFactor) * cosValue;
                    outerPointX                 = centerX + SIZE * 0.475 * sinValue;
                    outerPointY                 = centerY + SIZE * 0.475 * cosValue;
                    outerMediumPointX           = outerPointX;
                    outerMediumPointY           = outerPointY;
                    outerMinorPointX            = outerPointX;
                    outerMinorPointY            = outerPointY;
                    textPointX                  = centerX + SIZE * orthTextFactor * sinValue;
                    textPointY                  = centerY + SIZE * orthTextFactor * cosValue;
                    dotCenterX                  = centerX + SIZE * 0.4625 * sinValue;
                    dotCenterY                  = centerY + SIZE * 0.4625 * cosValue;
                    dotMediumCenterX            = centerX + SIZE * 0.465625 * sinValue;
                    dotMediumCenterY            = centerY + SIZE * 0.465625 * cosValue;
                    dotMinorCenterX             = centerX + SIZE * 0.46875 * sinValue;
                    dotMinorCenterY             = centerY + SIZE * 0.46875 * cosValue;
                    tickLabelTickMarkX          = centerX + SIZE * 0.445 * sinValue;
                    tickLabelTickMarkY          = centerY + SIZE * 0.445 * cosValue;

                    trapezoidMajorInnerAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    trapezoidMajorInnerAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    trapezoidMajorOuterAngle1   = Math.toRadians(angle - 1.2 + START_ANGLE);
                    trapezoidMajorOuterAngle2   = Math.toRadians(angle + 1.2 + START_ANGLE);
                    trapezoidMajorInnerPoint1X  = centerX + SIZE * 0.423 * Math.sin(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint1Y  = centerY + SIZE * 0.423 * Math.cos(trapezoidMajorInnerAngle1);
                    trapezoidMajorInnerPoint2X  = centerX + SIZE * 0.423 * Math.sin(trapezoidMajorInnerAngle2);
                    trapezoidMajorInnerPoint2Y  = centerY + SIZE * 0.423 * Math.cos(trapezoidMajorInnerAngle2);
                    trapezoidMajorOuterPoint1X  = centerX + SIZE * 0.475 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint1Y  = centerY + SIZE * 0.475 * Math.cos(trapezoidMajorOuterAngle1);
                    trapezoidMajorOuterPoint2X  = centerX + SIZE * 0.475 * Math.sin(trapezoidMajorOuterAngle2);
                    trapezoidMajorOuterPoint2Y  = centerY + SIZE * 0.475 * Math.cos(trapezoidMajorOuterAngle2);

                    trapezoidMediumInnerAngle1  = Math.toRadians(angle - 0.7 + START_ANGLE);
                    trapezoidMediumInnerAngle2  = Math.toRadians(angle + 0.7 + START_ANGLE);
                    trapezoidMediumOuterAngle1  = Math.toRadians(angle - 1.0 + START_ANGLE);
                    trapezoidMediumOuterAngle2  = Math.toRadians(angle + 1.0 + START_ANGLE);
                    trapezoidMediumInnerPoint1X = centerX + SIZE * 0.435 * Math.sin(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint1Y = centerY + SIZE * 0.435 * Math.cos(trapezoidMediumInnerAngle1);
                    trapezoidMediumInnerPoint2X = centerX + SIZE * 0.435 * Math.sin(trapezoidMediumInnerAngle2);
                    trapezoidMediumInnerPoint2Y = centerY + SIZE * 0.435 * Math.cos(trapezoidMediumInnerAngle2);
                    trapezoidMediumOuterPoint1X = centerX + SIZE * 0.475 * Math.sin(trapezoidMajorOuterAngle1);
                    trapezoidMediumOuterPoint1Y = centerY + SIZE * 0.475 * Math.cos(trapezoidMediumOuterAngle1);
                    trapezoidMediumOuterPoint2X = centerX + SIZE * 0.475 * Math.sin(trapezoidMediumOuterAngle2);
                    trapezoidMediumOuterPoint2Y = centerY + SIZE * 0.475 * Math.cos(trapezoidMediumOuterAngle2);

                    trapezoidMinorInnerAngle1   = Math.toRadians(angle - 0.6 + START_ANGLE);
                    trapezoidMinorInnerAngle2   = Math.toRadians(angle + 0.6 + START_ANGLE);
                    trapezoidMinorOuterAngle1   = Math.toRadians(angle - 0.8 + START_ANGLE);
                    trapezoidMinorOuterAngle2   = Math.toRadians(angle + 0.8 + START_ANGLE);
                    trapezoidMinorInnerPoint1X  = centerX + SIZE * 0.440 * Math.sin(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint1Y  = centerY + SIZE * 0.440 * Math.cos(trapezoidMinorInnerAngle1);
                    trapezoidMinorInnerPoint2X  = centerX + SIZE * 0.440 * Math.sin(trapezoidMinorInnerAngle2);
                    trapezoidMinorInnerPoint2Y  = centerY + SIZE * 0.440 * Math.cos(trapezoidMinorInnerAngle2);
                    trapezoidMinorOuterPoint1X  = centerX + SIZE * 0.475 * Math.sin(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint1Y  = centerY + SIZE * 0.475 * Math.cos(trapezoidMinorOuterAngle1);
                    trapezoidMinorOuterPoint2X  = centerX + SIZE * 0.475 * Math.sin(trapezoidMinorOuterAngle2);
                    trapezoidMinorOuterPoint2Y  = centerY + SIZE * 0.475 * Math.cos(trapezoidMinorOuterAngle2);

                    triangleMajorInnerPointX    = centerX + SIZE * 0.423 * sinValue;
                    triangleMajorInnerPointY    = centerY + SIZE * 0.423 * cosValue;
                    triangleMajorOuterPointX    = centerX + SIZE * 0.475 * sinValue;
                    triangleMajorOuterPointY    = centerY + SIZE * 0.475 * cosValue;

                    triangleMediumInnerPointX   = centerX + SIZE * 0.43 * sinValue;
                    triangleMediumInnerPointY   = centerY + SIZE * 0.43 * cosValue;
                    triangleMediumOuterPointX   = triangleMajorOuterPointX;
                    triangleMediumOuterPointY   = triangleMajorOuterPointY;

                    triangleMinorInnerPointX    = centerX + SIZE * 0.436 * sinValue;
                    triangleMinorInnerPointY    = centerY + SIZE * 0.436 * cosValue;
                    triangleMinorOuterPointX    = triangleMajorOuterPointX;
                    triangleMinorOuterPointY    = triangleMajorOuterPointY;
                    break;
            }

            // Set the general tickmark color
            CTX.setStroke(tickMarkColor);
            CTX.setFill(tickMarkColor);
            CTX.setLineCap(StrokeLineCap.BUTT);

            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw major tick mark
                isNotZero = Double.compare(0.0, counter) != 0;
                TickMarkType tickMarkType = null;
                if (majorTickMarksVisible) {
                    tickMarkType = majorTickMarkType;
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    CTX.setLineWidth(SIZE * (TickMarkType.BOX == tickMarkType || TickMarkType.PILL == tickMarkType ? 0.016 : MAX_TICK_MARK_WIDTH * majorTickMarkWidthFactor));
                    CTX.setLineCap(TickMarkType.PILL == tickMarkType ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
                } else if (minorTickMarksVisible) {
                    tickMarkType = minorTickMarkType;
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    CTX.setLineWidth(SIZE * (TickMarkType.BOX == tickMarkType || TickMarkType.PILL == tickMarkType ? 0.007 : MAX_TICK_MARK_WIDTH * minorTickMarkWidthFactor));
                    CTX.setLineCap(TickMarkType.PILL == tickMarkType ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
                }
                if (fullRange && !isNotZero) {
                    CTX.setFill(zeroColor);
                    CTX.setStroke(zeroColor);
                }
                if (null != tickMarkType) {
                    switch (tickMarkType) {
                        case TRAPEZOID:
                            if (majorTickMarksVisible) {
                                Helper.drawTrapezoid(CTX, trapezoidMajorInnerPoint1X, trapezoidMajorInnerPoint1Y, trapezoidMajorInnerPoint2X, trapezoidMajorInnerPoint2Y,
                                                    trapezoidMajorOuterPoint1X, trapezoidMajorOuterPoint1Y, trapezoidMajorOuterPoint2X, trapezoidMajorOuterPoint2Y);
                            } else if (minorTickMarksVisible) {
                                Helper.drawTrapezoid(CTX, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y,
                                                    trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                            }
                            break;
                        case TRIANGLE:
                            if (majorTickMarksVisible) {
                                if (TickLabelLocation.INSIDE == tickLabelLocation) {
                                    Helper.drawTriangle(CTX, triangleMajorInnerPointX, triangleMajorInnerPointY, trapezoidMajorOuterPoint1X, trapezoidMajorOuterPoint1Y, trapezoidMajorOuterPoint2X, trapezoidMajorOuterPoint2Y);
                                } else {
                                    Helper.drawTriangle(CTX, triangleMajorOuterPointX, triangleMajorOuterPointY, trapezoidMajorInnerPoint1X, trapezoidMajorInnerPoint1Y, trapezoidMajorInnerPoint2X, trapezoidMajorInnerPoint2Y);
                                }
                            } else if (minorTickMarksVisible) {
                                if (TickLabelLocation.INSIDE == tickLabelLocation) {
                                    Helper.drawTriangle(CTX, triangleMinorInnerPointX, triangleMinorInnerPointY, trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                                } else {
                                    Helper.drawTriangle(CTX, triangleMinorOuterPointX, triangleMinorOuterPointY, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y);
                                }
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
                                CTX.fillText(String.format(locale, tickLabelFormatString, counter), 0, 0);
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
                        CTX.fillText(String.format(locale, tickLabelFormatString, counter), 0, 0);
                    }
                    CTX.restore();
                }
            } else if (mediumTickMarksVisible &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0.0) != 0.0 &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0.0) == 0.0) {
                // Draw medium tick mark
                CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, mediumTickMarkColor) : mediumTickMarkColor);
                switch(mediumTickMarkType) {
                    case TRAPEZOID:
                        Helper.drawTrapezoid(CTX, trapezoidMediumInnerPoint1X, trapezoidMediumInnerPoint1Y, trapezoidMediumInnerPoint2X, trapezoidMediumInnerPoint2Y,
                                            trapezoidMediumOuterPoint1X, trapezoidMediumOuterPoint1Y, trapezoidMediumOuterPoint2X, trapezoidMediumOuterPoint2Y);
                        break;
                    case TRIANGLE:
                        if (TickLabelLocation.INSIDE == tickLabelLocation) {
                            Helper.drawTriangle(CTX, triangleMediumInnerPointX, triangleMediumInnerPointY, trapezoidMediumOuterPoint1X, trapezoidMediumOuterPoint1Y, trapezoidMediumOuterPoint2X, trapezoidMediumOuterPoint2Y);
                        } else {
                            Helper.drawTriangle(CTX, triangleMediumOuterPointX, triangleMediumOuterPointY, trapezoidMediumInnerPoint1X, trapezoidMediumInnerPoint1Y, trapezoidMediumInnerPoint2X, trapezoidMediumInnerPoint2Y);
                        }
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
                    case PILL:
                        CTX.setLineCap(StrokeLineCap.ROUND);
                        CTX.setLineWidth(SIZE * 0.009);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(CTX, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(CTX, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                    case LINE:
                    default:
                        CTX.setLineWidth(SIZE * MAX_TICK_MARK_WIDTH * mediumTickMarkWidthFactor);
                        if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                            Helper.drawLine(CTX, innerPointX, innerPointY, outerMediumPointX, outerMediumPointY);
                        } else {
                            Helper.drawLine(CTX, innerMediumPointX, innerMediumPointY, outerPointX, outerPointY);
                        }
                        break;
                }
            } else if (minorTickMarksVisible && Double.compare(counterBD.remainder(minorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw minor tick mark
                if (TickMarkType.TICK_LABEL != majorTickMarkType) {
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, minorTickMarkColor) : minorTickMarkColor);
                    switch (minorTickMarkType) {
                        case TRAPEZOID:
                            Helper.drawTrapezoid(CTX, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y,
                                                trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                            break;
                        case TRIANGLE:
                            if (TickLabelLocation.INSIDE == tickLabelLocation) {
                                Helper.drawTriangle(CTX, triangleMinorInnerPointX, triangleMinorInnerPointY, trapezoidMinorOuterPoint1X, trapezoidMinorOuterPoint1Y, trapezoidMinorOuterPoint2X, trapezoidMinorOuterPoint2Y);
                            } else {
                                Helper.drawTriangle(CTX, triangleMinorOuterPointX, triangleMinorOuterPointY, trapezoidMinorInnerPoint1X, trapezoidMinorInnerPoint1Y, trapezoidMinorInnerPoint2X, trapezoidMinorInnerPoint2Y);
                            }
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
                        case PILL:
                            CTX.setLineCap(StrokeLineCap.ROUND);
                            CTX.setLineWidth(SIZE * 0.007);
                            if (TickLabelLocation.OUTSIDE == tickLabelLocation) {
                                Helper.drawLine(CTX, innerPointX, innerPointY, outerMinorPointX, outerMinorPointY);
                            } else {
                                Helper.drawLine(CTX, innerMinorPointX, innerMinorPointY, outerPointX, outerPointY);
                            }
                            break;
                        case LINE:
                        default:
                            CTX.setLineWidth(SIZE * MAX_TICK_MARK_WIDTH * minorTickMarkWidthFactor);
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
        double              alphaVariationInPercent = Helper.clamp(0.0, 100.0, ALPHA_VARIATION_IN_PERCENT);
        final WritableImage IMAGE                   = new WritableImage(width, height);
        final PixelWriter   PIXEL_WRITER            = IMAGE.getPixelWriter();
        final Random        BW_RND                  = new Random();
        final Random        ALPHA_RND               = new Random();
        final double        ALPHA_START             = alphaVariationInPercent / 100 / 2;
        final double        ALPHA_VARIATION         = alphaVariationInPercent / 100;
        for (int y = 0 ; y < height ; y++) {
            for (int x = 0 ; x < width ; x++) {
                final Color  NOISE_COLOR = BW_RND.nextBoolean() == true ? BRIGHT_COLOR : DARK_COLOR;
                final double NOISE_ALPHA = Helper.clamp(0.0, 1.0, ALPHA_START + ALPHA_RND.nextDouble() * ALPHA_VARIATION);
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
        ZonedDateTime     time              = CLOCK.getTime();
        boolean           isAM              = time.get(ChronoField.AMPM_OF_DAY) == 0;
        double            xy                = TickLabelLocation.INSIDE == tickLabelLocation ? XY_INSIDE * SIZE : XY_OUTSIDE * SIZE;
        double            wh                = TickLabelLocation.INSIDE == tickLabelLocation ? WH_INSIDE * SIZE : WH_OUTSIDE * SIZE;
        double            offset            = 90;
        int               listSize          = SECTIONS.size();
        double            angleStep         = 360.0 / 60.0;
        boolean           highlightSections = CLOCK.isHighlightSections();
        for (int i = 0 ; i < listSize ; i++) {
            TimeSection section   = SECTIONS.get(i);
            LocalTime   start     = section.getStart();
            LocalTime   stop      = section.getStop();
            boolean     isStartAM = start.get(ChronoField.AMPM_OF_DAY) == 0;
            boolean     isStopAM  = stop.get(ChronoField.AMPM_OF_DAY) == 0;
            boolean     draw      = isAM ? (isStartAM || isStopAM) :(!isStartAM || !isStopAM);
            if (draw) {
                double sectionStartAngle = (start.getHour() % 12 * 5.0 + start.getMinute() / 12.0 + start.getSecond() / 300.0) * angleStep + 180;
                double sectionAngleExtend = ((stop.getHour() - start.getHour()) % 12 * 5.0 + (stop.getMinute() - start.getMinute()) / 12.0 + (stop.getSecond() - start.getSecond()) / 300.0) * angleStep;
                //TODO: Add an indicator to the section like -1 or similar
                // check if start was already yesterday
                if (start.getHour() > stop.getHour()) { sectionAngleExtend = (360.0 - Math.abs(sectionAngleExtend)); }
                CTX.save();
                if (highlightSections) {
                    CTX.setStroke(section.contains(time.toLocalTime()) ? section.getHighlightColor() : section.getColor());
                } else {
                    CTX.setStroke(section.getColor());
                }
                CTX.setLineWidth(SIZE * LINE_WIDTH);
                CTX.setLineCap(StrokeLineCap.BUTT);
                CTX.strokeArc(xy, xy, wh, wh, -(offset + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
                CTX.restore();
            }
        }
    }

    public static void drawTimeAreas(final Clock CLOCK, final GraphicsContext CTX, final List<TimeSection> AREAS, final double SIZE,
                                     final double XY_INSIDE, final double XY_OUTSIDE, final double WH_INSIDE, final double WH_OUTSIDE) {
        if (AREAS.isEmpty()) return;
        TickLabelLocation tickLabelLocation = CLOCK.getTickLabelLocation();
        ZonedDateTime     time              = CLOCK.getTime();
        boolean           isAM              = time.get(ChronoField.AMPM_OF_DAY) == 0;
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? XY_OUTSIDE * SIZE : XY_INSIDE * SIZE;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? WH_OUTSIDE * SIZE : WH_INSIDE * SIZE;
        double            offset            = 90;
        double            angleStep         = 360.0 / 60.0;
        int               listSize          = AREAS.size();
        boolean           highlightAreas    = CLOCK.isHighlightAreas();
        for (int i = 0; i < listSize ; i++) {
            TimeSection area      = AREAS.get(i);
            LocalTime   start     = area.getStart();
            LocalTime   stop      = area.getStop();
            boolean     isStartAM = start.get(ChronoField.AMPM_OF_DAY) == 0;
            boolean     isStopAM  = stop.get(ChronoField.AMPM_OF_DAY) == 0;
            boolean     draw      = isAM ? (isStartAM || isStopAM) :(!isStartAM || !isStopAM);
            if (draw) {
                double areaStartAngle  = (start.getHour() % 12 * 5.0 + start.getMinute() / 12.0 + start.getSecond() / 300.0) * angleStep + 180;;
                double areaAngleExtend = ((stop.getHour() - start.getHour()) % 12 * 5.0 + (stop.getMinute() - start.getMinute()) / 12.0 + (stop.getSecond() - start.getSecond()) / 300.0) * angleStep;
                //TODO: Add an indicator to the area like -1 or similar
                // check if start was already yesterday
                if (start.getHour() > stop.getHour()) { areaAngleExtend = (360.0 - Math.abs(areaAngleExtend)); }
                CTX.save();
                if (highlightAreas) {
                    CTX.setFill(area.contains(time.toLocalTime()) ? area.getHighlightColor() : area.getColor());
                } else {
                    CTX.setFill(area.getColor());
                }
                CTX.fillArc(xy, xy, wh, wh, -(offset + areaStartAngle), -areaAngleExtend, ArcType.ROUND);
                CTX.restore();
            }
        }
    }

    public static void drawAlarms(final Clock CLOCK, final double SIZE, final double ALARM_MARKER_SIZE, final double ALARM_MARKER_RADIUS, final Map<Alarm, Circle> ALARM_MAP, final DateTimeFormatter DATE_TIME_FORMATTER, final ZonedDateTime TIME) {
        if (CLOCK.isAlarmsVisible()) {
            double alarmSize = ALARM_MARKER_SIZE * SIZE;
            double center    = SIZE * 0.5;
            double angleStep = 360.0 / 60.0;
            for (Map.Entry<Alarm, Circle> entry : ALARM_MAP.entrySet()) {
                Alarm         alarm      = entry.getKey();
                ZonedDateTime alarmTime  = alarm.getTime();
                double        alarmAngle = (alarmTime.getMinute() + alarmTime.getSecond() / 60.0) * angleStep + 180;
                double        sinValue   = Math.sin(Math.toRadians((-alarmAngle)));
                double        cosValue   = Math.cos(Math.toRadians((-alarmAngle)));
                Color         alarmColor = alarm.isArmed() ? alarm.getColor() : INACTIVE_ALARM_COLOR;
                Circle        dot        = entry.getValue();
                dot.setRadius(alarmSize);
                dot.setCenterX(center + SIZE * ALARM_MARKER_RADIUS * sinValue);
                dot.setCenterY(center + SIZE * ALARM_MARKER_RADIUS * cosValue);
                dot.setFill(alarmColor);
                dot.setStroke(alarmColor.darker());
                dot.setPickOnBounds(false);
                dot.setOnMousePressed(e -> alarm.fireAlarmMarkerEvent(alarm.ALARM_MARKER_PRESSED_EVENT));
                dot.setOnMouseReleased(e -> alarm.fireAlarmMarkerEvent(alarm.ALARM_MARKER_RELEASED_EVENT));
                if (alarmTime.getDayOfMonth() == TIME.getDayOfMonth() &&
                    alarmTime.getMonthValue() == TIME.getMonthValue() &&
                    alarmTime.getYear() == TIME.getYear() &&
                    alarmTime.getHour() == TIME.getHour() &&
                    alarmTime.getMinute() >= TIME.getMinute()) {
                    dot.setManaged(true);
                    dot.setVisible(true);
                } else {
                    dot.setManaged(false);
                    dot.setVisible(false);
                }
                Tooltip alarmTooltip;
                if (alarm.getText().isEmpty()) {
                    alarmTooltip = new Tooltip(DATE_TIME_FORMATTER.format(alarm.getTime()));
                } else {
                    alarmTooltip = new Tooltip(new StringBuilder(alarm.getText()).append("\n").append(DATE_TIME_FORMATTER.format(alarm.getTime())).toString());
                }
                alarmTooltip.setTextAlignment(TextAlignment.CENTER);
                Tooltip.install(dot, alarmTooltip);
            }
        }
    }

    public static String formatNumber(final Gauge GAUGE, final double VALUE) {
        return formatNumber(GAUGE.getLocale(), GAUGE.getFormatString(), GAUGE.getDecimals(), VALUE);
    }

    public static String formatNumber(final Locale LOCALE, final String FORMAT_STRING, final int DECIMALS, final double VALUE) {
        double value = VALUE;
        if (value > 0) {
            value = Math.floor(value * Math.pow(10, DECIMALS)) / Math.pow(10, DECIMALS);
        } else if (value < 0) {
            value = Math.ceil(value * Math.pow(10, DECIMALS)) / Math.pow(10, DECIMALS);
        }
        return String.format(LOCALE, FORMAT_STRING, value);
    }

    public static String formatNumber(final Locale LOCALE, final double MIN_VALUE, final double MAX_VALUE, final int DECIMALS, final double VALUE) {
        StringBuilder sb        = new StringBuilder("%.").append(DECIMALS).append("f");
        String        f         = sb.toString();
        int           minLength = String.format(Locale.US, f, MIN_VALUE).length();
        int           maxLength = String.format(Locale.US, f, MAX_VALUE).length();
        int           length    = Math.max(minLength, maxLength);

        StringBuilder formatStringBuilder = new StringBuilder("%").append(length).append(".").append(DECIMALS).append("f");
        String        formatString        = formatStringBuilder.toString();

        double value = VALUE;
        if (value > 0) {
            value = Math.floor(VALUE * Math.pow(10, DECIMALS)) / Math.pow(10, DECIMALS);
        } else if (value < 0) {
            value = Math.ceil(VALUE * Math.pow(10, DECIMALS)) / Math.pow(10, DECIMALS);
        }

        return String.format(LOCALE, formatString, value);
    }
}
