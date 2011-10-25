package org.movsim.input.model.consumption;

import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.impl.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionModelInput {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionModelInput.class);
    
    
    
    private CarConsumptionModelInput carData;
    
    private EngineModelInput engineData;
    
    private String label;
    
    
    public ConsumptionModelInput(Element elem){
        
        this.label = elem.getAttributeValue("label");
        
        final Map<String, String> carDataMap = XmlUtils.putAttributesInHash(elem.getChild(XmlElementNames.ConsumptionCarData));
        carData = new CarConsumptionModelInput(carDataMap);

        
        engineData = new EngineModelInput(elem.getChild(XmlElementNames.ConsumptionEngineData));
        
    }


    public CarConsumptionModelInput getCarData() {
        return carData;
    }


    public EngineModelInput getEngineData() {
        return engineData;
    }


    public String getLabel() {
        return label;
    }

}
