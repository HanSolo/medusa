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
import eu.hansolo.medusa.tools.Helper;
import eu.hansolo.medusa.tools.Statistics;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.medusa.tools.Helper.clamp;


/**
 * Created by hansolo on 05.12.16.
 */
public class TileSparklineSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double            PREFERRED_WIDTH  = 250;
    private static final double            PREFERRED_HEIGHT = 250;
    private static final double            MINIMUM_WIDTH    = 50;
    private static final double            MINIMUM_HEIGHT   = 50;
    private static final double            MAXIMUM_WIDTH    = 1024;
    private static final double            MAXIMUM_HEIGHT   = 1024;
    private              double            size;
    private              Text              titleText;
    private              Text              valueText;
    private              Text              unitText;
    private              Text              averageText;
    private              Text              highText;
    private              Text              lowText;
    private              Rectangle         graphBounds;
    private              List<PathElement> pathElements;
    private              Path              sparkLine;
    private              Circle            dot;
    private              Rectangle         stdDeviationArea;
    private              Line              averageLine;
    private              Pane              pane;
    private              double            low;
    private              double            high;
    private              double            range;
    private              double            stdDeviation;
    private              String            formatString;
    private              Locale            locale;
    private              int               noOfDatapoints;
    private              List<Double>      dataList;


    // ******************** Constructors **************************************
    public TileSparklineSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        low            = gauge.getMaxValue();
        high           = gauge.getMinValue();
        range          = gauge.getRange();
        stdDeviation   = 0;
        formatString   = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale         = gauge.getLocale();
        noOfDatapoints = gauge.getAveragingPeriod();
        dataList       = new LinkedList<>();
        for (int i = 0; i < noOfDatapoints; i++) { dataList.add(gauge.getMinValue()); }

        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // Set initial size
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        graphBounds = new Rectangle(PREFERRED_WIDTH * 0.05, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.45);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getValue()));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        averageText = new Text(String.format(locale, formatString, getSkinnable().getAverage()));
        averageText.setFill(getSkinnable().getAverageColor());
        Helper.enableNode(averageText, getSkinnable().isAverageVisible());

        highText = new Text();
        highText.setTextOrigin(VPos.BOTTOM);
        highText.setFill(getSkinnable().getValueColor());

        lowText = new Text();
        lowText.setTextOrigin(VPos.TOP);
        lowText.setFill(getSkinnable().getValueColor());

        stdDeviationArea = new Rectangle();
        Helper.enableNode(stdDeviationArea, getSkinnable().isAverageVisible());

        averageLine = new Line();
        averageLine.setStroke(getSkinnable().getAverageColor());
        averageLine.getStrokeDashArray().addAll(PREFERRED_WIDTH * 0.005, PREFERRED_WIDTH * 0.005);
        Helper.enableNode(averageLine, getSkinnable().isAverageVisible());

        pathElements = new ArrayList<>(noOfDatapoints);
        pathElements.add(0, new MoveTo());
        for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }

        sparkLine = new Path();
        sparkLine.getElements().addAll(pathElements);
        sparkLine.setFill(null);
        sparkLine.setStroke(getSkinnable().getBarColor());
        sparkLine.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        sparkLine.setStrokeLineCap(StrokeLineCap.ROUND);
        sparkLine.setStrokeLineJoin(StrokeLineJoin.ROUND);

        dot = new Circle();
        dot.setFill(getSkinnable().getBarColor());

        pane = new Pane(titleText, valueText, unitText, stdDeviationArea, averageLine, sparkLine, dot, averageText, highText, lowText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> handleEvents("VALUE"));
    }


    // ******************** Methods *******************************************
    @Override protected double computeMinWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_WIDTH; }
    @Override protected double computeMinHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MINIMUM_HEIGHT; }
    @Override protected double computePrefWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefWidth(HEIGHT, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computePrefHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT) { return super.computePrefHeight(WIDTH, TOP, RIGHT, BOTTOM, LEFT); }
    @Override protected double computeMaxWidth(final double HEIGHT, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_WIDTH; }
    @Override protected double computeMaxHeight(final double WIDTH, final double TOP, final double RIGHT, final double BOTTOM, final double LEFT)  { return MAXIMUM_HEIGHT; }

    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            noOfDatapoints = getSkinnable().getAveragingPeriod();
            dataList.clear();
            for (int i = 0 ; i < noOfDatapoints ; i++) { dataList.add(0.0); }
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            range = getSkinnable().getRange();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
            Helper.enableNode(averageLine, getSkinnable().isAverageVisible());
            Helper.enableNode(averageText, getSkinnable().isAverageVisible());
            Helper.enableNode(stdDeviationArea, getSkinnable().isAverageVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {

        } else if ("ALERT".equals(EVENT_TYPE)) {

        } else if ("VALUE".equals(EVENT_TYPE)) {
            if(getSkinnable().isAnimated()) { getSkinnable().setAnimated(false); }
            if (!getSkinnable().isAveragingEnabled()) { getSkinnable().setAveragingEnabled(true); }
            double value = getSkinnable().getValue();
            addData(value);
            drawChart(value);
        }
    }

    private void addData(final double VALUE) {
        if (dataList.size() <= noOfDatapoints) {
            Collections.rotate(dataList, -1);
            dataList.set((noOfDatapoints - 1), VALUE);
        } else {
            dataList.add(VALUE);
        }
        stdDeviation = Statistics.getStdDev(dataList);
    }

    private void drawChart(final double VALUE) {
        low  = Statistics.getMin(dataList);
        high = Statistics.getMax(dataList);
        if (Double.compare(low, high) == 0) {
            low  = getSkinnable().getMinValue();
            high = getSkinnable().getMaxValue();
        }
        range = high - low;

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY = graphBounds.getHeight() / range;

        MoveTo begin = (MoveTo) pathElements.get(0);
        begin.setX(minX);
        begin.setY(maxY - Math.abs(low - dataList.get(0)) * stepY);
        for (int i = 1; i < (noOfDatapoints - 1); i++) {
            LineTo lineTo = (LineTo) pathElements.get(i);
            lineTo.setX(minX + i * stepX);
            lineTo.setY(maxY - Math.abs(low - dataList.get(i)) * stepY);
        }
        LineTo end = (LineTo) pathElements.get(noOfDatapoints - 1);
        end.setX(maxX);
        end.setY(maxY - Math.abs(low - dataList.get(noOfDatapoints - 1)) * stepY);

        dot.setCenterX(maxX);
        dot.setCenterY(end.getY());

        double average = getSkinnable().getAverage();
        double averageY = clamp(minY, maxY, maxY - Math.abs(low - average) * stepY);

        averageLine.setStartX(minX);
        averageLine.setEndX(maxX);
        averageLine.setStartY(averageY);
        averageLine.setEndY(averageY);

        stdDeviationArea.setY(averageLine.getStartY() - (stdDeviation * 0.5 * stepY));
        stdDeviationArea.setHeight(stdDeviation * stepY);

        valueText.setText(String.format(locale, formatString, VALUE));
        averageText.setText(String.format(locale, formatString, average));

        highText.setText(String.format(locale, formatString, high));
        lowText.setText(String.format(locale, formatString, low));
        resizeDynamicText();
    }


    // ******************** Resizing ******************************************
    private void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? size * 0.725 : size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate(size * 0.925 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.18);

        maxWidth = size * 0.3;
        fontSize = size * 0.05;
        averageText.setFont(Fonts.latoRegular(fontSize));
        if (averageText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(averageText, maxWidth, fontSize); }
        if (averageLine.getStartY() < graphBounds.getY() + graphBounds.getHeight() * 0.5) {
            averageText.setY(averageLine.getStartY() + (size * 0.0425));
        } else {
            averageText.setY(averageLine.getStartY() - (size * 0.0075));
        }

        highText.setFont(Fonts.latoRegular(fontSize));
        if (highText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(highText, maxWidth, fontSize); }
        highText.setY(graphBounds.getY() - size * 0.0125);

        lowText.setFont(Fonts.latoRegular(fontSize));
        if (lowText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(lowText, maxWidth, fontSize); }
        lowText.setY(size * 0.9);
    }
    private void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = 0.15 * size;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.3575);

        maxWidth = 0.3 * size;
        averageText.setX(size * 0.05);
        highText.setX(size * 0.05);
        lowText.setX(size * 0.05);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            graphBounds = new Rectangle(size * 0.05, size * 0.5, size * 0.9, size * 0.39);

            stdDeviationArea.setX(graphBounds.getX());
            stdDeviationArea.setWidth(graphBounds.getWidth());

            averageLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);

            drawChart(getSkinnable().getValue());
            sparkLine.setStrokeWidth(size * 0.01);
            dot.setRadius(size * 0.014);

            resizeStaticText();
            resizeDynamicText();
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(size * 0.025), Insets.EMPTY)));

        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();

        titleText.setText(getSkinnable().getTitle());
        resizeStaticText();

        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        averageText.setFill(getSkinnable().getAverageColor());
        highText.setFill(getSkinnable().getValueColor());
        lowText.setFill(getSkinnable().getValueColor());
        sparkLine.setStroke(getSkinnable().getBarColor());
        stdDeviationArea.setFill(Helper.getTranslucentColorFrom(getSkinnable().getAverageColor(), 0.1));
        averageLine.setStroke(getSkinnable().getAverageColor());
        dot.setFill(getSkinnable().getBarColor());
    }
}
