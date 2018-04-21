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

import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.events.UpdateEvent;
import eu.hansolo.medusa.events.UpdateEventListener;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
                            .skinType(Gauge.SkinType.MODERN)
                            .title("Input")
                            .returnToZero(false)
                            .animated(true)
                            .animationDuration(25)
                            .smoothing(true)
                            .decimals(0)
                            .needleBehavior(Gauge.NeedleBehavior.STANDARD)
                            .prefHeight(200)
                            .barColor(Color.CORNFLOWERBLUE)
                            .build();

        /*
        gauge.currentValueProperty().addListener(o -> {
            double currentValue = gauge.getCurrentValue();
            if (currentValue > 3) {
                gauge.setBarColor(Color.rgb(200, 80, 0));
            } else if (currentValue < -3) {
                gauge.setBarColor(Color.rgb(0, 80, 200));
            } else {
                gauge.setBarColor(Color.rgb(0, 200, 0));
            }
        });
        */

        lastTimerCall = System.nanoTime();
        timer         = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 100_000_000l) {
                    double value = RND.nextDouble() * 100;
                    //System.out.println(value);
                    gauge.setValue(value);
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(gauge);
        pane.setPadding(new Insets(10));

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
