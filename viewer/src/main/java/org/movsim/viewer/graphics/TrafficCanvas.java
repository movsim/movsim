/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 *
 * This file is part of
 *
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 *
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.viewer.graphics;

import org.apache.commons.lang3.StringUtils;
import org.movsim.input.ProjectMetaData;
import org.movsim.roadmappings.PosTheta;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.boundaries.AbstractTrafficSource;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSink;
import org.movsim.simulator.roadnetwork.controller.FlowConservingBottleneck;
import org.movsim.simulator.roadnetwork.controller.GradientProfile;
import org.movsim.simulator.roadnetwork.controller.SpeedLimit;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.roadnetwork.regulator.NotifyObject;
import org.movsim.simulator.roadnetwork.regulator.Regulator;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Colors;
import org.movsim.utilities.Units;
import org.movsim.viewer.roadmapping.PaintRoadMapping;
import org.movsim.viewer.ui.ViewProperties;
import org.movsim.viewer.util.SwingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * <p>
 * TrafficCanvas class.
 * </p>
 * <p>
 * <p>
 * Handles:
 * <ul>
 * <li>Drawing the road network and the vehicles upon it.</li>
 * <li>Standard mouse events.</li>
 * <li>Mouse-over and clicks on vehicles (when simulation paused).</li>
 * <li>Key events.</li>
 * </ul>
 * </p>
 * <p>
 * The vehicles are redrawn in their new positions in the drawForeground() method, which is indirectly invoked from repaint(). The
 * drawForeground() method has a synchronization lock so that vehicles are not updated or removed while they are being drawn.
 * </p>
 * <p>
 * Actual road networks and traffic scenarios should be set up in a subclass.
 * </p>
 */
