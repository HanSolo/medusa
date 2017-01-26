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

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
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


/**
 * Created by hansolo on 16.12.16.
 */
public class TileClockSkin extends ClockSkinBase {
    private static final DateTimeFormatter DATE_FORMATER      = DateTimeFormatter.ofPattern("EE d");
    private static final double            CLOCK_SCALE_FACTOR = 0.75;
    private              double            size;
    private              double            clockSize;
    private              Path              minuteTickMarks;
    private              Path              hourTickMarks;
    private              Rectangle         hour;
    private              Rectangle         minute;
    private              Rectangle         second;
    private              Circle            knob;
    private              Text              title;
    private              Text              amPmText;
    private              Text              dateText;
    private              Text              text;
    private              Pane              pane;
    private              Rotate            hourRotate;
    private              Rotate            minuteRotate;
    private              Rotate            secondRotate;
    private              Group             shadowGroupHour;
    private              Group             shadowGroupMinute;
    private              Group             shadowGroupSecond;
    private              DropShadow        dropShadow;


    // ******************** Constructors **************************************
    public TileClockSkin(Clock gauge) {
        super(gauge);
        minuteRotate = new Rotate();
        hourRotate   = new Rotate();
        secondRotate = new Rotate();

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

        minuteTickMarks = new Path();
        minuteTickMarks.setFillRule(FillRule.EVEN_ODD);
        minuteTickMarks.setFill(null);
        minuteTickMarks.setStroke(clock.getMinuteColor());
        minuteTickMarks.setStrokeLineCap(StrokeLineCap.ROUND);

        hourTickMarks = new Path();
        hourTickMarks.setFillRule(FillRule.EVEN_ODD);
        hourTickMarks.setFill(null);
        hourTickMarks.setStroke(clock.getHourColor());
        hourTickMarks.setStrokeLineCap(StrokeLineCap.ROUND);

        hour = new Rectangle(3, 60);
        hour.setArcHeight(3);
        hour.setArcWidth(3);
        hour.setStroke(clock.getHourColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Rectangle(3, 96);
        minute.setArcHeight(3);
        minute.setArcWidth(3);
        minute.setStroke(clock.getMinuteColor());
        minute.getTransforms().setAll(minuteRotate);

        second = new Rectangle(1, 96);
        second.setArcHeight(1);
        second.setArcWidth(1);
        second.setStroke(clock.getSecondColor());
        second.getTransforms().setAll(secondRotate);
        second.setVisible(clock.isSecondsVisible());
        second.setManaged(clock.isSecondsVisible());

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

        shadowGroupHour.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setTextOrigin(VPos.TOP);
        Helper.enableNode(title, clock.isTitleVisible());

        amPmText = new Text(clock.getTime().get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");

        dateText = new Text("");
        Helper.enableNode(dateText, clock.isDateVisible());

        text = new Text("");
        Helper.enableNode(text, clock.isTextVisible());

        pane = new Pane(hourTickMarks, minuteTickMarks, title, amPmText, dateText, text, shadowGroupHour, shadowGroupMinute, shadowGroupSecond);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(PREFERRED_WIDTH * 0.025), new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(PREFERRED_WIDTH * 0.025), Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(title, clock.isTitleVisible());
            Helper.enableNode(text, clock.isTextVisible());
            Helper.enableNode(dateText, clock.isDateVisible());
            Helper.enableNode(second, clock.isSecondsVisible());
        } else if ("FINISHED".equals(EVENT_TYPE)) {

        }
    }

    private void drawTicks() {
        minuteTickMarks.setCache(false);
        hourTickMarks.setCache(false);
        minuteTickMarks.getElements().clear();
        hourTickMarks.getElements().clear();
        double  sinValue;
        double  cosValue;
        double  startAngle             = 180;
        double  angleStep              = 360 / 60;
        Point2D center                 = new Point2D(clockSize * 0.5, clockSize * 0.5);
        boolean hourTickMarksVisible   = clock.isHourTickMarksVisible();
        boolean minuteTickMarksVisible = clock.isMinuteTickMarksVisible();
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + clockSize * 0.405 * sinValue, center.getY() + clockSize * 0.405 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + clockSize * 0.435 * sinValue, center.getY() + clockSize * 0.435 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + clockSize * 0.465 * sinValue, center.getY() + clockSize * 0.465 * cosValue);

            if (counter % 5 == 0) {
                // Draw hour tickmark
                if (hourTickMarksVisible) {
                    hourTickMarks.setStrokeWidth(clockSize * 0.01);
                    hourTickMarks.getElements().add(new MoveTo(innerPoint.getX(), innerPoint.getY()));
                    hourTickMarks.getElements().add(new LineTo(outerPoint.getX(), outerPoint.getY()));
                } else if (minuteTickMarksVisible) {
                    minuteTickMarks.setStrokeWidth(clockSize * 0.005);
                    minuteTickMarks.getElements().add(new MoveTo(innerMinutePoint.getX(), innerMinutePoint.getY()));
                    minuteTickMarks.getElements().add(new LineTo(outerPoint.getX(), outerPoint.getY()));
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                minuteTickMarks.setStrokeWidth(clockSize * 0.005);
                minuteTickMarks.getElements().add(new MoveTo(innerMinutePoint.getX(), innerMinutePoint.getY()));
                minuteTickMarks.getElements().add(new LineTo(outerPoint.getX(), outerPoint.getY()));
            }
        }
        minuteTickMarks.setCache(true);
        minuteTickMarks.setCacheHint(CacheHint.QUALITY);
        hourTickMarks.setCache(true);
        hourTickMarks.setCacheHint(CacheHint.QUALITY);
    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        if (clock.isDiscreteHours()) {
            hourRotate.setAngle(TIME.getHour() * 30);
        } else {
            hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));
        }

        if (clock.isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (second.isVisible()) {
            if (clock.isDiscreteSeconds()) {
                secondRotate.setAngle(TIME.getSecond() * 6);
            } else {
                secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
            }
        }

        amPmText.setText(clock.getTime().get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");
        Helper.adjustTextSize(amPmText, 0.2 * size, size * 0.05);
        amPmText.setX((size - amPmText.getLayoutBounds().getWidth()) * 0.5);
        amPmText.setY(size * 0.4);

        dateText.setText(DATE_FORMATER.format(TIME).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.setX((size - dateText.getLayoutBounds().getWidth()) * 0.5);
        dateText.setY(size * 0.65);
    }

    @Override public void updateAlarms() {}


    // ******************** Resizing ******************************************
    private void resizeText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;

        title.setFont(Fonts.latoRegular(fontSize));
        title.setText(clock.getTitle());
        if (title.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(title, maxWidth, fontSize); }
        title.setX(size * 0.05);
        title.setY(size * 0.05);

        maxWidth = size * 0.2;
        fontSize = size * 0.05;
        amPmText.setText(clock.getTime().get(ChronoField.AMPM_OF_DAY) == 0 ? "AM" : "PM");
        Helper.adjustTextSize(amPmText, maxWidth, fontSize);
        amPmText.setX((size - amPmText.getLayoutBounds().getWidth()) * 0.5);
        amPmText.setY(size * 0.4);

        maxWidth = size * 0.6;
        dateText.setFont(Fonts.latoRegular(fontSize));
        if (dateText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(dateText, maxWidth, fontSize); }
        dateText.setX((size - dateText.getLayoutBounds().getWidth()) * 0.5);
        dateText.setY(size * 0.65);

        maxWidth = size * 0.9;
        fontSize = size * 0.05;
        text.setText(clock.getText());
        if (text.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(text, maxWidth, fontSize); }
        text.setX(size * 0.05);
        text.setY(size * 0.95);
    }

    @Override protected void resize() {
        double width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        double height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();
        size          = width < height ? width : height;
        clockSize     = size * CLOCK_SCALE_FACTOR;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((clock.getWidth() - size) * 0.5, (clock.getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            drawTicks();
            hourTickMarks.relocate((size - hourTickMarks.getLayoutBounds().getWidth()) * 0.5,
                                   (size - hourTickMarks.getLayoutBounds().getHeight()) * 0.5);
            minuteTickMarks.relocate((size - minuteTickMarks.getLayoutBounds().getWidth()) * 0.5,
                                   (size - minuteTickMarks.getLayoutBounds().getHeight()) * 0.5);

            hour.setFill(clock.getHourColor());
            hour.setCache(false);
            hour.setWidth(clockSize * 0.015);
            hour.setHeight(clockSize * 0.29);
            hour.setArcWidth(clockSize * 0.015);
            hour.setArcHeight(clockSize * 0.015);
            hour.setCache(true);
            hour.setCacheHint(CacheHint.ROTATE);
            hour.relocate((size - hour.getWidth()) * 0.5, size * 0.21 / CLOCK_SCALE_FACTOR);

            minute.setFill(clock.getMinuteColor());
            minute.setCache(false);
            minute.setWidth(clockSize * 0.015);
            minute.setHeight(clockSize * 0.47);
            minute.setArcWidth(clockSize * 0.015);
            minute.setArcHeight(clockSize * 0.015);
            minute.setCache(true);
            minute.setCacheHint(CacheHint.ROTATE);
            minute.relocate((size - minute.getWidth()) * 0.5, size * 0.11 / CLOCK_SCALE_FACTOR);

            second.setFill(clock.getSecondColor());
            second.setCache(false);
            second.setWidth(clockSize * 0.005);
            second.setHeight(clockSize * 0.47);
            second.setArcWidth(clockSize * 0.015);
            second.setArcHeight(clockSize * 0.015);
            second.setCache(true);
            second.setCacheHint(CacheHint.ROTATE);
            second.relocate((size - second.getWidth()) * 0.5, size * 0.11 / CLOCK_SCALE_FACTOR);

            knob.setFill(clock.getKnobColor());
            knob.setRadius(clockSize * 0.0225);
            knob.setCenterX(center);
            knob.setCenterY(center);

            resizeText();

            minuteRotate.setPivotX(minute.getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getHeight());
            hourRotate.setPivotX(hour.getWidth() * 0.5);
            hourRotate.setPivotY(hour.getHeight());
            secondRotate.setPivotX(second.getWidth() * 0.5);
            secondRotate.setPivotY(second.getHeight());
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(size * 0.025), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(size * 0.025), Insets.EMPTY)));

        shadowGroupHour.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        // Tick Marks
        minuteTickMarks.setStroke(clock.getMinuteColor());
        hourTickMarks.setStroke(clock.getHourColor());

        ZonedDateTime time = clock.getTime();

        updateTime(time);

        resizeText();

        title.setFill(clock.getTitleColor());
        dateText.setFill(clock.getDateColor());
        text.setFill(clock.getTextColor());
        amPmText.setFill(clock.getTextColor());
    }
}
