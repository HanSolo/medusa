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

package eu.hansolo.medusa.demos;

import eu.hansolo.medusa.Clock;
import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.ClockBuilder;
import eu.hansolo.medusa.FGauge;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.GaugeDesign;
import eu.hansolo.medusa.GaugeDesign.GaugeBackground;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Marker.MarkerType;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.medusa.TimeSection;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.Random;


/**
 * Created by hansolo on 11.12.15.
 */
public class OverviewDemo extends Application {
    private static final Random         RND       = new Random();
    private static       int            noOfNodes = 0;
    private FGauge         framedGauge1;
    private Gauge          gauge1;
    private FGauge         framedGauge2;
    private Gauge          gauge2;
    private Gauge          gauge3;
    private Gauge          gauge4;
    private Gauge          gauge5;
    private Gauge          gauge6;
    private Gauge          gauge7;
    private Gauge          gauge8;
    private Gauge          gauge9;
    private Gauge          gauge10;
    private Gauge          gauge11;
    private Gauge          gauge12;
    private Gauge          gauge13;
    private Gauge          gauge14;
    private Gauge          gauge15;
    private Gauge          gauge16;
    private Gauge          gauge17;
    private Gauge          gauge18;
    private Gauge          gauge19;
    private Gauge          gauge20;
    private Gauge          gauge21;
    private Clock          clock1;
    private Clock          clock2;
    private Clock          clock3;
    private Clock          clock4;
    private Clock          clock5;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        gauge1 = GaugeBuilder.create()
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

        gauge2 = GaugeBuilder.create()
                             .minValue(0)
                             .maxValue(10)
                             .animated(true)
                             .autoScale(true)
                             .shadowsEnabled(true)
                             .ledColor(Color.rgb(250, 50, 0))
                             .gradientBarEnabled(true)
                             .gradientBarStops(new Stop(0.0, Color.rgb(0, 0, 255, 0.7)),
                                               new Stop(0.5, Color.rgb(0, 200, 255, 0.7)),
                                               new Stop(1.0, Color.rgb(0, 255, 0, 0.7)))
                             .majorTickMarkType(TickMarkType.TRIANGLE)
                             .majorTickMarksVisible(true)
                             .mediumTickMarkType(TickMarkType.TRIANGLE)
                             .mediumTickMarksVisible(true)
                             .minorTickMarkType(TickMarkType.LINE)
                             .minorTickMarksVisible(false)
                             .tickLabelsVisible(true)
                             .tickLabelOrientation(TickLabelOrientation.HORIZONTAL)
                             .tickLabelLocation(TickLabelLocation.INSIDE)
                             .title("Title")
                             .unit("Unit")
                             .foregroundBaseColor(Gauge.BRIGHT_COLOR)
                             .build();

        gauge3 = GaugeBuilder.create()
                             .skinType(SkinType.SIMPLE)
                             .sections(new Section(0, 16.66666, "0", Color.web("#11632f")),
                                       new Section(16.66666, 33.33333, "1", Color.web("#36843d")),
                                       new Section(33.33333, 50.0, "2", Color.web("#67a328")),
                                       new Section(50.0, 66.66666, "3", Color.web("#80b940")),
                                       new Section(66.66666, 83.33333, "4", Color.web("#95c262")),
                                       new Section(83.33333, 100.0, "5", Color.web("#badf8d")))
                             .title("Simple")
                             .threshold(50)
                             .animated(true)
                             .build();

        gauge4 = GaugeBuilder.create()
                             .skinType(SkinType.BULLET_CHART)
                             .backgroundPaint(Color.rgb(204, 204, 204))
                             .sections(new Section(0, 16.66666, "0", Color.web("#11632f")),
                                       new Section(16.66666, 33.33333, "1", Color.web("#36843d")),
                                       new Section(33.33333, 50.0, "2", Color.web("#67a328")),
                                       new Section(50.0, 66.66666, "3", Color.web("#80b940")),
                                       new Section(66.66666, 83.33333, "4", Color.web("#95c262")),
                                       new Section(83.33333, 100.0, "5", Color.web("#badf8d")))
                             .title("BulletChart")
                             .unit("US$ in thousands")
                             .threshold(50)
                             .orientation(Orientation.VERTICAL)
                             .animated(true)
                             .titleColor(Color.WHITE)
                             .unitColor(Color.WHITE)
                             .tickLabelColor(Color.WHITE)
                             .tickMarkColor(Color.WHITE)
                             .build();

