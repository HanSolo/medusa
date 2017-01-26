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
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 16.01.16.
 */
public class IndicatorSkin extends GaugeSkinBase {
    protected static final double PREFERRED_WIDTH  = 250;
    protected static final double PREFERRED_HEIGHT = 165;
    protected static final double MINIMUM_WIDTH    = 50;
    protected static final double MINIMUM_HEIGHT   = 50;
    protected static final double MAXIMUM_WIDTH    = 1024;
    protected static final double MAXIMUM_HEIGHT   = 1024;
    private static final double   ASPECT_RATIO     = 0.59375;
    private double                width;
    private double                height;
    private double                oldValue;
    private Arc                   barBackground;
    private Pane                  sectionLayer;
    private Arc                   bar;
    private Path                  needle;
    private MoveTo                needleMoveTo1;
    private CubicCurveTo          needleCubicCurveTo2;
    private CubicCurveTo          needleCubicCurveTo3;
    private CubicCurveTo          needleCubicCurveTo4;
    private CubicCurveTo          needleCubicCurveTo5;
    private CubicCurveTo          needleCubicCurveTo6;
    private CubicCurveTo          needleCubicCurveTo7;
    private ClosePath             needleClosePath8;
    private Rotate                needleRotate;
    private Text                  minValueText;
    private Text                  maxValueText;
    private Text                  titleText;
    private Pane                  pane;
    private double                angleRange;
    private double                minValue;
    private double                range;
    private double                angleStep;
    private double                startAngle;
    private boolean               colorGradientEnabled;
    private int                   noOfGradientStops;
    private boolean               sectionsAlwaysVisible;
    private boolean               sectionsVisible;
    private List<Section>         sections;
    private Tooltip               needleTooltip;
    private String                formatString;
    private Locale                locale;
    private Color                 barColor;
    private Tooltip               barTooltip;
    private InvalidationListener  currentValueListener;
    private InvalidationListener  sectionAlwaysVisibleListener;


    // ******************** Constructors **************************************
    public IndicatorSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleRange                   = Helper.clamp(90.0, 180.0, gauge.getAngleRange());
        startAngle                   = getStartAngle();
        oldValue                     = gauge.getValue();
        minValue                     = gauge.getMinValue();
        range                        = gauge.getRange();
        angleStep                    = angleRange / range;
        colorGradientEnabled         = gauge.isGradientBarEnabled();
        noOfGradientStops            = gauge.getGradientBarStops().size();
        sectionsAlwaysVisible        = gauge.getSectionsAlwaysVisible();
        sectionsVisible              = gauge.getSectionsVisible();
        sections                     = gauge.getSections();
        formatString                 = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale                       = gauge.getLocale();
        barColor                     = gauge.getBarColor();
        currentValueListener         = o -> rotateNeedle(gauge.getCurrentValue());
        sectionAlwaysVisibleListener = o -> bar.setVisible(!gauge.getSectionsAlwaysVisible());

        initGraphics();
        registerListeners();

