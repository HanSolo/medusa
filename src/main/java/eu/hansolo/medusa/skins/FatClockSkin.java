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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by hansolo on 02.02.16.
 */
public class FatClockSkin extends ClockSkinBase {
    private static final DateTimeFormatter  DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEEE\ndd.MM.YYYY\nHH:mm:ss");
    private static final DateTimeFormatter  DATE_FORMATER       = DateTimeFormatter.ofPattern("EE d");
    private static final DateTimeFormatter  TIME_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm");
    private              Map<Alarm, Circle> alarmMap            = new ConcurrentHashMap<>();
    private              double             size;
    private              Canvas             sectionsAndAreasCanvas;
    private              GraphicsContext    sectionsAndAreasCtx;
    private              Canvas             tickCanvas;
    private              GraphicsContext    tickCtx;
    private              Path               hour;
    private              Path               minute;
    private              Text               title;
    private              Text               dateText;
    private              Text               text;
    private              Pane               pane;
    private              Pane               alarmPane;
    private              Rotate             hourRotate;
    private              Rotate             minuteRotate;
    private              Group              shadowGroup;
    private              DropShadow         dropShadow;
    private              List<TimeSection>  sections;
    private              List<TimeSection>  areas;
    private              boolean            sectionsVisible;
    private              boolean            highlightSections;
    private              boolean            areasVisible;
    private              boolean            highlightAreas;


