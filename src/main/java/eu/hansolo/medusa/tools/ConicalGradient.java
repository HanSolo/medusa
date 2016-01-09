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

import javafx.animation.Interpolator;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Created by hansolo on 21.12.15.
 */
public class ConicalGradient {
    private static final double ANGLE_FACTOR = 1d / 360d;
    private Point2D             center;
    private List<Stop>          sortedStops;


    // ******************** Constructors **************************************
    public ConicalGradient(final Stop... STOPS) {
        this(null, Arrays.asList(STOPS));
    }
    public ConicalGradient(final List<Stop> STOPS) {
        this(null, STOPS);
    }
    public ConicalGradient(final Point2D CENTER, final Stop... STOPS) {
        this(CENTER, 0.0, Arrays.asList(STOPS));
    }
    public ConicalGradient(final Point2D CENTER, final List<Stop> STOPS) {
        this(CENTER, 0.0, STOPS);
    }
    public ConicalGradient(final Point2D CENTER, final double OFFSET, final Stop... STOPS) {
        this(CENTER, OFFSET, Arrays.asList(STOPS));
    }
    public ConicalGradient(final Point2D CENTER, final double OFFSET, final List<Stop> STOPS) {
        double offset = Helper.clamp(0d, 1d, OFFSET);
        center = CENTER;
        List<Stop> stops;
        if (null == STOPS || STOPS.isEmpty()) {
            stops = new ArrayList<>();
            stops.add(new Stop(0.0, Color.TRANSPARENT));
            stops.add(new Stop(1.0, Color.TRANSPARENT));
        } else {
            stops = STOPS;
        }

        HashMap<Double, Color> stopMap = new LinkedHashMap<>(stops.size());
        for (Stop stop : stops) {
            stopMap.put(stop.getOffset(), stop.getColor());
        }

        sortedStops = new LinkedList<>();
        final SortedSet<Double> sortedFractions = new TreeSet<>(stopMap.keySet());
        if (sortedFractions.last() < 1) {
            stopMap.put(1.0, stopMap.get(sortedFractions.first()));
            sortedFractions.add(1.0);
        }
        if (sortedFractions.first() > 0) {
            stopMap.put(0.0, stopMap.get(sortedFractions.last()));
            sortedFractions.add(0.0);
        }
        for (final Double FRACTION : sortedFractions) {
            sortedStops.add(new Stop(FRACTION, stopMap.get(FRACTION)));
        }
        if (offset > 0) {
            recalculate(offset);
        }
    }


    // ******************** Methods *******************************************
    public void recalculateWithAngle(final double ANGLE) {
        double angle = ANGLE % 360;
        recalculate(ANGLE_FACTOR * angle);
    }

    public void recalculate(final double OFFSET) {
        List<Stop> stops = new ArrayList<>(sortedStops.size());
        for (Stop stop : sortedStops) {
            double newOffset = (stop.getOffset() + OFFSET) % 1;
            if(Double.compare(newOffset, 0d) == 0) {
                newOffset = 1.0;
                stops.add(new Stop(0.000001, stop.getColor()));
            } else if (stop.getOffset() + OFFSET > 1d) {
                newOffset -= 0.000001;
            }
            stops.add(new Stop(newOffset, stop.getColor()));
        }

        HashMap<Double, Color> stopMap = new LinkedHashMap<>(stops.size());
        stops.forEach(stop -> stopMap.put(stop.getOffset(), stop.getColor()));

        List<Stop> sortedStops2 = new LinkedList<>();
        SortedSet<Double> sortedFractions = new TreeSet<>(stopMap.keySet());
        if (sortedFractions.last() < 1) {
            stopMap.put(1.0, stopMap.get(sortedFractions.first()));
            sortedFractions.add(1.0);
        }
        if (sortedFractions.first() > 0) {
            stopMap.put(0.0, stopMap.get(sortedFractions.last()));
            sortedFractions.add(0.0);
        }
        sortedFractions.forEach(fraction -> sortedStops2.add(new Stop(fraction, stopMap.get(fraction))));

        sortedStops.clear();
        sortedStops.addAll(sortedStops2);
    }

    public List<Stop> getStops() { return sortedStops; }

    public Point2D getCenter() { return center; }

