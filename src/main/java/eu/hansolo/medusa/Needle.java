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

import eu.hansolo.medusa.Gauge.NeedleType;
import eu.hansolo.medusa.Gauge.SkinType;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;


/**
 * Created by hansolo on 22.02.16.
 */
public enum Needle {
    INSTANCE;

    Needle() {

    }
    
    public Path getPath(final Path PATH, final double NEEDLE_WIDTH, final double NEEDLE_HEIGHT, final NeedleType NEEDLE_TYPE) {
        PATH.getElements().clear();
        switch(NEEDLE_TYPE) {
            case FAT:
                PATH.getElements().add(new MoveTo(0.275 * NEEDLE_WIDTH, 0.7029702970297029 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.275 * NEEDLE_WIDTH, 0.6287128712871287 * NEEDLE_HEIGHT, 0.375 * NEEDLE_WIDTH, 0.5693069306930693 * NEEDLE_HEIGHT, 0.5 * NEEDLE_WIDTH, 0.5693069306930693 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.625 * NEEDLE_WIDTH, 0.5693069306930693 * NEEDLE_HEIGHT, 0.725 * NEEDLE_WIDTH, 0.6287128712871287 * NEEDLE_HEIGHT, 0.725 * NEEDLE_WIDTH, 0.7029702970297029 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.725 * NEEDLE_WIDTH, 0.7772277227722773 * NEEDLE_HEIGHT, 0.625 * NEEDLE_WIDTH, 0.8366336633663366 * NEEDLE_HEIGHT, 0.5 * NEEDLE_WIDTH, 0.8366336633663366 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.375 * NEEDLE_WIDTH, 0.8366336633663366 * NEEDLE_HEIGHT, 0.275 * NEEDLE_WIDTH, 0.7772277227722773 * NEEDLE_HEIGHT, 0.275 * NEEDLE_WIDTH, 0.7029702970297029 * NEEDLE_HEIGHT));
                PATH.getElements().add(new ClosePath());
                PATH.getElements().add(new MoveTo(0.0, 0.7029702970297029 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.0, 0.8663366336633663 * NEEDLE_HEIGHT, 0.225 * NEEDLE_WIDTH, NEEDLE_HEIGHT, 0.5 * NEEDLE_WIDTH, NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.775 * NEEDLE_WIDTH, NEEDLE_HEIGHT, NEEDLE_WIDTH, 0.8663366336633663 * NEEDLE_HEIGHT, NEEDLE_WIDTH, 0.7029702970297029 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(NEEDLE_WIDTH, 0.5396039603960396 * NEEDLE_HEIGHT, 0.5 * NEEDLE_WIDTH, 0.0, 0.5 * NEEDLE_WIDTH, 0.0));
                PATH.getElements().add(new CubicCurveTo(0.5 * NEEDLE_WIDTH, 0.0, 0.0, 0.5396039603960396 * NEEDLE_HEIGHT, 0.0, 0.7029702970297029 * NEEDLE_HEIGHT));
                PATH.getElements().add(new ClosePath());
                break;
            case STANDARD:
            default:
                PATH.getElements().add(new MoveTo(0.25 * NEEDLE_WIDTH, 0.025423728813559324 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.25 * NEEDLE_WIDTH, 0.00847457627118644 * NEEDLE_HEIGHT, 0.375 * NEEDLE_WIDTH, 0, 0.5 * NEEDLE_WIDTH, 0));
                PATH.getElements().add(new CubicCurveTo(0.625 * NEEDLE_WIDTH, 0, 0.75 * NEEDLE_WIDTH, 0.00847457627118644 * NEEDLE_HEIGHT, 0.75 * NEEDLE_WIDTH, 0.025423728813559324 * NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0.75 * NEEDLE_WIDTH, 0.025423728813559324 * NEEDLE_HEIGHT, NEEDLE_WIDTH, NEEDLE_HEIGHT, NEEDLE_WIDTH, NEEDLE_HEIGHT));
                PATH.getElements().add(new LineTo(0, NEEDLE_HEIGHT));
                PATH.getElements().add(new CubicCurveTo(0, NEEDLE_HEIGHT, 0.25 * NEEDLE_WIDTH, 0.025423728813559324 * NEEDLE_HEIGHT, 0.25 * NEEDLE_WIDTH, 0.025423728813559324 * NEEDLE_HEIGHT));
                PATH.getElements().add(new ClosePath());
                break;
        }
        return PATH;
    }
}
