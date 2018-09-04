package org.movsim.viewer.javafx;


import org.apache.commons.lang3.StringUtils;
import org.jfree.fx.FXGraphics2D;
import org.movsim.autogen.Movsim;
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
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.ui.ViewProperties;
import org.movsim.xml.InputLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class FxSimCanvas extends javafx.scene.canvas.Canvas implements SimulationRunnable.UpdateDrawingCallback, SimulationRunnable.HandleExceptionCallback {

    private static final Logger LOG = LoggerFactory.getLogger(FxSimCanvas.class);

    private final Simulator simulator;
    private SimulationRunnable simulationRunnable = null;
    private long totalAnimationTime;

    private java.awt.Color backgroundColor;
    // optional background picture
    private String backgroundPicturePath;

    // scale factor pixels/m, smaller value means a smaller looking view
    public double scale;
    public int xOffset = 0;
    public int yOffset = 0;

    // Facade to draw with awt on a javafx canvas
    private FXGraphics2D fxGraphics2D;

    private AffineTransform transform = new AffineTransform();
    private final RoadNetwork roadNetwork;
    private Properties properties;

    protected TrafficCanvas.StatusControlCallbacks statusControlCallbacks;

    // pre-allocate vehicle drawing path
    private final GeneralPath vehiclePath = new GeneralPath();

    // pre-allocate clipping path for road mappings
    private final GeneralPath clipPath = new GeneralPath(Path2D.WIND_EVEN_ODD);

    // colors
    private java.awt.Color roadColor;
    private java.awt.Color roadEdgeColor;
    private java.awt.Color roadLineColor;
    private java.awt.Color sourceColor;
    private java.awt.Color sinkColor;

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
    private java.awt.Color brakeLightColor = java.awt.Color.RED;

    private float lineWidth;
    private float lineLength;
    private float gapLength;
    private float gapLengthExit;

    public enum VehicleColorMode {
        VELOCITY_COLOR,
        LANE_CHANGE,
        ACCELERATION_COLOR,
        VEHICLE_LABEL_COLOR,
        VEHICLE_COLOR,
        EXIT_COLOR,
        HIGHLIGHT_VEHICLE
    }

    private VehicleColorMode vehicleColorMode = VehicleColorMode.VELOCITY_COLOR;

    private java.awt.Color[] accelerationColors;
    private final Map<String, java.awt.Color> labelColors = new HashMap<>();

    private final double[] accelerations = new double[]{-7.5, -0.1, 0.2};

    /**
     * vehicle mouse-over support
     */
    private String popupString;
    private String popupStringExitEndRoad;
    private Vehicle vehiclePopup;
    private long lastVehicleViewed = -1;
    private long vehicleToHighlightId = -1;

    /**
     * Callbacks from this TrafficCanvas to the application UI.
     */
    public interface StatusControlCallbacks {
        /**
         * Callback to get the UI to display a status message.
         *
         * @param message the status message
         */
        public void showStatusMessage(String message);

        public void stateChanged();
    }

    public FxSimCanvas(int width, int height, Properties properties) {
        super(width, height);
        // Facade for using awt TODO replace all awt calls with javafx
        fxGraphics2D = new FXGraphics2D(this.getGraphicsContext2D());

        ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        Movsim movsimInput = InputLoader.unmarshallMovsim(projectMetaData.getInputFile());
        simulator = new Simulator(movsimInput);
        simulationRunnable = simulator.getSimulationRunnable();
        this.roadNetwork = simulator.getRoadNetwork();
        this.properties = properties;

        simulationRunnable.setUpdateDrawingCallback(this);
        simulationRunnable.setHandleExceptionCallback(this);
        if (projectMetaData.hasProjectName()) {
            this.properties = ViewProperties.loadProperties(projectMetaData.getProjectName(), projectMetaData.getPathToProjectFile());
            try {
                simulator.initialize();
            } catch (JAXBException | SAXException e) {
                throw new RuntimeException("Jaxb exception:" + e.toString());
            }
            vehicleToHighlightId = -1;
            initGraphicSettings();

            start();
        } else {
            System.out.println("Please provide scenario via -f option");
        }

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
        roadColor = new java.awt.Color(Integer.parseInt(properties.getProperty("roadColor"), hexRadix));
        roadEdgeColor = new java.awt.Color(Integer.parseInt(properties.getProperty("roadEdgeColor"), hexRadix));
        roadLineColor = new java.awt.Color(Integer.parseInt(properties.getProperty("roadLineColor"), hexRadix));
        sourceColor = new java.awt.Color(Integer.parseInt(properties.getProperty("sourceColor"), hexRadix));
        sinkColor = new java.awt.Color(Integer.parseInt(properties.getProperty("sinkColor"), hexRadix));
        setVehicleColorMode(VehicleColorMode.valueOf(properties.getProperty("vehicleColorMode")));

        setVmaxForColorSpectrum(Double.parseDouble(properties.getProperty("vmaxForColorSpectrum")));

        lineWidth = Float.parseFloat(properties.getProperty("lineWidth"));
        lineLength = Float.parseFloat(properties.getProperty("lineLength"));
        gapLength = Float.parseFloat(properties.getProperty("gapLength"));
        gapLengthExit = Float.parseFloat(properties.getProperty("gapLengthExit"));

        scale = Double.parseDouble(properties.getProperty("initialScale"));
        setSleepTime(Integer.parseInt(properties.getProperty("initial_sleep_time")));

        backgroundColor = new java.awt.Color(Integer.parseInt(properties.getProperty("backgroundColor"), hexRadix));
        backgroundPicturePath = properties.getProperty("backgroundPicturePath");
    }

    /**
     * Set the thread sleep time. This controls the animation speed.
     *
     * @param sleepTime_ms sleep time in milliseconds
     */
    public final void setSleepTime(int sleepTime_ms) {
        simulationRunnable.setSleepTime(sleepTime_ms);
    }

    void stateChanged() {
        if (statusControlCallbacks != null) {
            statusControlCallbacks.stateChanged();
        }
    }

    public void resetScaleAndOffset() {
        scale = Double.parseDouble(properties.getProperty("initialScale"));
        xOffset = Integer.parseInt(properties.getProperty("xOffset"));
        yOffset = Integer.parseInt(properties.getProperty("yOffset"));
        setTransformAndScale();
    }

    public void setTransformAndScale() {
        transform.setToIdentity();
        transform.scale(scale, scale);
        transform.translate(xOffset, yOffset);
        fxGraphics2D.transform(transform);
    }

    public void setTranslateFx() {
        transform.setToIdentity();
        transform.translate(xOffset, yOffset);
        fxGraphics2D.transform(transform);
    }

    private void toogleDrawRoadId() {
        drawRoadId = !drawRoadId;
    }

    private void initGraphicSettings() {
        initGraphicConfigFieldsFromProperties();
        resetScaleAndOffset();
        for (final RoadSegment roadSegment : roadNetwork) {
            roadSegment.roadMapping().setRoadColor(roadColor.getRGB());
        }
        for (String vehicleTypeLabel : simulator.getVehiclePrototypeLabels()) {
            final java.awt.Color color = new java.awt.Color(Colors.randomColor());
            LOG.info("set color for vehicle label={}", vehicleTypeLabel);
            labelColors.put(vehicleTypeLabel, color);
        }
        backgroundPicture = null;
        if (StringUtils.isNotBlank(backgroundPicturePath)) {
            try {
                String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
                File file = new File(currentPath, backgroundPicturePath);
                LOG.info("background image file parent={}, name={}", file.getParent(), file.getName());
                backgroundPicture = ImageIO.read(file);
            } catch (Exception e) {
                LOG.error("cannot load background image " + backgroundPicturePath, e);
            }
        }
    }

    public void setStatusControlCallbacks(TrafficCanvas.StatusControlCallbacks statusCallbacks) {
        this.statusControlCallbacks = statusCallbacks;
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad) {
        this.popupString = popupString;
        this.popupStringExitEndRoad = popupStringExitEndRoad;
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad, String trafficInflowString,
                                  String perturbationRampingFinishedString, String perturbationAppliedString, String simulationFinished) {
        setMessageStrings(popupString, popupStringExitEndRoad);
    }

    void setAccelerationColors() {
        accelerationColors = new java.awt.Color[]{java.awt.Color.WHITE, java.awt.Color.RED, java.awt.Color.BLACK, java.awt.Color.GREEN};
        assert accelerations.length == accelerationColors.length - 1;
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
//        update(fxGraphics2D);
    }

    public boolean isDrawSources() {
        return drawSources;
    }

    public boolean isDrawSinks() {
        return drawSinks;
    }

    public boolean isDrawSpeedLimits() {
        return drawSpeedLimits;
    }

    public boolean isDrawSlopes() {
        return drawSlopes;
    }

    public boolean isDrawFlowConservingBottlenecks() {
        return drawFlowConservingBottlenecks;
    }

    public void setDrawSources(boolean b) {
        this.drawSources = b;
    }

    public void setDrawSinks(boolean b) {
        this.drawSinks = b;
    }

    public void setDrawSpeedLimits(boolean b) {
        this.drawSpeedLimits = b;
    }

    public void setDrawSlopes(boolean b) {
        this.drawSlopes = b;
    }

    public void setDrawFlowConservingBottlenecks(boolean b) {
        this.drawFlowConservingBottlenecks = b;
    }

    public boolean isDrawNotifyObjects() {
        return drawNotifyObjects;
    }

    public void setDrawNotifyObjects(boolean drawNotifyObjects) {
        this.drawNotifyObjects = drawNotifyObjects;
    }

    protected java.awt.Color vehicleColor(Vehicle vehicle, double simulationTime) {
        java.awt.Color color;

        switch (vehicleColorMode) {
            case ACCELERATION_COLOR:
                final double a = vehicle.physicalQuantities().getAcc();
                final int count = accelerations.length;
                for (int i = 0; i < count; ++i) {
                    if (a < accelerations[i])
                        return accelerationColors[i];
                }
                color = accelerationColors[accelerationColors.length - 1];
                break;
            case EXIT_COLOR:
                color = java.awt.Color.BLACK;
                if (vehicle.exitRoadSegmentId() != Vehicle.ROAD_SEGMENT_ID_NOT_SET) {
                    color = java.awt.Color.WHITE;
                }
                break;
            case HIGHLIGHT_VEHICLE:
                color = vehicle.getId() == vehicleToHighlightId ? java.awt.Color.BLUE : java.awt.Color.BLACK;
                break;
            case LANE_CHANGE:
                color = java.awt.Color.BLACK;
                if (vehicle.inProcessOfLaneChange()) {
                    color = java.awt.Color.ORANGE;
                }
                break;
            case VEHICLE_COLOR:
                // use vehicle's cache for AWT color object
                color = (java.awt.Color) vehicle.colorObject();
                if (color == null) {
                    int vehColorInt = vehicle.color();
                    color = new java.awt.Color(Colors.red(vehColorInt), Colors.green(vehColorInt), Colors.blue(vehColorInt));
                    vehicle.setColorObject(color);
                }
                break;
            case VEHICLE_LABEL_COLOR:
                String label = vehicle.getLabel();
                color = labelColors.containsKey(label) ? labelColors.get(label) : java.awt.Color.WHITE;
                break;
            case VELOCITY_COLOR:
                double v = vehicle.physicalQuantities().getSpeed() * 3.6;
                color = getColorAccordingToSpectrum(0, getVmaxForColorSpectrum(), v);
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
    private void drawAfterVehiclesMoved(FXGraphics2D g, double simulationTime, long iterationCount) {
    }

    /**
     * <p>
     * Draws the foreground: everything that moves each timestep. For the traffic simulation that means draw all the vehicles:<br />
     * For each roadSection, draw all the vehicles in the roadSection, positioning them using the roadMapping for that roadSection.
     * </p>
     *
     * <p>
     * This method is synchronized with the <code>SimulationRunnable.run()</code> method, so that vehicles are not updated, added or removed
     * while they are being drawn.
     * </p>
     * <p>
     * tm The abstract method paintAfterVehiclesMoved is called after the vehicles have been moved, to allow any further required drawing on
     * the canvas.
     * </p>
     *
     * @param g
     */
    private void drawForeground(FXGraphics2D g) {
        // moveVehicles occurs in the UI thread, so must synchronize with the
        // update of the road network in the calculation thread.
        final long timeBeforePaint_ms = System.currentTimeMillis();
        synchronized (simulationRunnable.dataLock) {
            drawTrafficLights(g);
            final double simulationTime = simulationRunnable.simulationTime();
            for (final RoadSegment roadSegment : roadNetwork) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                assert roadMapping != null;
                PaintRoadMappingFx.setClipPath(g, roadMapping, clipPath);
                for (final Vehicle vehicle : roadSegment) {
                    drawVehicle(g, simulationTime, roadMapping, vehicle);
                }
                for (Iterator<Vehicle> vehIter = roadSegment.overtakingVehicles(); vehIter.hasNext(); ) {
                    Vehicle vehicle = vehIter.next();
                    drawVehicle(g, simulationTime, roadMapping, vehicle);
                }
            }
            totalAnimationTime += System.currentTimeMillis() - timeBeforePaint_ms;
            drawAfterVehiclesMoved(g, simulationRunnable.simulationTime(), simulationRunnable.iterationCount());
        }
    }

    private void drawVehicle(FXGraphics2D g, double simulationTime, RoadMapping roadMapping, Vehicle vehicle) {
        // draw vehicle polygon at new position
        final RoadMapping.PolygonFloat polygon = roadMapping.mapFloat(vehicle);
        vehiclePath.reset();
        vehiclePath.moveTo(polygon.getXPoint(0), polygon.getYPoint(0));
        vehiclePath.lineTo(polygon.getXPoint(1), polygon.getYPoint(1));
        vehiclePath.lineTo(polygon.getXPoint(2), polygon.getYPoint(2));
        vehiclePath.lineTo(polygon.getXPoint(3), polygon.getYPoint(3));
        vehiclePath.closePath();
        g.setPaint(vehicleColor(vehicle, simulationTime));
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
     *
     * @param g
     */
    private void drawBackground(FXGraphics2D g) {

        if (backgroundPicture != null)
            g.drawImage(backgroundPicture, 0, -(int) (backgroundPicture.getHeight()),
                    (int) (backgroundPicture.getWidth() * 1.01), 0, 0, 0, backgroundPicture.getWidth(),
                    backgroundPicture.getHeight(), null);

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

    private void drawRoadSegmentsAndLines(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            if (roadMapping.isPeer()) {
                LOG.debug("skip painting peer element={}", roadMapping);
                continue;
            }
            drawRoadSegment(g, roadMapping);
            drawRoadSegmentLines(g, roadMapping);
        }
    }

    private void drawRoadSegment(FXGraphics2D g, RoadMapping roadMapping) {
        BasicStroke roadStroke = new BasicStroke((float) roadMapping.roadWidth(), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER);
        g.setStroke(roadStroke);
        g.setColor(new Color(roadMapping.roadColor()));
        PaintRoadMappingFx.paintRoadMapping(g, roadMapping);
    }

    private void drawRoadSegmentLines(FXGraphics2D g, RoadMapping roadMapping) {
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
            PaintRoadMappingFx.paintRoadMapping(g, roadMapping, offset);
        }

        // draw the road edges
        g.setStroke(new BasicStroke());
        g.setColor(roadEdgeColor);
        double offset = roadMapping.getLaneGeometries().getLeft().getLaneCount()
                * roadMapping.getLaneGeometries().getLaneWidth();
        LOG.debug("draw road left outer edge: offset={}", offset);
        PaintRoadMappingFx.paintRoadMapping(g, roadMapping, offset);
        // edge of most outer edge
        offset = roadMapping.getMaxOffsetRight();
        LOG.debug("draw road most outer edge: offset={}", offset);
        PaintRoadMappingFx.paintRoadMapping(g, roadMapping, offset);
    }

    private void drawTrafficLights(FXGraphics2D g) {
        int strokeWidth = 3;
        for (final RoadSegment roadSegment : roadNetwork) {
            assert roadSegment.trafficLights() != null;
            for (TrafficLight trafficLight : roadSegment.trafficLights()) {
                java.awt.Color color = getTrafficLightColor(trafficLight);
                drawLine(g, roadSegment.roadMapping(), trafficLight.position(), strokeWidth, color);
            }
        }
    }

    private void drawLine(FXGraphics2D g, RoadMapping roadMapping, double position, int strokeWidth, Color color) {
        Color prevColor = g.getColor();
        final double lateralExtend = roadMapping.getLaneCountInDirection() * roadMapping.laneWidth();
        final PosTheta posTheta = roadMapping.map(position, 0/* offset */);
        final RoadMapping.PolygonFloat line = roadMapping.mapLine(posTheta, roadMapping.isPeer() ? +lateralExtend : -lateralExtend);
        g.setColor(color);
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(new Line2D.Float(line.getXPoint(0), line.getYPoint(0), line.getXPoint(1), line.getYPoint(1)));
        g.setColor(prevColor);
    }

    private java.awt.Color getTrafficLightColor(TrafficLight trafficLight) {
        java.awt.Color color = null;
        switch (trafficLight.status()) {
            case GREEN:
                color = java.awt.Color.GREEN;
                break;
            case GREEN_RED:
                color = java.awt.Color.YELLOW;
                break;
            case RED:
                color = java.awt.Color.RED;
                break;
            case RED_GREEN:
                color = java.awt.Color.ORANGE;
                break;
        }
        return color;
    }

    private void drawRoadSectionIds(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double position = roadMapping.isPeer() ? roadMapping.roadLength() : 0.0;
            final double offset = roadMapping.isPeer()
                    ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                    : roadMapping.getMaxOffsetRight();
            final PosTheta posTheta = roadMapping.map(position, offset);
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.PLAIN, fontHeight);
            g.setFont(font);
            g.setColor(java.awt.Color.BLACK);
            drawTextRotated(roadSegment.userId(), posTheta, font, g);
        }
    }

    private void drawTextRotated(String text, PosTheta posTheta, Font font, FXGraphics2D g) {
        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text); //$NON-NLS-1$
        AffineTransform at = AffineTransform.getTranslateInstance((int) posTheta.getScreenX(),
                (int) posTheta.getScreenY());
        at.rotate(-posTheta.getTheta());
        Shape glyph = gv.getOutline();
        Shape transformedGlyph = at.createTransformedShape(glyph);
        g.fill(transformedGlyph);
    }

    private void drawSpeedLimits(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            assert roadSegment.speedLimits() != null;
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.BOLD, fontHeight);
            final RoadMapping roadMapping = roadSegment.roadMapping();
            for (SpeedLimit speedLimit : roadSegment.speedLimits()) {
                g.setFont(font);
                final double position = speedLimit.position();
                final double offset = roadMapping.isPeer()
                        ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                        : roadMapping.getMaxOffsetRight();
                final PosTheta posTheta = roadMapping.map(position, offset);
                final String text = String.valueOf((int) (speedLimit.getSpeedLimitKmh())) + "km/h";
                g.setFont(font);
                java.awt.Color color = speedLimit.getSpeedLimit() < MovsimConstants.MAX_VEHICLE_SPEED ? java.awt.Color.RED
                        : java.awt.Color.DARK_GRAY;
                g.setColor(color);
                drawTextRotated(text, posTheta, font, g);
                drawLine(g, roadMapping, position, 1, color);
            }
        }
    }

    private void drawSlopes(FXGraphics2D g) {
        for (final RoadSegment roadSegment : roadNetwork) {
            final int fontHeight = 12;
            final Font font = new Font("SansSerif", Font.BOLD, fontHeight); //$NON-NLS-1$
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double offset = roadMapping.isPeer()
                    ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                    : roadMapping.getMaxOffsetRight();
            for (GradientProfile gradientProfile : roadSegment.gradientProfiles()) {
                for (Map.Entry<Double, Double> gradientEntry : gradientProfile.gradientEntries()) {
                    final double position = gradientEntry.getKey();
                    final PosTheta posTheta = roadMapping.map(position, offset);
                    final double gradient = gradientEntry.getValue() * 100;
                    final String text = String.valueOf((int) (gradient)) + "%";
                    drawTextRotated(text, posTheta, font, g);
                    drawLine(g, roadMapping, position, 1, java.awt.Color.DARK_GRAY);
                }
            }
        }
    }

    private void drawFlowConservingBottlenecks(FXGraphics2D g) {
        final int fontHeight = 12;
        final Font font = new Font("SansSerif", Font.BOLD, fontHeight); //$NON-NLS-1$
        final java.awt.Color color = java.awt.Color.ORANGE;
        final java.awt.Color prevColor = g.getColor();
        g.setColor(color);
        for (final RoadSegment roadSegment : roadNetwork) {
            final RoadMapping roadMapping = roadSegment.roadMapping();
            final double offset = roadMapping.isPeer()
                    ? roadMapping.getOffsetLeft(roadMapping.getLaneGeometries().getLeft().getLaneCount() - 1)
                    : roadMapping.getMaxOffsetRight();
            for (FlowConservingBottleneck bottleneck : roadSegment.flowConservingBottlenecks()) {
                final double posStart = bottleneck.position();
                PosTheta posTheta = roadMapping.map(posStart, offset);
                drawTextRotated(" bneck start", posTheta, font, g);
                drawLine(g, roadMapping, posStart, 2, color);

                final double posEnd = bottleneck.endPosition();
                posTheta = roadMapping.map(posEnd, offset);
                drawTextRotated(" bneck end", posTheta, font, g);
                drawLine(g, roadMapping, bottleneck.endPosition(), 2, color);
            }

        }
        g.setColor(prevColor);
    }

    private void drawSources(FXGraphics2D g) {
        for (RoadSegment roadSegment : roadNetwork) {
            AbstractTrafficSource trafficSource = roadSegment.trafficSource();
            if (trafficSource != null) {
                drawLine(g, roadSegment.roadMapping(), 0, 4, sourceColor);
            }
        }
    }

    private void drawSinks(FXGraphics2D g) {
        for (RoadSegment roadSegment : roadNetwork) {
            TrafficSink sink = roadSegment.sink();
            if (sink != null) {
                final RoadMapping roadMapping = roadSegment.roadMapping();
                drawLine(g, roadMapping, roadMapping.roadLength(), 4, sinkColor);
            }
        }
    }

    private void drawNotifyObjects(FXGraphics2D g) {
        for (Regulator regulator : simulator.getRegulators()) {
            for (NotifyObject notifyObject : regulator.getNotifyObjects()) {
                RoadMapping roadMapping = notifyObject.getRoadSegment().roadMapping();
                drawLine(g, roadMapping, notifyObject.getPosition(), 2, java.awt.Color.DARK_GRAY);
            }
        }
    }

    public void showSinkMouseOverInfo(Point point, TrafficSink sink) {
        StringBuilder sb = new StringBuilder();
        sb.append("outflow: ");
        sb.append((int) (Units.INVS_TO_INVH * sink.measuredOutflow()));
        sb.append(" veh/h");

//        mouseOverTipWindow.setVisible(false);
//        mouseOverTipWindow.show(point, sb.toString());
    }

    public void showSourceMouseOverInfo(Point point, AbstractTrafficSource source) {
        StringBuilder sb = new StringBuilder();
        sb.append("set inflow: ");
        sb.append((int) (Units.INVS_TO_INVH * source.getTotalInflow(simulationRunnable.simulationTime())));
        sb.append(" veh/h, actual inflow: ");
        sb.append((int) (Units.INVS_TO_INVH * source.measuredInflow()));
        sb.append(" veh/h, queue: ");
        sb.append(source.getQueueLength());

//        mouseOverTipWindow.setVisible(false);
//        mouseOverTipWindow.show(point, sb.toString());
    }

    public void showVehicleMouseOverInfo(Point point, Vehicle vehicle) {
        if (vehiclePopup == null || vehiclePopup.getId() != vehicle.getId()) {
            lastVehicleViewed = vehicle.getId();
//            mouseOverTipWindow.setVisible(false);
//            mouseOverTipWindow.show(point, vehicle);
        }

    }

    public void start() {
        totalAnimationTime = 0;
        simulationRunnable.start();
        stateChanged();
    }

    private void clearBackground(FXGraphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, (int) (getWidth()), (int) getHeight());

    }

    /**
     * hue values (see, e.g., http://help.adobe.com/en_US/Photoshop/11.0/images/wc_HSB.png): h=0:red, h=0.2: yellow, h=0.35: green, h=0.5:
     * blue, h=0.65: violet, then a long magenta region
     **/
    private Color getColorAccordingToSpectrum(double vmin, double vmax, double v) {
        assert vmax > vmin;
        // tune following values if not satisfied
        // (the floor function of any hue value >=1 will be subtracted by HSBtoRGB)

        final double hue_vmin = 1.00; // hue value for minimum speed value; red
        final double hue_vmax = 1.84; // hue value for max speed (1 will be subtracted); violetblue

        // possibly a nonlinear hue(speed) function looks nicer;
        // first try this truncuated-linear one

        float vRelative = (vmax > vmin) ? (float) ((v - vmin) / (vmax - vmin)) : 0;
        vRelative = Math.min(Math.max(0, vRelative), 1);
        final float h = (float) (hue_vmin + vRelative * (hue_vmax - hue_vmin));

        // use max. saturation
        final float s = (float) 1.0;

        // possibly a reduction of brightness near h=0.5 looks nicer;
        // first try max brightness (0-1)
        final float b = (float) 0.92;

        final int rgb = Color.HSBtoRGB(h, s, b);
        return v > 0.1 ? new Color(rgb) : Color.BLACK;
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
    public void updateDrawing(double simulationTime) {
        clearBackground(fxGraphics2D);
        drawBackground(fxGraphics2D);
        drawForeground(fxGraphics2D);
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
