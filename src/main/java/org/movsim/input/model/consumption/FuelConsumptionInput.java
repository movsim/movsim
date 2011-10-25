package org.movsim.input.model.consumption;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuelConsumptionInput {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FuelConsumptionInput.class);
    
    private List<ConsumptionModelInput> consumptionModelInput;
    
    @SuppressWarnings("unchecked")
    public FuelConsumptionInput(Element elem){
        
        if(elem==null){
            return;
        }
        
        consumptionModelInput = new ArrayList<ConsumptionModelInput>();
        final List<Element> fuelvehicleElements = elem.getChildren(XmlElementNames.ConsumptionModel); 
        
        for (final Element fuelModelElem : fuelvehicleElements) {
            consumptionModelInput.add(new ConsumptionModelInput(fuelModelElem));
        }
        
        
    }
    
    
    public List<ConsumptionModelInput> getConsumptionModelInput() {
        return consumptionModelInput;
    }
}
