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
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.Helper;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.hansolo.medusa.tools.Helper.enableNode;
import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 29.11.16.
 */
public class TileKpiSkin extends GaugeSkinBase {
    public  static final Color             GRAY = Color.rgb(139,144,146);
    private              double            size;
    private              double            oldValue;
    private              Arc               barBackground;
    private              Arc               thresholdBar;
    private              Rectangle         needleRect;
    private              Path              needle;
    private              Rotate            needleRotate;
    private              Rotate            needleRectRotate;
    private              Text              titleText;
    private              Text              valueText;
    private              Text              unitText;
    private              Text              minValueText;
    private              Text              maxValueText;
    private              Rectangle         thresholdRect;
    private              Text              thresholdText;
    private              Pane              sectionPane;
    private              Path              alertIcon;
    private              Tooltip           alertTooltip;
    private              Pane              pane;
    private              double            angleRange;
    private              double            minValue;
    private              double            threshold;
    private              Color             thresholdColor;
    private              double            range;
    private              double            angleStep;
    private              String            formatString;
    private              Locale            locale;
    private              boolean           sectionsVisible;
    private              boolean           highlightSections;
    private              List<Section>     sections;
    private              Map<Section, Arc> sectionMap;
    private              InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public TileKpiSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleRange           = Helper.clamp(90.0, 180.0, gauge.getAngleRange());
        oldValue             = gauge.getValue();
        minValue             = gauge.getMinValue();
        threshold            = gauge.getThreshold();
        thresholdColor       = gauge.getThresholdColor();
        range                = gauge.getRange();
        angleStep            = angleRange / range;
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale               = gauge.getLocale();
        sectionsVisible      = gauge.getSectionsVisible();
        highlightSections    = gauge.isHighlightSections();
        sections             = gauge.getSections();
        sectionMap           = new HashMap<>(sections.size());
        currentValueListener = o -> rotateNeedle(gauge.getCurrentValue());
        for(Section section : sections) { sectionMap.put(section, new Arc()); }

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
        barBackground.setStroke(gauge.getBarColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setFill(null);

        thresholdBar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.696, PREFERRED_WIDTH * 0.275, PREFERRED_WIDTH * 0.275, -angleRange * 0.5 + 90, 0);
        thresholdBar.setType(ArcType.OPEN);
        thresholdBar.setStroke(gauge.getThresholdColor());
        thresholdBar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        thresholdBar.setStrokeLineCap(StrokeLineCap.BUTT);
        thresholdBar.setFill(null);
        enableNode(thresholdBar, !gauge.getSectionsVisible());

        sectionPane = new Pane();
        enableNode(sectionPane, gauge.getSectionsVisible());

        if (sectionsVisible) { drawSections(); }

        alertIcon = new Path();
        alertIcon.setFillRule(FillRule.EVEN_ODD);
        alertIcon.setFill(Color.YELLOW);
        alertIcon.setStroke(null);
        enableNode(alertIcon, gauge.isAlert());
        alertTooltip = new Tooltip(gauge.getAlertMessage());
        Tooltip.install(alertIcon, alertTooltip);

        needleRotate     = new Rotate((gauge.getValue() - oldValue - minValue) * angleStep);
        needleRectRotate = new Rotate((gauge.getValue() - oldValue - minValue) * angleStep);

        needleRect = new Rectangle();
        needleRect.setFill(gauge.getBackgroundPaint());
        needleRect.getTransforms().setAll(needleRectRotate);

        needle = new Path();
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setFill(gauge.getNeedleColor());
        needle.setStrokeWidth(0);
        needle.setStroke(Color.TRANSPARENT);