        gauge5 = GaugeBuilder.create()
                             .skinType(SkinType.DASHBOARD)
                             .backgroundPaint(Color.web("#eff3f3"))
                             .animated(true)
                             .title("Dashboard")
                             .unit("°C")
                             .maxValue(40)
                             .barColor(Color.CRIMSON)
                             .valueColor(Color.WHITE)
                             .titleColor(Color.WHITE)
                             .unitColor(Color.WHITE)
                             .shadowsEnabled(true)
                             .gradientBarEnabled(true)
                             .gradientBarStops(new Stop(0.00, Color.BLUE),
                                               new Stop(0.25, Color.CYAN),
                                               new Stop(0.50, Color.LIME),
                                               new Stop(0.75, Color.YELLOW),
                                               new Stop(1.00, Color.RED))
                             .build();

        gauge6 = GaugeBuilder.create()
                             .skinType(SkinType.SPACE_X)
                             .animated(true)
                             .decimals(0)
                             .title("SpaceX")
                             .unit("km/h")
                             .maxValue(30000)
                             .threshold(25000)
                             .build();

        gauge7 = GaugeBuilder.create()
                             .skinType(SkinType.AMP)
                             .prefSize(250, 210)
                             .minValue(-1)
                             .maxValue(1)
                             .tickLabelDecimals(1)
                             .decimals(2)
                             .startFromZero(true)
                             .title("Amp")
                             .unit("db")
                             .sections(new Section(80, 100, Color.CRIMSON))
                             .sectionsVisible(true)
                             .animated(true)
                             .build();

        gauge8 = GaugeBuilder.create()
                             .skinType(SkinType.MODERN)
                             .prefSize(400, 400)
                             .sections(new Section(85, 90, "", Color.rgb(204, 0, 0, 0.5)),
                                       new Section(90, 95, "", Color.rgb(204, 0, 0, 0.75)),
                                       new Section(95, 100, "", Color.rgb(204, 0, 0)))
                             .sectionTextVisible(true)
                             .title("MODERN")
                             .unit("UNIT")
                             .threshold(85)
                             .thresholdVisible(true)
                             .animated(true)
                             .build();

        gauge9 = GaugeBuilder.create()
                             .minValue(-1)
                             .maxValue(2)
                             .tickLabelDecimals(1)
                             .autoScale(true)
                             .animated(true)
                             .startAngle(0)
                             .angleRange(270)
                             .threshold(0.5)
                             .thresholdVisible(true)
                             .majorTickMarkType(TickMarkType.TRIANGLE)
                             .mediumTickMarkType(TickMarkType.TRIANGLE)
                             .tickLabelColor(Color.WHITE)
                             .tickMarkColor(Color.WHITE)
                             .titleColor(Color.WHITE)
                             .subTitleColor(Color.WHITE)
                             .unitColor(Color.WHITE)
                             .zeroColor(Color.LIGHTSKYBLUE)
                             .lcdVisible(true)
                             .lcdDesign(LcdDesign.FLAT_CUSTOM)
                             .title("Title")
                             .subTitle("SubTitle")
                             .unit("Unit")
                             .interactive(true)
                             .buttonTooltipText("Test")
                             .needleSize(NeedleSize.THIN)
                             .tickLabelLocation(TickLabelLocation.OUTSIDE)
                             .onButtonPressed(event -> System.out.println("Button pressed"))
                             .onButtonReleased(event -> System.out.println("Button released"))
                             .scaleDirection(ScaleDirection.COUNTER_CLOCKWISE)
                             .sectionsVisible(true)
                             .sections(new Section(0, 1, Color.rgb(200, 150, 0, 0.5)))
                             .areasVisible(true)
                             .areas(new Section(1.5, 2, Color.rgb(200, 0, 0, 0.5)))
                             .markersVisible(true)
                             //.markersInterActive(true)
                             .markers(new Marker(1.75, Color.LIME))
                             .needleColor(Color.CYAN)
                             .build();

        gauge10 = GaugeBuilder.create()
                              .skinType(SkinType.FLAT)
                              .title("Flat")
                              .unit("Unit")
                              .foregroundBaseColor(Color.WHITE)
                              .animated(true)
                              .build();

        gauge11 = GaugeBuilder.create()
                              .skinType(SkinType.SLIM)
                              .animated(true)
                              .maxValue(10000)
                              .decimals(0)
                              .unit("STEPS")
                              .build();