public class TrafficCanvas extends SimulationCanvasBase
        implements SimulationRunnable.UpdateDrawingCallback, SimulationRunnable.HandleExceptionCallback {

    private static final long serialVersionUID = 7637533802145001440L;

    private static final Logger LOG = LoggerFactory.getLogger(TrafficCanvas.class);

    private static final String FONT_NAME = "SansSerif";

    protected final Simulator simulator;
    protected final RoadNetwork roadNetwork;
    private Properties properties;

    /**
     * Callbacks from this TrafficCanvas to the application UI.
     */
    public interface StatusControlCallbacks {
        /**
         * Callback to get the UI to display a status message.
         *
         * @param message the status message
         */
        void showStatusMessage(String message);

        void stateChanged();
    }

    protected StatusControlCallbacks statusControlCallbacks;

    // pre-allocate vehicle drawing path
    private final GeneralPath vehiclePath = new GeneralPath();

    // pre-allocate clipping path for road mappings
    private final GeneralPath clipPath = new GeneralPath(Path2D.WIND_EVEN_ODD);

    // colors
    private Color roadColor;
    private Color roadEdgeColor;
    private Color roadLineColor;
    private Color sourceColor;
    private Color sinkColor;

    private double vmaxForColorSpectrum;

    private boolean drawRoadId;
    private boolean drawSources;
    private boolean drawSinks;
    private boolean drawSpeedLimits;
    private boolean drawSlopes;
    private boolean drawFlowConservingBottlenecks;
    private boolean drawNotifyObjects;

    private BufferedImage backgroundPicture;

    // brake light handling
    private Color brakeLightColor = Color.RED;

    private float lineWidth;
    private float lineLength;
    private float gapLength;

    /**
     * Vehicle color support only the first four are used by the button. commandCyclevehicleColors()
     */
    public enum VehicleColorMode {
        VELOCITY_COLOR,
        LANE_CHANGE,
        ACCELERATION_COLOR,
        VEHICLE_LABEL_COLOR,
        VEHICLE_COLOR,
        EXIT_COLOR,
        HIGHLIGHT_VEHICLE
    }

    /**
     * Color mode displayed on startup
     */
    protected VehicleColorMode vehicleColorMode = VehicleColorMode.VELOCITY_COLOR;
    protected VehicleColorMode vehicleColorModeSave;

    protected double[] velocities;

    protected Color[] accelerationColors;
    protected final Map<String, Color> labelColors = new HashMap<>();

    private static final double[] ACCELERATIONS = new double[]{-7.5, -0.1, 0.2};

    /**
     * vehicle mouse-over support
     */
    String popupString;
    String popupStringExitEndRoad;
    protected Vehicle vehiclePopup;
    protected MouseOverTipWindow mouseOverTipWindow;
    final TrafficCanvasMouseListener mouseListener;
    final TrafficCanvasKeyListener controller;

    protected long lastVehicleViewed = -1;
    protected long vehicleToHighlightId = -1;

    public TrafficCanvas(Simulator simulator, Properties properties) {
        super(simulator.getSimulationRunnable());
        this.simulator = simulator;
        this.roadNetwork = simulator.getRoadNetwork();
        this.properties = properties;

        initGraphicConfigFieldsFromProperties();

        simulationRunnable.setUpdateDrawingCallback(this);
        simulationRunnable.setHandleExceptionCallback(this);

        setStatusControlCallbacks(statusControlCallbacks);

        controller = new TrafficCanvasKeyListener(this, roadNetwork);
        addKeyListener(controller);

        mouseListener = new TrafficCanvasMouseListener(this, controller, roadNetwork);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
        addMouseWheelListener(mouseListener);
    }

    /**
     * Returns the traffic canvas controller.
     *
     * @return the traffic canvas controller
     */
    public TrafficCanvasController controller() {
        return controller;
    }

    protected void initGraphicConfigFieldsFromProperties() {
        setDrawRoadId(Boolean.parseBoolean(properties.getProperty("drawRoadId")));
        setDrawSinks(Boolean.parseBoolean(properties.getProperty("drawSinks")));
        setDrawSources(Boolean.parseBoolean(properties.getProperty("drawSources")));
        setDrawSlopes(Boolean.parseBoolean(properties.getProperty("drawSlopes")));
        setDrawFlowConservingBottlenecks(Boolean.parseBoolean(properties.getProperty("drawFlowConservingBottlenecks")));
        setDrawSpeedLimits(Boolean.parseBoolean(properties.getProperty("drawSpeedLimits")));
        setDrawNotifyObjects(Boolean.parseBoolean(properties.getProperty("drawNotifyObjects")));

        final int hexRadix = 16;
        roadColor = new Color(Integer.parseInt(properties.getProperty("roadColor"), hexRadix));
        roadEdgeColor = new Color(Integer.parseInt(properties.getProperty("roadEdgeColor"), hexRadix));
        roadLineColor = new Color(Integer.parseInt(properties.getProperty("roadLineColor"), hexRadix));
        sourceColor = new Color(Integer.parseInt(properties.getProperty("sourceColor"), hexRadix));
        sinkColor = new Color(Integer.parseInt(properties.getProperty("sinkColor"), hexRadix));
        setVehicleColorMode(VehicleColorMode.valueOf(properties.getProperty("vehicleColorMode")));

        setVmaxForColorSpectrum(Double.parseDouble(properties.getProperty("vmaxForColorSpectrum")));

        lineWidth = Float.parseFloat(properties.getProperty("lineWidth"));
        lineLength = Float.parseFloat(properties.getProperty("lineLength"));
        gapLength = Float.parseFloat(properties.getProperty("gapLength"));

        scale = Double.parseDouble(properties.getProperty("initialScale"));
        setSleepTime(Integer.parseInt(properties.getProperty("initial_sleep_time")));

        setBackgroundColor(new Color(Integer.parseInt(properties.getProperty("backgroundColor"), hexRadix)));
        setBackgroundPicturePath(properties.getProperty("backgroundPicturePath"));
    }

    @Override
    void stateChanged() {
        if (statusControlCallbacks != null) {
            statusControlCallbacks.stateChanged();
        }
    }

    @Override
    public void reset() {
        super.reset();
        simulator.initialize();
        simulator.reset();
        vehicleToHighlightId = -1;
        initGraphicSettings();
        forceRepaintBackground();
    }

    @Override
    public void resetScaleAndOffset() {
        scale = Double.parseDouble(properties.getProperty("initialScale"));
        xOffset = Integer.parseInt(properties.getProperty("xOffset"));
        yOffset = Integer.parseInt(properties.getProperty("yOffset"));
        setTransform();
    }

    protected void toogleDrawRoadId() {
        drawRoadId = !drawRoadId;
    }

    public void setupTrafficScenario(String scenario, String path) {
        properties = ViewProperties.loadProperties(scenario, path);
        reset();
    }

    private void initGraphicSettings() {
        initGraphicConfigFieldsFromProperties();
        resetScaleAndOffset();
        for (final RoadSegment roadSegment : roadNetwork) {
            roadSegment.roadMapping().setRoadColor(roadColor.getRGB());
        }
        for (String vehicleTypeLabel : simulator.getVehiclePrototypeLabels()) {
            final Color color = new Color(Colors.randomColor());
            LOG.info("set color for vehicle label={}", vehicleTypeLabel);
            labelColors.put(vehicleTypeLabel, color);
        }

        backgroundPicture = readBackgroundImage(backgroundPicturePath);
    }

    private BufferedImage readBackgroundImage(String filename) {
        if (StringUtils.isNotBlank(filename)) {
            try {
                String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
                if (ProjectMetaData.getInstance().hasPathToProjectFile()) {
                    currentPath = ProjectMetaData.getInstance().getPathToProjectFile();
                }
                File file = new File(currentPath, filename);
                LOG.info("background image file parent={}, name={}", file.getParent(), file.getName());
                return ImageIO.read(file);
            } catch (Exception e) {
                LOG.error("cannot load background image " + filename, e);
            }
        }
        return null;
    }

    /**
     * Sets the status callback functions.
     *
     * @param statusCallbacks
     */
    public void setStatusControlCallbacks(StatusControlCallbacks statusCallbacks) {
        this.statusControlCallbacks = statusCallbacks;
    }

    /**
     * Sets the (locale dependent) message strings.
     *
     * @param popupString            popup window format string for vehicle that leaves road segment at a specific exit
     * @param popupStringExitEndRoad popup window format string for vehicle that leaves road segment at end
     */
    public void setMessageStrings(String popupString, String popupStringExitEndRoad) {
        this.popupString = popupString;
        this.popupStringExitEndRoad = popupStringExitEndRoad;
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad, String trafficInflowString,
                                  String perturbationRampingFinishedString, String perturbationAppliedString, String simulationFinished) {
        setMessageStrings(popupString, popupStringExitEndRoad);
    }

    void setAccelerationColors() {
        accelerationColors = new Color[]{Color.WHITE, Color.RED, Color.BLACK, Color.GREEN};
        assert ACCELERATIONS.length == accelerationColors.length - 1;
    }

    public double getVmaxForColorSpectrum() {
        return vmaxForColorSpectrum;
    }

    public void setVmaxForColorSpectrum(double vmaxForColorSpectrum) {
        this.vmaxForColorSpectrum = vmaxForColorSpectrum;
    }

    public boolean isDrawRoadId() {
        return drawRoadId;
    }

    public void setDrawRoadId(boolean drawRoadId) {
        this.drawRoadId = drawRoadId;
        repaint();
    }

    /**
     * @return the drawSouces
     */
    public boolean isDrawSources() {
        return drawSources;
    }

    /**
     * @return the drawSinks
     */
    public boolean isDrawSinks() {
        return drawSinks;
    }

    /**
     * @return the drawSpeedLimits
     */
    public boolean isDrawSpeedLimits() {
        return drawSpeedLimits;
    }

    /**
     * @return the drawSlopes
     */
    public boolean isDrawSlopes() {
        return drawSlopes;
    }

    public boolean isDrawFlowConservingBottlenecks() {
        return drawFlowConservingBottlenecks;
    }

    public void setDrawSources(boolean b) {
        this.drawSources = b;
        repaint();
    }

    public void setDrawSinks(boolean b) {
        this.drawSinks = b;
        repaint();
    }

    public void setDrawSpeedLimits(boolean b) {
        this.drawSpeedLimits = b;
        repaint();
    }

    public void setDrawSlopes(boolean b) {
        this.drawSlopes = b;
        repaint();
    }

    public void setDrawFlowConservingBottlenecks(boolean b) {
        this.drawFlowConservingBottlenecks = b;
        repaint();
    }

    public boolean isDrawNotifyObjects() {
        return drawNotifyObjects;
    }

    public void setDrawNotifyObjects(boolean drawNotifyObjects) {
        this.drawNotifyObjects = drawNotifyObjects;
        repaint();
    }

    /**
     * Returns the color of the vehicle. The color may depend on the vehicle's properties, such as its velocity.
     *
     * @param vehicle
     * @param simulationTime
     */
    protected Color vehicleColor(Vehicle vehicle) {
        Color color;

        switch (vehicleColorMode) {
            case ACCELERATION_COLOR:
                final double a = vehicle.physicalQuantities().getAcc();
                final int count = ACCELERATIONS.length;
                for (int i = 0; i < count; ++i) {
                    if (a < ACCELERATIONS[i])
                        return accelerationColors[i];
                }
                color = accelerationColors[accelerationColors.length - 1];
                break;
            case EXIT_COLOR:
                color = Color.BLACK;
                if (vehicle.exitRoadSegmentId() != Vehicle.ROAD_SEGMENT_ID_NOT_SET) {
                    color = Color.WHITE;
                }
                break;
            case HIGHLIGHT_VEHICLE:
                color = vehicle.getId() == vehicleToHighlightId ? Color.BLUE : Color.BLACK;
                break;
            case LANE_CHANGE:
                color = Color.BLACK;
                if (vehicle.inProcessOfLaneChange()) {
                    color = Color.ORANGE;
                }
                break;
            case VEHICLE_COLOR:
                // use vehicle's cache for AWT color object
                color = (Color) vehicle.colorObject();
                if (color == null) {
                    int vehColorInt = vehicle.color();
                    color = new Color(Colors.red(vehColorInt), Colors.green(vehColorInt), Colors.blue(vehColorInt));
                    vehicle.setColorObject(color);
                }
                break;
            case VEHICLE_LABEL_COLOR:
                String label = vehicle.getLabel();
                color = labelColors.containsKey(label) ? labelColors.get(label) : Color.WHITE;
                break;
            case VELOCITY_COLOR:
                double v = vehicle.physicalQuantities().getSpeed() * 3.6;
                color = SwingHelper.getColorAccordingToSpectrum(0, getVmaxForColorSpectrum(), v);
                break;
            default:
                throw new IllegalStateException("unknown vehicleColorMode" + vehicleColorMode);
        }
        return color;
    }

    public void setVehicleColorMode(VehicleColorMode vehicleColorMode) {
        this.vehicleColorMode = vehicleColorMode;
    }

    /**
     * Callback to allow the application to make any further required drawing after the vehicles have been moved.
     */
    protected void drawAfterVehiclesMoved(Graphics2D g, double simulationTime, long iterationCount) {
    }

    /**
     * <p>
     * Draws the foreground: everything that moves each timestep. For the traffic simulation that means draw all the vehicles:<br />
     * For each roadSection, draw all the vehicles in the roadSection, positioning them using the roadMapping for that roadSection.
     * </p>
     * <p>
     * <p>
     * This method is synchronized with the <code>SimulationRunnable.run()</code> method, so that vehicles are not updated, added or removed
     * while they are being drawn.
     * </p>
     * <p>
     * tm The abstract method paintAfterVehiclesMoved is called after the vehicles have been moved, to allow any further required drawing on
     * the canvas.
     * </p>
     */
    @Override
    protected void drawForeground(Graphics2D g) {
        // moveVehicles occurs in the UI thread, so must synchronize with the
        // update of the road network in the calculation thread.
        final long timeBeforePaint_ms = System.currentTimeMillis();
        synchronized (simulationRunnable.dataLock) {
            drawTrafficLights(g);
            for (final RoadSegment roadSegment : roadNetwork) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                assert roadMapping != null;
                PaintRoadMapping.setClipPath(g, roadMapping, clipPath);
                for (final Vehicle vehicle : roadSegment) {
                    drawVehicle(g, roadMapping, vehicle);
                }
                for (Iterator<Vehicle> vehIter = roadSegment.overtakingVehicles(); vehIter.hasNext(); ) {
                    Vehicle vehicle = vehIter.next();
                    drawVehicle(g, roadMapping, vehicle);
                }
            }
            totalAnimationTime += System.currentTimeMillis() - timeBeforePaint_ms;
            drawAfterVehiclesMoved(g, simulationRunnable.simulationTime(), simulationRunnable.iterationCount());
        }
    }

    private void drawVehicle(Graphics2D g, RoadMapping roadMapping, Vehicle vehicle) {
        // draw vehicle polygon at new position
        final RoadMapping.PolygonFloat polygon = roadMapping.mapFloat(vehicle);
        vehiclePath.reset();
        vehiclePath.moveTo(polygon.getXPoint(0), polygon.getYPoint(0));
        vehiclePath.lineTo(polygon.getXPoint(1), polygon.getYPoint(1));
        vehiclePath.lineTo(polygon.getXPoint(2), polygon.getYPoint(2));
        vehiclePath.lineTo(polygon.getXPoint(3), polygon.getYPoint(3));
        vehiclePath.closePath();
        g.setPaint(vehicleColor(vehicle));
        g.fill(vehiclePath);
        if (vehicle.isBrakeLightOn()) {
            // if the vehicle is decelerating then display the
            vehiclePath.reset();
            // points 2 & 3 are at the rear of vehicle
            if (roadMapping.isPeer()) {
                vehiclePath.moveTo(polygon.getXPoint(0), polygon.getYPoint(0));
                vehiclePath.lineTo(polygon.getXPoint(1), polygon.getYPoint(1));
            } else {
                vehiclePath.moveTo(polygon.getXPoint(2), polygon.getYPoint(2));
                vehiclePath.lineTo(polygon.getXPoint(3), polygon.getYPoint(3));
            }
            vehiclePath.closePath();
            g.setPaint(brakeLightColor);
            g.draw(vehiclePath);
        }
    }

    /**
     * Draws the background: everything that does not move each timestep. The background consists of the road segments and the sources and
     * sinks, if they are visible.
     */
    @Override
    protected void drawBackground(Graphics2D g) {

        if (backgroundPicture != null) {
            int height = backgroundPicture.getHeight();
            int width = backgroundPicture.getWidth();
            g.drawImage(backgroundPicture, 0, -height, (int) (width * 1.01), 0, 0, 0, width, height, null);
        }

        drawRoadSegmentsAndLines(g);

        if (drawSources) {
            drawSources(g);
        }

        if (drawSinks) {
            drawSinks(g);
        }

        if (drawSpeedLimits) {
            drawSpeedLimits(g);
        }

        if (drawSlopes) {
            drawSlopes(g);
        }

        if (drawFlowConservingBottlenecks) {
            drawFlowConservingBottlenecks(g);
        }

        if (drawRoadId) {
            drawRoadSectionIds(g);
        }

        if (drawNotifyObjects) {
            drawNotifyObjects(g);
        }
    }

    /**
     * Draws each road segment in the road network.
     */
    private void drawRoadSegmentsAndLines(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            if (roadMapping.isPeer()) {
                LOG.debug("skip painting peer element={}", roadMapping);
                continue;
            }
            TrafficCanvasUtils.drawRoadSegment(g, roadMapping);
            drawRoadSegmentLines(g, roadMapping);
        }
    }

    /**
     * Draws the road lines and road edges.
     *
     * @param g
     */
    private void drawRoadSegmentLines(Graphics2D g, RoadMapping roadMapping) {
        final float dashPhase = (float) (roadMapping.roadLength() % (lineLength + gapLength));

        final Stroke lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                new float[]{lineLength, gapLength}, dashPhase);
        g.setStroke(lineStroke);
        g.setColor(roadLineColor);

        // draw the road lines: left lane is positive
        int maxRightLane = -roadMapping.getLaneGeometries().getRight().getLaneCount();
        int maxLeftLane = roadMapping.getLaneGeometries().getLeft().getLaneCount();
        for (int lane = maxRightLane + 1; lane < maxLeftLane; lane++) {
            final double offset = lane * roadMapping.getLaneGeometries().getLaneWidth();
            LOG.debug("draw road lines: lane={}, offset={}", lane, offset);
            // FIXME after reimpl
            // if (lane == roadMapping.trafficLaneMin() || lane == roadMapping.trafficLaneMax()) {
            // // use exit stroke pattern for on-ramps, off-ramps etc
            // final Stroke exitStroke = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
            // 10.0f, new float[] { 5.0f, gapLengthExit }, 5.0f);
            // g.setStroke(exitStroke);
            // } else {
            g.setStroke(lineStroke);
            // }
            PaintRoadMapping.paintRoadMapping(g, roadMapping, offset);
        }

        // draw the road edges
        g.setStroke(new BasicStroke());
        g.setColor(roadEdgeColor);
        double offset = roadMapping.getLaneGeometries().getLeft().getLaneCount() * roadMapping.getLaneGeometries()
                .getLaneWidth();
        LOG.debug("draw road left outer edge: offset={}", offset);
        PaintRoadMapping.paintRoadMapping(g, roadMapping, offset);
        // edge of most outer edge
        offset = roadMapping.getMaxOffsetRight();
        LOG.debug("draw road most outer edge: offset={}", offset);
        PaintRoadMapping.paintRoadMapping(g, roadMapping, offset);
    }

    private void drawTrafficLights(Graphics2D g) {
        int strokeWidth = 3;
        for (final RoadSegment roadSegment : roadNetwork) {
            assert roadSegment.trafficLights() != null;
            for (TrafficLight trafficLight : roadSegment.trafficLights()) {
                Color color = getTrafficLightColor(trafficLight);
                TrafficCanvasUtils.drawLine(g, roadSegment.roadMapping(), trafficLight.position(), strokeWidth, color);
            }
        }
    }

    private static Color getTrafficLightColor(TrafficLight trafficLight) {
        Color color = null;
        switch (trafficLight.status()) {
            case GREEN:
                color = Color.GREEN;
                break;
            case GREEN_RED:
                color = Color.YELLOW;
                break;
            case RED:
                color = Color.RED;
                break;
            case RED_GREEN:
                color = Color.ORANGE;
                break;
        }
        return color;
    }

    /**
     * Draws the ids for the road sections, sources and sinks.
     *
     * @param g
     */
    private void drawRoadSectionIds(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double position = roadMapping.isPeer() ? roadMapping.roadLength() : 0.0;
            final double offset = roadMapping.isPeer() ?
                    roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1) :
                    roadMapping.getMaxOffsetRight();
            final PosTheta posTheta = roadMapping.map(position, offset);
            final int fontHeight = 12;
            final Font font = new Font(FONT_NAME, Font.PLAIN, fontHeight);
            g.setFont(font);
            g.setColor(Color.BLACK);
            TrafficCanvasUtils.drawTextRotated(roadSegment.userId(), posTheta, font, g);
        }
    }

    private void drawSpeedLimits(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            assert roadSegment.speedLimits() != null;
            final int fontHeight = 12;
            final Font font = new Font(FONT_NAME, Font.BOLD, fontHeight);
            final RoadMapping roadMapping = roadSegment.roadMapping();
            for (SpeedLimit speedLimit : roadSegment.speedLimits()) {
                g.setFont(font);
                final double position = speedLimit.position();
                final double offset = roadMapping.isPeer() ?
                        roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1) :
                        roadMapping.getMaxOffsetRight();
                final PosTheta posTheta = roadMapping.map(position, offset);
                final String text = String.valueOf((int) (speedLimit.getSpeedLimitKmh())) + "km/h";
                g.setFont(font);
                Color color =
                        speedLimit.getSpeedLimit() < MovsimConstants.MAX_VEHICLE_SPEED ? Color.RED : Color.DARK_GRAY;
                g.setColor(color);
                TrafficCanvasUtils.drawTextRotated(text, posTheta, font, g);
                TrafficCanvasUtils.drawLine(g, roadMapping, position, 1, color);
            }
        }
    }

    private void drawSlopes(Graphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final int fontHeight = 12;
            final Font font = new Font(FONT_NAME, Font.BOLD, fontHeight); //$NON-NLS-1$
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double offset = roadMapping.isPeer() ?
                    roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1) :
                    roadMapping.getMaxOffsetRight();
            for (GradientProfile gradientProfile : roadSegment.gradientProfiles()) {
                for (Entry<Double, Double> gradientEntry : gradientProfile.gradientEntries()) {
                    final double position = gradientEntry.getKey();
                    final PosTheta posTheta = roadMapping.map(position, offset);
                    final double gradient = gradientEntry.getValue() * 100;
                    final String text = String.valueOf((int) (gradient)) + "%";
                    TrafficCanvasUtils.drawTextRotated(text, posTheta, font, g);
                    TrafficCanvasUtils.drawLine(g, roadMapping, position, 1, Color.DARK_GRAY);
                }
            }
        }
    }

    private void drawFlowConservingBottlenecks(Graphics2D g) {
        final int fontHeight = 12;
        final Font font = new Font(FONT_NAME, Font.BOLD, fontHeight); //$NON-NLS-1$
        final Color color = Color.ORANGE;
        final Color prevColor = g.getColor();
        g.setColor(color);
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double offset = roadMapping.isPeer() ?
                    roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1) :
                    roadMapping.getMaxOffsetRight();
            for (FlowConservingBottleneck bottleneck : roadSegment.flowConservingBottlenecks()) {
                final double posStart = bottleneck.position();
                PosTheta posTheta = roadMapping.map(posStart, offset);
                TrafficCanvasUtils.drawTextRotated(" bneck start", posTheta, font, g);
                TrafficCanvasUtils.drawLine(g, roadMapping, posStart, 2, color);

                final double posEnd = bottleneck.endPosition();
                posTheta = roadMapping.map(posEnd, offset);
                TrafficCanvasUtils.drawTextRotated(" bneck end", posTheta, font, g);
                TrafficCanvasUtils.drawLine(g, roadMapping, bottleneck.endPosition(), 2, color);
            }

        }
        g.setColor(prevColor);
    }

    /**
     * Draws the sources.
     *
     * @param g
     */
    private void drawSources(Graphics2D g) {
        for (RoadSegment roadSegment : roadNetwork) {
            AbstractTrafficSource trafficSource = roadSegment.trafficSource();
            if (trafficSource != null) {
                TrafficCanvasUtils.drawLine(g, roadSegment.roadMapping(), 0, 4, sourceColor);
            }
        }
    }

    /**
     * Draws the sinks.
     *
     * @param g
     */
    private void drawSinks(Graphics2D g) {
        for (RoadSegment roadSegment : roadNetwork) {
            TrafficSink sink = roadSegment.sink();
            if (sink != null) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                TrafficCanvasUtils.drawLine(g, roadMapping, roadMapping.roadLength(), 4, sinkColor);
            }
        }
    }

    private void drawNotifyObjects(Graphics2D g) {
        for (Regulator regulator : simulator.getRegulators()) {
            for (NotifyObject notifyObject : regulator.getNotifyObjects()) {
                RoadMapping roadMapping = notifyObject.getRoadSegment().roadMapping();
                TrafficCanvasUtils.drawLine(g, roadMapping, notifyObject.getPosition(), 2, Color.DARK_GRAY);
            }
        }
    }

    public void showSinkMouseOverInfo(Point point, TrafficSink sink) {
        StringBuilder sb = new StringBuilder();
        sb.append("outflow: ");
        sb.append((int) (Units.INVS_TO_INVH * sink.measuredOutflow()));
        sb.append(" veh/h");

        mouseOverTipWindow.setVisible(false);
        mouseOverTipWindow.show(point, sb.toString());
    }

    public void showSourceMouseOverInfo(Point point, AbstractTrafficSource source) {
        StringBuilder sb = new StringBuilder();
        sb.append("set inflow: ");
        sb.append((int) (Units.INVS_TO_INVH * source.getTotalInflow(simulationTime())));
        sb.append(" veh/h, actual inflow: ");
        sb.append((int) (Units.INVS_TO_INVH * source.measuredInflow()));
        sb.append(" veh/h, queue: ");
        sb.append(source.getQueueLength());

        mouseOverTipWindow.setVisible(false);
        mouseOverTipWindow.show(point, sb.toString());
    }

    public void showVehicleMouseOverInfo(Point point, Vehicle vehicle) {
        if (vehiclePopup == null || vehiclePopup.getId() != vehicle.getId()) {
            lastVehicleViewed = vehicle.getId();
            mouseOverTipWindow.setVisible(false);
            mouseOverTipWindow.show(point, vehicle);
        }

    }

    // ============================================================================================
    // SimulationRunnable callbacks
    // ============================================================================================

    /**
     * <p>
     * Implements SimulationRunnable.UpdateDrawingCallback.updateDrawing().
     * </p>
     * <p>
     * Calls repaint() which causes UI framework to asynchronously call update(g).
     * </p>
     */
    @Override
    public void updateDrawing(double simulationTime) {
        repaint();
    }

    /**
     * <p>
     * Implements SimulationRunnable.HandleExceptionCallback.handleException().
     * </p>
     * <p>
     * Called back from the TrafficRunnable thread, in the synchronization block, if an exception occurs.
     * </p>
     */
    @Override
    public void handleException(Exception e) {
    }

}
