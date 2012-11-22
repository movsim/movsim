package org.movsim.consumption.input.xml.batch;

import java.util.Map;

import org.jdom.Element;
import org.movsim.utilities.XmlUtils;

import com.google.common.base.Preconditions;

public class ColumnInput {
	
	//time="3" speed="12" acceleration="26" gradient="24"
	private final int timeColumn;
	private final int speedColumn;
	private final int accelerationColumn;
	private final int gradientColumn;
	
	public ColumnInput(Element element) {
        Preconditions.checkNotNull(element);
		Map<String, String> attributeMap = XmlUtils.putAttributesInHash(element);
		this.timeColumn = Integer.parseInt(attributeMap.get("time"));
		this.speedColumn = Integer.parseInt(attributeMap.get("speed"));
		this.accelerationColumn = Integer.parseInt(attributeMap.get("acceleration"));
		this.gradientColumn = Integer.parseInt(attributeMap.get("gradient"));
	}

    public int getTimeColumn() {
        return timeColumn;
    }

    public int getSpeedColumn() {
        return speedColumn;
    }

    public int getAccelerationColumn() {
        return accelerationColumn;
    }

    public int getGradientColumn() {
        return gradientColumn;
    }

}