        gauge12 = GaugeBuilder.create()
                              .skinType(SkinType.KPI)
                              .foregroundBaseColor(Color.WHITE)
                              .needleColor(Color.WHITE)
                              .animated(true)
                              .threshold(75)
                              .title("TITLE")
                              .build();

        gauge13 = GaugeBuilder.create()
                              .skinType(SkinType.INDICATOR)
                              .animated(true)
                              .build();

        gauge14 = GaugeBuilder.create()
                              .skinType(SkinType.QUARTER)
                              .animated(true)
                              .foregroundBaseColor(Color.WHITE)
                              .build();

        gauge15 = GaugeBuilder.create()
                              .skinType(SkinType.HORIZONTAL)
                              .animated(true)
                              .foregroundBaseColor(Color.WHITE)
                              .build();

        gauge16 = GaugeBuilder.create()
                              .skinType(SkinType.VERTICAL)
                              .animated(true)
                              .foregroundBaseColor(Color.WHITE)
                              .build();

        gauge17 = GaugeBuilder.create()
                              .skinType(SkinType.LCD)
                              .animated(true)
                              .title("Temperature")
                              .subTitle("Office")
                              .unit("°C")
                              .lcdDesign(LcdDesign.STANDARD_GREEN)
                              .build();

        gauge18 = GaugeBuilder.create()
                              .skinType(SkinType.TINY)
                              .animated(true)
                              .minValue(0)
                              .sections(new Section(0, 33, Color.rgb(0, 200, 0, 0.75)),
                                        new Section(33, 66, Color.rgb(200, 200, 0, 0.75)),
                                        new Section(66, 100, Color.rgb(200, 0, 0, 0.75)))
                              .build();

        gauge19 = GaugeBuilder.create()
                              .skinType(SkinType.BATTERY)
                              .animated(true)
                              .sectionsVisible(true)
                              .sections(new Section(0, 10, Color.rgb(200, 0, 0, 0.8)),
                                        new Section(10, 30, Color.rgb(200, 200, 0, 0.8)),
                                        new Section(30, 100, Color.rgb(0, 200, 0, 0.8)))
                              .build();

        gauge20 = GaugeBuilder.create()
                              .skinType(SkinType.LEVEL)
                              .title("Capacity")
                              .titleColor(Color.WHITE)
                              .animated(true)
                              .gradientBarEnabled(true)
                              .gradientBarStops(new Stop(0.0, Color.RED),
                                                new Stop(0.25, Color.ORANGE),
                                                new Stop(0.5, Color.YELLOW),
                                                new Stop(0.75, Color.YELLOWGREEN),
                                                new Stop(1.0, Color.LIME))
                              .build();

        gauge21 = GaugeBuilder.create()
                              .skinType(SkinType.LINEAR)
                              .title("Linear")
                              .orientation(Orientation.HORIZONTAL)
                              .sectionsVisible(true)
                              .foregroundBaseColor(Color.WHITE)
                              .barColor(Color.LIGHTSKYBLUE)
                              .sections(new Section(0, 20, Color.BLUE),
                                        new Section(80, 100, Color.RED))
                              .build();

        clock1 = ClockBuilder.create()
                             .skinType(ClockSkinType.YOTA2)
                             .sectionsVisible(true)
                             .sections(new TimeSection(LocalTime.of(8, 0, 0), LocalTime.of(12, 0, 0), Color.rgb(0, 200, 0, 0.5)),
                                       new TimeSection(LocalTime.of(13, 0, 0), LocalTime.of(17, 0, 0), Color.rgb(0, 200, 0, 0.5)))
                             .running(true)
                             .build();

        clock2 = ClockBuilder.create()
                             .skinType(ClockSkinType.LCD)
                             .lcdDesign(LcdDesign.DARKBLUE)
                             .title("Münster")
                             .titleVisible(true)
                             .secondsVisible(true)
                             .alarmsEnabled(true)
                             .dateVisible(true)
                             .running(true)
                             .build();

        clock3 = ClockBuilder.create()
                             .skinType(ClockSkinType.PEAR)
                             .running(true)
                             .build();

        clock4 = ClockBuilder.create()
                             .skinType(ClockSkinType.PLAIN)
                             .areasVisible(true)
                             .areas(new TimeSection(LocalTime.of(9, 0, 0), LocalTime.of(12, 0, 0), Color.rgb(255, 0, 255, 0.3)))
                             .running(true)
                             .build();

        clock5 = ClockBuilder.create()
                             .skinType(ClockSkinType.DB)
                             .backgroundPaint(Color.WHITE)
                             .running(true)
                             .build();

