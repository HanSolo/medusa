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

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import java.util.List;
import java.util.Locale;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static eu.hansolo.medusa.tools.Helper.clamp;
import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 29.01.16.
 */
public class LinearSkin extends GaugeSkinBase {
    protected static final double MINIMUM_WIDTH   = 100;
    protected static final double MINIMUM_HEIGHT  = 100;
    protected static final double MAXIMUM_WIDTH   = 1024;
    protected static final double MAXIMUM_HEIGHT  = 1024;
    private double                preferredWidth  = 140;
    private double                preferredHeight = 350;
    private double                aspectRatio     = 2.5;
    private double                size;
    private double                width;
    private double                height;
    private double                stepSize;
    private Pane                  pane;
    private Orientation           orientation;
    private Canvas                ticksAndSectionsCanvas;
    private GraphicsContext       ticksAndSections;
    private Text                  unitText;
    private Text                  titleText;
    private Text                  valueText;
    private Line                  barBorder1;
    private Line                  barBorder2;
    private Rectangle             barBackground;
    private Rectangle             bar;
    private Rectangle             barHighlight;
    private double                ledSize;
    private InnerShadow           ledOnShadow;
    private InnerShadow           ledOffShadow;
    private Paint                 ledFramePaint;
    private Paint                 ledOnPaint;
    private Paint                 ledOffPaint;
    private Paint                 ledHighlightPaint;
    private Canvas                ledCanvas;
    private GraphicsContext       led;
    private Rectangle             lcd;
    private String                tickLabelFormatString;
    private Locale                locale;
    private double                minValuePosition;
    private double                maxValuePosition;
    private double                zeroPosition;
    private List<Section>         sections;
    private List<Section>         areas;
    private InvalidationListener  currentValueListener;
    private InvalidationListener  paneSizeListener;


    // ******************** Constructors **************************************
    public LinearSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        orientation           = gauge.getOrientation();
        tickLabelFormatString = new StringBuilder("%.").append(Integer.toString(gauge.getTickLabelDecimals())).append("f").toString();
        locale                = gauge.getLocale();
        sections              = gauge.getSections();
        areas                 = gauge.getAreas();
        currentValueListener  = o -> setBar(gauge.getCurrentValue());
        paneSizeListener      = o -> handleEvents("RESIZE");

        if (Orientation.VERTICAL == orientation) {
            preferredWidth  = 140;
            preferredHeight = 350;
        } else {
            preferredWidth  = 350;
            preferredHeight = 140;
        }

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // Set initial size
        if (Double.compare(gauge.getPrefWidth(), 0.0) <= 0 || Double.compare(gauge.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(gauge.getWidth(), 0.0) <= 0 || Double.compare(gauge.getHeight(), 0.0) <= 0) {
            if (gauge.getPrefWidth() > 0 && gauge.getPrefHeight() > 0) {
                gauge.setPrefSize(gauge.getPrefWidth(), gauge.getPrefHeight());
            } else {
                gauge.setPrefSize(preferredWidth, preferredHeight);
            }
        }

        barBorder1 = new Line();
        barBorder2 = new Line();

        barBackground = new Rectangle();

        ticksAndSectionsCanvas = new Canvas(preferredWidth, preferredHeight);
        ticksAndSections       = ticksAndSectionsCanvas.getGraphicsContext2D();

        ledCanvas = new Canvas();
        led       = ledCanvas.getGraphicsContext2D();
        Helper.enableNode(ledCanvas, gauge.isLedVisible());

        lcd = new Rectangle(0.3 * preferredWidth, 0.014 * preferredHeight);
        lcd.setArcWidth(0.0125 * preferredHeight);
        lcd.setArcHeight(0.0125 * preferredHeight);
        lcd.relocate((preferredWidth - lcd.getWidth()) * 0.5, 0.44 * preferredHeight);
        Helper.enableNode(lcd, gauge.isLcdVisible() && gauge.isValueVisible());

        bar = new Rectangle();
        bar.setStroke(null);

        barHighlight = new Rectangle();
        barHighlight.setStroke(null);
        Helper.enableNode(barHighlight, gauge.isBarEffectEnabled());

        titleText = new Text(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        unitText  = new Text(gauge.getUnit());
        unitText.setFill(gauge.getUnitColor());
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()));
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

