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
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static eu.hansolo.medusa.tools.Helper.enableNode;


/**
 * Created by hansolo on 29.01.16.
 */
public class DBClockSkin extends ClockSkinBase {
    private static final DateTimeFormatter  DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE\ndd.MM.YYYY\nHH:mm:ss");
    private static final DateTimeFormatter  TIME_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm");
    private              Map<Alarm, Circle> alarmMap            = new ConcurrentHashMap<>();
    private              double             size;
    private              Canvas             sectionsAndAreasCanvas;
    private              GraphicsContext    sectionsAndAreasCtx;
    private              Canvas             tickCanvas;
    private              GraphicsContext    tickCtx;
    private              Rectangle          hour;
    private              Rectangle          minute;
    private              Path               second;
    private              Circle             knob;
    private              Text               title;
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
    public DBClockSkin(Clock clock) {
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
    @Override protected void initGraphics() {
        // Set initial size
        if (Double.compare(clock.getPrefWidth(), 0.0) <= 0 || Double.compare(clock.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(clock.getWidth(), 0.0) <= 0 || Double.compare(clock.getHeight(), 0.0) <= 0) {
            if (clock.getPrefWidth() > 0 && clock.getPrefHeight() > 0) {
                clock.setPrefSize(clock.getPrefWidth(), clock.getPrefHeight());
            } else {
                clock.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        sectionsAndAreasCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionsAndAreasCtx    = sectionsAndAreasCanvas.getGraphicsContext2D();

        tickCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        tickCtx    = tickCanvas.getGraphicsContext2D();

        alarmPane = new Pane();

        hour  = new Rectangle(3, 60);
        hour.setArcHeight(3);
        hour.setArcWidth(3);
        hour.setStroke(null);
        hour.setFill(clock.getHourColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Rectangle(3, 96);
        minute.setArcHeight(3);
        minute.setArcWidth(3);
        minute.setStroke(null);
        minute.setFill(clock.getMinuteColor());
        minute.getTransforms().setAll(minuteRotate);

        second = new Path();
        second.setFillRule(FillRule.EVEN_ODD);
        second.setStroke(null);
        second.setFill(clock.getSecondColor());
        second.getTransforms().setAll(secondRotate);
        enableNode(second, clock.isSecondsVisible());

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        knob = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, 4.5);
        knob.setStroke(null);
        knob.setFill(clock.getKnobColor());
        knob.setEffect(dropShadow);

        shadowGroupHour   = new Group(hour);
        shadowGroupMinute = new Group(minute);
        shadowGroupSecond = new Group(second);

        shadowGroupHour.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(clock.isTitleVisible());
        title.setManaged(clock.isTitleVisible());

        text = new Text("");
        text.setVisible(clock.isTextVisible());
        text.setManaged(clock.isTextVisible());

        pane = new Pane(sectionsAndAreasCanvas, tickCanvas, alarmPane, title, text, shadowGroupHour, shadowGroupMinute, shadowGroupSecond, knob);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {
            title.setVisible(clock.isTitleVisible());
            title.setManaged(clock.isTitleVisible());
            text.setVisible(clock.isTextVisible());
            text.setManaged(clock.isTextVisible());
            second.setVisible(clock.isSecondsVisible());
            second.setManaged(clock.isSecondsVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = clock.getSections();
            highlightSections = clock.isHighlightSections();
            sectionsVisible   = clock.getSectionsVisible();
            areas             = clock.getAreas();
            highlightAreas    = clock.isHighlightAreas();
            areasVisible      = clock.getAreasVisible();
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
        Color   hourTickMarkColor      = clock.getHourTickMarkColor();
        Color   minuteTickMarkColor    = clock.getMinuteTickMarkColor();
        boolean hourTickMarksVisible   = clock.isHourTickMarksVisible();
        boolean minuteTickMarksVisible = clock.isMinuteTickMarksVisible();
        tickCtx.clearRect(0, 0, size, size);
        tickCtx.setLineCap(StrokeLineCap.BUTT);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerMainPoint   = new Point2D(center.getX() + size * 0.335 * sinValue, center.getY() + size * 0.335 * cosValue);
            Point2D innerPoint       = new Point2D(center.getX() + size * 0.365 * sinValue, center.getY() + size * 0.365 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.425 * sinValue, center.getY() + size * 0.425 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.465 * sinValue, center.getY() + size * 0.465 * cosValue);

            if (counter % 5 == 0 && counter % 3 == 0) {
                // Draw 12, 3, 6, 9 hour tickmark
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.0375);
                    tickCtx.strokeLine(innerMainPoint.getX(), innerMainPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.02);
                    tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 5 == 0) {
                // Draw hour tickmark
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.0375);
                    tickCtx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible){
                    tickCtx.setLineWidth(size * 0.02);
                    tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.setLineWidth(size * 0.02);
                tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createSecondPointer() {
        double width  = size * 0.11866667;
        double height = size * 0.46266667;
        second.setCache(false);
        second.getElements().clear();
        second.getElements().add(new MoveTo(0.1348314606741573 * width, 0.4365994236311239 * height));
        second.getElements().add(new CubicCurveTo(0.1348314606741573 * width, 0.38328530259365995 * height,
                                                  0.29775280898876405 * width, 0.3414985590778098 * height,
                                                  0.5 * width, 0.3414985590778098 * height));
        second.getElements().add(new CubicCurveTo(0.702247191011236 * width, 0.3414985590778098 * height,
                                                  0.8651685393258427 * width, 0.38328530259365995 * height,
                                                  0.8651685393258427 * width, 0.4365994236311239 * height));
        second.getElements().add(new CubicCurveTo(0.8651685393258427 * width, 0.4884726224783862 * height,
                                                  0.702247191011236 * width, 0.5302593659942363 * height,
                                                  0.5 * width, 0.5302593659942363 * height));
        second.getElements().add(new CubicCurveTo(0.29775280898876405 * width, 0.5302593659942363 * height,
                                                  0.1348314606741573 * width, 0.4884726224783862 * height,
                                                  0.1348314606741573 * width, 0.4365994236311239 * height));
        second.getElements().add(new ClosePath());
        second.getElements().add(new MoveTo(0.0, 0.4365994236311239 * height));
        second.getElements().add(new CubicCurveTo(0.0, 0.5 * height,
                                                  0.1853932584269663 * width, 0.553314121037464 * height,
                                                  0.42134831460674155 * width, 0.5634005763688761 * height));
        second.getElements().add(new CubicCurveTo(0.42134831460674155 * width, 0.5634005763688761 * height,
                                                  0.398876404494382 * width, height,
                                                  0.398876404494382 * width, height));
        second.getElements().add(new LineTo(0.601123595505618 * width, height));
        second.getElements().add(new CubicCurveTo(0.601123595505618 * width, height,
                                                  0.5786516853932584 * width, 0.5634005763688761 * height,
                                                  0.5786516853932584 * width, 0.5634005763688761 * height));
        second.getElements().add(new CubicCurveTo(0.8146067415730337 * width, 0.553314121037464 * height,
                                                  width, 0.5 * height,
                                                  width, 0.4365994236311239 * height));
        second.getElements().add(new CubicCurveTo(width, 0.3703170028818444 * height,
                                                  0.8089887640449438 * width, 0.3170028818443804 * height,
                                                  0.5617977528089888 * width, 0.30835734870317005 * height));
        second.getElements().add(new CubicCurveTo(0.5617977528089888 * width, 0.30835734870317005 * height,
                                                  0.5449438202247191 * width, 0.0,
                                                  0.5449438202247191 * width, 0.0));
        second.getElements().add(new LineTo(0.4550561797752809 * width, 0.0));
        second.getElements().add(new CubicCurveTo(0.4550561797752809 * width, 0.0,
                                                  0.43820224719101125 * width, 0.30835734870317005 * height,
                                                  0.43820224719101125 * width, 0.30835734870317005 * height));
        second.getElements().add(new CubicCurveTo(0.19101123595505617 * width, 0.3170028818443804 * height,
                                                  0.0, 0.3703170028818444 * height,
                                                  0.0, 0.4365994236311239 * height));
        second.getElements().add(new ClosePath());
        second.setCache(true);
        second.setCacheHint(CacheHint.ROTATE);
    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        if (clock.isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (clock.isDiscreteSeconds() && second.isVisible()) {
            secondRotate.setAngle(TIME.getSecond() * 6);
        } else {
            secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
        }

        if (clock.isDiscreteHours()) {
            hourRotate.setAngle(TIME.getHour() * 30);
        } else {
            hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));
        }

        if (text.isVisible()) {
            text.setText(TIME_FORMATTER.format(TIME));
            Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);
        }

        // Show all alarms within the next hour
        if (TIME.getMinute() == 0 && TIME.getSecond() == 0) Helper.drawAlarms(clock, size, 0.02, 0.445, alarmMap, DATE_TIME_FORMATTER, TIME);

        // Highlight Areas and Sections
        if (highlightAreas | highlightSections) {
            sectionsAndAreasCtx.clearRect(0, 0, size, size);
            if (areasVisible) Helper.drawTimeAreas(clock, sectionsAndAreasCtx, areas, size, 0.035, 0.035, 0.93, 0.93);
            if (sectionsVisible) Helper.drawTimeSections(clock, sectionsAndAreasCtx, sections, size, 0.056, 0.056, 0.89, 0.89, 0.0395);
        }
    }

    @Override public void updateAlarms() {
        alarmMap.clear();
        for (Alarm alarm : clock.getAlarms()) { alarmMap.put(alarm, new Circle()); }
    }


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        double width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        double height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((clock.getWidth() - size) * 0.5, (clock.getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            sectionsAndAreasCanvas.setWidth(size);
            sectionsAndAreasCanvas.setHeight(size);

            tickCanvas.setWidth(size);
            tickCanvas.setHeight(size);

            alarmPane.setMaxSize(size, size);

            hour.setFill(clock.getHourColor());
            hour.setCache(false);
            hour.setWidth(size * 0.05);
            hour.setHeight(size * 0.3125);
            hour.setCache(true);
            hour.setCacheHint(CacheHint.ROTATE);
            hour.relocate((size - hour.getWidth()) * 0.5, center - size * 0.3125);

            minute.setFill(clock.getMinuteColor());
            minute.setCache(false);
            minute.setWidth(size * 0.038);
            minute.setHeight(size * 0.4375);
            minute.setCache(true);
            minute.setCacheHint(CacheHint.ROTATE);
            minute.relocate((size - minute.getWidth()) * 0.5, center - size * 0.4375);

            createSecondPointer();
            second.setFill(clock.getSecondColor());
            second.relocate((size - second.getLayoutBounds().getWidth()) * 0.5, center - second.getLayoutBounds().getHeight());

            knob.setFill(clock.getKnobColor());
            knob.setRadius(size * 0.05);
            knob.setCenterX(center);
            knob.setCenterY(center);

            title.setFill(clock.getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            text.setFill(clock.getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            minuteRotate.setPivotX(minute.getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getHeight());
            hourRotate.setPivotX(hour.getWidth() * 0.5);
            hourRotate.setPivotY(hour.getHeight());
            secondRotate.setPivotX(second.getLayoutBounds().getWidth() * 0.5);
            secondRotate.setPivotY(second.getLayoutBounds().getHeight());
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        hour.setFill(clock.getHourColor());
        minute.setFill(clock.getMinuteColor());
        second.setFill(clock.getSecondColor());
        knob.setFill(clock.getKnobColor());
        title.setFill(clock.getTitleColor());
        text.setFill(clock.getTextColor());

        shadowGroupHour.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections
        sectionsAndAreasCtx.clearRect(0, 0, size, size);
        if (areasVisible) Helper.drawTimeAreas(clock, sectionsAndAreasCtx, areas, size, 0.035, 0.035, 0.93, 0.93);
        if (sectionsVisible) Helper.drawTimeSections(clock, sectionsAndAreasCtx, sections, size, 0.056, 0.056, 0.89, 0.89, 0.0395);

        // Tick Marks
        tickCanvas.setCache(false);
        drawTicks();
        tickCanvas.setCache(true);
        tickCanvas.setCacheHint(CacheHint.QUALITY);

        ZonedDateTime time = clock.getTime();

        updateTime(time);

        title.setText(clock.getTitle());
        Helper.adjustTextSize(title, 0.6 * size, size * 0.12);
        title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

        text.setText(TIME_FORMATTER.format(time));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

        alarmPane.getChildren().setAll(alarmMap.values());
        Helper.drawAlarms(clock, size, 0.02, 0.445, alarmMap, DATE_TIME_FORMATTER, time);
    }
}