        framedGauge1 = new FGauge(gauge1, GaugeDesign.ENZO, GaugeBackground.DARK_GRAY);

        framedGauge2 = new FGauge(gauge2, GaugeDesign.METAL);

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 3_000_000_000l) {
                    framedGauge1.getGauge().setValue(RND.nextDouble() * gauge1.getRange() + gauge1.getMinValue());
                    framedGauge2.getGauge().setValue(RND.nextDouble() * gauge2.getRange() + gauge2.getMinValue());
                    gauge3.setValue(RND.nextDouble() * gauge3.getRange() + gauge4.getMinValue());
                    gauge4.setValue(RND.nextDouble() * gauge4.getRange() + gauge4.getMinValue());
                    gauge5.setValue(RND.nextDouble() * gauge5.getRange() + gauge5.getMinValue());
                    gauge6.setValue(RND.nextDouble() * gauge6.getRange() + gauge6.getMinValue());
                    gauge7.setValue(RND.nextDouble() * gauge7.getRange() + gauge7.getMinValue());
                    gauge8.setValue(RND.nextDouble() * gauge8.getRange() + gauge8.getMinValue());
                    gauge9.setValue(RND.nextDouble() * gauge9.getRange() + gauge9.getMinValue());
                    gauge10.setValue(RND.nextDouble() * gauge10.getRange() + gauge10.getMinValue());
                    gauge11.setValue(RND.nextDouble() * gauge11.getRange() + gauge11.getMinValue());
                    gauge12.setValue(RND.nextDouble() * gauge12.getRange() + gauge12.getMinValue());
                    gauge13.setValue(RND.nextDouble() * gauge13.getRange() + gauge13.getMinValue());
                    gauge14.setValue(RND.nextDouble() * gauge14.getRange() + gauge14.getMinValue());
                    gauge15.setValue(RND.nextDouble() * gauge15.getRange() + gauge15.getMinValue());
                    gauge16.setValue(RND.nextDouble() * gauge16.getRange() + gauge16.getMinValue());
                    gauge17.setValue(RND.nextDouble() * gauge17.getRange() + gauge17.getMinValue());
                    gauge18.setValue(RND.nextDouble() * gauge18.getRange() + gauge18.getMinValue());
                    gauge19.setValue(RND.nextDouble() * gauge19.getRange() + gauge19.getMinValue());
                    gauge20.setValue(RND.nextDouble() * gauge20.getRange() + gauge20.getMinValue());
                    gauge21.setValue(RND.nextDouble() * gauge21.getRange() + gauge21.getMinValue());
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        GridPane pane = new GridPane();
        pane.add(framedGauge1, 0, 0);
        pane.add(framedGauge2, 1, 0);
        pane.add(gauge3, 2, 0);
        pane.add(gauge4, 3, 0);
        pane.add(gauge5, 4, 0);
        pane.add(clock1, 5, 0);
        pane.add(clock5, 6, 0);
        pane.add(gauge6, 0, 1);
        pane.add(gauge7, 1, 1);
        pane.add(gauge8, 2, 1);
        pane.add(gauge9, 3, 1);
        pane.add(gauge10, 4, 1);
        pane.add(clock2, 5, 1);
        pane.add(gauge21, 6, 1);
        pane.add(gauge11, 0, 2);
        pane.add(gauge12, 1, 2);
        pane.add(gauge13, 2, 2);
        pane.add(gauge14, 3, 2);
        pane.add(gauge15, 4, 2);
        pane.add(clock3, 5, 2);
        pane.add(gauge16, 0, 3);
        pane.add(gauge17, 1, 3);
        pane.add(gauge18, 2, 3);
        pane.add(gauge19, 3, 3);
        pane.add(gauge20, 4, 3);
        pane.add(clock4, 5, 3);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(10));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getColumnConstraints().add(new ColumnConstraints(180));
        pane.getRowConstraints().add(new RowConstraints(180));
        pane.getRowConstraints().add(new RowConstraints(180));
        pane.getRowConstraints().add(new RowConstraints(180));
        pane.getRowConstraints().add(new RowConstraints(180));
        pane.getRowConstraints().add(new RowConstraints(180));
        pane.getRowConstraints().add(new RowConstraints(180));
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(90, 90, 90), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);

        stage.setTitle("Medusa Gauges and Clocks");
        stage.setScene(scene);
        stage.show();

        timer.start();

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");
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
