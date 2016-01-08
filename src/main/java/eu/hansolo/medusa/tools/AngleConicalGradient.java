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

package eu.hansolo.medusa.tools;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Shape;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by hansolo on 21.12.15.
 */
public class AngleConicalGradient {
    private ConicalGradient gradient;

    public AngleConicalGradient(final Map<Double, Color> ANGLE_STOP_MAP) {
        this(null, ANGLE_STOP_MAP);
    }
    public AngleConicalGradient(final Point2D CENTER, final Map<Double, Color> ANGLE_STOP_MAP) {
        this(CENTER, 0.0, ANGLE_STOP_MAP);
    }
    public AngleConicalGradient(final Point2D CENTER, final double OFFSET_ANGLE, final Map<Double, Color> ANGLE_STOP_MAP) {
        final double ANGLE_FACTOR = 1d / 360d;
        double offset = Helper.clamp(0d, 1d, (OFFSET_ANGLE % 360) * ANGLE_FACTOR);
        List<Stop> stops = new LinkedList<>();
        ANGLE_STOP_MAP.keySet().forEach(fraction -> stops.add(new Stop(Helper.clamp(0d, 1d, (fraction % 360) * ANGLE_FACTOR), ANGLE_STOP_MAP.get(fraction))));
        gradient = new ConicalGradient(CENTER, offset, stops);
    }

    public void recalculateWithAngle(final double ANGLE) { gradient.recalculateWithAngle(ANGLE); }

    public List<Stop> getStops() { return gradient.getStops(); }

    public Point2D getCenter() { return gradient.getCenter(); }

    public Image getImage(final double WIDTH, final double HEIGHT) { return gradient.getImage(WIDTH, HEIGHT); }

    public ImagePattern apply(final Shape SHAPE) { return gradient.apply(SHAPE); }
}
