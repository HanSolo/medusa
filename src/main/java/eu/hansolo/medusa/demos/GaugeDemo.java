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
                            .prefSize(500,500)                                                                  // Set the preferred size of the control
                            // Related to Foreground Elements
                            .foregroundBaseColor(Color.BLACK)                                                   // Defines a color for title, subtitle, unit, value, tick label, tick mark, major tick mark, medium tick mark and minor tick mark
                            // Related to Title Text
                            .title("Title")                                                                     // Set the text for the title
                            .titleColor(Color.BLACK)                                                            // Define the color for the title text
                            // Related to Sub Title Text
                            .subTitle("SubTitle")                                                               // Set the text for the subtitle
                            .subTitleColor(Color.BLACK)                                                         // Define the color for the subtitle text
                            // Related to Unit Text
                            .unit("Unit")                                                                       // Set the text for the unit
                            .unitColor(Color.BLACK)                                                             // Define the color for the unit
                            // Related to Value Text
                            .valueColor(Color.BLACK)                                                            // Define the color for the value text
                            .decimals(0)                                                                        // Set the number of decimals for the value/lcd text
                            // Related to LCD
                            .lcdVisible(false)                                                                   // Display a LCD instead of the plain value text
                            .lcdDesign(LcdDesign.STANDARD)                                                      // Set the design for the LCD
                            .lcdFont(LcdFont.DIGITAL_BOLD)                                                      // Set the font for the LCD (STANDARD, LCD, DIGITAL, DIGITAL_BOLD, ELEKTRA)
                            // Related to scale
                            .scaleDirection(ScaleDirection.CLOCKWISE)                                           // Define the direction of the Scale (CLOCKWISE, COUNTER_CLOCKWISE)
                            .minValue(0)                                                                        // Set the start value of the scale
                            .maxValue(100)                                                                      // Set the end value of the scale
                            .startAngle(320)                                                                    // Set the start angle of your scale (bottom -> 0, direction -> CCW)
                            .angleRange(280)                                                                    // Set the angle range of your scale starting from the start angle
                            // Related to Tick Labels
                            .tickLabelDecimals(0)                                                               // Set the number of decimals for the tick labels
                            .tickLabelLocation(TickLabelLocation.INSIDE)                                        // Define wether the tick labels should be inside or outside the scale (INSIDE, OUTSIDE)
                            .tickLabelOrientation(TickLabelOrientation.HORIZONTAL)                              // Define the orientation of the tick labels (ORTHOGONAL,  HORIZONTAL, TANGENT)
                            .onlyFirstAndLastTickLabelVisible(false)                                            // Define if only the first and last tick label should be visible
                            .tickLabelSectionsVisible(false)                                                    // Define if sections for tick labels should be visible
                            .tickLabelSections(section1, section2)                                              // Define sections to color tick labels
                            .tickLabelColor(Color.BLACK)                                                        // Define the color for tick labels (overriden by tick label sections)
                            // Related to Tick Marks
                            .tickMarkSectionsVisible(false)                                                     // Define if sections for tick marks should be visible
                            .tickMarkSections(section1, section2)                                               // Define sections to color tick marks
                            // Related to Major Tick Marks
                            .majorTickMarksVisible(true)                                                        // Define if major tick marks should be visible
                            .majorTickMarkType(TickMarkType.LINE)                                               // Define the tick mark type for major tick marks (LINE, DOT, TRIANGLE, TICK_LABEL)
                            .majorTickMarkColor(Color.BLACK)                                                    // Define the color for the major tick marks (overriden by tick mark sections)
                            // Related to Medium Tick Marks
                            .mediumTickMarksVisible(true)                                                       // Define if medium tick marks should be visible
                            .mediumTickMarkType(TickMarkType.LINE)                                              // Define the tick mark type for medium tick marks (LINE, DOT, TRIANGLE)
                            .mediumTickMarkColor(Color.BLACK)                                                   // Define the color for the medium tick marks (overriden by tick mark sections)
                            // Related to Minor Tick Marks
                            .minorTickMarksVisible(true)                                                        // Define if minor tick marks should be visible
                            .minorTickMarkType(TickMarkType.LINE)                                               // Define the tick mark type for minor tick marks (LINE, DOT, TRIANGLE)
                            .minorTickMarkColor(Color.BLACK)                                                    // Define the color for minor tick marks (override by tick mark sections)
                            // Related to LED
                            .ledVisible(false)                                                                  // Defines if the LED should be visible
                            .ledType(LedType.STANDARD)                                                          // Defines the type of the LED (STANDARD, FLAT)
                            .ledColor(Color.rgb(255, 200, 0))                                                   // Defines the color of the LED
                            .ledBlinking(false)                                                                 // Defines if the LED should blink
                            // Related to Needle
                            .needleShape(NeedleShape.ANGLED)                                                    // Defines the shape of the needle (ANGLED, ROUND, FLAT)
                            .needleSize(NeedleSize.STANDARD)                                                    // Defines the size of the needle (THIN, STANDARD, THICK)
                            .needleColor(Color.CRIMSON)                                                         // Defines the color of the needle
                            // Related to Needle behavior
                            .startFromZero(false)                                                               // Defines if the needle should start from the 0 value
                            .returnToZero(false)                                                                // Defines if the needle should always return to the 0 value (only makes sense when animated==true)
                            // Related to Knob
                            .knobType(KnobType.STANDARD)                                                        // Defines the type for the center knob (STANDARD, PLAIN, METAL, FLAT)
                            .knobColor(Color.LIGHTGRAY)                                                         // Defines the color that should be used for the center knob
                            .interactive(false)                                                                 // Defines if it should be possible to press the center knob
                            .onButtonPressed(buttonEvent -> System.out.println("Knob pressed"))                 // Defines a handler that will be triggered when the center knob was pressed
                            .onButtonReleased(buttonEvent -> System.out.println("Knob released"))               // Defines a handler that will be triggered when the center knob was released
                            // Related to Threshold
                            .thresholdVisible(false)                                                            // Defines if the threshold indicator should be visible
                            .threshold(50)                                                                      // Defines the value for the threshold
                            .thresholdColor(Color.RED)                                                          // Defines the color for the threshold
                            .checkThreshold(false)                                                              // Defines if each value should be checked against the threshold
                            .onThresholdExceeded(thresholdEvent -> System.out.println("Threshold exceeded"))    // Defines a handler that will be triggered if checkThreshold==true and the threshold is exceeded
                            .onThresholdUnderrun(thresholdEvent -> System.out.println("Threshold underrun"))    // Defines a handler that will be triggered if checkThreshold==true and the threshold is underrun
                            // Related to Gradient Bar
                            .colorGradientEnabled(false)                                                        // Defines if a gradient filled bar should be visible to visualize a range
                            .gradientLookupStops(new Stop(0.0, Color.BLUE),                                     // Defines a conical color gradient that will be use to color the gradient bar
                                                 new Stop(0.25, Color.CYAN),
                                                 new Stop(0.5, Color.LIME),
                                                 new Stop(0.75, Color.YELLOW),
                                                 new Stop(1.0, Color.RED))
                            // Related to Sections
                            .sectionsVisible(false)                                                             // Defines if sections will be visible
                            .sections(section1, section2)                                                       // Defines sections that will be drawn (won't be drawn if colorGradientEnabled==true)
                            .checkSectionsForValue(false)                                                       // Defines if each section will be checked against the current value (if true section events will be fired)
                            // Related to Areas
                            .areasVisible(false)                                                                // Defines if areas will be visible
                            .areas(section1, section2)                                                          // Defines areas that will be drawn
                            // Related to Markers
                            .markersVisible(false)                                                              // Defines if markers will be visible
                            .markers(marker1, marker2)                                                          // Defines markers that will be drawn
                            // Related to Value
                            .animated(false)                                                                    // Defines if the needle will be animated
                            .animationDuration(500)                                                             // Defines the speed of the needle in milliseconds (10 - 10000 ms)
                            .onValueChanged(o -> System.out.println(((DoubleProperty) o).get()))                // Defines an InvalidationListener that will be triggered each time the value changed
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
