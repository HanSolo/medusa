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
 * Created by hansolo on 18.11.16.
 */
public class IndustrialClockSkin extends ClockSkinBase {
    private static final DateTimeFormatter  DATE_TIME_FORMATTER   = DateTimeFormatter.ofPattern("EEEE\ndd.MM.YYYY\nHH:mm:ss");
    private static final DateTimeFormatter  DATE_TEXT_FORMATTER   = DateTimeFormatter.ofPattern("EE");
    private static final DateTimeFormatter  DATE_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("d");
    private static final DateTimeFormatter  TIME_FORMATTER        = DateTimeFormatter.ofPattern("HH:mm");
    private              Map<Alarm, Circle> alarmMap              = new ConcurrentHashMap<>();
    private              double             size;
    private              Canvas             sectionsAndAreasCanvas;
    private              GraphicsContext    sectionsAndAreasCtx;
    private              Canvas             tickCanvas;
    private              GraphicsContext    tickCtx;
    private              Path               hour;
    private              Path               minute;
    private              Path               second;
    private              Text               title;
    private              Text               dateText;
    private              Text               dateNumber;
    private              Text               text;
    private              Circle             centerDot;
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
    public IndustrialClockSkin(Clock clock) {
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
        second.setVisible(clock.isSecondsVisible());
        second.setManaged(clock.isSecondsVisible());

        centerDot = new Circle();
        centerDot.setFill(Color.WHITE);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroupHour   = new Group(hour);
        shadowGroupMinute = new Group(minute);
        shadowGroupSecond = new Group(second);

        shadowGroupHour.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        title = new Text("");
        title.setVisible(clock.isTitleVisible());
        title.setManaged(clock.isTitleVisible());

        dateText = new Text("");
        dateText.setVisible(clock.isDateVisible());
        dateText.setManaged(clock.isDateVisible());

        dateNumber = new Text("");
        dateNumber.setVisible(clock.isDateVisible());
        dateNumber.setManaged(clock.isDateVisible());

        text = new Text("");
        text.setVisible(clock.isTextVisible());
        text.setManaged(clock.isTextVisible());

        pane = new Pane(sectionsAndAreasCanvas, tickCanvas, alarmPane, title, dateText, dateNumber, text, shadowGroupMinute, shadowGroupHour, shadowGroupSecond, centerDot);
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
            dateNumber.setVisible(clock.isDateVisible());
            dateNumber.setManaged(clock.isDateVisible());
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
        double  angleStep              = 360 / 240 +0.5;
        Point2D center                 = new Point2D(size * 0.5, size * 0.5);
        Color   hourTickMarkColor      = clock.getHourTickMarkColor();
        Color   minuteTickMarkColor    = clock.getMinuteTickMarkColor();
        boolean hourTickMarksVisible   = clock.isHourTickMarksVisible();
        boolean minuteTickMarksVisible = clock.isMinuteTickMarksVisible();
        tickCtx.clearRect(0, 0, size, size);
        tickCtx.setLineCap(StrokeLineCap.BUTT);
        tickCtx.setLineWidth(size * 0.02493075);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 239) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint       = new Point2D(center.getX() + size * 0.35277778 * sinValue, center.getY() + size * 0.35277778 * cosValue);
            Point2D innerMinutePoint = new Point2D(center.getX() + size * 0.40972222 * sinValue, center.getY() + size * 0.40972222 * cosValue);
            Point2D outerPoint       = new Point2D(center.getX() + size * 0.46388889 * sinValue, center.getY() + size * 0.46388889 * cosValue);

