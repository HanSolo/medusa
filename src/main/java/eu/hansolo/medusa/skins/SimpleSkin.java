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
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Locale;

import static eu.hansolo.medusa.tools.Helper.enableNode;
import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 20.12.15.
 */
public class SimpleSkin extends GaugeSkinBase {
    private double                      START_ANGLE = 315;
    private double                      ANGLE_RANGE = 270;
    private double                      size;
    private Pane                        pane;
    private Canvas                      sectionsCanvas;
    private GraphicsContext             sectionsCtx;
    private Path                        needle;
    private MoveTo                      needleMoveTo1;
    private CubicCurveTo                needleCubicCurveTo2;
    private CubicCurveTo                needleCubicCurveTo3;
    private CubicCurveTo                needleCubicCurveTo4;
    private LineTo                      needleLineTo5;
    private LineTo                      needleLineTo6;
    private CubicCurveTo                needleCubicCurveTo7;
    private ClosePath                   needleClosePath8;
    private Rotate                      needleRotate;
    private Text                        valueText;
    private Text                        titleText;
    private Text                        subTitleText;
    private double                      angleStep;
    private Locale                      locale;
    private List<Section>               sections;
    private boolean                     highlightSections;
    private double                      minValue;
    private double                      maxValue;
    private ListChangeListener<Section> sectionListener;
    private InvalidationListener        currentValueListener;


    // ******************** Constructors **************************************
    public SimpleSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleStep            = ANGLE_RANGE / (gauge.getMaxValue() - gauge.getMinValue());
        locale               = gauge.getLocale();
        sections             = gauge.getSections();
        highlightSections    = gauge.isHighlightSections();
        minValue             = gauge.getMinValue();
        maxValue             = gauge.getMaxValue();
        sectionListener      = c -> handleEvents("RESIZE");
        currentValueListener = o -> rotateNeedle(gauge.getCurrentValue());

        initGraphics();
        registerListeners();
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

        sectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionsCtx    = sectionsCanvas.getGraphicsContext2D();

        needleRotate   = new Rotate(180 - START_ANGLE);

