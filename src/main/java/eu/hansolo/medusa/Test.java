/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2021 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.medusa;

import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.Marker.MarkerType;
import eu.hansolo.medusa.tools.GradientLookup;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.util.Random;


public class Test extends Application {
    private static final Random         RND       = new Random();
    private static       int            noOfNodes = 0;
    private              Gauge          gauge;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


    @Override public void init() {
        gauge = GaugeBuilder.create()
                            .skinType(SkinType.SIMPLE_DIGITAL)
                            .prefSize(400, 400)
                            .foregroundBaseColor(Color.web("#ebeefd"))
                            .barBackgroundColor(Color.web("#262c49"))
                            .decimals(2)
                            .maxValue(200)
                            .arcExtend(5)
                            .gradientLookup(new GradientLookup(new Stop(0, Color.web("#8f62cb")),
                                                               new Stop(0.25, Color.web("#7367f0")),
                                                               new Stop(0.5, Color.web("#ad5ea5")),
                                                               new Stop(0.75, Color.web("#e85456")),
                                                               new Stop(1.0, Color.web("#e85456").brighter())))
                            .gradientBarEnabled(true)
                            .animated(true)
                            .build();

        gauge = GaugeBuilder.create()
                    .minValue(0)
                    .maxValue(1)
                    .tickLabelDecimals(1)
                    .decimals(2)
                    .autoScale(true)
                    .animated(true)
                    //.backgroundPaint(Color.TRANSPARENT)
                    //.borderPaint(Color.LIGHTGRAY)
                    //.knobColor(Color.rgb(0, 90, 120))
                    .shadowsEnabled(true)
                    //.tickLabelColor(Color.rgb(0, 175, 248))
                    //.ledColor(Color.rgb(0, 175, 248))
                    .ledVisible(true)
                    .ledBlinking(true)
                    .sectionsVisible(true)
                    .sections(new Section(0.5, 0.75, Color.rgb(139, 195, 102, 0.5)))
                    .areasVisible(true)
                    .areas(new Section(0.75, 1.0, Color.rgb(234, 83, 79, 0.5)))
                    .majorTickMarkColor(Color.MAGENTA)
                    //.minorTickMarkColor(Color.rgb(0, 175, 248))
                    .majorTickMarkType(TickMarkType.TRAPEZOID)
                    .mediumTickMarkType(TickMarkType.DOT)
                    .minorTickMarkType(TickMarkType.LINE)
                    .tickLabelOrientation(TickLabelOrientation.ORTHOGONAL)
                    .tickMarkSections(new Section(0.25, 0.5, Color.rgb(241, 161, 71)))
                    .tickMarkSectionsVisible(true)
                    .markers(new Marker(0.5, "", Color.CYAN, MarkerType.TRIANGLE))
                    .markersVisible(true)
                    //.majorTickMarksVisible(true)
                    //.minorTickMarksVisible(true)
                    .tickLabelLocation(TickLabelLocation.INSIDE)
                    //.tickLabelsVisible(true)
                    .tickLabelSections(new Section(0.1, 0.3, Color.rgb(0, 175, 248)))
                    //.tickLabelSectionsVisible(true)
                    .title("Title")
                    //.titleColor(Color.rgb(223, 223, 223))
                    .unit("Unit")
                    .lcdDesign(LcdDesign.SECTIONS)
                    .lcdVisible(true)
                    .lcdFont(LcdFont.STANDARD)
                    //.unitColor(Color.rgb(223, 223, 223))
                    //.valueColor(Color.rgb(223, 223, 223))
                    .needleSize(NeedleSize.THICK)
                    .build();

        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 2_000_000_000l) {
                    double value = RND.nextDouble() * gauge.getRange() + gauge.getMinValue();
                    gauge.setValue(value);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(gauge);
        pane.setPadding(new Insets(10));
        pane.setBackground(new Background(new BackgroundFill(Color.web("#10163a"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("Test");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(pane);
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
                for (Node n : tempChildren) { calcNoOfNodes(n); }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
