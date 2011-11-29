package org.movsim.input.model.consumption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuelConsumptionInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FuelConsumptionInput.class);

    private Map<String, ConsumptionModelInput> consumptionModelInput;

    @SuppressWarnings("unchecked")
    public FuelConsumptionInput(Element elem) {

        // fuel consumption element is optional
        if (elem == null)
            return;

        consumptionModelInput = new HashMap<String, ConsumptionModelInput>();

        final List<Element> fuelvehicleElements = elem.getChildren(XmlElementNames.ConsumptionModel);

        for (final Element fuelModelElem : fuelvehicleElements) {
            final ConsumptionModelInput consModel = new ConsumptionModelInput(fuelModelElem);
            consumptionModelInput.put(consModel.getLabel(), consModel);
        }
    }

    // public ConsumptionModelInput getConsumptionModelInput(String label) {
    // if (!consumptionModelInput.containsKey(label)) {
    // logger.error("consumption model with label={} is not available from the input. Exit.", label);
    // System.exit(-1);
    // }
    // return consumptionModelInput.get(label);
    // }

    public Map<String, ConsumptionModelInput> getConsumptionModelInput() {
        return consumptionModelInput;
    }
}
