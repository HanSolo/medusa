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

import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
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


/**
 * Created by hansolo on 20.12.15.
 */
public class SimpleSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private double              START_ANGLE      = 315;
    private double              ANGLE_RANGE      = 270;
    private double              size;
    private Pane                pane;
    private Canvas              sectionsCanvas;
    private GraphicsContext     sectionsCtx;
    private Path                needle;
    private MoveTo              needleMoveTo1;
    private CubicCurveTo        needleCubicCurveTo2;
    private CubicCurveTo        needleCubicCurveTo3;
    private CubicCurveTo        needleCubicCurveTo4;
    private LineTo              needleLineTo5;
    private LineTo              needleLineTo6;
    private CubicCurveTo        needleCubicCurveTo7;
    private ClosePath           needleClosePath8;
    private Rotate              needleRotate;
    private Text                valueText;
    private Text                titleText;
    private Text                subTitleText;
    private double              angleStep;
    private String              formatString;
    private Locale              locale;
    private List<Section>       sections;
    private boolean             highlightSections;
    private double              minValue;
    private double              maxValue;


    // ******************** Constructors **************************************
    public SimpleSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleStep         = ANGLE_RANGE / (gauge.getMaxValue() - gauge.getMinValue());
        formatString      = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale            = gauge.getLocale();
        sections          = gauge.getSections();
        highlightSections = gauge.isHighlightSections();
        minValue          = gauge.getMinValue();
        maxValue          = gauge.getMaxValue();

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
    }

    private void initGraphics() {
        sectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionsCtx    = sectionsCanvas.getGraphicsContext2D();

        needleRotate   = new Rotate(180 - START_ANGLE);

        angleStep          = ANGLE_RANGE / (getSkinnable().getRange());
        double targetAngle = 180 - START_ANGLE + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep;
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
        needle.setFill(getSkinnable().getNeedleColor());
        needle.setStroke(getSkinnable().getBorderPaint());
        needle.setStrokeLineCap(StrokeLineCap.ROUND);
        needle.setStrokeLineJoin(StrokeLineJoin.BEVEL);

        valueText = new Text(String.format(locale, formatString, getSkinnable().getMinValue()) + getSkinnable().getUnit());
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());
        Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());

        subTitleText = new Text(getSkinnable().getSubTitle());
        subTitleText.setTextOrigin(VPos.CENTER);
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        Helper.enableNode(subTitleText, !getSkinnable().getSubTitle().isEmpty());

        // Add all nodes
        pane = new Pane(sectionsCanvas, needle, valueText, titleText, subTitleText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(event -> handleEvents(event.eventType.name()));

        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            if (getSkinnable().getCheckSectionsForValue()) {
                double currentValue = getSkinnable().getCurrentValue();
                int listSize = sections.size();
                for (int i = 0 ; i < listSize ; i++) { sections.get(i).checkForValue(currentValue); }
            }
            // Highlight Sections if enabled
            if (highlightSections) {
                drawSections();
            }
        } else if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = getSkinnable().getMinValue();
            maxValue  = getSkinnable().getMaxValue();
            angleStep = ANGLE_RANGE / getSkinnable().getRange();
            needleRotate.setAngle((180 - START_ANGLE) + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
            resize();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections          = getSkinnable().getSections();
            highlightSections = getSkinnable().isHighlightSections();
            resize();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(subTitleText, !getSkinnable().getSubTitle().isEmpty());
        }
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle(final double VALUE) {
        double targetAngle = 180 - START_ANGLE + (VALUE - getSkinnable().getMinValue()) * angleStep;
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle));
        valueText.setText(String.format(locale, formatString, VALUE) + getSkinnable().getUnit());
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        if (valueText.getLayoutBounds().getWidth() > 0.395 * size) { resizeText(); }
    }


    // ******************** Drawing *******************************************
    private void drawSections() {
        sectionsCtx.clearRect(0, 0, size, size);
        double value               = getSkinnable().getCurrentValue();
        boolean sectionTextVisible = getSkinnable().isSectionTextVisible();
        boolean sectionIconVisible = getSkinnable().getSectionIconsVisible();
        double offset              = START_ANGLE - 90;
        int listSize               = sections.size();
        double xy                  = size * 0.015;
        double wh                  = size - (size * 0.03);
        double sectionXY           = size * 0.1375;
        double sectionWH           = size - (size * 0.275);
        angleStep                  = ANGLE_RANGE / (getSkinnable().getRange());
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
        sectionsCtx.setStroke(getSkinnable().getBorderPaint());
        sectionsCtx.setLineWidth(size * 0.025);
        sectionsCtx.strokeArc(xy, xy, wh, wh, offset + 90, ANGLE_RANGE, ArcType.ROUND);

        if (getSkinnable().getTickLabelsVisible()) {
            sectionsCtx.setFont(Fonts.robotoRegular(size * 0.1));
            sectionsCtx.setFill(getSkinnable().getTickLabelColor());
            sectionsCtx.setTextBaseline(VPos.TOP);
            sectionsCtx.setTextAlign(TextAlignment.LEFT);
            sectionsCtx.fillText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMinValue()), size * 0.15075377, size * 0.86180905, size * 0.3);
            sectionsCtx.setTextAlign(TextAlignment.RIGHT);
            sectionsCtx.fillText(String.format(locale, "%." + getSkinnable().getTickLabelDecimals() + "f", getSkinnable().getMaxValue()), size * 0.84924623, size * 0.86180905, size * 0.3);
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
    
    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

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

            double currentValue = (needleRotate.getAngle() + START_ANGLE - 180) / angleStep + getSkinnable().getMinValue();
            valueText.setText(String.format(locale, "%." + getSkinnable().getDecimals() + "f", currentValue) + getSkinnable().getUnit());
            valueText.setVisible(getSkinnable().isValueVisible());

            titleText.setText(getSkinnable().getTitle());

            resizeText();
        }
    }

    private void redraw() {
        needle.setFill(getSkinnable().getNeedleColor());
        needle.setStroke(getSkinnable().getNeedleBorderColor());
        titleText.setFill(getSkinnable().getTitleColor());
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        titleText.setText(getSkinnable().getTitle());
        resizeText();
    }
}
