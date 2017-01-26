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

import eu.hansolo.medusa.Gauge;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;


/**
 * Created by hansolo on 08.07.16.
 */
public class ChargeSkin extends GaugeSkinBase {
    private enum BarColor {
        GRAY(Color.rgb(243, 243, 243), Color.rgb(143, 143, 143)),
        RED(Color.rgb(243, 0, 6), Color.rgb(143, 0, 9)),
        ORANGE(Color.rgb(253, 148, 21), Color.rgb(253, 97, 8)),
        GREEN(Color.rgb(110, 190, 5), Color.rgb(50, 177, 7));

        public final Color COLOR_FROM;
        public final Color COLOR_TO;

        BarColor(final Color COLOR_FROM, final Color COLOR_TO) {
            this.COLOR_FROM = COLOR_FROM;
            this.COLOR_TO   = COLOR_TO;
        }
    }
    protected static final double         PREFERRED_WIDTH  = 306;
    protected static final double         PREFERRED_HEIGHT = 66;
    protected static final double         MINIMUM_WIDTH    = 153;
    protected static final double         MINIMUM_HEIGHT   = 33;
    protected static final double         MAXIMUM_WIDTH    = 918;
    protected static final double         MAXIMUM_HEIGHT   = 198;
    private static double                 aspectRatio      = PREFERRED_HEIGHT / PREFERRED_WIDTH;
    private        Region[]               bars;
    private        Background[]           barBackgrounds;
    private        Border                 barBorder;
    private        double                 width;
    private        double                 height;
    private        HBox                   pane;
    private        Paint                  backgroundPaint;
    private        Paint                  borderPaint;
    private        double                 borderWidth;
    private        InvalidationListener   currentValueListener;
    private        ChangeListener<Number> paneWidthListener;


    // ******************** Constructors **************************************
    public ChargeSkin(Gauge gauge) {
        super(gauge);
        backgroundPaint      = Color.TRANSPARENT;
        borderPaint          = Color.TRANSPARENT;
        borderWidth          = 0;
        bars                 = new Region[12];
        barBackgrounds       = new Background[24];
        currentValueListener = o -> handleEvents("VALUE");
        paneWidthListener    = (o, ov, nv) -> { if (ov.intValue() == 0 && nv.intValue() > 0) Platform.runLater(() -> resize()); };

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

        for (int i = 0 ; i < 12 ; i++) {
            Region bar = new Region();
            bar.setPrefSize(20, 20 + (i * 4));
            bars[i] = bar;
        }

        pane = new HBox(bars);
        pane.setSpacing(PREFERRED_WIDTH * 0.01960784);
        pane.setAlignment(Pos.BOTTOM_CENTER);
        pane.setFillHeight(false);
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        getChildren().setAll(pane);
    }

    @Override protected void registerListeners() {
        super.registerListeners();
        gauge.currentValueProperty().addListener(currentValueListener);
        pane.widthProperty().addListener(paneWidthListener);
    }


    // ******************** Methods *******************************************
    @Override public void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        if(Double.compare(bars[0].getLayoutBounds().getWidth(), 0) == 0) resize();
    }
    
    @Override protected void handleEvents(final String EVENT_TYPE) {
        super.handleEvents(EVENT_TYPE);
        if ("VALUE".equals(EVENT_TYPE)) {
            redraw();
        }
    }

    @Override public void dispose() {
        gauge.currentValueProperty().removeListener(currentValueListener);
        pane.widthProperty().removeListener(paneWidthListener);
        super.dispose();
    }


    // ******************** Resizing ******************************************
    @Override protected void resize() {
        width  = gauge.getWidth() - gauge.getInsets().getLeft() - gauge.getInsets().getRight();
        height = gauge.getHeight() - gauge.getInsets().getTop() - gauge.getInsets().getBottom();

        if (aspectRatio * width > height) {
            width = 1 / (aspectRatio / height);
        } else if (1 / (aspectRatio / height) > width) {
            height = aspectRatio * width;
        }

        if (width > 0 && height > 0) {
            pane.setMaxSize(width, height);
            pane.setPrefSize(width, height);
            pane.relocate((gauge.getWidth() - width) * 0.5, (gauge.getHeight() - height) * 0.5);
            pane.setSpacing(width * 0.01960784);

            double barWidth = 0;
            for (int i = 0 ; i < 12 ; i++) {
                bars[i].setPrefSize(0.3030303 * height, (0.3030303 * height + i * 0.06060606 * height));
                Bounds bounds = bars[i].getLayoutBounds();
                barWidth      = bounds.getWidth();
                if (barWidth == 0) return;

                BarColor barColor;
                if (i < 2) {
                    barColor = BarColor.RED;
                } else if (i < 9) {
                    barColor = BarColor.ORANGE;
                } else {
                    barColor = BarColor.GREEN;
                }
                barBackgrounds[i] = new Background(new BackgroundFill[] {
                    new BackgroundFill(Color.WHITE, new CornerRadii(1024), Insets.EMPTY),
                    new BackgroundFill(new LinearGradient(0, bounds.getMinY(), 0, bounds.getMaxY(), false, CycleMethod.NO_CYCLE, new Stop(0.0, barColor.COLOR_FROM), new Stop(1.0, barColor.COLOR_TO)), new CornerRadii(1024), new Insets(0.15 * barWidth))
                });
                barBackgrounds[i + 12] = new Background(new BackgroundFill[] {
                    new BackgroundFill(Color.WHITE, new CornerRadii(1024), Insets.EMPTY),
                    new BackgroundFill(new LinearGradient(0, bounds.getMinY(), 0, bounds.getMaxY(), false, CycleMethod.NO_CYCLE, new Stop(0.0, BarColor.GRAY.COLOR_FROM), new Stop(1.0, BarColor.GRAY.COLOR_TO)), new CornerRadii(1024), new Insets(0.15 * barWidth))
                });
            }
            barBorder = new Border(new BorderStroke(Color.rgb(102, 102, 102), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(0.05 * barWidth)));
        }
        redraw();
    }

    @Override protected void redraw() {
        int chargedBars = (int) (gauge.getCurrentValue() * 13);
        for (int i = 0 ; i < 12 ; i++) {
            bars[i].setBackground(i < chargedBars ? barBackgrounds[i] : barBackgrounds[i + 12]);
            bars[i].setBorder(barBorder);
        }
    }
}
