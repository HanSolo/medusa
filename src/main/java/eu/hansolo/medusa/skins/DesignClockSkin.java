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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;


/**
 * Created by hansolo on 09.11.16.
 */
public class DesignClockSkin extends ClockSkinBase {
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

        rotationRadius = PREFERRED_WIDTH * 1.25;

        clip = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH *0.5);

        tickCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        tickCanvas.setClip(clip);
        tickCtx = tickCanvas.getGraphicsContext2D();

        needle = new Line(PREFERRED_WIDTH * 0.5 , 0, PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT);
        needle.setFill(null);
        needle.setStrokeLineCap(StrokeLineCap.BUTT);
        needle.setStroke(clock.getHourColor());

        dropShadow = new DropShadow();
        dropShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.25), 0.015 * PREFERRED_WIDTH, 0.0, 0.0, 0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle);
        shadowGroup.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 4, 0.0, 0, 1);

        pane = new Pane(tickCanvas, shadowGroup);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setEffect(innerShadow);

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VISIBILITY".equals(EVENT_TYPE)) {

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
        Color   hourTickMarkColor      = clock.getHourTickMarkColor();
        Color   minuteTickMarkColor    = clock.getMinuteTickMarkColor();
        Color   tickLabelColor         = clock.getTickLabelColor();
        boolean hourTickMarksVisible   = clock.isHourTickMarksVisible();
        boolean minuteTickMarksVisible = clock.isMinuteTickMarksVisible();
        boolean tickLabelsVisible      = clock.isTickLabelsVisible();
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
    @Override public void updateTime(final ZonedDateTime TIME) {
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

    @Override public void updateAlarms() {}


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        double width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        double height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            rotationRadius = size * 0.93;

            pane.setMaxSize(size, size);
            pane.relocate((clock.getWidth() - size) * 0.5, (clock.getHeight() - size) * 0.5);

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

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        shadowGroup.setEffect(clock.getShadowsEnabled() ? dropShadow : null);

        // Tick Marks
        tickCanvas.setCache(false);
        drawTicks();
        tickCanvas.setCache(true);
        tickCanvas.setCacheHint(CacheHint.QUALITY);

        needle.setStroke(clock.getHourColor());

        ZonedDateTime time = clock.getTime();

        updateTime(time);
    }
}
