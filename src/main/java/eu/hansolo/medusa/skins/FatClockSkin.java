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
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TimeSection;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;


/**
 * Created by hansolo on 02.02.16.
 */
public class FatClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double            PREFERRED_WIDTH  = 250;
    private static final double            PREFERRED_HEIGHT = 250;
    private static final double            MINIMUM_WIDTH    = 50;
    private static final double            MINIMUM_HEIGHT   = 50;
    private static final double            MAXIMUM_WIDTH    = 1024;
    private static final double            MAXIMUM_HEIGHT   = 1024;
    private static final DateTimeFormatter DATE_FORMATER    = DateTimeFormatter.ofPattern("EE d");
    private static final DateTimeFormatter TIME_FORMATTER   = DateTimeFormatter.ofPattern("HH:mm");
    private double            size;
    private Canvas            ticksAndSectionsCanvas;
    private GraphicsContext   ticksAndSections;
    private Path              hour;
    private Path              minute;
    private Text              title;
    private Text              dateText;
    private Text              text;
    private Pane              pane;
    private Rotate            hourRotate;
    private Rotate            minuteRotate;
    private Group             shadowGroup;
    private DropShadow        dropShadow;
    private List<TimeSection> sections;
    private List<TimeSection> areas;


    // ******************** Constructors **************************************
    public FatClockSkin(Clock clock) {
        super(clock);

        minuteRotate = new Rotate();
        hourRotate   = new Rotate();

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

        hour  = new Path();
        hour.setFillRule(FillRule.EVEN_ODD);
        hour.setStroke(null);
        hour.setFill(getSkinnable().getHourNeedleColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Path();
        minute.setFillRule(FillRule.EVEN_ODD);
        minute.setStroke(null);
        minute.setFill(getSkinnable().getMinuteNeedleColor());
        minute.getTransforms().setAll(minuteRotate);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(hour, minute);
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(getSkinnable().isTitleVisible());
        title.setManaged(getSkinnable().isTitleVisible());

        dateText = new Text("");
        dateText.setVisible(getSkinnable().isDateVisible());
        dateText.setManaged(getSkinnable().isDateVisible());

        text = new Text("");
        text.setVisible(getSkinnable().isTextVisible());
        text.setManaged(getSkinnable().isTextVisible());

        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.getChildren().addAll(ticksAndSectionsCanvas, title, dateText, text, shadowGroup);

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
            dateText.setVisible(getSkinnable().isDateVisible());
            dateText.setManaged(getSkinnable().isDateVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
            areas    = getSkinnable().getAreas();
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
        boolean tickLabelsVisible      = getSkinnable().isTickLabelsVisible();
        Font    font                   = Fonts.robotoCondensedRegular(size * 0.075);
        ticksAndSections.setLineCap(StrokeLineCap.BUTT);
        ticksAndSections.setFont(font);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.44533333 * sinValue, center.getY() + size * 0.44533333 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.47266667 * sinValue, center.getY() + size * 0.47266667 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.5 * sinValue, center.getY() + size * 0.5 * cosValue);
            Point2D textPoint        = new Point2D(center.getX() + size * 0.39 * sinValue, center.getY() + size * 0.39 * cosValue);

            if (counter % 5 == 0) {
                // Draw hour tickmark
                ticksAndSections.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    ticksAndSections.setLineWidth(size * 0.016);
                    ticksAndSections.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    ticksAndSections.setLineWidth(size * 0.005);
                    ticksAndSections.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }

                if (tickLabelsVisible) {
                    ticksAndSections.save();
                    ticksAndSections.translate(textPoint.getX(), textPoint.getY());

                    Helper.rotateContextForText(ticksAndSections, startAngle, angle, TickLabelOrientation.HORIZONTAL);
                    ticksAndSections.setTextAlign(TextAlignment.CENTER);
                    ticksAndSections.setTextBaseline(VPos.CENTER);
                    ticksAndSections.setFill(hourTickMarkColor);
                    if (counter == 0) {
                        ticksAndSections.fillText("12", 0, 0);
                    } else {
                        ticksAndSections.fillText(Integer.toString((int) (counter / 5)), 0, 0);
                    }

                    ticksAndSections.restore();
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                ticksAndSections.setLineWidth(size * 0.01066667);
                ticksAndSections.setStroke(minuteTickMarkColor);
                ticksAndSections.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createHourPointer() {
        double width  = size * 0.09733333;
        double height = size * 0.42066667;
        hour.getElements().clear();
        hour.getElements().add(new MoveTo(0.0, 0.0));
        hour.getElements().add(new CubicCurveTo(0.0, 0.0, 0.0, 0.884310618066561 * height, 0.0, 0.884310618066561 * height));
        hour.getElements().add(new CubicCurveTo(0.0, 0.9477020602218701 * height, 0.22602739726027396 * width, height, 0.5 * width, height));
        hour.getElements().add(new CubicCurveTo(0.773972602739726 * width, height, width, 0.9477020602218701 * height, width, 0.884310618066561 * height));
        hour.getElements().add(new CubicCurveTo(width, 0.884310618066561 * height, width, 0.0, width, 0.0));
        hour.getElements().add(new LineTo(0.0, 0.0));
        hour.getElements().add(new ClosePath());
    }

    private void createMinutePointer() {
        double width  = size * 0.09733333;
        double height = size * 0.548;
        minute.getElements().clear();
        minute.getElements().add(new MoveTo(0.0, 0.0));
        minute.getElements().add(new CubicCurveTo(0.0, 0.0, 0.0, 0.9111922141119222 * height, 0.0, 0.9111922141119222 * height));
        minute.getElements().add(new CubicCurveTo(0.0, 0.9598540145985401 * height, 0.22602739726027396 * width, height, 0.5 * width, height));
        minute.getElements().add(new CubicCurveTo(0.773972602739726 * width, height, width, 0.9598540145985401 * height, width, 0.9111922141119222 * height));
        minute.getElements().add(new CubicCurveTo(width, 0.9111922141119222 * height, width, 0.0, width, 0.0));
        minute.getElements().add(new LineTo(0.0, 0.0));
        minute.getElements().add(new ClosePath());
    }


    // ******************** Resizing ******************************************
    public void updateTime(final ZonedDateTime TIME) {
        if (getSkinnable().isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
        }

        hourRotate.setAngle(0.5 * (60 * TIME.getHour() + TIME.getMinute()));

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

            createHourPointer();
            hour.setFill(getSkinnable().getHourNeedleColor());
            hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, size * 0.12733333);

            createMinutePointer();
            minute.setFill(getSkinnable().getMinuteNeedleColor());
            minute.relocate((size - minute.getLayoutBounds().getWidth()) * 0.5, 0);

            title.setFill(getSkinnable().getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateText.setFill(getSkinnable().getDateColor());
            dateText.setFont(Fonts.latoLight(size * 0.05));
            dateText.relocate((center - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(getSkinnable().getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            minuteRotate.setPivotX(minute.getLayoutBounds().getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getLayoutBounds().getHeight() * 0.91119221);
            hourRotate.setPivotX(hour.getLayoutBounds().getWidth() * 0.5);
            hourRotate.setPivotY(hour.getLayoutBounds().getHeight() * 0.88431062);
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections and Tick Marks
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, size, size);
        if (getSkinnable().getAreasVisible()) Helper.drawTimeAreas(getSkinnable(), ticksAndSections, areas, size, 0.03, 0.03, 0.94, 0.94);
        if (getSkinnable().getSectionsVisible()) Helper.drawTimeSections(getSkinnable(), ticksAndSections, sections, size, 0.065, 0.065, 0.87, 0.87, 0.07);
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

        dateText.setText(DATE_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);
    }
}
