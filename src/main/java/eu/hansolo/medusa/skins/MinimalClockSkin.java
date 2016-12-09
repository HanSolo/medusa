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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;


/**
 * Created by hansolo on 04.04.16.
 */
public class MinimalClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH     = 250;
    private static final double            PREFERRED_HEIGHT    = 250;
    private static final double            MINIMUM_WIDTH       = 50;
    private static final double            MINIMUM_HEIGHT      = 50;
    private static final double            MAXIMUM_WIDTH       = 1024;
    private static final double            MAXIMUM_HEIGHT      = 1024;
    private static final DateTimeFormatter DATE_TEXT_FORMATTER = DateTimeFormatter.ofPattern("ccc., dd. MMM.");
    private static final DateTimeFormatter HOUR_FORMATTER      = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter MINUTE_FORMATTER    = DateTimeFormatter.ofPattern("mm");
    private double            size;
    private Circle            secondBackgroundCircle;
    private Text              dateText;
    private Text              hour;
    private Text              minute;
    private Circle            minuteCircle;
    private Arc               secondArc;
    private Pane              pane;
    private DoubleProperty    minuteAngle;
    private Timeline          timeline;


    // ******************** Constructors **************************************
    public MinimalClockSkin(Clock clock) {
        super(clock);

        minuteAngle = new DoublePropertyBase(-1) {
            @Override public Object getBean() { return clock; }
            @Override public String getName() { return "minuteAngle"; }
        };
        timeline    = new Timeline();

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

        ZonedDateTime time = getSkinnable().getTime();

        secondBackgroundCircle = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.48);
        secondBackgroundCircle.setStrokeWidth(PREFERRED_WIDTH * 0.008);
        secondBackgroundCircle.setStrokeType(StrokeType.CENTERED);
        secondBackgroundCircle.setStrokeLineCap(StrokeLineCap.ROUND);
        secondBackgroundCircle.setFill(null);
        secondBackgroundCircle.setStroke(Helper.getTranslucentColorFrom(getSkinnable().getSecondColor(), 0.2));
        secondBackgroundCircle.setVisible(getSkinnable().isSecondsVisible());
        secondBackgroundCircle.setManaged(getSkinnable().isSecondsVisible());

        dateText = new Text(DATE_TEXT_FORMATTER.format(time));
        dateText.setVisible(getSkinnable().isDateVisible());
        dateText.setManaged(getSkinnable().isDateVisible());

        hour = new Text(HOUR_FORMATTER.format(time));
        hour.setFill(getSkinnable().getHourColor());

        minute = new Text(MINUTE_FORMATTER.format(time));
        minute.setFill(getSkinnable().getMinuteColor());

        minuteCircle = new Circle(0.075 * PREFERRED_WIDTH);

        secondArc = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.96, PREFERRED_WIDTH * 0.48, 90, (-6 * getSkinnable().getTime().getSecond()));
        secondArc.setStrokeWidth(PREFERRED_WIDTH * 0.008);
        secondArc.setStrokeType(StrokeType.CENTERED);
        secondArc.setStrokeLineCap(StrokeLineCap.BUTT);
        secondArc.setFill(null);
        secondArc.setStroke(getSkinnable().getSecondColor());
        secondArc.setVisible(getSkinnable().isSecondsVisible());
        secondArc.setManaged(getSkinnable().isSecondsVisible());

        pane = new Pane(secondBackgroundCircle, dateText, hour, secondArc, minuteCircle, minute);
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), new Insets(PREFERRED_WIDTH * 0.04))));

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
        minuteAngle.addListener(o -> moveMinute(minuteAngle.get()));
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
            boolean isDateVisible = getSkinnable().isDateVisible();
            dateText.setVisible(isDateVisible);
            dateText.setManaged(isDateVisible);
            boolean isSecondsVisible = getSkinnable().isSecondsVisible();
            secondBackgroundCircle.setVisible(isSecondsVisible);
            secondBackgroundCircle.setManaged(isSecondsVisible);
            secondArc.setVisible(isSecondsVisible);
            secondArc.setManaged(isSecondsVisible);
        } else if ("FINISHED".equals(EVENT_TYPE)) {

        }
    }


    // ******************** Graphics ******************************************
    public void updateTime(final ZonedDateTime TIME) {
        if (dateText.isVisible()) {
            dateText.setText(DATE_TEXT_FORMATTER.format(TIME));
            Helper.adjustTextSize(dateText, 0.6 * size, size * 0.08);
            dateText.relocate((size - dateText.getLayoutBounds().getWidth()) * 0.5, size * 0.22180451);
        }

        hour.setText(HOUR_FORMATTER.format(TIME));
        Helper.adjustTextSize(hour, 0.6 * size, 0.6 * size);
        hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, (size - hour.getLayoutBounds().getHeight()) * 0.65);

        minute.setText(MINUTE_FORMATTER.format(TIME));
        Helper.adjustTextSize(minute, 0.1 * size, 0.075 * size);
        if (minuteAngle.get() == -1) moveMinute(6 * TIME.getMinute());
        KeyValue kv1 = new KeyValue(minuteAngle, minuteAngle.get());
        KeyValue kv2 = new KeyValue(minuteAngle, 6 * TIME.getMinute());
        KeyFrame kf1 = new KeyFrame(Duration.ZERO, kv1);
        KeyFrame kf2 = new KeyFrame(Duration.millis(100), kv2);
        timeline.getKeyFrames().setAll(kf1, kf2);
        timeline.play();

        if (secondBackgroundCircle.isVisible()) {
            secondArc.setLength((-6 * TIME.getSecond()));
            if (getSkinnable().isDiscreteSeconds()) {
                secondArc.setLength((-6 * TIME.getSecond()));
            } else {
                secondArc.setLength((-6 * TIME.getSecond() - TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006));
            }
        }
    }

    private void moveMinute(final double ANGLE) {
        double center   = size * 0.5;
        double sinValue = Math.sin(Math.toRadians(-ANGLE + 180));
        double cosValue = Math.cos(Math.toRadians(-ANGLE + 180));

        minuteCircle.setCenterX(center + size * 0.45 * sinValue);
        minuteCircle.setCenterY(center + size * 0.45 * cosValue);
        minute.relocate(minuteCircle.getCenterX() - (minute.getLayoutBounds().getWidth() * 0.5),
                        minuteCircle.getCenterY() - (minute.getLayoutBounds().getHeight() * 0.5));
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

            secondBackgroundCircle.setCenterX(center);
            secondBackgroundCircle.setCenterY(center);
            secondBackgroundCircle.setRadius(size * 0.45);
            secondBackgroundCircle.setStrokeWidth(size * 0.02819549);

            secondArc.setCenterX(center);
            secondArc.setCenterY(center);
            secondArc.setRadiusX(size * 0.45);
            secondArc.setRadiusY(size * 0.45);
            secondArc.setStrokeWidth(size * 0.02819549);

            dateText.setFont(Fonts.robotoLight(size * 0.08082707));

            hour.setFont(Fonts.robotoThin(size * 0.6));
            minute.setFont(Fonts.robotoRegular(size * 0.075));

            minuteCircle.setRadius(size * 0.075);
            moveMinute(6 * getSkinnable().getTime().getMinute());
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), new Insets(size * 0.035))));

        secondBackgroundCircle.setStroke(Helper.getTranslucentColorFrom(getSkinnable().getSecondColor(), 0.6));
        secondArc.setStroke(getSkinnable().getSecondColor());

        dateText.setFill(getSkinnable().getTextColor());
        hour.setFill(getSkinnable().getTextColor());
        minuteCircle.setFill(getSkinnable().getMinuteColor());
        minute.setFill(getSkinnable().getTextColor());

        ZonedDateTime time = getSkinnable().getTime();
        updateTime(time);
    }
}
