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
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 13.01.16.
 */
public class SlimSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ANGLE_RANGE      = 360;
    private double        size;
    private Arc           barBackground;
    private Arc           bar;
    private Text          titleText;
    private Text          valueText;
    private Text          unitText;
    private Pane          pane;
    private double        range;
    private double        angleStep;
    private boolean       colorGradientEnabled;
    private int           noOfGradientStops;
    private boolean       sectionsVisible;
    private List<Section> sections;


    // ******************** Constructors **************************************
    public SlimSkin(Gauge gauge) {
        super(gauge);
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        colorGradientEnabled = gauge.isColorGradientEnabled();
        noOfGradientStops    = gauge.getGradientLookupStops().size();
        sectionsVisible      = gauge.areSectionsVisible();
        sections             = gauge.getSections();

        init();
        initGraphics();
        registerListeners();
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
        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.48590226, PREFERRED_HEIGHT * 0.48590226, 90, 360);
        barBackground.setType(ArcType.OPEN);
        barBackground.setStroke(getSkinnable().getBarBackgroundColor());
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        barBackground.setStrokeLineCap(StrokeLineCap.ROUND);
        barBackground.setFill(null);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.48590226, PREFERRED_HEIGHT * 0.48590226, 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(getSkinnable().getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.02819549 * 2);
        bar.setStrokeLineCap(StrokeLineCap.ROUND);
        bar.setFill(null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFill(getSkinnable().getTitleColor());

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getCurrentValue()));
        valueText.setFill(getSkinnable().getValueColor());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());

        pane = new Pane(barBackground, bar, titleText, valueText, unitText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> setBar(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            range     = getSkinnable().getRange();
            angleStep = ANGLE_RANGE / range;
            redraw();
        }
    }

    private void setBar(final double VALUE) {
        double minValue = getSkinnable().getMinValue();
        if (minValue > 0) {
            bar.setLength(((VALUE - minValue) * (-1)) * angleStep);
        } else {
            if (VALUE < 0) {
                bar.setLength((-VALUE + minValue) * angleStep);
            } else {
                bar.setLength(((minValue - VALUE) * angleStep));
            }
        }
        setBarColor(VALUE);
        valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", VALUE));
        resizeValueText();
    }
    private void setBarColor(final double VALUE) {
        if (!sectionsVisible && !colorGradientEnabled) {
            bar.setStroke(getSkinnable().getBarColor());
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            bar.setStroke(getSkinnable().getGradientLookup().getColorAt(VALUE / range));
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
        colorGradientEnabled = getSkinnable().isColorGradientEnabled();
        noOfGradientStops    = getSkinnable().getGradientLookupStops().size();
        sectionsVisible      = getSkinnable().areSectionsVisible();
        sections             = getSkinnable().getSections();

        titleText.setText(getSkinnable().getTitle());
        unitText.setText(getSkinnable().getUnit());
        resizeTitleAndUnitText();

        barBackground.setStroke(getSkinnable().getBarBackgroundColor());
        setBarColor(getSkinnable().getCurrentValue());
        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
    }

    private void resizeValueText() {
        double maxWidth = 0.86466165 * size;
        valueText.setFont(Fonts.latoLight(size * 0.2556391));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, 0.3056391, size); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void resizeTitleAndUnitText() {
        double maxWidth = 0.69548872 * size;
        double fontSize = size * 0.08082707;
        titleText.setFont(Fonts.latoBold(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, 0.13082707, size); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.22180451);
        unitText.setFont(Fonts.latoBold(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, 0.13082707, size); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.68984962);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            barBackground.setCenterX(size * 0.5);
            barBackground.setCenterY(size * 0.5);
            barBackground.setRadiusX(size * 0.48590226);
            barBackground.setRadiusY(size * 0.48590226);
            barBackground.setStrokeWidth(size * 0.02819549);

            bar.setCenterX(size * 0.5);
            bar.setCenterY(size * 0.5);
            bar.setRadiusX(size * 0.48590226);
            bar.setRadiusY(size * 0.48590226);
            bar.setStrokeWidth(size * 0.02819549);

            resizeTitleAndUnitText();
            resizeValueText();
        }
    }
}
