package org.movsim.consumption.input.xml.batch;

import java.util.Map;

import org.jdom.Element;
import org.movsim.utilities.XmlUtils;

import com.google.common.base.Preconditions;

public class ConversionInput {

    // <CONVERSION time="HH:mm:ss" speed="0.2777777" gradient="0.01" />
    private final String timeFormat;
    private final double speedConversionFactor;
    private final double gradientConversionFactor;
    private final double positionConversionFactor;

    public ConversionInput(Element element) {
        Preconditions.checkNotNull(element);
        Map<String, String> attributeMap = XmlUtils.putAttributesInHash(element);
        this.timeFormat = attributeMap.get("time");
        this.speedConversionFactor = Double.parseDouble((attributeMap.get("speed")));
        this.gradientConversionFactor = Double.parseDouble((attributeMap.get("gradient")));
        this.positionConversionFactor = Double.parseDouble((attributeMap.get("position")));
    }

    /**
     * @return the timeFormat
     */
    public String getTimeFormat() {
        return timeFormat;
    }

    /**
     * @return the speedConversionFactor
     */
    public double getSpeedConversionFactor() {
        return speedConversionFactor;
    }

    /**
     * @return the gradientConversionFactor
     */
    public double getGradientConversionFactor() {
        return gradientConversionFactor;
    }

    public double getPositionConversionFactor() {
        return positionConversionFactor;
    }

}