        rotateNeedle(gauge.getCurrentValue());
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

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, -angleRange);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(gauge.getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        sectionLayer = new Pane();
        sectionLayer.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, angleRange * 0.5 + 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(gauge.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);
        //bar.setMouseTransparent(sectionsAlwaysVisible ? true : false);
        bar.setVisible(!sectionsAlwaysVisible);

        needleRotate = new Rotate((gauge.getValue() - oldValue - minValue) * angleStep);

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
        needle.setFill(gauge.getNeedleColor());
        needle.setPickOnBounds(false);
        needle.setStrokeType(StrokeType.INSIDE);
        needle.setStrokeWidth(1);
        needle.setStroke(gauge.getBackgroundPaint());

        needleTooltip = new Tooltip(String.format(locale, formatString, gauge.getValue()));
        needleTooltip.setTextAlignment(TextAlignment.CENTER);
        Tooltip.install(needle, needleTooltip);

        minValueText = new Text(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMinValue()));
        minValueText.setFill(gauge.getTitleColor());
        Helper.enableNode(minValueText, gauge.getTickLabelsVisible());

        maxValueText = new Text(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMaxValue()));
        maxValueText.setFill(gauge.getTitleColor());
        Helper.enableNode(maxValueText, gauge.getTickLabelsVisible());

        titleText = new Text(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        if (!sections.isEmpty() && sectionsVisible && !sectionsAlwaysVisible) {
            barTooltip = new Tooltip();
            barTooltip.setTextAlignment(TextAlignment.CENTER);
            Tooltip.install(bar, barTooltip);
        }

        pane = new Pane(barBackground, sectionLayer, bar, needle, minValueText, maxValueText, titleText);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
        gauge.sectionsAlwaysVisibleProperty().addListener(sectionAlwaysVisibleListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            angleRange = Helper.clamp(90.0, 180.0, gauge.getAngleRange());
            startAngle = getStartAngle();
            minValue   = gauge.getMinValue();
            range      = gauge.getRange();
            sections   = gauge.getSections();
            angleStep  = angleRange / range;
            redraw();
            rotateNeedle(gauge.getCurrentValue());
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            needleTooltip.setText(String.format(locale, formatString, gauge.getValue()));
            double value = gauge.getValue();
            if (gauge.isValueVisible()) {
                Bounds bounds       = pane.localToScreen(pane.getBoundsInLocal());                
                double xFactor      = value > gauge.getRange() * 0.8 ? 0.0 : 0.25;
                double tooltipAngle = value * angleStep;
                double sinValue     = Math.sin(Math.toRadians(180 + angleRange * 0.5 - tooltipAngle));
                double cosValue     = Math.cos(Math.toRadians(180 + angleRange * 0.5 - tooltipAngle));
                double needleTipX   = bounds.getMinX() + bounds.getWidth() * 0.5 + bounds.getHeight() * sinValue;
                double needleTipY   = bounds.getMinY() + bounds.getHeight() * 0.72 + bounds.getHeight() * cosValue;
                needleTooltip.show(needle, needleTipX, needleTipY);
                needleTooltip.setAnchorX(needleTooltip.getX() - needleTooltip.getWidth() * xFactor);
            }
            if (sections.isEmpty() || sectionsAlwaysVisible) return;
            for (Section section : sections) {
                if (section.contains(value)) {
                    barTooltip.setText(section.getText());
                    break;
                }
            }
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
        }
    }

    private double getStartAngle() {
        ScaleDirection scaleDirection = gauge.getScaleDirection();
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
        bar.setLength(-(gauge.getCurrentValue() - minValue) * angleStep);
        setBarColor(VALUE);
    }
    
    private void setBarColor(final double VALUE) {
        if (!sectionsVisible && !colorGradientEnabled) {
            bar.setStroke(barColor);
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            bar.setStroke(gauge.getGradientLookup().getColorAt((VALUE - minValue) / range));
        } else {
            bar.setStroke(barColor);
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    break;
                }
            }
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        gauge.sectionsAlwaysVisibleProperty().removeListener(sectionAlwaysVisibleListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    private void resizeStaticText() {
        double maxWidth = width * 0.28472222;
        double fontSize = height * 0.12631579;

        minValueText.setFont(Fonts.latoRegular(fontSize));
        if (minValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minValueText, maxWidth, fontSize); }
        minValueText.relocate((width * 0.28472222) - minValueText.getLayoutBounds().getWidth(), height * 0.885);

        maxValueText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        maxValueText.relocate(width * 0.71527778, height * 0.885);

        maxWidth = width * 0.9;
        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((width - titleText.getLayoutBounds().getWidth()) * 0.5, height * 0.95);
    }

    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();

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
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);

            barBackground.setCenterX(centerX);
            barBackground.setCenterY(centerY);
            barBackground.setRadiusX(barRadius);
            barBackground.setRadiusY(barRadius);
            barBackground.setStrokeWidth(barWidth);
            barBackground.setStartAngle(angleRange * 0.5 + 90);
            barBackground.setLength(-angleRange);

            if (sectionsVisible && sectionsAlwaysVisible) {
                sectionLayer.setPrefSize(width, height);
                drawSections();
            }

            bar.setCenterX(centerX);
            bar.setCenterY(centerY);
            bar.setRadiusX(barRadius);
            bar.setRadiusY(barRadius);
            bar.setStrokeWidth(barWidth);
            bar.setStartAngle(angleRange * 0.5 + 90);
            bar.setLength(-(gauge.getCurrentValue() - minValue) * angleStep);

            double needleWidth  = height * 0.13157895;
            double needleHeight = height * 0.91315789;

            needle.setCache(true);

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

            needle.setCache(true);
            needle.setCacheHint(CacheHint.ROTATE);

            needle.relocate((width - needle.getLayoutBounds().getWidth()) * 0.5, centerY - needle.getLayoutBounds().getHeight() + needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight() - needle.getLayoutBounds().getWidth() * 0.5);

            resizeStaticText();
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(gauge.getBorderWidth() / PREFERRED_HEIGHT * height))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));

        barColor             = gauge.getBarColor();

        locale               = gauge.getLocale();
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();

        minValueText.setText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMinValue()));
        maxValueText.setText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMaxValue()));
        resizeStaticText();

        barBackground.setStroke(gauge.getBarBackgroundColor());
        bar.setStroke(gauge.getBarColor());
        needle.setFill(gauge.getNeedleColor());

        minValueText.setVisible(gauge.getTickLabelsVisible());
        maxValueText.setVisible(gauge.getTickLabelsVisible());

        minValueText.setFill(gauge.getTitleColor());
        maxValueText.setFill(gauge.getTitleColor());
        titleText.setFill(gauge.getTitleColor());
    }
    
    private void drawSections() {
        if (sections.isEmpty()) return;
        sectionLayer.getChildren().clear();

        double    centerX     = width * 0.5;
        double    centerY     = height * 0.85;
        double    barRadius   = height * 0.54210526;
        double    barWidth    = width * 0.28472222;
        List<Arc> sectionBars = new ArrayList<>(sections.size());
        for (Section section : sections) {
            Arc sectionBar = new Arc(centerX, centerY, barRadius, barRadius, angleRange * 0.5 + 90 - (section.getStart() * angleStep), -((section.getStop() - section.getStart()) - minValue) * angleStep);
            sectionBar.setType(ArcType.OPEN);
            sectionBar.setStroke(section.getColor());
            sectionBar.setStrokeWidth(barWidth);
            sectionBar.setStrokeLineCap(StrokeLineCap.BUTT);
            sectionBar.setFill(null);
            Tooltip sectionTooltip = new Tooltip(new StringBuilder(section.getText()).append("\n").append(String.format(Locale.US, "%.2f", section.getStart())).append(" - ").append(String.format(Locale.US, "%.2f", section.getStop())).toString());
            sectionTooltip.setTextAlignment(TextAlignment.CENTER);
            Tooltip.install(sectionBar, sectionTooltip);
            sectionBars.add(sectionBar);
        }
        sectionLayer.getChildren().addAll(sectionBars);
    }
}
