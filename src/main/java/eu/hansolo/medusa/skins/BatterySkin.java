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
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Locale;


/**
 * Created by hansolo on 25.01.16.
 */
public class BatterySkin extends GaugeSkinBase {
    private static final double PREFERRED_WIDTH  = 160;
    private static final double PREFERRED_HEIGHT = 160;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;
    private Orientation          orientation;
    private double               size;
    private Pane                 pane;
    private Path                 batteryBackground;
    private Path                 battery;
    private LinearGradient       batteryPaint;
    private Text                 valueText;
    private List<Section>        sections;
    private Locale               locale;
    private InvalidationListener currentValueListener;



    // ******************** Constructors **************************************
    public BatterySkin(Gauge gauge) {
        super(gauge);
        orientation          = gauge.getOrientation();
        sections             = gauge.getSections();
        locale               = gauge.getLocale();
        currentValueListener = o -> setBar(gauge.getCurrentValue());

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

        batteryBackground = new Path();
        batteryBackground.setFillRule(FillRule.EVEN_ODD);
        batteryBackground.setStroke(null);

        battery = new Path();
        battery.setFillRule(FillRule.EVEN_ODD);
        battery.setStroke(null);

        valueText = new Text(String.format(locale, "%.0f%%", gauge.getCurrentValue()));
        valueText.setVisible(gauge.isValueVisible());
        valueText.setManaged(gauge.isValueVisible());

        // Add all nodes
        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.getChildren().setAll(batteryBackground, battery, valueText);

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
        handleEvents("VISIBILITY");
    }


    // ******************** Methods *******************************************
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("RECALC".equals(EVENT_TYPE)) {
            setBar(gauge.getCurrentValue());
            resize();
            redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = gauge.getSections();
            resize();
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            valueText.setVisible(gauge.isValueVisible());
            valueText.setManaged(gauge.isValueVisible());
            redraw();
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        super.dispose();
    }


    // ******************** Private Methods ***********************************
    private void setBar(final double VALUE) {
        double factor = VALUE / gauge.getRange();
        Color barColor = gauge.getBarColor();
        if (gauge.isGradientBarEnabled() && !gauge.getGradientBarStops().isEmpty()) {
            barColor = gauge.getGradientLookup().getColorAt(factor);
        } else if (gauge.getSectionsVisible() && !sections.isEmpty()) {
            int listSize = sections.size();
            for (int i = 0 ; i < listSize ; i++) {
                if (sections.get(i).contains(VALUE)) {
                    barColor = sections.get(i).getColor();
                    break;
                }
            }
        }

        if (Orientation.HORIZONTAL == orientation) {
            batteryPaint = new LinearGradient(0, 0, size, 0, false, CycleMethod.NO_CYCLE,
                                              new Stop(0, barColor),
                                              new Stop(factor, barColor),
                                              new Stop(factor, Color.TRANSPARENT),
                                              new Stop(1, Color.TRANSPARENT));
        } else {
            batteryPaint = new LinearGradient(0, 0, 0, size, false, CycleMethod.NO_CYCLE,
                                              new Stop(0, Color.TRANSPARENT),
                                              new Stop(1 - factor, Color.TRANSPARENT),
                                              new Stop(1 - factor, barColor),
                                              new Stop(1, barColor));
        }
        battery.setFill(batteryPaint);

        valueText.setText(String.format(locale, "%.0f%%", factor * 100));
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }

