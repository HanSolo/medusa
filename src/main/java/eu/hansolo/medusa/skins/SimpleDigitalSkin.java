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
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import java.util.List;
import java.util.Locale;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
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
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 09.02.16.
 */
public class SimpleDigitalSkin extends GaugeSkinBase {
    private static final double  ANGLE_RANGE = 280;
    private double               size;
    private double               center;
    private Pane                 pane;
    private Canvas               backgroundCanvas;
    private GraphicsContext      backgroundCtx;
    private Canvas               barCanvas;
    private GraphicsContext      barCtx;
    private Text                 titleText;
    private Text                 valueBkgText;
    private Text                 valueText;
    private Color                barColor;
    private Color                valueColor;
    private Color                unitColor;
    private double               minValue;
    private double               maxValue;
    private double               range;
    private double               angleStep;
    private boolean              isStartFromZero;
    private double               barWidth;
    private boolean              sectionsVisible;
    private List<Section>        sections;
    private boolean              thresholdVisible;
    private Color                thresholdColor;
    private InvalidationListener decimalListener;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public SimpleDigitalSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        minValue             = gauge.getMinValue();
        maxValue             = gauge.getMaxValue();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        barColor             = gauge.getBarColor();
        valueColor           = gauge.getValueColor();
        unitColor            = gauge.getUnitColor();
        isStartFromZero      = gauge.isStartFromZero();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        thresholdVisible     = gauge.isThresholdVisible();
        thresholdColor       = gauge.getThresholdColor();
        decimalListener      = o -> handleEvents("DECIMALS");
        currentValueListener = o -> setBar(gauge.getCurrentValue());

        initGraphics();
        registerListeners();

