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
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 16.01.16.
 */
public class IndicatorSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 165;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ASPECT_RATIO     = 0.59375;
    private double       width;
    private double       height;
    private double       oldValue;
    private Arc          barBackground;
    private Arc          bar;
    private Path         needle;
    private MoveTo       needleMoveTo1;
    private CubicCurveTo needleCubicCurveTo2;
    private CubicCurveTo needleCubicCurveTo3;
    private CubicCurveTo needleCubicCurveTo4;
    private CubicCurveTo needleCubicCurveTo5;
    private CubicCurveTo  needleCubicCurveTo6;
    private CubicCurveTo  needleCubicCurveTo7;
    private ClosePath     needleClosePath8;
    private Rotate        needleRotate;
    private Text          minValueText;
    private Text          maxValueText;
    private Pane          pane;
    private double        angleRange;
    private double        minValue;
    private double        range;
    private double        angleStep;
    private double        startAngle;
    private boolean       colorGradientEnabled;
    private int           noOfGradientStops;
    private boolean       sectionsVisible;
    private List<Section> sections;
    private Tooltip       needleTooltip;
    private String        formatString;


    // ******************** Constructors **************************************
    public IndicatorSkin(Gauge gauge) {
        super(gauge);
        angleRange           = Helper.clamp(90d, 180d, gauge.getAngleRange());
        startAngle           = getStartAngle();
        oldValue             = gauge.getValue();
        minValue             = gauge.getMinValue();
        range                = gauge.getRange();
        angleStep            = angleRange / range;
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        formatString = String.join("", "%.", Integer.toString(gauge.getDecimals()), "f");

        init();
        initGraphics();
        registerListeners();

        rotateNeedle(gauge.getCurrentValue());
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
        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, -angleRange);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(getSkinnable().getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(getSkinnable().getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        needleRotate = new Rotate((getSkinnable().getValue() - oldValue - minValue) * angleStep);

        needleMoveTo1       = new MoveTo();
        needleCubicCurveTo2 = new CubicCurveTo();
        needleCubicCurveTo3 = new CubicCurveTo();
        needleCubicCurveTo4 = new CubicCurveTo();
        needleCubicCurveTo5 = new CubicCurveTo();
        needleCubicCurveTo6 = new CubicCurveTo();
        needleCubicCurveTo7 = new CubicCurveTo();
        needleClosePath8    = new ClosePath();
        needle = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4, needleCubicCurveTo5, needleCubicCurveTo6, needleCubicCurveTo7, needleClosePath8);
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setFill(getSkinnable().getNeedleColor());
        needle.setStrokeType(StrokeType.INSIDE);
        needle.setStrokeWidth(1);
        needle.setStroke(getSkinnable().getBackgroundPaint());

        needleTooltip = new Tooltip(String.format(Locale.US, formatString, getSkinnable().getValue()));
        needleTooltip.setTextAlignment(TextAlignment.CENTER);
        Tooltip.install(needle, needleTooltip);

        minValueText = new Text(String.format(Locale.US, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMinValue()));
        minValueText.setFill(getSkinnable().getTitleColor());
        minValueText.setVisible(getSkinnable().getTickLabelsVisible());

        maxValueText = new Text(String.format(Locale.US, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMaxValue()));
        maxValueText.setFill(getSkinnable().getTitleColor());
        maxValueText.setVisible(getSkinnable().getTickLabelsVisible());

        pane = new Pane(barBackground, bar, needle, minValueText, maxValueText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> rotateNeedle(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            angleRange = Helper.clamp(90d, 180d, getSkinnable().getAngleRange());
            startAngle = getStartAngle();
            minValue   = getSkinnable().getMinValue();
            range      = getSkinnable().getRange();
            angleStep  = angleRange / range;
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            needleTooltip.setText(String.format(Locale.US, formatString, getSkinnable().getValue()));
            if (getSkinnable().isValueVisible()) {
                Bounds bounds        = pane.localToScreen(pane.getBoundsInLocal());
                double value         = getSkinnable().getValue();
                double xFactor       = value > getSkinnable().getRange() * 0.8 ? 0.0 : 0.25;
                double tooltipAngle  = value * angleStep;
                double sinValue      = Math.sin(Math.toRadians(180 + angleRange * 0.5 - tooltipAngle));
                double cosValue      = Math.cos(Math.toRadians(180 + angleRange * 0.5 - tooltipAngle));
                double needleTipX    = bounds.getMinX() + width * 0.5 + height * sinValue;
                double needleTipY    = bounds.getMinY() + height * 0.72 + height * cosValue;
                needleTooltip.show(needle, needleTipX, needleTipY);
                needleTooltip.setAnchorX(needleTooltip.getX() - needleTooltip.getWidth() * xFactor);
            }
        }
    }

    private double getStartAngle() {
        ScaleDirection scaleDirection = getSkinnable().getScaleDirection();
        switch(scaleDirection) {
            //case COUNTER_CLOCKWISE: return 180 - angleRange * 0.5;
            case CLOCKWISE        :
            default               : return 180 + angleRange * 0.5;
        }
    }

    private void rotateNeedle(final double VALUE) {
        double needleStartAngle = angleRange * 0.5;
        double targetAngle      = (VALUE - minValue) * angleStep - needleStartAngle;
        targetAngle = Helper.clamp(-needleStartAngle, -needleStartAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);
        bar.setLength(-(getSkinnable().getCurrentValue() - minValue) * angleStep);
        setBarColor(VALUE);
    }
    private void setBarColor(final double VALUE) {
        if (!sectionsVisible && !colorGradientEnabled) {
            bar.setStroke(getSkinnable().getBarColor());
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            bar.setStroke(getSkinnable().getGradientLookup().getColorAt((VALUE - minValue) / range));
        } else {
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }
    }


    // ******************** Resizing ******************************************
    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        formatString         = String.join("", "%.", Integer.toString(getSkinnable().getDecimals()), "f");
        colorGradientEnabled = getSkinnable().isGradientBarEnabled();
        noOfGradientStops    = getSkinnable().getGradientBarStops().size();
        sectionsVisible      = getSkinnable().getSectionsVisible();
        sections             = getSkinnable().getSections();

        minValueText.setText(String.format(Locale.US, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMinValue()));
        maxValueText.setText(String.format(Locale.US, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMaxValue()));
        resizeStaticText();

        barBackground.setStroke(getSkinnable().getBarBackgroundColor());
        bar.setStroke(getSkinnable().getBarColor());
        needle.setFill(getSkinnable().getNeedleColor());

        minValueText.setVisible(getSkinnable().getTickLabelsVisible());
        maxValueText.setVisible(getSkinnable().getTickLabelsVisible());

        minValueText.setFill(getSkinnable().getTitleColor());
        maxValueText.setFill(getSkinnable().getTitleColor());
    }

    private void resizeStaticText() {
        double maxWidth = width * 0.28472222;
        double fontSize = height * 0.12631579;

        minValueText.setFont(Fonts.latoRegular(fontSize));
        if (minValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minValueText, maxWidth, fontSize); }
        minValueText.relocate((width * 0.28472222) - minValueText.getLayoutBounds().getWidth(), height * 0.885);

        maxValueText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        maxValueText.relocate(width * 0.71527778, height * 0.885);
    }

    private void resize() {
        width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();

        if (ASPECT_RATIO * width > height) {
            width = 1 / (ASPECT_RATIO / height);
        } else if (1 / (ASPECT_RATIO / height) > width) {
            height = ASPECT_RATIO * width;
        }

        if (width > 0 && height > 0) {
            double centerX   = width * 0.5;
            double centerY   = height * 0.85;
            double barRadius = height * 0.54210526;
            double barWidth  = width * 0.28472222;

            pane.setMaxSize(width, height);
            pane.relocate((getSkinnable().getWidth() - width) * 0.5, (getSkinnable().getHeight() - height) * 0.5);

            barBackground.setCenterX(centerX);
            barBackground.setCenterY(centerY);
            barBackground.setRadiusX(barRadius);
            barBackground.setRadiusY(barRadius);
            barBackground.setStrokeWidth(barWidth);
            barBackground.setStartAngle(angleRange * 0.5 + 90);
            barBackground.setLength(-angleRange);

            bar.setCenterX(centerX);
            bar.setCenterY(centerY);
            bar.setRadiusX(barRadius);
            bar.setRadiusY(barRadius);
            bar.setStrokeWidth(barWidth);
            bar.setStartAngle(angleRange * 0.5 + 90);
            bar.setLength(-(getSkinnable().getCurrentValue() - minValue) * angleStep);

            double needleWidth  = height * 0.13157895;
            double needleHeight = height * 0.91315789;

            needleMoveTo1.setX(0.0); needleMoveTo1.setY(0.927953890489914 * needleHeight);

            needleCubicCurveTo2.setControlX1(0); needleCubicCurveTo2.setControlY1(0.968299711815562 * needleHeight);
            needleCubicCurveTo2.setControlX2(0.22 * needleWidth); needleCubicCurveTo2.setControlY2(needleHeight);
            needleCubicCurveTo2.setX(0.5 * needleWidth); needleCubicCurveTo2.setY(needleHeight);

            needleCubicCurveTo3.setControlX1(0.78 * needleWidth); needleCubicCurveTo3.setControlY1(needleHeight);
            needleCubicCurveTo3.setControlX2(needleWidth); needleCubicCurveTo3.setControlY2(0.968299711815562 * needleHeight);
            needleCubicCurveTo3.setX(needleWidth); needleCubicCurveTo3.setY(0.927953890489914 * needleHeight);

            needleCubicCurveTo4.setControlX1(needleWidth); needleCubicCurveTo4.setControlY1(0.92507204610951 * needleHeight);
            needleCubicCurveTo4.setControlX2(0.6 * needleWidth); needleCubicCurveTo4.setControlY2(0.0144092219020173 * needleHeight);
            needleCubicCurveTo4.setX(0.6 * needleWidth); needleCubicCurveTo4.setY(0.0144092219020173 * needleHeight);

            needleCubicCurveTo5.setControlX1(0.6 * needleWidth); needleCubicCurveTo5.setControlY1(0.0144092219020173 * needleHeight);
            needleCubicCurveTo5.setControlX2(0.58 * needleWidth); needleCubicCurveTo5.setControlY2(0);
            needleCubicCurveTo5.setX(0.5 * needleWidth); needleCubicCurveTo5.setY(0);

            needleCubicCurveTo6.setControlX1(0.42 * needleWidth); needleCubicCurveTo6.setControlY1(0);
            needleCubicCurveTo6.setControlX2(0.4 * needleWidth); needleCubicCurveTo6.setControlY2(0.0144092219020173 * needleHeight);
            needleCubicCurveTo6.setX(0.4 * needleWidth); needleCubicCurveTo6.setY(0.0144092219020173 * needleHeight);

            needleCubicCurveTo7.setControlX1(0.4 * needleWidth); needleCubicCurveTo7.setControlY1(0.0144092219020173 * needleHeight);
            needleCubicCurveTo7.setControlX2(0); needleCubicCurveTo7.setControlY2(0.92507204610951 * needleHeight);
            needleCubicCurveTo7.setX(0); needleCubicCurveTo7.setY(0.927953890489914 * needleHeight);

            needle.relocate((width - needle.getLayoutBounds().getWidth()) * 0.5, centerY - needle.getLayoutBounds().getHeight() + needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight() - needle.getLayoutBounds().getWidth() * 0.5);

            resizeStaticText();
        }
    }
}
