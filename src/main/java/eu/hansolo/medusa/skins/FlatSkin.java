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
import java.util.List;
import java.util.Locale;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

import static eu.hansolo.medusa.tools.Helper.formatNumber;


/**
 * Created by hansolo on 06.01.16.
 */
public class FlatSkin extends GaugeSkinBase {
    private static final double  ANGLE_RANGE      = 360;
    private double               size;
    private Circle               colorRing;
    private Arc                  bar;
    private Line                 separator;
    private Text                 titleText;
    private Text                 valueText;
    private Text                 unitText;
    private Pane                 pane;
    private double               minValue;
    private double               range;
    private double               angleStep;
    private boolean              colorGradientEnabled;
    private int                  noOfGradientStops;
    private boolean              sectionsVisible;
    private List<Section>        sections;
    private InvalidationListener currentValueListener;


    // ******************** Constructors **************************************
    public FlatSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        minValue             = gauge.getMinValue();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        currentValueListener = o -> setBar(gauge.getCurrentValue());

        initGraphics();
        registerListeners();

        setBar(gauge.getCurrentValue());
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

        colorRing = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.5);
        colorRing.setFill(Color.TRANSPARENT);
        colorRing.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        colorRing.setStroke(gauge.getBarColor());

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(gauge.getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.15);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        separator = new Line(PREFERRED_WIDTH * 0.5, 1, PREFERRED_WIDTH * 0.5, 0.16667 * PREFERRED_HEIGHT);
        separator.setStroke(gauge.getBorderPaint());
        separator.setFill(Color.TRANSPARENT);

        titleText = new Text(gauge.getTitle());
        titleText.setFont(Fonts.robotoLight(PREFERRED_WIDTH * 0.08));
        titleText.setFill(gauge.getTitleColor());
        Helper.enableNode(titleText, !gauge.getTitle().isEmpty());

        valueText = new Text(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), gauge.getCurrentValue()));
        valueText.setFont(Fonts.robotoRegular(PREFERRED_WIDTH * 0.27333));
        valueText.setFill(gauge.getValueColor());
        Helper.enableNode(valueText, gauge.isValueVisible());

        unitText = new Text(gauge.getUnit());
        unitText.setFont(Fonts.robotoLight(PREFERRED_WIDTH * 0.08));
        unitText.setFill(gauge.getUnitColor());
        Helper.enableNode(unitText, !gauge.getUnit().isEmpty());

        pane = new Pane(colorRing, bar, separator, titleText, valueText, unitText);
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth()))));

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
            minValue  = gauge.getMinValue();
            range     = gauge.getRange();
            angleStep = ANGLE_RANGE / range;
            sections  = gauge.getSections();
            redraw();
            setBar(gauge.getCurrentValue());
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(titleText, !gauge.getTitle().isEmpty());
            Helper.enableNode(unitText, !gauge.getUnit().isEmpty());
            Helper.enableNode(valueText, gauge.isValueVisible());
        }
    }

    private void setBar( final double VALUE ) {
        double barLength = 0;
        double min = gauge.getMinValue();
        double max = gauge.getMaxValue();
        double clampedValue = Helper.clamp(min, max, VALUE);

        if ( gauge.isStartFromZero() ) {
            if ( ( VALUE > min || min < 0 ) && ( VALUE < max || max > 0 ) ) {
                if ( max < 0 ) {
                    barLength = ( max - clampedValue ) * angleStep;
                } else if ( min > 0 ) {
                    barLength = ( min - clampedValue ) * angleStep;
                } else {
                    barLength = - clampedValue * angleStep;
                }
            }
        } else {
            barLength = ( min - clampedValue ) * angleStep;
        }

        bar.setLength(barLength);

        setBarColor(VALUE);
        valueText.setText(formatNumber(gauge.getLocale(), gauge.getFormatString(), gauge.getDecimals(), VALUE));
        resizeValueText();

    }

    private void setBarColor( final double VALUE ) {
        if (!sectionsVisible && !colorGradientEnabled) {
            bar.setStroke(gauge.getBarColor());
            colorRing.setStroke(gauge.getBarColor());
        } else if (colorGradientEnabled && noOfGradientStops > 1) {
            Color dynamicColor = gauge.getGradientLookup().getColorAt((VALUE - minValue) / range);
            bar.setStroke(dynamicColor);
            colorRing.setStroke(dynamicColor);
        } else {
            bar.setStroke(gauge.getBarColor());
            colorRing.setStroke(gauge.getBarColor());
            for (Section section : sections) {
                if (section.contains(VALUE)) {
                    bar.setStroke(section.getColor());
                    colorRing.setStroke(section.getColor());
                    break;
                }
            }
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }
    

    // ******************** Resizing ******************************************
    private void resizeTitleText() {
        double maxWidth = 0.56667 * size;
        double fontSize = 0.08 * size;
        titleText.setFont(Fonts.robotoLight(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.225);
    }
    private void resizeValueText() {
        double maxWidth = 0.5 * size;
        double fontSize = 0.3 * size;
        valueText.setFont(Fonts.robotoRegular(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void resizeUnitText() {
        double maxWidth = 0.56667 * size;
        double fontSize = 0.08 * size;
        unitText.setFont(Fonts.robotoLight(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.66);
    }

    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            colorRing.setCenterX(size * 0.5);
            colorRing.setCenterY(size * 0.5);
            colorRing.setRadius(size * 0.5);
            colorRing.setStrokeWidth(size * 0.0075);
            colorRing.setStrokeType(StrokeType.INSIDE);

            bar.setCenterX(size * 0.5);
            bar.setCenterY(size * 0.5);
            bar.setRadiusX(size * 0.4135);
            bar.setRadiusY(size * 0.4135);
            bar.setStrokeWidth(size * 0.12);

            separator.setStartX(size * 0.5);
            separator.setStartY(size * 0.0275);
            separator.setEndX(size * 0.5);
            separator.setEndY(size * 0.145);

            resizeTitleText();
            resizeValueText();
            resizeUnitText();
        }
    }

    @Override protected void redraw() {
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();

        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(gauge.getBorderWidth() / PREFERRED_WIDTH * size))));

        setBarColor(gauge.getCurrentValue());
        valueText.setFill(gauge.getValueColor());
        unitText.setFill(gauge.getUnitColor());
        titleText.setFill(gauge.getTitleColor());
        separator.setStroke(gauge.getBorderPaint());

        titleText.setText(gauge.getTitle());
        resizeTitleText();

        unitText.setText(gauge.getUnit());
        resizeUnitText();
    }
}
