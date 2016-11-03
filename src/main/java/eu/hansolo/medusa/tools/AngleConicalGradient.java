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

import eu.hansolo.medusa.Gauge.ScaleDirection;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by hansolo on 21.12.15.
 */
public class AngleConicalGradient {
    private ConicalGradient gradient;

    public AngleConicalGradient(final Map<Double, Color> ANGLE_STOP_MAP, final ScaleDirection DIRECTION) {
        this(0, 0, ANGLE_STOP_MAP, DIRECTION);
    }
    public AngleConicalGradient(final double CENTER_X, final double CENTER_Y, final Map<Double, Color> ANGLE_STOP_MAP, final ScaleDirection DIRECTION) {
        this(CENTER_X, CENTER_Y, 0.0, ANGLE_STOP_MAP, DIRECTION);
    }
    public AngleConicalGradient(final double CENTER_X, final double CENTER_Y, final double OFFSET_ANGLE, final Map<Double, Color> ANGLE_STOP_MAP, final ScaleDirection DIRECTION) {
        final double ANGLE_FACTOR = 1.0 / 360.0;
        double       offset       = Helper.clamp(0, 1, (OFFSET_ANGLE % 360.0) * ANGLE_FACTOR);
        List<Stop>   stops        = new ArrayList<>();
        for (double fraction : ANGLE_STOP_MAP.keySet()) {
            stops.add(new Stop(Helper.clamp(0, 1, (fraction % 360.0) * ANGLE_FACTOR), ANGLE_STOP_MAP.get(fraction)));
        }
        gradient = new ConicalGradient(CENTER_X, CENTER_Y, offset, DIRECTION, stops);
    }

    public void recalculateWithAngle(final double ANGLE) { gradient.recalculateWithAngle(ANGLE); }

    public List<Stop> getStops() { return gradient.getStops(); }

    public double[] getCenter() { return gradient.getCenter(); }
    public Point2D getCenterPoint() { return gradient.getCenterPoint(); }

    public Image getImage(final double WIDTH, final double HEIGHT) { return gradient.getImage(WIDTH, HEIGHT); }
    public Image getRoundImage(final double SIZE) { return gradient.getRoundImage(SIZE); }

    public ImagePattern apply(final Shape SHAPE) { return gradient.apply(SHAPE); }

    public ImagePattern getImagePattern(final Rectangle BOUNDS) { return gradient.getImagePattern(BOUNDS); }
}
