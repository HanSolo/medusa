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
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.tools.Helper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


/**
 * Created by hansolo on 28.01.16.
 */
public class LcdClockSkin extends ClockSkinBase {
    private static final double     PREFERRED_WIDTH    = 220;//190;
    private static final double     PREFERRED_HEIGHT   = 100;
    private static final Color      DARK_NOISE_COLOR   = Color.rgb(100, 100, 100, 0.10);
    private static final Color      BRIGHT_NOISE_COLOR = Color.rgb(200, 200, 200, 0.05);
    private static final DropShadow FOREGROUND_SHADOW  = new DropShadow();
    private static final double     ASPECT_RATIO       = 0.52631579;
    private double                  width;
    private double                  height;
    private Pane                    pane;
    private Paint                   lcdPaint;
    private Paint                   lcdFramePaint;
    private ImageView               crystalOverlay;
    private Image                   crystalImage;
    private Rectangle               crystalClip;
    private InnerShadow             mainInnerShadow0;
    private InnerShadow             mainInnerShadow1;
    private Text                    timeText;
    private Text                    backgroundTimeText;
    private Text                    secondText;
    private Text                    backgroundSecondText;
    private Text                    title;
    private Text                    dateText;
    private Text                    dayOfWeekText;
    private Path                    alarm;
    private DateTimeFormatter       dateFormat;
    private double                  digitalFontSizeFactor;
    private Font                    timeFont;
    private Font                    secondFont;
    private Font                    titleFont;
    private Font                    smallFont;
    private StringBuilder           backgroundTextBuilder;
    private Group                   shadowGroup;


