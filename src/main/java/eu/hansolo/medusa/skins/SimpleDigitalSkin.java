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
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
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
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 09.02.16.
 */
public class SimpleDigitalSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ANGLE_RANGE      = 300;
    private double          size;
    private double          center;
    private Pane            pane;
    private Canvas          backgroundCanvas;
    private GraphicsContext backgroundCtx;
    private Canvas          barCanvas;
    private GraphicsContext barCtx;
    private Text            valueBkgText;
    private Text            valueText;
    private Color           barColor;
    private Color           valueColor;
    private Color           unitColor;
    private double          minValue;
    private double          maxValue;
    private double          range;
    private double          angleStep;
    private boolean         isStartFromZero;
    private double          barWidth;
    private String          formatString;
    private Locale          locale;
    private boolean         sectionsVisible;
    private List<Section>   sections;
    private boolean         thresholdVisible;
    private Color           thresholdColor;


    // ******************** Constructors **************************************
    public SimpleDigitalSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        minValue         = gauge.getMinValue();
        maxValue         = gauge.getMaxValue();
        range            = gauge.getRange();
        angleStep        = ANGLE_RANGE / range;
        formatString     = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale           = gauge.getLocale();
        barColor         = gauge.getBarColor();
        valueColor       = gauge.getValueColor();
        unitColor        = gauge.getUnitColor();
        isStartFromZero  = gauge.isStartFromZero();
        sectionsVisible  = gauge.getSectionsVisible();
        sections         = gauge.getSections();
        thresholdVisible = gauge.isThresholdVisible();
        thresholdColor   = gauge.getThresholdColor();

        init();
        initGraphics();
        registerListeners();

        setBar(gauge.getCurrentValue());
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
        backgroundCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        backgroundCtx    = backgroundCanvas.getGraphicsContext2D();

        barCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        barCtx    = barCanvas.getGraphicsContext2D();

        valueBkgText = new Text();
        valueBkgText.setStroke(null);
        valueBkgText.setFill(Helper.getTranslucentColorFrom(valueColor, 0.1));
        Helper.enableNode(valueBkgText, getSkinnable().isValueVisible());

        valueText = new Text();
        valueText.setStroke(null);
        valueText.setFill(valueColor);
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        pane = new Pane(backgroundCanvas, barCanvas, valueBkgText, valueText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().decimalsProperty().addListener(o -> handleEvents("DECIMALS"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> setBar(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = getSkinnable().getMinValue();
            maxValue  = getSkinnable().getMaxValue();
            range     = getSkinnable().getRange();
            angleStep = ANGLE_RANGE / range;
            redraw();
        } else if ("SECTIONS".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueBkgText, getSkinnable().isValueVisible());
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            sectionsVisible  = getSkinnable().getSectionsVisible();
            thresholdVisible = getSkinnable().isThresholdVisible();
        } else if ("DECIMALS".equals(EVENT_TYPE)) {
            formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        }
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

        if (thresholdVisible && VALUE > getSkinnable().getThreshold()) {
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
                    for (int i = (minValueAngle - 1); i >= 0; i--) {
                        if (i % 10 == 0 && i > v - 6) {
                            barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.1, barWidth * 0.5 + barWidth * 0.1, size - barWidth - barWidth * 0.2, size - barWidth - barWidth * 0.2, (-i - 139), 9.2, ArcType.OPEN);
                        }
                    }
                } else {
                    for (int i = minValueAngle; i <= 300; i++) {
                        if (i % 10 == 0 && i < v) {
                            barCtx.strokeArc(barWidth * 0.5 + barWidth * 0.1, barWidth * 0.5 + barWidth * 0.1, size - barWidth - barWidth * 0.2, size - barWidth - barWidth * 0.2, (-i - 139), 9.2, ArcType.OPEN);
                        }
                    }
                }
            }
        }
        valueText.setText(String.format(locale, formatString, VALUE));
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
        if (!getSkinnable().getUnit().isEmpty()) {
            backgroundCtx.setTextAlign(TextAlignment.CENTER);
            backgroundCtx.setFont(Fonts.robotoBold(0.09 * size));
            backgroundCtx.setFill(unitColor);
            backgroundCtx.fillText(getSkinnable().getUnit(), center, size * 0.75, size * 0.4);
        }

        backgroundCanvas.setCache(true);
        backgroundCanvas.setCacheHint(CacheHint.QUALITY);

        // draw the value
        if (getSkinnable().isValueVisible()) {
            StringBuilder valueBkg = new StringBuilder();
            int len = String.valueOf((int) getSkinnable().getMaxValue()).length();
            if (getSkinnable().getMinValue() < 0) { len++; }
            for (int i = 0 ; i < len ; i++) { valueBkg.append("8"); }
            if (getSkinnable().getDecimals() > 0) {
                valueBkg.append(".");
                len = getSkinnable().getDecimals();
                for (int i = 0 ; i < len ; i++) { valueBkg.append("8"); }
            }
            valueBkgText.setText(valueBkg.toString());
            valueBkgText.setX((size - valueBkgText.getLayoutBounds().getWidth()) * 0.5);
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
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

            valueBkgText.setFont(Fonts.digitalReadoutBold(0.25 * size));
            valueBkgText.setY(center + (valueBkgText.getLayoutBounds().getHeight() * 0.325));

            valueText.setFont(Fonts.digitalReadoutBold(0.25 * size));
            valueText.setY(center + (valueText.getLayoutBounds().getHeight() * 0.325));

            drawBackground();
            setBar(getSkinnable().getCurrentValue());
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        locale     = getSkinnable().getLocale();
        barColor   = getSkinnable().getBarColor();
        valueColor = getSkinnable().getValueColor();
        unitColor  = getSkinnable().getUnitColor();
        drawBackground();

        setBar(getSkinnable().getCurrentValue());

        valueBkgText.setFill(Helper.getTranslucentColorFrom(valueColor, 0.1));

        valueText.setFill(valueColor);
    }
}
