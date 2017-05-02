/*
 * Copyright (c) 2015 by Gerrit Grunwald
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
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.effect.BlurType;
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
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 28.12.15.
 */
public class DashboardSkin extends GaugeSkinBase {
    protected static final double PREFERRED_WIDTH  = 200;
    protected static final double PREFERRED_HEIGHT = 148;
    protected static final double MINIMUM_WIDTH    = 50;
    protected static final double MINIMUM_HEIGHT   = 50;
    protected static final double MAXIMUM_WIDTH    = 1024;
    protected static final double MAXIMUM_HEIGHT   = 1024;
    private static final double   ASPECT_RATIO     = 0.74;
    private static final double   ANGLE_RANGE      = 180;
    private double                size;
    private double                width;
    private double                height;
    private double                centerX;
    private double                currentValueAngle;
    private Pane                  pane;
    private Text                  unitText;
    private Text                  titleText;
    private Text                  valueText;
    private Text                  minText;
    private Text                  maxText;
    private Path                  barBackground;
    private MoveTo                barBackgroundStart;
    private ArcTo                 barBackgroundOuterArc;
    private LineTo                barBackgroundLineToInnerArc;
    private ArcTo                 barBackgroundInnerArc;
    private Path                  dataBar;
    private MoveTo                dataBarStart;
    private ArcTo                 dataBarOuterArc;
    private LineTo                dataBarLineToInnerArc;
    private ArcTo                 dataBarInnerArc;
    private Line                  threshold;
    private Text                  thresholdText;
    private InnerShadow           innerShadow;
    private Font                  smallFont;
    private Font                  bigFont;
    private double                range;
    private double                angleStep;
    private boolean               colorGradientEnabled;
    private int                   noOfGradientStops;
    private boolean               sectionsVisible;
    private List<Section>         sections;
    private String                formatString;
    private String                otherFormatString;
    private Locale                locale;
    private double                minValue;
    private InvalidationListener  currentValueListener;


    // ******************** Constructors **************************************
    public DashboardSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        currentValueAngle    = 0;
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        otherFormatString    = new StringBuilder("%.").append(Integer.toString(gauge.getTickLabelDecimals())).append("f").toString();
        locale               = gauge.getLocale();
        currentValueListener = o -> setBar(gauge.getCurrentValue());

        initGraphics();
        registerListeners();

        setBar(gauge.getValue());
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        // Set initial size
        if (Double.compare(gauge.getPrefWidth(), 0.0) <= 0 || Double.compare(gauge.getPrefHeight(), 0.0) <= 0 ||
            Double.compare(gauge.getWidth(), 0.0) <= 0 || Double.compare(gauge.getHeight(), 0.0) <= 0) {
            if (gauge.getPrefWidth() > 0 && gauge.getPrefHeight() > 0) {
                gauge.setPrefSize(gauge.getPrefWidth(), gauge.getPrefHeight());
            } else {
                gauge.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        unitText = new Text(gauge.getUnit());
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setFill(gauge.getUnitColor());
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        titleText = new Text(gauge.getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

        minValue = gauge.getMinValue();
        minText  = new Text(String.format(locale, otherFormatString, minValue));
        minText.setTextOrigin(VPos.CENTER);
        minText.setFill(gauge.getValueColor());

        maxText = new Text(String.format(locale, otherFormatString, gauge.getMaxValue()));
        maxText.setTextOrigin(VPos.CENTER);
        maxText.setFill(gauge.getValueColor());

        boolean tickLabelsVisible = gauge.getTickLabelsVisible();
        Helper.enableNode(minText, tickLabelsVisible);
        Helper.enableNode(maxText, tickLabelsVisible);

        innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.3), 30.0, 0.0, 0.0, 10.0);

        barBackgroundStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        barBackgroundOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, true, true);
        barBackgroundLineToInnerArc = new LineTo(0.72222 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        barBackgroundInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT, false, false);

        barBackground = new Path();
        barBackground.setFillRule(FillRule.EVEN_ODD);
        barBackground.getElements().add(barBackgroundStart);
        barBackground.getElements().add(barBackgroundOuterArc);
        barBackground.getElements().add(barBackgroundLineToInnerArc);
        barBackground.getElements().add(barBackgroundInnerArc);
        barBackground.getElements().add(new ClosePath());
        barBackground.setFill(gauge.getBarBackgroundColor());
        barBackground.setStroke(gauge.getBorderPaint());
        barBackground.setEffect(gauge.isShadowsEnabled() ? innerShadow : null);

