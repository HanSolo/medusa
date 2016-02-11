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
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;


/**
 * Created by hansolo on 11.02.16.
 */
public class SlimClockSkin extends SkinBase<Clock> implements Skin<Clock> {
    private static final double             PREFERRED_WIDTH     = 250;
    private static final double             PREFERRED_HEIGHT    = 250;
    private static final double             MINIMUM_WIDTH       = 50;
    private static final double             MINIMUM_HEIGHT      = 50;
    private static final double             MAXIMUM_WIDTH       = 1024;
    private static final double             MAXIMUM_HEIGHT      = 1024;
    private static final DateTimeFormatter  DATE_TEXT_FORMATTER = DateTimeFormatter.ofPattern("cccc");
    private static final DateTimeFormatter  HOUR_FORMATTER      = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter  MINUTE_FORMATTER    = DateTimeFormatter.ofPattern("mm");
    private DateTimeFormatter  dateNumberFormatter;
    private double            size;
    private Circle            secondBackgroundCircle;
    private Text              dateText;
    private Text              dateNumbers;
    private Text              hour;
    private Text              minute;
    private Arc               secondArc;
    private Pane              pane;


    // ******************** Constructors **************************************
    public SlimClockSkin(Clock clock) {
        super(clock);

        dateNumberFormatter = Helper.getLocalizedDateFormat(clock.getLocale());

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

        dateNumbers = new Text(dateNumberFormatter.format(time));
        dateNumbers.setVisible(getSkinnable().isDateVisible());
        dateNumbers.setManaged(getSkinnable().isDateVisible());

        hour = new Text(HOUR_FORMATTER.format(time));
        hour.setFill(getSkinnable().getHourColor());

        minute = new Text(MINUTE_FORMATTER.format(time));
        minute.setFill(getSkinnable().getMinuteColor());

        secondArc = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.96, PREFERRED_WIDTH * 0.48, 90, (-6 * getSkinnable().getTime().getSecond()));
        secondArc.setStrokeWidth(PREFERRED_WIDTH * 0.008);
        secondArc.setStrokeType(StrokeType.CENTERED);
        secondArc.setStrokeLineCap(StrokeLineCap.ROUND);
        secondArc.setFill(null);
        secondArc.setStroke(getSkinnable().getSecondColor());
        secondArc.setVisible(getSkinnable().isSecondsVisible());
        secondArc.setManaged(getSkinnable().isSecondsVisible());

        pane = new Pane(secondBackgroundCircle, dateText, dateNumbers, hour, minute, secondArc);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

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
    }


    // ******************** Methods *******************************************
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
            dateNumbers.setVisible(isDateVisible);
            dateNumbers.setManaged(isDateVisible);
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

            dateNumbers.setText(dateNumberFormatter.format(TIME));
            Helper.adjustTextSize(dateNumbers, 0.6 * size, size * 0.08);
            dateNumbers.relocate((size -dateNumbers.getLayoutBounds().getWidth()) * 0.5, size * 0.68984962);
        }

        hour.setText(HOUR_FORMATTER.format(TIME));
        Helper.adjustTextSize(hour, 0.4 * size, 0.328 * size);
        hour.relocate(0.136 * size, (size - hour.getLayoutBounds().getHeight()) * 0.5);

        minute.setText(MINUTE_FORMATTER.format(TIME));
        Helper.adjustTextSize(minute, 0.4 * size, 0.328 * size);
        minute.relocate(0.544 * size, (size - minute.getLayoutBounds().getHeight()) * 0.5);

        if (secondBackgroundCircle.isVisible()) {
            secondArc.setLength((-6 * TIME.getSecond()));
            if (getSkinnable().isDiscreteSeconds()) {
                secondArc.setLength((-6 * TIME.getSecond()));
            } else {
                secondArc.setLength((-6 * TIME.getSecond() -TIME.get(ChronoField.MILLI_OF_SECOND) * 0.006));
            }
        }
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
            secondBackgroundCircle.setRadius(size * 0.48590226);
            secondBackgroundCircle.setStrokeWidth(size * 0.02819549);

            secondArc.setCenterX(center);
            secondArc.setCenterY(center);
            secondArc.setRadiusX(size * 0.48590226);
            secondArc.setRadiusY(size * 0.48590226);
            secondArc.setStrokeWidth(size * 0.02819549);

            dateText.setFont(Fonts.robotoLight(size * 0.08082707));
            dateNumbers.setFont(Fonts.robotoLight(size * 0.08082707));

            hour.setFont(Fonts.robotoMedium(size * 0.328));
            minute.setFont(Fonts.robotoThin(size * 0.328));
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / 250 * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        secondBackgroundCircle.setStroke(Helper.getTranslucentColorFrom(getSkinnable().getSecondColor(), 0.2));
        secondArc.setStroke(getSkinnable().getSecondColor());

        dateText.setFill(getSkinnable().getDateColor());
        dateNumbers.setFill(getSkinnable().getDateColor());

        hour.setFill(getSkinnable().getHourColor());
        minute.setFill(getSkinnable().getMinuteColor());

        ZonedDateTime time = getSkinnable().getTime();
        updateTime(time);
    }
}

