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
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
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
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

import java.util.Locale;


/**
 * Created by hansolo on 27.04.16.
 */
public class WhiteSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private static final double ANGLE_RANGE      = 360;

    private double        size;
    private double        center;
    private DropShadow    shadow;
    private DropShadow    textShadow;
    private Arc           backgroundRing;
    private Arc           barBackground;
    private Arc           bar;
    private Text          valueText;
    private Text          unitText;
    private Pane          pane;
    private double        minValue;
    private double        range;
    private double        angleStep;
    private String        formatString;
    private Locale        locale;


    // ******************** Constructors **************************************
    public WhiteSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        minValue     = gauge.getMinValue();
        range        = gauge.getRange();
        angleStep    = ANGLE_RANGE / range;
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        locale       = gauge.getLocale();

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
        shadow     = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 12, 0, 3, 3);
        textShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 4, 0, 2, 2);

        valueText = new Text(String.format(Locale.US, "%.0f", getSkinnable().getValue()));
        valueText.setFill(Color.WHITE);
        valueText.setFont(Fonts.robotoBold(PREFERRED_WIDTH * 0.20625));
        valueText.setTextOrigin(VPos.CENTER);
        valueText.relocate(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.46875);
        valueText.setEffect(textShadow);
        Helper.enableNode(valueText, getSkinnable().isValueVisible());

        unitText  = new Text(getSkinnable().getUnit());
        unitText.setFill(Color.WHITE);
        unitText.setFont(Fonts.robotoBold(PREFERRED_WIDTH * 0.0875));
        unitText.setTextOrigin(VPos.CENTER);
        unitText.relocate(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.65625);
        unitText.setEffect(textShadow);
        Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());

        backgroundRing = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                                 PREFERRED_WIDTH * 0.43125, PREFERRED_HEIGHT * 0.43125,
                                 0, 360);
        backgroundRing.setFill(null);
        backgroundRing.setStroke(Color.rgb(255, 255, 255, 0.9));
        backgroundRing.setStrokeLineCap(StrokeLineCap.BUTT);
        backgroundRing.setStrokeWidth(PREFERRED_WIDTH * 0.1375);
        backgroundRing.setEffect(shadow);

        barBackground = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                                PREFERRED_WIDTH * 0.43125, PREFERRED_HEIGHT * 0.43125,
                                0, 360);
        barBackground.setFill(null);
        barBackground.setStroke(Color.rgb(255, 255, 255, 0.4));
        barBackground.setStrokeLineCap(StrokeLineCap.BUTT);
        barBackground.setStrokeWidth(PREFERRED_WIDTH * 0.1375);

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5,
                      PREFERRED_WIDTH * 0.43125, PREFERRED_HEIGHT * 0.43125,
                      90, -getSkinnable().getAngleStep() * getSkinnable().getValue());
        bar.setFill(null);
        bar.setStroke(Color.WHITE);
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.1375);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);

        pane = new Pane(valueText, unitText, backgroundRing, barBackground, bar);
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth()))));

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
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            Helper.enableNode(valueText, getSkinnable().isValueVisible());
            Helper.enableNode(unitText, !getSkinnable().getUnit().isEmpty());
        }
    }

    private void setBar(final double VALUE) {
        if (minValue > 0) {
            bar.setLength((minValue - VALUE) * angleStep);
        } else {
            bar.setLength(-VALUE * angleStep);
        }
        valueText.setText(String.format(locale, formatString, VALUE));
        resizeValueText();
    }


    // ******************** Resizing ******************************************
    private void resizeValueText() {
        double maxWidth = 0.5 * size;
        double fontSize = 0.20625 * size;
        valueText.setFont(Fonts.robotoBold(fontSize));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, fontSize); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * (unitText.getText().isEmpty() ? 0.5 : 0.42));
    }
    private void resizeUnitText() {
        double maxWidth = 0.56667 * size;
        double fontSize = 0.0875 * size;
        unitText.setFont(Fonts.robotoBold(fontSize));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.66);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            shadow.setRadius(size * 0.06);
            shadow.setOffsetX(size * 0.02);
            shadow.setOffsetY(size * 0.02);

            textShadow.setRadius(size * 0.0125);
            textShadow.setOffsetX(size * 0.00625);
            textShadow.setOffsetY(size * 0.00625);

            center = size * 0.5;

            valueText.setFont(Fonts.robotoBold(size * 0.20625));

            unitText.setFont(Fonts.robotoBold(size * 0.0875));

            Arc outerRing = new Arc(size * 0.5, size * 0.5,
                                    size * 0.43125, size * 0.43125,
                                    0, 360);
            outerRing.setFill(null);
            outerRing.setStroke(Color.WHITE);
            outerRing.setStrokeLineCap(StrokeLineCap.BUTT);
            outerRing.setStrokeWidth(size * 0.3);

            Arc innerRing = new Arc(size * 0.5, size * 0.5,
                                    size * 0.43125, size * 0.43125,
                                    0, 360);
            innerRing.setFill(null);
            innerRing.setStroke(Color.WHITE);
            innerRing.setStrokeLineCap(StrokeLineCap.BUTT);
            innerRing.setStrokeWidth(size * 0.1375);

            Shape shape = Shape.subtract(outerRing, innerRing);

            backgroundRing.setCenterX(center);
            backgroundRing.setCenterY(center);
            backgroundRing.setRadiusX(size * 0.43125);
            backgroundRing.setRadiusY(size * 0.43125);
            backgroundRing.setStrokeWidth(size * 0.1375);
            backgroundRing.setClip(shape);

            barBackground.setCenterX(center);
            barBackground.setCenterY(center);
            barBackground.setRadiusX(size * 0.43125);
            barBackground.setRadiusY(size * 0.43125);
            barBackground.setStrokeWidth(size * 0.1375);

            bar.setCenterX(center);
            bar.setCenterY(center);
            bar.setRadiusX(size * 0.43125);
            bar.setRadiusY(size * 0.43125);
            bar.setStrokeWidth(size * 0.1375);

            resizeValueText();
            resizeUnitText();
        }
    }

    private void redraw() {
        locale       = getSkinnable().getLocale();
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();

        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(getSkinnable().getBorderWidth() / PREFERRED_WIDTH * size))));

        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());

        unitText.setText(getSkinnable().getUnit());
        resizeUnitText();
    }
}
