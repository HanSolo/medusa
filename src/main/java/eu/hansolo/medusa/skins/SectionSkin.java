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
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.List;
import java.util.Locale;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 12.02.16.
 */
public class SectionSkin extends GaugeSkinBase {
    private double                      START_ANGLE = 300;
    private double                      ANGLE_RANGE = 240;
    private double                      size;
    private Pane                        pane;
    private Path                        ring;
    private Canvas                      sectionsCanvas;
    private GraphicsContext             sectionsCtx;
    private Circle                      mask;
    private Circle                      knob;
    private Path                        needle;
    private Rotate                      needleRotate;
    private Text                        valueText;
    private Text                        titleText;
    private double                      angleStep;
    private List<Section>               sections;
    private boolean                     highlightSections;
    private boolean                     sectionsVisible;
    private double                      minValue;
    private double                      maxValue;
    private ListChangeListener<Section> sectionListener;
    private InvalidationListener        currentValueListener;


    // ******************** Constructors **************************************
    public SectionSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        angleStep            = ANGLE_RANGE / (gauge.getMaxValue() - gauge.getMinValue());
        sections             = gauge.getSections();
        highlightSections    = gauge.isHighlightSections();
        sectionsVisible      = gauge.getSectionsVisible();
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

        ring = new Path();
        ring.setFillRule(FillRule.EVEN_ODD);
        ring.setStroke(null);
        ring.setFill(Gauge.DARK_COLOR);
        ring.setEffect(new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(255, 255, 255, 0.35), 1, 0, 0, 1));

        sectionsCanvas = new Canvas(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        sectionsCtx    = sectionsCanvas.getGraphicsContext2D();

        mask = new Circle();
        mask.setStroke(null);
        mask.setFill(gauge.getBackgroundPaint());

        knob = new Circle();
        knob.setStroke(null);
        knob.setFill(gauge.getKnobColor());

        angleStep = ANGLE_RANGE / (gauge.getRange());
        double targetAngle = 180 - START_ANGLE + (gauge.getValue() - gauge.getMinValue()) * angleStep;

        needleRotate = new Rotate(180 - START_ANGLE);
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle));

        needle = new Path();
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.setStroke(null);
        needle.getTransforms().setAll(needleRotate);

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getMinValue()) + gauge.getUnit());
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

        titleText = new Text(gauge.getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        // Add all nodes
        pane = new Pane(ring, sectionsCanvas, mask, knob, needle, valueText, titleText);

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
            Helper.enableNode(valueText, gauge.isValueVisible());
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
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
    private void createNeedle() {
        double needleWidth  = size * 0.04;
        double needleHeight = size * 0.4675;
        needle.getElements().clear();
        needle.getElements().add(new MoveTo(0.3125 * needleWidth, 0.015957446808510637 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.3125 * needleWidth, 0.005319148936170213 * needleHeight,
                                                  0.4375 * needleWidth, 0.0,
                                                  0.5 * needleWidth, 0.0));
        needle.getElements().add(new CubicCurveTo(0.5625 * needleWidth, 0.0,
                                                  0.6875 * needleWidth, 0.005319148936170213 * needleHeight,
                                                  0.6875 * needleWidth, 0.015957446808510637 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.6875 * needleWidth, 0.015957446808510637 * needleHeight,
                                                  needleWidth, 0.9946808510638298 * needleHeight,
                                                  needleWidth, 0.9946808510638298 * needleHeight));
        needle.getElements().add(new LineTo(0.0, 0.9946808510638298 * needleHeight));
        needle.getElements().add(new CubicCurveTo(0.0, 0.9946808510638298 * needleHeight,
                                                  0.3125 * needleWidth, 0.015957446808510637 * needleHeight,
                                                  0.3125 * needleWidth, 0.015957446808510637 * needleHeight));
        needle.getElements().add(new ClosePath());
        needle.setFill(new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                          needle.getLayoutBounds().getMaxX(), 0,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0.0, gauge.getNeedleColor().darker()),
                                          new Stop(0.5, gauge.getNeedleColor()),
                                          new Stop(1.0, gauge.getNeedleColor().darker())));
    }

    private void drawSections() {
        sectionsCtx.clearRect(0, 0, size, size);
        if (!sectionsVisible | sections.isEmpty()) return;
        double value               = gauge.getCurrentValue();
        boolean sectionTextVisible = gauge.isSectionTextVisible();
        boolean sectionIconVisible = gauge.getSectionIconsVisible();
        double offset              = START_ANGLE - 90;
        int listSize               = sections.size();
        double xy                  = size * 0.0325;
        double wh                  = size - (size * 0.065);
        double center              = size * 0.5;
        double textPointX;
        double textPointY;
        angleStep                  = ANGLE_RANGE / gauge.getRange();
        sectionsCtx.setFont(Fonts.robotoCondensedLight(0.05 * size));
        sectionsCtx.setTextAlign(TextAlignment.CENTER);
        sectionsCtx.setTextBaseline(VPos.CENTER);
        double sinValue;
        double cosValue;
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
                sectionsCtx.setFill(section.contains(value) ? section.getHighlightColor() : section.getColor());
            } else {
                sectionsCtx.setFill(section.getColor());
            }
            sectionsCtx.fillArc(xy, xy, wh, wh, (offset - SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.ROUND);
            sectionsCtx.setStroke(Gauge.DARK_COLOR);
            sectionsCtx.setLineWidth(size * 0.005);
            sectionsCtx.strokeArc(xy, xy, wh, wh, (offset - SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.ROUND);

            // Draw Section Text
            if (sectionTextVisible) {
                double angle = offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5;
                sinValue   = -Math.sin(Math.toRadians(angle));
                cosValue   = -Math.cos(Math.toRadians(angle));
                textPointX = center + size * 0.4 * sinValue;
                textPointY = center + size * 0.4 * cosValue;
                sectionsCtx.save();

                sectionsCtx.translate(textPointX, textPointY);
                sectionsCtx.rotate(-angle);
                sectionsCtx.translate(-textPointX,-textPointY);

                sectionsCtx.setFill(section.getTextColor());
                sectionsCtx.fillText(section.getText(), textPointX, textPointY, 0.2 * size);
                sectionsCtx.restore();
            } else if (size > 0 && sectionIconVisible) {
                // Draw Section Icon
                Image icon = section.getImage();
                if (null != icon) {
                    sinValue = -Math.sin(Math.toRadians(offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                    cosValue = -Math.cos(Math.toRadians(offset - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                    Point2D iconPoint = new Point2D(center + size * 0.365 * sinValue, size * 0.5 + size * 0.365 * cosValue);
                    sectionsCtx.drawImage(icon, iconPoint.getX() - size * 0.06, iconPoint.getY() - size * 0.06, size * 0.12, size * 0.12);
                }
            }
            sectionsCtx.restore();
        }
    }

    private void resizeText() {
        double fontSize = size * 0.1;
        valueText.setFont(Fonts.robotoMedium(fontSize));
        double maxWidth = 0.395 * size;
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        valueText.setTranslateY(size * 0.68);

        fontSize = size * 0.11;
        titleText.setFont(Fonts.robotoMedium(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(size * 0.85);
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((gauge.getWidth() - size) * 0.5, (gauge.getHeight() - size) * 0.5);

            ring.getElements().clear();
            ring.getElements().add(new MoveTo(0.5 * size, 0.03 * size));
            ring.getElements().add(new CubicCurveTo(0.76 * size, 0.03 * size, 0.97 * size, 0.24 * size, 0.97 * size, 0.5 * size));
            ring.getElements().add(new CubicCurveTo(0.97 * size, 0.76 * size, 0.76 * size, 0.97 * size, 0.5 * size, 0.97 * size));
            ring.getElements().add(new CubicCurveTo(0.24 * size, 0.97 * size, 0.03 * size, 0.76 * size, 0.03 * size, 0.5 * size));
            ring.getElements().add(new CubicCurveTo(0.03 * size, 0.24 * size, 0.24 * size, 0.03 * size, 0.5 * size, 0.03 * size));
            ring.getElements().add(new ClosePath());
            ring.getElements().add(new MoveTo(0.5 * size, 0.2125 * size));
            ring.getElements().add(new CubicCurveTo(0.6575 * size, 0.2125 * size, 0.7875 * size, 0.3425 * size, 0.7875 * size, 0.5 * size));
            ring.getElements().add(new CubicCurveTo(0.7875 * size, 0.6575 * size, 0.6575 * size, 0.7875 * size, 0.5 * size, 0.7875 * size));
            ring.getElements().add(new CubicCurveTo(0.3425 * size, 0.7875 * size, 0.2125 * size, 0.6575 * size, 0.2125 * size, 0.5 * size));
            ring.getElements().add(new CubicCurveTo(0.2125 * size, 0.3425 * size, 0.3425 * size, 0.2125 * size, 0.5 * size, 0.2125 * size));
            ring.getElements().add(new ClosePath());

            sectionsCanvas.setCache(false);
            sectionsCanvas.setWidth(size);
            sectionsCanvas.setHeight(size);
            drawSections();
            sectionsCanvas.setCache(true);
            sectionsCanvas.setCacheHint(CacheHint.QUALITY);

            mask.setCenterX(size * 0.5);
            mask.setCenterY(size * 0.5);
            mask.setRadius(size * 0.2855);

            knob.setCenterX(size * 0.5);
            knob.setCenterY(size * 0.5);
            knob.setRadius(size * 0.10375);

            needle.setCache(false);
            createNeedle();
            needle.setCache(true);
            needle.setCacheHint(CacheHint.ROTATE);

            needle.relocate((size - needle.getLayoutBounds().getWidth()) * 0.5, (size * 0.5) - needle.getLayoutBounds().getHeight());
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getMaxY());

            double currentValue = (needleRotate.getAngle() + START_ANGLE - 180) / angleStep + gauge.getMinValue();
            valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()) + gauge.getUnit());
            valueText.setVisible(gauge.isValueVisible());

            titleText.setText(gauge.getTitle());

            resizeText();
        }
    }

    @Override protected void redraw() {
        sectionsVisible = gauge.getSectionsVisible();
        drawSections();
        needle.setFill(new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                          needle.getLayoutBounds().getMaxX(), 0,
                                          false, CycleMethod.NO_CYCLE,
                                          new Stop(0.0, gauge.getNeedleColor().darker()),
                                          new Stop(0.5, gauge.getNeedleColor()),
                                          new Stop(1.0, gauge.getNeedleColor().darker())));
        titleText.setFill(gauge.getTitleColor());
        valueText.setFill(gauge.getValueColor());
        mask.setFill(gauge.getBackgroundPaint());
        knob.setFill(gauge.getKnobColor());
        titleText.setText(gauge.getTitle());
        resizeText();
    }
}
