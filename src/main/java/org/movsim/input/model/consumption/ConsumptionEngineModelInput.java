package org.movsim.input.model.consumption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.consumption.FuelConstants;
import org.movsim.input.XmlElementNames;
import org.movsim.input.impl.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionEngineModelInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionEngineModelInput.class);
    
    private double maxPower;  // in kW
    private double cylinderVolume; // in liter
    private double idleConsumptionRateLiterPerSecond;  // in liter per second
    private double minSpecificConsumption;  // in kg/Ws
    private double effectivePressureMinimum;  // in Pascal
    private double effectivePressureMaximum;  // in Pascal
    private double idleRotationRate;  // per second
    private double maxRotationRate; // per second
    
    private List<Double> gearRatios;
    
    @SuppressWarnings("unchecked")
    public ConsumptionEngineModelInput(Element elem){
        
        final Map<String, String> engineDataMap = XmlUtils.putAttributesInHash(elem);
        this.maxPower = 1000*Double.parseDouble(engineDataMap.get("max_power_kW"));
        this.cylinderVolume = 0.001*Double.parseDouble(engineDataMap.get("cylinder_vol_cm3"));  // in liter
        this.idleConsumptionRateLiterPerSecond = Double.parseDouble(engineDataMap.get("idle_cons_rate_linvh"))/3600.;
        this.minSpecificConsumption = Double.parseDouble(engineDataMap.get("cspec_min_g_per_kwh"))/3.6e9;
        this.effectivePressureMinimum = FuelConstants.CONVERSION_BAR_TO_PASCAL*Double.parseDouble(engineDataMap.get("pe_min_bar"));
        this.effectivePressureMaximum = FuelConstants.CONVERSION_BAR_TO_PASCAL*Double.parseDouble(engineDataMap.get("pe_max_bar"));
        this.idleRotationRate = Double.parseDouble(engineDataMap.get("idle_rotation_rate_invmin"))/60;
        this.maxRotationRate = Double.parseDouble(engineDataMap.get("max_rotation_rate_invmin"))/60;
        
        // gear box of engine
        Element gearsElem = elem.getChild(XmlElementNames.ConsumptionEngineGears);
        if(gearsElem != null){
            parseGears(gearsElem.getChildren(XmlElementNames.ConsumptionEngineGear));
        }
        else{
            setDefaultGears();
        }
        
    }
    
    // default gear box with 5 gears
    private void setDefaultGears() {
        gearRatios = new ArrayList<Double>();
        gearRatios.add(13.9);
        gearRatios.add(7.8);
        gearRatios.add(5.26);
        gearRatios.add(3.79);
        gearRatios.add(3.09);
    }

    private void parseGears(List<Element> gearElems){
        List<Double> localGears = new ArrayList<Double>();
        
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
        gearRatios = new ArrayList<Double>();
        for(Double phiGear : localGears ){
            gearRatios.add(phiGear.doubleValue());
        }
    }



    public double getMaxPower() {
        return maxPower;
    }



    public double getCylinderVolume() {
        return cylinderVolume;
    }



    public double getIdleConsumptionRateLiterPerSecond() {
        return idleConsumptionRateLiterPerSecond;
    }



    public double getEffectivePressureMinimum() {
        return effectivePressureMinimum;
    }



    public double getEffectivePressureMaximum() {
        return effectivePressureMaximum;
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

    public double getMinSpecificConsumption() {
        return minSpecificConsumption;
    }
}
