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

import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.Gauge.SkinType;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.time.LocalTime;
import java.time.ZonedDateTime;
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
    private              Clock          clock;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


    @Override public void init() {
        gauge = GaugeBuilder.create()
                            .skinType(SkinType.SECTION)
                            .prefSize(400, 400)
                            .needleColor(Color.ORANGE)
                            .minValue(0)
                            .maxValue(105)
                            .animated(true)
                            .highlightSections(true)
                            .sections(
                                SectionBuilder.create().start(0).stop(15).text("EXCELLENT").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(18, 158, 81)).textColor(Gauge.DARK_COLOR).build(),
                                SectionBuilder.create().start(15).stop(30).text("VERY\nGOOD").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(151, 208, 77)).textColor(Gauge.DARK_COLOR).build(),
                                SectionBuilder.create().start(30).stop(45).text("GOOD").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(197, 223, 0)).textColor(Gauge.DARK_COLOR).build(),
                                SectionBuilder.create().start(45).stop(60).text("FAIRLY\nGOOD").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(251, 245, 0)).textColor(Gauge.DARK_COLOR).build(),
                                SectionBuilder.create().start(60).stop(75).text("AVERAGE").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(247, 206, 0)).textColor(Gauge.DARK_COLOR).build(),
                                SectionBuilder.create().start(75).stop(90).text("BELOW\nAVERAGE").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(227, 124, 1)).textColor(Gauge.DARK_COLOR).build(),
                                SectionBuilder.create().start(90).stop(105).text("POOR").color(Color.rgb(223, 223, 223)).highlightColor(Color.rgb(223, 49, 23)).textColor(Gauge.DARK_COLOR).build())
                            .build();

        TimeSection gardenLightOn = TimeSectionBuilder.create()
                                                      .start(LocalTime.of(18, 00, 00))
                                                      .stop(LocalTime.of(22, 00, 00))
                                                      .color(Color.rgb(200, 100, 0, 0.1))
                                                      .highlightColor(Color.rgb(200, 100, 0, 0.75))
                                                      .onTimeSectionEntered(event -> System.out.println("Garden light on"))
                                                      .onTimeSectionLeft(event -> System.out.println("Garden light off"))
                                                      .build();

        TimeSection lunchBreak = TimeSectionBuilder.create()
                                                   .start(LocalTime.of(12, 00, 00))
                                                   .stop(LocalTime.of(13, 00, 00))
                                                   .color(Color.rgb(200, 0, 0, 0.1))
                                                   .highlightColor(Color.rgb(200, 0, 0, 0.75))
                                                   .build();

        clock = ClockBuilder.create()
                            //.skinType(ClockSkinType.SLIM)
                            .prefSize(400, 400)
                            .sectionsVisible(true)
                            .highlightSections(true)
                            .sections(gardenLightOn)
                            .areasVisible(true)
                            .areas(lunchBreak)
                            .running(true)
                            .build();

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
        StackPane pane = new StackPane(clock);
        pane.setPadding(new Insets(20));
        LinearGradient gradient = new LinearGradient(0, 0, 0, pane.getLayoutBounds().getHeight(),
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, Color.rgb(38, 38, 38)),
                                                     new Stop(1.0, Color.rgb(15, 15, 15)));
        //pane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        //pane.setBackground(new Background(new BackgroundFill(Color.rgb(39,44,50), CornerRadii.EMPTY, Insets.EMPTY)));
        //pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        //pane.setBackground(new Background(new BackgroundFill(Color.rgb(67,66,64), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("Medusa");
        stage.setScene(scene);
        stage.show();

        //gauge.setValue(105);
        //clock.setTime(ZonedDateTime.now().plusHours(2));

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