        pane = new Pane(barBorder1,
                        barBorder2,
                        barBackground,
                        ticksAndSectionsCanvas,
                        titleText,
                        unitText,
                        ledCanvas,
                        lcd,
                        valueText,
                        bar,
                        barHighlight);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
        pane.widthProperty().addListener(paneSizeListener);
        pane.heightProperty().addListener(paneSizeListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("FINISHED".equals(EVENT_TYPE)) {
            if ( gauge.isHighlightSections() ) {
                redraw();
            }
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(ledCanvas, gauge.isLedVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(lcd, (gauge.isLcdVisible() && gauge.isValueVisible()));
            Helper.enableNode(barHighlight, gauge.isBarEffectEnabled());
            resize();
            redraw();
        } else if ("LED".equals(EVENT_TYPE)) {
            if (gauge.isLedVisible()) { drawLed(); }
        } else if ("LCD".equals(EVENT_TYPE)) {
            if (gauge.isLcdVisible()) redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = gauge.getSections();
            areas    = gauge.getAreas();
            resize();
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            orientation = gauge.getOrientation();
            if (Orientation.VERTICAL == orientation) {
                width    = height / aspectRatio;
//                stepSize = Math.abs(0.67143 * height / gauge.getRange());
            } else {
                height   = width / aspectRatio;
//                stepSize = Math.abs(0.75 * width / gauge.getRange());
            }
            resize();
            redraw();
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        pane.widthProperty().removeListener(paneSizeListener);
        pane.heightProperty().removeListener(paneSizeListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void drawTickMarks(final GraphicsContext CTX) {
        if (Double.compare(stepSize, 0) <= 0) return;

        CTX.setFont(Fonts.robotoLight(0.06 * size));
        CTX.setStroke(gauge.getTickMarkColor());
        CTX.setFill(gauge.getTickLabelColor());

        Point2D innerPoint;
        Point2D innerMediumPoint;
        Point2D innerMinorPoint;
        Point2D outerPoint;
        Point2D textPoint;

        double minPosition;
        double maxPosition;
        if (Orientation.VERTICAL == orientation) {
            minPosition = barBackground.getLayoutY() + size * 0.0035;
            maxPosition = barBackground.getLayoutY() + barBackground.getLayoutBounds().getHeight();
        } else {
            minPosition = barBackground.getLayoutX();
            maxPosition = barBackground.getLayoutX() + barBackground.getLayoutBounds().getWidth();
        }

        double anchorX        = barBackground.getLayoutX() - 0.075 * width;
        double anchorY        = barBackground.getLayoutY() + barBackground.getHeight() + 0.075 * height;
        double majorTickSpace = gauge.getMajorTickSpace();
        double minorTickSpace = gauge.getMinorTickSpace();
        double minValue       = gauge.getMinValue();
        double maxValue       = gauge.getMaxValue();

        if (gauge.getSectionsVisible()) drawSections(CTX);

        int counter = 0;
        for (double i = minPosition ; Double.compare(i, maxPosition + 1) <= 0 ; i += stepSize) {
            if (Orientation.VERTICAL == orientation) {
                innerPoint       = new Point2D(anchorX, i);
                innerMediumPoint = new Point2D(anchorX + 0.015 * width, i);
                innerMinorPoint  = new Point2D(anchorX + 0.03 * width, i);
                outerPoint       = new Point2D(anchorX + 0.05 * width, i);
                textPoint        = new Point2D(anchorX - 0.02 * width, i);
            } else {
                innerPoint       = new Point2D(i, anchorY);
                innerMediumPoint = new Point2D(i, anchorY - 0.015 * height);
                innerMinorPoint  = new Point2D(i, anchorY - 0.03 * height);
                outerPoint       = new Point2D(i, anchorY - 0.05 * height);
                textPoint        = new Point2D(i, anchorY + 0.05 * height);
            }

            if (counter % majorTickSpace == 0) {
                // Draw major tickmark
                CTX.setLineWidth(size * 0.007);
                CTX.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());

                // Draw text
                CTX.setTextBaseline(VPos.CENTER);
                if (Orientation.VERTICAL == orientation) {
                    CTX.setTextAlign(TextAlignment.RIGHT);
                    CTX.fillText(String.format(locale, tickLabelFormatString, (maxValue -= majorTickSpace) + majorTickSpace), textPoint.getX(), textPoint.getY());
                } else {
                    CTX.setTextAlign(TextAlignment.CENTER);
                    CTX.fillText(String.format(locale, tickLabelFormatString, (minValue += majorTickSpace) - majorTickSpace), textPoint.getX(), textPoint.getY());
                }
            } else if (minorTickSpace % 2 != 0 && counter % 5 == 0) {
                CTX.setLineWidth(size * 0.006);
                CTX.strokeLine(innerMediumPoint.getX(), innerMediumPoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (counter % minorTickSpace == 0) {
                CTX.setLineWidth(size * 0.005);
                CTX.strokeLine(innerMinorPoint.getX(), innerMinorPoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
            counter++;
        }
    }

    private void drawSections(final GraphicsContext CTX) {
        if (!gauge.getSectionsVisible() && sections.isEmpty()) return;
        double value    = gauge.getCurrentValue();
        int    listSize = sections.size();
        double minValue = gauge.getMinValue();
        double minPosition;
        CTX.save();
        if (Orientation.VERTICAL == orientation) {
            minPosition    = barBackground.getLayoutY() + barBackground.getLayoutBounds().getHeight() - size * 0.0035;
            double anchorX = barBackground.getLayoutX() - 0.079 * width;
            double sectionHeight;
            for (int i = 0 ; i < listSize ;i++) {
                Section section = sections.get(i);
                sectionHeight   = (section.getStop() - section.getStart()) * stepSize;
                if ( gauge.isHighlightSections() ) {
                    CTX.setFill(section.contains(value) ? section.getHighlightColor() : section.getColor());
                } else {
                    CTX.setFill(section.getColor());
                }
                CTX.fillRect(anchorX, minPosition - sectionHeight - (section.getStart() - minValue) * stepSize, 0.057 * width, sectionHeight);
            }
        } else {
            minPosition    = barBackground.getLayoutX();
            double anchorY = barBackground.getLayoutY() + barBackground.getHeight() + 0.021 * height;
            double sectionWidth;
            for (int i = 0 ; i < listSize ;i++) {
                Section section = sections.get(i);
                sectionWidth    = (section.getStop() - section.getStart()) * stepSize;
                if ( gauge.isHighlightSections() ) {
                    CTX.setFill(section.contains(value) ? section.getHighlightColor() : section.getColor());
                } else {
                    CTX.setFill(section.getColor());
                }
                CTX.fillRect(minPosition + (section.getStart() - minValue) * stepSize, anchorY, sectionWidth, 0.059 * height);
            }
        }
        CTX.restore();
    }

    private void setBar(final double VALUE) {

        double maxValue = ( gauge.getMaxValue() - gauge.getMinValue() ) * stepSize;

        if (Orientation.VERTICAL == orientation) {

            double valueHeight = 0;
            double layoutY = 0;

            if (gauge.isStartFromZero()) {

                double maxV = gauge.getMaxValue();
                double maxY = maxV * stepSize;
                double minV = gauge.getMinValue();
                double minY = minV * stepSize;
                double valY = VALUE * stepSize;

                if ( ( valY > minY || minY < 0 ) && ( valY < maxY || maxY > 0 ) ) {

                    valY = clamp(minY, maxY, valY);

                    if ( maxY < 0 ) {
                        layoutY = - maxY;
                        valueHeight = maxY - valY;
                    } else if ( minY > 0 ) {
                        layoutY = - valY;
                        valueHeight = valY - minY;
                    } else if ( valY < 0 ) {
                        layoutY = 0;
                        valueHeight = - valY;
                    } else {
                        layoutY = - valY;
                        valueHeight = valY;
                    }

                }

            } else {
                valueHeight = clamp(0, maxValue, ( VALUE - gauge.getMinValue() ) * stepSize);
                layoutY = -valueHeight;
            }

            bar.setLayoutY(layoutY);
            bar.setHeight(valueHeight);
            barHighlight.setLayoutY(layoutY);
            barHighlight.setHeight(valueHeight);

            valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));

            if (gauge.isLcdVisible()) {
                valueText.setLayoutX((0.88 * width - valueText.getLayoutBounds().getWidth()));
            } else {
                valueText.setLayoutX((width - valueText.getLayoutBounds().getWidth()) * 0.5);
            }

        } else {

            double valueWidth = 0;
            double layoutX = 0;

            if (gauge.isStartFromZero()) {

                double maxV = gauge.getMaxValue();
                double maxX = maxV * stepSize;
                double minV = gauge.getMinValue();
                double minX = minV * stepSize;
                double valX = VALUE * stepSize;

                if ( ( valX > minX || minX < 0 ) && ( valX < maxX || maxX > 0 ) ) {

                    valX = clamp(minX, maxX, valX);

                    if ( maxX < 0 ) {
                        layoutX = valX;
                        valueWidth = maxX - valX;
                    } else if ( minX > 0 ) {
                        layoutX = minX;
                        valueWidth = valX - minX;
                    } else if ( valX < 0 ) {
                        layoutX = valX;
                        valueWidth = - valX;
                    } else {
                        layoutX = 0;
                        valueWidth = valX;
                    }

                }

            } else {
                valueWidth = clamp(0, maxValue, ( VALUE - gauge.getMinValue() ) * stepSize);
            }

            bar.setLayoutX(layoutX);
            bar.setWidth(valueWidth);
            barHighlight.setLayoutX(layoutX);
            barHighlight.setWidth(valueWidth);

            valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
            valueText.setLayoutX(( 0.98 * width - valueText.getLayoutBounds().getWidth() ));

        }

        setBarColor(VALUE);

    }
    private void setBarColor(final double VALUE) {
        if (!gauge.getAreasVisible() && !gauge.isGradientBarEnabled()) {
            bar.setFill(gauge.getBarColor());
        } else if (gauge.isGradientBarEnabled() && gauge.getGradientBarStops().size() > 1) {
            bar.setFill(gauge.getGradientLookup().getColorAt((VALUE - gauge.getMinValue()) / gauge.getRange()));
        } else {
            bar.setFill(gauge.getBarColor());
            int listSize = areas.size();
            for (int i = 0 ; i < listSize ; i++) {
                Section area = areas.get(i);
                if (area.contains(VALUE)) {
                    bar.setFill(area.getColor());
                    break;
                }
            }
        }
    }

    private void drawLed() {
        led.clearRect(0, 0, ledSize, ledSize);

        boolean isFlatLed = LedType.FLAT == gauge.getLedType();

        if (!isFlatLed) {
            led.setFill(ledFramePaint);
            led.fillOval(0, 0, ledSize, ledSize);
        } else {
            double lineWidth = 0.0037037 * size;
            led.setStroke(ledFramePaint);
            led.setLineWidth(lineWidth);
            led.strokeOval(lineWidth, lineWidth, ledSize - 2 * lineWidth, ledSize - 2 * lineWidth);
        }

        led.save();
        if (gauge.isLedOn()) {
            led.setEffect(ledOnShadow);
            led.setFill(ledOnPaint);
        } else {
            led.setEffect(ledOffShadow);
            led.setFill(ledOffPaint);
        }
        if (isFlatLed) {
            led.fillOval(0.2 * ledSize, 0.2 * ledSize, 0.6 * ledSize, 0.6 * ledSize);
        } else {
            led.fillOval(0.14 * ledSize, 0.14 * ledSize, 0.72 * ledSize, 0.72 * ledSize);
        }
        led.restore();

        led.setFill(ledHighlightPaint);
        led.fillOval(0.21 * ledSize, 0.21 * ledSize, 0.58 * ledSize, 0.58 * ledSize);
    }

    private void resizeText() {
        if (Orientation.VERTICAL == orientation) {
            double maxWidth = width * 0.95;
            double fontSize = width * 0.13;

            titleText.setFont(Fonts.robotoRegular(fontSize));
            titleText.setText(gauge.getTitle());
            Helper.adjustTextSize(titleText, maxWidth, fontSize);
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0);

            fontSize = width * 0.08;
            unitText.setFont(Fonts.robotoRegular(fontSize));
            unitText.setText(gauge.getUnit());
            Helper.adjustTextSize(unitText, maxWidth, fontSize);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.075 * height);

            if (gauge.isLcdVisible()) {
                switch(gauge.getLcdFont()) {
                    case LCD:
                        valueText.setFont(Fonts.digital(0.24 * width));
                        valueText.relocate((0.88 * width - valueText.getLayoutBounds().getWidth()), 0.877 * height);
                        break;
                    case DIGITAL:
                        valueText.setFont(Fonts.digitalReadout(0.225 * width));
                        valueText.relocate((0.88 * width - valueText.getLayoutBounds().getWidth()), 0.875 * height);
                        break;
                    case DIGITAL_BOLD:
                        valueText.setFont(Fonts.digitalReadoutBold(0.225 * width));
                        valueText.relocate((0.88 * width - valueText.getLayoutBounds().getWidth()), 0.875 * height);
                        break;
                    case ELEKTRA:
                        valueText.setFont(Fonts.elektra(0.24 * width));
                        valueText.relocate((0.88 * width - valueText.getLayoutBounds().getWidth()), 0.867 * height);
                        break;
                    case STANDARD:
                    default:
                        valueText.setFont(Fonts.robotoMedium(0.22 * width));
                        valueText.relocate((0.88 * width - valueText.getLayoutBounds().getWidth()), 0.855 * height);
                        break;
                }
            } else {
                valueText.setFont(Fonts.robotoRegular(0.17 * width));
                valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, height * 0.877);
            }
        } else {
            double maxWidth = width * 0.8;
            double fontSize = height * 0.15;

            titleText.setFont(Fonts.robotoRegular(fontSize));
            titleText.setText(gauge.getTitle());
            Helper.adjustTextSize(titleText, maxWidth, fontSize);
            titleText.relocate(0.03571429 * height, 0.03571429 * height);

            fontSize = height * 0.1;
            unitText.setFont(Fonts.robotoRegular(fontSize));
            unitText.setText(gauge.getUnit());
            Helper.adjustTextSize(unitText, maxWidth, fontSize);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.8 * height);

            if (gauge.isLcdVisible()) {
                switch(gauge.getLcdFont()) {
                    case LCD:
                        valueText.setFont(Fonts.digital(0.24 * height));
                        valueText.relocate((0.98 * width - valueText.getLayoutBounds().getWidth()), 0.052 * height);
                        break;
                    case DIGITAL:
                        valueText.setFont(Fonts.digitalReadout(0.225 * height));
                        valueText.relocate((0.98 * width - valueText.getLayoutBounds().getWidth()), 0.05 * height);
                        break;
                    case DIGITAL_BOLD:
                        valueText.setFont(Fonts.digitalReadoutBold(0.225 * height));
                        valueText.relocate((0.98 * width - valueText.getLayoutBounds().getWidth()), 0.05 * height);
                        break;
                    case ELEKTRA:
                        valueText.setFont(Fonts.elektra(0.24 * height));
                        valueText.relocate((0.98 * width - valueText.getLayoutBounds().getWidth()), 0.042 * height);
                        break;
                    case STANDARD:
                    default:
                        valueText.setFont(Fonts.robotoMedium(0.22 * height));
                        valueText.relocate((0.98 * width - valueText.getLayoutBounds().getWidth()), 0.03 * height);
                        break;
                }
            } else {
                valueText.setFont(Fonts.robotoRegular(0.17 * height));
                valueText.relocate((0.98 * width - valueText.getLayoutBounds().getWidth()), 0.03 * height);
            }
        }
    }

    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();

        if (width > 0 && height > 0) {
            orientation = gauge.getOrientation();

            double  currentValue   = gauge.getCurrentValue();
            Color   tickMarkColor  = gauge.getTickMarkColor();
            Color   barBorderColor = Color.color(tickMarkColor.getRed(), tickMarkColor.getGreen(), tickMarkColor.getBlue(), 0.5);
            boolean isFlatLed      = LedType.FLAT == gauge.getLedType();

            if (Orientation.VERTICAL == orientation) {
                width    = height / aspectRatio;
                size     = width < height ? width : height;
                stepSize = Math.abs((0.66793 * height) / gauge.getRange());

                pane.setMaxSize(width, height);
                pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

                width  = pane.getLayoutBounds().getWidth();
                height = pane.getLayoutBounds().getHeight();

                barBackground.setWidth(0.14286 * width);
                barBackground.setHeight(0.67143 * height);
                barBackground.relocate((width - barBackground.getWidth()) * 0.5, (height - barBackground.getHeight()) * 0.5);
                barBackground.setStroke(null);
                barBackground.setFill(new LinearGradient(0, barBackground.getLayoutBounds().getMinY(),
                                                         0, barBackground.getLayoutBounds().getMaxY(),
                                                         false, CycleMethod.NO_CYCLE,
                                                         new Stop(0.0, Color.rgb(255, 255, 255, 0.05)),
                                                         new Stop(0.5, Color.rgb(255, 255, 255, 0.15)),
                                                         new Stop(1.0, Color.rgb(255, 255, 255, 0.05))));

                minValuePosition = barBackground.getLayoutY() + barBackground.getLayoutBounds().getHeight();
                maxValuePosition = barBackground.getLayoutY();
                zeroPosition     = minValuePosition + gauge.getMinValue() * stepSize;

                barBorder1.setStartX(barBackground.getLayoutX() - 1);
                barBorder1.setStartY(maxValuePosition);
                barBorder1.setEndX(barBackground.getLayoutX() - 1);
                barBorder1.setEndY(minValuePosition);
                barBorder2.setStartX(barBackground.getLayoutX() + barBackground.getLayoutBounds().getWidth() + 1);
                barBorder2.setStartY(maxValuePosition);
                barBorder2.setEndX(barBackground.getLayoutX() + barBackground.getLayoutBounds().getWidth() + 1);
                barBorder2.setEndY(minValuePosition);

                barBorder1.setStroke(barBorderColor);
                barBorder2.setStroke(barBorderColor);

                bar.setWidth(0.14286 * width);
                bar.setLayoutX(0);
                bar.setLayoutY(0);
                bar.setTranslateX((width - bar.getWidth()) * 0.5);
                bar.setTranslateY(gauge.isStartFromZero() ? zeroPosition : minValuePosition);

                barHighlight.setWidth(bar.getWidth());
                barHighlight.setLayoutX(0);
                barHighlight.setLayoutY(0);
                barHighlight.setTranslateX(bar.getTranslateX());
                barHighlight.setTranslateY(bar.getTranslateY());

                setBar(currentValue);

                ticksAndSectionsCanvas.setCache(false);
                ticksAndSectionsCanvas.setWidth(height / aspectRatio);
                ticksAndSectionsCanvas.setHeight(height);
                ticksAndSections.clearRect(0, 0, ticksAndSectionsCanvas.getWidth(), ticksAndSectionsCanvas.getHeight());
                drawTickMarks(ticksAndSections);
                ticksAndSectionsCanvas.setCache(true);
                ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

                ledSize = isFlatLed ? 0.08 * width : 0.09 * width;
                ledCanvas.setWidth(ledSize);
                ledCanvas.setHeight(ledSize);
                ledCanvas.relocate((width - ledSize) * 0.5, 0.12 * height);
                ledOffShadow = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
                ledOnShadow  = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
                if (!isFlatLed) ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, gauge.getLedColor(), 0.36 * ledSize, 0, 0, 0));

                if (gauge.isLcdVisible()) {
                    lcd.setWidth(0.8 * width);
                    lcd.setHeight(0.22 * width);
                    lcd.setArcWidth(0.0125 * size);
                    lcd.setArcHeight(0.0125 * size);
                    lcd.relocate((width - lcd.getWidth()) * 0.5, 0.87 * height);
                }
            } else {
                height   = width / aspectRatio;
                size     = width < height ? width : height;
                stepSize = Math.abs(0.9 * width / gauge.getRange());

                pane.setMaxSize(width, height);
                pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

                width  = pane.getLayoutBounds().getWidth();
                height = pane.getLayoutBounds().getHeight();

                barBackground.setWidth(0.9 * width);
                barBackground.setHeight(0.14286 * height);
                barBackground.relocate((width - barBackground.getWidth()) * 0.5, (height - barBackground.getHeight()) * 0.5);
                barBackground.setStroke(null);
                barBackground.setFill(new LinearGradient(barBackground.getLayoutBounds().getMinX(), 0,
                                                         barBackground.getLayoutBounds().getMaxX(), 0,
                                                         false, CycleMethod.NO_CYCLE,
                                                         new Stop(0.0, Color.rgb(255, 255, 255, 0.05)),
                                                         new Stop(0.5, Color.rgb(255, 255, 255, 0.15)),
                                                         new Stop(1.0, Color.rgb(255, 255, 255, 0.05))));

                minValuePosition = barBackground.getLayoutX();
                maxValuePosition = barBackground.getLayoutX() + barBackground.getLayoutBounds().getWidth();
                zeroPosition     = minValuePosition - gauge.getMinValue() * stepSize;

                barBorder1.setStartX(minValuePosition);
                barBorder1.setStartY(barBackground.getLayoutY() - 1);
                barBorder1.setEndX(maxValuePosition);
                barBorder1.setEndY(barBackground.getLayoutY() - 1);
                barBorder2.setStartX(minValuePosition);
                barBorder2.setStartY(barBackground.getLayoutY() + barBackground.getLayoutBounds().getHeight() + 1);
                barBorder2.setEndX(maxValuePosition);
                barBorder2.setEndY(barBackground.getLayoutY() + barBackground.getLayoutBounds().getHeight() + 1);

                barBorder1.setStroke(barBorderColor);
                barBorder2.setStroke(barBorderColor);

                bar.setHeight(0.14286 * height);
                bar.setLayoutX(0);
                bar.setLayoutY(0);
                bar.setTranslateX(gauge.isStartFromZero() ? zeroPosition : minValuePosition);
                bar.setTranslateY((height - bar.getHeight()) * 0.5);

                barHighlight.setHeight(bar.getHeight());
                barHighlight.setLayoutX(0);
                barHighlight.setLayoutY(0);
                barHighlight.setTranslateX(bar.getTranslateX());
                barHighlight.setTranslateY(bar.getTranslateY());

                setBar(currentValue);

                ticksAndSectionsCanvas.setCache(false);
                ticksAndSectionsCanvas.setWidth(width);
                ticksAndSectionsCanvas.setHeight(height);
                ticksAndSections.clearRect(0, 0, ticksAndSectionsCanvas.getWidth(), ticksAndSectionsCanvas.getHeight());
                drawTickMarks(ticksAndSections);
                ticksAndSectionsCanvas.setCache(true);
                ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

                ledSize = isFlatLed ? 0.08 * height : 0.09 * height;
                ledCanvas.setWidth(ledSize);
                ledCanvas.setHeight(ledSize);
                ledCanvas.relocate(0.955 * width, (height - ledSize) * 0.5);
                ledOffShadow = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
                ledOnShadow  = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
                if (!isFlatLed) ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, gauge.getLedColor(), 0.36 * ledSize, 0, 0, 0));

                if (gauge.isLcdVisible()) {
                    lcd.setWidth(0.3 * width);
                    lcd.setHeight(0.22 * height);
                    lcd.setArcWidth(0.0125 * size);
                    lcd.setArcHeight(0.0125 * size);
                    lcd.relocate((width - lcd.getWidth()) - 0.03571429 * height, 0.03571429 * height);
                }
            }

            resizeText();
        }
    }

    @Override protected void redraw() {
        locale                = gauge.getLocale();
        tickLabelFormatString = new StringBuilder("%.").append(Integer.toString(gauge.getTickLabelDecimals())).append("f").toString();

        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(Orientation.HORIZONTAL == orientation ? gauge.getBorderWidth() / preferredHeight * height : gauge.getBorderWidth() / preferredWidth * width))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        if (gauge.getAreasVisible()) {
            setBarColor(gauge.getCurrentValue());
        } else {
            bar.setFill(gauge.getBarColor());
        }

        if (Orientation.VERTICAL == orientation) {
            barHighlight.setFill(new LinearGradient(barHighlight.getLayoutX(), 0, barHighlight.getLayoutX() + barHighlight.getWidth(), 0,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, Color.rgb(255, 255, 255, 0.65)),
                                                    new Stop(0.92, Color.TRANSPARENT),
                                                    new Stop(1.0, Color.rgb(0, 0, 0, 0.2))));
        } else {
            barHighlight.setFill(new LinearGradient(0, barHighlight.getLayoutY(), 0, barHighlight.getLayoutY() + barHighlight.getHeight(),
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, Color.rgb(255, 255, 255, 0.65)),
                                                    new Stop(0.92, Color.TRANSPARENT),
                                                    new Stop(1.0, Color.rgb(0, 0, 0, 0.2))));
        }

        // LED
        if (gauge.isLedVisible()) {
            final Color LED_COLOR = gauge.getLedColor();
            switch(gauge.getLedType()) {
                case FLAT:
                    ledFramePaint = Color.WHITE;
                    ledOnPaint = new LinearGradient(0, 0.25 * ledSize,
                                                    0, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR),
                                                    new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)));
                    ledOffPaint = new LinearGradient(0, 0.25 * ledSize,
                                                     0, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.13, 1.0)));
                    ledHighlightPaint = Color.TRANSPARENT;
                    break;
                case STANDARD:
                default:
                    ledFramePaint = new LinearGradient(0.14 * ledSize, 0.14 * ledSize,
                                                       0.84 * ledSize, 0.84 * ledSize,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.rgb(20, 20, 20, 0.65)),
                                                       new Stop(0.15, Color.rgb(20, 20, 20, 0.65)),
                                                       new Stop(0.26, Color.rgb(41, 41, 41, 0.65)),
                                                       new Stop(0.26, Color.rgb(41, 41, 41, 0.64)),
                                                       new Stop(0.85, Color.rgb(200, 200, 200, 0.41)),
                                                       new Stop(1.0, Color.rgb(200, 200, 200, 0.35)));
                    ledOnPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                    0.74 * ledSize, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.77, 1.0)),
                                                    new Stop(0.49, LED_COLOR.deriveColor(0.0, 1.0, 0.5, 1.0)),
                                                    new Stop(1.0, LED_COLOR));
                    ledOffPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                     0.74 * ledSize, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0.0, 1.0, 0.20, 1.0)),
                                                     new Stop(0.49, LED_COLOR.deriveColor(0.0, 1.0, 0.13, 1.0)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0.0, 1.0, 0.2, 1.0)));
                    ledHighlightPaint = new RadialGradient(0, 0,
                                                           0.3 * ledSize, 0.3 * ledSize,
                                                           0.29 * ledSize,
                                                           false, CycleMethod.NO_CYCLE,
                                                           new Stop(0.0, Color.WHITE),
                                                           new Stop(1.0, Color.TRANSPARENT));
                    break;
            }
            drawLed();
        }

        // Tickmarks, Sections and Areas
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, ticksAndSectionsCanvas.getWidth(), ticksAndSectionsCanvas.getHeight());
        drawTickMarks(ticksAndSections);
        ticksAndSectionsCanvas.setCache(true);
        ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

        // LCD
        LcdDesign lcdDesign = gauge.getLcdDesign();
        Color[]   lcdColors = lcdDesign.getColors();
        if (gauge.isLcdVisible() && gauge.isValueVisible()) {
            LinearGradient lcdGradient = new LinearGradient(0, 1, 0, lcd.getHeight() - 1,
                                                            false, CycleMethod.NO_CYCLE,
                                                            new Stop(0, lcdColors[0]),
                                                            new Stop(0.03, lcdColors[1]),
                                                            new Stop(0.5, lcdColors[2]),
                                                            new Stop(0.5, lcdColors[3]),
                                                            new Stop(1.0, lcdColors[4]));
            Paint lcdFramePaint;
            if (LcdDesign.FLAT_CUSTOM == lcdDesign) {
                lcdFramePaint = lcdDesign.lcdForegroundColor;
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
        }

        // Text
        titleText.setText(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        unitText.setFill(gauge.getUnitColor());
        unitText.setText(gauge.getUnit());
        valueText.setFill(gauge.isLcdVisible() ? lcdColors[5] : gauge.getValueColor());
        resizeText();
    }
}
