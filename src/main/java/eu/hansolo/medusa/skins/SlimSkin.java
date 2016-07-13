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
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
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
    private double        minValue;
    private double        range;
    private double        angleStep;
    private boolean       colorGradientEnabled;
    private int           noOfGradientStops;
    private boolean       sectionsVisible;
    private List<Section> sections;
    private String        formatString;
    private Locale        locale;


    // ******************** Constructors **************************************
    public SlimSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        minValue             = gauge.getMinValue();
        range                = gauge.getRange();
        angleStep            = ANGLE_RANGE / range;
        colorGradientEnabled = gauge.isGradientBarEnabled();
        noOfGradientStops    = gauge.getGradientBarStops().size();
        sectionsVisible      = gauge.getSectionsVisible();
        sections             = gauge.getSections();
        formatString         = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale               = gauge.getLocale();

        init();
        initGraphics();
        registerListeners();

        setBar(gauge.getCurrentValue());
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
        Helper.enableNode(titleText, !getSkinnable().getSubTitle().isEmpty());

        valueText = new Text(String.format(locale, formatString, getSkinnable().getCurrentValue()));
        valueText.setFill(getSkinnable().getValueColor());
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFill(getSkinnable().getUnitColor());
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        pane = new Pane(barBackground, bar, titleText, valueText, unitText);
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

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
            redraw();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            minValue  = getSkinnable().getMinValue();
            range     = getSkinnable().getRange();
            angleStep = ANGLE_RANGE / range;
            sections  = getSkinnable().getSections();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(titleText, !getSkinnable().getTitle().isEmpty());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
        }
    }

    private void setBar(final double VALUE) {
        if (minValue > 0) {
            bar.setLength((minValue - VALUE) * angleStep);
        } else {
            bar.setLength(-VALUE * angleStep);
        }
        setBarColor(VALUE);
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeValueText();
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
    private void resizeValueText() {
        double maxWidth = size * 0.86466165;
        double fontSize = size * 0.2556391;
        valueText.setFont(Fonts.latoLight(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void resizeStaticText() {
        double maxWidth = size * 0.69548872;
        double fontSize = size * 0.08082707;
        titleText.setFont(Fonts.latoBold(fontSize));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.22180451);
        unitText.setFont(Fonts.latoBold(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
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

            resizeStaticText();
            resizeValueText();
        }
    }

    private void redraw() {
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        locale               = getSkinnable().getLocale();
        formatString         = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        colorGradientEnabled = getSkinnable().isGradientBarEnabled();
        noOfGradientStops    = getSkinnable().getGradientBarStops().size();
        sectionsVisible      = getSkinnable().getSectionsVisible();

        titleText.setText(getSkinnable().getTitle());
        unitText.setText(getSkinnable().getUnit());
        resizeStaticText();

        barBackground.setStroke(getSkinnable().getBarBackgroundColor());
        setBarColor(getSkinnable().getCurrentValue());
        titleText.setFill(getSkinnable().getTitleColor());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
    }
}
