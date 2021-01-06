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

import eu.hansolo.medusa.Clock.ClockSkinType;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.Marker.MarkerType;
import eu.hansolo.medusa.tools.GradientLookup;
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

import java.time.*;
import java.util.Random;


public class Demo extends Application {
    private static final Random         RND            = new Random();
    private static final double         MIN_CELL_SIZE  = 80;
    private static final double         PREF_CELL_SIZE = 120;
    private static final double         MAX_CELL_SIZE  = 150;
    private static       int            noOfNodes      = 0;
    private              Gauge          gauge1;
    private              Gauge          gauge2;
    private              Gauge          gauge3;
    private              Gauge          gauge4;
    private              Gauge          gauge5;
    private              Gauge          gauge6;
    private              Gauge          gauge7;
    private              Gauge          gauge8;
    private              Gauge          gauge9;
    private              Gauge          gauge10;
    private              Gauge          gauge11;
    private              Gauge          gauge12;
    private              Gauge          gauge13;
    private              Gauge          gauge14;
    private              Gauge          gauge15;
    private              Gauge          gauge16;
    private              Gauge          gauge17;
    private              Gauge          gauge18;
    private              Gauge          gauge19;
    private              Gauge          gauge20;
    private              Gauge          gauge21;
    private              Gauge          gauge22;
    private              Gauge          gauge23;
    private              Gauge          gauge24;
    private              Gauge          gauge25;
    private              Gauge          gauge26;
    private              Gauge          gauge27;
    private              Gauge          gauge28;
    private              Gauge          gauge29;
    private              Gauge          gauge30;
    private              Gauge          gauge31;
    private              Gauge          gauge32;
    private              Clock          clock1;
    private              Clock          clock2;
    private              Clock          clock3;
    private              Clock          clock4;
    private              Clock          clock5;
    private              Clock          clock6;
    private              Clock          clock7;
    private              Clock          clock8;
    private              Clock          clock9;
    private              Clock          clock10;
    private              Clock          clock11;
    private              Clock          clock12;
    private              long           lastTimerCall;
    private              AnimationTimer timer;


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
                             .majorTickMarkType(TickMarkType.TRAPEZOID)
                             .majorTickMarksVisible(true)
                             .mediumTickMarkType(TickMarkType.TRAPEZOID)
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
                             .sectionsVisible(true)
                             .title("Simple")
                             .threshold(50)
                             .animated(true)
                             .build();

        gauge4 = GaugeBuilder.create()
                             .skinType(SkinType.BULLET_CHART)
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
                             .animated(true)
                             .title("Dashboard")
                             .unit("\u00B0C")
                             .maxValue(40)
                             .barColor(Color.CRIMSON)
                             .valueColor(Color.WHITE)
                             .titleColor(Color.WHITE)
                             .unitColor(Color.WHITE)
                             .thresholdVisible(true)
                             .threshold(35)
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
                             .majorTickMarkType(TickMarkType.TRAPEZOID)
                             .mediumTickMarkType(TickMarkType.TRAPEZOID)
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
                             .highlightAreas(true)
                             .areas(new Section(1.5, 2, Color.rgb(200, 0, 0, 0.1), Color.rgb(255, 0, 0)))
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
                              .unit("\u00B0C")
                              .lcdDesign(LcdDesign.BLUE_LIGHTBLUE2)
                              .thresholdVisible(true)
                              .threshold(25)
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

        gauge22 = GaugeBuilder.create()
                              .skinType(SkinType.DIGITAL)
                              .foregroundBaseColor(Color.rgb(0, 222, 249))
                              .barColor(Color.rgb(0, 222, 249))
                              .title("SLIM")
                              .unit("UNIT")
                              .animated(true)
                              .build();

        gauge23 = GaugeBuilder.create()
                              .skinType(SkinType.SIMPLE_DIGITAL)
                              .foregroundBaseColor(Color.rgb(0, 249, 222))
                              .barColor(Color.rgb(0, 249, 222))
                              .unit("KPH")
                              .animated(true)
                              .build();

