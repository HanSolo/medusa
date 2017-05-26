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
import eu.hansolo.medusa.events.UpdateEvent;
import eu.hansolo.medusa.events.UpdateEvent.EventType;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;



/**
 * User: hansolo
 * Date: 04.01.16
 * Time: 06:31
 */
public class Test extends Application {
    private static final Random          RND       = new Random();
    private static       int             noOfNodes = 0;
    private              Gauge           gauge1;
    private              Gauge           gauge2;
    private              long            lastTimerCall;
    private              AnimationTimer  timer;


    @Override public void init() {
        gauge1 = GaugeBuilder.create()
                             .skinType(SkinType.SIMPLE_SECTION)
                             .decimals(2)
                             .minValue(0)
                             .maxValue(120)
                             .animated(true)
                             .build();

        gauge2 = GaugeBuilder.create()
                             .skinType(SkinType.SIMPLE_SECTION)
                             .decimals(2)
                             .minValue(0)
                             .maxValue(120)
                             .animated(true)
                             .build();


        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 2_000_000_000l) {
                    double v1 = gauge1.getRange() * ( RND.nextDouble() * 1.3 - 0.15 ) + gauge1.getMinValue();
                    double v2 = gauge2.getRange() * ( RND.nextDouble() * 1.3 - 0.15 ) + gauge2.getMinValue();
                    gauge1.setValue(v1);
                    gauge2.setValue(v2);

                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        VBox pane = new VBox(gauge1, gauge2);
        pane.setPadding(new Insets(20));
        pane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Scene scene = new Scene(pane);
        scene.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        stage.setTitle("Medusa");
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
