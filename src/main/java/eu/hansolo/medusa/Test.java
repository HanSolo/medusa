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

package eu.hansolo.medusa;

import eu.hansolo.medusa.Gauge.ButtonEvent;
import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.LcdFont;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.TickLabelLocation;
import eu.hansolo.medusa.Gauge.TickLabelOrientation;
import eu.hansolo.medusa.Gauge.TickMarkType;
import eu.hansolo.medusa.GaugeDesign.GaugeBackground;
import eu.hansolo.medusa.Marker.MarkerType;
import eu.hansolo.medusa.skins.BulletChartSkin;
import eu.hansolo.medusa.skins.FlatSkin;
import eu.hansolo.medusa.skins.ModernSkin;
import eu.hansolo.medusa.skins.SimpleSkin;
import eu.hansolo.medusa.skins.SpaceXSkin;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.awt.*;
import java.util.Random;


/**
 * User: hansolo
 * Date: 04.01.16
 * Time: 06:31
 */
public class Test extends Application {
    private static final Random         RND = new Random();
    private static       int            noOfNodes = 0;
    private              Gauge          gauge;
    private              FGauge         framedGauge;
    private              long           lastTimerCall;
    private              AnimationTimer timer;

    @Override public void init() {
        gauge = GaugeBuilder.create()
                            .prefSize(500,500)
                            .backgroundPaint(Color.rgb(90,90,90))
                            //.zeroColor(Color.YELLOW)
                            .mediumTickMarksVisible(false)
                            .minorTickMarksVisible(false)
                            .tickMarkColor(Color.WHITE)
                            .tickLabelColor(Color.WHITE)
                            .titleColor(Color.WHITE)
                            .unitColor(Color.WHITE)
                            .valueColor(Color.WHITE)
                            .majorTickMarkType(TickMarkType.TRIANGLE)
                            .sectionsVisible(true)
                            .sections(new Section(80, 100, Color.ORANGE))
                            //.areasVisible(true)
                            .areas(new Section(90, 100, Color.rgb(200, 0, 0, 0.75)))
                            .thresholdVisible(true)
                            .threshold(50)
                            .checkThreshold(true)
                            .onThresholdExceeded(e -> gauge.setLedBlinking(true))
                            .onThresholdUnderrun(e -> gauge.setLedBlinking(false))
                            .lcdVisible(true)
                            .lcdDesign(LcdDesign.FLAT_ORANGE)
                            .lcdFont(LcdFont.STANDARD)
                            .ledVisible(true)
                            //.shadowsEnabled(true)
                            .needleShape(NeedleShape.FLAT)
                            .knobType(KnobType.FLAT)
                            .knobColor(Color.ORANGE)
                            .ledType(LedType.FLAT)
                            .ledColor(Color.ORANGE)
                            .interactive(true)
                            .onButtonPressed(e -> System.out.println("Pressed"))
                            .onButtonReleased(e -> System.out.println("Released"))
                            .animated(true)
                            .build();

        GaugeDesign.FLAT.frameColor = Color.ORANGE;
        framedGauge = new FGauge(gauge, GaugeDesign.FLAT, GaugeBackground.TRANSPARENT);
        framedGauge.setForegroundVisible(false);

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 3_000_000_000l) {
                    gauge.setValue(RND.nextDouble() * gauge.getRange() + gauge.getMinValue());
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(framedGauge);
        pane.setPadding(new Insets(10));
        //pane.setBackground(new Background(new BackgroundFill(Color.rgb(50, 50, 50), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("Medusa");
        stage.setScene(scene);
        stage.show();

        //gauge.setNeedleColor(Color.LIME);

        // Calculate number of nodes
        calcNoOfNodes(gauge);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    // ******************** Misc **********************************************
    private static void calcNoOfNodes(Node node) {
        if (node instanceof Parent) {
            if (((Parent) node).getChildrenUnmodifiable().size() != 0) {
                ObservableList<Node> tempChildren = ((Parent) node).getChildrenUnmodifiable();
                noOfNodes += tempChildren.size();
                tempChildren.forEach(n -> calcNoOfNodes(n));
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