        gauge24 = GaugeBuilder.create()
                              .skinType(SkinType.SECTION)
                              .needleColor(Color.ORANGE)
                              .minValue(0)
                              .maxValue(105)
                              .animated(true)
                              .highlightSections(true)
                              .sections(
                                  SectionBuilder.create()
                                                .start(0)
                                                .stop(15)
                                                .text("EXCELLENT")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(18, 158, 81))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build(),
                                  SectionBuilder.create()
                                                .start(15)
                                                .stop(30)
                                                .text("VERY\nGOOD")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(151, 208, 77))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build(),
                                  SectionBuilder.create()
                                                .start(30)
                                                .stop(45)
                                                .text("GOOD")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(197, 223, 0))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build(),
                                  SectionBuilder.create()
                                                .start(45)
                                                .stop(60)
                                                .text("FAIRLY\nGOOD")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(251, 245, 0))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build(),
                                  SectionBuilder.create()
                                                .start(60)
                                                .stop(75)
                                                .text("AVERAGE")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(247, 206, 0))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build(),
                                  SectionBuilder.create()
                                                .start(75)
                                                .stop(90)
                                                .text("BELOW\nAVERAGE")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(227, 124, 1))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build(),
                                  SectionBuilder.create()
                                                .start(90)
                                                .stop(105)
                                                .text("POOR")
                                                .color(Color.rgb(223, 223, 223))
                                                .highlightColor(Color.rgb(223, 49, 23))
                                                .textColor(Gauge.DARK_COLOR)
                                                .build())
                              .build();

        gauge25 = GaugeBuilder.create()
                              .skinType(SkinType.BAR)
                              .barColor(Color.rgb(237, 22, 72))
                              .valueColor(Color.WHITE)
                              .unitColor(Color.WHITE)
                              .unit("KPH")
                              .animated(true)
                              .build();

        gauge26 = GaugeBuilder.create()
                              .skinType(SkinType.WHITE)
                              .unit("")
                              .build();

        gauge27 = GaugeBuilder.create()
                              .skinType(SkinType.CHARGE)
                              .build();
        GridPane.setColumnSpan(gauge27, 2);

        gauge28 = GaugeBuilder.create()
                              .skinType(SkinType.SIMPLE_SECTION)
                              .title("Title")
                              .unit("unit")
                              .titleColor(Color.WHITE)
                              .unitColor(Color.WHITE)
                              .valueColor(Color.WHITE)
                              .sections(new Section(0, 33, Color.LIME),
                                        new Section(33, 66, Color.YELLOW),
                                        new Section(66, 100, Color.CRIMSON))
                              .build();

        gauge29 = GaugeBuilder.create()
                              .skinType(SkinType.TILE_KPI)
                              .threshold(75)
                              .animated(true)
                              .build();

        gauge30 = GaugeBuilder.create()
                              .skinType(SkinType.TILE_TEXT_KPI)
                              .animated(true)
                              .build();

        gauge31 = GaugeBuilder.create()
                              .skinType(SkinType.TILE_SPARK_LINE)
                              .averageVisible(true)
                              .build();

        gauge32 = GaugeBuilder.create()
                              .skinType(SkinType.NASA)
                              .prefSize(364, 364)
                              .title("VELOCITY")
                              .unit("MP/H")
                              .decimals(0)
                              .maxValue(20000)
                              .gradientLookup(new GradientLookup(new Stop(0, Color.web("#B0BABE")),
                                                                 new Stop(0.2, Color.web("#B0BABE")),
                                                                 new Stop(0.5, Color.web("#FFBD5B")),
                                                                 new Stop(0.725, Color.web("#AC626B")),
                                                                 new Stop(1.0, Color.web("#CF0513"))))
                              .gradientBarEnabled(true)
                              .animated(true)
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
                             .title("Muenster")
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

        clock6 = ClockBuilder.create()
                             .skinType(ClockSkinType.FAT)
                             .backgroundPaint(Color.WHITE)
                             .hourTickMarkColor(Color.rgb(200, 200, 200))
                             .minuteTickMarkColor(Color.rgb(200, 200, 200))
                             .tickLabelColor(Color.rgb(200, 200, 200))
                             .running(true)
                             .build();

        clock7 = ClockBuilder.create()
                             .skinType(ClockSkinType.ROUND_LCD)
                             .hourColor(Color.rgb(38, 166, 154))
                             .minuteColor(Color.rgb(77, 182, 172))
                             .secondColor(Color.rgb(128, 203, 196))
                             .textColor(Color.rgb(128, 203, 196))
                             .dateColor(Color.rgb(128, 203, 196))
                             .running(true)
                             .build();

        clock8 = ClockBuilder.create()
                             .skinType(ClockSkinType.SLIM)
                             .running(true)
                             .build();

        clock9 = ClockBuilder.create()
                             .skinType(ClockSkinType.DIGITAL)
                             .running(true)
                             .textColor(Color.WHITE)
                             .dateColor(Color.LIGHTGRAY)
                             .build();

        clock10 = ClockBuilder.create()
                              .skinType(ClockSkinType.DESIGN)
                              .running(true)
                              .build();

        clock11 = ClockBuilder.create()
                              .skinType(ClockSkinType.INDUSTRIAL)
                              .running(true)
                              .build();

        clock12 = ClockBuilder.create()
                              .skinType(ClockSkinType.TILE)
                              .running(true)
                              .build();

        //framedGauge1 = new FGauge(gauge1, GaugeDesign.ENZO, GaugeBackground.DARK_GRAY);
        //framedGauge2 = new FGauge(gauge2, GaugeDesign.METAL);

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 3_000_000_000l) {
                    //framedGauge1.getGauge().setValue(RND.nextDouble() * gauge1.getRange() + gauge1.getMinValue());
                    //framedGauge2.getGauge().setValue(RND.nextDouble() * gauge2.getRange() + gauge2.getMinValue());
                    gauge3.setValue(RND.nextDouble() * gauge3.getRange() + gauge3.getMinValue());
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
                    gauge22.setValue(RND.nextDouble() * gauge22.getRange() + gauge22.getMinValue());
                    gauge23.setValue(RND.nextDouble() * gauge23.getRange() + gauge23.getMinValue());
                    gauge24.setValue(RND.nextDouble() * gauge24.getRange() + gauge24.getMinValue());
                    gauge25.setValue(RND.nextDouble() * gauge25.getRange() + gauge25.getMinValue());
                    gauge26.setValue(RND.nextDouble() * gauge26.getRange() + gauge26.getMinValue());
                    gauge27.setValue(RND.nextDouble() * gauge27.getRange() + gauge27.getMinValue());
                    gauge28.setValue(RND.nextDouble() * gauge28.getRange() + gauge28.getMinValue());
                    gauge29.setValue(RND.nextDouble() * gauge29.getRange() + gauge29.getMinValue());
                    gauge30.setValue(RND.nextDouble() * gauge30.getRange() + gauge30.getMinValue());
                    gauge31.setValue(RND.nextDouble() * gauge31.getRange() + gauge31.getMinValue());
                    gauge32.setValue(RND.nextDouble() * gauge32.getRange() + gauge32.getMinValue());
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        GridPane pane = new GridPane();
        pane.add(gauge1, 0, 0);
        pane.add(gauge2, 1, 0);
        pane.add(gauge3, 2, 0);
        pane.add(gauge4, 3, 0);
        pane.add(gauge5, 4, 0);
        pane.add(clock1, 5, 0);
        pane.add(clock5, 6, 0);
        pane.add(gauge22, 7, 0);
        pane.add(gauge29, 8, 0);

        pane.add(gauge6, 0, 1);
        pane.add(gauge7, 1, 1);
        pane.add(gauge8, 2, 1);
        pane.add(gauge9, 3, 1);
        pane.add(gauge10, 4, 1);
        pane.add(clock2, 5, 1);
        pane.add(gauge21, 6, 1);
        pane.add(gauge23, 7, 1);
        pane.add(gauge30, 8, 1);

        pane.add(gauge11, 0, 2);
        pane.add(gauge12, 1, 2);
        pane.add(gauge13, 2, 2);
        pane.add(gauge14, 3, 2);
        pane.add(gauge15, 4, 2);
        pane.add(clock3, 5, 2);
        pane.add(clock6, 6, 2);
        pane.add(clock8, 7, 2);
        pane.add(gauge31, 8, 2);

        pane.add(gauge16, 0, 3);
        pane.add(gauge17, 1, 3);
        pane.add(gauge18, 2, 3);
        pane.add(gauge19, 3, 3);
        pane.add(gauge20, 4, 3);
        pane.add(clock4, 5, 3);
        pane.add(clock7, 6, 3);
        pane.add(gauge24, 7, 3);
        pane.add(clock12, 8, 3);

        pane.add(gauge25, 0, 4);
        pane.add(gauge26, 1, 4);
        pane.add(gauge27, 2, 4);
        pane.add(gauge28, 4, 4);
        pane.add(clock9, 5, 4);
        pane.add(clock10, 6, 4);
        pane.add(clock11, 7, 4);
        pane.add(gauge32, 8, 4);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(10));
        for (int i = 0 ; i < 9 ; i++) {
            pane.getColumnConstraints().add(new ColumnConstraints(MIN_CELL_SIZE, PREF_CELL_SIZE, MAX_CELL_SIZE));
        }
        for (int i = 0 ; i < 5 ; i++) {
            pane.getRowConstraints().add(new RowConstraints(MIN_CELL_SIZE, PREF_CELL_SIZE, MAX_CELL_SIZE));
        }
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
