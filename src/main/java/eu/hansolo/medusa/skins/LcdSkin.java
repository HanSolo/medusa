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

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by hansolo on 21.01.16.
 */
public class LcdSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double                PREFERRED_WIDTH    = 275;
    private static final double                PREFERRED_HEIGHT   = 100;
    private static final double                MINIMUM_WIDTH      = 5;
    private static final double                MINIMUM_HEIGHT     = 5;
    private static final double                MAXIMUM_WIDTH      = 1024;
    private static final double                MAXIMUM_HEIGHT     = 1024;
    private static final Color                 DARK_NOISE_COLOR   = Color.rgb(100, 100, 100, 0.10);
    private static final Color                 BRIGHT_NOISE_COLOR = Color.rgb(200, 200, 200, 0.05);
    private static final DropShadow            FOREGROUND_SHADOW  = new DropShadow();
    private static       double                aspectRatio        = 0.36363636;
    private static       Text                  oneSegment         = new Text("8");
    private static       Text                  dotSegment         = new Text(".");
    private              double                width;
    private              double                height;
    private              Pane                  pane;
    private              Paint                 lcdPaint;
    private              Paint                 lcdFramePaint;
    private              ImageView             crystalOverlay;
    private              Image                 crystalImage;
    private              Rectangle             crystalClip;
    private              InnerShadow           mainInnerShadow0;
    private              InnerShadow           mainInnerShadow1;
    private              Path                  threshold;
    private              Text                  valueText;
    private              Text                  backgroundText;
    private              Text                  unitText;
    private              Text                  title;
    private              Text                  lowerRightText;
    private              Text                  upperLeftText;
    private              Text                  upperRightText;
    private              Text                  lowerCenterText;
    private              double                valueOffsetLeft;
    private              double                valueOffsetRight;
    private              double                digitalFontSizeFactor;
    private              Font                  valueFont;
    private              Font                  unitFont;
    private              Font                  titleFont;
    private              Font                  smallFont;
    private              double                oneSegmentWidth;
    private              double                dotSegmentWidth;
    private              double                widthOfDecimals;
    private              double                availableWidth;
    private              int                   noOfSegments;
    private              StringBuilder         backgroundTextBuilder;
    private              Group                 shadowGroup;
    private              String                valueFormatString;
    private              String                otherFormatString;
    private              Locale                locale;
    private              List<Section>         sections;
    private              Map<Section, Color[]> sectionColorMap;


    // ******************** Constructors **************************************
    public LcdSkin(Gauge gauge) {
        super(gauge);
        width                 = PREFERRED_WIDTH;
        height                = PREFERRED_HEIGHT;
        valueOffsetLeft       = 0.0;
        valueOffsetRight      = 0.0;
        digitalFontSizeFactor = 1.0;
        backgroundTextBuilder = new StringBuilder();
        valueFormatString     = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        otherFormatString     = new StringBuilder("%.").append(Integer.toString(gauge.getTickLabelDecimals())).append("f").toString();
        locale                = gauge.getLocale();
        sections              = gauge.getSections();
        sectionColorMap       = new HashMap<>(sections.size());
        updateSectionColors();
        FOREGROUND_SHADOW.setOffsetX(0);
        FOREGROUND_SHADOW.setOffsetY(1);
        FOREGROUND_SHADOW.setColor(Color.rgb(0, 0, 0, 0.5));
        FOREGROUND_SHADOW.setBlurType(BlurType.TWO_PASS_BOX);
        FOREGROUND_SHADOW.setRadius(2);
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() > 0 && getSkinnable().getPrefHeight() > 0) {
                getSkinnable().setPrefSize(getSkinnable().getPrefWidth(), getSkinnable().getPrefHeight());
            } else {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }

        if (getSkinnable().getPrefWidth() != PREFERRED_WIDTH || getSkinnable().getPrefHeight() != PREFERRED_HEIGHT) {
            aspectRatio = getSkinnable().getPrefHeight() / getSkinnable().getPrefWidth();
        }
    }

    private void initGraphics() {
        mainInnerShadow0 = new InnerShadow();
        mainInnerShadow0.setOffsetX(0.0);
        mainInnerShadow0.setOffsetY(0.0);
        mainInnerShadow0.setRadius(0.0625 * PREFERRED_HEIGHT);
        mainInnerShadow0.setColor(Color.rgb(255, 255, 255, 0.5));
        mainInnerShadow0.setBlurType(BlurType.TWO_PASS_BOX);

        mainInnerShadow1 = new InnerShadow();
        mainInnerShadow1.setOffsetX(0.0);
        mainInnerShadow1.setOffsetY(1.0);
        mainInnerShadow1.setRadius(0.04166667 * PREFERRED_HEIGHT);
        mainInnerShadow1.setColor(Color.rgb(0, 0, 0, 0.65));
        mainInnerShadow1.setBlurType(BlurType.TWO_PASS_BOX);
        mainInnerShadow1.setInput(mainInnerShadow0);

        crystalClip = new Rectangle(0, 0, width, height);
        crystalClip.setArcWidth(5);
        crystalClip.setArcHeight(5);

        crystalImage   = Helper.createNoiseImage(PREFERRED_WIDTH, PREFERRED_HEIGHT, DARK_NOISE_COLOR, BRIGHT_NOISE_COLOR, 8);
        crystalOverlay = new ImageView(crystalImage);
        crystalOverlay.setClip(crystalClip);
        boolean crystalEnabled = getSkinnable().isLcdCrystalEnabled();
        Helper.enableNode(crystalOverlay, crystalEnabled);

        threshold = new Path();
        threshold.setStroke(null);
        Helper.enableNode(threshold, getSkinnable().isThresholdVisible());
        
        backgroundText = new Text(String.format(locale, valueFormatString, getSkinnable().getCurrentValue()));
        backgroundText.setFill(getSkinnable().getLcdDesign().lcdBackgroundColor);
        backgroundText.setOpacity((LcdFont.LCD == getSkinnable().getLcdFont() || LcdFont.ELEKTRA == getSkinnable().getLcdFont()) ? 1 : 0);

        valueText = new Text(String.format(locale, valueFormatString, getSkinnable().getCurrentValue()));
        valueText.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        title = new Text(getSkinnable().getTitle());
        title.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);
        Helper.enableNode(title, !getSkinnable().getTitle().isEmpty());

        lowerRightText = new Text(getSkinnable().getSubTitle());
        lowerRightText.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);
        Helper.enableNode(lowerRightText, !getSkinnable().getSubTitle().isEmpty());

        upperLeftText = new Text(String.format(locale, otherFormatString, getSkinnable().getMinMeasuredValue()));
        upperLeftText.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);
        Helper.enableNode(upperLeftText, getSkinnable().isMinMeasuredValueVisible());

        upperRightText = new Text(String.format(locale, otherFormatString, getSkinnable().getMaxMeasuredValue()));
        upperRightText.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);
        Helper.enableNode(upperRightText, getSkinnable().isMaxMeasuredValueVisible());

        lowerCenterText = new Text(String.format(locale, otherFormatString, getSkinnable().getOldValue()));
        lowerCenterText.setFill(getSkinnable().getLcdDesign().lcdForegroundColor);
        Helper.enableNode(lowerCenterText, getSkinnable().isOldValueVisible());

        shadowGroup = new Group();
        shadowGroup.setEffect(getSkinnable().isShadowsEnabled() ? FOREGROUND_SHADOW : null);
        shadowGroup.getChildren().setAll(threshold,
                                         valueText,
                                         unitText,
                                         title,
                                         lowerRightText,
                                         upperLeftText,
                                         upperRightText,
                                         lowerCenterText);

        pane = new Pane(crystalOverlay, backgroundText, shadowGroup);
        pane.setEffect(getSkinnable().isShadowsEnabled() ? mainInnerShadow1 : null);
        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(e -> handleEvents("REDRAW"));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("REDRAW".equals(EVENT_TYPE)) {
            pane.setEffect(getSkinnable().isShadowsEnabled() ? mainInnerShadow1 : null);
            shadowGroup.setEffect(getSkinnable().isShadowsEnabled() ? FOREGROUND_SHADOW : null);
            updateLcdDesign(height);
            redraw();
        } else if ("RESIZE".equals(EVENT_TYPE)) {
            aspectRatio = getSkinnable().getPrefHeight() / getSkinnable().getPrefWidth();
            resize();
            redraw();
        } else if ("LCD".equals(EVENT_TYPE)) {
            updateLcdDesign(height);
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(crystalOverlay, getSkinnable().isLcdCrystalEnabled());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
            Helper.enableNode(upperLeftText, getSkinnable().isMinMeasuredValueVisible());
            Helper.enableNode(upperRightText, getSkinnable().isMaxMeasuredValueVisible());
            Helper.enableNode(lowerRightText, !getSkinnable().getSubTitle().isEmpty());
            Helper.enableNode(lowerCenterText, getSkinnable().isOldValueVisible());
            resize();
            redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
            updateSectionColors();
            resize();
            redraw();
        }
    }


    // ******************** Private Methods ***********************************
    private boolean isNoOfDigitsInvalid() {
        final double AVAILABLE_WIDTH = width - 2 - valueOffsetLeft - valueOffsetRight;
        final double NEEDED_WIDTH    = valueText.getLayoutBounds().getWidth();
        return Double.compare(AVAILABLE_WIDTH, NEEDED_WIDTH) < 0;
    }

    private void updateLcdDesign(final double HEIGHT) {
        LcdDesign lcdDesign = getSkinnable().getLcdDesign();
        Color[]   lcdColors = lcdDesign.getColors();

        if (LcdDesign.SECTIONS == lcdDesign) {
            double currentValue = getSkinnable().getCurrentValue();
            int listSize = sections.size();
            for (int i = 0 ; i < listSize ; i++) {
                Section section = sections.get(i);
                if (section.contains(currentValue)) {
                    lcdColors = sectionColorMap.get(section);
                    break;
                }
            }
        }

        lcdPaint = new LinearGradient(0, 1, 0, HEIGHT - 1,
                                      false, CycleMethod.NO_CYCLE,
                                      new Stop(0, lcdColors[0]),
                                      new Stop(0.03, lcdColors[1]),
                                      new Stop(0.5, lcdColors[2]),
                                      new Stop(0.5, lcdColors[3]),
                                      new Stop(1.0, lcdColors[4]));
        if (lcdDesign.name().startsWith("FLAT")) {
            lcdFramePaint = getSkinnable().getBorderPaint();

            lcdPaint      = getSkinnable().getBackgroundPaint();

            Color lcdForegroundColor = (Color) getSkinnable().getForegroundPaint();
            backgroundText.setFill(Color.color(lcdForegroundColor.getRed(), lcdForegroundColor.getGreen(), lcdForegroundColor.getBlue(), 0.1));
            valueText.setFill(lcdForegroundColor);
            upperLeftText.setFill(lcdForegroundColor);
            title.setFill(lcdForegroundColor);
            upperRightText.setFill(lcdForegroundColor);
            unitText.setFill(lcdForegroundColor);
            lowerRightText.setFill(lcdForegroundColor);
            lowerCenterText.setFill(lcdForegroundColor);
            threshold.setFill(lcdForegroundColor);
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

            backgroundText.setFill(lcdDesign.lcdBackgroundColor);
            valueText.setFill(lcdDesign.lcdForegroundColor);
            upperLeftText.setFill(lcdDesign.lcdForegroundColor);
            title.setFill(lcdDesign.lcdForegroundColor);
            upperRightText.setFill(lcdDesign.lcdForegroundColor);
            unitText.setFill(lcdDesign.lcdForegroundColor);
            lowerRightText.setFill(lcdDesign.lcdForegroundColor);
            lowerCenterText.setFill(lcdDesign.lcdForegroundColor);
            threshold.setFill(lcdDesign.lcdForegroundColor);
        }

        pane.setBackground(new Background(new BackgroundFill(lcdPaint, new CornerRadii(0.10416667 * HEIGHT), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(lcdFramePaint, BorderStrokeStyle.SOLID, new CornerRadii(0.05 * HEIGHT), new BorderWidths(0.02083333 * HEIGHT))));
    }

    private void updateSectionColors() {
        int listSize = sections.size();
        sectionColorMap.clear();
        for (int i = 0 ; i < listSize ; i++) {
            Color sectionColor = sections.get(i).getColor();
            Color lcdForegroundColor;
            if (Helper.isMonochrome(sectionColor)) {
                lcdForegroundColor = Helper.isDark(sectionColor) ? Color.WHITE : Color.BLACK;
            } else {
                lcdForegroundColor = Color.hsb(sectionColor.getHue(), sectionColor.getSaturation(), sectionColor.getBrightness() * 0.3);
            }
            Color lcdBackgroundColor = Color.color(sectionColor.getRed(), sectionColor.getGreen(), sectionColor.getBlue(), 0.1);
            sectionColorMap.put(sections.get(i), getSectionColors(lcdBackgroundColor, lcdForegroundColor));
        }
    }

    private Color[] getSectionColors(final Color LCD_BACKGROUND_COLOR, final Color LCD_FOREGROUND_COLOR) {
        double hue = LCD_BACKGROUND_COLOR.getHue();
        double sat = LCD_BACKGROUND_COLOR.getSaturation();

        Color[] colors;
        if (Helper.isMonochrome(LCD_BACKGROUND_COLOR)) {
            // Section color is monochrome
            colors = new Color[]{
                Color.hsb(hue, 0, 0.69),
                Color.hsb(hue, 0, 1.0),
                Color.hsb(hue, 0, 0.76),
                Color.hsb(hue, 0, 0.76),
                Color.hsb(hue, sat, 0.69),
                Helper.isDark(LCD_BACKGROUND_COLOR) ? Color.WHITE : Color.BLACK,
                Helper.isDark(LCD_BACKGROUND_COLOR) ? Color.rgb(255, 255, 255, 0.1) : Color.rgb(0, 0, 0, 0.1)
            };
        } else {
            // Section color is not monochrome
            colors = new Color[]{
                Color.hsb(hue, sat, 0.69),
                Color.hsb(hue, sat, 1.0),
                Color.hsb(hue, sat, 0.76),
                Color.hsb(hue, sat, 0.76),
                Color.hsb(hue, sat, 0.69),
                LCD_FOREGROUND_COLOR,
                Color.color(LCD_BACKGROUND_COLOR.getRed(), LCD_BACKGROUND_COLOR.getGreen(), LCD_BACKGROUND_COLOR.getBlue(), 0.1)
            };
        }
        return colors;
    }
    
    private void updateFonts() {
        digitalFontSizeFactor = 1.0;
        switch(getSkinnable().getLcdFont()) {
            case LCD:
                valueFont = Fonts.digital(0.6 * height);
                digitalFontSizeFactor = 1.4;
                break;
            case DIGITAL:
                valueFont = Fonts.digitalReadout(0.5833333333 * height);
                break;
            case DIGITAL_BOLD:
                valueFont = Fonts.digitalReadoutBold(0.5833333333 * height);
                break;
            case ELEKTRA:
                valueFont = Fonts.elektra(0.62 * height);
                break;
            case STANDARD:
            default:
                valueFont = Fonts.robotoMedium(0.5 * height);
                break;
        }
        backgroundText.setFont(valueFont);
        backgroundText.setOpacity((LcdFont.LCD == getSkinnable().getLcdFont() ||
                                   LcdFont.DIGITAL == getSkinnable().getLcdFont() ||
                                   LcdFont.DIGITAL_BOLD == getSkinnable().getLcdFont() ||
                                   LcdFont.ELEKTRA == getSkinnable().getLcdFont()) ? 1 : 0);
        valueText.setFont(valueFont);
        unitFont  = Fonts.latoBold(0.26 * height);
        titleFont = Fonts.latoBold(0.1666666667 * height);
        smallFont = Fonts.latoBold(0.1666666667 * height);
    }

    private void updateBackgroundText() {
        // Setup the semitransparent background text
        backgroundText.setCache(false);
        backgroundText.setTextOrigin(VPos.BASELINE);
        backgroundText.setTextAlignment(TextAlignment.RIGHT);

        // Setup the semitransparent background text
        // Width of one segment
        oneSegment.setFont(valueFont);
        dotSegment.setText(".");
        if (LcdFont.LCD == getSkinnable().getLcdFont()) {
            oneSegment.setText("8");
        } else if (LcdFont.DIGITAL == getSkinnable().getLcdFont()) {
            oneSegment.setText("_");
        } else if (LcdFont.DIGITAL_BOLD == getSkinnable().getLcdFont()) {
            oneSegment.setText("_");
        } else if (LcdFont.ELEKTRA == getSkinnable().getLcdFont()) {
            oneSegment.setText("_");
        }
        oneSegmentWidth = oneSegment.getLayoutBounds().getWidth();
        dotSegmentWidth = dotSegment.getLayoutBounds().getWidth();
        
        // Width of decimals
        widthOfDecimals = 0 == getSkinnable().getDecimals() ? 0 : getSkinnable().getDecimals() * oneSegmentWidth + (LcdFont.LCD == getSkinnable().getLcdFont() ? oneSegmentWidth : dotSegmentWidth);

        // Available width
        availableWidth = width - (0.0151515152 * width) - (0.0416666667 * height) - 2 - valueOffsetRight - widthOfDecimals;

        // Number of segments
        noOfSegments = (int) Math.floor(availableWidth / oneSegmentWidth);

        // Add segments to background text
        backgroundTextBuilder.setLength(0);
        for (int i = 0 ; i < getSkinnable().getDecimals() ; i++) {
            backgroundTextBuilder.append(oneSegment.getText());
        }

        if (getSkinnable().getDecimals() != 0) {
            backgroundTextBuilder.insert(0, ".");
        }

        for (int i = 0 ; i < noOfSegments ; i++) {
            backgroundTextBuilder.insert(0, oneSegment.getText());
        }
        backgroundText.setText(backgroundTextBuilder.toString());
        
        backgroundText.setCache(true);
        backgroundText.setCacheHint(CacheHint.SCALE);
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            updateLcdDesign(height);

            mainInnerShadow0.setRadius(0.0625 * height);
            mainInnerShadow1.setRadius(0.04166667 * height);

            if (crystalOverlay.isVisible()) {
                crystalClip.setWidth(width);
                crystalClip.setHeight(height);
                crystalOverlay.setImage(Helper.createNoiseImage(width, height, DARK_NOISE_COLOR, BRIGHT_NOISE_COLOR, 8));
                crystalOverlay.setCache(true);
            }

            double tSize = 0.2 * height;
            threshold.getElements().clear();
            threshold.getElements().add(new MoveTo(0.41666667 * tSize, 0.75 * tSize));
            threshold.getElements().add(new LineTo(0.583333333333333 * tSize, 0.75 * tSize));
            threshold.getElements().add(new LineTo(0.583333333333333 * tSize, 0.916666666666667 * tSize));
            threshold.getElements().add(new LineTo(0.416666666666667 * tSize, 0.916666666666667 * tSize));
            threshold.getElements().add(new LineTo(0.416666666666667 * tSize, 0.75 * tSize));
            threshold.getElements().add(new ClosePath());
            threshold.getElements().add(new MoveTo(0.416666666666667 * tSize, 0.333333333333333 * tSize));
            threshold.getElements().add(new LineTo(0.583333333333333 * tSize, 0.333333333333333 * tSize));
            threshold.getElements().add(new LineTo(0.583333333333333 * tSize, 0.666666666666667 * tSize));
            threshold.getElements().add(new LineTo(0.416666666666667 * tSize, 0.666666666666667 * tSize));
            threshold.getElements().add(new LineTo(0.416666666666667 * tSize, 0.333333333333333 * tSize));
            threshold.getElements().add(new ClosePath());
            threshold.getElements().add(new MoveTo(tSize, tSize));
            threshold.getElements().add(new LineTo(0.5 * tSize, 0));
            threshold.getElements().add(new LineTo(0, tSize));
            threshold.getElements().add(new LineTo(tSize, tSize));
            threshold.getElements().add(new ClosePath());
            threshold.relocate(0.027961994662429348 * width, 0.75 * height - 2);

            updateFonts();

            // Setup the lcd unit
            unitText.setFont(unitFont);
            unitText.setTextOrigin(VPos.BASELINE);
            unitText.setTextAlignment(TextAlignment.RIGHT);

            unitText.setText(getSkinnable().getUnit());
            if (unitText.visibleProperty().isBound()) {
                unitText.visibleProperty().unbind();
            }

            valueOffsetLeft = height * 0.04;

            if (getSkinnable().getUnit().isEmpty()) {
                valueOffsetRight = height * 0.0833333333;
                valueText.setX((width - valueText.getLayoutBounds().getWidth()) - valueOffsetRight);
            } else {
                unitText.setX((width - unitText.getLayoutBounds().getWidth()) - height * 0.04);
                unitText.setY(height - (valueText.getLayoutBounds().getHeight() * digitalFontSizeFactor) * 0.5);
                valueOffsetRight = (unitText.getLayoutBounds().getWidth() + height * 0.0833333333); // distance between value and unit
                valueText.setX(width - 2 - valueText.getLayoutBounds().getWidth() - valueOffsetRight);
            }
            valueText.setY(height - (valueText.getLayoutBounds().getHeight() * digitalFontSizeFactor) * 0.5);

            // Visualize the lcd semitransparent background text
            updateBackgroundText();

            if (getSkinnable().getUnit().isEmpty()) {
                backgroundText.setX((width - backgroundText.getLayoutBounds().getWidth()) - valueOffsetRight);
            } else {
                backgroundText.setX(width - 2 - backgroundText.getLayoutBounds().getWidth() - valueOffsetRight);
            }
            backgroundText.setY(height - (backgroundText.getLayoutBounds().getHeight() * digitalFontSizeFactor) * 0.5);

            // Setup the font for the lcd title, number system, min measured, max measure and former value
            // Title
            title.setFont(titleFont);
            title.setTextOrigin(VPos.BASELINE);
            title.setTextAlignment(TextAlignment.CENTER);
            title.setText(getSkinnable().getTitle());
            title.setX((width - title.getLayoutBounds().getWidth()) * 0.5);
            title.setY(pane.getLayoutBounds().getMinY() + title.getLayoutBounds().getHeight() - 0.04 * height + 4);

            // Info Text
            lowerRightText.setFont(smallFont);
            lowerRightText.setTextOrigin(VPos.BASELINE);
            lowerRightText.setTextAlignment(TextAlignment.RIGHT);
            lowerRightText.setText(getSkinnable().getSubTitle());
            lowerRightText.setX(pane.getLayoutBounds().getMinX() + (pane.getLayoutBounds().getWidth() - lowerRightText.getLayoutBounds().getWidth()) * 0.5);
            lowerRightText.setY(pane.getLayoutBounds().getMinY() + height - 4 - 0.0416666667 * height);

            // Min measured value
            upperLeftText.setFont(smallFont);
            upperLeftText.setTextOrigin(VPos.BASELINE);
            upperLeftText.setTextAlignment(TextAlignment.RIGHT);
            upperLeftText.setX(pane.getLayoutBounds().getMinX() + 0.0416666667 * height);
            upperLeftText.setY(pane.getLayoutBounds().getMinY() + upperLeftText.getLayoutBounds().getHeight() - 0.04 * height + 4);

            // Max measured value
            upperRightText.setFont(smallFont);
            upperRightText.setTextOrigin(VPos.BASELINE);
            upperRightText.setTextAlignment(TextAlignment.RIGHT);
            upperRightText.setY(pane.getLayoutBounds().getMinY() + upperRightText.getLayoutBounds().getHeight() - 0.04 * height + 4);

            // Former value
            lowerCenterText.setFont(smallFont);
            lowerCenterText.setTextOrigin(VPos.BASELINE);
            lowerCenterText.setTextAlignment(TextAlignment.CENTER);
            lowerCenterText.setX((width - lowerCenterText.getLayoutBounds().getWidth()) * 0.5);
            lowerCenterText.setY(pane.getLayoutBounds().getMinY() + height - 4 - 0.0416666667 * height);
        }
    }

    private void redraw() {
        locale            = getSkinnable().getLocale();
        valueFormatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        otherFormatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getTickLabelDecimals())).append("f").toString();

        threshold.setVisible(Double.compare(getSkinnable().getCurrentValue(), getSkinnable().getThreshold()) >= 0);

        if (isNoOfDigitsInvalid()) {
            valueText.setText("-E-");
        } else {
            valueText.setText(String.format(locale, valueFormatString, getSkinnable().getCurrentValue()));
        }

        updateBackgroundText();

        // Visualize the lcd semitransparent background text
        if (getSkinnable().getUnit().isEmpty()) {
            backgroundText.setX((width - backgroundText.getLayoutBounds().getWidth()) - valueOffsetRight);
        } else {
            backgroundText.setX(width - 2 - backgroundText.getLayoutBounds().getWidth() - valueOffsetRight);
        }
        backgroundText.setY(height - (backgroundText.getLayoutBounds().getHeight() * digitalFontSizeFactor) * 0.5);

        if (getSkinnable().getUnit().isEmpty()) {
            valueText.setX((width - valueText.getLayoutBounds().getWidth()) - valueOffsetRight);
        } else {
            valueText.setX((width - 2 - valueText.getLayoutBounds().getWidth()) - valueOffsetRight);
        }

        // Update the title
        title.setText(getSkinnable().getTitle());
        title.setX((width - title.getLayoutBounds().getWidth()) * 0.5);

        // Update the upper left text
        upperLeftText.setText(String.format(locale, otherFormatString, getSkinnable().getMinMeasuredValue()));
        if (upperLeftText.getX() + upperLeftText.getLayoutBounds().getWidth() > title.getX()) {
            upperLeftText.setText("...");
        }

        // Update the upper right text
        upperRightText.setText(String.format(locale, otherFormatString, getSkinnable().getMaxMeasuredValue()));
        upperRightText.setX(width - upperRightText.getLayoutBounds().getWidth() - 0.0416666667 * height);
        if (upperRightText.getX() < title.getX() + title.getLayoutBounds().getWidth()) {
            upperRightText.setText("...");
            upperRightText.setX(width - upperRightText.getLayoutBounds().getWidth() - 0.0416666667 * height);
        }

        // Update the lower center text
        lowerCenterText.setText(String.format(locale, otherFormatString, getSkinnable().getOldValue()));
        lowerCenterText.setX((width - lowerCenterText.getLayoutBounds().getWidth()) * 0.5);

        // Update the lower right text
        lowerRightText.setText(getSkinnable().getSubTitle());
        lowerRightText.setX(width - lowerRightText.getLayoutBounds().getWidth() - 0.0416666667 * height);
        lowerRightText.setY(pane.getLayoutBounds().getMinY() + height - 3 - 0.0416666667 * height);
        if (lowerRightText.getX() < lowerCenterText.getX() + lowerCenterText.getLayoutBounds().getWidth()) {
            lowerRightText.setText("...");
            lowerRightText.setX(width - lowerRightText.getLayoutBounds().getWidth() - 0.0416666667 * height);
        }
    }
}
