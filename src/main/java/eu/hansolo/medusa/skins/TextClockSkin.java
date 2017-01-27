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
import javafx.geometry.VPos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


/**
 * Created by hansolo on 29.09.16.
 */
public class TextClockSkin extends ClockSkinBase {
    protected static final double            PREFERRED_WIDTH       = 250;
    protected static final double            PREFERRED_HEIGHT      = 100;
    protected static final double            MINIMUM_WIDTH         = 50;
    protected static final double            MINIMUM_HEIGHT        = 20;
    protected static final double            MAXIMUM_WIDTH         = 1024;
    protected static final double            MAXIMUM_HEIGHT        = 1024;
    private   static final DateTimeFormatter HHMM_FORMATTER        = DateTimeFormatter.ofPattern("HH:mm");
    private   static final DateTimeFormatter HHMMSS_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm:ss");
    private   static final DateTimeFormatter AMPM_HHMM_FORMATTER   = DateTimeFormatter.ofPattern("hh:mm a");
    private   static final DateTimeFormatter AMPM_HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private                double            aspectRatio           = 0.4;
    private                double            width;
    private                double            height;
    private                DateTimeFormatter dateFormat;
    private                Text              timeText;
    private                Text              dateText;
    private                Pane              pane;
    private                Color             textColor;
    private                Color             dateColor;
    private                Font              customFont;


    // ******************** Constructors **************************************
    public TextClockSkin(Clock clock) {
        super(clock);
        textColor  = clock.getTextColor();
        dateColor  = clock.getDateColor();
        dateFormat = Helper.getDateFormat(clock.getLocale());
        customFont = clock.getCustomFont();

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

        timeText = new Text();
        timeText.setTextOrigin(VPos.CENTER);
        timeText.setFill(textColor);

        dateText = new Text();
        dateText.setTextOrigin(VPos.CENTER);
        dateText.setFill(dateColor);

        pane = new Pane(timeText, dateText);
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(clock.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

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
    private void drawTime(final ZonedDateTime TIME) {
        // draw the time
        if (clock.isTextVisible()) {
            if (Locale.US == clock.getLocale()) {
                timeText.setText(clock.isSecondsVisible() ? AMPM_HHMMSS_FORMATTER.format(TIME) : AMPM_HHMM_FORMATTER.format(TIME));
            } else {
                timeText.setText(clock.isSecondsVisible() ? HHMMSS_FORMATTER.format(TIME) : HHMM_FORMATTER.format(TIME));
            }
            timeText.setX((width - timeText.getLayoutBounds().getWidth()) * 0.5);
        }

        // draw the date
        if (clock.isDateVisible()) {
            dateText.setText(dateFormat.format(TIME));
            dateText.setX((width - dateText.getLayoutBounds().getWidth()) * 0.5);
        }
    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        drawTime(TIME);
    }

    @Override public void updateAlarms() {}


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((clock.getWidth() - height) * 0.5, (clock.getHeight() - height) * 0.5);

            if (clock.isTextVisible()) {
                if (null == customFont) {
                    timeText.setFont(Locale.US == clock.getLocale() ? Fonts.robotoLight(0.5 * height) : Fonts.robotoLight(0.6 * height));
                } else {
                    timeText.setFont(Locale.US == clock.getLocale() ? new Font(customFont.getName(), 0.5 * height) : new Font(customFont.getName(), 0.6 * height));
                }
                timeText.setX((width - timeText.getLayoutBounds().getWidth()) * 0.5);
                if (clock.isDateVisible()) {
                    timeText.setY(height * 0.3);
                } else {
                    timeText.setY(height * 0.5);
                }
            }
            if (clock.isDateVisible()) {
                if (null == customFont) {
                    dateText.setFont(Fonts.robotoLight(0.3 * height));
                } else {
                    dateText.setFont(new Font(customFont.getName(), 0.3 * height));
                }
                dateText.setX((width - dateText.getLayoutBounds().getWidth()) * 0.5);
                dateText.setY(height * 0.8);
            }
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(clock.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(clock.getBorderWidth() / PREFERRED_WIDTH * height))));
        pane.setBackground(new Background(new BackgroundFill(clock.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        ZonedDateTime time = clock.getTime();

        dateFormat = Helper.getDateFormat(clock.getLocale());

        textColor  = clock.getTextColor();
        timeText.setFill(textColor);

        dateColor  = clock.getDateColor();
        dateText.setFill(dateColor);

        drawTime(time);
    }
}