    // ******************** Constructors **************************************
    public LcdClockSkin(Clock clock) {
        super(clock);
        digitalFontSizeFactor = 1.0;
        backgroundTextBuilder = new StringBuilder();
        FOREGROUND_SHADOW.setOffsetX(0);
        FOREGROUND_SHADOW.setOffsetY(1);
        FOREGROUND_SHADOW.setColor(Color.rgb(0, 0, 0, 0.5));
        FOREGROUND_SHADOW.setBlurType(BlurType.TWO_PASS_BOX);
        FOREGROUND_SHADOW.setRadius(2);
        adjustDateFormat();
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

        mainInnerShadow0 = new InnerShadow();
        mainInnerShadow0.setOffsetX(0.0);
        mainInnerShadow0.setOffsetY(0.0);
        mainInnerShadow0.setRadius(3.0 / 132.0 * PREFERRED_WIDTH);
        mainInnerShadow0.setColor(Color.rgb(255, 255, 255, 0.5));
        mainInnerShadow0.setBlurType(BlurType.TWO_PASS_BOX);

        mainInnerShadow1 = new InnerShadow();
        mainInnerShadow1.setOffsetX(0.0);
        mainInnerShadow1.setOffsetY(1.0);
        mainInnerShadow1.setRadius(2.0 / 132.0 * PREFERRED_WIDTH);
        mainInnerShadow1.setColor(Color.rgb(0, 0, 0, 0.65));
        mainInnerShadow1.setBlurType(BlurType.TWO_PASS_BOX);
        mainInnerShadow1.setInput(mainInnerShadow0);

        crystalClip = new Rectangle(0, 0, PREFERRED_WIDTH, PREFERRED_HEIGHT);
        crystalClip.setArcWidth(5);
        crystalClip.setArcHeight(5);

        crystalImage   = Helper.createNoiseImage(PREFERRED_WIDTH, PREFERRED_HEIGHT, DARK_NOISE_COLOR, BRIGHT_NOISE_COLOR, 8);
        crystalOverlay = new ImageView(crystalImage);
        crystalOverlay.setClip(crystalClip);
        boolean crystalEnabled = clock.isLcdCrystalEnabled();
        crystalOverlay.setManaged(crystalEnabled);
        crystalOverlay.setVisible(crystalEnabled);

        boolean secondsVisible = clock.isSecondsVisible();

        backgroundTimeText = new Text("");
        backgroundTimeText.setFill(clock.getLcdDesign().lcdBackgroundColor);
        backgroundTimeText.setOpacity((LcdFont.LCD == clock.getLcdFont() || LcdFont.ELEKTRA == clock.getLcdFont()) ? 1 : 0);

        backgroundSecondText = new Text("");
        backgroundSecondText.setFill(clock.getLcdDesign().lcdBackgroundColor);
        backgroundSecondText.setOpacity((LcdFont.LCD == clock.getLcdFont() || LcdFont.ELEKTRA == clock.getLcdFont()) ? 1 : 0);
        backgroundSecondText.setManaged(secondsVisible);
        backgroundSecondText.setVisible(secondsVisible);

        timeText = new Text("");
        timeText.setFill(clock.getLcdDesign().lcdForegroundColor);

        secondText = new Text("");
        secondText.setFill(clock.getLcdDesign().lcdForegroundColor);
        secondText.setManaged(secondsVisible);
        secondText.setVisible(secondsVisible);

        title = new Text(clock.getTitle());
        title.setFill(clock.getLcdDesign().lcdForegroundColor);
        boolean titleVisible = clock.isTitleVisible();
        title.setManaged(titleVisible);
        title.setVisible(titleVisible);

        dateText = new Text(dateFormat.format(clock.getTime()));
        dateText.setFill(clock.getLcdDesign().lcdForegroundColor);
        boolean dateVisible = clock.isDateVisible();
        dateText.setManaged(dateVisible);
        dateText.setVisible(dateVisible);

        dayOfWeekText = new Text("");
        dayOfWeekText.setFill(clock.getLcdDesign().lcdForegroundColor);
        dayOfWeekText.setManaged(dateVisible);
        dayOfWeekText.setVisible(dateVisible);

        alarm = new Path();
        alarm.setFillRule(FillRule.EVEN_ODD);
        alarm.setStroke(null);
        boolean alarmVisible = clock.getAlarms().size() > 0;
        alarm.setManaged(alarmVisible);
        alarm.setVisible(alarmVisible);

        shadowGroup = new Group();
        shadowGroup.setEffect(clock.getShadowsEnabled() ? FOREGROUND_SHADOW : null);
        shadowGroup.getChildren().setAll(timeText,
                                         secondText,
                                         title,
                                         dateText,
                                         dayOfWeekText,
                                         alarm);

        pane = new Pane();
        pane.setEffect(clock.getShadowsEnabled() ? mainInnerShadow1 : null);
        pane.getChildren().setAll(crystalOverlay,
                                  backgroundTimeText,
                                  backgroundSecondText,
                                  shadowGroup);
        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("REDRAW".equals(EVENT_TYPE)) {
            pane.setEffect(clock.getShadowsEnabled() ? mainInnerShadow1 : null);
            shadowGroup.setEffect(clock.getShadowsEnabled() ? FOREGROUND_SHADOW : null);
            updateLcdDesign(height);
            redraw();
        } else if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("LCD".equals(EVENT_TYPE)) {
            updateLcdDesign(height);
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            boolean crystalEnable = clock.isLcdCrystalEnabled();
            crystalOverlay.setManaged(crystalEnable);
            crystalOverlay.setVisible(crystalEnable);
            boolean secondsVisible = clock.isSecondsVisible();
            backgroundSecondText.setManaged(secondsVisible);
            backgroundSecondText.setVisible(secondsVisible);
            secondText.setManaged(secondsVisible);
            secondText.setVisible(secondsVisible);
            boolean dateVisible = clock.isDateVisible();
            dateText.setManaged(dateVisible);
            dateText.setVisible(dateVisible);
            dayOfWeekText.setManaged(dateVisible);
            dayOfWeekText.setVisible(dateVisible);
            boolean titleVisible = clock.isTitleVisible();
            title.setManaged(titleVisible);
            title.setVisible(titleVisible);
            boolean alarmVisible = clock.getAlarms().size() > 0;
            alarm.setManaged(alarmVisible);
            alarm.setVisible(alarmVisible);
            resize();
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            adjustDateFormat();
            redraw();
        }
    }