    public Image getImage(final double WIDTH, final double HEIGHT) {
        int   width  = (int) WIDTH  <= 0 ? 100 : (int) WIDTH;
        int   height = (int) HEIGHT <= 0 ? 100 : (int) HEIGHT;
        Color color  = Color.TRANSPARENT;
        final WritableImage RASTER       = new WritableImage(width, height);
        final PixelWriter   PIXEL_WRITER = RASTER.getPixelWriter();
        if (null == center) {
            center = new Point2D(width * 0.5, height * 0.5);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double dx = x - center.getX();
                double dy = y - center.getY();
                double distance = Math.sqrt((dx * dx) + (dy * dy));
                distance = Double.compare(distance, 0) == 0 ? 1 : distance;

                double angle = Math.abs(Math.toDegrees(Math.acos(dx / distance)));
                if (dx >= 0 && dy <= 0) {
                    angle = 90.0 - angle;
                } else if (dx >= 0 && dy >= 0) {
                    angle += 90.0;
                } else if (dx <= 0 && dy >= 0) {
                    angle += 90.0;
                } else if (dx <= 0 && dy <= 0) {
                    angle = 450.0 - angle;
                }
                for (int i = 0; i < (sortedStops.size() - 1); i++) {
                    if (angle >= (sortedStops.get(i).getOffset() * 360) && angle < (sortedStops.get(i + 1).getOffset() * 360)) {
                        double fraction = (angle - sortedStops.get(i).getOffset() * 360) / ((sortedStops.get(i + 1).getOffset() - sortedStops.get(i).getOffset()) * 360);
                        color = (Color) Interpolator.LINEAR.interpolate(sortedStops.get(i).getColor(), sortedStops.get(i + 1).getColor(), fraction);
                    }
                }
                PIXEL_WRITER.setColor(x, y, color);
            }
        }
        return RASTER;
    }

    public Image getRoundImage(final double SIZE) {
        int   size  = (int) SIZE  <= 0 ? 100 : (int) SIZE;
        Color color = Color.TRANSPARENT;
        final WritableImage RASTER       = new WritableImage(size, size);
        final PixelWriter   PIXEL_WRITER = RASTER.getPixelWriter();
        if (null == center) { center = new Point2D(size * 0.5, size * 0.5); }
        double radius = size * 0.5;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x - center.getX();
                double dy = y - center.getY();
                double distance = Math.sqrt((dx * dx) + (dy * dy));
                distance = Double.compare(distance, 0) == 0 ? 1 : distance;

                double angle = Math.abs(Math.toDegrees(Math.acos(dx / distance)));

                if (dx >= 0 && dy <= 0) {
                    angle = 90.0 - angle;
                } else if (dx >= 0 && dy >= 0) {
                    angle += 90.0;
                } else if (dx <= 0 && dy >= 0) {
                    angle += 90.0;
                } else if (dx <= 0 && dy <= 0) {
                    angle = 450.0 - angle;
                }
                if (distance > radius) {
                    color = Color.TRANSPARENT;
                } else {
                    for (int i = 0; i < (sortedStops.size() - 1); i++) {
                        if (angle >= (sortedStops.get(i).getOffset() * 360) && angle < (sortedStops.get(i + 1).getOffset() * 360)) {
                            double fraction = (angle - sortedStops.get(i).getOffset() * 360) / ((sortedStops.get(i + 1).getOffset() - sortedStops.get(i).getOffset()) * 360);
                            color = (Color) Interpolator.LINEAR.interpolate(sortedStops.get(i).getColor(), sortedStops.get(i + 1).getColor(), fraction);
                        }
                    }
                }
                PIXEL_WRITER.setColor(x, y, color);
            }
        }
        return RASTER;
    }

    public ImagePattern apply(final Shape SHAPE) {
        double x      = SHAPE.getLayoutBounds().getMinX();
        double y      = SHAPE.getLayoutBounds().getMinY();
        double width  = SHAPE.getLayoutBounds().getWidth();
        double height = SHAPE.getLayoutBounds().getHeight();
        center        = new Point2D(width * 0.5, height * 0.5);
        return new ImagePattern(getImage(width, height), x, y, width, height, false);
    }
}