    private Path createVerticalBattery(final Path PATH) {
        PATH.getElements().clear();
        PATH.getElements().add(new MoveTo(0.25 * size, 0.175 * size));
        PATH.getElements().add(new CubicCurveTo(0.25 * size, 0.175 * size,
                                                    0.25 * size, 0.925 * size,
                                                    0.25 * size, 0.925 * size));
        PATH.getElements().add(new CubicCurveTo(0.25 * size, 0.96875 * size,
                                                    0.28125 * size, size,
                                                    0.325 * size, size));
        PATH.getElements().add(new CubicCurveTo(0.325 * size, size,
                                                    0.675 * size, size,
                                                    0.675 * size, size));
        PATH.getElements().add(new CubicCurveTo(0.71875 * size, size,
                                                    0.75 * size, 0.96875 * size,
                                                    0.75 * size, 0.925 * size));
        PATH.getElements().add(new CubicCurveTo(0.75 * size, 0.925 * size,
                                                    0.75 * size, 0.175 * size,
                                                    0.75 * size, 0.175 * size));
        PATH.getElements().add(new CubicCurveTo(0.75 * size, 0.13125 * size,
                                                    0.71875 * size, 0.1 * size,
                                                    0.675 * size, 0.1 * size));
        PATH.getElements().add(new CubicCurveTo(0.675 * size, 0.1 * size,
                                                    0.6 * size, 0.1 * size,
                                                    0.6 * size, 0.1 * size));
        PATH.getElements().add(new LineTo(0.6 * size, 0.0));
        PATH.getElements().add(new LineTo(0.4 * size, 0.0));
        PATH.getElements().add(new LineTo(0.4 * size, 0.1 * size));
        PATH.getElements().add(new CubicCurveTo(0.4 * size, 0.1 * size,
                                                    0.325 * size, 0.1 * size,
                                                    0.325 * size, 0.1 * size));
        PATH.getElements().add(new CubicCurveTo(0.28125 * size, 0.1 * size,
                                                    0.25 * size, 0.13125 * size,
                                                    0.25 * size, 0.175 * size));
        PATH.getElements().add(new ClosePath());
        return PATH;
    }
    
    private Path createHorizontalBattery(final Path PATH) {
        PATH.getElements().clear();
        PATH.getElements().add(new MoveTo(0.825 * size, 0.25 * size));
        PATH.getElements().add(new CubicCurveTo(0.825 * size, 0.25 * size,
                                                      0.075 * size, 0.25 * size,
                                                      0.075 * size, 0.25 * size));
        PATH.getElements().add(new CubicCurveTo(0.03125 * size, 0.25 * size,
                                                      0.0, 0.28125 * size,
                                                      0.0, 0.325 * size));
        PATH.getElements().add(new CubicCurveTo(0.0, 0.325 * size,
                                                      0.0, 0.675 * size,
                                                      0.0, 0.675 * size));
        PATH.getElements().add(new CubicCurveTo(0.0, 0.71875 * size,
                                                      0.03125 * size, 0.75 * size,
                                                      0.075 * size, 0.75 * size));
        PATH.getElements().add(new CubicCurveTo(0.075 * size, 0.75 * size,
                                                      0.825 * size, 0.75 * size,
                                                      0.825 * size, 0.75 * size));
        PATH.getElements().add(new CubicCurveTo(0.86875 * size, 0.75 * size,
                                                      0.9 * size, 0.71875 * size,
                                                      0.9 * size, 0.675 * size));
        PATH.getElements().add(new CubicCurveTo(0.9 * size, 0.675 * size,
                                                      0.9 * size, 0.6 * size,
                                                      0.9 * size, 0.6 * size));
        PATH.getElements().add(new LineTo(size, 0.6 * size));
        PATH.getElements().add(new LineTo(size, 0.4 * size));
        PATH.getElements().add(new LineTo(0.9 * size, 0.4 * size));
        PATH.getElements().add(new CubicCurveTo(0.9 * size, 0.4 * size,
                                                      0.9 * size, 0.325 * size,
                                                      0.9 * size, 0.325 * size));
        PATH.getElements().add(new CubicCurveTo(0.9 * size, 0.28125 * size,
                                                      0.86875 * size, 0.25 * size,
                                                      0.825 * size, 0.25 * size));
        PATH.getElements().add(new ClosePath());
        return PATH;
    }
    
    @Override protected void resize() {
        double width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        double height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();

        size = width < height ? width : height;
        
        if (size > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((gauge.getWidth() - size) * 0.5, (gauge.getHeight() - size) * 0.5);

            valueText.setFont(Fonts.latoLight(0.175 * size));
            valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);

            orientation = gauge.getOrientation();
            if (Orientation.VERTICAL == orientation) {
                createVerticalBattery(batteryBackground);
                createVerticalBattery(battery);
            } else {
                createHorizontalBattery(batteryBackground);
                createHorizontalBattery(battery);
            }
            redraw();
        }
    }

    @Override protected void redraw() {
        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(gauge.getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(gauge.getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        locale = gauge.getLocale();

        Color barBackgroundColor = gauge.getBarBackgroundColor();
        batteryBackground.setFill(Color.color(barBackgroundColor.getRed(), barBackgroundColor.getGreen(), barBackgroundColor.getBlue(), 0.3));

        valueText.setFill(gauge.getValueColor());

        setBar(gauge.getCurrentValue());
    }
}
