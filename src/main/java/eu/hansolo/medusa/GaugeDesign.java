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

/**
 * Created by hansolo on 18.12.15.
 */
public enum GaugeDesign {
    STEEL_SERIES("steel-series-frame", 22.5) { String getInsets(final double SIZE) {
        return "-fx-background-insets: " + 0 + "," + (0.0037037 * SIZE) + "," + (FRAME_FACTOR * SIZE) + ";";
    }},
    ENZO("enzo-frame", 13) { String getInsets(final double SIZE) {
        return "-fx-background-insets: " + 0 + "," + (0.02222222 * SIZE) + "," + (0.02592593 * SIZE) + "," + (FRAME_FACTOR * SIZE) + ";";
    }};

    final public String FRAME_STYLE;
    final public double FRAME_SIZE;
    final public double FRAME_FACTOR;

    abstract String getInsets(final double SIZE);

    GaugeDesign(final String FRAME_STYLE, final double FRAME_SIZE) {
        this.FRAME_STYLE  = FRAME_STYLE;
        this.FRAME_SIZE   = FRAME_SIZE;
        this.FRAME_FACTOR = FRAME_SIZE / FramedGauge.PREFERRED_WIDTH;
    }
}
