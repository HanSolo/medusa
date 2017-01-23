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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by hansolo on 28.01.16.
 */
public class ClockSkin extends ClockSkinBase {
    private static final DateTimeFormatter  DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE\ndd.MM.YYYY\nHH:mm:ss");
    private static final DateTimeFormatter  DATE_FORMATER       = DateTimeFormatter.ofPattern("EE d");
    private static final DateTimeFormatter  TIME_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm");
    private              Map<Alarm, Circle> alarmMap            = new ConcurrentHashMap<>();
    private              double             size;
    private              Canvas             sectionsAndAreasCanvas;
    private              GraphicsContext    sectionsAndAreasCtx;
    private              Canvas             tickCanvas;
    private              GraphicsContext    tickCtx;
    private              Rectangle          hour;
    private              Rectangle          minute;
    private              Rectangle          second;
    private              Circle             knob;
    private              Text               title;
    private              Text               dateText;
    private              Text               text;
    private              Pane               alarmPane;
    private              Pane               pane;
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
    public ClockSkin(Clock clock) {
        super(clock);

        minuteRotate      = new Rotate();
        hourRotate        = new Rotate();
        secondRotate      = new Rotate();

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

        hour = new Rectangle(3, 60);
        hour.setArcHeight(3);
        hour.setArcWidth(3);
        hour.setStroke(getSkinnable().getHourColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Rectangle(3, 96);
        minute.setArcHeight(3);
        minute.setArcWidth(3);
        minute.setStroke(getSkinnable().getMinuteColor());
        minute.getTransforms().setAll(minuteRotate);

        second = new Rectangle(1, 96);
        second.setArcHeight(1);
        second.setArcWidth(1);
        second.setStroke(getSkinnable().getSecondColor());
        second.getTransforms().setAll(secondRotate);
        second.setVisible(getSkinnable().isSecondsVisible());
        second.setManaged(getSkinnable().isSecondsVisible());

        knob = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, 4.5);
        knob.setStroke(Color.web("#282a3280"));

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

        dateText = new Text("");
        dateText.setVisible(getSkinnable().isDateVisible());
        dateText.setManaged(getSkinnable().isDateVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane(sectionsAndAreasCanvas, tickCanvas, alarmPane, title, dateText, text, shadowGroupHour, shadowGroupMinute, shadowGroupSecond);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {
            title.setVisible(getSkinnable().isTitleVisible());
            title.setManaged(getSkinnable().isTitleVisible());
            text.setVisible(getSkinnable().isTextVisible());
            text.setManaged(getSkinnable().isTextVisible());
            dateText.setVisible(getSkinnable().isDateVisible());
            dateText.setManaged(getSkinnable().isDateVisible());
            second.setVisible(getSkinnable().isSecondsVisible());
            second.setManaged(getSkinnable().isSecondsVisible());
            boolean alarmsVisible = getSkinnable().isAlarmsVisible();
            for (Shape shape : alarmMap.values()) {
                shape.setManaged(alarmsVisible);
                shape.setVisible(alarmsVisible);
            }
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = getSkinnable().getSections();
            highlightSections = getSkinnable().isHighlightSections();
            sectionsVisible   = getSkinnable().getSectionsVisible();
            areas             = getSkinnable().getAreas();
            highlightAreas    = getSkinnable().isHighlightAreas();
            areasVisible      = getSkinnable().getAreasVisible();
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {

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
        tickCtx.setLineCap(StrokeLineCap.ROUND);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.405 * sinValue, center.getY() + size * 0.405 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.435 * sinValue, center.getY() + size * 0.435 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.465 * sinValue, center.getY() + size * 0.465 * cosValue);

            if (counter % 5 == 0) {
                // Draw hour tickmark
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.01);
                    tickCtx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.005);
                    tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                tickCtx.setLineWidth(size * 0.005);
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    @Override public void updateTime(final ZonedDateTime TIME) {
        if (getSkinnable().isDiscreteHours()) {
            hourRotate.setAngle(TIME.getHour() * 30);
        } else {
            hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));
        }

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

        if (text.isVisible()) {
            text.setText(TIME_FORMATTER.format(TIME));
            Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);
        }

