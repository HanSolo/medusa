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
import javafx.scene.effect.InnerShadow;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;


/**
 * Created by hansolo on 09.11.16.
 */
public class DesignClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double          PREFERRED_WIDTH  = 250;
    private static final double          PREFERRED_HEIGHT = 250;
    private static final double          MINIMUM_WIDTH    = 50;
    private static final double          MINIMUM_HEIGHT   = 50;
    private static final double          MAXIMUM_WIDTH    = 1024;
    private static final double          MAXIMUM_HEIGHT   = 1024;
    private              double          size;
    private              double          rotationRadius;
    private              Canvas          tickCanvas;
    private              GraphicsContext tickCtx;
    private              Circle          clip;
    private              Line            needle;
    private              Pane            pane;
    private              Group           shadowGroup;
    private              DropShadow      dropShadow;
    private              InnerShadow     innerShadow;


    // ******************** Constructors **************************************
    public DesignClockSkin(Clock clock) {
        super(clock);

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

        rotationRadius = PREFERRED_WIDTH * 1.25;

        clip = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH *0.5);

        tickCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        tickCanvas.setClip(clip);
        tickCtx = tickCanvas.getGraphicsContext2D();

        needle = new Line(PREFERRED_WIDTH * 0.5 , 0, PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT);
        needle.setFill(null);
        needle.setStrokeLineCap(StrokeLineCap.BUTT);
        needle.setStroke(getSkinnable().getHourColor());

        dropShadow = new DropShadow();
        dropShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.25), 0.015 * PREFERRED_WIDTH, 0.0, 0.0, 0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle);
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 4, 0.0, 0, 1);

        pane = new Pane(tickCanvas, shadowGroup);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setEffect(innerShadow);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        if (getSkinnable().isAnimated()) {
            getSkinnable().currentTimeProperty().addListener(o -> updateTime(
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(getSkinnable().getCurrentTime()), ZoneId.of(ZoneId.systemDefault().getId()))));
        } else {
            getSkinnable().timeProperty().addListener(o -> updateTime(getSkinnable().getTime()));
        }
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

        } else if ("SECTION".equals(EVENT_TYPE)) {

            redraw();
        }
    }


    // ******************** Canvas ********************************************
    private void drawTicks() {
        double  sinValue;
        double  cosValue;
        double  ticksSize              = size * 2.5;
        double  startAngle             = 180;
        double  angleStep              = 360 / 72;
        Point2D center                 = new Point2D(ticksSize * 0.5, ticksSize * 0.5);
        Color   hourTickMarkColor      = getSkinnable().getHourTickMarkColor();
        Color   minuteTickMarkColor    = getSkinnable().getMinuteTickMarkColor();
        Color   tickLabelColor         = getSkinnable().getTickLabelColor();
        boolean hourTickMarksVisible   = getSkinnable().isHourTickMarksVisible();
        boolean minuteTickMarksVisible = getSkinnable().isMinuteTickMarksVisible();
        boolean tickLabelsVisible      = getSkinnable().isTickLabelsVisible();
        Font    font                   = Fonts.robotoRegular(ticksSize * 0.084);
        tickCtx.clearRect(0, 0, ticksSize, ticksSize);
        tickCtx.setLineCap(StrokeLineCap.ROUND);
        tickCtx.setFont(font);
        tickCtx.setLineWidth(ticksSize * 0.007);
        for (double angle = 0, counter = 0 ; Double.compare(counter, 72) <= 0 ; angle -= angleStep, counter++) {
            sinValue = Math.sin(Math.toRadians(angle + startAngle));
            cosValue = Math.cos(Math.toRadians(angle + startAngle));

            Point2D innerPoint           = new Point2D(center.getX() + ticksSize * 0.45 * sinValue, center.getY() + ticksSize * 0.45 * cosValue);
            Point2D innerFiveMinutePoint = new Point2D(center.getX() + ticksSize * 0.47 * sinValue, center.getY() + ticksSize * 0.47 * cosValue);
            Point2D innerMinutePoint     = new Point2D(center.getX() + ticksSize * 0.49 * sinValue, center.getY() + ticksSize * 0.49 * cosValue);
            Point2D outerPoint           = new Point2D(center.getX() + ticksSize * 0.495 * sinValue, center.getY() + ticksSize * 0.495 * cosValue);
            Point2D textPoint            = new Point2D(center.getX() + ticksSize * 0.385 * sinValue, center.getY() + ticksSize * 0.385 * cosValue);

            if (counter % 6 == 0) {
                tickCtx.setStroke(hourTickMarkColor);
                if (hourTickMarksVisible) {
                    tickCtx.strokeLine(innerPoint.getX(), innerPoint.getY(), outerPoint.getX(), outerPoint.getY());
                } else if (minuteTickMarksVisible) {
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
                        tickCtx.fillText(Integer.toString((int) (counter / 6)), 0, 0);
                    }

                    tickCtx.restore();
                }
            } else if (counter % 3 == 0 && minuteTickMarksVisible) {
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.strokeLine(innerFiveMinutePoint.getX(), innerFiveMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            } else if (counter % 1 == 0 && minuteTickMarksVisible) {
                tickCtx.setStroke(minuteTickMarkColor);
                tickCtx.strokeLine(innerMinutePoint.getX(), innerMinutePoint.getY(), outerPoint.getX(), outerPoint.getY());
            }
        }
    }


    // ******************** Graphics ******************************************
    public void updateTime(final ZonedDateTime TIME) {
        double rotationAngle = 0.5 * (60 * TIME.getHour() + TIME.getMinute() + TIME.getSecond() * 0.0166666667 + TIME.get(ChronoField.MILLI_OF_SECOND) * 0.0000166667);
        double rotationX     = size * 0.5 + rotationRadius * Math.sin(Math.toRadians(rotationAngle + 180));
        double rotationY     = size * 0.5 - rotationRadius * Math.cos(Math.toRadians(rotationAngle + 180));
        tickCanvas.relocate(rotationX - tickCanvas.getHeight() * 0.5, rotationY - tickCanvas.getHeight() * 0.5);
        needle.setRotate(rotationAngle);

        double canvasCenterX = tickCanvas.getWidth() * 0.5;
        double canvasCenterY = tickCanvas.getHeight() * 0.5;
        double radius        = tickCanvas.getHeight() * 0.372;
        double rotX          = canvasCenterX + radius * Math.sin(Math.toRadians(rotationAngle));
        double rotY          = canvasCenterY - radius * Math.cos(Math.toRadians(rotationAngle));
        clip.setCenterX(rotX);
        clip.setCenterY(rotY);
    }


    // ******************** Resizing ******************************************
    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            rotationRadius = size * 0.93;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            innerShadow.setRadius(0.016 * size);
            innerShadow.setOffsetY(0.004 * size);

            clip.setRadius(size * 0.5);

            tickCanvas.setWidth(size * 2.5);
            tickCanvas.setHeight(size * 2.5);
            tickCanvas.setClip(clip);

            needle.setStrokeWidth(size * 0.008);
            needle.setStartX(size * 0.5);
            needle.setEndX(size * 0.5);
            needle.setStartY(0);
            needle.setEndY(size);
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Tick Marks
        tickCanvas.setCache(false);
        drawTicks();
        tickCanvas.setCache(true);
        tickCanvas.setCacheHint(CacheHint.QUALITY);

        needle.setStroke(getSkinnable().getHourColor());

        ZonedDateTime time = getSkinnable().getTime();

        updateTime(time);
    }
}
