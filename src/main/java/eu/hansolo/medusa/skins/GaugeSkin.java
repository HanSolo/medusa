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

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.ScaleDirection;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.Marker;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.tools.AngleConicalGradient;
import eu.hansolo.medusa.tools.Helper;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by hansolo on 11.12.15.
 */
public class GaugeSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double             PREFERRED_WIDTH  = 250;
    private static final double             PREFERRED_HEIGHT = 250;
    private static final double             MINIMUM_WIDTH    = 50;
    private static final double             MINIMUM_HEIGHT   = 50;
    private static final double             MAXIMUM_WIDTH    = 1024;
    private static final double             MAXIMUM_HEIGHT   = 1024;
    private              Map<Marker, Shape> markerMap        = new ConcurrentHashMap<>();
    private double                   oldValue;
    private double                   size;
    private Pane                     pane;
    private InnerShadow              backgroundInnerShadow;
    private Canvas                   ticksAndSectionsCanvas;
    private GraphicsContext          ticksAndSections;
    private double                   ledSize;
    private InnerShadow              ledOnShadow;
    private InnerShadow              ledOffShadow;
    private Paint                    ledFramePaint;
    private Paint                    ledOnPaint;
    private Paint                    ledOffPaint;
    private Paint                    ledHighlightPaint;
    private Canvas                   ledCanvas;
    private GraphicsContext          led;
    private Pane                     markerPane;
    private Path                     threshold;
    private Rectangle                lcd;
    private Path                     needle;
    private MoveTo                   needleMoveTo1;
    private CubicCurveTo             needleCubicCurveTo2;
    private CubicCurveTo             needleCubicCurveTo3;
    private CubicCurveTo             needleCubicCurveTo4;
    private LineTo                   needleLineTo5;
    private CubicCurveTo             needleCubicCurveTo6;
    private ClosePath                needleClosePath7;
    private Rotate                   needleRotate;
    private Paint                    needlePaint;
    private Canvas                   knobCanvas;
    private GraphicsContext          knob;
    private Group                    shadowGroup;
    private DropShadow               dropShadow;
    private Text                     titleText;
    private Text                     subTitleText;
    private Text                     unitText;
    private Text                     valueText;
    private double                   angleStep;
    private double                   startAngle;
    private double                   angleRange;
    private String                   limitString;
    private EventHandler<MouseEvent> mouseHandler;
    private Tooltip                  buttonTooltip;
    private Tooltip                  thresholdTooltip;
    private String                   formatString;
    private double                   minValue;
    private double                   maxValue;
    private List<Section>            sections;
    private List<Section>            areas;


    // ******************** Constructors **************************************
    public GaugeSkin(Gauge gauge) {
        super(gauge);
        if (gauge.isAutoScale()) gauge.calcAutoScale();
        startAngle   = gauge.getStartAngle();
        angleRange   = gauge.getAngleRange();
        angleStep    = gauge.getAngleStep();
        oldValue     = gauge.getValue();
        minValue     = gauge.getMinValue();
        maxValue     = gauge.getMaxValue();
        limitString  = "";
        formatString = new StringBuilder("%.").append(Integer.toString(gauge.getDecimals())).append("f").toString();
        sections     = gauge.getSections();
        areas        = gauge.getAreas();
        mouseHandler = event -> handleMouseEvent(event);
        updateMarkers();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() < 0 && getSkinnable().getPrefHeight() < 0) {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        backgroundInnerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(10, 10, 10, 0.45), 8, 0d, 8d, 0d);

        ticksAndSectionsCanvas = new Canvas();
        ticksAndSections = ticksAndSectionsCanvas.getGraphicsContext2D();

        ledCanvas = new Canvas();
        led = ledCanvas.getGraphicsContext2D();

        thresholdTooltip = new Tooltip("Threshold\n(" + String.format(Locale.US, formatString, getSkinnable().getThreshold()) + ")");
        thresholdTooltip.setTextAlignment(TextAlignment.CENTER);

        threshold = new Path();
        Tooltip.install(threshold, thresholdTooltip);

        markerPane = new Pane();

        lcd = new Rectangle(0.3 * PREFERRED_WIDTH, 0.014 * PREFERRED_HEIGHT);
        lcd.setArcWidth(0.0125 * PREFERRED_HEIGHT);
        lcd.setArcHeight(0.0125 * PREFERRED_HEIGHT);
        lcd.relocate((PREFERRED_WIDTH - lcd.getWidth()) * 0.5, 0.44 * PREFERRED_HEIGHT);
        lcd.setManaged(getSkinnable().isLcdVisible());
        lcd.setVisible(getSkinnable().isLcdVisible());

        needleRotate = new Rotate(180 - startAngle);
        needleRotate.setAngle(needleRotate.getAngle() + (getSkinnable().getValue() - oldValue - minValue) * angleStep);
        needleMoveTo1       = new MoveTo();
        needleCubicCurveTo2 = new CubicCurveTo();
        needleCubicCurveTo3 = new CubicCurveTo();
        needleCubicCurveTo4 = new CubicCurveTo();
        needleLineTo5       = new LineTo();
        needleCubicCurveTo6 = new CubicCurveTo();
        needleClosePath7    = new ClosePath();
        needle              = new Path(needleMoveTo1, needleCubicCurveTo2, needleCubicCurveTo3, needleCubicCurveTo4, needleLineTo5, needleCubicCurveTo6, needleClosePath7);
        needle.setFillRule(FillRule.EVEN_ODD);
        needle.getTransforms().setAll(needleRotate);
        needle.setStrokeType(StrokeType.INSIDE);
        needle.setStroke(Color.TRANSPARENT);

        buttonTooltip    = new Tooltip();
        buttonTooltip.setTextAlignment(TextAlignment.CENTER);

        knobCanvas = new Canvas();
        knob = knobCanvas.getGraphicsContext2D();
        knobCanvas.setPickOnBounds(false);

        dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.25));
        dropShadow.setBlurType(BlurType.TWO_PASS_BOX);
        dropShadow.setRadius(0.015 * PREFERRED_WIDTH);
        dropShadow.setOffsetY(0.015 * PREFERRED_WIDTH);

        shadowGroup = new Group(needle, knobCanvas);
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        titleText = new Text(getSkinnable().getTitle());
        titleText.setTextOrigin(VPos.CENTER);
        titleText.setMouseTransparent(true);

        subTitleText = new Text(getSkinnable().getSubTitle());
        subTitleText.setTextOrigin(VPos.CENTER);
        subTitleText.setMouseTransparent(true);

        unitText = new Text(getSkinnable().getUnit());
        unitText.setMouseTransparent(true);
        unitText.setTextOrigin(VPos.CENTER);
        unitText.setMouseTransparent(true);

        valueText = new Text(String.format(Locale.US, formatString, getSkinnable().getValue()));
        valueText.setMouseTransparent(true);
        valueText.setTextOrigin(VPos.CENTER);
        valueText.setMouseTransparent(true);

        // Set initial value
        double targetAngle = 180 - startAngle + (getSkinnable().getCurrentValue() - minValue) * angleStep;
        targetAngle        = Helper.clamp(180 - startAngle, 180 - startAngle + angleRange, targetAngle);
        needleRotate.setAngle(targetAngle);

        // Add all nodes
        pane = new Pane();
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));
        pane.getChildren().setAll(ticksAndSectionsCanvas,
                                  markerPane,
                                  ledCanvas,
                                  lcd,
                                  titleText,
                                  subTitleText,
                                  unitText,
                                  valueText,
                                  shadowGroup);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().getMarkers().addListener((ListChangeListener<Marker>) c -> {
            updateMarkers();
            redraw();
        });

        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(e -> rotateNeedle(getSkinnable().getCurrentValue()));

        handleEvents("INTERACTIVITY");
        handleEvents("VISIBILITY");
    }


    // ******************** Methods *******************************************
    protected void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
            redraw();
        } else if ("FINISHED".equals(EVENT_TYPE)) {
            double currentValue = getSkinnable().getCurrentValue();
            // Check sections for value and fire section events
            if (getSkinnable().getCheckSectionsForValue()) {
                int listSize = sections.size();
                for (int i = 0 ; i < listSize ; i++) { sections.get(i).checkForValue(currentValue); }
            }

            // Check areas for value and fire section events
            if (getSkinnable().getCheckAreasForValue()) {
                int listSize = areas.size();
                for (int i = 0 ; i < listSize ; i++) { areas.get(i).checkForValue(currentValue); }
            }
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {
            ledCanvas.setManaged(getSkinnable().isLedVisible());
            ledCanvas.setVisible(getSkinnable().isLedVisible());

            titleText.setVisible(!getSkinnable().getTitle().isEmpty());
            titleText.setManaged(!getSkinnable().getTitle().isEmpty());

            subTitleText.setVisible(!getSkinnable().getSubTitle().isEmpty());
            subTitleText.setManaged(!getSkinnable().getSubTitle().isEmpty());

            unitText.setVisible(!getSkinnable().getUnit().isEmpty());
            unitText.setManaged(!getSkinnable().getUnit().isEmpty());

            valueText.setManaged(getSkinnable().isValueVisible());
            valueText.setVisible(getSkinnable().isValueVisible());

            lcd.setManaged(getSkinnable().isLcdVisible() && getSkinnable().isValueVisible());
            lcd.setVisible(getSkinnable().isLcdVisible() && getSkinnable().isValueVisible());

            boolean markersVisible = getSkinnable().getMarkersVisible();
            for (Shape shape : markerMap.values()) {
                shape.setManaged(markersVisible);
                shape.setVisible(markersVisible);
            }

            threshold.setManaged(getSkinnable().isThresholdVisible());
            threshold.setVisible(getSkinnable().isThresholdVisible());
        } else if ("LED".equals(EVENT_TYPE)) {
            if (getSkinnable().isLedVisible()) { drawLed(); }
        } else if ("LCD".equals(EVENT_TYPE)) {
            if (getSkinnable().isLcdVisible()) redraw();
        } else if ("RECALC".equals(EVENT_TYPE)) {
            if (getSkinnable().isAutoScale()) getSkinnable().calcAutoScale();
            startAngle = getSkinnable().getStartAngle();
            angleRange = getSkinnable().getAngleRange();
            angleStep  = getSkinnable().getAngleStep();
            minValue   = getSkinnable().getMinValue();
            maxValue   = getSkinnable().getMaxValue();
            needleRotate.setAngle((180 - startAngle) + (getSkinnable().getValue() - minValue) * angleStep);
            if (getSkinnable().getValue() < minValue) {
                getSkinnable().setValue(minValue);
                oldValue = minValue;
            }
            if (getSkinnable().getValue() > maxValue) {
                getSkinnable().setValue(maxValue);
                oldValue = maxValue;
            }
            resize();
            redraw();
        } else if ("SECTION".equals(EVENT_TYPE)) {
            sections = getSkinnable().getSections();
            areas    = getSkinnable().getAreas();
            resize();
            redraw();
        } else if ("INTERACTIVITY".equals(EVENT_TYPE)) {
            if (getSkinnable().isInteractive()) {
                knobCanvas.setOnMousePressed(mouseHandler);
                knobCanvas.setOnMouseReleased(mouseHandler);
                if (!getSkinnable().getButtonTooltipText().isEmpty()) {
                    buttonTooltip.setText(getSkinnable().getButtonTooltipText());
                    Tooltip.install(knobCanvas, buttonTooltip);
                }
            } else {
                knobCanvas.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);
                knobCanvas.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseHandler);
                Tooltip.uninstall(knobCanvas, buttonTooltip);
            }
        }
    }

    public void handleMouseEvent(final MouseEvent EVENT) {
        final EventType TYPE = EVENT.getEventType();
        if (MouseEvent.MOUSE_PRESSED == TYPE) {
            getSkinnable().fireButtonEvent(getSkinnable().BUTTON_PRESSED_EVENT);
            drawKnob(true);
        } else if (MouseEvent.MOUSE_RELEASED == TYPE) {
            getSkinnable().fireButtonEvent(getSkinnable().BUTTON_RELEASED_EVENT);
            drawKnob(false);
        }
    }


    // ******************** Private Methods ***********************************
    private void rotateNeedle(final double VALUE) {
        double startOffsetAngle = 180 - startAngle;
        double targetAngle;
        if (ScaleDirection.CLOCKWISE == getSkinnable().getScaleDirection()) {
            targetAngle = startOffsetAngle + (VALUE - minValue) * angleStep;
            targetAngle = Helper.clamp(startOffsetAngle, startOffsetAngle + angleRange, targetAngle);
        } else {
            targetAngle = startOffsetAngle - (VALUE - minValue) * angleStep;
            targetAngle = Helper.clamp(startOffsetAngle - angleRange, startOffsetAngle, targetAngle);
        }
        needleRotate.setAngle(targetAngle);
        valueText.setText(limitString + String.format(Locale.US, formatString, VALUE));
        if (getSkinnable().isLcdVisible()) {
            valueText.setTranslateX((0.691 * size - valueText.getLayoutBounds().getWidth()));
        } else {
            valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
        }
    }

    private void drawGradientBar() {
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.115 * size : 0.0515 * size;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? size * 0.77 : size * 0.897;
        double            offset            = 90 - startAngle;
        ScaleDirection    scaleDirection    = getSkinnable().getScaleDirection();
        List<Stop>        stops             = getSkinnable().getGradientBarStops();
        Map<Double, Color> stopAngleMap     = new HashMap<>(stops.size());
        for (Stop stop : stops) { stopAngleMap.put(stop.getOffset() * angleRange, stop.getColor()); }
        double                  offsetFactor        = ScaleDirection.CLOCKWISE == scaleDirection ? (startAngle - 90) : (startAngle + 180);
        AngleConicalGradient    gradient            = new AngleConicalGradient(size * 0.5, size * 0.5, offsetFactor, stopAngleMap, getSkinnable().getScaleDirection());

        double barStartAngle  = ScaleDirection.CLOCKWISE == scaleDirection ? -minValue * angleStep : minValue * angleStep;
        double barAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? getSkinnable().getRange() * angleStep : -getSkinnable().getRange() * angleStep;
        ticksAndSections.save();
        ticksAndSections.setStroke(gradient.getImagePattern(new Rectangle(xy - 0.026 * size, xy - 0.026 * size, wh + 0.052 * size, wh + 0.052 * size)));
        ticksAndSections.setLineWidth(size * 0.052);
        ticksAndSections.setLineCap(StrokeLineCap.BUTT);
        ticksAndSections.strokeArc(xy, xy, wh, wh, -(offset + barStartAngle), -barAngleExtend, ArcType.OPEN);
        ticksAndSections.restore();
    }

    private void drawSections() {
        if (getSkinnable().getSections().isEmpty()) return;
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.115 * size : 0.0515 * size;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? size * 0.77 : size * 0.897;
        double            offset            = 90 - startAngle;
        ScaleDirection    scaleDirection    = getSkinnable().getScaleDirection();
        int               listSize          = sections.size();
        for (int i = 0 ; i < listSize ; i++) {
            Section section = sections.get(i);
            double sectionStartAngle;
            if (Double.compare(section.getStart(), maxValue) <= 0 && Double.compare(section.getStop(), minValue) >= 0) {
                if (Double.compare(section.getStart(), minValue) < 0 && Double.compare(section.getStop(), maxValue) < 0) {
                    sectionStartAngle = 0;
                } else {
                    sectionStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStart() - minValue) * angleStep : -(section.getStart() - minValue) * angleStep;
                }
                double sectionAngleExtend;
                if (Double.compare(section.getStop(), maxValue) > 0) {
                    sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - section.getStart()) * angleStep : -(maxValue - section.getStart()) * angleStep;
                } else {
                    sectionAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (section.getStop() - section.getStart()) * angleStep : -(section.getStop() - section.getStart()) * angleStep;
                }
                ticksAndSections.save();
                ticksAndSections.setStroke(section.getColor());
                ticksAndSections.setLineWidth(size * 0.052);
                ticksAndSections.setLineCap(StrokeLineCap.BUTT);
                ticksAndSections.strokeArc(xy, xy, wh, wh, -(offset + sectionStartAngle), -sectionAngleExtend, ArcType.OPEN);
                ticksAndSections.restore();
            }
        }
    }

    private void drawAreas() {
        if (getSkinnable().getAreas().isEmpty()) return;
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double            xy                = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.0895 * size : 0.025 * size;
        double            wh                = TickLabelLocation.OUTSIDE == tickLabelLocation ? size * 0.821 : size * 0.95;
        double            offset            = 90 - startAngle;
        ScaleDirection    scaleDirection    = getSkinnable().getScaleDirection();

        int listSize = areas.size();
        for (int i = 0; i < listSize ; i++) {
            Section area = areas.get(i);
            double areaStartAngle;
            if (Double.compare(area.getStart(), maxValue) <= 0 && Double.compare(area.getStop(), minValue) >= 0) {
                if (area.getStart() < minValue && area.getStop() < maxValue) {
                    areaStartAngle = 0;
                } else {
                    areaStartAngle = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStart() - minValue) * angleStep : -(area.getStart() - minValue) * angleStep;
                }
                double areaAngleExtend;
                if (area.getStop() > maxValue) {
                    areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (maxValue - area.getStart()) * angleStep : -(maxValue - area.getStart()) * angleStep;
                } else {
                    areaAngleExtend = ScaleDirection.CLOCKWISE == scaleDirection ? (area.getStop() - area.getStart()) * angleStep : -(area.getStop() - area.getStart()) * angleStep;
                }
                ticksAndSections.save();
                ticksAndSections.setFill(area.getColor());
                ticksAndSections.fillArc(xy, xy, wh, wh, -(offset + areaStartAngle), - areaAngleExtend, ArcType.ROUND);
                ticksAndSections.restore();
            }
        }
    }

    private void drawLed() {
        led.clearRect(0, 0, ledSize, ledSize);

        boolean isFlatLed = LedType.FLAT == getSkinnable().getLedType();

        if (!isFlatLed) {
            led.setFill(ledFramePaint);
            led.fillOval(0, 0, ledSize, ledSize);
        } else {
            double lineWidth = 0.0037037 * size;
            led.setStroke(ledFramePaint);
            led.setLineWidth(lineWidth);
            led.strokeOval(lineWidth, lineWidth, ledSize - 2 * lineWidth, ledSize - 2 * lineWidth);
        }

        led.save();
        if (getSkinnable().isLedOn()) {
            led.setEffect(ledOnShadow);
            led.setFill(ledOnPaint);
        } else {
            led.setEffect(ledOffShadow);
            led.setFill(ledOffPaint);
        }
        if (isFlatLed) {
            led.fillOval(0.2 * ledSize, 0.2 * ledSize, 0.6 * ledSize, 0.6 * ledSize);
        } else {
            led.fillOval(0.14 * ledSize, 0.14 * ledSize, 0.72 * ledSize, 0.72 * ledSize);
        }
        led.restore();

        led.setFill(ledHighlightPaint);
        led.fillOval(0.21 * ledSize, 0.21 * ledSize, 0.58 * ledSize, 0.58 * ledSize);
    }

    private void drawMarkers() {
        markerPane.getChildren().setAll(markerMap.values());
        markerPane.getChildren().add(threshold);
        TickLabelLocation tickLabelLocation = getSkinnable().getTickLabelLocation();
        double         markerSize     = TickLabelLocation.OUTSIDE == tickLabelLocation ? 0.0125 * size : 0.015 * size;
        double         pathHalf       = markerSize * 0.3;
        double         centerX        = size * 0.5;
        double         centerY        = size * 0.5;
        ScaleDirection scaleDirection = getSkinnable().getScaleDirection();
        if (getSkinnable().getMarkersVisible()) {

            for (Map.Entry<Marker, Shape> entry : markerMap.entrySet()) {
                Marker marker = entry.getKey();
                Shape  shape  = entry.getValue();
                double valueAngle;
                if (ScaleDirection.CLOCKWISE == scaleDirection) {
                    valueAngle = startAngle - (marker.getValue() - minValue) * angleStep;
                } else {
                    valueAngle = startAngle + (marker.getValue() - minValue) * angleStep;
                }
                double sinValue = Math.sin(Math.toRadians(valueAngle));
                double cosValue = Math.cos(Math.toRadians(valueAngle));
                switch (marker.getMarkerType()) {
                    case TRIANGLE:
                        Path triangle = (Path) shape;
                        triangle.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                triangle.getElements().add(new MoveTo(centerX + size * 0.38 * sinValue, centerY + size * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                triangle.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                triangle.getElements().add(new MoveTo(centerX + size * 0.465 * sinValue, centerY + size * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                triangle.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                triangle.getElements().add(new ClosePath());
                                break;
                        }
                        break;
                    case DOT:
                        Circle dot = (Circle) shape;
                        dot.setRadius(markerSize);
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                dot.setCenterX(centerX + size * 0.3945 * sinValue);
                                dot.setCenterY(centerY + size * 0.3945 * cosValue);
                                break;
                            default:
                                dot.setCenterX(centerX + size * 0.449 * sinValue);
                                dot.setCenterY(centerY + size * 0.449 * cosValue);
                                break;
                        }
                        break;
                    case STANDARD:
                    default:
                        Path standard = (Path) shape;
                        standard.getElements().clear();
                        switch (tickLabelLocation) {
                            case OUTSIDE:
                                standard.getElements().add(new MoveTo(centerX + size * 0.38 * sinValue, centerY + size * 0.38 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.4575 * sinValue, centerY + size * 0.4575 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.4575 * sinValue, centerY + size * 0.4575 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.4075 * sinValue, centerY + size * 0.4075 * cosValue));
                                standard.getElements().add(new ClosePath());
                                break;
                            case INSIDE:
                            default:
                                standard.getElements().add(new MoveTo(centerX + size * 0.465 * sinValue, centerY + size * 0.465 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle - pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle - pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.386 * sinValue, centerY + size * 0.386 * cosValue));
                                sinValue = Math.sin(Math.toRadians(valueAngle + pathHalf));
                                cosValue = Math.cos(Math.toRadians(valueAngle + pathHalf));
                                standard.getElements().add(new LineTo(centerX + size * 0.386 * sinValue, centerY + size * 0.386 * cosValue));
                                standard.getElements().add(new LineTo(centerX + size * 0.436 * sinValue, centerY + size * 0.436 * cosValue));
                                standard.getElements().add(new ClosePath());
                                break;
                        }
                        break;
                }
                Color markerColor = marker.getColor();
                shape.setFill(markerColor);
                shape.setStroke(markerColor.darker());
                shape.setPickOnBounds(false);
                Tooltip markerTooltip;
                if (marker.getText().isEmpty()) {
                    markerTooltip = new Tooltip(Double.toString(marker.getValue()));
                } else {
                    markerTooltip = new Tooltip(new StringBuilder(marker.getText()).append("\n(").append(Double.toString(marker.getValue())).append(")").toString());
                }
                markerTooltip.setTextAlignment(TextAlignment.CENTER);
                Tooltip.install(shape, markerTooltip);
                shape.setOnMousePressed(e -> marker.fireMarkerEvent(marker.MARKER_PRESSED_EVENT));
                shape.setOnMouseReleased(e -> marker.fireMarkerEvent(marker.MARKER_RELEASED_EVENT));
            }
        }

        if (getSkinnable().isThresholdVisible()) {
            // Draw threshold
            threshold.getElements().clear();
            double thresholdAngle;
            if (ScaleDirection.CLOCKWISE == scaleDirection) {
                thresholdAngle = startAngle - (getSkinnable().getThreshold() - minValue) * angleStep;
            } else {
                thresholdAngle = startAngle + (getSkinnable().getThreshold() - minValue) * angleStep;
            }
            double thresholdSize = Helper.clamp(3d, 3.5, 0.01 * size);
            double sinValue      = Math.sin(Math.toRadians(thresholdAngle));
            double cosValue      = Math.cos(Math.toRadians(thresholdAngle));
            switch (tickLabelLocation) {
                case OUTSIDE:
                    threshold.getElements().add(new MoveTo(centerX + size * 0.38 * sinValue, centerY + size * 0.38 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.34 * sinValue, centerY + size * 0.34 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.34 * sinValue, centerY + size * 0.34 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
                case INSIDE:
                default:
                    threshold.getElements().add(new MoveTo(centerX + size * 0.465 * sinValue, centerY + size * 0.465 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle - thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle - thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.425 * sinValue, centerY + size * 0.425 * cosValue));
                    sinValue = Math.sin(Math.toRadians(thresholdAngle + thresholdSize));
                    cosValue = Math.cos(Math.toRadians(thresholdAngle + thresholdSize));
                    threshold.getElements().add(new LineTo(centerX + size * 0.425 * sinValue, centerY + size * 0.425 * cosValue));
                    threshold.getElements().add(new ClosePath());
                    break;
            }
            threshold.setFill(getSkinnable().getThresholdColor());
            threshold.setStroke(getSkinnable().getTickMarkColor());
        }
    }

    private void updateMarkers() {
        markerMap.clear();
        for (Marker marker : getSkinnable().getMarkers()) {
            switch(marker.getMarkerType()) {
                case TRIANGLE: markerMap.put(marker, new Path()); break;
                case DOT     : markerMap.put(marker, new Circle()); break;
                case STANDARD:
                default:       markerMap.put(marker, new Path()); break;
            }
        }
    }

    private void drawKnob(final boolean PRESSED) {
        knobCanvas.setCache(false);
        double w = knobCanvas.getWidth();
        double h = knobCanvas.getHeight();
        knob.clearRect(0, 0, w, h);

        Color  knobColor = getSkinnable().getKnobColor();
        double hue       = knobColor.getHue();
        double sat       = knobColor.getSaturation();
        double alp       = knobColor.getOpacity();
        double brg       = Color.BLACK.equals(knobColor) ? 0.2 : knobColor.getBrightness();
        double gradTop;
        double gradBot;

        switch (getSkinnable().getKnobType()) {
            case PLAIN:
                knob.setFill(new LinearGradient(0, 0, 0, h, false, CycleMethod.NO_CYCLE,
                                               new Stop(0.0, Color.rgb(180,180,180)),
                                               new Stop(0.46, Color.rgb(63,63,63)),
                                               new Stop(1.0, Color.rgb(40,40,40))));
                knob.fillOval(0, 0, w, h);

                knob.setFill(new LinearGradient(0, 0.11764706 * h, 0, 0.76470588 * h, false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.9 : brg * 1.0, alp)),
                                                new Stop(0.01, Color.hsb(hue, sat, PRESSED ? brg * 0.75 : brg * 0.85, alp)),
                                                new Stop(0.5, Color.hsb(hue, sat, PRESSED ? brg * 0.4 : brg * 0.5, alp)),
                                                new Stop(0.51, Color.hsb(hue, sat, PRESSED ? brg * 0.35 : brg * 0.45, alp)),
                                                new Stop(1.0, Color.hsb(hue, sat, PRESSED ? brg * 0.7 : brg * 0.8, alp))));
                knob.fillOval(w * 0.11764706, h * 0.11764706, w - w * 0.23529412, h - h * 0.23529412);

                knob.setFill(new RadialGradient(0, 0, 0.5 * w, 0.47 * h, w * 0.38, false, CycleMethod.NO_CYCLE,
                                               new Stop(0, Color.TRANSPARENT),
                                               new Stop(0.76, Color.TRANSPARENT),
                                               new Stop(1.0, Color.rgb(0, 0, 0, PRESSED ? 0.5 : 0.2))));
                knob.fillOval(w * 0.11764706, h * 0.11764706, w - w * 0.23529412, h - h * 0.23529412);
                break;
            case METAL:
                knob.setFill(new LinearGradient(0, 0, 0, h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(92,95,101)),
                                                new Stop(0.47, Color.rgb(46,49,53)),
                                                new Stop(1.0, Color.rgb(22,23,26))));
                knob.fillOval(0, 0, w, h);

                knob.setFill(new LinearGradient(0, 0.058823529411764705 * h, 0, 0.9411764705882353 * h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.7 : brg * 0.9, alp)),
                                                new Stop(0.0, Color.hsb(hue, sat, PRESSED ? brg * 0.3 : brg * 0.5, alp))));
                knob.fillOval(0.05882353 * w, 0.05882353 * h, w * 0.88235294, h * 0.88235294);

                knob.beginPath();
                knob.moveTo(0.17647058823529413 * w, 0.8235294117647058 * h);
                knob.bezierCurveTo(0.29411764705882354 * w, 0.8823529411764706 * h, 0.35294117647058826 * w, 0.9411764705882353 * h, 0.5294117647058824 * w, 0.9411764705882353 * h);
                knob.bezierCurveTo(0.6470588235294118 * w, 0.9411764705882353 * h, 0.7058823529411765 * w, 0.8823529411764706 * h, 0.8235294117647058 * w, 0.8235294117647058 * h);
                knob.bezierCurveTo(0.7647058823529411 * w, 0.7058823529411765 * h, 0.6470588235294118 * w, 0.5882352941176471 * h, 0.5294117647058824 * w, 0.5882352941176471 * h);
                knob.bezierCurveTo(0.35294117647058826 * w, 0.5882352941176471 * h, 0.23529411764705882 * w, 0.7058823529411765 * h, 0.17647058823529413 * w, 0.8235294117647058 * h);
                knob.closePath();
                knob.setFill(new RadialGradient(0, 0,
                                                0.47058823529411764 * w, 0.8823529411764706 * h,
                                                0.3235294117647059 * w,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(255, 255, 255, PRESSED ? 0.3 : 0.6)),
                                                new Stop(1.0, Color.TRANSPARENT)));
                knob.fill();

                knob.beginPath();
                knob.moveTo(0.058823529411764705 * w, 0.29411764705882354 * h);
                knob.bezierCurveTo(0.17647058823529413 * w, 0.35294117647058826 * h, 0.35294117647058826 * w, 0.35294117647058826 * h, 0.5294117647058824 * w, 0.35294117647058826 * h);
                knob.bezierCurveTo(0.6470588235294118 * w, 0.35294117647058826 * h, 0.8235294117647058 * w, 0.35294117647058826 * h, 0.9411764705882353 * w, 0.29411764705882354 * h);
                knob.bezierCurveTo(0.8823529411764706 * w, 0.11764705882352941 * h, 0.7058823529411765 * w, 0.0 * h, 0.5294117647058824 * w, 0.0 * h);
                knob.bezierCurveTo(0.29411764705882354 * w, 0.0 * h, 0.11764705882352941 * w, 0.11764705882352941 * h, 0.058823529411764705 * w, 0.29411764705882354 * h);
                knob.closePath();
                knob.setFill(new RadialGradient(0, 0,
                                                0.47058823529411764 * w, 0.0,
                                                0.4411764705882353 * w,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(255, 255, 255, PRESSED ? 0.45 : 0.75)),
                                                new Stop(1.0, Color.TRANSPARENT)));
                knob.fill();

                knob.setFill(new LinearGradient(0.5294117647058824 * w, 0.23529411764705882 * h,
                                                0.5294117647058824 * w, 0.7647058823529411 * h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.BLACK),
                                                new Stop(1.0, Color.rgb(204, 204, 204))));
                knob.fillOval(0.23529412 * w, 0.23529412 * h, 0.52941176 * w, 0.52941176 * h);

                knob.setFill(new LinearGradient(0.5294117647058824 * w, 0.29411764705882354 * h,
                                                0.5294117647058824 * w, 0.7058823529411765 * h,
                                                false, CycleMethod.NO_CYCLE,
                                                new Stop(0.0, Color.rgb(1,6,11)),
                                                new Stop(1.0, Color.rgb(50,52,56))));
                knob.fillOval(0.29411765 * w, 0.29411765 * h, 0.41176471 * w, 0.41176471 * h);
                break;
            case FLAT:
                double lineWidth = 0.00740741 * size;
                double knobSize  = w - 2 * lineWidth;
                knob.setFill(PRESSED ? knobColor.darker() : knobColor);
                knob.setStroke(PRESSED ? Color.WHITE.darker() : Color.WHITE);
                knob.setLineWidth(lineWidth);
                knob.fillOval(lineWidth, lineWidth, knobSize, knobSize);
                knob.strokeOval(lineWidth, lineWidth, knobSize, knobSize);
                break;
            case STANDARD:
            default:
                knob.setFill(new LinearGradient(0, 0, 0, h,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.rgb(133, 133, 133).brighter().brighter()),
                                                       new Stop(0.52, Color.rgb(133, 133, 133)),
                                                       new Stop(1.0, Color.rgb(133, 133, 133).darker().darker())));
                knob.fillOval(0, 0, w, h);
                gradTop = PRESSED ? h - size * 0.01 : size * 0.005;
                gradBot = PRESSED ? size * 0.005 : h - size * 0.01;
                knob.setFill(new LinearGradient(0,gradTop, 0, gradBot,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.hsb(hue, sat, brg * 0.85, alp)),
                                                       new Stop(0.45, Color.hsb(hue, sat, brg * 0.65, alp)),
                                                       new Stop(1.0, Color.hsb(hue, sat, brg * 0.4, alp))));
                knob.fillOval(size * 0.005, size * 0.005, w - size * 0.01, h - size * 0.01);
                break;
        }
        knobCanvas.setCache(true);
        knobCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private void resizeText() {
        double maxWidth = 0.4 * size;
        double fontSize = 0.06 * size;

        titleText.setFont(Fonts.robotoMedium(fontSize));
        titleText.setText(getSkinnable().getTitle());
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, fontSize); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.26);

        fontSize = 0.05 * size;
        unitText.setFont(Fonts.robotoRegular(fontSize));
        unitText.setText(getSkinnable().getUnit());
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, fontSize); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.35);

        maxWidth = 0.25 * size;
        subTitleText.setFont(Fonts.robotoRegular(fontSize));
        subTitleText.setText(getSkinnable().getSubTitle());
        if (subTitleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(subTitleText, maxWidth, fontSize); }
        subTitleText.relocate((size - subTitleText.getLayoutBounds().getWidth()) * 0.5, size * 0.76);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (size > 0) {
            double center = size * 0.5;

            pane.setMaxSize(size, size);
            pane.relocate((getSkinnable().getWidth() - size) * 0.5, (getSkinnable().getHeight() - size) * 0.5);

            dropShadow.setRadius(0.008 * size);
            dropShadow.setOffsetY(0.008 * size);

            backgroundInnerShadow.setOffsetX(0);
            backgroundInnerShadow.setOffsetY(size * 0.03);
            backgroundInnerShadow.setRadius(size * 0.04);

            pane.setEffect(getSkinnable().isInnerShadowEnabled() ? backgroundInnerShadow : null);

            ticksAndSectionsCanvas.setWidth(size);
            ticksAndSectionsCanvas.setHeight(size);

            markerPane.setPrefSize(size, size);

            boolean isFlatLed = LedType.FLAT == getSkinnable().getLedType();
            ledSize = isFlatLed ? 0.05 * size : 0.06 * size;
            ledCanvas.setWidth(ledSize);
            ledCanvas.setHeight(ledSize);
            ledCanvas.relocate(0.65 * size, 0.47 * size);
            ledOffShadow = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            ledOnShadow  = isFlatLed ? null : new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 0.07 * ledSize, 0, 0, 0);
            if (!isFlatLed) ledOnShadow.setInput(new DropShadow(BlurType.TWO_PASS_BOX, getSkinnable().getLedColor(), 0.36 * ledSize, 0, 0, 0));

            resizeText();

            if (getSkinnable().isLcdVisible()) {
                lcd.setWidth(0.4 * size);
                lcd.setHeight(0.114 * size);
                lcd.setArcWidth(0.0125 * size);
                lcd.setArcHeight(0.0125 * size);
                lcd.relocate((size - lcd.getWidth()) * 0.5, 0.583 * size);

                switch(getSkinnable().getLcdFont()) {
                    case LCD:
                        valueText.setFont(Fonts.digital(0.108 * size));
                        valueText.setTranslateY(0.64 * size);
                        break;
                    case DIGITAL:
                        valueText.setFont(Fonts.digitalReadout(0.105 * size));
                        valueText.setTranslateY(0.65 * size);
                        break;
                    case DIGITAL_BOLD:
                        valueText.setFont(Fonts.digitalReadoutBold(0.105 * size));
                        valueText.setTranslateY(0.65 * size);
                        break;
                    case ELEKTRA:
                        valueText.setFont(Fonts.elektra(0.1116 * size));
                        valueText.setTranslateY(0.645 * size);
                        break;
                    case STANDARD:
                    default:
                        valueText.setFont(Fonts.robotoMedium(0.09 * size));
                        valueText.setTranslateY(0.64 * size);
                        break;
                }
                valueText.setTranslateX((0.691 * size - valueText.getLayoutBounds().getWidth()));
            } else {
                valueText.setFont(Fonts.robotoMedium(size * 0.1));
                valueText.setTranslateX((size - valueText.getLayoutBounds().getWidth()) * 0.5);
                valueText.setTranslateY(size * 0.65);
            }
            
            double needleWidth  = size * getSkinnable().getNeedleSize().FACTOR;
            double needleHeight = TickLabelLocation.OUTSIDE == getSkinnable().getTickLabelLocation() ? size * 0.3965 : size * 0.455;

            needleMoveTo1.setX(0.25 * needleWidth); needleMoveTo1.setY(0.025423728813559324 * needleHeight);

            needleCubicCurveTo2.setControlX1(0.25 * needleWidth); needleCubicCurveTo2.setControlY1(0.00847457627118644 * needleHeight);
            needleCubicCurveTo2.setControlX2(0.375 * needleWidth); needleCubicCurveTo2.setControlY2(0);
            needleCubicCurveTo2.setX(0.5 * needleWidth); needleCubicCurveTo2.setY(0);

            needleCubicCurveTo3.setControlX1(0.625 * needleWidth); needleCubicCurveTo3.setControlY1(0);
            needleCubicCurveTo3.setControlX2(0.75 * needleWidth); needleCubicCurveTo3.setControlY2(0.00847457627118644 * needleHeight);
            needleCubicCurveTo3.setX(0.75 * needleWidth); needleCubicCurveTo3.setY(0.025423728813559324 * needleHeight);

            needleCubicCurveTo4.setControlX1(0.75 * needleWidth); needleCubicCurveTo4.setControlY1(0.025423728813559324 * needleHeight);
            needleCubicCurveTo4.setControlX2(needleWidth); needleCubicCurveTo4.setControlY2(needleHeight);
            needleCubicCurveTo4.setX(needleWidth); needleCubicCurveTo4.setY(needleHeight);

            needleLineTo5.setX(0); needleLineTo5.setY(needleHeight);

            needleCubicCurveTo6.setControlX1(0); needleCubicCurveTo6.setControlY1(needleHeight);
            needleCubicCurveTo6.setControlX2(0.25 * needleWidth); needleCubicCurveTo6.setControlY2(0.025423728813559324 * needleHeight);
            needleCubicCurveTo6.setX(0.25 * needleWidth); needleCubicCurveTo6.setY(0.025423728813559324 * needleHeight);

            needle.relocate(center - needle.getLayoutBounds().getWidth() * 0.5, center - needle.getLayoutBounds().getHeight());
            needleRotate.setPivotX(needle.getLayoutBounds().getWidth() * 0.5);
            needleRotate.setPivotY(needle.getLayoutBounds().getHeight());

            knobCanvas.setWidth(size * 0.1);
            knobCanvas.setHeight(size * 0.1);
            knobCanvas.relocate(center - size * 0.05, center - size * 0.05);

            buttonTooltip.setText(getSkinnable().getButtonTooltipText());
        }
    }

    private void redraw() {
        formatString = new StringBuilder("%.").append(Integer.toString(getSkinnable().getDecimals())).append("f").toString();
        shadowGroup.setEffect(getSkinnable().getShadowsEnabled() ? dropShadow : null);

        // Background stroke and fill
        pane.setBorder(new Border(new BorderStroke(getSkinnable().getBorderPaint(), BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(1))));
        pane.setBackground(new Background(new BackgroundFill(getSkinnable().getBackgroundPaint(), new CornerRadii(1024), Insets.EMPTY)));

        // Areas, Sections and Tick Marks
        ticksAndSectionsCanvas.setCache(false);
        ticksAndSections.clearRect(0, 0, size, size);
        if (getSkinnable().getAreasVisible()) drawAreas();
        if (getSkinnable().isGradientBarEnabled() && getSkinnable().getGradientLookup() != null) {
            drawGradientBar();
        } else if (getSkinnable().getSectionsVisible()) {
            drawSections();
        }
        Helper.drawRadialTickMarks(getSkinnable(), ticksAndSections, minValue, maxValue, startAngle, angleRange, angleStep, size * 0.5, size * 0.5, size);
        ticksAndSectionsCanvas.setCache(true);
        ticksAndSectionsCanvas.setCacheHint(CacheHint.QUALITY);

        // LED
        if (getSkinnable().isLedVisible()) {
            final Color LED_COLOR = getSkinnable().getLedColor();
            switch(getSkinnable().getLedType()) {
                case FLAT:
                    ledFramePaint = Color.WHITE;
                    ledOnPaint = new LinearGradient(0, 0.25 * ledSize,
                                                    0, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR),
                                                    new Stop(1.0, LED_COLOR.deriveColor(0d, 1d, 0.5, 1d)));
                    ledOffPaint = new LinearGradient(0, 0.25 * ledSize,
                                                     0, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0d, 1d, 0.5, 1d)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0d, 1d, 0.13, 1d)));
                    ledHighlightPaint = Color.TRANSPARENT;
                    break;
                case STANDARD:
                default:
                    ledFramePaint = new LinearGradient(0.14 * ledSize, 0.14 * ledSize,
                                                       0.84 * ledSize, 0.84 * ledSize,
                                                       false, CycleMethod.NO_CYCLE,
                                                       new Stop(0.0, Color.rgb(20, 20, 20, 0.65)),
                                                       new Stop(0.15, Color.rgb(20, 20, 20, 0.65)),
                                                       new Stop(0.26, Color.rgb(41, 41, 41, 0.65)),
                                                       new Stop(0.26, Color.rgb(41, 41, 41, 0.64)),
                                                       new Stop(0.85, Color.rgb(200, 200, 200, 0.41)),
                                                       new Stop(1.0, Color.rgb(200, 200, 200, 0.35)));
                    ledOnPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                       0.74 * ledSize, 0.74 * ledSize,
                                                    false, CycleMethod.NO_CYCLE,
                                                    new Stop(0.0, LED_COLOR.deriveColor(0d, 1d, 0.77, 1d)),
                                                    new Stop(0.49, LED_COLOR.deriveColor(0d, 1d, 0.5, 1d)),
                                                    new Stop(1.0, LED_COLOR));
                    ledOffPaint = new LinearGradient(0.25 * ledSize, 0.25 * ledSize,
                                                        0.74 * ledSize, 0.74 * ledSize,
                                                     false, CycleMethod.NO_CYCLE,
                                                     new Stop(0.0, LED_COLOR.deriveColor(0d, 1d, 0.20, 1d)),
                                                     new Stop(0.49, LED_COLOR.deriveColor(0d, 1d, 0.13, 1d)),
                                                     new Stop(1.0, LED_COLOR.deriveColor(0d, 1d, 0.2, 1d)));
                    ledHighlightPaint = new RadialGradient(0, 0,
                                                           0.3 * ledSize, 0.3 * ledSize,
                                                           0.29 * ledSize,
                                                           false, CycleMethod.NO_CYCLE,
                                                           new Stop(0.0, Color.WHITE),
                                                           new Stop(1.0, Color.TRANSPARENT));
                    break;
            }
            drawLed();
        }

        // LCD
        LcdDesign lcdDesign = getSkinnable().getLcdDesign();
        Color[]   lcdColors = lcdDesign.getColors();
        if (getSkinnable().isLcdVisible() && getSkinnable().isValueVisible()) {
            LinearGradient lcdGradient = new LinearGradient(0, 1, 0, lcd.getHeight() - 1,
                                                            false, CycleMethod.NO_CYCLE,
                                                            new Stop(0, lcdColors[0]),
                                                            new Stop(0.03, lcdColors[1]),
                                                            new Stop(0.5, lcdColors[2]),
                                                            new Stop(0.5, lcdColors[3]),
                                                            new Stop(1.0, lcdColors[4]));
            Paint lcdFramePaint;
            if (LcdDesign.FLAT_CUSTOM == lcdDesign) {
                lcdFramePaint = lcdDesign.lcdForegroundColor;
            } else {
                lcdFramePaint = new LinearGradient(0, 0, 0, lcd.getHeight(),
                                                   false, CycleMethod.NO_CYCLE,
                                                   new Stop(0.0, Color.rgb(26, 26, 26)),
                                                   new Stop(0.01, Color.rgb(77, 77, 77)),
                                                   new Stop(0.99, Color.rgb(77, 77, 77)),
                                                   new Stop(1.0, Color.rgb(221, 221, 221)));
            }
            lcd.setFill(lcdGradient);
            lcd.setStroke(lcdFramePaint);
        }

        // Text
        titleText.setFill(getSkinnable().getTitleColor());
        unitText.setFill(getSkinnable().getUnitColor());
        subTitleText.setFill(getSkinnable().getSubTitleColor());
        valueText.setFill(getSkinnable().isLcdVisible() ? lcdColors[5] : getSkinnable().getValueColor());
        resizeText();

        // Needle
        Color needleColor = getSkinnable().getNeedleColor();
        switch(getSkinnable().getNeedleShape()) {
            case ROUND:
                needlePaint = new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                                 needle.getLayoutBounds().getMaxX(), 0,
                                                 false, CycleMethod.NO_CYCLE,
                                                 new Stop(0.0, needleColor.darker()),
                                                 new Stop(0.5, needleColor.brighter().brighter()),
                                                 new Stop(1.0, needleColor.darker()));
                needle.setStrokeWidth(0);
                needle.setStroke(Color.TRANSPARENT);
                break;
            case FLAT:
                needlePaint = needleColor;
                needle.setStrokeWidth(0.0037037 * size);
                needle.setStroke(Color.WHITE);
                break;
            case ANGLED:
            default:
                needlePaint = new LinearGradient(needle.getLayoutBounds().getMinX(), 0,
                                                 needle.getLayoutBounds().getMaxX(), 0,
                                                 false, CycleMethod.NO_CYCLE,
                                                 new Stop(0.0, needleColor.darker()),
                                                 new Stop(0.5, needleColor.darker()),
                                                 new Stop(0.5, needleColor.brighter()),
                                                 new Stop(1.0, needleColor.brighter()));
                needle.setStrokeWidth(0);
                needle.setStroke(Color.TRANSPARENT);
                break;
        }
        needle.setFill(needlePaint);

        // Knob
        drawKnob(false);

        // Markers
        drawMarkers();
        thresholdTooltip.setText("Threshold\n(" + String.format(Locale.US, formatString, getSkinnable().getThreshold()) + ")");
    }
}
