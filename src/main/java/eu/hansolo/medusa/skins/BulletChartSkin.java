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
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 25.12.15.
 */
public class BulletChartSkin extends GaugeSkinBase {
    private              double                      preferredWidth  = 400;
    private              double                      preferredHeight = 64;
    private              Pane                        pane;
    private              double                      width;
    private              double                      height;
    private              double                      aspectRatio;
    private              Orientation                 orientation;
    private              Canvas                      tickMarkCanvas;
    private              GraphicsContext             tickMarksCtx;
    private              Canvas                      sectionsCanvas;
    private              GraphicsContext             sectionsCtx;
    private              Text                        titleText;
    private              Text                        unitText;
    private              Rectangle                   barRect;
    private              Rectangle                   thresholdRect;
    private              double                      stepSize;
    private              Tooltip                     barTooltip;
    private              Tooltip                     thresholdTooltip;
    private              ListChangeListener<Section> sectionListener;
    private              ListChangeListener<Marker>  markerListener;
    private              InvalidationListener        currentValueListener;
    private              InvalidationListener        paneSizeListener;



    // ******************** Constructors **************************************
    public BulletChartSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        orientation              = gauge.getOrientation();
        barTooltip               = new Tooltip();
        thresholdTooltip         = new Tooltip();
        sectionListener          = c -> redraw();
        markerListener           = c -> redraw();
        currentValueListener     = o -> updateBar();
        paneSizeListener         = o -> handleEvents("RESIZE");

        if (Orientation.VERTICAL == orientation) {
            preferredWidth  = 64;
            preferredHeight = 400;
        } else {
            preferredWidth  = 400;
            preferredHeight = 64;
        }
        gauge.setPrefSize(preferredWidth, preferredHeight);

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

        orientation = gauge.getOrientation();

        aspectRatio = preferredHeight / preferredWidth;