        if (dateText.isVisible()) {
            dateText.setText(DATE_FORMATER.format(TIME).toUpperCase());
            Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
            dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);
        }

        // Show all alarms within the next hour
        if (TIME.getMinute() == 0 && TIME.getSecond() == 0) Helper.drawAlarms(getSkinnable(), size, 0.02, 0.45, alarmMap, DATE_TIME_FORMATTER, TIME);

        // Highlight Areas and Sections
        if (highlightAreas | highlightSections) {
            sectionsAndAreasCtx.clearRect(0, 0, size, size);
            if (areasVisible) Helper.drawTimeAreas(getSkinnable(), sectionsAndAreasCtx, areas, size, 0.03, 0.03, 0.94, 0.94);
            if (sectionsVisible) Helper.drawTimeSections(getSkinnable(), sectionsAndAreasCtx, sections, size, 0.065, 0.065, 0.87, 0.87, 0.07);
        }
    }

    @Override public void updateAlarms() {
        alarmMap.clear();
        for (Alarm alarm : getSkinnable().getAlarms()) { alarmMap.put(alarm, new Circle()); }
    }


    // ******************** Resizing ******************************************
    @Override protected void resize() {
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

            hour.setFill(getSkinnable().getHourColor());
            hour.setCache(false);
            hour.setWidth(size * 0.015);
            hour.setHeight(size * 0.29);
            hour.setArcWidth(size * 0.015);
            hour.setArcHeight(size * 0.015);
            hour.setCache(true);
            hour.setCacheHint(CacheHint.ROTATE);
            hour.relocate((size - hour.getWidth()) * 0.5, size * 0.21);

            minute.setFill(getSkinnable().getMinuteColor());
            minute.setCache(false);
            minute.setWidth(size * 0.015);
            minute.setHeight(size * 0.47);
            minute.setArcWidth(size * 0.015);
            minute.setArcHeight(size * 0.015);
            minute.setCache(true);
            minute.setCacheHint(CacheHint.ROTATE);
            minute.relocate((size - minute.getWidth()) * 0.5, size * 0.03);

            second.setFill(getSkinnable().getSecondColor());
            second.setCache(false);
            second.setWidth(size * 0.005);
            second.setHeight(size * 0.47);
            second.setArcWidth(size * 0.015);
            second.setArcHeight(size * 0.015);
            second.setCache(true);
            second.setCacheHint(CacheHint.ROTATE);
            second.relocate((size - second.getWidth()) * 0.5, size * 0.03);

            knob.setFill(getSkinnable().getKnobColor());
            knob.setRadius(size * 0.0225);
            knob.setCenterX(center);
            knob.setCenterY(center);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateText.setFill(getSkinnable().getDateColor());
            dateText.setFont(Fonts.latoLight(size * 0.05));
            dateText.relocate((center - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(getSkinnable().getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            minuteRotate.setPivotX(minute.getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getHeight());
            hourRotate.setPivotX(hour.getWidth() * 0.5);
            hourRotate.setPivotY(hour.getHeight());
            secondRotate.setPivotX(second.getWidth() * 0.5);
            secondRotate.setPivotY(second.getHeight());
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / 250 * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        hour.setFill(clock.getHourColor());
        minute.setFill(clock.getMinuteColor());
        second.setFill(clock.getSecondColor());
        knob.setFill(clock.getKnobColor());
        title.setFill(clock.getTitleColor());
        dateText.setFill(clock.getDateColor());
        text.setFill(clock.getTextColor());

        shadowGroupHour.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections
        sectionsAndAreasCtx.clearRect(0, 0, size, size);
        if (areasVisible)    Helper.drawTimeAreas(getSkinnable(), sectionsAndAreasCtx, areas, size, 0.03, 0.03, 0.94, 0.94);
        if (sectionsVisible) Helper.drawTimeSections(getSkinnable(), sectionsAndAreasCtx, sections, size, 0.065, 0.065, 0.87, 0.87, 0.07);

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

        dateText.setText(DATE_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

        alarmPane.getChildren().setAll(alarmMap.values());
        Helper.drawAlarms(getSkinnable(), size, 0.02, 0.45, alarmMap, DATE_TIME_FORMATTER, time);
    }
}
