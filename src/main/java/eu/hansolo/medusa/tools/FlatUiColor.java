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

package eu.hansolo.medusa.tools;

import javafx.scene.paint.Color;


/**
 * Created by hansolo on 11.01.16.
 */
public enum FlatUiColor {
    TURQOISE(31, 188, 156) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    GREEN_SEA(26, 160, 133) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    EMERLAND(87, 214, 141) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    NEPHRITIS(39, 174, 96) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    ANDROID_BRIGHT(171, 214, 73) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    ANDROID_DARK(147, 184, 63) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    RASPBERRY_BRIGHT(214, 38, 79) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    RASPBERRY_DARK(184, 37, 71) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    PETER_RIVER(92, 172, 226) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    BELIZE_HOLE(83, 153, 198) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    AMETHYST(175, 122, 196) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    WISTERIA(142, 68, 173) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    SUNFLOWER(241, 196, 40) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    ORANGE(245, 175, 65) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    CARROT(245, 175, 65) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    PUMPKIN(211, 85, 25) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    ALIZARIN(234, 111, 99) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    POMEGRANATE(204, 96, 85) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    CLOUDS(239, 243, 243) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    SILVER(189, 195, 199) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    CONCRETE(149, 165, 166) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    ASBESTOS(127, 140, 141) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    WET_ASPHALT(52, 73, 94) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }},
    MIDNIGHT_BLUE(44, 62, 80) { @Override public Color get() { return Color.rgb(RED, GREEN, BLUE); }};

    public abstract Color get();

    public final int RED;
    public final int GREEN;
    public final int BLUE;


    FlatUiColor(final int R, final int G, final int B) {
        RED   = R;
        GREEN = G;
        BLUE  = B;
    }
}
