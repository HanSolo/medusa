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

import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.tools.ConicalGradient;
import eu.hansolo.medusa.tools.Helper;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderImage;
import javafx.scene.layout.BorderRepeat;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;


/**
 * Created by hansolo on 18.12.15.
 */
public enum GaugeDesign {
    NONE(0) {
        @Override public Border getBorder(final double SIZE) { return Border.EMPTY; }
    },
    METAL(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            double fromX = 0;
            double fromY = 0;
            double toX   = 0;
            double toY   = SIZE;
            Stop[] stops = {
                new Stop(0, Color.rgb(254, 254, 254)),
                new Stop(0.07, Color.rgb(210, 210, 210)),
                new Stop(0.12, Color.rgb(179,179,179)),
                new Stop(1.0, Color.rgb(213,213,213))
            };
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(fromX, fromY, toX, toY, false, CycleMethod.NO_CYCLE, stops),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    TILTED_GRAY(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            double fromX = 0.2336448598130841 * SIZE;
            double fromY = 0.08411214953271028 * SIZE;
            double toX   = (0.2336448598130841 + 0.5789369637935792) * SIZE;
            double toY   = (0.08411214953271028 + 0.8268076708711319) * SIZE;
            Stop[] stops = {
                new Stop(0.0, Color.WHITE),
                new Stop(0.07, Color.rgb(210, 210, 210)),
                new Stop(0.16, Color.rgb(179, 179, 179)),
                new Stop(0.33, Color.WHITE),
                new Stop(0.55, Color.rgb(197, 197, 197)),
                new Stop(0.79, Color.WHITE),
                new Stop(1.0, Color.rgb(102, 102, 102))
            };
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(fromX, fromY, toX, toY, false, CycleMethod.NO_CYCLE, stops),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    TILTED_BLACK(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            double fromX = 0.2336448598130841 * SIZE;
            double fromY = 0.08411214953271028 * SIZE;
            double toX   = (0.2336448598130841 + 0.5789369637935792) * SIZE;
            double toY   = (0.08411214953271028 + 0.8268076708711319) * SIZE;
            Stop[] stops = {
                new Stop(0.0, Color.rgb(102, 102, 102)),
                new Stop(0.21, Color.BLACK),
                new Stop(0.47, Color.rgb(102, 102, 102)),
                new Stop(1.0, Color.BLACK)
            };
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(fromX, fromY, toX, toY, false, CycleMethod.NO_CYCLE, stops),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    STEEL(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            double fromX = 0;
            double fromY = 0;
            double toX   = 0;
            double toY   = SIZE;
            Stop[] stops = {
                new Stop(0, Color.rgb(231,237,237)),
                new Stop(0.03, Color.rgb(189,199,198)),
                new Stop(0.06, Color.rgb(192,201,200)),
                new Stop(0.48, Color.rgb(23,31,33)),
                new Stop(0.93, Color.rgb(196,205,204)),
                new Stop(0.96, Color.rgb(194,204,203)),
                new Stop(1.00, Color.rgb(189,201,199))
            };
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(fromX, fromY, toX, toY, false, CycleMethod.NO_CYCLE, stops),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    BRASS(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            double fromX = 0;
            double fromY = 0;
            double toX   = 0;
            double toY   = SIZE;
            Stop[] stops = {
                new Stop(0, Color.rgb(249, 243, 155)),
                new Stop(0.05, Color.rgb(246, 226, 101)),
                new Stop(0.10, Color.rgb(240, 225, 132)),
                new Stop(0.50, Color.rgb(90, 57, 22)),
                new Stop(0.90, Color.rgb(249, 237, 139)),
                new Stop(0.95, Color.rgb(243, 226, 108)),
                new Stop(1.00, Color.rgb(202, 182, 113))
            };
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(fromX, fromY, toX, toY, false, CycleMethod.NO_CYCLE, stops),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    GOLD(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            double fromX = 0;
            double fromY = 0;
            double toX   = 0;
            double toY   = SIZE;
            Stop[] stops = {
                new Stop(0.00, Color.rgb(255, 255, 207)),
                new Stop(0.15, Color.rgb(255, 237, 96)),
                new Stop(0.22, Color.rgb(254, 199, 57)),
                new Stop(0.30, Color.rgb(255, 249, 203)),
                new Stop(0.38, Color.rgb(255, 199, 64)),
                new Stop(0.44, Color.rgb(252, 194, 60)),
                new Stop(0.51, Color.rgb(255, 204, 59)),
                new Stop(0.60, Color.rgb(213, 134, 29)),
                new Stop(0.68, Color.rgb(255, 201, 56)),
                new Stop(0.75, Color.rgb(212, 135, 29)),
                new Stop(1.00, Color.rgb(247, 238, 101))
            };
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(new LinearGradient(fromX, fromY, toX, toY, false, CycleMethod.NO_CYCLE, stops),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    BLACK_METAL(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            Stop[] stops = {
                new Stop(0.0, Color.rgb(254, 254, 254)),
                new Stop(0.125, Color.rgb(0, 0, 0)),
                new Stop(0.347222, Color.rgb(153, 153, 153)),
                new Stop(0.5, Color.rgb(0, 0, 0)),
                new Stop(0.6805555, Color.rgb(153, 153, 153)),
                new Stop(0.875, Color.rgb(0, 0, 0)),
                new Stop(1.0, Color.rgb(254, 254, 254))
            };
            ConicalGradient gradient = new ConicalGradient(SIZE * 0.5, SIZE * 0.5, ScaleDirection.CLOCKWISE, stops);
            Image        image       = gradient.getRoundImage(SIZE);
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132, 132, 132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderImage  innerBorder = new BorderImage(image,
                                                       BorderWidths.DEFAULT,
                                                       new Insets(0.0037037 * SIZE),
                                                       BorderWidths.EMPTY, true,
                                                       BorderRepeat.STRETCH,
                                                       BorderRepeat.STRETCH);
            return new Border(new BorderStroke[]{outerBorder}, new BorderImage[]{innerBorder});
        }
    },
    SHINY_METAL(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            Stop[] stops = {
                new Stop(0.0, Color.rgb(254, 254, 254)),
                new Stop(0.125, Color.rgb(179, 179, 179)),
                new Stop(0.347222, Color.rgb(238, 238, 238)),
                new Stop(0.45, Color.rgb(179, 179, 179)),
                new Stop(0.5, Color.rgb(179, 179, 179)),
                new Stop(0.55, Color.rgb(179, 179, 179)),
                new Stop(0.6805555, Color.rgb(238, 238, 238)),
                new Stop(0.875, Color.rgb(179, 179, 179)),
                new Stop(1.0, Color.rgb(254, 254, 254))
            };
            ConicalGradient gradient = new ConicalGradient(SIZE * 0.5, SIZE * 0.5, ScaleDirection.CLOCKWISE, stops);
            Image        image       = gradient.getRoundImage(SIZE);
            BorderStroke outerBorder = new BorderStroke(Color.rgb(132, 132, 132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderImage  innerBorder = new BorderImage(image,
                                                       new BorderWidths(1),
                                                       new Insets(0.0037037 * SIZE),
                                                       BorderWidths.EMPTY,
                                                       true,
                                                       BorderRepeat.STRETCH,
                                                       BorderRepeat.STRETCH);
            return new Border(new BorderStroke[]{outerBorder}, new BorderImage[]{innerBorder});
        }
    },
    ENZO(0.04814815) {
        @Override public Border getBorder(final double SIZE) {
            BorderStroke outerBorder     = new BorderStroke(new LinearGradient(0, 0, 0, SIZE, false, CycleMethod.NO_CYCLE,
                                                                               new Stop(0, Color.rgb(224,224,224)),
                                                                               new Stop(0.26, Color.rgb(133,133,133)),
                                                                               new Stop(1.0, Color.rgb(84,84,84))),
                                                                               BorderStrokeStyle.SOLID,
                                                                               new CornerRadii(1024),
                                                                               new BorderWidths(FRAME_FACTOR * SIZE),
                                                                               Insets.EMPTY);
            BorderStroke highlightBorder = new BorderStroke(new LinearGradient(0, 0.02222222 * SIZE, 0, (SIZE - 0.04444444 * SIZE),
                                                                               false, CycleMethod.NO_CYCLE,
                                                                               new Stop(0, Color.rgb(255,255,255)),
                                                                               new Stop(0.50, Color.rgb(146,146,147)),
                                                                               new Stop(1.0, Color.rgb(135,136,138))),
                                                                               BorderStrokeStyle.SOLID,
                                                                               new CornerRadii(1024),
                                                                               new BorderWidths(FRAME_FACTOR * SIZE),
                                                                               new Insets(0.02222222 * SIZE));
            BorderStroke innerBorder     = new BorderStroke(new LinearGradient(0, 0.02592593 * SIZE, 0, (SIZE - 0.05185186 * SIZE),
                                                                               false, CycleMethod.NO_CYCLE,
                                                                               new Stop(0, Color.rgb(71,72,72)),
                                                                               new Stop(0.50, Color.rgb(110,106,107)),
                                                                               new Stop(1.0, Color.rgb(186,185,187))),
                                                                               BorderStrokeStyle.SOLID,
                                                                               new CornerRadii(1024),
                                                                               new BorderWidths(FRAME_FACTOR * SIZE),
                                                                               new Insets(0.02592593 * SIZE));
            return new Border(outerBorder, highlightBorder, innerBorder);
        }
    },
    FLAT(0.08333333) {
        @Override public Border getBorder(final double SIZE) {
            //double fromX = 0;
            //double fromY = 0;
            //double toX   = 0;
            //double toY   = SIZE;

            BorderStroke outerBorder = new BorderStroke(Color.rgb(132,132,132),
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(1),
                                                        Insets.EMPTY);
            BorderStroke innerBorder = new BorderStroke(frameColor,
                                                        BorderStrokeStyle.SOLID,
                                                        new CornerRadii(1024),
                                                        new BorderWidths(FRAME_FACTOR * SIZE),
                                                        new Insets(0.0037037 * SIZE));
            return new Border(outerBorder, innerBorder);
        }
    },
    TRANSPARENT(0) {
        @Override public Border getBorder(final double SIZE) {
            return Border.EMPTY;
        }
    };

    public enum GaugeBackground {
        DARK_GRAY() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                Stop[] stops = {
                    new Stop(0, Color.BLACK),
                    new Stop(0.40, Color.rgb(51, 51, 51)),
                    new Stop(1.0, Color.rgb(153,153,153))
                };
                return new LinearGradient(0, Y1, 0, Y2, false, CycleMethod.NO_CYCLE, stops);
            }
        },
        BEIGE() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                Stop[] stops = {
                    new Stop(0, Color.rgb(178, 172, 150)),
                    new Stop(0.40, Color.rgb(204, 205, 184)),
                    new Stop(1.0, Color.rgb(231, 231, 214))
                };
                return new LinearGradient(0, Y1, 0, Y2, false, CycleMethod.NO_CYCLE, stops);
            }
        },
        ANTHRACITE() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                Stop[] stops = {
                    new Stop(0, Color.rgb(50, 50, 54)),
                    new Stop(0.40, Color.rgb(47, 47, 51)),
                    new Stop(1.0, Color.rgb(69, 69, 74))
                };
                return new LinearGradient(0, Y1, 0, Y2, false, CycleMethod.NO_CYCLE, stops);
            }
        },
        LIGHT_GRAY() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                Stop[] stops = {
                    new Stop(0, Color.rgb(130, 130, 130)),
                    new Stop(0.40, Color.rgb(181, 181, 181)),
                    new Stop(1.0, Color.rgb(253, 253, 253))
                };
                return new LinearGradient(0, Y1, 0, Y2, false, CycleMethod.NO_CYCLE, stops);
            }
        },
        WHITE() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                return Color.WHITE;
            }
        },
        BLACK() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                return Color.BLACK;
            }
        },
        CARBON() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                return Helper.createCarbonPattern();
            }
        },
        TRANSPARENT() {
            @Override public Paint getPaint(final double X1, final double Y1, final double X2, final double Y2) {
                return Color.TRANSPARENT;
            }
        };

        public abstract Paint getPaint(final double X1, final double Y1, final double X2, final double Y2);
    }

    public final double FRAME_FACTOR;
    public       Color  frameColor;

    public abstract Border getBorder(final double SIZE);

    GaugeDesign(final double FRAME_FACTOR) {
        this.FRAME_FACTOR = FRAME_FACTOR;
        frameColor        = Color.TRANSPARENT;
    }
}