        dataBarStart          = new MoveTo(0, 0.675 * PREFERRED_HEIGHT);
        dataBarOuterArc       = new ArcTo(0.675 * PREFERRED_HEIGHT, 0.675 * PREFERRED_HEIGHT, 0, 0, 0, false, true);
        dataBarLineToInnerArc = new LineTo(0.27778 * PREFERRED_WIDTH, 0.675 * PREFERRED_HEIGHT);
        dataBarInnerArc       = new ArcTo(0.3 * PREFERRED_HEIGHT, 0.3 * PREFERRED_HEIGHT, 0, 0, 0, false, false);

        dataBar = new Path();
        dataBar.setFillRule(FillRule.EVEN_ODD);
        dataBar.getElements().add(dataBarStart);
        dataBar.getElements().add(dataBarOuterArc);
        dataBar.getElements().add(dataBarLineToInnerArc);
        dataBar.getElements().add(dataBarInnerArc);
        dataBar.getElements().add(new ClosePath());
        dataBar.setFill(gauge.getBarColor());
        dataBar.setStroke(gauge.getBorderPaint());
        dataBar.setEffect(gauge.isShadowsEnabled() ? innerShadow : null);

        threshold = new Line();
        threshold.setStrokeLineCap(StrokeLineCap.BUTT);
        Helper.enableNode(threshold, gauge.isThresholdVisible());

        thresholdText = new Text(String.format(locale, formatString, gauge.getThreshold()));
        Helper.enableNode(thresholdText, gauge.isThresholdVisible());

