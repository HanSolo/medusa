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

import javafx.scene.paint.Color;


/**
 * Created by hansolo on 30.12.15.
 */
public enum LcdDesign {
    BEIGE(Color.rgb(200, 200, 177), Color.rgb(241, 237, 207), Color.rgb(234, 230, 194), Color.rgb(225, 220, 183), Color.rgb(237, 232, 191), Color.BLACK, Color.rgb(0, 0, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE(Color.WHITE, Color.rgb(231, 246, 255), Color.rgb(170, 224, 255), Color.rgb(136, 212, 255), Color.rgb(192, 232, 255), Color.rgb(18, 69, 100), Color.rgb(18, 69, 100, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    ORANGE(Color.WHITE, Color.rgb(255, 245, 225), Color.rgb(255, 217, 147), Color.rgb(255, 201, 104), Color.rgb(255, 227, 173), Color.rgb(80, 55, 0), Color.rgb(80, 55, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    RED(Color.WHITE, Color.rgb(255, 225, 225), Color.rgb(252, 114, 115), Color.rgb(252, 114, 115), Color.rgb(254, 178, 178), Color.rgb(79, 12, 14), Color.rgb(79, 12, 14, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    YELLOW(Color.WHITE, Color.rgb(245, 255, 186), Color.rgb(158, 205, 0), Color.rgb(158, 205, 0), Color.rgb(210, 255, 0), Color.rgb(64, 83, 0), Color.rgb(64, 83, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    WHITE(Color.WHITE, Color.WHITE, Color.rgb(241, 246, 242), Color.rgb(229, 239, 244), Color.WHITE, Color.BLACK, Color.rgb(0, 0, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    GRAY(Color.rgb(65, 65, 65), Color.rgb(117, 117, 117), Color.rgb(87, 87, 87), Color.rgb(65, 65, 65), Color.rgb(81, 81, 81), Color.WHITE, Color.rgb(255, 255, 255, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLACK(Color.rgb(65, 65, 65), Color.rgb(102, 102, 102), Color.rgb(51, 51, 51), Color.BLACK, Color.rgb(51, 51, 51), Color.rgb(204, 204, 204), Color.rgb(204, 204, 204, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    GREEN(Color.rgb(33, 67, 67), Color.rgb(33, 67, 67), Color.rgb(29, 58, 58), Color.rgb(28, 57, 57), Color.rgb(23, 46, 46), Color.rgb(0, 185, 165), Color.rgb(0, 185, 165, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    GREEN_DARKGREEN(Color.rgb(27, 41, 17), Color.rgb(70, 84, 58), Color.rgb(36, 60, 14), Color.rgb(24, 50, 1), Color.rgb(8, 10, 7), Color.rgb(152, 255, 74), Color.rgb(152, 255, 74, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE2(Color.rgb(0, 68, 103), Color.rgb(8, 109, 165), Color.rgb(0, 72, 117), Color.rgb(0, 72, 117), Color.rgb(0, 68, 103), Color.rgb(111, 182, 228), Color.rgb(111, 182, 228, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE_BLACK(Color.rgb(22, 125, 212), Color.rgb(3, 162, 254), Color.rgb(3, 162, 254), Color.rgb(3, 162, 254), Color.rgb(11, 172, 244), Color.BLACK, Color.rgb(0, 0, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE_DARKBLUE(Color.rgb(18, 33, 88), Color.rgb(18, 33, 88), Color.rgb(19, 30, 90), Color.rgb(17, 31, 94), Color.rgb(21, 25, 90), Color.rgb(23, 99, 221), Color.rgb(23, 99, 221, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE_LIGHTBLUE(Color.rgb(88, 107, 132), Color.rgb(53, 74, 104), Color.rgb(27, 37, 65), Color.rgb(5, 12, 40), Color.rgb(32, 47, 79), Color.rgb(71, 178, 254), Color.rgb(71, 178, 254, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE_GRAY(Color.rgb(135, 174, 255), Color.rgb(101, 159, 255), Color.rgb(44, 93, 255), Color.rgb(27, 65, 254), Color.rgb(12, 50, 255), Color.rgb(178, 180, 237), Color.rgb(178, 180, 237, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    STANDARD(Color.rgb(131, 133, 119), Color.rgb(176, 183, 167), Color.rgb(165, 174, 153), Color.rgb(166, 175, 156), Color.rgb(175, 184, 165), Color.rgb(35, 42, 52), Color.rgb(35, 42, 52, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    LIGHTGREEN(Color.rgb(194, 212, 188), Color.rgb(212, 234, 206), Color.rgb(205, 224, 194), Color.rgb(206, 225, 194), Color.rgb(214, 233, 206), Color.rgb(0, 12, 6), Color.rgb(0, 12, 6, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    STANDARD_GREEN(Color.WHITE, Color.rgb(219, 230, 220), Color.rgb(179, 194, 178), Color.rgb(153, 176, 151), Color.rgb(114, 138, 109), Color.rgb(0, 12, 6), Color.rgb(0, 12, 6, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE_BLUE(Color.rgb(100, 168, 253), Color.rgb(100, 168, 253), Color.rgb(95, 160, 250), Color.rgb(80, 144, 252), Color.rgb(74, 134, 255), Color.rgb(0, 44, 187), Color.rgb(0, 44, 187, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    RED_DARKRED(Color.rgb(72, 36, 50), Color.rgb(185, 111, 110), Color.rgb(148, 66, 72), Color.rgb(83, 19, 20), Color.rgb(7, 6, 14), Color.rgb(254, 139, 146), Color.rgb(254, 139, 146, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    DARKBLUE(Color.rgb(14, 24, 31), Color.rgb(46, 105, 144), Color.rgb(19, 64, 96), Color.rgb(6, 20, 29), Color.rgb(8, 9, 10), Color.rgb(61, 179, 255), Color.rgb(61, 179, 255, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    PURPLE(Color.rgb(175, 164, 255), Color.rgb(188, 168, 253), Color.rgb(176, 159, 255), Color.rgb(174, 147, 252), Color.rgb(168, 136, 233), Color.rgb(7, 97, 72), Color.rgb(7, 97, 72, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLACK_RED(Color.rgb(8, 12, 11), Color.rgb(10, 11, 13), Color.rgb(11, 10, 15), Color.rgb(7, 13, 9), Color.rgb(9, 13, 14), Color.rgb(181, 0, 38), Color.rgb(181, 0, 38, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    DARKGREEN(Color.rgb(25, 85, 0), Color.rgb(47, 154, 0), Color.rgb(30, 101, 0), Color.rgb(30, 101, 0), Color.rgb(25, 85, 0), Color.rgb(35, 49, 35), Color.rgb(35, 49, 35, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    AMBER(Color.rgb(182, 71, 0), Color.rgb(236, 155, 25), Color.rgb(212, 93, 5), Color.rgb(212, 93, 5), Color.rgb(182, 71, 0), Color.rgb(89, 58, 10), Color.rgb(89, 58, 10, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    LIGHTBLUE(Color.rgb(125, 146, 184), Color.rgb(197, 212, 231), Color.rgb(138, 155, 194), Color.rgb(138, 155, 194), Color.rgb(125, 146, 184), Color.rgb(9, 0, 81), Color.rgb(9, 0, 81, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    GREEN_BLACK(Color.rgb(1, 47, 0), Color.rgb(20, 106, 61), Color.rgb(33, 125, 84), Color.rgb(33, 125, 84), Color.rgb(33, 109, 63), Color.rgb(3, 15, 11), Color.rgb(3, 15, 11, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    YELLOW_BLACK(Color.rgb(223, 248, 86), Color.rgb(222, 255, 28), Color.rgb(213, 245, 24), Color.rgb(213, 245, 24), Color.rgb(224, 248, 88), Color.rgb(9, 19, 0), Color.rgb(9, 19, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLACK_YELLOW(Color.rgb(43, 3, 3), Color.rgb(29, 0, 0), Color.rgb(26, 2, 2), Color.rgb(31, 5, 8), Color.rgb(30, 1, 3), Color.rgb(255, 254, 24), Color.rgb(255, 254, 24, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    LIGHTGREEN_BLACK(Color.rgb(79, 121, 19), Color.rgb(96, 169, 0), Color.rgb(120, 201, 2), Color.rgb(118, 201, 0), Color.rgb(105, 179, 4), Color.rgb(0, 35, 0), Color.rgb(0, 35, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    DARKPURPLE(Color.rgb(35, 24, 75), Color.rgb(42, 20, 111), Color.rgb(40, 22, 103), Color.rgb(40, 22, 103), Color.rgb(41, 21, 111), Color.rgb(158, 167, 210), Color.rgb(158, 167, 210, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    DARKAMBER(Color.rgb(134, 39, 17), Color.rgb(120, 24, 0), Color.rgb(83, 15, 12), Color.rgb(83, 15, 12), Color.rgb(120, 24, 0), Color.rgb(233, 140, 44), Color.rgb(233, 140, 44, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    BLUE_LIGHTBLUE2(Color.rgb(15, 84, 151), Color.rgb(60, 103, 198), Color.rgb(67, 109, 209), Color.rgb(67, 109, 209), Color.rgb(64, 101, 190), Color.rgb(193, 253, 254), Color.rgb(193, 253, 254, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    GRAY_PURPLE(Color.rgb(153, 164, 161), Color.rgb(203, 215, 213), Color.rgb(202, 212, 211), Color.rgb(202, 212, 211), Color.rgb(198, 209, 213), Color.rgb(99, 124, 204), Color.rgb(99, 124, 204, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    YOCTOPUCE(Color.web("#0E181F"), Color.web("#232341"), Color.web("#1E1E3C"), Color.web("#1E1E3C"), Color.web("#191937"), Color.web("#99E5FF"), Color.rgb(153,229,255, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    SECTIONS(Color.web("#B2B2B2"), Color.web("#FFFFFF"), Color.web("#C4C4C4"), Color.web("#C4C4C4"), Color.web("#B2B2B2"), Color.web("#000000"), Color.rgb(0, 0, 0, 0.1)) { @Override public Color[] getColors() { return this.COLORS; } },
    FLAT_CUSTOM(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE, Color.TRANSPARENT) { @Override public Color[] getColors() { return this.COLORS; } };


    protected final Color[] COLORS;
    public        Color     lcdForegroundColor;
    public        Color     lcdBackgroundColor;

    public abstract Color[] getColors();

    LcdDesign(final Color BG0, final Color BG1, final Color BG2, final Color BG3, final Color BG4, final Color FG, final Color FGT) {
        COLORS             = new Color[] {BG0, BG1, BG2, BG3, BG4, FG, FGT};
        lcdForegroundColor = FG;
        lcdBackgroundColor = FGT;
    }
}
