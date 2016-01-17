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
import eu.hansolo.medusa.Gauge.ThresholdEvent;
import eu.hansolo.medusa.Gauge.TickLabelLocation;
import eu.hansolo.medusa.Gauge.TickLabelOrientation;
import eu.hansolo.medusa.Gauge.TickMarkType;
import eu.hansolo.medusa.GaugeDesign.GaugeBackground;
import eu.hansolo.medusa.Marker.MarkerType;
import eu.hansolo.medusa.skins.AmpSkin;
import eu.hansolo.medusa.skins.BulletChartSkin;
import eu.hansolo.medusa.skins.DashboardSkin;
import eu.hansolo.medusa.skins.FlatSkin;
import eu.hansolo.medusa.skins.IndicatorSkin;
import eu.hansolo.medusa.skins.KpiSkin;
import eu.hansolo.medusa.skins.ModernSkin;
import eu.hansolo.medusa.skins.SimpleSkin;
import eu.hansolo.medusa.skins.SlimSkin;
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
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.awt.*;
import java.util.Random;

import static javafx.scene.paint.Color.RED;


/**
 * User: hansolo
 * Date: 04.01.16
 * Time: 06:31
 */
public class Test extends Application {
    private static final Random         RND = new Random();
    private static       int            noOfNodes = 0;
    private              Gauge          gauge;
    private              long           lastTimerCall;
    private              AnimationTimer timer;

    @Override public void init() {
        /*
        gauge = GaugeBuilder.create()
                            .skin(IndicatorSkin.class)
                            //.prefSize(500,500)
                            .animated(true)
                            //.valueVisible(true)
                            .colorGradientEnabled(false)
                            .sectionsVisible(true)
                            .sections(new Section(0, 33, Color.rgb(34,180,11)),
                                      new Section(33, 66, Color.rgb(255,146,0)),
                                      new Section(66, 100, Color.rgb(255,0,39)))
                            .build();
        */

        gauge = GaugeBuilder.create()
                             .minValue(0)
                             .maxValue(1)
                             .tickLabelDecimals(1)
                             .decimals(2)
                             .autoScale(true)
                             .animated(true)
                             .returnToZero(true)
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
                             .majorTickMarkType(TickMarkType.TRIANGLE)
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
        StackPane pane = new StackPane(gauge);
        pane.setPadding(new Insets(10));
        //pane.setBackground(new Background(new BackgroundFill(Color.rgb(39,44,50), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("Medusa");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(gauge);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        //gauge.setValue(50);

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