        pane = new Pane(unitText, titleText, valueText, minText, maxText, barBackground, dataBar, threshold, thresholdText);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            range     = gauge.getRange();
            angleStep = ANGLE_RANGE / range;
            minValue  = gauge.getMinValue();
            sections  = gauge.getSections();
            resize();
            redraw();
            setBar(gauge.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
            boolean tickLabelsVisible = gauge.getTickLabelsVisible();
            Helper.enableNode(minText, tickLabelsVisible);
            Helper.enableNode(maxText, tickLabelsVisible);
            boolean thresholdVisible = gauge.isThresholdVisible();
            Helper.enableNode(threshold, thresholdVisible);
            Helper.enableNode(thresholdText, thresholdVisible);
            resize();
            redraw();
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void setBar( final double VALUE ) {

        currentValueAngle = Helper.clamp(90.0, 270.0, ( VALUE - minValue ) * angleStep + 90.0);

        double smallHeight     = 0.675 * height;
        double tinyHeight      = 0.3 * height;
        double currentValueSin = Math.sin(-Math.toRadians(currentValueAngle));
        double currentValueCos = Math.cos(-Math.toRadians(currentValueAngle));

        dataBarOuterArc.setX(centerX + smallHeight * currentValueSin);
        dataBarOuterArc.setY(centerX + smallHeight * currentValueCos);
        dataBarLineToInnerArc.setX(centerX + tinyHeight * currentValueSin);
        dataBarLineToInnerArc.setY(centerX + tinyHeight * currentValueCos);

        if (gauge.isStartFromZero()) {

            double min = gauge.getMinValue();
            double max = gauge.getMaxValue();

            if ( ( VALUE > min || min < 0 ) && ( VALUE < max || max > 0 ) ) {
                if ( max < 0 ) {
                    dataBarStart.setX(centerX + smallHeight);
                    dataBarStart.setY(smallHeight);
                    dataBarOuterArc.setSweepFlag(false);
                    dataBarInnerArc.setX(centerX + tinyHeight);
                    dataBarInnerArc.setY(smallHeight);
                    dataBarInnerArc.setSweepFlag(true);
                } else if ( min > 0 ) {
                    dataBarStart.setX(0);
                    dataBarStart.setY(smallHeight);
                    dataBarOuterArc.setSweepFlag(true);
                    dataBarInnerArc.setX(0.27778 * width);
                    dataBarInnerArc.setY(smallHeight);
                    dataBarInnerArc.setSweepFlag(false);
                } else {

                    double zeroAngle = Helper.clamp(90.0, 270.0, 90.0 - minValue * angleStep);
                    double zeroSin   = Math.sin(-Math.toRadians(zeroAngle));
                    double zeroCos   = Math.cos(-Math.toRadians(zeroAngle));

                    dataBarStart.setX(centerX + smallHeight * zeroSin);
                    dataBarStart.setY(centerX + smallHeight * zeroCos);
                    dataBarInnerArc.setX(centerX + tinyHeight * zeroSin);
                    dataBarInnerArc.setY(centerX + tinyHeight * zeroCos);

                    if ( VALUE < 0 ) {
                        dataBarOuterArc.setSweepFlag(false);
                        dataBarInnerArc.setSweepFlag(true);
                    } else {
                        dataBarOuterArc.setSweepFlag(true);
                        dataBarInnerArc.setSweepFlag(false);
                    }

                }
            }

        } else {
            dataBarStart.setX(0);
            dataBarStart.setY(smallHeight);
            dataBarOuterArc.setSweepFlag(true);
            dataBarInnerArc.setX(0.27778 * width);
            dataBarInnerArc.setY(smallHeight);
            dataBarInnerArc.setSweepFlag(false);
        }

        setBarColor(VALUE);

        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        if ( valueText.getLayoutBounds().getWidth() > 0.28 * width ) {
            Helper.adjustTextSize(valueText, 0.28 * width, size * 0.24);
        }
        valueText.relocate(( width - valueText.getLayoutBounds().getWidth() ) * 0.5, 0.615 * height + ( 0.3 * height - valueText.getLayoutBounds().getHeight() ) * 0.5);

    }

    private void setBarColor(final double VALUE) {
        if (!sectionsVisible && !colorGradientEnabled) {
            dataBar.setFill(gauge.getBarColor());
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            dataBar.setFill(gauge.getGradientLookup().getColorAt((VALUE - minValue) / range));
        } else {
            dataBar.setFill(gauge.getBarColor());
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    dataBar.setFill(section.getColor());
                    break;
                }
            }
        }
    }

    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size   = width < height ? width : height;

        if (ASPECT_RATIO * width > height) {
            width  = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

            centerX   = width * 0.5;
            smallFont = Fonts.robotoThin(0.12 * height);
            bigFont   = Fonts.robotoRegular(0.24 * height);

            unitText.setFont(smallFont);
            unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.5 * height);

            double maxWidth = 0.95 * width;
            titleText.setFont(smallFont);
            if (titleText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(titleText, maxWidth, size * 0.12);
            titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.88 * height);

            maxWidth = 0.28 * width;
            valueText.setFont(bigFont);
            if (valueText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(valueText, maxWidth, size * 0.24);
            valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.615 * height + (0.3 * height - valueText.getLayoutBounds().getHeight()) * 0.5);

            maxWidth = 0.27  * width;
            minText.setText(String.format(locale, otherFormatString, minValue));
            minText.setFont(smallFont);
            if (minText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(minText, maxWidth, size * 0.12);
            minText.relocate(((0.27778 * width) - minText.getLayoutBounds().getWidth()) * 0.5, 0.7 * height);

            maxText.setText(String.format(locale, otherFormatString, gauge.getMaxValue()));
            maxText.setFont(smallFont);
            if (maxText.getLayoutBounds().getWidth() > maxWidth) Helper.adjustTextSize(maxText, maxWidth, size * 0.12);
            maxText.relocate(((0.27778 * width) - maxText.getLayoutBounds().getWidth()) * 0.5 + 0.72222 * width, 0.7 * height);

            if (gauge.isShadowsEnabled()) {
                innerShadow.setRadius(0.075 * height);
                innerShadow.setOffsetY(0.025 * height);
            }

            barBackgroundStart.setX(0);
            barBackgroundStart.setY(0.675 * height);
            barBackgroundOuterArc.setRadiusX(0.675 * height);
            barBackgroundOuterArc.setRadiusY(0.675 * height);
            barBackgroundOuterArc.setX(width);
            barBackgroundOuterArc.setY(0.675 * height);
            barBackgroundLineToInnerArc.setX(0.72222 * width);
            barBackgroundLineToInnerArc.setY(0.675 * height);
            barBackgroundInnerArc.setRadiusX(0.3 * height);
            barBackgroundInnerArc.setRadiusY(0.3 * height);
            barBackgroundInnerArc.setX(0.27778 * width);
            barBackgroundInnerArc.setY(0.675 * height);

            currentValueAngle = Helper.clamp(90.0, 270.0, (gauge.getCurrentValue() - minValue) * angleStep + 90.0);
            dataBarStart.setX(0);
            dataBarStart.setY(0.675 * height);
            dataBarOuterArc.setRadiusX(0.675 * height);
            dataBarOuterArc.setRadiusY(0.675 * height);
            dataBarOuterArc.setX(centerX + (0.675 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarOuterArc.setY(centerX + (0.675 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setX(centerX + (0.3 * height) * Math.sin(-Math.toRadians(currentValueAngle)));
            dataBarLineToInnerArc.setY(centerX + (0.3 * height) * Math.cos(-Math.toRadians(currentValueAngle)));
            dataBarInnerArc.setRadiusX(0.3 * height);
            dataBarInnerArc.setRadiusY(0.3 * height);
            dataBarInnerArc.setX(0.27778 * width);
            dataBarInnerArc.setY(0.675 * height);

            threshold.setStroke(gauge.getThresholdColor());
            threshold.setStrokeWidth(Helper.clamp(1.0, 2.0, 0.00675676 * height));
            double thresholdInnerRadius = 0.3 * height;
            double thresholdOuterRadius = 0.675 * height;
            double thresholdAngle       = Helper.clamp(90.0, 270.0, (gauge.getThreshold() - minValue) * angleStep + 90.0);
            threshold.setStartX(centerX + thresholdInnerRadius * Math.sin(-Math.toRadians(thresholdAngle)));
            threshold.setStartY(centerX + thresholdInnerRadius * Math.cos(-Math.toRadians(thresholdAngle)));
            threshold.setEndX(centerX + thresholdOuterRadius * Math.sin(-Math.toRadians(thresholdAngle)));
            threshold.setEndY(centerX + thresholdOuterRadius * Math.cos(-Math.toRadians(thresholdAngle)));

            double thresholdTextRadius = 0.26 * height;
            thresholdText.setFill(gauge.getValueColor());
            thresholdText.setText(String.format(locale, formatString, gauge.getThreshold()));
            thresholdText.setFont(Fonts.robotoBold(size * 0.047));
            thresholdText.setRotate(thresholdAngle + 180);
            thresholdText.relocate(centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.sin(-Math.toRadians(thresholdAngle)),
                                   centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.cos(-Math.toRadians(thresholdAngle)));
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth() / 250 * size))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();

        barBackground.setFill(gauge.getBarBackgroundColor());
        barBackground.setEffect(gauge.isShadowsEnabled() ? innerShadow : null);

        setBarColor(gauge.getCurrentValue());

        dataBar.setEffect(gauge.isShadowsEnabled() ? innerShadow : null);

        threshold.setStroke(gauge.getThresholdColor());
        double thresholdInnerRadius = 0.3 * height;
        double thresholdOuterRadius = 0.675 * height;
        double thresholdAngle       = Helper.clamp(90.0, 270.0, (gauge.getThreshold() - minValue) * angleStep + 90.0);
        threshold.setStartX(centerX + thresholdInnerRadius * Math.sin(-Math.toRadians(thresholdAngle)));
        threshold.setStartY(centerX + thresholdInnerRadius * Math.cos(-Math.toRadians(thresholdAngle)));
        threshold.setEndX(centerX + thresholdOuterRadius * Math.sin(-Math.toRadians(thresholdAngle)));
        threshold.setEndY(centerX + thresholdOuterRadius * Math.cos(-Math.toRadians(thresholdAngle)));

        redrawText();
    }

    private void redrawText() {
        locale            = gauge.getLocale();
        formatString      = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        otherFormatString = new StringBuilder("%.").append(Integer.toString(gauge.getTickLabelDecimals())).append("f").toString();

        titleText.setFill(gauge.getTitleColor());
        titleText.setText(gauge.getTitle());
        titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, 0.88 * height);

        valueText.setFill(gauge.getValueColor());
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()));
        valueText.relocate((width - valueText.getLayoutBounds().getWidth()) * 0.5, 0.615 * height + (0.3 * height - valueText.getLayoutBounds().getHeight()) * 0.5);

        minText.setFill(gauge.getValueColor());
        minText.setText(String.format(locale, otherFormatString, gauge.getMinValue()));
        minText.relocate(((0.27778 * width) - minText.getLayoutBounds().getWidth()) * 0.5, 0.7 * height);

        maxText.setFill(gauge.getValueColor());
        maxText.setText(String.format(locale, otherFormatString, gauge.getMaxValue()));
        maxText.relocate(((0.27778 * width) - maxText.getLayoutBounds().getWidth()) * 0.5 + 0.72222 * width, 0.7 * height);

        unitText.setFill(gauge.getUnitColor());
        unitText.setText(gauge.getUnit());
        unitText.relocate((width - unitText.getLayoutBounds().getWidth()) * 0.5, 0.5 * height);

        double thresholdAngle      = Helper.clamp(90.0, 270.0, (gauge.getThreshold() - minValue) * angleStep + 90.0);
        double thresholdTextRadius = 0.26 * height;
        thresholdText.setFill(gauge.getValueColor());
        thresholdText.setText(String.format(locale, formatString, gauge.getThreshold()));
        thresholdText.setFont(Fonts.robotoBold(size * 0.047));
        thresholdText.setRotate(thresholdAngle + 180);
        thresholdText.relocate(centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.sin(-Math.toRadians(thresholdAngle)),
                               centerX - (thresholdText.getLayoutBounds().getWidth() * 0.5) + thresholdTextRadius * Math.cos(-Math.toRadians(thresholdAngle)));
    }
}
