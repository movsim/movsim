package org.movsim.input.model.consumption;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.impl.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineModelInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(EngineModelInput.class);
    
    
    private double maxPower;
    private double cylinderVolume;
    private double idleConsumptionRateLiterPerHour;
    private double effectivePressureMinimumBar;
    private double effectivePressureMaximumBar;
    private double idleRotationRatePerMin;
    private double maxRotationRatePerMin;
    
    
    private TDoubleList gearRatios;
    
    
    public EngineModelInput(Element elem){
        
        final Map<String, String> engineDataMap = XmlUtils.putAttributesInHash(elem);
        
        this.maxPower = 1000*Double.parseDouble(engineDataMap.get("max_power_kW"));
        this.cylinderVolume = Double.parseDouble(engineDataMap.get("cylinder_vol"));
        this.idleConsumptionRateLiterPerHour = Double.parseDouble(engineDataMap.get("idle_cons_rate_linvh"));
        this.effectivePressureMinimumBar = Double.parseDouble(engineDataMap.get("pe_min_bar"));
        this.effectivePressureMaximumBar = Double.parseDouble(engineDataMap.get("pe_max_bar"));
        this.idleRotationRatePerMin = Double.parseDouble(engineDataMap.get("idle_rotation_rate"));
        this.maxRotationRatePerMin = Double.parseDouble(engineDataMap.get("max_rotation_rate"));
        
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
        gearRatios = new TDoubleArrayList();
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
        gearRatios = new TDoubleArrayList();
        for(Double phiGear : localGears ){
            gearRatios.add(phiGear.doubleValue());
        }
    }
}
