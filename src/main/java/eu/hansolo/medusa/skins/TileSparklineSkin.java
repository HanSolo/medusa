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
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static eu.hansolo.medusa.tools.Helper.clamp;
import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 05.12.16.
 */
public class TileSparklineSkin extends GaugeSkinBase {
    private              double               size;
    private              Text                 titleText;
    private              Text                 valueText;
    private              Text                 unitText;
    private              Text                 averageText;
    private              Text                 highText;
    private              Text                 lowText;
    private              Text                 subTitleText;
    private              Rectangle            graphBounds;
    private              List<PathElement>    pathElements;
    private              Path                 sparkLine;
    private              Circle               dot;
    private              Rectangle            stdDeviationArea;
    private              Line                 averageLine;
    private              Pane                 pane;
    private              double               low;
    private              double               high;
    private              double               minValue;
    private              double               maxValue;
    private              double               range;
    private              double               stdDeviation;
    private              String               formatString;
    private              Locale               locale;
    private              int                  noOfDatapoints;
    private              List<Double>         dataList;
    private              InvalidationListener currentValueListener;
    private              InvalidationListener averagingListener;


    // ******************** Constructors **************************************
    public TileSparklineSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        low                  = gauge.getMaxValue();
        high                 = gauge.getMinValue();
        minValue             = gauge.getMinValue();
        maxValue             = gauge.getMaxValue();
        range                = gauge.getRange();
        stdDeviation         = 0;
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale               = gauge.getLocale();
        noOfDatapoints       = gauge.getAveragingPeriod();
        dataList             = new LinkedList<>();
        currentValueListener = o -> handleEvents("VALUE");
        averagingListener    = o -> handleEvents("AVERAGING_PERIOD");
        for (int i = 0; i < noOfDatapoints; i++) { dataList.add(minValue); }

        // To get smooth lines in the chart we need at least 4 values
        if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");

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
                gauge.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        graphBounds = new Rectangle(PREFERRED_WIDTH * 0.05, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.9, PREFERRED_HEIGHT * 0.45);

