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
import eu.hansolo.medusa.Section.SectionEvent;
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
    private double              angleStep;


    // ******************** Constructors **************************************
    public SimpleSkin(Gauge gauge) {
        super(gauge);
        angleStep        = ANGLE_RANGE / (gauge.getMaxValue() - gauge.getMinValue());

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
        targetAngle        = Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle);
        setRotationAngle(targetAngle);

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

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getMinValue()) + getSkinnable().getUnit());
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setFill(getSkinnable().getValueColor());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setFill(getSkinnable().getTitleColor());

        // Add all nodes
        pane = new Pane();
        pane.getChildren().setAll(sectionsCanvas,
                                  needle,
                                  valueText,
                                  titleText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().getSections().addListener((ListChangeListener<Section>) change -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(event -> handleEvents(event.eventType.name()));

        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle());

        needleRotate.angleProperty().addListener(o -> handleEvents("ANGLE"));
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            needle.setFill(getSkinnable().getNeedleColor());
            needle.setStroke(getSkinnable().getBorderPaint());
            titleText.setFill(getSkinnable().getTitleColor());
            valueText.setFill(getSkinnable().getValueColor());
        } else if ("ANGLE".equals(EVENT_TYPE)) {
            double currentValue = getSkinnable().getCurrentValue();
            valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue) + getSkinnable().getUnit());
            valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
            if (valueText.getLayoutBounds().getWidth() > 0.395 * size) { resizeText(); }
            // Check sections
            for (Section section : getSkinnable().getSections()) {
                if (section.contains(currentValue)) {
                    section.fireSectionEvent(new Section.SectionEvent(section, null, SectionEvent.SECTION_ENTERED));
                    break;
                }
            }
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            angleStep = ANGLE_RANGE / getSkinnable().getRange();
            needleRotate.setAngle((180 - START_ANGLE) + (getSkinnable().getValue() - getSkinnable().getMinValue()) * angleStep);
            resize();
        }
    }


    // ******************** Private Methods ***********************************
    private void setRotationAngle(final double ANGLE) {
        needleRotate.setAngle(Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, ANGLE));
    }

    private void rotateNeedle() {
        angleStep          = ANGLE_RANGE / (getSkinnable().getRange());
        double targetAngle = 180 - START_ANGLE + (getSkinnable().getCurrentValue() - getSkinnable().getMinValue()) * angleStep;
        targetAngle        = Helper.clamp(180 - START_ANGLE, 180 - START_ANGLE + ANGLE_RANGE, targetAngle);
        setRotationAngle(targetAngle);
    }

    private void drawSections() {
        sectionsCtx.clearRect(0, 0, size, size);
        final double MIN_VALUE       = getSkinnable().getMinValue();
        final double MAX_VALUE       = getSkinnable().getMaxValue();
        final double OFFSET          = START_ANGLE - 90;
        final int NO_OF_SECTIONS     = getSkinnable().getSections().size();
        final double SECTIONS_OFFSET = size * 0.015;
        final double SECTIONS_SIZE   = size - (size * 0.03);
        angleStep                    = ANGLE_RANGE / (getSkinnable().getRange());
        double sinValue;
        double cosValue;
        for (int i = 0 ; i < NO_OF_SECTIONS ; i++) {
            final Section SECTION = getSkinnable().getSections().get(i);
            final double SECTION_START_ANGLE;
            if (SECTION.getStart() > MAX_VALUE || SECTION.getStop() < MIN_VALUE) continue;

            if (SECTION.getStart() < MIN_VALUE && SECTION.getStop() < MAX_VALUE) {
                SECTION_START_ANGLE = MIN_VALUE * angleStep;
            } else {
                SECTION_START_ANGLE = (SECTION.getStart() - MIN_VALUE) * angleStep;
            }
            final double SECTION_ANGLE_EXTEND;
            if (SECTION.getStop() > MAX_VALUE) {
                SECTION_ANGLE_EXTEND = (MAX_VALUE - SECTION.getStart()) * angleStep;
            } else {
                SECTION_ANGLE_EXTEND = (SECTION.getStop() - SECTION.getStart()) * angleStep;
            }
            sectionsCtx.save();
            sectionsCtx.setFill(SECTION.getColor());
            sectionsCtx.fillArc(SECTIONS_OFFSET, SECTIONS_OFFSET, SECTIONS_SIZE, SECTIONS_SIZE, (OFFSET - SECTION_START_ANGLE), -SECTION_ANGLE_EXTEND, ArcType.ROUND);

            // Draw Section Text
            if (getSkinnable().isSectionTextVisible()) {
                sinValue = -Math.sin(Math.toRadians(OFFSET - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                cosValue = -Math.cos(Math.toRadians(OFFSET - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                Point2D textPoint = new Point2D(size * 0.5 + size * 0.365 * sinValue, size * 0.5 + size * 0.365 * cosValue);
                sectionsCtx.setFont(Fonts.robotoMedium(0.08 * size));
                sectionsCtx.setTextAlign(TextAlignment.CENTER);
                sectionsCtx.setTextBaseline(VPos.CENTER);
                sectionsCtx.setFill(SECTION.getTextColor());
                sectionsCtx.fillText(SECTION.getText(), textPoint.getX(), textPoint.getY());
            }

            // Draw Section Icon
            if (size > 0) {
                if (getSkinnable().areSectionIconsVisible() && !getSkinnable().isSectionTextVisible()) {
                    if (null != SECTION.getImage()) {
                        Image icon = SECTION.getImage();
                        sinValue = -Math.sin(Math.toRadians(OFFSET - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                        cosValue = -Math.cos(Math.toRadians(OFFSET - 90 - SECTION_START_ANGLE - SECTION_ANGLE_EXTEND * 0.5));
                        Point2D iconPoint = new Point2D(size * 0.5 + size * 0.365 * sinValue, size * 0.5 + size * 0.365 * cosValue);
                        sectionsCtx.drawImage(icon, iconPoint.getX() - size * 0.06, iconPoint.getY() - size * 0.06, size * 0.12, size * 0.12);
                    }
                }
            }
            sectionsCtx.restore();
        }
        // Draw white border around area
        sectionsCtx.setStroke(getSkinnable().getBorderPaint());
        sectionsCtx.setLineWidth(size * 0.025);
        sectionsCtx.strokeArc(SECTIONS_OFFSET, SECTIONS_OFFSET, SECTIONS_SIZE, SECTIONS_SIZE, OFFSET + 90, ANGLE_RANGE, ArcType.ROUND);
    }

    private void resizeText() {
        valueText.setFont(Fonts.robotoMedium(size * 0.145));
        double maxWidth = 0.395 * size;
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { adjustTextSize(valueText, maxWidth, 0.15); }
        valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        valueText.setTranslateY(size * (titleText.getText().isEmpty() ? 0.5 : 0.48));

        titleText.setFont(Fonts.robotoMedium(size * 0.045));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { adjustTextSize(titleText, maxWidth, 0.05); }
        titleText.setTranslateX((size - titleText.getLayoutBounds().getWidth()) * 0.5);
        titleText.setTranslateY(size * 0.5 + valueText.getFont().getSize() * 0.7);
    }

    private void adjustTextSize(final Text TEXT, final double MAX_WIDTH, final double DECREMENT_FACTOR) {
        double decrement = 0d;
        while (TEXT.getLayoutBounds().getWidth() > MAX_WIDTH && TEXT.getFont().getSize() > 0) {
            TEXT.setFont(Fonts.robotoMedium(size * (DECREMENT_FACTOR - decrement)));
            decrement += 0.01;
        }
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

            needleLineTo5.setX(0.5 * size); needleLineTo5.setY(0.0225 * size);

            needleLineTo6.setX(0.4607375 * size); needleLineTo6.setY(0.2784125 * size);

            needleCubicCurveTo7.setControlX1(0.3551895 * size); needleCubicCurveTo7.setControlY1(0.296985 * size);
            needleCubicCurveTo7.setControlX2(0.275 * size); needleCubicCurveTo7.setControlY2(0.3891265 * size);
            needleCubicCurveTo7.setX(0.275 * size); needleCubicCurveTo7.setY(0.5 * size);

            needle.setStrokeWidth(size * 0.025);

            needle.relocate(needle.getLayoutBounds().getMinX(), needle.getLayoutBounds().getMinY());
            needleRotate.setPivotX(size * 0.5);
            needleRotate.setPivotY(size * 0.5);

            double currentValue = (needleRotate.getAngle() + START_ANGLE - 180) / angleStep + getSkinnable().getMinValue();
            valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", currentValue) + getSkinnable().getUnit());
            valueText.setVisible(getSkinnable().isValueVisible());

            titleText.setText(getSkinnable().getTitle());

            resizeText();
        }
    }

    private void redraw() {
        titleText.setText(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        resizeText();
    }
}
