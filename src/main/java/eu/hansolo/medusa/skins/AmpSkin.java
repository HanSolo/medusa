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

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.math.BigDecimal;
import java.util.Locale;


/**
 * Created by hansolo on 30.12.15.
 */
public class AmpSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 310;
    private static final double PREFERRED_HEIGHT = 260;
    private static final double MINIMUM_WIDTH    = 31;
    private static final double MINIMUM_HEIGHT   = 26;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 858;
    private static final double ASPECT_RATIO     = 0.83870968;
    private static final double START_ANGLE      = 225;
    private static final double ANGLE_RANGE      = 90;
    private double          oldValue;
    private double          width;
    private double          height;
    private Pane            pane;
    private SVGPath         foreground;
    private Canvas          ticksAndSectionsCanvas;
    private GraphicsContext ticksAndSections;
    private double          ledSize;
    private InnerShadow     ledOnShadow;
    private InnerShadow     ledOffShadow;
    private LinearGradient  frameGradient;
    private LinearGradient  ledOnGradient;
    private LinearGradient  ledOffGradient;
    private RadialGradient  highlightGradient;
    private Canvas          ledCanvas;
    private GraphicsContext led;
    private Path            needle;
    private MoveTo          needleMoveTo1;
    private CubicCurveTo    needleCubicCurveTo2;
    private CubicCurveTo    needleCubicCurveTo3;
    private CubicCurveTo    needleCubicCurveTo4;
    private LineTo          needleLineTo5;
    private CubicCurveTo    needleCubicCurveTo6;
    private ClosePath       needleClosePath7;
    private Rotate          needleRotate;
    private Group           shadowGroup;
    private InnerShadow     lightEffect;
    private DropShadow      dropShadow;
    private Text            titleText;
    private Text            unitText;
    private Rectangle       lcd;
    private Label           lcdText;
    private double          angleStep;
    private String          formatString;
    private Locale          locale;


    // ******************** Constructors **************************************
    public AmpSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleStep    = gauge.getAngleRange() / gauge.getRange();
        oldValue     = gauge.getValue();
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale       = gauge.getLocale();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
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
        ticksAndSectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksAndSections       = ticksAndSectionsCanvas.getGraphicsContext2D();

        ledCanvas = new Canvas();
        led       = ledCanvas.getGraphicsContext2D();

        needleRotate = new Rotate(180 - START_ANGLE);
        needleRotate.setAngle(needleRotate.getAngle() + (getSkinnable().getValue() - oldValue - getSkinnable().getMinValue()) * angleStep);

        needleMoveTo1       = new MoveTo();
        needleCubicCurveTo2 = new CubicCurveTo();
        needleCubicCurveTo3 = new CubicCurveTo();
        needleCubicCurveTo4 = new CubicCurveTo();
        needleLineTo5       = new LineTo();
        needleCubicCurveTo6 = new CubicCurveTo();
        needleClosePath7    = new ClosePath();
        needle              = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4, needleLineTo5, needleCubicCurveTo6, needleClosePath7);
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle);
        shadowGroup.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setMouseTransparent(true);
        unitText.setTextOrigin(VPos.CENTER);

        lcd = new Rectangle(0.3 * PREFERRED_WIDTH, 0.1 * PREFERRED_HEIGHT);
        lcd.setArcWidth(0.0125 * PREFERRED_HEIGHT);
        lcd.setArcHeight(0.0125 * PREFERRED_HEIGHT);
        lcd.relocate((PREFERRED_WIDTH - lcd.getWidth()) * 0.5, 0.44 * PREFERRED_HEIGHT);
        lcd.setManaged(getSkinnable().isLcdVisible());
        lcd.setVisible(getSkinnable().isLcdVisible());

        lcdText = new Label(String.format(locale, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getValue()));
        lcdText.setAlignment(Pos.CENTER_RIGHT);
        lcdText.setVisible(getSkinnable().isLcdVisible());

        // Set initial value
        angleStep          = ANGLE_RANGE / getSkinnable().getRange();
        double targetAngle = 180 - START_ANGLE + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle);
        needleRotate.setAngle(targetAngle);

        lightEffect = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.65), 2, 0.0, 0.0, 2.0);

        foreground = new SVGPath();
        foreground.setContent("M 26 26.5 C 26 20.2432 26.2432 20 32.5 20 L 277.5 20 C 283.7568 20 284 20.2432 284 26.5 L 284 143.5 C 284 149.7568 283.7568 150 277.5 150 L 32.5 150 C 26.2432 150 26 149.7568 26 143.5 L 26 26.5 ZM 0 6.7241 L 0 253.2758 C 0 260 0 260 6.75 260 L 303.25 260 C 310 260 310 260 310 253.2758 L 310 6.7241 C 310 0 310 0 303.25 0 L 6.75 0 C 0 0 0 0 0 6.7241 Z");
        foreground.setEffect(lightEffect);

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(ticksAndSectionsCanvas,
                                  ledCanvas,
                                  unitText,
                                  lcd,
                                  lcdText,
                                  shadowGroup,
                                  foreground,
                                  titleText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) c -> redraw());
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));

        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle());

        needleRotate.angleProperty().addListener(observable -> handleEvents("ANGLE"));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("ANGLE".equals(EVENT_TYPE)) {
            double currentValue = (needleRotate.getAngle() + START_ANGLE - 180) / angleStep + getSkinnable().getMinValue();
            lcdText.setText((String.format(locale, formatString, currentValue)));
            lcdText.setTranslateX((width - lcdText.getPrefWidth()) * 0.5);
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            if (getSkinnable().isLedVisible()) {
                ledCanvas.setManaged(true);
                ledCanvas.setVisible(true);
            } else {
                ledCanvas.setManaged(false);
                ledCanvas.setVisible(false);
            }
            if (getSkinnable().getTitle().isEmpty()) {
                titleText.setVisible(false);
                titleText.setManaged(false);
            } else {
                titleText.setManaged(true);
                titleText.setVisible(true);
            }
            if (getSkinnable().getUnit().isEmpty()) {
                unitText.setVisible(false);
                unitText.setManaged(false);
            } else {
                unitText.setManaged(true);
                unitText.setVisible(true);
            }
            if (getSkinnable().isLcdVisible()) {
                lcd.setManaged(true);
                lcd.setVisible(true);
                lcdText.setManaged(true);
                lcdText.setVisible(true);
            } else {
                lcd.setVisible(false);
                lcd.setManaged(false);
                lcdText.setVisible(false);
                lcdText.setManaged(false);
            }
            redraw();
        } else if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("LED".equals(EVENT_TYPE)) {
            if (getSkinnable().isLedVisible()) { drawLed(led); }
        } else if ("LCD".equals(EVENT_TYPE)) {
            if (getSkinnable().isLcdVisible()) redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            angleStep = getSkinnable().getAngleStep();
            needleRotate.setAngle((180 - START_ANGLE) + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
            if (getSkinnable().getValue() < getSkinnable().getMinValue()) {
                getSkinnable().setValue(getSkinnable().getMinValue());
                oldValue = getSkinnable().getMinValue();
            }
            if (getSkinnable().getValue() > getSkinnable().getMaxValue()) {
                getSkinnable().setValue(getSkinnable().getMaxValue());
                oldValue = getSkinnable().getMaxValue();
            }
            redraw();
        }
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle() {
        angleStep          = ANGLE_RANGE / (getSkinnable().getRange());
        double targetAngle = 180 - START_ANGLE + (getSkinnable().getCurrentValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle);
        needleRotate.setAngle(targetAngle);
    }

    private void drawTickMarks(final GraphicsContext CTX) {
        double     sinValue;
        double     cosValue;
        double     orthText         = TickLabelOrientation.ORTHOGONAL == getSkinnable().getTickLabelOrientation() ? 0.51 : 0.52;
        Point2D    center           = new Point2D(width * 0.5, height * 0.77);
        double     minorTickSpace   = getSkinnable().getMinorTickSpace();
        double     minValue         = getSkinnable().getMinValue();
        double     maxValue         = getSkinnable().getMaxValue();
        double     tmpAngleStep     = angleStep * minorTickSpace;
        int        decimals         = getSkinnable().getTickLabelDecimals();
        BigDecimal minorTickSpaceBD = BigDecimal.valueOf(minorTickSpace);
        BigDecimal majorTickSpaceBD = BigDecimal.valueOf(getSkinnable().getMajorTickSpace());
        BigDecimal mediumCheck2     = BigDecimal.valueOf(2 * minorTickSpace);
        BigDecimal mediumCheck5     = BigDecimal.valueOf(5 * minorTickSpace);
        BigDecimal counterBD        = BigDecimal.valueOf(minValue);
        double     counter          = minValue;

        Font tickLabelFont = Fonts.robotoCondensedRegular((decimals == 0 ? 0.045 : 0.038) * height);
        CTX.setFont(tickLabelFont);

        for (double angle = 0 ; Double.compare(-ANGLE_RANGE - tmpAngleStep, angle) < 0 ; angle -= tmpAngleStep) {
            sinValue = Math.sin(Math.toRadians(angle + START_ANGLE));
            cosValue = Math.cos(Math.toRadians(angle + START_ANGLE));

            Point2D innerPoint       = new Point2D(center.getX() + width * 0.41987097 * sinValue, center.getY() + width * 0.41987097 * cosValue);
            Point2D outerMinorPoint  = new Point2D(center.getX() + width * 0.45387097 * sinValue, center.getY() + width * 0.45387097 * cosValue);
            Point2D outerMediumPoint = new Point2D(center.getX() + width * 0.46387097 * sinValue, center.getY() + width * 0.46387097 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + width * 0.48387097 * sinValue, center.getY() + width * 0.48387097 * cosValue);
            Point2D textPoint        = new Point2D(center.getX() + width * orthText * sinValue, center.getY() + width * orthText * cosValue);

            CTX.setStroke(getSkinnable().getTickMarkColor());
            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw major tickmark
                CTX.setLineWidth(height * 0.0055);
                CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());

                // Draw text
                CTX.save();
                CTX.translate(textPoint.getX(), textPoint.getY());
                switch(getSkinnable().getTickLabelOrientation()) {
                    case ORTHOGONAL:
                        if ((360 - START_ANGLE - angle) % 360 > 90 && (360 - START_ANGLE - angle) % 360 < 270) {
                            CTX.rotate((180 - START_ANGLE - angle) % 360);
                        } else {
                            CTX.rotate((360 - START_ANGLE - angle) % 360);
                        }
                        break;
                    case TANGENT:
                        if ((360 - START_ANGLE - angle - 90) % 360 > 90 && (360 - START_ANGLE - angle - 90) % 360 < 270) {
                            CTX.rotate((90 - START_ANGLE - angle) % 360);
                        } else {
                            CTX.rotate((270 - START_ANGLE - angle) % 360);
                        }
                        break;
                    case HORIZONTAL:
                    default:
                        break;
                }
                CTX.setTextAlign(TextAlignment.CENTER);
                CTX.setTextBaseline(VPos.CENTER);
                CTX.setFill(getSkinnable().getTickLabelColor());
                CTX.fillText(String.format(locale, "%." + decimals + "f", counter), 0, 0);
                CTX.restore();
            } else if (getSkinnable().getMediumTickMarksVisible() &&
                       Double.compare(minorTickSpaceBD.remainder(mediumCheck2).doubleValue(), 0.0) != 0.0 &&
                       Double.compare(counterBD.remainder(mediumCheck5).doubleValue(), 0.0) == 0.0) {
                CTX.setLineWidth(height * 0.0035);
                CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerMediumPoint.getX(), outerMediumPoint.getY());
            } else if (getSkinnable().getMinorTickMarksVisible() && Double.compare(counterBD.remainder(minorTickSpaceBD).doubleValue(), 0.0) == 0) {
                CTX.setLineWidth(height * 0.00225);
                CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerMinorPoint.getX(), outerMinorPoint.getY());
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
        }
    }

    private void drawSections(final GraphicsContext CTX) {
        final double x         = width * 0.06;
        final double y         = width * 0.21;
        final double w         = width * 0.88;
        final double h         = height * 1.05;
        final double MIN_VALUE = getSkinnable().getMinValue();
        final double MAX_VALUE = getSkinnable().getMaxValue();
        final double OFFSET    = 90 - START_ANGLE;

        int listSize = getSkinnable().getSections().size();
        for (int i = 0 ; i < listSize ; i++) {
            final Section SECTION = getSkinnable().getSections().get(i);
            final double  SECTION_START_ANGLE;
            if (Double.compare(SECTION.getStart(), MAX_VALUE) <= 0 && Double.compare(SECTION.getStop(), MIN_VALUE) >= 0) {
                if (SECTION.getStart() < MIN_VALUE && SECTION.getStop() < MAX_VALUE) {
                    SECTION_START_ANGLE = 0;
                } else {
                    SECTION_START_ANGLE = (SECTION.getStart() - MIN_VALUE) * angleStep;
                }
                final double SECTION_ANGLE_EXTEND;
                if (SECTION.getStop() > MAX_VALUE) {
                    SECTION_ANGLE_EXTEND = (MAX_VALUE - SECTION.getStart()) * angleStep;
                } else {
                    SECTION_ANGLE_EXTEND = (SECTION.getStop() - SECTION.getStart()) * angleStep;
                }

                CTX.save();
                CTX.setStroke(SECTION.getColor());
                CTX.setLineWidth(height * 0.0415);
                CTX.setLineCap(StrokeLineCap.BUTT);
                CTX.strokeArc(x, y, w, h, -(OFFSET + SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.OPEN);
                CTX.restore();
            }
        }
    }

    private void drawLed(final GraphicsContext CTX) {
        CTX.clearRect(0, 0, ledSize, ledSize);
        CTX.setFill(frameGradient);
        CTX.fillOval(0, 0, ledSize, ledSize);

        CTX.save();
        if (getSkinnable().isLedOn()) {
            CTX.setEffect(ledOnShadow);
            CTX.setFill(ledOnGradient);
        } else {
            CTX.setEffect(ledOffShadow);
            CTX.setFill(ledOffGradient);
        }
        CTX.fillOval(0.14 * ledSize, 0.14 * ledSize, 0.72 * ledSize, 0.72 * ledSize);
        CTX.restore();

        CTX.setFill(highlightGradient);
        CTX.fillOval(0.21 * ledSize, 0.21 * ledSize, 0.58 * ledSize, 0.58 * ledSize);
    }

    private double clamp(final double MIN_VALUE, final double MAX_VALUE, final double VALUE) {
        if (VALUE < MIN_VALUE) return MIN_VALUE;
        if (VALUE > MAX_VALUE) return MAX_VALUE;
        return VALUE;
    }

    private void resizeText() {
        titleText.setFont(Fonts.robotoMedium(height * 0.11));
        titleText.setTranslateX((width - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(height * 0.76);

        unitText.setFont(Fonts.robotoMedium(height * 0.1));
        unitText.setTranslateX((width - unitText.getLayoutBounds().getWidth()) * 0.5);
        unitText.setTranslateY(height * 0.37);

        lcdText.setPadding(new Insets(0, 0.005 * width, 0, 0.005 * width));

        switch(getSkinnable().getLcdFont()) {
            case LCD:
                lcdText.setFont(Fonts.digital(0.108 * height));
                lcdText.setTranslateY(0.45 * height);
                break;
            case DIGITAL:
                lcdText.setFont(Fonts.digitalReadout(0.105 * height));
                lcdText.setTranslateY(0.44 * height);
                break;
            case DIGITAL_BOLD:
                lcdText.setFont(Fonts.digitalReadoutBold(0.105 * height));
                lcdText.setTranslateY(0.44 * height);
                break;
            case ELEKTRA:
                lcdText.setFont(Fonts.elektra(0.1116 * height));
                lcdText.setTranslateY(0.435 * height);
                break;
            case STANDARD:
            default:
                lcdText.setFont(Fonts.robotoMedium(0.09 * height));
                lcdText.setTranslateY(0.43 * height);
                break;
        }
        lcdText.setPrefSize(0.3 * width, 0.014 * height);
        lcdText.setTranslateX((width - lcdText.getPrefWidth()) * 0.5);
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            dropShadow.setRadius(0.01 * height);
            dropShadow.setOffsetY(0.01 * height);

            ticksAndSectionsCanvas.setWidth(width);
            ticksAndSectionsCanvas.setHeight(height);

            lcd.setWidth(0.3 * width);
            lcd.setHeight(0.1 * height);
            lcd.setArcWidth(0.0125 * height);
            lcd.setArcHeight(0.0125 * height);
            lcd.relocate((width - lcd.getWidth()) * 0.5, 0.44 * height);

            ledSize = 0.06 * height;
            final Color LED_COLOR = getSkinnable().getLedColor();
            frameGradient = new LinearGradient(0.14 * ledSize, 0.14 * ledSize,
                                               0.84 * ledSize, 0.84 * ledSize,
                                               false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, Color.rgb(20, 20, 20, 0.65)),
                                               new Stop(0.15, Color.rgb(20, 20, 20, 0.65)),
                                               new Stop(0.26, Color.rgb(41, 41, 41, 0.65)),
                                               new Stop(0.26, Color.rgb(41, 41, 41, 0.64)),
                                               new Stop(0.85, Color.rgb(200, 200, 200, 0.41)),
                                               new Stop(1.0, Color.rgb(200, 200, 200, 0.35)));

            ledOnGradient = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                               0.74 * ledSize, 0.74 * ledSize,
                                               false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.77, 1.0)),
                                               new Stop(0.49, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)),
                                               new Stop(1.0, LED_COLOR));

            ledOffGradient = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                0.74 * ledSize, 0.74 * ledSize,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.20, 1.0)),
                                                new Stop(0.49, LED_COLOR.deriveColor(0.0, 1.0, 0.13, 1.0)),
                                                new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.2, 1.0)));

            highlightGradient = new RadialGradient(0, 0,
                                                   0.3 * ledSize, 0.3 * ledSize,
                                                   0.29 * ledSize,
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.WHITE),
                                                   new Stop(1.0, Color.TRANSPARENT));

            ledCanvas.setWidth(ledSize);
            ledCanvas.setHeight(ledSize);
            ledCanvas.relocate(0.11 * width, 0.10 * height);
            ledOffShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            ledOnShadow  = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, getSkinnable().getLedColor(), 0.36 * ledSize, 0, 0, 0));


            double needleWidth  = height * 0.015;
            double needleHeight = height * 0.58;

            needle.setCache(false);

            needleMoveTo1.setX(0.25 * needleWidth); needleMoveTo1.setY(0.025423728813559324 * needleHeight);

            needleCubicCurveTo2.setControlX1(0.25 * needleWidth); needleCubicCurveTo2.setControlY1(0.00847457627118644 * needleHeight);
            needleCubicCurveTo2.setControlX2(0.375 * needleWidth); needleCubicCurveTo2.setControlY2(0);
            needleCubicCurveTo2.setX(0.5 * needleWidth); needleCubicCurveTo2.setY(0);

            needleCubicCurveTo3.setControlX1(0.625 * needleWidth); needleCubicCurveTo3.setControlY1(0);
            needleCubicCurveTo3.setControlX2(0.75 * needleWidth); needleCubicCurveTo3.setControlY2(0.00847457627118644 * needleHeight);
            needleCubicCurveTo3.setX(0.75 * needleWidth); needleCubicCurveTo3.setY(0.025423728813559324 * needleHeight);

            needleCubicCurveTo4.setControlX1(0.75 * needleWidth); needleCubicCurveTo4.setControlY1(0.025423728813559324 * needleHeight);
            needleCubicCurveTo4.setControlX2(needleWidth); needleCubicCurveTo4.setControlY2(needleHeight);
            needleCubicCurveTo4.setX(needleWidth); needleCubicCurveTo4.setY(needleHeight);

            needleLineTo5.setX(0); needleLineTo5.setY(needleHeight);

            needleCubicCurveTo6.setControlX1(0); needleCubicCurveTo6.setControlY1(needleHeight);
            needleCubicCurveTo6.setControlX2(0.25 * needleWidth); needleCubicCurveTo6.setControlY2(0.025423728813559324 * needleHeight);
            needleCubicCurveTo6.setX(0.25 * needleWidth); needleCubicCurveTo6.setY(0.025423728813559324 * needleHeight);

            needle.setCache(true);
            needle.setCacheHint(CacheHint.ROTATE);

            LinearGradient needleGradient = new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                                               needle.getLayoutBounds().getMaxX(), 0,
                                                               false, CycleMethod.NO_CYCLE,
                                                               new Stop(0.0, getSkinnable().getNeedleColor()),
                                                               new Stop(0.5, getSkinnable().getNeedleColor()),
                                                               new Stop(0.5, getSkinnable().getNeedleColor().brighter().brighter()),
                                                               new Stop(1.0, getSkinnable().getNeedleColor().brighter().brighter()));

            needle.setFill(needleGradient);
            needle.setStrokeWidth(0);
            needle.setStroke(Color.TRANSPARENT);

            needle.relocate((width - needle.getLayoutBounds().getWidth()) * 0.5, height * 0.77 - needle.getLayoutBounds().getHeight());
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight());

            lightEffect.setRadius(Helper.clamp(0.5, 1.5, 0.00769231 * height));
            lightEffect.setOffsetY(Helper.clamp(0.5, 1.5, 0.00769231 * height));
            foreground.setScaleX(width / PREFERRED_WIDTH);
            foreground.setScaleY(height / PREFERRED_HEIGHT);
            foreground.setTranslateX((width - PREFERRED_WIDTH) * 0.5);
            foreground.setTranslateY((height - PREFERRED_HEIGHT) * 0.5);

            resizeText();
        }
    }

    private void redraw() {
        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();

        Color backgroundColor = getSkinnable().getBackgroundPaint() instanceof Color ? (Color) getSkinnable().getBackgroundPaint() : Color.WHITE;
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, width, height);
        ticksAndSections.setFill(new LinearGradient(0, 0, 0, height, false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, Color.TRANSPARENT),
                                                    new Stop(0.07692308, backgroundColor.deriveColor(0.0, 1.0, 0.706, 1.0)),
                                                    new Stop(0.08461538, backgroundColor.deriveColor(0.0, 1.0, 0.921, 1.0)),
                                                    new Stop(0.56923077, backgroundColor),
                                                    new Stop(0.579, backgroundColor.deriveColor(0.0, 1.0, 0.706, 1.0)),
                                                    new Stop(1.0, Color.TRANSPARENT)));

        ticksAndSections.fillRect(0, 0, width, height);
        if (getSkinnable().getSectionsVisible()) drawSections(ticksAndSections);
        drawTickMarks(ticksAndSections);
        ticksAndSectionsCanvas.setCache(true);
        ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

        titleText.setFill(getSkinnable().getTitleColor());
        unitText.setFill(getSkinnable().getUnitColor());
        if (getSkinnable().isLcdVisible()) {
            LcdDesign lcdDesign = getSkinnable().getLcdDesign();
            Color[] lcdColors = lcdDesign.getColors();
            LinearGradient lcdGradient = new LinearGradient(0, 1, 0, lcd.getHeight() - 1,
                                                            false, CycleMethod.NO_CYCLE,
                                                            new Stop(0, lcdColors[0]),
                                                            new Stop(0.03, lcdColors[1]),
                                                            new Stop(0.5, lcdColors[2]),
                                                            new Stop(0.5, lcdColors[3]),
                                                            new Stop(1.0, lcdColors[4]));
            Paint lcdFramePaint;
            if (lcdDesign.name().startsWith("FLAT")) {
                lcdFramePaint = Color.WHITE;
            } else {
                lcdFramePaint = new LinearGradient(0, 0, 0, lcd.getHeight(),
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(26, 26, 26)),
                                                   new Stop(0.01, Color.rgb(77, 77, 77)),
                                                   new Stop(0.99, Color.rgb(77, 77, 77)),
                                                   new Stop(1.0, Color.rgb(221, 221, 221)));
            }
            lcd.setFill(lcdGradient);
            lcd.setStroke(lcdFramePaint);

            lcdText.setTextFill(lcdColors[5]);
        }

        if (getSkinnable().isLedVisible()) drawLed(led);

        shadowGroup.setEffect(getSkinnable().isShadowsEnabled() ? dropShadow : null);

        foreground.setFill(getSkinnable().getForegroundPaint());
    }
}
