/*
 * Copyright (c) 2015 by Gerrit Grunwald
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

import javafx.geometry.Insets;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;


/**
 * Created by hansolo on 18.12.15.
 */
public enum GaugeDesign {
    STEEL_SERIES_METAL(0.08333333) {
        public BorderStroke[] getBorderStrokes(final double SIZE) {
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132), BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(0));
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(0, 0, 0, SIZE, false, CycleMethod.NO_CYCLE,
                                                                           new Stop(0, Color.rgb(254, 254, 254)),
                                                                           new Stop(0.07, Color.rgb(210, 210, 210)),
                                                                           new Stop(0.12, Color.rgb(179,179,179)),
                                                                           new Stop(1.0, Color.rgb(213,213,213))),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        BorderWidths.FULL,
                                                        new Insets(0.0037037 * SIZE));
            BorderStroke bodyStroke  = new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(FRAME_FACTOR * SIZE));
            return new BorderStroke[] {outerBorder, innerBorder, bodyStroke };
        }
    },
    STEEL_SERIES_STEEL(0.08333333) {
        public BorderStroke[] getBorderStrokes(final double SIZE) {
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132), BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(0));
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(0, 0, 0, SIZE, false, CycleMethod.NO_CYCLE,
                                                                           new Stop(0, Color.rgb(231,237,237)),
                                                                           new Stop(0.03, Color.rgb(189,199,198)),
                                                                           new Stop(0.06, Color.rgb(192,201,200)),
                                                                           new Stop(0.48, Color.rgb(23,31,33)),
                                                                           new Stop(0.93, Color.rgb(196,205,204)),
                                                                           new Stop(0.96, Color.rgb(194,204,203)),
                                                                           new Stop(1.00, Color.rgb(189,201,199))),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        BorderWidths.FULL,
                                                        new Insets(0.0037037 * SIZE));
            BorderStroke bodyStroke  = new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(FRAME_FACTOR * SIZE));
            return new BorderStroke[] {outerBorder, innerBorder, bodyStroke };
        }
    },
    ENZO(0.04814815) {
        public BorderStroke[] getBorderStrokes(final double SIZE) {
            BorderStroke outerBorder     = new BorderStroke(new LinearGradient(0, 0, 0, SIZE, false, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(224,224,224)), new Stop(0.26, Color.rgb(133,133,133)), new Stop(1.0, Color.rgb(84,84,84))), BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(0));
            BorderStroke highlightBorder = new BorderStroke(new LinearGradient(0, 0.02222222 * SIZE, 0, (SIZE - 0.04444444 * SIZE), false, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(255,255,255)), new Stop(0.50, Color.rgb(146,146,147)), new Stop(1.0, Color.rgb(135,136,138))), BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(0.02222222 * SIZE));
            BorderStroke innerBorder     = new BorderStroke(new LinearGradient(0, 0.02592593 * SIZE, 0, (SIZE - 0.05185186 * SIZE), false, CycleMethod.NO_CYCLE, new Stop(0, Color.rgb(71,72,72)), new Stop(0.50, Color.rgb(110,106,107)), new Stop(1.0, Color.rgb(186,185,187))), BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(0.02592593 * SIZE));
            BorderStroke bodyStroke      = new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(1024), BorderWidths.FULL, new Insets(FRAME_FACTOR * SIZE));
            return new BorderStroke[] {outerBorder, highlightBorder, innerBorder, bodyStroke };
        }
    };

    public enum GaugeBackground {
        DARK_GRAY,
        RETRO
    }

    final public double     FRAME_FACTOR;

    public abstract BorderStroke[] getBorderStrokes(final double SIZE);

    GaugeDesign(final double FRAME_FACTOR) {
        this.FRAME_FACTOR = FRAME_FACTOR;
    }
}
