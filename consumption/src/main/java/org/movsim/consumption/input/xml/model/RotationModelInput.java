package org.movsim.consumption.input.xml.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.consumption.input.xml.XmlElementNames;
import org.movsim.utilities.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RotationModelInput {
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(RotationModelInput.class);

    /** in meter */
    private final double dynamicTyreRadius;

    /** per second */
    private final double idleRotationRate;

    /** per second */
    private final double maxRotationRate;

    private final List<Double> gearRatios;

    public RotationModelInput(Element element) {
        final Map<String, String> map = XmlUtils.putAttributesInHash(element);
        this.dynamicTyreRadius = Double.parseDouble(map.get("dynamic_tyre_radius"));
        this.idleRotationRate = Double.parseDouble(map.get("idle_rotation_rate_invmin")) / 60;
        this.maxRotationRate = Double.parseDouble(map.get("max_rotation_rate_invmin")) / 60;

        // gear box of engine
        gearRatios = new ArrayList<Double>();
        List<Element> gearElements = element.getChildren(XmlElementNames.ConsumptionEngineGear);
        if (gearElements != null) {
            parseGears(gearElements);
        } else {
            setDefaultGears();
        }
    }

    /**
     * Sets default gear box with 5 gears
     */
    private void setDefaultGears() {
        gearRatios.add(13.9);
        gearRatios.add(7.8);
        gearRatios.add(5.26);
        gearRatios.add(3.79);
        gearRatios.add(3.09);
    }

    private void parseGears(List<Element> gearElems) {
        final List<Double> localGears = new ArrayList<Double>();

        for (final Element gearElem : gearElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(gearElem);
            localGears.add(Double.parseDouble(map.get("phi")));
        }

        Collections.sort(localGears, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                final Double pos1 = new Double((o1).doubleValue());
                final Double pos2 = new Double((o2).doubleValue());
                return pos2.compareTo(pos1); // sort with DECREASING transmission ratios (gear 1 has highest ratio)
            }
        });

        // put double values in dedicated collection
        for (final Double phiGear : localGears) {
            gearRatios.add(phiGear.doubleValue());
        }
    }

    public double getIdleRotationRate() {
        return idleRotationRate;
    }

    public double getMaxRotationRate() {
        return maxRotationRate;
    }

    public List<Double> getGearRatios() {
        return gearRatios;
    }

    public double getDynamicTyreRadius() {
        return dynamicTyreRadius;
    }
}