            if (counter % 20 == 0) {
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.setLineWidth(size * 0.02493075);
                    tickCtx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
                    tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
                }
            } else if (counter % 4 == 0 && minuteTickMarksVisible) {
                tickCtx.setLineWidth(size * 0.00833333);
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    private void createHourPointer() {
        double width  = size;
        double height = size;
        hour.setCache(false);
        hour.getElements().clear();
        hour.getElements().add(new MoveTo(0.4930555555555556 * width, 0.28541666666666665 * height));
        hour.getElements().add(new CubicCurveTo(0.4930555555555556 * width, 0.28125 * height,
                                                0.49583333333333335 * width, 0.27847222222222223 * height,
                                                0.5 * width, 0.27847222222222223 * height));
        hour.getElements().add(new CubicCurveTo(0.5041666666666667 * width, 0.27847222222222223 * height,
                                                0.5069444444444444 * width, 0.28125 * height,
                                                0.5069444444444444 * width, 0.28541666666666665 * height));
        hour.getElements().add(new CubicCurveTo(0.5069444444444444 * width, 0.28541666666666665 * height,
                                                0.5069444444444444 * width, 0.39375 * height,
                                                0.5069444444444444 * width, 0.39375 * height));
        hour.getElements().add(new LineTo(0.4930555555555556 * width, 0.39375 * height));
        hour.getElements().add(new CubicCurveTo(0.4930555555555556 * width, 0.39375 * height,
                                                0.4930555555555556 * width, 0.28541666666666665 * height,
                                                0.4930555555555556 * width, 0.28541666666666665 * height));
        hour.getElements().add(new ClosePath());
        hour.getElements().add(new MoveTo(0.4847222222222222 * width, 0.28541666666666665 * height));
        hour.getElements().add(new CubicCurveTo(0.4847222222222222 * width, 0.28541666666666665 * height,
                                                0.4847222222222222 * width, 0.49722222222222223 * height,
                                                0.4847222222222222 * width, 0.49722222222222223 * height));
        hour.getElements().add(new CubicCurveTo(0.4847222222222222 * width, 0.5055555555555555 * height,
                                                0.49166666666666664 * width, 0.5125 * height,
                                                0.5 * width, 0.5125 * height));
        hour.getElements().add(new CubicCurveTo(0.5083333333333333 * width, 0.5125 * height,
                                                0.5152777777777777 * width, 0.5055555555555555 * height,
                                                0.5152777777777777 * width, 0.49722222222222223 * height));
        hour.getElements().add(new CubicCurveTo(0.5152777777777777 * width, 0.49722222222222223 * height,
                                                0.5152777777777777 * width, 0.28541666666666665 * height,
                                                0.5152777777777777 * width, 0.28541666666666665 * height));
        hour.getElements().add(new CubicCurveTo(0.5152777777777777 * width, 0.27708333333333335 * height,
                                                0.5083333333333333 * width, 0.2701388888888889 * height,
                                                0.5 * width, 0.2701388888888889 * height));
        hour.getElements().add(new CubicCurveTo(0.49166666666666664 * width, 0.2701388888888889 * height,
                                                0.4847222222222222 * width, 0.27708333333333335 * height,
                                                0.4847222222222222 * width, 0.28541666666666665 * height));
        hour.getElements().add(new ClosePath());
        hour.setCache(true);
        hour.setCacheHint(CacheHint.ROTATE);
    }

    private void createMinutePointer() {
        double width  = size;
        double height = size;
        minute.setCache(false);
        minute.getElements().clear();
        minute.getElements().add(new MoveTo(0.4930555555555556 * width, 0.17083333333333334 * height));
        minute.getElements().add(new CubicCurveTo(0.4930555555555556 * width, 0.16666666666666666 * height,
                                                  0.49583333333333335 * width, 0.1638888888888889 * height,
                                                  0.5 * width, 0.1638888888888889 * height));
        minute.getElements().add(new CubicCurveTo(0.5041666666666667 * width, 0.1638888888888889 * height,
                                                  0.5069444444444444 * width, 0.16666666666666666 * height,
                                                  0.5069444444444444 * width, 0.17083333333333334 * height));
        minute.getElements().add(new CubicCurveTo(0.5069444444444444 * width, 0.17083333333333334 * height,
                                                  0.5069444444444444 * width, 0.33194444444444443 * height,
                                                  0.5069444444444444 * width, 0.33194444444444443 * height));
        minute.getElements().add(new LineTo(0.4930555555555556 * width, 0.33194444444444443 * height));
        minute.getElements().add(new CubicCurveTo(0.4930555555555556 * width, 0.33194444444444443 * height,
                                                  0.4930555555555556 * width, 0.17083333333333334 * height,
                                                  0.4930555555555556 * width, 0.17083333333333334 * height));
        minute.getElements().add(new ClosePath());
        minute.getElements().add(new MoveTo(0.4722222222222222 * width, 0.5 * height));
        minute.getElements().add(new CubicCurveTo(0.4722222222222222 * width, 0.5152777777777777 * height,
                                                  0.4847222222222222 * width, 0.5277777777777778 * height,
                                                  0.5 * width, 0.5277777777777778 * height));
        minute.getElements().add(new CubicCurveTo(0.5152777777777777 * width, 0.5277777777777778 * height,
                                                  0.5277777777777778 * width, 0.5152777777777777 * height,
                                                  0.5277777777777778 * width, 0.5 * height));
        minute.getElements().add(new CubicCurveTo(0.5277777777777778 * width, 0.49027777777777776 * height,
                                                  0.5229166666666667 * width, 0.48194444444444445 * height,
                                                  0.5152777777777777 * width, 0.47708333333333336 * height));
        minute.getElements().add(new CubicCurveTo(0.5152777777777777 * width, 0.47708333333333336 * height,
                                                  0.5152777777777777 * width, 0.17083333333333334 * height,
                                                  0.5152777777777777 * width, 0.17083333333333334 * height));
        minute.getElements().add(new CubicCurveTo(0.5152777777777777 * width, 0.1625 * height,
                                                  0.5083333333333333 * width, 0.15555555555555556 * height,
                                                  0.5 * width, 0.15555555555555556 * height));
        minute.getElements().add(new CubicCurveTo(0.49166666666666664 * width, 0.15555555555555556 * height,
                                                  0.4847222222222222 * width, 0.1625 * height,
                                                  0.4847222222222222 * width, 0.17083333333333334 * height));
        minute.getElements().add(new CubicCurveTo(0.4847222222222222 * width, 0.17083333333333334 * height,
                                                  0.4847222222222222 * width, 0.47708333333333336 * height,
                                                  0.4847222222222222 * width, 0.47708333333333336 * height));
        minute.getElements().add(new CubicCurveTo(0.47708333333333336 * width, 0.48194444444444445 * height,
                                                  0.4722222222222222 * width, 0.49027777777777776 * height,
                                                  0.4722222222222222 * width, 0.5 * height));
        minute.getElements().add(new ClosePath());
        minute.setCache(true);
        minute.setCacheHint(CacheHint.ROTATE);
    }

    private void createSecondPointer() {
        double width  = size;
        double height = size;
        second.setCache(false);
        second.getElements().clear();
        second.getElements().add(new MoveTo(0.4951388888888889 * width, 0.5 * height));
        second.getElements().add(new CubicCurveTo(0.4951388888888889 * width, 0.49722222222222223 * height,
                                                  0.49722222222222223 * width, 0.4951388888888889 * height,
                                                  0.5 * width, 0.4951388888888889 * height));
        second.getElements().add(new CubicCurveTo(0.5027777777777778 * width, 0.4951388888888889 * height,
                                                  0.5048611111111111 * width, 0.49722222222222223 * height,
                                                  0.5048611111111111 * width, 0.5 * height));
        second.getElements().add(new CubicCurveTo(0.5048611111111111 * width, 0.5027777777777778 * height,
                                                  0.5027777777777778 * width, 0.5048611111111111 * height,
                                                  0.5 * width, 0.5048611111111111 * height));
        second.getElements().add(new CubicCurveTo(0.49722222222222223 * width, 0.5048611111111111 * height,
                                                  0.4951388888888889 * width, 0.5027777777777778 * height,
                                                  0.4951388888888889 * width, 0.5 * height));
        second.getElements().add(new ClosePath());
        second.getElements().add(new MoveTo(0.4875 * width, 0.5 * height));
        second.getElements().add(new CubicCurveTo(0.4875 * width, 0.5048611111111111 * height,
                                                  0.49027777777777776 * width, 0.5090277777777777 * height,
                                                  0.49444444444444446 * width, 0.5111111111111111 * height));
        second.getElements().add(new CubicCurveTo(0.49444444444444446 * width, 0.5111111111111111 * height,
                                                  0.49444444444444446 * width, 0.5638888888888889 * height,
                                                  0.49444444444444446 * width, 0.5638888888888889 * height));
        second.getElements().add(new LineTo(0.5055555555555555 * width, 0.5638888888888889 * height));
        second.getElements().add(new CubicCurveTo(0.5055555555555555 * width, 0.5638888888888889 * height,
                                                  0.5055555555555555 * width, 0.5111111111111111 * height,
                                                  0.5055555555555555 * width, 0.5111111111111111 * height));
        second.getElements().add(new CubicCurveTo(0.5097222222222222 * width, 0.5090277777777777 * height,
                                                  0.5125 * width, 0.5048611111111111 * height,
                                                  0.5125 * width, 0.5 * height));
        second.getElements().add(new CubicCurveTo(0.5125 * width, 0.4951388888888889 * height,
                                                  0.5097222222222222 * width, 0.4909722222222222 * height,
                                                  0.5055555555555555 * width, 0.4888888888888889 * height));
        second.getElements().add(new CubicCurveTo(0.5055555555555555 * width, 0.4888888888888889 * height,
                                                  0.5055555555555555 * width, 0.19027777777777777 * height,
                                                  0.5055555555555555 * width, 0.19027777777777777 * height));
        second.getElements().add(new LineTo(0.49444444444444446 * width, 0.19027777777777777 * height));
        second.getElements().add(new CubicCurveTo(0.49444444444444446 * width, 0.19027777777777777 * height,
                                                  0.49444444444444446 * width, 0.4888888888888889 * height,
                                                  0.49444444444444446 * width, 0.4888888888888889 * height));
        second.getElements().add(new CubicCurveTo(0.49027777777777776 * width, 0.4909722222222222 * height,
                                                  0.4875 * width, 0.4951388888888889 * height,
                                                  0.4875 * width, 0.5 * height));
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

        if (second.isVisible()) {
            if (clock.isDiscreteSeconds()) {
                secondRotate.setAngle(TIME.getSecond() * 6);
            } else {
                secondRotate.setAngle(TIME.getSecond() * 6 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006);
            }
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
            dateText.setText(DATE_TEXT_FORMATTER.format(TIME).toUpperCase());
            Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
            dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.4), (size - dateText.getLayoutBounds().getHeight()) * 0.5);
        }

        if (dateNumber.isVisible()) {
            dateNumber.setText(DATE_NUMBER_FORMATTER.format(TIME).toUpperCase());
            Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
            dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.51), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);
        }

        // Show all alarms within the next hour
        if (TIME.getMinute() == 0 && TIME.getSecond() == 0) Helper.drawAlarms(clock, size, 0.0225, 0.4775, alarmMap, DATE_TIME_FORMATTER, TIME);;

        // Highlight Areas and Sections
        if (highlightAreas | highlightSections) {
            sectionsAndAreasCtx.clearRect(0, 0, size, size);
            if (areasVisible) Helper.drawTimeAreas(clock, sectionsAndAreasCtx, areas, size, 0, 0, 1, 1);
            if (sectionsVisible) Helper.drawTimeSections(clock, sectionsAndAreasCtx, sections, size, 0.02, 0.02, 0.96, 0.96, 0.04);
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

            dropShadow.setRadius(0.012 * size);
            dropShadow.setOffsetY(0.012 * size);

            sectionsAndAreasCanvas.setWidth(size);
            sectionsAndAreasCanvas.setHeight(size);

            tickCanvas.setWidth(size);
            tickCanvas.setHeight(size);

            alarmPane.setMaxSize(size, size);

            createHourPointer();
            hour.setFill(clock.getHourColor());
            hour.relocate((size - hour.getLayoutBounds().getWidth()) * 0.5, size * 0.27361111);

            createMinutePointer();
            minute.setFill(clock.getMinuteColor());
            minute.relocate((size - minute.getLayoutBounds().getWidth()) * 0.5, size * 0.15555556);

            createSecondPointer();
            second.setFill(clock.getSecondColor());
            second.relocate((size - second.getLayoutBounds().getWidth()) * 0.5, size * 0.19027778);

            centerDot.setCenterX(size * 0.5);
            centerDot.setCenterY(size * 0.5);
            centerDot.setRadius(size * 0.00486111);

            title.setFill(clock.getTextColor());
            title.setFont(Fonts.latoLight(size * 0.12));
            title.relocate((size - title.getLayoutBounds().getWidth()) * 0.5, size * 0.25);

            dateText.setFill(clock.getDateColor());
            dateText.setFont(Fonts.latoLight(size * 0.06666667));
            dateText.relocate((center - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.4), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

            dateNumber.setFill(clock.getSecondColor());
            dateNumber.setFont(Fonts.latoLight(size * 0.06666667));
            dateNumber.relocate((center - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.51), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);

            text.setFill(clock.getTextColor());
            text.setFont(Fonts.latoLight(size * 0.12));
            text.relocate((size - text.getLayoutBounds().getWidth()) * 0.5, size * 0.6);

            hourRotate.setPivotX(size * 0.5);
            hourRotate.setPivotY(size * 0.5);
            minuteRotate.setPivotX(size * 0.5);
            minuteRotate.setPivotY(size * 0.5);
            secondRotate.setPivotX(size * 0.5);
            secondRotate.setPivotY(size * 0.5);
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        hour.setFill(clock.getHourColor());
        minute.setFill(clock.getMinuteColor());
        second.setFill(clock.getSecondColor());
        //centerDot.setFill(clock.getKnobColor());
        title.setFill(clock.getTitleColor());
        dateText.setFill(clock.getDateColor());
        text.setFill(clock.getTextColor());
        
        shadowGroupHour.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupMinute.setEffect(clock.getShadowsEnabled() ? dropShadow : null);
        shadowGroupSecond.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        // Areas, Sections
        sectionsAndAreasCtx.clearRect(0, 0, size, size);
        if (areasVisible) Helper.drawTimeAreas(clock, sectionsAndAreasCtx, areas, size, 0, 0, 1, 1);
        if (sectionsVisible) Helper.drawTimeSections(clock, sectionsAndAreasCtx, sections, size, 0.02, 0.02, 0.96, 0.96, 0.04);

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

        dateText.setText(DATE_TEXT_FORMATTER.format(time).toUpperCase());
        Helper.adjustTextSize(dateText, 0.3 * size, size * 0.05);
        dateText.relocate(((size * 0.5) - dateText.getLayoutBounds().getWidth()) * 0.5 + (size * 0.4), (size - dateText.getLayoutBounds().getHeight()) * 0.5);

        dateNumber.setText(DATE_NUMBER_FORMATTER.format(time).toUpperCase());
        Helper.adjustTextSize(dateNumber, 0.3 * size, size * 0.05);
        dateNumber.relocate(((size * 0.5) - dateNumber.getLayoutBounds().getWidth()) * 0.5 + (size * 0.51), (size - dateNumber.getLayoutBounds().getHeight()) * 0.5);

        alarmPane.getChildren().setAll(alarmMap.values());
        Helper.drawAlarms(clock, size, 0.0225, 0.4775, alarmMap, DATE_TIME_FORMATTER, time);
    }
}
