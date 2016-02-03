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
import eu.hansolo.medusa.TimeSection;
import eu.hansolo.medusa.tools.Helper;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;


/**
 * Created by hansolo on 29.01.16.
 */
public class DBClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH  = 250;
    private static final double            PREFERRED_HEIGHT = 250;
    private static final double            MINIMUM_WIDTH    = 50;
    private static final double            MINIMUM_HEIGHT   = 50;
    private static final double            MAXIMUM_WIDTH    = 1024;
    private static final double            MAXIMUM_HEIGHT   = 1024;
    private static final DateTimeFormatter TIME_FORMATTER   = DateTimeFormatter.ofPattern("HH:mm");
    private              double            size;
    private              Canvas            ticksAndSectionsCanvas;
    private              GraphicsContext   ticksAndSections;
    private              Rectangle         hour;
    private              Rectangle         minute;
    private              Path              second;
    private              Circle            knob;
    private              Text              title;
    private              Text              text;
    private              Pane              pane;
    private              Rotate            hourRotate;
    private              Rotate            minuteRotate;
    private              Rotate            secondRotate;
    private              Group             shadowGroup;
    private              DropShadow        dropShadow;
    private              List<TimeSection> sections;
    private              List<TimeSection> areas;


    // ******************** Constructors **************************************
    public DBClockSkin(Clock clock) {
        super(clock);

        minuteRotate = new Rotate();
        hourRotate   = new Rotate();
        secondRotate = new Rotate();

        sections     = clock.getSections();
        areas        = clock.getAreas();

        init();
        initGraphics();
        registerListeners();
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
        ticksAndSectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        hour  = new Rectangle(3, 60);
        hour.setArcHeight(3);
        hour.setArcWidth(3);
        hour.setStroke(Color.web("#282a3280"));
        hour.getTransforms().setAll(hourRotate);

        minute = new Rectangle(3, 96);
        minute.setArcHeight(3);
        minute.setArcWidth(3);
        minute.setStroke(Color.web("#282a3280"));
        minute.getTransforms().setAll(minuteRotate);

        second = new Path();
        second.setFillRule(FillRule.EVEN_ODD);
        second.setStroke(null);
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

        shadowGroup = new Group(hour, minute, second, knob);
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(getSkinnable().isTitleVisible());
        title.setManaged(getSkinnable().isTitleVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.getChildren().addAll(ticksAndSectionsCanvas, title, text, shadowGroup);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        //getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
        getSkinnable().currentTimeProperty().addListener(o ->
            updateTime(ZonedDateTime.ofInstant(Instant.ofEpochSecond(getSkinnable().getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId())))
        );
    }


    // ******************** Methods *******************************************
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
            second.setVisible(getSkinnable().isSecondsVisible());
            second.setManaged(getSkinnable().isSecondsVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
            areas    = getSkinnable().getAreas();
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
        ticksAndSections.setLineCap(StrokeLineCap.BUTT);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerMainPoint   = new Point2D(center.getX() + size * 0.335 * sinValue, center.getY() + size * 0.335 * cosValue);
            Point2D innerPoint       = new Point2D(center.getX() + size * 0.365 * sinValue, center.getY() + size * 0.365 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.425 * sinValue, center.getY() + size * 0.425 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.465 * sinValue, center.getY() + size * 0.465 * cosValue);

            if (counter % 5 == 0 && counter % 3 == 0) {
                // Draw 12, 3, 6, 9 hour tickmark
                ticksAndSections.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    ticksAndSections.setLineWidth(size * 0.0375);
                    ticksAndSections.strokeLine(innerMainPoint.getX(), innerMainPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    ticksAndSections.setLineWidth(size * 0.02);
                    ticksAndSections.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 5 == 0) {
                // Draw hour tickmark
                ticksAndSections.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    ticksAndSections.setLineWidth(size * 0.0375);
                    ticksAndSections.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible){
                    ticksAndSections.setLineWidth(size * 0.02);
                    ticksAndSections.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                ticksAndSections.setStroke(minuteTickMarkColor);
                ticksAndSections.setLineWidth(size * 0.02);
                ticksAndSections.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createSecondPointer() {
        double width  = size * 0.11866667;
        double height = size * 0.46266667;
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
    }


    // ******************** Resizing ******************************************
    public void updateTime(final ZonedDateTime TIME) {
        if (getSkinnable().isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        if (getSkinnable().isDiscreteSeconds() && second.isVisible()) {
            secondRotate.setAngle(TIME.getSecond() * 6);
        } else {
            secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
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
    }

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

            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);

            hour.setFill(getSkinnable().getHourColor());
            hour.setWidth(size * 0.05);
            hour.setHeight(size * 0.3125);
            hour.relocate((size - hour.getWidth()) * 0.5, center - size * 0.3125);

            minute.setFill(getSkinnable().getMinuteColor());
            minute.setWidth(size * 0.038);
            minute.setHeight(size * 0.4375);
            minute.relocate((size - minute.getWidth()) * 0.5, center - size * 0.4375);

            createSecondPointer();
            second.setFill(getSkinnable().getSecondColor());
            second.relocate((size - second.getLayoutBounds().getWidth()) * 0.5, center - second.getLayoutBounds().getHeight());

            knob.setFill(getSkinnable().getKnobColor());
            knob.setRadius(size * 0.05);
            knob.setCenterX(center);
            knob.setCenterY(center);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            text.setFill(getSkinnable().getTextColor());
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

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections and Tick Marks
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, size, size);
        if (getSkinnable().getAreasVisible()) Helper.drawTimeAreas(getSkinnable(), ticksAndSections, areas, size, 0.035, 0.035, 0.93, 0.93);
        if (getSkinnable().getSectionsVisible()) Helper.drawTimeSections(getSkinnable(), ticksAndSections, sections, size, 0.056, 0.056, 0.89, 0.89, 0.0395);
        drawTicks();
        ticksAndSectionsCanvas.setCache(true);
        ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

        ZonedDateTime time = getSkinnable().getTime();

        updateTime(time);

        title.setText(getSkinnable().getTitle());
        Helper.adjustTextSize(title, 0.6 * size, size * 0.12);
        title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

        text.setText(TIME_FORMATTER.format(time));
        Helper.adjustTextSize(text, 0.6 * size, size * 0.12);
        text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);
    }
}
