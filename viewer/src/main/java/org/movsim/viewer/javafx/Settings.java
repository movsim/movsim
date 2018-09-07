package org.movsim.viewer.javafx;

import org.apache.commons.lang3.StringUtils;
import org.movsim.input.ProjectMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class Settings {
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private boolean drawRoadId;
    private boolean drawSources;
    private boolean drawSinks;
    private boolean drawSpeedLimits;
    private boolean drawSlopes;
    private boolean drawFlowConservingBottlenecks;
    private boolean drawNotifyObjects;

    // colors
    private javafx.scene.paint.Color roadColor;
    private java.awt.Color roadEdgeColor;
    private java.awt.Color roadLineColor;
    private javafx.scene.paint.Color sourceColor;
    private javafx.scene.paint.Color sinkColor;

    // brake light handling
    private javafx.scene.paint.Color brakeLightColor = javafx.scene.paint.Color.RED;

    private float lineWidth;
    private float lineLength;
    private float gapLength;
    private float gapLengthExit;

    private double vmaxForColorSpectrum;

    private final Map<String, javafx.scene.paint.Color> labelColors = new HashMap<>();

    // scale factor pixels/m, smaller value means a smaller looking view
    double scale = 1;
    int xOffset = 0;
    int yOffset = 0;
    private int xPixSizeWindow;
    private int yPixSizeWindow;

    private javafx.scene.paint.Color backgroundColor;
    // optional background picture
    private String backgroundPicturePath;
    private BufferedImage backgroundPicture;

    enum VehicleColorMode {
        VELOCITY_COLOR,
        LANE_CHANGE,
        ACCELERATION_COLOR,
        VEHICLE_LABEL_COLOR,
        VEHICLE_COLOR,
        EXIT_COLOR,
        HIGHLIGHT_VEHICLE
    }

    private VehicleColorMode vehicleColorMode = VehicleColorMode.VELOCITY_COLOR;

    Map<String, javafx.scene.paint.Color> getLabelColors() {
        return labelColors;
    }

    public javafx.scene.paint.Color getBackgroundColor() {
        return backgroundColor;
    }

    void setBackgroundColor(javafx.scene.paint.Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    String getBackgroundPicturePath() {
        return backgroundPicturePath;
    }

    void setBackgroundPicturePath(String backgroundPicturePath) {
        this.backgroundPicturePath = backgroundPicturePath;
    }

    double getScale() {
        return scale;
    }

    void setScale(double scale) {
        xOffset -= 0.5 * xPixSizeWindow * (1.0 / this.scale - 1.0 / scale);
        yOffset -= 0.5 * yPixSizeWindow * (1.0 / this.scale - 1.0 / scale);
        this.scale = scale;
    }

    int getxOffset() {
        return xOffset;
    }

    void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    int getyOffset() {
        return yOffset;
    }

    void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    javafx.scene.paint.Color getBrakeLightColor() {
        return brakeLightColor;
    }

    void setBrakeLightColor(javafx.scene.paint.Color brakeLightColor) {
        this.brakeLightColor = brakeLightColor;
    }

    float getLineWidth() {
        return lineWidth;
    }

    void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    float getLineLength() {
        return lineLength;
    }

    void setLineLength(float lineLength) {
        this.lineLength = lineLength;
    }

    float getGapLength() {
        return gapLength;
    }

    void setGapLength(float gapLength) {
        this.gapLength = gapLength;
    }

    float getGapLengthExit() {
        return gapLengthExit;
    }

    void setGapLengthExit(float gapLengthExit) {
        this.gapLengthExit = gapLengthExit;
    }

    VehicleColorMode getVehicleColorMode() {
        return vehicleColorMode;
    }

    void setVehicleColorMode(VehicleColorMode vehicleColorMode) {
        this.vehicleColorMode = vehicleColorMode;
    }

    boolean isDrawRoadId() {
        return drawRoadId;
    }

    void setDrawRoadId(boolean drawRoadId) {
        this.drawRoadId = drawRoadId;
    }

    boolean isDrawSources() {
        return drawSources;
    }

    void setDrawSources(boolean drawSources) {
        this.drawSources = drawSources;
    }

    boolean isDrawSinks() {
        return drawSinks;
    }

    void setDrawSinks(boolean drawSinks) {
        this.drawSinks = drawSinks;
    }

    boolean isDrawSpeedLimits() {
        return drawSpeedLimits;
    }

    void setDrawSpeedLimits(boolean drawSpeedLimits) {
        this.drawSpeedLimits = drawSpeedLimits;
    }

    boolean isDrawSlopes() {
        return drawSlopes;
    }

    void setDrawSlopes(boolean drawSlopes) {
        this.drawSlopes = drawSlopes;
    }

    boolean isDrawFlowConservingBottlenecks() {
        return drawFlowConservingBottlenecks;
    }

    void setDrawFlowConservingBottlenecks(boolean drawFlowConservingBottlenecks) {
        this.drawFlowConservingBottlenecks = drawFlowConservingBottlenecks;
    }

    boolean isDrawNotifyObjects() {
        return drawNotifyObjects;
    }

    void setDrawNotifyObjects(boolean drawNotifyObjects) {
        this.drawNotifyObjects = drawNotifyObjects;
    }

    javafx.scene.paint.Color getRoadColor() {
        return roadColor;
    }

    void setRoadColor(javafx.scene.paint.Color roadColor) {
        this.roadColor = roadColor;
    }

    Color getRoadEdgeColor() {
        return roadEdgeColor;
    }

    void setRoadEdgeColor(Color roadEdgeColor) {
        this.roadEdgeColor = roadEdgeColor;
    }

    Color getRoadLineColor() {
        return roadLineColor;
    }

    void setRoadLineColor(Color roadLineColor) {
        this.roadLineColor = roadLineColor;
    }

    javafx.scene.paint.Color getSourceColor() {
        return sourceColor;
    }

    void setSourceColor(javafx.scene.paint.Color sourceColor) {
        this.sourceColor = sourceColor;
    }

    javafx.scene.paint.Color getSinkColor() {
        return sinkColor;
    }

    void setSinkColor(javafx.scene.paint.Color sinkColor) {
        this.sinkColor = sinkColor;
    }

    double getVmaxForColorSpectrum() {
        return vmaxForColorSpectrum;
    }

    void setVmaxForColorSpectrum(double vmaxForColorSpectrum) {
        this.vmaxForColorSpectrum = vmaxForColorSpectrum;
    }

    BufferedImage getBackgroundPicture() {
        return backgroundPicture;
    }

    void setBackgroundPicture(BufferedImage backgroundPicture) {
        this.backgroundPicture = backgroundPicture;
    }

    int getxPixSizeWindow() {
        return xPixSizeWindow;
    }

    void setxPixSizeWindow(int xPixSizeWindow) {
        this.xPixSizeWindow = xPixSizeWindow;
    }

    int getyPixSizeWindow() {
        return yPixSizeWindow;
    }

    void setyPixSizeWindow(int yPixSizeWindow) {
        this.yPixSizeWindow = yPixSizeWindow;
    }


    Settings() {
    }

    void initGraphicConfigFieldsFromProperties(Properties properties) {
        setDrawRoadId(Boolean.parseBoolean(properties.getProperty("drawRoadId")));
        setDrawSinks(Boolean.parseBoolean(properties.getProperty("drawSinks")));
        setDrawSources(Boolean.parseBoolean(properties.getProperty("drawSources")));
        setDrawSlopes(Boolean.parseBoolean(properties.getProperty("drawSlopes")));
        setDrawFlowConservingBottlenecks(Boolean.parseBoolean(properties.getProperty("drawFlowConservingBottlenecks")));
        setDrawSpeedLimits(Boolean.parseBoolean(properties.getProperty("drawSpeedLimits")));
        setDrawNotifyObjects(Boolean.parseBoolean(properties.getProperty("drawNotifyObjects")));

        final int hexRadix = 16;
        roadColor = javafx.scene.paint.Color.web(properties.getProperty("roadColor"));
        roadEdgeColor = new java.awt.Color(Integer.parseInt(properties.getProperty("roadEdgeColor"), hexRadix));
        roadLineColor = new java.awt.Color(Integer.parseInt(properties.getProperty("roadLineColor"), hexRadix));
        sourceColor = javafx.scene.paint.Color.web(properties.getProperty("sourceColor"));
        sinkColor = javafx.scene.paint.Color.web(properties.getProperty("sinkColor"));

        setVehicleColorMode(VehicleColorMode.valueOf(properties.getProperty("vehicleColorMode")));
        setVmaxForColorSpectrum(Double.parseDouble(properties.getProperty("vmaxForColorSpectrum")));

        lineWidth = Float.parseFloat(properties.getProperty("lineWidth"));
        lineLength = Float.parseFloat(properties.getProperty("lineLength"));
        gapLength = Float.parseFloat(properties.getProperty("gapLength"));
        gapLengthExit = Float.parseFloat(properties.getProperty("gapLengthExit"));


        xPixSizeWindow = Integer.valueOf(properties.getProperty("xPixSizeWindow", "1000"));
        yPixSizeWindow = Integer.valueOf(properties.getProperty("yPixSizeWindow", "800"));
        setxOffset(Integer.parseInt(properties.getProperty("xOffset")));
        setyOffset(Integer.parseInt(properties.getProperty("yOffset")));
        setScale(Double.parseDouble(properties.getProperty("initialScale")));

        backgroundColor = javafx.scene.paint.Color.web(properties.getProperty("backgroundColor"));
        backgroundPicturePath = properties.getProperty("backgroundPicturePath");

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
}