        titleText = new Text(gauge.getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        unitText = new Text(gauge.getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        tickMarkCanvas    = new Canvas(0.79699248 * preferredWidth, 0.08333333 * preferredHeight);
        tickMarksCtx      = tickMarkCanvas.getGraphicsContext2D();

        sectionsCanvas    = new Canvas(0.79699248 * preferredWidth, 0.5 * preferredHeight);
        sectionsCtx       = sectionsCanvas.getGraphicsContext2D();

        barRect           = new Rectangle();
        Tooltip.install(barRect, barTooltip);

        thresholdRect     = new Rectangle();
        Tooltip.install(thresholdRect, thresholdTooltip);

        pane = new Pane(titleText,
                        unitText,
                        tickMarkCanvas,
                        sectionsCanvas,
                        barRect,
                        thresholdRect);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.getSections().addListener(sectionListener);
        gauge.getTickMarkSections().addListener(sectionListener);
        gauge.getTickLabelSections().addListener(sectionListener);
        gauge.getMarkers().addListener(markerListener);
        gauge.currentValueProperty().addListener(currentValueListener);
        pane.widthProperty().addListener(paneSizeListener);
        pane.heightProperty().addListener(paneSizeListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        }else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
            redraw();
        } else if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            if (Orientation.VERTICAL == orientation) {
                width    = height / aspectRatio;
                stepSize = (0.79699248 * height) / gauge.getRange();
            } else {
                height   = width / aspectRatio;
                stepSize = (0.79699248 * width) / gauge.getRange();
            }
            resize();
            redraw();
            updateBar();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            barTooltip.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getValue()));
        }
    }

    @Override public void dispose() {
        gauge.getSections().removeListener(sectionListener);
        gauge.getTickMarkSections().removeListener(sectionListener);
        gauge.getTickLabelSections().removeListener(sectionListener);
        gauge.getMarkers().removeListener(markerListener);
        gauge.currentValueProperty().removeListener(currentValueListener);
        pane.widthProperty().removeListener(paneSizeListener);
        pane.heightProperty().removeListener(paneSizeListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void drawTickMarks(final GraphicsContext CTX) {
        tickMarkCanvas.setCache(false);
        CTX.clearRect(0, 0, tickMarkCanvas.getWidth(), tickMarkCanvas.getHeight());
        CTX.setFill(gauge.getMajorTickMarkColor());

        List<Section> tickMarkSections         = gauge.getTickMarkSections();
        List<Section> tickLabelSections        = gauge.getTickLabelSections();
        Color         majorTickMarkColor       = gauge.getTickMarkColor();
        Color         tickLabelColor           = gauge.getTickLabelColor();
        boolean       smallRange               = Double.compare(gauge.getRange(), 10.0) <= 0;
        double        minValue                 = gauge.getMinValue();
        double        maxValue                 = gauge.getMaxValue();
        double        tmpStepSize              = smallRange ? stepSize / 10 : stepSize;
        Font          tickLabelFont            = Fonts.robotoRegular(0.15 * (Orientation.VERTICAL == orientation ? width : height));
        boolean       tickMarkSectionsVisible  = gauge.getTickMarkSectionsVisible();
        boolean       tickLabelSectionsVisible = gauge.getTickLabelSectionsVisible();
        double        offsetX                  = 0.18345865 * width;
        double        offsetY                  = 0.1 * height;
        double        innerPointX              = 0;
        double        innerPointY              = 0;
        double        outerPointX              = 0.07 * width;
        double        outerPointY              = 0.08 * height;
        double        textPointX               = 0.55 * tickMarkCanvas.getWidth();
        double        textPointY               = 0.7 * tickMarkCanvas.getHeight();
        BigDecimal    minorTickSpaceBD         = BigDecimal.valueOf(gauge.getMinorTickSpace());
        BigDecimal    majorTickSpaceBD         = BigDecimal.valueOf(gauge.getMajorTickSpace());
        BigDecimal    counterBD                = BigDecimal.valueOf(gauge.getMinValue());
        double        counter                  = minValue;
        double        range                    = gauge.getRange();

        for (double i = 0 ; Double.compare(i, range) <= 0 ; i++) {
            if (Orientation.VERTICAL == orientation) {
                innerPointY = counter * tmpStepSize + offsetY;
                outerPointY = innerPointY;
                textPointY  = innerPointY;
            } else {
                innerPointX = counter * tmpStepSize + offsetX;
                outerPointX = innerPointX;
                textPointX  = innerPointX;
            }

            // Set the general tickmark color
            CTX.setStroke(gauge.getTickMarkColor());
            if (Double.compare(counterBD.remainder(majorTickSpaceBD).doubleValue(), 0.0) == 0) {
                // Draw major tick mark
                if (gauge.getMajorTickMarksVisible()) {
                    CTX.setFill(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    CTX.setStroke(tickMarkSectionsVisible ? Helper.getColorOfSection(tickMarkSections, counter, majorTickMarkColor) : majorTickMarkColor);
                    CTX.setLineWidth(1);
                    CTX.strokeLine(innerPointX, innerPointY, outerPointX, outerPointY);
                }
                // Draw tick label text
                if (gauge.getTickLabelsVisible()) {
                    CTX.save();
                    CTX.translate(textPointX, textPointY);
                    CTX.setFont(tickLabelFont);
                    CTX.setTextAlign(TextAlignment.CENTER);
                    CTX.setTextBaseline(VPos.CENTER);
                    CTX.setFill(tickLabelSectionsVisible ? Helper.getColorOfSection(tickLabelSections, counter, tickLabelColor) : tickLabelColor);
                    if (Orientation.VERTICAL == orientation) {
                        CTX.fillText(Integer.toString((int) (maxValue - counter)), 0, 0);
                    } else {
                        CTX.fillText(Integer.toString((int) counter), 0, 0);
                    }
                    CTX.restore();
                }
            }
            counterBD = counterBD.add(minorTickSpaceBD);
            counter   = counterBD.doubleValue();
            if (counter > maxValue) break;
        }

        tickMarkCanvas.setCache(true);
        tickMarkCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private void drawSections(final GraphicsContext CTX) {
        sectionsCanvas.setCache(false);
        CTX.clearRect(0, 0, sectionsCanvas.getWidth(), sectionsCanvas.getHeight());
        CTX.setFill(gauge.getBackgroundPaint());
        if (Orientation.VERTICAL == orientation) {
            CTX.fillRect(0, 0, 0.5 * width, 0.89 * height);
        } else {
            CTX.fillRect(0, 0, 0.79699248 * width, 0.5 * height);
        }

        double minValue = gauge.getMinValue();
        double maxValue = gauge.getMaxValue();

        int listSize = gauge.getSections().size();
        for (int i = 0 ; i < listSize ; i++) {
            final Section SECTION = gauge.getSections().get(i);
            final double SECTION_START;
            if (Double.compare(SECTION.getStart(), maxValue) <= 0 && Double.compare(SECTION.getStop(), minValue) >= 0) {
                if (Double.compare(SECTION.getStart(), minValue) < 0 && Double.compare(SECTION.getStop(), maxValue) < 0) {
                    SECTION_START = minValue * stepSize;
                } else {
                    SECTION_START = (SECTION.getStart() - minValue) * stepSize;
                }
                final double SECTION_SIZE;
                if (Double.compare(SECTION.getStop(), maxValue) > 0) {
                    SECTION_SIZE = (maxValue - SECTION.getStart()) * stepSize;
                } else {
                    SECTION_SIZE = (SECTION.getStop() - SECTION.getStart()) * stepSize;
                }
                CTX.save();
                CTX.setFill(SECTION.getColor());
                if (Orientation.VERTICAL == orientation) {
                    CTX.fillRect(0.0, 0.89 * height - SECTION_START - SECTION_SIZE, 0.5 * width, SECTION_SIZE);
                } else {
                    CTX.fillRect(SECTION_START, 0.0, SECTION_SIZE, 0.5 * height);
                }
                CTX.restore();
            }
        }
        sectionsCanvas.setCache(true);
        sectionsCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private void updateBar() {
        double currentValue = gauge.getCurrentValue();
        if (Orientation.VERTICAL == orientation) {
            barRect.setY(height - 0.06 * width - currentValue * stepSize);
            barRect.setHeight(currentValue * stepSize);
            thresholdRect.setY(height - gauge.getThreshold() * stepSize - 0.08625 * width);
        } else {
            barRect.setWidth(currentValue * stepSize);
            thresholdRect.setX(gauge.getThreshold() * stepSize - 0.03125 * height + 0.1835 * width);
        }
    }

    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();

        double currentValue = gauge.getCurrentValue();

        orientation = gauge.getOrientation();
        if (Orientation.VERTICAL == orientation) {
            width = height / aspectRatio;
            stepSize = (0.89 * height) / gauge.getRange();

            pane.setMaxSize(width, height);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

            width  = pane.getLayoutBounds().getWidth();
            height = pane.getLayoutBounds().getHeight();

            tickMarkCanvas.setWidth(0.39 * width);
            tickMarkCanvas.setHeight(height);
            tickMarkCanvas.relocate(0.578125 * width, 0);

            sectionsCanvas.setWidth(0.5 * width);
            sectionsCanvas.setHeight(0.89 * height);
            sectionsCanvas.relocate(0.078125 * width, 0.1 * height);

            barRect.setWidth(0.16666667 * width);
            barRect.setHeight(currentValue * stepSize);
            barRect.setX(0.078125 * width + (0.5 * width - barRect.getWidth()) * 0.5);
            barRect.setY(height - currentValue * stepSize);

            thresholdRect.setX(0.16145833 * width);
            thresholdRect.setY(height - gauge.getThreshold() * stepSize - 0.03125 * width);
            thresholdRect.setWidth(0.33333333 * width);
            thresholdRect.setHeight(0.0625 * width);

            double maxTextWidth = width;
            titleText.setFont(Fonts.robotoRegular(0.24 * width));
            if (titleText.getLayoutBounds().getWidth() > maxTextWidth) { Helper.adjustTextSize(titleText, maxTextWidth, 0.24 * width); }
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.03 * width);

            unitText.setFont(Fonts.robotoRegular(0.15 * width));
            if (unitText.getLayoutBounds().getWidth() > maxTextWidth) { Helper.adjustTextSize(unitText, maxTextWidth, 0.15 * width); }
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.35 * width);
        } else {
            height   = width * aspectRatio;
            stepSize = (0.79699248 * width) / gauge.getRange();

            pane.setMaxSize(width, height);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

            width  = pane.getLayoutBounds().getWidth();
            height = pane.getLayoutBounds().getHeight();

            tickMarkCanvas.setWidth(width);
            tickMarkCanvas.setHeight(0.29166667 * height);
            tickMarkCanvas.relocate(0, 0.60416667 * height);

            sectionsCanvas.setWidth(0.79699248 * width);
            sectionsCanvas.setHeight(0.5 * height);
            sectionsCanvas.relocate(0.18345865 * width, 0.10416667 * height);

            barRect.setWidth(currentValue * stepSize);
            barRect.setHeight(0.16666667 * height);
            barRect.setX(0.18345865 * width);
            barRect.setY(0.10416667 * height + (0.5 * height - barRect.getHeight()) * 0.5);

            thresholdRect.setX(gauge.getThreshold() * stepSize - 0.03125 * height + 0.20300752 * width);
            thresholdRect.setY(0.1875 * height);
            thresholdRect.setWidth(0.0625 * height);
            thresholdRect.setHeight(0.33333333 * height);

            double maxTextWidth = 0.20300752 * width;
            titleText.setFont(Fonts.robotoMedium(0.24 * height));
            if (titleText.getLayoutBounds().getWidth() > maxTextWidth) { Helper.adjustTextSize(titleText, maxTextWidth, 0.24 * width); }
            titleText.relocate(0.17593985 * width - (titleText.getLayoutBounds().getWidth()), 0.075 * height);

            unitText.setFont(Fonts.robotoRegular(0.15 * height));
            if (unitText.getLayoutBounds().getWidth() > maxTextWidth) { Helper.adjustTextSize(unitText, maxTextWidth, 0.15 * width); }
            unitText.relocate(0.17593985 * width - (unitText.getLayoutBounds().getWidth()), 0.4 * height);
        }
        redraw();
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        drawTickMarks(tickMarksCtx);
        drawSections(sectionsCtx);
        thresholdRect.setFill(gauge.getThresholdColor());
        thresholdTooltip.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getThreshold()));
        barRect.setFill(gauge.getBarColor());
        titleText.setFill(gauge.getTitleColor());
        unitText.setFill(gauge.getUnitColor());
        updateBar();
    }
}