        angleStep          = ANGLE_RANGE / (gauge.getRange());
        double targetAngle = 180 - START_ANGLE + (gauge.getValue() - gauge.getMinValue()) * angleStep;
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle));

        needleMoveTo1       = new MoveTo();
        needleCubicCurveTo2 = new CubicCurveTo();
        needleCubicCurveTo3 = new CubicCurveTo();
        needleCubicCurveTo4 = new CubicCurveTo();
        needleLineTo5       = new LineTo();
        needleLineTo6       = new LineTo();
        needleCubicCurveTo7 = new CubicCurveTo();
        needleClosePath8    = new ClosePath();
        needle              = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4, needleLineTo5, needleLineTo6, needleCubicCurveTo7, needleClosePath8);
        needle.setFillRule(FillRule.EVEN_ODD);

        needle.getTransforms().setAll(needleRotate);
        needle.setFill(gauge.getNeedleColor());
        needle.setStroke(gauge.getBorderPaint());
        needle.setStrokeLineCap(StrokeLineCap.ROUND);
        needle.setStrokeLineJoin(StrokeLineJoin.BEVEL);

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getMinValue()) + gauge.getUnit());
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(gauge.getValueColor());
        enableNode(valueText, gauge.isValueVisible());

        titleText = new Text(gauge.getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(gauge.getTitleColor());
        enableNode(titleText, !gauge.getTitle().isEmpty());

        subTitleText = new Text(gauge.getSubTitle());
        subTitleText.setTextOrigin(VPos.CENTER);
        subTitleText.setFill(gauge.getSubTitleColor());
        enableNode(subTitleText, !gauge.getSubTitle().isEmpty());

        // Add all nodes
        pane = new Pane(sectionsCanvas, needle, valueText, titleText, subTitleText);

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.getSections().addListener(sectionListener);
        gauge.currentValueProperty().addListener(currentValueListener);
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("FINISHED".equals(EVENT_TYPE)) {
            if (gauge.getCheckSectionsForValue()) {
                double currentValue = gauge.getCurrentValue();
                int listSize = sections.size();
                for (int i = 0 ; i < listSize ; i++) { sections.get(i).checkForValue(currentValue); }
            }
            // Highlight Sections if enabled
            if (highlightSections) {
                drawSections();
            }
        } else if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = gauge.getMinValue();
            maxValue  = gauge.getMaxValue();
            angleStep = ANGLE_RANGE / gauge.getRange();
            needleRotate.setAngle((180 - START_ANGLE) + (gauge.getValue() - gauge.getMinValue()) * angleStep);
            resize();
            rotateNeedle(gauge.getCurrentValue());
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = gauge.getSections();
            highlightSections = gauge.isHighlightSections();
            resize();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            enableNode(valueText, gauge.isValueVisible());
            enableNode(titleText, !gauge.getTitle().isEmpty());
            enableNode(subTitleText, !gauge.getSubTitle().isEmpty());
        }
    }

    @Override public void dispose() {
        gauge.getSections().removeListener(sectionListener);
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle(final double VALUE) {
        double targetAngle = 180 - START_ANGLE + (VALUE - gauge.getMinValue()) * angleStep;
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle));
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE) + gauge.getUnit());
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        if (valueText.getLayoutBounds().getWidth() > 0.395 * size) { resizeText(); }
    }


    // ******************** Drawing *******************************************
    private void drawSections() {
        sectionsCtx.clearRect(0, 0, size, size);
        double value               = gauge.getCurrentValue();
        boolean sectionTextVisible = gauge.isSectionTextVisible();
        boolean sectionIconVisible = gauge.getSectionIconsVisible();
        double offset              = START_ANGLE - 90;
        int listSize               = sections.size();
        double xy                  = size * 0.015;
        double wh                  = size - (size * 0.03);
        double sectionXY           = size * 0.1375;
        double sectionWH           = size - (size * 0.275);
        angleStep                  = ANGLE_RANGE / (gauge.getRange());
        double sinValue;
        double cosValue;
        sectionsCtx.setLineWidth(size * 0.27);
        sectionsCtx.setLineCap(StrokeLineCap.BUTT);
        for (int i = 0 ; i < listSize ; i++) {
            Section section = sections.get(i);
            final double SECTION_START_ANGLE;
            if (section.getStart() > maxValue || section.getStop() < minValue) continue;

            if (section.getStart() < minValue && section.getStop() < maxValue) {
                SECTION_START_ANGLE = 0;
            } else {
                SECTION_START_ANGLE = (section.getStart() - minValue) * angleStep;
            }
            final double SECTION_ANGLE_EXTEND;
            if (Double.compare(section.getStop(), maxValue) > 0) {
                SECTION_ANGLE_EXTEND = (maxValue - section.getStart()) * angleStep;
            } else if (Double.compare(section.getStart(), minValue) < 0) {
                SECTION_ANGLE_EXTEND = (section.getStop() - minValue) * angleStep;
            } else {
                SECTION_ANGLE_EXTEND = (section.getStop() - section.getStart()) * angleStep;
            }
            sectionsCtx.save();
            if (highlightSections) {
                sectionsCtx.setStroke(section.contains(value) ? section.getHighlightColor() : section.getColor());
            } else {
                sectionsCtx.setStroke(section.getColor());
            }
            sectionsCtx.strokeArc(sectionXY, sectionXY, sectionWH, sectionWH, (offset - SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.OPEN);

            // Draw Section Text
            if (sectionTextVisible) {
                sinValue = -Math.sin(Math.toRadians(offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                cosValue = -Math.cos(Math.toRadians(offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                Point2D textPoint = new Point2D(size * 0.5 + size * 0.365 * sinValue, size * 0.5 + size * 0.365 * cosValue);
                sectionsCtx.setFont(Fonts.robotoMedium(0.08 * size));
                sectionsCtx.setTextAlign(TextAlignment.CENTER);
                sectionsCtx.setTextBaseline(VPos.CENTER);
                sectionsCtx.setFill(section.getTextColor());
                sectionsCtx.fillText(section.getText(), textPoint.getX(), textPoint.getY(), 0.2 * size);
            } else if (size > 0 && sectionIconVisible) {
                // Draw Section Icon
                Image icon = section.getImage();
                if (null != icon) {
                    sinValue = -Math.sin(Math.toRadians(offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                    cosValue = -Math.cos(Math.toRadians(offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                    Point2D iconPoint = new Point2D(size * 0.5 + size * 0.365 * sinValue, size * 0.5 + size * 0.365 * cosValue);
                    sectionsCtx.drawImage(icon, iconPoint.getX() - size * 0.06, iconPoint.getY() - size * 0.06, size * 0.12, size * 0.12);
                }
            }
            sectionsCtx.restore();
        }
        // Draw white border around area
        sectionsCtx.setStroke(gauge.getBorderPaint());
        sectionsCtx.setLineWidth(size * 0.025);
        sectionsCtx.strokeArc(xy, xy, wh, wh, offset + 90, ANGLE_RANGE, ArcType.ROUND);

        if (gauge.getTickLabelsVisible()) {
            sectionsCtx.setFont(Fonts.robotoRegular(size * 0.1));
            sectionsCtx.setFill(gauge.getTickLabelColor());
            sectionsCtx.setTextBaseline(VPos.TOP);
            sectionsCtx.setTextAlign(TextAlignment.LEFT);
            sectionsCtx.fillText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMinValue()), size * 0.15075377, size * 0.86180905, size * 0.3);
            sectionsCtx.setTextAlign(TextAlignment.RIGHT);
            sectionsCtx.fillText(String.format(locale, "%." + gauge.getTickLabelDecimals() + "f", gauge.getMaxValue()), size * 0.84924623, size * 0.86180905, size * 0.3);
        }
    }

    private void resizeText() {
        double fontSize = size * 0.25;
        double maxWidth = 0.35 * size;
        valueText.setFont(Fonts.robotoMedium(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        valueText.setTranslateY(size * (titleText.getText().isEmpty() ? 0.5 : 0.46));

        fontSize = size * 0.075;
        maxWidth = size * 0.3;
        titleText.setFont(Fonts.robotoMedium(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(size * 0.5 + valueText.getFont().getSize() * 0.45);

        maxWidth = size * 0.45;
        subTitleText.setFont(Fonts.robotoMedium(fontSize));
        if (subTitleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(subTitleText, maxWidth, fontSize); }
        subTitleText.setTranslateX((size - subTitleText.getLayoutBounds().getWidth()) * 0.5);
        subTitleText.setTranslateY(size * 0.8);
    }
    
    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((gauge.getWidth() - size) * 0.5, (gauge.getHeight() - size) * 0.5);

            sectionsCanvas.setCache(false);
            sectionsCanvas.setWidth(size);
            sectionsCanvas.setHeight(size);
            drawSections();
            sectionsCanvas.setCache(true);
            sectionsCanvas.setCacheHint(CacheHint.QUALITY);

            needle.setCache(false);

            needleMoveTo1.setX(0.275 * size); needleMoveTo1.setY(0.5 * size);

            needleCubicCurveTo2.setControlX1(0.275 * size); needleCubicCurveTo2.setControlY1(0.62426575 * size);
            needleCubicCurveTo2.setControlX2(0.37573425 * size); needleCubicCurveTo2.setControlY2(0.725 * size);
            needleCubicCurveTo2.setX(0.5 * size); needleCubicCurveTo2.setY(0.725 * size);

            needleCubicCurveTo3.setControlX1(0.62426575 * size); needleCubicCurveTo3.setControlY1(0.725 * size);
            needleCubicCurveTo3.setControlX2(0.725 * size); needleCubicCurveTo3.setControlY2(0.62426575 * size);
            needleCubicCurveTo3.setX(0.725 * size); needleCubicCurveTo3.setY(0.5 * size);

            needleCubicCurveTo4.setControlX1(0.725 * size); needleCubicCurveTo4.setControlY1(0.3891265 * size);
            needleCubicCurveTo4.setControlX2(0.6448105 * size); needleCubicCurveTo4.setControlY2(0.296985 * size);
            needleCubicCurveTo4.setX(0.5392625 * size); needleCubicCurveTo4.setY(0.2784125 * size);

            needleLineTo5.setX(0.5 * size); needleLineTo5.setY(0.004 * size);

            needleLineTo6.setX(0.4607375 * size); needleLineTo6.setY(0.2784125 * size);

            needleCubicCurveTo7.setControlX1(0.3551895 * size); needleCubicCurveTo7.setControlY1(0.296985 * size);
            needleCubicCurveTo7.setControlX2(0.275 * size); needleCubicCurveTo7.setControlY2(0.3891265 * size);
            needleCubicCurveTo7.setX(0.275 * size); needleCubicCurveTo7.setY(0.5 * size);

            needle.setCache(true);
            needle.setCacheHint(CacheHint.ROTATE);

            needle.setStrokeWidth(size * 0.025);

            needle.relocate(needle.getLayoutBounds().getMinX(), needle.getLayoutBounds().getMinY());
            needleRotate.setPivotX(size * 0.5);
            needleRotate.setPivotY(size * 0.5);

            double currentValue = (needleRotate.getAngle() + START_ANGLE - 180) / angleStep + gauge.getMinValue();
            valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), currentValue) + gauge.getUnit());
            valueText.setVisible(gauge.isValueVisible());

            titleText.setText(gauge.getTitle());

            resizeText();
        }
    }

    @Override protected void redraw() {
        enableNode(sectionsCanvas, gauge.getSectionsVisible());
        needle.setFill(gauge.getNeedleColor());
        needle.setStroke(gauge.getNeedleBorderColor());
        titleText.setFill(gauge.getTitleColor());
        subTitleText.setFill(gauge.getSubTitleColor());
        valueText.setFill(gauge.getValueColor());
        locale       = gauge.getLocale();
        titleText.setText(gauge.getTitle());
        resizeText();
    }
}
