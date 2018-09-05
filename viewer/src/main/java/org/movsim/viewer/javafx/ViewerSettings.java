package org.movsim.viewer.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ViewerSettings {
    private static final Logger LOG = LoggerFactory.getLogger(ViewerSettings.class);

    private boolean drawRoadId;
    private boolean drawSources;
    private boolean drawSinks;
    private boolean drawSpeedLimits;
    private boolean drawSlopes;
    private boolean drawFlowConservingBottlenecks;
    private boolean drawNotifyObjects;

    // colors
    private java.awt.Color roadColor;
    private java.awt.Color roadEdgeColor;
    private java.awt.Color roadLineColor;
    private java.awt.Color sourceColor;
    private java.awt.Color sinkColor;

    // brake light handling
    private java.awt.Color brakeLightColor = java.awt.Color.RED;

    private float lineWidth;
    private float lineLength;
    private float gapLength;
    private float gapLengthExit;

    private double vmaxForColorSpectrum;

    private final Map<String, Color> labelColors = new HashMap<>();

    // scale factor pixels/m, smaller value means a smaller looking view
    public double scale;
    public int xOffset = 0;
    public int yOffset = 0;

    private javafx.scene.paint.Color backgroundColor;
    // optional background picture
    private String backgroundPicturePath;

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

    public Map<String, Color> getLabelColors() {
        return labelColors;
    }

    public javafx.scene.paint.Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(javafx.scene.paint.Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBackgroundPicturePath() {
        return backgroundPicturePath;
    }

    public void setBackgroundPicturePath(String backgroundPicturePath) {
        this.backgroundPicturePath = backgroundPicturePath;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
//        final int width = 1000; TODO scale
//        final int height = 1000;
//        xOffset -= 0.5 * width * (1.0 / this.scale - 1.0 / scale);
//        yOffset -= 0.5 * height * (1.0 / this.scale - 1.0 / scale);
        this.scale = scale;
    }

    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public Color getBrakeLightColor() {
        return brakeLightColor;
    }

    public void setBrakeLightColor(Color brakeLightColor) {
        this.brakeLightColor = brakeLightColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getLineLength() {
        return lineLength;
    }

    public void setLineLength(float lineLength) {
        this.lineLength = lineLength;
    }

    public float getGapLength() {
        return gapLength;
    }

    public void setGapLength(float gapLength) {
        this.gapLength = gapLength;
    }

    public float getGapLengthExit() {
        return gapLengthExit;
    }

    public void setGapLengthExit(float gapLengthExit) {
        this.gapLengthExit = gapLengthExit;
    }

    public VehicleColorMode getVehicleColorMode() {
        return vehicleColorMode;
    }

    public void setVehicleColorMode(VehicleColorMode vehicleColorMode) {
        this.vehicleColorMode = vehicleColorMode;
    }

    public boolean isDrawRoadId() {
        return drawRoadId;
    }

    public void setDrawRoadId(boolean drawRoadId) {
        this.drawRoadId = drawRoadId;
    }

    public boolean isDrawSources() {
        return drawSources;
    }

    public void setDrawSources(boolean drawSources) {
        this.drawSources = drawSources;
    }

    public boolean isDrawSinks() {
        return drawSinks;
    }

    public void setDrawSinks(boolean drawSinks) {
        this.drawSinks = drawSinks;
    }

    public boolean isDrawSpeedLimits() {
        return drawSpeedLimits;
    }

    public void setDrawSpeedLimits(boolean drawSpeedLimits) {
        this.drawSpeedLimits = drawSpeedLimits;
    }

    public boolean isDrawSlopes() {
        return drawSlopes;
    }

    public void setDrawSlopes(boolean drawSlopes) {
        this.drawSlopes = drawSlopes;
    }

    public boolean isDrawFlowConservingBottlenecks() {
        return drawFlowConservingBottlenecks;
    }

    public void setDrawFlowConservingBottlenecks(boolean drawFlowConservingBottlenecks) {
        this.drawFlowConservingBottlenecks = drawFlowConservingBottlenecks;
    }

    public boolean isDrawNotifyObjects() {
        return drawNotifyObjects;
    }

    public void setDrawNotifyObjects(boolean drawNotifyObjects) {
        this.drawNotifyObjects = drawNotifyObjects;
    }

    public Color getRoadColor() {
        return roadColor;
    }

    public void setRoadColor(Color roadColor) {
        this.roadColor = roadColor;
    }

    public Color getRoadEdgeColor() {
        return roadEdgeColor;
    }

    public void setRoadEdgeColor(Color roadEdgeColor) {
        this.roadEdgeColor = roadEdgeColor;
    }

    public Color getRoadLineColor() {
        return roadLineColor;
    }

    public void setRoadLineColor(Color roadLineColor) {
        this.roadLineColor = roadLineColor;
    }

    public Color getSourceColor() {
        return sourceColor;
    }

    public void setSourceColor(Color sourceColor) {
        this.sourceColor = sourceColor;
    }

    public Color getSinkColor() {
        return sinkColor;
    }

    public void setSinkColor(Color sinkColor) {
        this.sinkColor = sinkColor;
    }

    public double getVmaxForColorSpectrum() {
        return vmaxForColorSpectrum;
    }

    public void setVmaxForColorSpectrum(double vmaxForColorSpectrum) {
        this.vmaxForColorSpectrum = vmaxForColorSpectrum;
    }

    ViewerSettings() {
    }

    protected void initGraphicConfigFieldsFromProperties(Properties properties) {
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
        xOffset = Integer.parseInt(properties.getProperty("xOffset"));
        yOffset = Integer.parseInt(properties.getProperty("yOffset"));

        backgroundColor = javafx.scene.paint.Color.web(properties.getProperty("backgroundColor"));
        backgroundPicturePath = properties.getProperty("backgroundPicturePath");
    }
}