    // ******************** Private Methods ***********************************
    private String ensureTwoDigits(final int NUMBER) {
        if (NUMBER < 10) return "0" + NUMBER;
        return Integer.toString(NUMBER);
    }

    private void updateLcdDesign(final double HEIGHT) {
        LcdDesign lcdDesign = clock.getLcdDesign();
        Color[]   lcdColors = lcdDesign.getColors();

        lcdPaint = new LinearGradient(0, 1, 0, HEIGHT - 1,
                                      false, CycleMethod.NO_CYCLE,
                                      new Stop(0, lcdColors[0]),
                                      new Stop(0.03, lcdColors[1]),
                                      new Stop(0.5, lcdColors[2]),
                                      new Stop(0.5, lcdColors[3]),
                                      new Stop(1.0, lcdColors[4]));
        if (lcdDesign.name().startsWith("FLAT")) {
            lcdFramePaint = clock.getBorderPaint();

            lcdPaint      = clock.getBackgroundPaint();

            Color lcdForegroundColor = (Color) clock.getForegroundPaint();
            backgroundTimeText.setFill(Color.color(lcdForegroundColor.getRed(), lcdForegroundColor.getGreen(), lcdForegroundColor.getBlue(), 0.1));
            backgroundSecondText.setFill(Color.color(lcdForegroundColor.getRed(), lcdForegroundColor.getGreen(), lcdForegroundColor.getBlue(), 0.1));
            timeText.setFill(lcdForegroundColor);
            secondText.setFill(lcdForegroundColor);
            title.setFill(lcdForegroundColor);
            dateText.setFill(lcdForegroundColor);
            dayOfWeekText.setFill(lcdForegroundColor);
            alarm.setFill(lcdForegroundColor);
        } else {
            lcdFramePaint = new LinearGradient(0, 0.02083333 * height, 0, HEIGHT - 0.02083333 * HEIGHT,
                                               false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, Color.rgb(26, 26, 26)),
                                               new Stop(0.015, Color.rgb(77, 77, 77)),
                                               new Stop(0.985, Color.rgb(77, 77, 77)),
                                               new Stop(1.0, Color.rgb(221, 221, 221)));

            lcdPaint = new LinearGradient(0, 1, 0, HEIGHT - 1,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0, lcdColors[0]),
                                          new Stop(0.03, lcdColors[1]),
                                          new Stop(0.5, lcdColors[2]),
                                          new Stop(0.5, lcdColors[3]),
                                          new Stop(1.0, lcdColors[4]));