    // ******************** Constructors **************************************
    public FatClockSkin(Clock clock) {
        super(clock);

        minuteRotate      = new Rotate();
        hourRotate        = new Rotate();

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

        hour  = new Path();
        hour.setFillRule(FillRule.EVEN_ODD);
        hour.setStroke(null);
        hour.setFill(clock.getHourColor());
        hour.getTransforms().setAll(hourRotate);

        minute = new Path();
        minute.setFillRule(FillRule.EVEN_ODD);
        minute.setStroke(null);
        minute.setFill(clock.getMinuteColor());
        minute.getTransforms().setAll(minuteRotate);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(hour, minute);
        shadowGroup.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(clock.isTitleVisible());
        title.setManaged(clock.isTitleVisible());

        dateText = new Text("");
        dateText.setVisible(clock.isDateVisible());
        dateText.setManaged(clock.isDateVisible());

        text = new Text("");
        text.setVisible(clock.isTextVisible());
        text.setManaged(clock.isTextVisible());

        pane = new Pane(sectionsAndAreasCanvas, tickCanvas, alarmPane, title, dateText, text, shadowGroup);
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
            dateText.setVisible(clock.isDateVisible());
            dateText.setManaged(clock.isDateVisible());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = clock.getSections();
            highlightSections = clock.isHighlightSections();
            sectionsVisible   = clock.getSectionsVisible();
            areas             = clock.getAreas();
            highlightAreas    = clock.isHighlightAreas();
            areasVisible      = clock.getAreasVisible();
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
        Color   hourTickMarkColor      = clock.getHourTickMarkColor();
        Color   minuteTickMarkColor    = clock.getMinuteTickMarkColor();
        Color   tickLabelColor         = clock.getTickLabelColor();
        boolean hourTickMarksVisible   = clock.isHourTickMarksVisible();
        boolean minuteTickMarksVisible = clock.isMinuteTickMarksVisible();
        boolean tickLabelsVisible      = clock.isTickLabelsVisible();
        Font    font                   = Fonts.robotoCondensedRegular(size * 0.075);
        tickCtx.clearRect(0, 0, size, size);
        tickCtx.setLineCap(StrokeLineCap.BUTT);
        tickCtx.setFont(font);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 59) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.44533333 * sinValue, center.getY() + size * 0.44533333 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.47266667 * sinValue, center.getY() + size * 0.47266667 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.5 * sinValue, center.getY() + size * 0.5 * cosValue);
            Point2D textPoint        = new Point2D(center.getX() + size * 0.39 * sinValue, center.getY() + size * 0.39 * cosValue);

            if (counter % 5 == 0) {
                // Draw hour tickmark
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.016);
                    tickCtx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.005);
                    tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }

                if (tickLabelsVisible) {
                    tickCtx.save();
                    tickCtx.translate(textPoint.getX(), textPoint.getY());

                    Helper.rotateContextForText(tickCtx, startAngle, angle, TickLabelOrientation.HORIZONTAL);
                    tickCtx.setTextAlign(TextAlignment.CENTER);
                    tickCtx.setTextBaseline(VPos.CENTER);
                    tickCtx.setFill(tickLabelColor);
                    if (counter == 0) {
                        tickCtx.fillText("12", 0, 0);
                    } else {
                        tickCtx.fillText(Integer.toString((int) (counter / 5)), 0, 0);
                    }

                    tickCtx.restore();
                }
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                // Draw minute tickmark
                tickCtx.setLineWidth(size * 0.01066667);
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createHourPointer() {
        double width  = size * 0.09733333;
        double height = size * 0.42066667;
        hour.setCache(false);
        hour.getElements().clear();
        hour.getElements().add(new MoveTo(0.0, 0.0));
        hour.getElements().add(new CubicCurveTo(0.0, 0.0, 0.0, 0.884310618066561 * height, 0.0, 0.884310618066561 * height));
        hour.getElements().add(new CubicCurveTo(0.0, 0.9477020602218701 * height, 0.22602739726027396 * width, height, 0.5 * width, height));
        hour.getElements().add(new CubicCurveTo(0.773972602739726 * width, height, width, 0.9477020602218701 * height, width, 0.884310618066561 * height));
        hour.getElements().add(new CubicCurveTo(width, 0.884310618066561 * height, width, 0.0, width, 0.0));
        hour.getElements().add(new LineTo(0.0, 0.0));
        hour.getElements().add(new ClosePath());
        hour.setCache(true);
        hour.setCacheHint(CacheHint.ROTATE);
    }

    private void createMinutePointer() {
        double width  = size * 0.09733333;
        double height = size * 0.548;
        minute.setCache(false);
        minute.getElements().clear();
        minute.getElements().add(new MoveTo(0.0, 0.0));
        minute.getElements().add(new CubicCurveTo(0.0, 0.0, 0.0, 0.9111922141119222 * height, 0.0, 0.9111922141119222 * height));
        minute.getElements().add(new CubicCurveTo(0.0, 0.9598540145985401 * height, 0.22602739726027396 * width, height, 0.5 * width, height));
        minute.getElements().add(new CubicCurveTo(0.773972602739726 * width, height, width, 0.9598540145985401 * height, width, 0.9111922141119222 * height));
        minute.getElements().add(new CubicCurveTo(width, 0.9111922141119222 * height, width, 0.0, width, 0.0));
        minute.getElements().add(new LineTo(0.0, 0.0));
        minute.getElements().add(new ClosePath());
        minute.setCache(true);
        minute.setCacheHint(CacheHint.ROTATE);
    }
    
    @Override public void updateTime(final ZonedDateTime TIME) {
        if (clock.isDiscreteMinutes()) {
            minuteRotate.setAngle(TIME.getMinute() * 6);
        } else {
            minuteRotate.setAngle(TIME.getMinute() * 6 + TIME.getSecond() * 0.1);
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

        if (dateText.isVisible()) {
            dateText.setText(DATE_FORMATER.format(TIME).toUpperCase());
            Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
            dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);
        }

        // Show all alarms within the next hour
        if (TIME.getMinute() == 0 && TIME.getSecond() == 0) Helper.drawAlarms(clock, size, 0.015, 0.485, alarmMap, DATE_TIME_FORMATTER, TIME);

        // Highlight Areas and Sections
        if (highlightAreas | highlightSections) {
            sectionsAndAreasCtx.clearRect(0, 0, size, size);
            if (areasVisible) Helper.drawTimeAreas(clock, sectionsAndAreasCtx, areas, size, 0.0, 0.0, 1.0, 1.0);
            if (sectionsVisible) Helper.drawTimeSections(clock, sectionsAndAreasCtx, sections, size, 0.0275, 0.0275, 0.945, 0.945, 0.055);
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

            createHourPointer();
            hour.setFill(clock.getHourColor());
            hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, size * 0.12733333);

            createMinutePointer();
            minute.setFill(clock.getMinuteColor());
            minute.relocate((size - minute.getLayoutBounds().getWidth()) * 0.5, 0);

            title.setFill(clock.getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateText.setFill(clock.getDateColor());
            dateText.setFont(Fonts.latoLight(size * 0.05));
            dateText.relocate((center - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(clock.getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            minuteRotate.setPivotX(minute.getLayoutBounds().getWidth() * 0.5);
            minuteRotate.setPivotY(minute.getLayoutBounds().getHeight() * 0.91119221);
            hourRotate.setPivotX(hour.getLayoutBounds().getWidth() * 0.5);
            hourRotate.setPivotY(hour.getLayoutBounds().getHeight() * 0.88431062);
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        hour.setFill(clock.getHourColor());
        minute.setFill(clock.getMinuteColor());
        title.setFill(clock.getTextColor());
        dateText.setFill(clock.getDateColor());
        text.setFill(clock.getTextColor());

        shadowGroup.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections
        sectionsAndAreasCtx.clearRect(0, 0, size, size);
        if (areasVisible) Helper.drawTimeAreas(clock, sectionsAndAreasCtx, areas, size, 0.0, 0.0, 1.0, 1.0);
        if (sectionsVisible) Helper.drawTimeSections(clock, sectionsAndAreasCtx, sections, size, 0.0275, 0.0275, 0.945, 0.945, 0.055);

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

        dateText.setText(DATE_FORMATER.format(time).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.45), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

        alarmPane.getChildren().setAll(alarmMap.values());
        Helper.drawAlarms(clock, size, 0.015, 0.485, alarmMap, DATE_TIME_FORMATTER, time);


    }
}