        titleText = new Text(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, gauge.getValue()));
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

        unitText = new Text(gauge.getUnit());
        unitText.setFill(gauge.getUnitColor());
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        averageText = new Text(String.format(locale, formatString, gauge.getAverage()));
        averageText.setFill(gauge.getAverageColor());
        Helper.enableNode(averageText, gauge.isAverageVisible());

        highText = new Text();
        highText.setTextOrigin(VPos.BOTTOM);
        highText.setFill(gauge.getValueColor());

        lowText = new Text();
        lowText.setTextOrigin(VPos.TOP);
        lowText.setFill(gauge.getValueColor());

        subTitleText = new Text(gauge.getSubTitle());
        subTitleText.setTextOrigin(VPos.TOP);
        subTitleText.setFill(gauge.getSubTitleColor());

        stdDeviationArea = new Rectangle();
        Helper.enableNode(stdDeviationArea, gauge.isAverageVisible());

        averageLine = new Line();
        averageLine.setStroke(gauge.getAverageColor());
        averageLine.getStrokeDashArray().addAll(PREFERRED_WIDTH * 0.005, PREFERRED_WIDTH * 0.005);
        Helper.enableNode(averageLine, gauge.isAverageVisible());

        pathElements = new ArrayList<>(noOfDatapoints);
        pathElements.add(0, new MoveTo());
        for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }

        sparkLine = new Path();
        sparkLine.getElements().addAll(pathElements);
        sparkLine.setFill(null);
        sparkLine.setStroke(gauge.getBarColor());
        sparkLine.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        sparkLine.setStrokeLineCap(StrokeLineCap.ROUND);
        sparkLine.setStrokeLineJoin(StrokeLineJoin.ROUND);

        dot = new Circle();
        dot.setFill(gauge.getBarColor());

        pane = new Pane(titleText, valueText, unitText, stdDeviationArea, averageLine, sparkLine, dot, averageText, highText, lowText, subTitleText);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(PREFERRED_WIDTH * 0.025), new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(PREFERRED_WIDTH * 0.025), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
        gauge.averagingPeriodProperty().addListener(averagingListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            minValue = gauge.getMinValue();
            maxValue = gauge.getMaxValue();
            range    = gauge.getRange();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
            Helper.enableNode(subTitleText, !gauge.getSubTitle().isEmpty());
            Helper.enableNode(averageLine, gauge.isAverageVisible());
            Helper.enableNode(averageText, gauge.isAverageVisible());
            Helper.enableNode(stdDeviationArea, gauge.isAverageVisible());
            redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {

        } else if ("ALERT".equals(EVENT_TYPE)) {

        } else if ("VALUE".equals(EVENT_TYPE)) {
            if(gauge.isAnimated()) { gauge.setAnimated(false); }
            if (!gauge.isAveragingEnabled()) { gauge.setAveragingEnabled(true); }
            double value = clamp(minValue, maxValue, gauge.getValue());
            addData(value);
            drawChart(value);
        } else if ("AVERAGING_PERIOD".equals(EVENT_TYPE)) {
            noOfDatapoints = gauge.getAveragingPeriod();
            // To get smooth lines in the chart we need at least 4 values
            if (noOfDatapoints < 4) throw new IllegalArgumentException("Please increase the averaging period to a value larger than 3.");
            for (int i = 0; i < noOfDatapoints; i++) { dataList.add(minValue); }
            pathElements.clear();
            pathElements.add(0, new MoveTo());
            for (int i = 1 ; i < noOfDatapoints ; i++) { pathElements.add(i, new LineTo()); }
            sparkLine.getElements().setAll(pathElements);
            redraw();
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
            low  = minValue;
            high = maxValue;
        }
        range = high - low;

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY = graphBounds.getHeight() / range;

        if (gauge.isSmoothing()) {
            smooth(dataList);
        } else {
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
        }

        double average = gauge.getAverage();
        double averageY = clamp(minY, maxY, maxY - Math.abs(low - average) * stepY);

        averageLine.setStartX(minX);
        averageLine.setEndX(maxX);
        averageLine.setStartY(averageY);
        averageLine.setEndY(averageY);

        stdDeviationArea.setY(averageLine.getStartY() - (stdDeviation * 0.5 * stepY));
        stdDeviationArea.setHeight(stdDeviation * stepY);

        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        averageText.setText(String.format(locale, formatString, average));

        highText.setText(String.format(locale, formatString, high));
        lowText.setText(String.format(locale, formatString, low));
        resizeDynamicText();
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        gauge.averagingPeriodProperty().removeListener(averagingListener);
        super.dispose();
    }


    // ******************** Smoothing *****************************************
    public void smooth(final List<Double> DATA_LIST) {
        int      size = DATA_LIST.size();
        double[] x    = new double[size];
        double[] y    = new double[size];

        low  = Statistics.getMin(DATA_LIST);
        high = Statistics.getMax(DATA_LIST);
        if (Double.compare(low, high) == 0) {
            low  = minValue;
            high = maxValue;
        }
        range = high - low;

        double minX  = graphBounds.getX();
        double maxX  = minX + graphBounds.getWidth();
        double minY  = graphBounds.getY();
        double maxY  = minY + graphBounds.getHeight();
        double stepX = graphBounds.getWidth() / (noOfDatapoints - 1);
        double stepY = graphBounds.getHeight() / range;

        for (int i = 0 ; i < size ; i++) {
            x[i] = minX + i * stepX;
            y[i] = maxY - Math.abs(low - DATA_LIST.get(i)) * stepY;
        }

        Pair<Double[], Double[]> px = computeControlPoints(x);
        Pair<Double[], Double[]> py = computeControlPoints(y);

        sparkLine.getElements().clear();
        for (int i = 0 ; i < size - 1 ; i++) {
            sparkLine.getElements().add(new MoveTo(x[i], y[i]));
            sparkLine.getElements().add(new CubicCurveTo(px.getKey()[i], py.getKey()[i], px.getValue()[i], py.getValue()[i], x[i + 1], y[i + 1]));
        }
        dot.setCenterX(maxX);
        dot.setCenterY(y[size - 1]);
    }
    private Pair<Double[], Double[]> computeControlPoints(final double[] K) {
        int      n  = K.length - 1;
        Double[] p1 = new Double[n];
        Double[] p2 = new Double[n];

	    /*rhs vector*/
        double[] a = new double[n];
        double[] b = new double[n];
        double[] c = new double[n];
        double[] r = new double[n];

	    /*left most segment*/
        a[0] = 0;
        b[0] = 2;
        c[0] = 1;
        r[0] = K[0]+2*K[1];

	    /*internal segments*/
        for (int i = 1; i < n - 1; i++) {
            a[i] = 1;
            b[i] = 4;
            c[i] = 1;
            r[i] = 4 * K[i] + 2 * K[i + 1];
        }

	    /*right segment*/
        a[n-1] = 2;
        b[n-1] = 7;
        c[n-1] = 0;
        r[n-1] = 8 * K[n - 1] + K[n];

	    /*solves Ax = b with the Thomas algorithm*/
        for (int i = 1; i < n; i++) {
            double m = a[i] / b[i - 1];
            b[i] = b[i] - m * c[i - 1];
            r[i] = r[i] - m * r[i - 1];
        }

        p1[n-1] = r[n-1] / b[n-1];
        for (int i = n - 2; i >= 0; --i) { p1[i] = (r[i] - c[i] * p1[i + 1]) / b[i]; }

        for (int i = 0 ; i < n - 1 ; i++) { p2[i] = 2 * K[i + 1] - p1[i + 1]; }
        p2[n - 1] = 0.5 * (K[n] + p1[n - 1]);

        return new Pair<>(p1, p2);
    }


    // ******************** Resizing ******************************************
    private void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? size * 0.725 : size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate(size * 0.925 - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth(), size * 0.15);
        } else {
            valueText.relocate(size * 0.95 - valueText.getLayoutBounds().getWidth(), size * 0.15);
        }

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

        maxWidth = size * 0.15;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate(size * 0.95 - unitText.getLayoutBounds().getWidth(), size * 0.3275);

        averageText.setX(size * 0.05);
        highText.setX(size * 0.05);
        lowText.setX(size * 0.05);

        maxWidth = size * 0.75;
        fontSize = size * 0.05;
        subTitleText.setFont(Fonts.latoRegular(fontSize));
        if (subTitleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(subTitleText, maxWidth, fontSize); }
        subTitleText.relocate(size * 0.95 - subTitleText.getLayoutBounds().getWidth(), size * 0.9);
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            graphBounds = new Rectangle(size * 0.05, size * 0.5, size * 0.9, size * 0.39);

            stdDeviationArea.setX(graphBounds.getX());
            stdDeviationArea.setWidth(graphBounds.getWidth());

            averageLine.getStrokeDashArray().setAll(graphBounds.getWidth() * 0.01, graphBounds.getWidth() * 0.01);

            drawChart(gauge.getValue());
            sparkLine.setStrokeWidth(size * 0.01);
            dot.setRadius(size * 0.014);

            resizeStaticText();
            resizeDynamicText();
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(size * 0.025), new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(size * 0.025), Insets.EMPTY)));

        locale       = gauge.getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();

        titleText.setText(gauge.getTitle());
        subTitleText.setText(gauge.getSubTitle());
        resizeStaticText();

        titleText.setFill(gauge.getTitleColor());
        valueText.setFill(gauge.getValueColor());
        averageText.setFill(gauge.getAverageColor());
        highText.setFill(gauge.getValueColor());
        lowText.setFill(gauge.getValueColor());
        subTitleText.setFill(gauge.getSubTitleColor());
        sparkLine.setStroke(gauge.getBarColor());
        stdDeviationArea.setFill(Helper.getTranslucentColorFrom(gauge.getAverageColor(), 0.1));
        averageLine.setStroke(gauge.getAverageColor());
        dot.setFill(gauge.getBarColor());
    }
}