        titleText = new Text(gauge.getTitle());
        titleText.setFill(gauge.getTitleColor());
        enableNode(titleText, !gauge.getTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, gauge.getCurrentValue()));
        valueText.setFill(gauge.getValueColor());
        enableNode(valueText, gauge.isValueVisible() && !gauge.isAlert());

        unitText = new Text(gauge.getUnit());
        unitText.setFill(gauge.getUnitColor());
        enableNode(unitText, gauge.isValueVisible() && !gauge.isAlert());

        minValueText = new Text(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMinValue()));
        minValueText.setFill(gauge.getTitleColor());

        maxValueText = new Text(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMaxValue()));
        maxValueText.setFill(gauge.getTitleColor());

        thresholdRect = new Rectangle();
        thresholdRect.setFill(sectionsVisible ? GRAY : gauge.getThresholdColor());
        enableNode(thresholdRect, gauge.isThresholdVisible());

        thresholdText = new Text(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getThreshold()));
        thresholdText.setFill(sectionsVisible ? Color.TRANSPARENT : gauge.getBackgroundPaint());
        enableNode(thresholdText, gauge.isThresholdVisible());

        pane = new Pane(barBackground, thresholdBar, sectionPane, alertIcon, needleRect, needle, titleText, valueText, unitText, minValueText, maxValueText, thresholdRect, thresholdText);
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(PREFERRED_WIDTH * 0.025), new BorderWidths(gauge.getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(PREFERRED_WIDTH * 0.025), Insets.EMPTY)));

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
            angleRange        = Helper.clamp(90.0, 180.0, gauge.getAngleRange());
            minValue          = gauge.getMinValue();
            threshold         = gauge.getThreshold();
            range             = gauge.getRange();
            angleStep         = angleRange / range;
            highlightSections = gauge.isHighlightSections();
            redraw();
            rotateNeedle(gauge.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            enableNode(titleText, !gauge.getTitle().isEmpty());
            enableNode(valueText, gauge.isValueVisible());
            enableNode(sectionPane, gauge.getSectionsVisible());
            enableNode(thresholdRect, gauge.isThresholdVisible());
            enableNode(thresholdText, gauge.isThresholdVisible());
            enableNode(unitText, !gauge.getUnit().isEmpty());
            sectionsVisible = gauge.getSectionsVisible();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = gauge.getSections();
            sectionMap.clear();
            for(Section section : sections) { sectionMap.put(section, new Arc()); }
        } else if ("ALERT".equals(EVENT_TYPE)) {
            enableNode(valueText, gauge.isValueVisible() && !gauge.isAlert());
            enableNode(unitText, gauge.isValueVisible() && !gauge.isAlert());
            enableNode(alertIcon, gauge.isAlert());
            alertTooltip.setText(gauge.getAlertMessage());
        }
    }

    private void rotateNeedle(final double VALUE) {
        double needleStartAngle = angleRange * 0.5;
        double targetAngle = (VALUE - minValue) * angleStep - needleStartAngle;
        targetAngle = Helper.clamp(-needleStartAngle, -needleStartAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);
        needleRectRotate.setAngle(targetAngle);
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        thresholdRect.setFill(VALUE > threshold ? thresholdColor : GRAY);
        resizeDynamicText();
        highlightSections(VALUE);
    }

    private void highlightSections(final double VALUE) {
        if (!sectionsVisible || sections.isEmpty()) return;
        if (highlightSections) {
            sections.forEach(section -> sectionMap.get(section).setVisible(section.contains(VALUE)));
        } else {
            sections.forEach(section -> sectionMap.get(section).setOpacity(section.contains(VALUE) ? 1.0 : 0.25));
        }
    }

    private void drawSections() {
        if (!sectionsVisible || sections.isEmpty()) return;
        sectionPane.getChildren().clear();

        double centerX      = size * 0.5;
        double centerY      = size * 0.7825;
        double innerRadius  = size * 0.2775;
        double outerRadius  = size * 0.3225;
        int    noOfSections = sections.size();
        List<Line> sectionLines = new ArrayList<>(noOfSections);
        for (int i = 0 ; i < noOfSections - 1 ; i++) {
            Section section = sections.get(i);
            double  angle   = Helper.clamp(90.0, 270.0, (section.getStop() - minValue) * angleStep + 90.0);
            Line    line    = new Line(centerX + innerRadius * Math.sin(-Math.toRadians(angle)), centerY + innerRadius * Math.cos(-Math.toRadians(angle)),
                                       centerX + outerRadius * Math.sin(-Math.toRadians(angle)), centerY + outerRadius * Math.cos(-Math.toRadians(angle)));
            line.setStroke(gauge.getBackgroundPaint());
            sectionLines.add(line);
        }
        sectionPane.getChildren().addAll(sectionLines);

        double barRadius = size * 0.3;
        double barWidth  = size * 0.045;
        double maxValue  = gauge.getMaxValue();
        for (Section section : sections) {
            double startAngle = (section.getStart() - minValue) * angleStep - angleRange;
            double length;
            if (section.getStop() > maxValue) {
                length = (maxValue - section.getStart()) * angleStep;
            } else if (Double.compare(section.getStart(), minValue) < 0) {
                length = (section.getStop() - minValue) * angleStep;
            } else {
                length = (section.getStop() - section.getStart()) * angleStep;
            }
            Arc sectionArc = new Arc(centerX, centerY, barRadius, barRadius, -startAngle, -length);

            sectionArc.setType(ArcType.OPEN);
            sectionArc.setStroke(section.getColor());
            sectionArc.setStrokeWidth(barWidth);
            sectionArc.setStrokeLineCap(StrokeLineCap.BUTT);
            sectionArc.setFill(null);
            sectionArc.setVisible(!highlightSections);
            sectionArc.setOpacity(highlightSections ? 1.0 : 0.25);
            Tooltip sectionTooltip = new Tooltip(section.getText());
            sectionTooltip.setTextAlignment(TextAlignment.CENTER);
            Tooltip.install(sectionArc, sectionTooltip);
            sectionMap.put(section, sectionArc);
        }
        sectionPane.getChildren().addAll(sectionMap.values());
    }

    private void drawNeedle() {
        double needleWidth  = size * 0.05;
        double needleHeight = size * 0.3325;
        needle.setCache(false);

        needle.getElements().clear();
        needle.getElements().add(new MoveTo(0.25 * needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.25 * needleWidth, 0.9022556390977443 * needleHeight,
                                                  0.35 * needleWidth, 0.8872180451127819 * needleHeight,
                                                  0.5 * needleWidth, 0.8872180451127819 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.65 * needleWidth, 0.8872180451127819 * needleHeight,
                                                  0.75 * needleWidth, 0.9022556390977443 * needleHeight,
                                                  0.75 * needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.75 * needleWidth, 0.9473684210526315 * needleHeight,
                                                  0.65 * needleWidth, 0.9624060150375939 * needleHeight,
                                                  0.5 * needleWidth, 0.9624060150375939 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.35 * needleWidth, 0.9624060150375939 * needleHeight,
                                                  0.25 * needleWidth, 0.9473684210526315 * needleHeight,
                                                  0.25 * needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new ClosePath());
        needle.getElements().add(new MoveTo(0.0, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.0, 0.9699248120300752 * needleHeight,
                                                  0.2 * needleWidth, needleHeight,
                                                  0.5 * needleWidth, needleHeight));
        needle.getElements().add(new CubicCurveTo(0.8 * needleWidth, needleHeight,
                                                  needleWidth, 0.9699248120300752 * needleHeight,
                                                  needleWidth, 0.924812030075188 * needleHeight));
        needle.getElements().add(new CubicCurveTo(needleWidth, 0.8947368421052632 * needleHeight,
                                                  0.85 * needleWidth, 0.8646616541353384 * needleHeight,
                                                  0.65 * needleWidth, 0.849624060150376 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.65 * needleWidth, 0.849624060150376 * needleHeight,
                                                  0.65 * needleWidth, 0.022556390977443608 * needleHeight,
                                                  0.65 * needleWidth, 0.022556390977443608 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.65 * needleWidth, 0.007518796992481203 * needleHeight,
                                                  0.6 * needleWidth, 0.0,
                                                  0.5 * needleWidth, 0.0));
        needle.getElements().add(new CubicCurveTo(0.4 * needleWidth, 0.0,
                                                  0.35 * needleWidth, 0.007518796992481203 * needleHeight,
                                                  0.35 * needleWidth, 0.022556390977443608 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.35 * needleWidth, 0.022556390977443608 * needleHeight,
                                                  0.35 * needleWidth, 0.849624060150376 * needleHeight,
                                                  0.35 * needleWidth, 0.849624060150376 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.15 * needleWidth, 0.8646616541353384 * needleHeight,
                                                  0.0, 0.8947368421052632 * needleHeight,
                                                  0.0, 0.924812030075188 * needleHeight));
        needle.getElements().add(new ClosePath());
        needle.setCache(true);
        needle.setCacheHint(CacheHint.ROTATE);
    }

    private void drawAlertIcon() {
        alertIcon.setCache(false);
        double iconWidth  = size * 0.155;
        double iconHeight = size * 0.135;
        alertIcon.getElements().clear();
        alertIcon.getElements().add(new MoveTo(0.4411764705882353 * iconWidth, 0.3380952380952381 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4411764705882353 * iconWidth, 0.3 * iconHeight,
                                                     0.4684873949579832 * iconWidth, 0.2714285714285714 * iconHeight,
                                                     0.5 * iconWidth, 0.2714285714285714 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5315126050420168 * iconWidth, 0.2714285714285714 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.3 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.3380952380952381 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5588235294117647 * iconWidth, 0.3380952380952381 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.6 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.6 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5588235294117647 * iconWidth, 0.6357142857142857 * iconHeight,
                                                     0.5315126050420168 * iconWidth, 0.6666666666666666 * iconHeight,
                                                     0.5 * iconWidth, 0.6666666666666666 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4684873949579832 * iconWidth, 0.6666666666666666 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.6357142857142857 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.6 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4411764705882353 * iconWidth, 0.6 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.3380952380952381 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.3380952380952381 * iconHeight));
        alertIcon.getElements().add(new ClosePath());
        alertIcon.getElements().add(new MoveTo(0.4411764705882353 * iconWidth, 0.8 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4411764705882353 * iconWidth, 0.7642857142857142 * iconHeight,
                                                     0.4684873949579832 * iconWidth, 0.7333333333333333 * iconHeight,
                                                     0.5 * iconWidth, 0.7333333333333333 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5315126050420168 * iconWidth, 0.7333333333333333 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.7642857142857142 * iconHeight,
                                                     0.5588235294117647 * iconWidth, 0.8 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.5588235294117647 * iconWidth, 0.8380952380952381 * iconHeight,
                                                     0.5315126050420168 * iconWidth, 0.8666666666666667 * iconHeight,
                                                     0.5 * iconWidth, 0.8666666666666667 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4684873949579832 * iconWidth, 0.8666666666666667 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.8380952380952381 * iconHeight,
                                                     0.4411764705882353 * iconWidth, 0.8 * iconHeight));
        alertIcon.getElements().add(new ClosePath());
        alertIcon.getElements().add(new MoveTo(0.5504201680672269 * iconWidth, 0.04285714285714286 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.523109243697479 * iconWidth, -0.011904761904761904 * iconHeight,
                                                     0.47689075630252103 * iconWidth, -0.011904761904761904 * iconHeight,
                                                     0.4495798319327731 * iconWidth, 0.04285714285714286 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.4495798319327731 * iconWidth, 0.04285714285714286 * iconHeight,
                                                     0.012605042016806723 * iconWidth, 0.9 * iconHeight,
                                                     0.012605042016806723 * iconWidth, 0.9 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(-0.014705882352941176 * iconWidth, 0.9547619047619048 * iconHeight,
                                                     0.0063025210084033615 * iconWidth, iconHeight,
                                                     0.06302521008403361 * iconWidth, iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.06302521008403361 * iconWidth, iconHeight,
                                                     0.9369747899159664 * iconWidth, iconHeight,
                                                     0.9369747899159664 * iconWidth, iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.9936974789915967 * iconWidth, iconHeight,
                                                     1.0147058823529411 * iconWidth, 0.9547619047619048 * iconHeight,
                                                     0.9873949579831933 * iconWidth, 0.9 * iconHeight));
        alertIcon.getElements().add(new CubicCurveTo(0.9873949579831933 * iconWidth, 0.9 * iconHeight,
                                                     0.5504201680672269 * iconWidth, 0.04285714285714286 * iconHeight,
                                                     0.5504201680672269 * iconWidth, 0.04285714285714286 * iconHeight));
        alertIcon.getElements().add(new ClosePath());
        alertIcon.setCache(true);
        alertIcon.setCacheHint(CacheHint.SPEED);
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    private void resizeDynamicText() {
        double maxWidth = unitText.isManaged() ? size * 0.725 : size * 0.9;
        double fontSize = size * 0.24;
        valueText.setFont(Fonts.latoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }

        maxWidth = size * 0.15;
        fontSize = size * 0.06;
        unitText.setFont(Fonts.latoRegular(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        if (unitText.isVisible()) {
            valueText.relocate((size - valueText.getLayoutBounds().getWidth() - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.15);
            unitText.relocate(valueText.getLayoutX() + valueText.getLayoutBounds().getWidth() + size * 0.025, size * 0.3275);
        } else {
            valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, size * 0.15);
        }

        if (sectionsVisible) {
            fontSize = size * 0.08;
            thresholdText.setFont(Fonts.latoRegular(fontSize));
            thresholdText.setTextOrigin(VPos.CENTER);
            if (thresholdText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(thresholdText, maxWidth, fontSize); }
            thresholdText.setX((size - thresholdText.getLayoutBounds().getWidth()) * 0.5);
            thresholdText.setY(thresholdRect.getLayoutBounds().getMinY() + thresholdRect.getHeight() * 0.5);
        }
    }
    private void resizeStaticText() {
        double maxWidth = size * 0.9;
        double fontSize = size * 0.06;
        double textRadius;
        double sinValue;
        double cosValue;
        double textX;
        double textY;

        titleText.setFont(Fonts.latoRegular(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate(size * 0.05, size * 0.05);

        maxWidth = size * 0.15;
        fontSize = size * 0.07;
        maxValueText.setFont(Fonts.latoRegular(fontSize));
        if (maxValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(maxValueText, maxWidth, fontSize); }
        textRadius = size * 0.3;
        sinValue  = Math.sin(Math.toRadians(90 + (180 - angleRange) * 0.5));
        cosValue  = Math.cos(Math.toRadians(90 + (180 - angleRange) * 0.5));
        textX     = size * 0.5 + textRadius * sinValue;
        textY     = size * 0.83 + textRadius * cosValue;
        maxValueText.setTranslateX(-maxValueText.getLayoutBounds().getWidth() * 0.5);
        maxValueText.setTranslateY(-maxValueText.getLayoutBounds().getHeight() * 0.5);
        maxValueText.relocate(textX, textY);

        minValueText.setFont(Fonts.latoRegular(maxValueText.getFont().getSize()));
        if (minValueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(minValueText, maxWidth, fontSize); }
        sinValue  = Math.sin(Math.toRadians(-90 - (180 - angleRange) * 0.5));
        cosValue  = Math.cos(Math.toRadians(-90 - (180 - angleRange) * 0.5));
        textX     = size * 0.5 + textRadius * sinValue;
        textY     = size * 0.83 + textRadius * cosValue;
        minValueText.setTranslateX(-minValueText.getLayoutBounds().getWidth() * 0.5);
        minValueText.setTranslateY(-minValueText.getLayoutBounds().getHeight() * 0.5);
        minValueText.relocate(textX, textY);

        if (!sectionsVisible) {
            fontSize = size * 0.08;
            thresholdText.setFont(Fonts.latoRegular(fontSize));
            thresholdText.setTextOrigin(VPos.CENTER);
            if (thresholdText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(thresholdText, maxWidth, fontSize); }
            thresholdText.setX((size - thresholdText.getLayoutBounds().getWidth()) * 0.5);
            thresholdText.setY(thresholdRect.getLayoutBounds().getMinY() + thresholdRect.getHeight() * 0.5);
        }
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            double centerX   = size * 0.5;
            double centerY   = size * 0.7825;
            double barRadius = size * 0.3;
            double barWidth  = size * 0.045;

            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            sectionPane.setMaxSize(size, size);

            barBackground.setCenterX(centerX);
            barBackground.setCenterY(centerY);
            barBackground.setRadiusX(barRadius);
            barBackground.setRadiusY(barRadius);
            barBackground.setStrokeWidth(barWidth);
            barBackground.setStartAngle(angleRange * 0.5 + 90);
            barBackground.setLength(-angleRange);

            thresholdBar.setCenterX(centerX);
            thresholdBar.setCenterY(centerY);
            thresholdBar.setRadiusX(barRadius);
            thresholdBar.setRadiusY(barRadius);
            thresholdBar.setStrokeWidth(barWidth);
            thresholdBar.setStartAngle(90 - angleRange * 0.5);
            thresholdBar.setLength((gauge.getMaxValue() - gauge.getThreshold()) * angleStep);

            if (sectionsVisible) { drawSections(); }

            drawAlertIcon();
            alertIcon.relocate((size - alertIcon.getLayoutBounds().getWidth()) * 0.5, size * 0.244);

            needleRect.setWidth(size * 0.035);
            needleRect.setHeight(size * 0.05);
            needleRect.relocate((size - needleRect.getWidth()) * 0.5, size * 0.4575);
            needleRectRotate.setPivotX(needleRect.getLayoutBounds().getWidth() * 0.5);
            needleRectRotate.setPivotY(size * 0.325);

            drawNeedle();

            needle.relocate((size - needle.getLayoutBounds().getWidth()) * 0.5, size * 0.475);
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight() - needle.getLayoutBounds().getWidth() * 0.5);

            resizeStaticText();
            resizeDynamicText();

            thresholdRect.setWidth(thresholdText.getLayoutBounds().getWidth() + size * 0.05);
            thresholdRect.setHeight(thresholdText.getLayoutBounds().getHeight());
            thresholdRect.setX((size - thresholdRect.getWidth()) * 0.5);
            thresholdRect.setY(size * 0.845);
            thresholdRect.setArcWidth(size * 0.025);
            thresholdRect.setArcHeight(size * 0.025);
        }
    }

    @Override protected void redraw() {
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(size * 0.025), new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(size * 0.025), Insets.EMPTY)));

        locale       = gauge.getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();

        thresholdColor = gauge.getThresholdColor();

        sectionsVisible = gauge.getSectionsVisible();
        enableNode(sectionPane, sectionsVisible);

        titleText.setText(gauge.getTitle());
        unitText.setText(gauge.getUnit());
        minValueText.setText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMinValue()));
        maxValueText.setText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMaxValue()));
        thresholdText.setText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getThreshold()));
        resizeStaticText();

        barBackground.setStroke(gauge.getBarColor());
        thresholdBar.setStroke(gauge.getThresholdColor());
        needleRect.setFill(gauge.getBackgroundPaint());
        needle.setFill(gauge.getNeedleColor());
        titleText.setFill(gauge.getTitleColor());
        minValueText.setFill(gauge.getTitleColor());
        maxValueText.setFill(gauge.getTitleColor());
        thresholdRect.setFill(sectionsVisible ? Color.TRANSPARENT : gauge.getValue() > gauge.getThreshold() ? gauge.getThresholdColor() : GRAY);
        thresholdText.setFill(sectionsVisible ? Color.TRANSPARENT : gauge.getBackgroundPaint());
        valueText.setFill(gauge.getValueColor());

        highlightSections(gauge.getValue());
    }
}
