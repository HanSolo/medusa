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

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.LcdFont;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.Gauge.TickLabelLocation;
import eu.hansolo.medusa.Gauge.TickLabelOrientation;
import eu.hansolo.medusa.Gauge.TickMarkType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Marker.MarkerType;
import eu.hansolo.medusa.MarkerBuilder;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.SectionBuilder;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

import java.util.Random;


/**
 * This demo shows the usage of the standard Medusa gauge
 */
public class GaugeDemo extends Application {
    private static final Random RND = new Random();
    private              Gauge  gauge;
    private              Button button;

    @Override public void init() {
        /**
         *  Definition of Sections that can be used for
         *  - Sections
         *  - Areas
         *  - TickMarkSections
         *  - TickLabelSections
         *
         *  Sections will be checked against current value if gauge.getCheckSectionsForValue == true
         */
        Section section1 = SectionBuilder.create()
                                         .start(50)
                                         .stop(75)
                                         .color(Color.rgb(255, 200, 0))
                                         .onSectionEntered(sectionEvent -> System.out.println("Entered Section 1"))
                                         .onSectionLeft(sectionEvent -> System.out.println("Left Section 1"))
                                         .build();

        Section section2 = SectionBuilder.create()
                                         .start(75)
                                         .stop(100)
                                         .color(Color.rgb(255, 0, 0))
                                         .onSectionEntered(sectionEvent -> System.out.println("Entered Section 2"))
                                         .onSectionLeft(sectionEvent -> System.out.println("Left Section 2"))
                                         .build();

        /**
         *  Definition of Markers
         *  You can attach a listener for the following event types
         *  - MARKER_PRESSED
         *  - MARKER_RELEASED
         *  - MARKER_EXCEEDED
         *  - MARKER_UNDERRUN
         *  via the MarkerBuilder
         *  To receive MARKER_EXCEEDED and MARKER_UNDERRUN events you have to check the
         *  current value against each marker by calling the markers checkForValue() method
         */
        Marker marker1 = MarkerBuilder.create()
                                      .value(25)
                                      .text("Marker 1")
                                      .color(Color.HOTPINK)
                                      .markerType(MarkerType.DOT)
                                      .onMarkerPressed(markerEvent -> System.out.println("Marker 1 pressed"))
                                      .onMarkerReleased(markerEvent -> System.out.println("Marker 1 released"))
                                      .build();

        Marker marker2 = MarkerBuilder.create()
                                      .value(75)
                                      .text("Marker 2")
                                      .color(Color.PURPLE)
                                      .markerType(MarkerType.STANDARD)
                                      .onMarkerPressed(markerEvent -> System.out.println("Marker 2 pressed"))
                                      .onMarkerReleased(markerEvent -> System.out.println("Marker 2 released"))
                                      .build();

        /**
         *  Create the Medusa Gauge with most of it's options
         *  Simply play with the boolean variables first to see what mean.
         */
        gauge = GaugeBuilder.create()
                            .prefSize(500,500)                                                                  // Preferred size of the control
                            // Related to Foreground Elements
                            .foregroundBaseColor(Color.BLACK)                                                   // Color for title, subtitle, unit, value, tick label, zeroColor, tick mark, major tick mark, medium tick mark and minor tick mark
                            // Related to Title Text
                            .title("Title")                                                                     // Text for title
                            .titleColor(Color.BLACK)                                                            // Color for title text
                            // Related to Sub Title Text
                            .subTitle("SubTitle")                                                               // Text for subtitle
                            .subTitleColor(Color.BLACK)                                                         // Color for subtitle text
                            // Related to Unit Text
                            .unit("Unit")                                                                       // Text for unit
                            .unitColor(Color.BLACK)                                                             // Color for unit text
                            // Related to Value Text
                            .valueColor(Color.BLACK)                                                            // Color for value text
                            .decimals(0)                                                                        // Number of decimals for the value/lcd text
                            // Related to LCD
                            .lcdVisible(false)                                                                  // LCD instead of the plain value text
                            .lcdDesign(LcdDesign.STANDARD)                                                      // Design for LCD
                            .lcdFont(LcdFont.DIGITAL_BOLD)                                                      // Font for LCD (STANDARD, LCD, DIGITAL, DIGITAL_BOLD, ELEKTRA)
                            // Related to scale
                            .scaleDirection(ScaleDirection.CLOCKWISE)                                           // Direction of Scale (CLOCKWISE, COUNTER_CLOCKWISE)
                            .minValue(0)                                                                        // Start value of Scale
                            .maxValue(100)                                                                      // End value of Scale
                            .startAngle(320)                                                                    // Start angle of Scale (bottom -> 0, direction -> CCW)
                            .angleRange(280)                                                                    // Angle range of Scale starting from the start angle
                            // Related to Tick Labels
                            .tickLabelDecimals(0)                                                               // Number of decimals for tick labels
                            .tickLabelLocation(TickLabelLocation.INSIDE)                                        // Should tick labels be inside or outside Scale (INSIDE, OUTSIDE)
                            .tickLabelOrientation(TickLabelOrientation.HORIZONTAL)                              // Orientation of tick labels (ORTHOGONAL,  HORIZONTAL, TANGENT)
                            .onlyFirstAndLastTickLabelVisible(false)                                            // Should only the first and last tick label be visible
                            .tickLabelSectionsVisible(false)                                                    // Should sections for tick labels be visible
                            .tickLabelSections(section1, section2)                                              // Sections to color tick labels
                            .tickLabelColor(Color.BLACK)                                                        // Color for tick labels (overriden by tick label sections)
                            // Related to Tick Marks
                            .tickMarkSectionsVisible(false)                                                     // Should sections for tick marks be visible
                            .tickMarkSections(section1, section2)                                               // Sections to color tick marks
                            // Related to Major Tick Marks
                            .majorTickMarksVisible(true)                                                        // Should major tick marks be visible
                            .majorTickMarkType(TickMarkType.LINE)                                               // Tick mark type for major tick marks (LINE, DOT, TRIANGLE, TICK_LABEL)
                            .majorTickMarkColor(Color.BLACK)                                                    // Color for major tick marks (overriden by tick mark sections)
                            // Related to Medium Tick Marks
                            .mediumTickMarksVisible(true)                                                       // Should medium tick marks be visible
                            .mediumTickMarkType(TickMarkType.LINE)                                              // Tick mark type for medium tick marks (LINE, DOT, TRIANGLE)
                            .mediumTickMarkColor(Color.BLACK)                                                   // Color for medium tick marks (overriden by tick mark sections)
                            // Related to Minor Tick Marks
                            .minorTickMarksVisible(true)                                                        // Should minor tick marks be visible
                            .minorTickMarkType(TickMarkType.LINE)                                               // Tick mark type for minor tick marks (LINE, DOT, TRIANGLE)
                            .minorTickMarkColor(Color.BLACK)                                                    // Color for minor tick marks (override by tick mark sections)
                            // Related to LED
                            .ledVisible(false)                                                                  // Should LED be visible
                            .ledType(LedType.STANDARD)                                                          // Type of the LED (STANDARD, FLAT)
                            .ledColor(Color.rgb(255, 200, 0))                                                   // Color of LED
                            .ledBlinking(false)                                                                 // Should LED blink
                            .ledOn(false)                                                                       // LED on or off
                            // Related to Needle
                            .needleShape(NeedleShape.ANGLED)                                                    // Shape of needle (ANGLED, ROUND, FLAT)
                            .needleSize(NeedleSize.STANDARD)                                                    // Size of needle (THIN, STANDARD, THICK)
                            .needleColor(Color.CRIMSON)                                                         // Color of needle
                            // Related to Needle behavior
                            .startFromZero(false)                                                               // Should needle start from the 0 value
                            .returnToZero(false)                                                                // Should needle return to the 0 value (only makes sense when animated==true)
                            // Related to Knob
                            .knobType(KnobType.STANDARD)                                                        // Type for center knob (STANDARD, PLAIN, METAL, FLAT)
                            .knobColor(Color.LIGHTGRAY)                                                         // Color of center knob
                            .interactive(false)                                                                 // Should center knob be act as button
                            .onButtonPressed(buttonEvent -> System.out.println("Knob pressed"))                 // Handler (triggered when the center knob was pressed)
                            .onButtonReleased(buttonEvent -> System.out.println("Knob released"))               // Handler (triggered when the center knob was released)
                            // Related to Threshold
                            .thresholdVisible(false)                                                            // Should threshold indicator be visible
                            .threshold(50)                                                                      // Value of threshold
                            .thresholdColor(Color.RED)                                                          // Color of threshold indicator
                            .checkThreshold(false)                                                              // Should each value be checked against threshold
                            .onThresholdExceeded(thresholdEvent -> System.out.println("Threshold exceeded"))    // Handler (triggered if checkThreshold==true and the threshold is exceeded)
                            .onThresholdUnderrun(thresholdEvent -> System.out.println("Threshold underrun"))    // Handler (triggered if checkThreshold==true and the threshold is underrun)
                            // Related to Gradient Bar
                            .colorGradientEnabled(false)                                                        // Should gradient filled bar be visible to visualize a range
                            .gradientLookupStops(new Stop(0.0, Color.BLUE),                                     // Color gradient that will be use to color fill bar
                                                 new Stop(0.25, Color.CYAN),
                                                 new Stop(0.5, Color.LIME),
                                                 new Stop(0.75, Color.YELLOW),
                                                 new Stop(1.0, Color.RED))
                            // Related to Sections
                            .sectionsVisible(false)                                                             // Should sections be visible
                            .sections(section1, section2)                                                       // Sections that will be drawn (won't be drawn if colorGradientEnabled==true)
                            .checkSectionsForValue(false)                                                       // Should each section be checked against current value (if true section events will be fired)
                            // Related to Areas
                            .areasVisible(false)                                                                // Should areas be visible
                            .areas(section1, section2)                                                          // Areas that will be drawn
                            // Related to Markers
                            .markersVisible(false)                                                              // Should markers be visible
                            .markers(marker1, marker2)                                                          // Markers that will be drawn
                            // Related to Value
                            .animated(false)                                                                    // Should needle be animated
                            .animationDuration(500)                                                             // Speed of needle in milliseconds (10 - 10000 ms)
                            .onValueChanged(o -> System.out.println(((DoubleProperty) o).get()))                // InvalidationListener (triggered each time the value changed)
                            .build();

        button = new Button("Set Value");
        button.setOnMousePressed(event -> gauge.setValue(RND.nextDouble() * gauge.getRange() + gauge.getMinValue()));
    }

    @Override public void start(Stage stage) {
        HBox pane = new HBox(gauge, button);
        pane.setPadding(new Insets(10));
        pane.setSpacing(20);

        Scene scene = new Scene(pane);

        stage.setTitle("Gauge Demo");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
