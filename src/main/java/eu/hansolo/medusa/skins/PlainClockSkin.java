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

import eu.hansolo.medusa.Alarm;
import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.TimeSection;
import eu.hansolo.medusa.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
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
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by hansolo on 29.01.16.
 */
public class PlainClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double             PREFERRED_WIDTH      = 250;
    private static final double             PREFERRED_HEIGHT     = 250;
    private static final double             MINIMUM_WIDTH        = 50;
    private static final double             MINIMUM_HEIGHT       = 50;
    private static final double             MAXIMUM_WIDTH        = 1024;
    private static final double             MAXIMUM_HEIGHT       = 1024;
    private static final DateTimeFormatter  DATE_TIME_FORMATTER  = DateTimeFormatter.ofPattern("EEEE\ndd.MM.YYYY\nHH:mm:ss");
    private static final DateTimeFormatter  DATE_NUMBER_FORMATER = DateTimeFormatter.ofPattern("d");
    private static final DateTimeFormatter  TIME_FORMATTER       = DateTimeFormatter.ofPattern("HH:mm");
    private              Map<Alarm, Circle> alarmMap             = new ConcurrentHashMap<>();
    private              double             size;
    private              Canvas             sectionsAndAreasCanvas;
    private              GraphicsContext    sectionsAndAreasCtx;
    private              Canvas             tickCanvas;
    private              GraphicsContext    tickCtx;
    private              Path               hour;
    private              Path               minute;
    private              Path               second;
    private              Circle             knob;
    private              Text               title;
    private              Text               dateNumber;
    private              Text               text;
    private              Pane               pane;
    private              Pane               alarmPane;
    private              Rotate             hourRotate;
    private              Rotate             minuteRotate;
    private              Rotate             secondRotate;
    private              Group              shadowGroupHour;
    private              Group              shadowGroupMinute;
    private              Group              shadowGroupSecond;
    private              DropShadow         dropShadow;
    private              List<TimeSection>  sections;
    private              List<TimeSection>  areas;
    private              boolean            sectionsVisible;
    private              boolean            highlightSections;
    private              boolean            areasVisible;
    private              boolean            highlightAreas;


    // ******************** Constructors **************************************
    public PlainClockSkin(Clock clock) {
        super(clock);

        minuteRotate      = new Rotate();
        hourRotate        = new Rotate();
        secondRotate      = new Rotate();

        sections          = clock.getSections();
        areas             = clock.getAreas();

        sections          = clock.getSections();
        highlightSections = clock.isHighlightSections();
        sectionsVisible   = clock.getSectionsVisible();
        areas             = clock.getAreas();
        highlightAreas    = clock.isHighlightAreas();
        areasVisible      = clock.getAreasVisible();

        updateAlarms();

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

        sectionsAndAreasCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionsAndAreasCtx    = sectionsAndAreasCanvas.getGraphicsContext2D();

        tickCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        tickCtx    = tickCanvas.getGraphicsContext2D();

        alarmPane = new Pane();

        hour  = new Path();
        hour.setFillRule(FillRule.EVEN_ODD);
        hour.setStroke(null);
        hour.getTransforms().setAll(hourRotate);

        minute = new Path();
        minute.setFillRule(FillRule.EVEN_ODD);
        minute.setStroke(null);
        minute.getTransforms().setAll(minuteRotate);

        second = new Path();
        second.setFillRule(FillRule.EVEN_ODD);
        second.setStroke(null);
        second.getTransforms().setAll(secondRotate);
        second.setVisible(getSkinnable().isSecondsVisible());
        second.setManaged(getSkinnable().isSecondsVisible());

        knob = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.0148448);
        knob.setStroke(null);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroupHour   = new Group(hour);
        shadowGroupMinute = new Group(minute);
        shadowGroupSecond = new Group(second, knob);

        shadowGroupHour.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(getSkinnable().isTitleVisible());
        title.setManaged(getSkinnable().isTitleVisible());

        dateNumber = new Text("");
        dateNumber.setVisible(getSkinnable().isDateVisible());
        dateNumber.setManaged(getSkinnable().isDateVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane(sectionsAndAreasCanvas, tickCanvas, alarmPane, title, dateNumber, text, shadowGroupHour, shadowGroupMinute, shadowGroupSecond);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        if (getSkinnable().isAnimated()) {
            getSkinnable().currentTimeProperty().addListener(o -> updateTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(getSkinnable().getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId()))));
        } else {
            getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
        }
        getSkinnable().getAlarms().addListener((ListChangeListener<Alarm>) c -> {
            updateAlarms();
            redraw();
        });
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
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            title.setVisible(getSkinnable().isTitleVisible());
            title.setManaged(getSkinnable().isTitleVisible());
            text.setVisible(getSkinnable().isTextVisible());
            text.setManaged(getSkinnable().isTextVisible());
            dateNumber.setVisible(getSkinnable().isDateVisible());
            dateNumber.setManaged(getSkinnable().isDateVisible());
            second.setVisible(getSkinnable().isSecondsVisible());
            second.setManaged(getSkinnable().isSecondsVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = getSkinnable().getSections();
            highlightSections = getSkinnable().isHighlightSections();
            sectionsVisible   = getSkinnable().getSectionsVisible();
            areas             = getSkinnable().getAreas();
            highlightAreas    = getSkinnable().isHighlightAreas();
            areasVisible      = getSkinnable().getAreasVisible();
            redraw();
        }
    }


    // ******************** Canvas ********************************************
    private void drawTicks() {
        double  sinValue;
        double  cosValue;
        double  startAngle             = 180;
        double  angleStep              = 360 / 60;
        Point2D center                 = new Point2D(size * 0.5, size * 0.5);
        Color   hourTickMarkColor      = getSkinnable().getHourTickMarkColor();
        Color   minuteTickMarkColor    = getSkinnable().getMinuteTickMarkColor();
        boolean hourTickMarksVisible   = getSkinnable().isHourTickMarksVisible();
        boolean minuteTickMarksVisible = getSkinnable().isMinuteTickMarksVisible();
        tickCtx.clearRect(0, 0, size, size);
        tickCtx.setLineCap(StrokeLineCap.BUTT);
        tickCtx.setLineWidth(size * 0.00539811);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.40350877 * sinValue, center.getY() + size * 0.40350877 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.451417 * sinValue, center.getY() + size * 0.451417 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.47435897 * sinValue, center.getY() + size * 0.47435897 * cosValue);

            if (counter % 5 == 0) {
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createHourPointer() {
        double width  = size * 0.04723347;
        double height = size * 0.35897436;
        hour.setCache(false);
        hour.getElements().clear();
        hour.getElements().add(new MoveTo(0.18571428571428572 * width, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(0.18571428571428572 * width, 0.9116541353383458 * height,
                                                0.32857142857142857 * width, 0.8928571428571429 * height,
                                                0.5 * width, 0.8928571428571429 * height));
        hour.getElements().add(new CubicCurveTo(0.6714285714285714 * width, 0.8928571428571429 * height,
                                                0.8142857142857143 * width, 0.9116541353383458 * height,
                                                0.8142857142857143 * width, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(0.8142857142857143 * width, 0.956766917293233 * height,
                                                0.6714285714285714 * width, 0.9755639097744361 * height,
                                                0.5 * width, 0.9755639097744361 * height));
        hour.getElements().add(new CubicCurveTo(0.32857142857142857 * width, 0.9755639097744361 * height,
                                                0.18571428571428572 * width, 0.956766917293233 * height,
                                                0.18571428571428572 * width, 0.9342105263157895 * height));
        hour.getElements().add(new ClosePath());
        hour.getElements().add(new MoveTo(0.0, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(0.0, 0.9699248120300752 * height,
                                                0.22857142857142856 * width, height,
                                                0.5 * width, height));
        hour.getElements().add(new CubicCurveTo(0.7714285714285715 * width, height,
                                                width, 0.9699248120300752 * height,
                                                width, 0.9342105263157895 * height));
        hour.getElements().add(new CubicCurveTo(width, 0.9116541353383458 * height,
                                                0.9142857142857143 * width, 0.8928571428571429 * height,
                                                0.7857142857142857 * width, 0.8796992481203008 * height));
        hour.getElements().add(new CubicCurveTo(0.7857142857142857 * width, 0.8796992481203008 * height,
                                                0.7857142857142857 * width, 0.07706766917293233 * height,
                                                0.7857142857142857 * width, 0.07706766917293233 * height));
        hour.getElements().add(new LineTo(0.5 * width, 0.0));
        hour.getElements().add(new LineTo(0.21428571428571427 * width, 0.07706766917293233 * height));
        hour.getElements().add(new CubicCurveTo(0.21428571428571427 * width, 0.07706766917293233 * height,
                                                0.21428571428571427 * width, 0.8796992481203008 * height,
                                                0.21428571428571427 * width, 0.8796992481203008 * height));
        hour.getElements().add(new CubicCurveTo(0.08571428571428572 * width, 0.8928571428571429 * height,
                                                0.0, 0.9116541353383458 * height,
                                                0.0, 0.9342105263157895 * height));
        hour.getElements().add(new ClosePath());
        hour.setCache(true);
        hour.setCacheHint(CacheHint.ROTATE);
    }

    private void createMinutePointer() {
        double width  = size * 0.04723347;
        double height = size * 0.47503374;
        minute.setCache(false);
        minute.getElements().clear();
        minute.getElements().add(new MoveTo(0.18571428571428572 * width, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.18571428571428572 * width, 0.9332386363636364 * height,
                                                  0.32857142857142857 * width, 0.9190340909090909 * height,
                                                  0.5 * width, 0.9190340909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.6714285714285714 * width, 0.9190340909090909 * height,
                                                  0.8142857142857143 * width, 0.9332386363636364 * height,
                                                  0.8142857142857143 * width, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.8142857142857143 * width, 0.9673295454545454 * height,
                                                  0.6714285714285714 * width, 0.9815340909090909 * height,
                                                  0.5 * width, 0.9815340909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.32857142857142857 * width, 0.9815340909090909 * height,
                                                  0.18571428571428572 * width, 0.9673295454545454 * height,
                                                  0.18571428571428572 * width, 0.9502840909090909 * height));
        minute.getElements().add(new ClosePath());
        minute.getElements().add(new MoveTo(0.0, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(0.0, 0.9772727272727273 * height,
                                                  0.22857142857142856 * width, height,
                                                  0.5 * width, height));
        minute.getElements().add(new CubicCurveTo(0.7714285714285715 * width, height,
                                                  width, 0.9772727272727273 * height,
                                                  width, 0.9502840909090909 * height));
        minute.getElements().add(new CubicCurveTo(width, 0.9318181818181818 * height,
                                                  0.9 * width, 0.9147727272727273 * height,
                                                  0.7428571428571429 * width, 0.90625 * height));
        minute.getElements().add(new CubicCurveTo(0.7428571428571429 * width, 0.90625 * height,
                                                  0.7428571428571429 * width, 0.05113636363636364 * height,
                                                  0.7428571428571429 * width, 0.05113636363636364 * height));
        minute.getElements().add(new LineTo(0.5 * width, 0.0));
        minute.getElements().add(new LineTo(0.2571428571428571 * width, 0.05113636363636364 * height));
        minute.getElements().add(new CubicCurveTo(0.2571428571428571 * width, 0.05113636363636364 * height,
                                                  0.2571428571428571 * width, 0.90625 * height,
                                                  0.2571428571428571 * width, 0.90625 * height));
        minute.getElements().add(new CubicCurveTo(0.1 * width, 0.9147727272727273 * height,
                                                  0.0, 0.9318181818181818 * height,
                                                  0.0, 0.9502840909090909 * height));
        minute.getElements().add(new ClosePath());
        minute.setCache(true);
        minute.setCacheHint(CacheHint.ROTATE);
    }

    private void createSecondPointer() {
        double width  = size * 0.04723347;
        double height = size * 0.50404858;
        second.setCache(false);
        second.getElements().clear();
        second.getElements().add(new MoveTo(0.18571428571428572 * width, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(0.18571428571428572 * width, 0.9370816599732262 * height,
                                                  0.32857142857142857 * width, 0.9236947791164659 * height,
                                                  0.5 * width, 0.9236947791164659 * height));
        second.getElements().add(new CubicCurveTo(0.6714285714285714 * width, 0.9236947791164659 * height,
                                                  0.8142857142857143 * width, 0.9370816599732262 * height,
                                                  0.8142857142857143 * width, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(0.8142857142857143 * width, 0.9692101740294511 * height,
                                                  0.6714285714285714 * width, 0.9825970548862115 * height,
                                                  0.5 * width, 0.9825970548862115 * height));
        second.getElements().add(new CubicCurveTo(0.32857142857142857 * width, 0.9825970548862115 * height,
                                                  0.18571428571428572 * width, 0.9692101740294511 * height,
                                                  0.18571428571428572 * width, 0.9531459170013387 * height));
        second.getElements().add(new ClosePath());
        second.getElements().add(new MoveTo(0.0, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(0.0, 0.9785809906291834 * height,
                                                  0.22857142857142856 * width, height,
                                                  0.5 * width, height));
        second.getElements().add(new CubicCurveTo(0.7714285714285715 * width, height,
                                                  width, 0.9785809906291834 * height,
                                                  width, 0.9531459170013387 * height));
        second.getElements().add(new CubicCurveTo(width, 0.9330655957161981 * height,
                                                  0.8714285714285714 * width, 0.9170013386880856 * height,
                                                  0.6857142857142857 * width, 0.9089692101740294 * height));
        second.getElements().add(new CubicCurveTo(0.6857142857142857 * width, 0.9089692101740294 * height,
                                                  0.6857142857142857 * width, 0.04819277108433735 * height,
                                                  0.6857142857142857 * width, 0.04819277108433735 * height));
        second.getElements().add(new LineTo(0.5 * width, 0.0));
        second.getElements().add(new LineTo(0.3142857142857143 * width, 0.04819277108433735 * height));
        second.getElements().add(new CubicCurveTo(0.3142857142857143 * width, 0.04819277108433735 * height,
                                                  0.3142857142857143 * width, 0.9089692101740294 * height,
                                                  0.3142857142857143 * width, 0.9089692101740294 * height));
        second.getElements().add(new CubicCurveTo(0.12857142857142856 * width, 0.9170013386880856 * height,
                                                  0.0, 0.9330655957161981 * height,
                                                  0.0, 0.9531459170013387 * height));
        second.getElements().add(new ClosePath());
        second.setCache(true);
        second.setCacheHint(CacheHint.ROTATE);
    }

    private void updateAlarms() {
        alarmMap.clear();
        for (Alarm alarm : getSkinnable().getAlarms()) { alarmMap.put(alarm, new Circle()); }
    }

    public void updateTime(final ZonedDateTime TIME) {
        if (getSkinnable().isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (second.isVisible()) {
            if (getSkinnable().isDiscreteSeconds()) {
                secondRotate.setAngle(TIME.getSecond() * 6);
            } else {
                secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
            }
        }

        if (getSkinnable().isDiscreteHours()) {
            hourRotate.setAngle(TIME.getHour() * 30);
        } else {
            hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));
        }

        if (text.isVisible()) {
            text.setText(TIME_FORMATTER.format(TIME));
            Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);
        }

        if (dateNumber.isVisible()) {
            dateNumber.setText(DATE_NUMBER_FORMATER.format(TIME).toUpperCase());
            Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
            dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.6), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);
        }

        // Show all alarms within the next hour
        if (TIME.getMinute() == 0 && TIME.getSecond() == 0) Helper.drawAlarms(getSkinnable(), size, 0.015, 0.46, alarmMap, DATE_TIME_FORMATTER, TIME);

        // Highlight Areas and Sections
        if (highlightAreas | highlightSections) {
            sectionsAndAreasCtx.clearRect(0, 0, size, size);
            if (areasVisible) Helper.drawTimeAreas(getSkinnable(), sectionsAndAreasCtx, areas, size, 0.025, 0.025, 0.95, 0.95);
            if (sectionsVisible) Helper.drawTimeSections(getSkinnable(), sectionsAndAreasCtx, sections, size, 0.06, 0.06, 0.88, 0.88, 0.07);
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            sectionsAndAreasCanvas.setWidth(size);
            sectionsAndAreasCanvas.setHeight(size);

            tickCanvas.setWidth(size);
            tickCanvas.setHeight(size);

            alarmPane.setMaxSize(size, size);

            createHourPointer();
            hour.setFill(getSkinnable().getHourColor());
            hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, size * 0.16464238);

            createMinutePointer();
            minute.setFill(getSkinnable().getMinuteColor());
            minute.relocate((size - minute.getLayoutBounds().getWidth()) * 0.5, size * 0.048583);

            createSecondPointer();
            second.setFill(getSkinnable().getSecondColor());
            second.relocate((size - second.getLayoutBounds().getWidth()) * 0.5, size * 0.01956815);

            knob.setCenterX(center);
            knob.setCenterY(center);
            knob.setRadius(size * 0.0148448);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateNumber.setFill(getSkinnable().getDateColor());
            dateNumber.setFont(Fonts.latoLight(size * 0.06666667));
            dateNumber.relocate((center - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.6), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(getSkinnable().getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            hourRotate.setPivotX(hour.getLayoutBounds().getWidth() * 0.5);
            hourRotate.setPivotY(hour.getLayoutBounds().getHeight() * 0.93421053);
            minuteRotate.setPivotX(minute.getLayoutBounds().getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getLayoutBounds().getHeight() * 0.95028409);
            secondRotate.setPivotX(second.getLayoutBounds().getWidth() * 0.5);
            secondRotate.setPivotY(second.getLayoutBounds().getHeight() * 0.95314592);
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroupHour.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections
        sectionsAndAreasCtx.clearRect(0, 0, size, size);
        if (areasVisible) Helper.drawTimeAreas(getSkinnable(), sectionsAndAreasCtx, areas, size, 0.025, 0.025, 0.95, 0.95);
        if (sectionsVisible) Helper.drawTimeSections(getSkinnable(), sectionsAndAreasCtx, sections, size, 0.06, 0.06, 0.88, 0.88, 0.07);

        // Tick Marks
        tickCanvas.setCache(false);
        drawTicks();
        tickCanvas.setCache(true);
        tickCanvas.setCacheHint(CacheHint.QUALITY);

        ZonedDateTime time = getSkinnable().getTime();

        updateTime(time);

        title.setText(getSkinnable().getTitle());
        Helper.adjustTextSize(title, 0.6 * size, size * 0.12);
        title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

        text.setText(TIME_FORMATTER.format(time));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        dateNumber.setText(DATE_NUMBER_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
        dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.6), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);

        knob.setFill(new RadialGradient(0, 0, size * 0.5, size * 0.5, size * 0.0148448,
                                        false, CycleMethod.NO_CYCLE,
                                        new Stop(0.0, Color.rgb(1, 2, 1)),
                                        new Stop(0.4, Color.rgb(15, 15, 15)),
                                        new Stop(0.6, Color.rgb(153, 153, 153)),
                                        new Stop(0.8, Color.rgb(27, 10, 27)),
                                        new Stop(1.0, Color.rgb(27, 10, 27))));

        alarmPane.getChildren().setAll(alarmMap.values());
        Helper.drawAlarms(getSkinnable(), size, 0.015, 0.46, alarmMap, DATE_TIME_FORMATTER, time);
    }
}