            backgroundTimeText.setFill(lcdDesign.lcdBackgroundColor);
            backgroundSecondText.setFill(lcdDesign.lcdBackgroundColor);
            timeText.setFill(lcdDesign.lcdForegroundColor);
            secondText.setFill(lcdDesign.lcdForegroundColor);
            title.setFill(lcdDesign.lcdForegroundColor);
            dateText.setFill(lcdDesign.lcdForegroundColor);
            dayOfWeekText.setFill(lcdDesign.lcdForegroundColor);
            alarm.setFill(lcdDesign.lcdForegroundColor);
        }

        pane.setBackground(new Background(new BackgroundFill(lcdPaint, new CornerRadii(0.10416667 * HEIGHT), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(lcdFramePaint, BorderStrokeStyle.SOLID, new CornerRadii(0.05 * HEIGHT), new BorderWidths(0.02083333 * HEIGHT))));
    }

    private void adjustDateFormat() {
        Locale locale = clock.getLocale();
        if (Locale.US == locale) {
            dateFormat = DateTimeFormatter.ofPattern("MM/dd/YYYY");
        } else if (Locale.CHINA == locale) {
            dateFormat = DateTimeFormatter.ofPattern("YYYY.MM.dd");
        } else {
            dateFormat = DateTimeFormatter.ofPattern("dd.MM.YYYY");
        }
    }

    private void updateFonts() {
        digitalFontSizeFactor = 1.0;
        switch(clock.getLcdFont()) {
            case LCD:
                digitalFontSizeFactor = 1.05;
                timeFont   = Fonts.digital(0.675 * height);
                secondFont = Fonts.digital(0.2 * height);
                break;
            case DIGITAL:
                digitalFontSizeFactor = 0.7;
                timeFont   = Fonts.digitalReadout(0.7 * height);
                secondFont = Fonts.digitalReadout(0.2 * height);
                break;
            case DIGITAL_BOLD:
                digitalFontSizeFactor = 0.7;
                timeFont   = Fonts.digitalReadoutBold(0.7 * height);
                secondFont = Fonts.digitalReadoutBold(0.2 * height);
                break;
            case ELEKTRA:
                digitalFontSizeFactor = 0.8;
                timeFont   = Fonts.elektra(0.7 * height);
                secondFont = Fonts.elektra(0.186 * height);
                break;
            case STANDARD:
            default:
                digitalFontSizeFactor = 0.7;
                timeFont   = Fonts.robotoMedium(0.6 * height);
                secondFont = Fonts.robotoMedium(0.2 * height);
                break;
        }

        backgroundTimeText.setFont(timeFont);
        backgroundTimeText.setOpacity((LcdFont.LCD == clock.getLcdFont() ||
                                       LcdFont.DIGITAL == clock.getLcdFont() ||
                                       LcdFont.DIGITAL_BOLD == clock.getLcdFont() ||
                                       LcdFont.ELEKTRA == clock.getLcdFont()) ? 1 : 0);

        backgroundSecondText.setFont(secondFont);
        backgroundSecondText.setOpacity((LcdFont.LCD == clock.getLcdFont() ||
                                         LcdFont.DIGITAL == clock.getLcdFont() ||
                                         LcdFont.DIGITAL_BOLD == clock.getLcdFont() ||
                                         LcdFont.ELEKTRA == clock.getLcdFont()) ? 1 : 0);

        timeText.setFont(timeFont);
        titleFont = Fonts.latoBold(0.16 * height);
        smallFont = Fonts.latoBold(0.12 * height);
    }

    private void updateBackgroundText() {
        // Setup the semitransparent background timeText
        backgroundTimeText.setTextOrigin(VPos.BASELINE);
        backgroundTimeText.setTextAlignment(TextAlignment.RIGHT);

        backgroundSecondText.setTextOrigin(VPos.BASELINE);
        backgroundSecondText.setTextAlignment(TextAlignment.RIGHT);

        // Setup the semitransparent background timeText
        // Width of one time segment
        String backgroundSegment = "_";
        switch(clock.getLcdFont()) {
            case LCD         : backgroundSegment = "8"; break;
            case DIGITAL     :
            case DIGITAL_BOLD:
            case ELEKTRA     : backgroundSegment = "_"; break;
        }

        // Add segments to background timeText
        backgroundTextBuilder.setLength(0);
        backgroundTextBuilder.append(backgroundSegment);
        backgroundTextBuilder.append(backgroundSegment);
        backgroundTextBuilder.append(":");
        backgroundTextBuilder.append(backgroundSegment);
        backgroundTextBuilder.append(backgroundSegment);
        backgroundTimeText.setText(backgroundTextBuilder.toString());
        backgroundSecondText.setText(backgroundSegment + backgroundSegment);
    }

    private void updateTimePosition() {

        double secondsWidth = secondText.getLayoutBounds().getWidth();
        double secodsOffset = height * 0.03;
        double timeWidth    = timeText.getLayoutBounds().getWidth();
        double timeX        = ( clock.isSecondsVisible() )
                            ? ( width - 2 - timeWidth - secodsOffset - secondsWidth ) * 0.5
                            : ( width - 2 - timeWidth ) * 0.5;
        double timeY        = height - timeText.getLayoutBounds().getHeight() * digitalFontSizeFactor * 0.5;
        double secondsX     = timeX + 1 + timeWidth + secodsOffset;

        backgroundTimeText.setX(timeX);
        backgroundTimeText.setY(timeY);
        backgroundSecondText.setX(secondsX);
        backgroundSecondText.setY(timeY);

        timeText.setX(timeX);
        timeText.setY(timeY);
        secondText.setX(secondsX);
        secondText.setY(timeY);

    }

    @Override public void updateTime(final ZonedDateTime TIME) {
        timeText.setText(ensureTwoDigits(TIME.getHour()) + ":" + ensureTwoDigits(TIME.getMinute()));
        secondText.setText(ensureTwoDigits(TIME.getSecond()));

        updateBackgroundText();
        updateTimePosition();

        title.setText(clock.getTitle());
        title.setX((width - title.getLayoutBounds().getWidth()) * 0.5);

        dayOfWeekText.setText(TIME.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, clock.getLocale()));
        dayOfWeekText.setX(0.0416666667 * height);

        dateText.setText(dateFormat.format(TIME));
        dateText.setX(width - dateText.getLayoutBounds().getWidth() - 0.0416666667 * height);
        dateText.setY(pane.getLayoutBounds().getMinY() + height - 3 - 0.0416666667 * height);
    }

    @Override public void updateAlarms() {}

    private void createAlarmIcon(final boolean ENABLED) {
        double iconSize = 0.16 * height;
        alarm.getElements().clear();
        if (ENABLED) {
            alarm.getElements().add(new MoveTo(0.65625 * iconSize, 0.875 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.6328125 * iconSize, 0.9453125 * iconSize,
                                                    0.5703125 * iconSize, iconSize,
                                                    0.5 * iconSize, iconSize));
            alarm.getElements().add(new CubicCurveTo(0.4296875 * iconSize, iconSize,
                                                    0.3671875 * iconSize, 0.9453125 * iconSize,
                                                    0.34375 * iconSize, 0.875 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.34375 * iconSize, 0.875 * iconSize,
                                                    0.65625 * iconSize, 0.875 * iconSize,
                                                    0.65625 * iconSize, 0.875 * iconSize));
            alarm.getElements().add(new ClosePath());
            alarm.getElements().add(new MoveTo(0.9921875 * iconSize, 0.7578125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.65625 * iconSize, 0.53125 * iconSize,
                                                    0.90625 * iconSize, 0.0,
                                                    0.5 * iconSize, 0.0));
            alarm.getElements().add(new CubicCurveTo(0.5 * iconSize, 0.0,
                                                    0.5 * iconSize, 0.0,
                                                    0.5 * iconSize, 0.0));
            alarm.getElements().add(new CubicCurveTo(0.5 * iconSize, 0.0,
                                                    0.5 * iconSize, 0.0,
                                                    0.5 * iconSize, 0.0));
            alarm.getElements().add(new CubicCurveTo(0.09375 * iconSize, 0.0,
                                                    0.34375 * iconSize, 0.53125 * iconSize,
                                                    0.0078125 * iconSize, 0.7578125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.0, 0.765625 * iconSize,
                                                    0.0, 0.7734375 * iconSize,
                                                    0.0, 0.78125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.0, 0.78125 * iconSize,
                                                    0.0, 0.7890625 * iconSize,
                                                    0.0, 0.7890625 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.0078125 * iconSize, 0.8046875 * iconSize,
                                                    0.015625 * iconSize, 0.8125 * iconSize,
                                                    0.03125 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.03125 * iconSize, 0.8125 * iconSize,
                                                    0.5 * iconSize, 0.8125 * iconSize,
                                                    0.5 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.5 * iconSize, 0.8125 * iconSize,
                                                    0.96875 * iconSize, 0.8125 * iconSize,
                                                    0.96875 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.984375 * iconSize, 0.8125 * iconSize,
                                                    0.9921875 * iconSize, 0.8046875 * iconSize,
                                                    iconSize, 0.7890625 * iconSize));
            alarm.getElements().add(new CubicCurveTo(iconSize, 0.7890625 * iconSize,
                                                    iconSize, 0.78125 * iconSize,
                                                    iconSize, 0.78125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(iconSize, 0.7734375 * iconSize,
                                                    iconSize, 0.765625 * iconSize,
                                                    0.9921875 * iconSize, 0.7578125 * iconSize));
            alarm.getElements().add(new ClosePath());
        } else {
            alarm.getElements().add(new MoveTo(0.0703125 * iconSize, 0.015625 * iconSize));
            alarm.getElements().add(new LineTo(0.015625 * iconSize, 0.0703125 * iconSize));
            alarm.getElements().add(new LineTo(0.9296875 * iconSize, 0.984375 * iconSize));
            alarm.getElements().add(new LineTo(0.984375 * iconSize, 0.9296875 * iconSize));
            alarm.getElements().add(new LineTo(0.0703125 * iconSize, 0.015625 * iconSize));
            alarm.getElements().add(new ClosePath());
            alarm.getElements().add(new MoveTo(0.65625 * iconSize, 0.875 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.65625 * iconSize, 0.875 * iconSize,
                                                    0.34375 * iconSize, 0.875 * iconSize,
                                                    0.34375 * iconSize, 0.875 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.3671875 * iconSize, 0.9453125 * iconSize,
                                                    0.4296875 * iconSize, iconSize,
                                                    0.5 * iconSize, iconSize));
            alarm.getElements().add(new CubicCurveTo(0.5703125 * iconSize, iconSize,
                                                    0.6328125 * iconSize, 0.9453125 * iconSize,
                                                    0.65625 * iconSize, 0.875 * iconSize));
            alarm.getElements().add(new ClosePath());
            alarm.getElements().add(new MoveTo(0.2265625 * iconSize, 0.3125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.1953125 * iconSize, 0.46875 * iconSize,
                                                    0.171875 * iconSize, 0.6484375 * iconSize,
                                                    0.0078125 * iconSize, 0.7578125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.0, 0.765625 * iconSize,
                                                    0.0, 0.7734375 * iconSize,
                                                    0.0, 0.78125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.0, 0.78125 * iconSize,
                                                    0.0, 0.7890625 * iconSize,
                                                    0.0, 0.7890625 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.0078125 * iconSize, 0.8046875 * iconSize,
                                                    0.015625 * iconSize, 0.8125 * iconSize,
                                                    0.03125 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.03125 * iconSize, 0.8125 * iconSize,
                                                    0.5 * iconSize, 0.8125 * iconSize,
                                                    0.5 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new LineTo(0.7265625 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.7265625 * iconSize, 0.8125 * iconSize,
                                                    0.2265625 * iconSize, 0.3125 * iconSize,
                                                    0.2265625 * iconSize, 0.3125 * iconSize));
            alarm.getElements().add(new ClosePath());
            alarm.getElements().add(new MoveTo(0.9921875 * iconSize, 0.7578125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.65625 * iconSize, 0.53125 * iconSize,
                                                    0.90625 * iconSize, 0.0,
                                                    0.5 * iconSize, 0.0));
            alarm.getElements().add(new CubicCurveTo(0.3515625 * iconSize, 0.0,
                                                    0.2890625 * iconSize, 0.0703125 * iconSize,
                                                    0.2578125 * iconSize, 0.171875 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.2578125 * iconSize, 0.171875 * iconSize,
                                                    0.8984375 * iconSize, 0.8125 * iconSize,
                                                    0.8984375 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.8984375 * iconSize, 0.8125 * iconSize,
                                                    0.96875 * iconSize, 0.8125 * iconSize,
                                                    0.96875 * iconSize, 0.8125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(0.984375 * iconSize, 0.8125 * iconSize,
                                                    0.9921875 * iconSize, 0.8046875 * iconSize,
                                                    iconSize, 0.7890625 * iconSize));
            alarm.getElements().add(new CubicCurveTo(iconSize, 0.7890625 * iconSize,
                                                    iconSize, 0.78125 * iconSize,
                                                    iconSize, 0.78125 * iconSize));
            alarm.getElements().add(new CubicCurveTo(iconSize, 0.7734375 * iconSize,
                                                    iconSize, 0.765625 * iconSize,
                                                    0.9921875 * iconSize, 0.7578125 * iconSize));
            alarm.getElements().add(new ClosePath());
        }
    }

    @Override protected void resize() {
        width  = clock.getWidth() - clock.getInsets().getLeft() - clock.getInsets().getRight();
        height = clock.getHeight() - clock.getInsets().getTop() - clock.getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((clock.getWidth() - width) * 0.5, (clock.getHeight() - height) * 0.5);

            updateLcdDesign(height);

            mainInnerShadow0.setRadius(0.0625 * height);
            mainInnerShadow1.setRadius(0.04166667 * height);

            ZonedDateTime time = clock.getTime();

            if (crystalOverlay.isVisible()) {
                crystalClip.setWidth(width);
                crystalClip.setHeight(height);
                crystalOverlay.setImage(Helper.createNoiseImage(width, height, DARK_NOISE_COLOR, BRIGHT_NOISE_COLOR, 8));
                crystalOverlay.setCache(true);
            }

            updateFonts();

            updateBackgroundText();
            updateTimePosition();

            secondText.setFont(secondFont);
            secondText.setTextOrigin(VPos.BASELINE);
            secondText.setTextAlignment(TextAlignment.RIGHT);

            // Setup the font for the lcd title, number system, min measured, max measure and former value
            // Title
            title.setFont(titleFont);
            title.setTextOrigin(VPos.BASELINE);
            title.setTextAlignment(TextAlignment.CENTER);
            title.setText(time.getDayOfWeek().name());
            title.setX((width - title.getLayoutBounds().getWidth()) * 0.5);
            title.setY(pane.getLayoutBounds().getMinY() + title.getLayoutBounds().getHeight() - 0.04 * height + 2);

            // Date
            dateText.setFont(smallFont);
            dateText.setTextOrigin(VPos.BASELINE);
            dateText.setTextAlignment(TextAlignment.RIGHT);
            dateText.setText(dateFormat.format(time));
            dateText.setX(width - dateText.getLayoutBounds().getWidth() - 0.0416666667 * height);
            dateText.setY(pane.getLayoutBounds().getMinY() + height - 3 - 0.0416666667 * height);

            // Day of week
            dayOfWeekText.setFont(smallFont);
            dayOfWeekText.setTextOrigin(VPos.BASELINE);
            dayOfWeekText.setTextAlignment(TextAlignment.LEFT);
            dayOfWeekText.setX(0.0416666667 * height);
            dayOfWeekText.setY(pane.getLayoutBounds().getMinY() + height - 3 - 0.0416666667 * height);
        }
    }

    @Override protected void redraw() {
        updateBackgroundText();
        updateTimePosition();

        ZonedDateTime time = clock.getTime();
        timeText.setText(ensureTwoDigits(time.getHour()) + ":" + ensureTwoDigits(time.getMinute()));
        secondText.setText(ensureTwoDigits(time.getSecond()));

        title.setText(clock.getTitle());
        title.setX((width - title.getLayoutBounds().getWidth()) * 0.5);

        dateText.setText(dateFormat.format(time));
        dateText.setX(width - dateText.getLayoutBounds().getWidth() - 0.0416666667 * height);
        dateText.setY(pane.getLayoutBounds().getMinY() + height - 3 - 0.0416666667 * height);

        dayOfWeekText.setText(time.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, clock.getLocale()));
        dayOfWeekText.setX(0.0416666667 * height);
        dayOfWeekText.setY(pane.getLayoutBounds().getMinY() + height - 3 - 0.0416666667 * height);

        createAlarmIcon(clock.isAlarmsEnabled());
        alarm.relocate(width * 0.885, height * 0.28);
    }
}