        setBar(gauge.getCurrentValue());
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // Set initial size
        if (Double.compare(gauge.getPrefWidth(), 0.0) <= 0 || Double.compare(gauge.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(gauge.getWidth(), 0.0) <= 0 || Double.compare(gauge.getHeight(), 0.0) <= 0) {
            if (gauge.getPrefWidth() > 0 && gauge.getPrefHeight() > 0) {
                gauge.setPrefSize(gauge.getPrefWidth(), gauge.getPrefHeight());
            } else {
                gauge.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        backgroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        backgroundCtx    = backgroundCanvas.getGraphicsContext2D();

        barCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        barCtx    = barCanvas.getGraphicsContext2D();

        titleText = new Text(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        valueBkgText = new Text();
        valueBkgText.setStroke(null);
        valueBkgText.setFill(Helper.getTranslucentColorFrom(valueColor, 0.1));
        Helper.enableNode(valueBkgText, gauge.isValueVisible());

        valueText = new Text();
        valueText.setStroke(null);
        valueText.setFill(valueColor);
        Helper.enableNode(valueText, gauge.isValueVisible());

        pane = new Pane(backgroundCanvas, barCanvas, titleText, valueBkgText, valueText);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.decimalsProperty().addListener(decimalListener);
        gauge.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = gauge.getMinValue();
            maxValue  = gauge.getMaxValue();
            range     = gauge.getRange();
            angleStep = ANGLE_RANGE / range;
            redraw();
            setBar(gauge.getCurrentValue());
        } else if ("SECTIONS".equals(EVENT_TYPE)) {
            sections = gauge.getSections();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueBkgText, gauge.isValueVisible());
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            sectionsVisible  = gauge.getSectionsVisible();
            thresholdVisible = gauge.isThresholdVisible();
            resize();
            redraw();
        }
    }

    @Override public void dispose() {
        gauge.decimalsProperty().removeListener(decimalListener);
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Canvas ********************************************
    private void setBar(final double VALUE) {
        barCtx.clearRect(0, 0, size, size);
        barCtx.setLineCap(StrokeLineCap.BUTT);
        barCtx.setStroke(barColor);
        barCtx.setLineWidth(barWidth);

        if (sectionsVisible) {
            int listSize = sections.size();
            for (int i = 0 ; i < listSize ;i++) {
                Section section = sections.get(i);
                if (section.contains(VALUE)) {
                    barCtx.setStroke(section.getColor());
                    break;
                }
            }
        }

        if (thresholdVisible && VALUE > gauge.getThreshold()) {
            barCtx.setStroke(thresholdColor);
        }

        double v             = (VALUE - minValue) * angleStep;
        int    minValueAngle = (int) (-minValue * angleStep);
        if (!isStartFromZero) {
            for (int i = 0; i < 280; i++) {
                if (i % 10 == 0 && i < v) {
                    barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.1, barWidth * 0.5 + barWidth * 0.1, size - barWidth - barWidth * 0.2, size - barWidth - barWidth * 0.2, (-i - 139), 9.2, ArcType.OPEN);
                }
            }
        } else {
            if (Double.compare(VALUE, 0) != 0) {
                if (VALUE < 0) {
                    for (int i = Math.min(minValueAngle, 280) - 1; i >= 0; i--) {
                        if (i % 10 == 0 && i > v - 10) {
                            barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.1, barWidth * 0.5 + barWidth * 0.1, size - barWidth - barWidth * 0.2, size - barWidth - barWidth * 0.2, (-i - 139), 9.2, ArcType.OPEN);
                        }
                    }
                } else {
                    for (int i = Math.max(minValueAngle, 0) - 5; i < 280; i++) {
                        if (i % 10 == 0 && i < v) {
                            barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.1, barWidth * 0.5 + barWidth * 0.1, size - barWidth - barWidth * 0.2, size - barWidth - barWidth * 0.2, (-i - 139), 9.2, ArcType.OPEN);
                        }
                    }
                }
            }
        }
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        valueText.setLayoutX(valueBkgText.getLayoutBounds().getMaxX() - valueText.getLayoutBounds().getWidth());
    }

    private void drawBackground() {
        backgroundCanvas.setCache(false);
        backgroundCtx.setLineCap(StrokeLineCap.BUTT);
        backgroundCtx.clearRect(0, 0, size, size);

        // draw translucent background
        backgroundCtx.setStroke(Color.rgb(0, 12, 6, 0.1));
        Color bColor = Helper.getTranslucentColorFrom(barColor, 0.1);
        for (int i = -50 ; i < 230 ; i++) {
            backgroundCtx.save();
            if (i % 10 == 0) {
                // draw value bar
                backgroundCtx.setStroke(bColor);
                backgroundCtx.setLineWidth(barWidth);
                backgroundCtx.strokeArc(barWidth * 0.5 + barWidth * 0.1, barWidth * 0.5 + barWidth * 0.1, size - barWidth - barWidth * 0.2, size - barWidth - barWidth * 0.2, i + 1, 9.2, ArcType.OPEN);
            }
            backgroundCtx.restore();
        }

        // draw the unit
        if (!gauge.getUnit().isEmpty()) {
            backgroundCtx.setTextAlign(TextAlignment.CENTER);
            backgroundCtx.setFont(Fonts.robotoBold(0.09 * size));
            backgroundCtx.setFill(unitColor);
            backgroundCtx.fillText(gauge.getUnit(), center, size * 0.75, size * 0.4);
        }

        backgroundCanvas.setCache(true);
        backgroundCanvas.setCacheHint(CacheHint.QUALITY);

        // draw the value
        if (gauge.isValueVisible()) {
            StringBuilder valueBkg = new StringBuilder();
            int len = String.valueOf((int) gauge.getMaxValue()).length();
            if (gauge.getMinValue() < 0) { len++; }
            for (int i = 0 ; i < len ; i++) { valueBkg.append("8"); }
            if (gauge.getDecimals() > 0) {
                valueBkg.append(".");
                len = gauge.getDecimals();
                for (int i = 0 ; i < len ; i++) { valueBkg.append("8"); }
            }
            valueBkgText.setText(valueBkg.toString());
            valueBkgText.setX((size - valueBkgText.getLayoutBounds().getWidth()) * 0.5);
        }
    }


    // ******************** Resizing ******************************************
    private void resizeStaticText() {
        double maxWidth = size * 0.455;
        double fontSize = size * 0.08082707;
        titleText.setFont(Fonts.latoBold(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.22180451);
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            center   = size * 0.5;
            barWidth = size * 0.125;

            backgroundCanvas.setWidth(size);
            backgroundCanvas.setHeight(size);

            barCanvas.setWidth(size);
            barCanvas.setHeight(size);

            valueBkgText.setFont(Fonts.digitalReadoutBold(0.215 * size));
            valueBkgText.setY(center + (valueBkgText.getLayoutBounds().getHeight() * 0.325));

            valueText.setFont(Fonts.digitalReadoutBold(0.215 * size));
            valueText.setY(center + (valueText.getLayoutBounds().getHeight() * 0.325));

            titleText.setText(gauge.getTitle());
            titleText.setFill(gauge.getTitleColor());

            drawBackground();
            resizeStaticText();
            setBar(gauge.getCurrentValue());
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        barColor        = gauge.getBarColor();
        valueColor      = gauge.getValueColor();
        unitColor       = gauge.getUnitColor();
        sectionsVisible = gauge.getSectionsVisible();
        drawBackground();
        resizeStaticText();

        setBar(gauge.getCurrentValue());

        titleText.setText(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.22180451);

        valueBkgText.setFill(Helper.getTranslucentColorFrom(valueColor, 0.1));
        valueText.setFill(valueColor);
    }
}
