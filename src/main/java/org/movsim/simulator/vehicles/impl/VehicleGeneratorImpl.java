/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.movsim.input.InputData;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.simulation.HeterogeneityInputData;
import org.movsim.input.model.vehicle.longModel.ModelInputData;
import org.movsim.input.model.vehicle.longModel.ModelInputDataACC;
import org.movsim.input.model.vehicle.longModel.ModelInputDataGipps;
import org.movsim.input.model.vehicle.longModel.ModelInputDataIDM;
import org.movsim.input.model.vehicle.longModel.ModelInputDataKCA;
import org.movsim.input.model.vehicle.longModel.ModelInputDataNSM;
import org.movsim.input.model.vehicle.longModel.ModelInputDataNewell;
import org.movsim.input.model.vehicle.longModel.ModelInputDataOVM_VDIFF;
import org.movsim.simulator.Constants;
import org.movsim.simulator.impl.MyRandom;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.equilibrium.EquilibriumProperties;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumACC;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumGipps;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumIDM;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumKCA;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumNSM;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumNewell;
import org.movsim.simulator.vehicles.equilibrium.impl.EquilibriumOVM_VDIFF;
import org.movsim.simulator.vehicles.longmodels.LongitudinalModel;
import org.movsim.simulator.vehicles.longmodels.impl.ACC;
import org.movsim.simulator.vehicles.longmodels.impl.Gipps;
import org.movsim.simulator.vehicles.longmodels.impl.IDM;
import org.movsim.simulator.vehicles.longmodels.impl.KCA;
import org.movsim.simulator.vehicles.longmodels.impl.NSM;
import org.movsim.simulator.vehicles.longmodels.impl.Newell;
import org.movsim.simulator.vehicles.longmodels.impl.OVM_VDIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VehicleGeneratorImpl implements VehicleGenerator {
    final static Logger logger = LoggerFactory.getLogger(VehicleGeneratorImpl.class);
    
    // Aufwand mit prototypes wg. einmaliger berechnung des FD 
    // Und ggf. einmaliger Neuberechnung FD nach Parameteraenderung !!
    
    private final String projectName;  
    
    // enthaelt die Menge der definierte Models ... notwendig fuer GUI 
    //private HashMap<String, LongitudinalModel> longModels;
    
    // enthaelt die Heterogenitaet der tatsaechlich simulierten Fahrzeuge 
    private HashMap<String, VehiclePrototype> prototypes;

    private double requiredTimestep;
    
    private final boolean isWithReactionTimes;
    
    public VehicleGeneratorImpl(boolean isWithGUI, InputData simInput){
        
        this.projectName = simInput.getProjectName();
       
        // erzeuge long models: 
        //longModels = new HashMap<String, LongitudinalModel>();
        //createLongModels(simInput);

        // erzeuge vehicle prototypen gemaess heterogeneity
        prototypes = new HashMap<String, VehiclePrototype>();
        final double sumFraction = createPrototypes(simInput);

        // normalize heterogeneity fractions
        normalizeFractions(sumFraction);
        
        // output fundamental diagrams
        if( !isWithGUI && simInput.getSimulationInput().getSingleRoadInput().isWithWriteFundamentalDiagrams()){
            writeFundamentalDiagrams();
        }
        
        isWithReactionTimes = checkForReactionTimes();
        
    }

    
    
    private double createPrototypes(InputData simInput) {
    	
        requiredTimestep = simInput.getSimulationInput().getTimestep();  //default for continuous micro models
    	
        Map<String, VehicleInput> vehInputMap = createMap(simInput.getVehicleInputData());
        
        List<HeterogeneityInputData> heterogenInputData = simInput.getSimulationInput().getSingleRoadInput().getHeterogeneityInputData();
        
        double sumFraction=0;
        for( HeterogeneityInputData heterogen : heterogenInputData){
            final String keyName = heterogen.getKeyName();
            logger.debug("key name={}", keyName);
            if(!vehInputMap.containsKey(keyName)){
                logger.info("no corresponding vehicle found. Ignore heterogeneity with label={}", keyName);
                continue;
            }
            VehicleInput vehInput = vehInputMap.get(keyName);
            
            final double vehLength = vehInput.getLength();
            final LongitudinalModel longModel = longModelFactory(vehInput.getModelInputData(), vehLength);
            
            EquilibriumProperties fundDia = fundDiagramFactory(vehLength, longModel);
            
            
            final double fraction = heterogen.getFraction();
            logger.info("fraction = {}", fraction);
            
            // TODO check logic: prototypes (e.g. obstacles) needed for IC but with fraction == 0
			//if (fraction > 0) {
				sumFraction += fraction;
				final VehiclePrototype vehProto = new VehiclePrototype(
						fraction, longModel, fundDia, vehInput);
				prototypes.put(keyName, vehProto);
				
				// set simulation update time here from model classes:
				final double requiredTimestepLocal = longModel.getRequiredUpdateTime();
				if( requiredTimestepLocal > Constants.SMALL_VALUE ){
				    if( Math.abs( requiredTimestepLocal-requiredTimestep ) > Constants.SMALL_VALUE){
						logger.error("inconsistent model input: cannot simulate these models with incompatible update times dtModel={} and dtSim={}", requiredTimestepLocal, requiredTimestep);
						System.exit(-1);
					}
					else{
						requiredTimestep = requiredTimestepLocal;
						logger.info("set simulation timestep to dt={} for model = {}", requiredTimestep, longModel.modelName());
					}
				}
				logger.debug("simulation timestep: dt={}. ", requiredTimestep);
			//}
			
        }
        return sumFraction;
    }
    
    
    private Map<String, VehicleInput> createMap(List<VehicleInput> vehicleInputData) {
        HashMap<String, VehicleInput> map = new HashMap<String, VehicleInput>();
        for(VehicleInput vehInput : vehicleInputData){
            final String keyName = vehInput.getLabel();
            map.put(keyName, vehInput);
        }
        return map;
    }



    private EquilibriumProperties fundDiagramFactory(double vehLength, LongitudinalModel longModel){
        if( longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_IDM)){
            return new EquilibriumIDM(vehLength, (IDM)longModel);
        }
        else if( longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_ACC)){
            return new EquilibriumACC(vehLength, (ACC)longModel);
        }
        else if(longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_OVM_VDIFF)){
            return new EquilibriumOVM_VDIFF(vehLength, (OVM_VDIFF)longModel);
        }
        else if(longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_GIPPS)){
            return new EquilibriumGipps(vehLength, (Gipps)longModel);
        }
        else if(longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_NEWELL)){
            return new EquilibriumNewell(vehLength, (Newell)longModel);
        }
        else if(longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_NSM)){
            return new EquilibriumNSM(vehLength, (NSM)longModel);
        }
        else if(longModel.modelName().equalsIgnoreCase(Constants.MODEL_NAME_KCA)){
            return new EquilibriumKCA(vehLength, (KCA)longModel);
        }
        else{
            logger.error("no fundamental diagram constructed for model {}. exit.", longModel.modelName());
            System.exit(0); 
        }
        return null; // not reached after exit !
        
    }
    
    private LongitudinalModel longModelFactory(ModelInputData modelInputData, double vehLength){
        String modelName = modelInputData.getModelName();
        LongitudinalModel longModel = null;
        logger.info("modelName = {}", modelName);
        if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_IDM)){
            longModel = new IDM(modelName, (ModelInputDataIDM)modelInputData);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_ACC)){
            longModel = new ACC(modelName, (ModelInputDataACC)modelInputData);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_OVM_VDIFF)){
            longModel = new OVM_VDIFF(modelName, (ModelInputDataOVM_VDIFF)modelInputData);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_GIPPS)){
            longModel = new Gipps(modelName, (ModelInputDataGipps)modelInputData);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_NEWELL)){
            return new Newell(modelName, (ModelInputDataNewell)modelInputData);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_NSM)){
            longModel = new NSM(modelName, (ModelInputDataNSM)modelInputData);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_KCA)){
            longModel = new KCA(modelName, (ModelInputDataKCA)modelInputData, vehLength); // needs vehicle length
        }
        else{
            logger.error("create model by inputParameter: Model {} not known !", modelName);
            System.exit(0); //TODO
        }
        return longModel; 
    }

    @SuppressWarnings("unchecked")
    private void normalizeFractions(double sumFraction){
        Iterator it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = (String)it.next();
            final double fraction = prototypes.get(key).fraction();
            prototypes.get(key).setFraction(fraction/sumFraction);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void writeFundamentalDiagrams(){
        Iterator it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = (String)it.next();
            final String filename = projectName+".fund_"+key;
            final VehiclePrototype proto = prototypes.get(key);
            if(proto.fraction()>0){
                // avoid writing fundDia of "obstacles" 
                proto.writeFundamentalDiagram(filename);
            }
        }
    }
    
   
    
    private LongitudinalModel longModelFactory(LongitudinalModel modelToCopy){
        LongitudinalModel longModel = null;
        final String modelName = modelToCopy.modelName();
        if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_IDM)){
            longModel = new IDM((IDM)modelToCopy);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_ACC)){
            longModel = new ACC((ACC)modelToCopy);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_OVM_VDIFF)){
            longModel = new OVM_VDIFF((OVM_VDIFF)modelToCopy);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_GIPPS)){
            longModel = new Gipps((Gipps)modelToCopy);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_NSM)){
            longModel = new NSM((NSM)modelToCopy);
        }
        else if(modelName.equalsIgnoreCase(Constants.MODEL_NAME_KCA)){
            longModel = new KCA((KCA)modelToCopy);
        }
        else{
            logger.error("create model by copy constructor: Model {} not known ! %n", modelName);
            System.exit(0); //TODO
        }
        return longModel; 
    }
    
    private CyclicBufferImpl cyclicBufferFactory(){
        if(isWithReactionTimes){
            return new CyclicBufferImpl();    
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public VehiclePrototype getVehiclePrototype() {
        final double randomNumber = MyRandom.nextDouble();
        double sumFraction = 0; 
        Iterator it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = (String)it.next();
            sumFraction += prototypes.get(key).fraction();
            if(sumFraction>=randomNumber){
                return prototypes.get(key);
            }
        }
        logger.error("no vehicle prototype found for randomNumber= {}", randomNumber);
        System.exit(-1);
        return null; // not reached after exit
    }
    
    
    public Vehicle createVehicle(VehiclePrototype prototype){
        final int vehID = MyRandom.nextInt(); // for veh index
        //final double length = prototype.length();
        //final double reactionTime = prototype.reactionTime();
        final VehicleInput vehInput = prototype.getVehicleInput();
        final LongitudinalModel longModel = longModelFactory(prototype.getLongModel());
        final CyclicBufferImpl cyclicBuffer = cyclicBufferFactory();
        
        final Vehicle veh = new VehicleImpl(vehID, longModel, vehInput, cyclicBuffer);
        return veh;
    }
    
    public Vehicle createVehicle(){
        VehiclePrototype prototype = getVehiclePrototype();
        return createVehicle(prototype);
    }
    

    public Vehicle createVehicle(String typeLabel) {
        if(!prototypes.containsKey(typeLabel)){
            logger.error("cannot create vehicle. label = {} not defined. exit. ", typeLabel);
            System.exit(-1);
        }
       VehiclePrototype prototype = prototypes.get(typeLabel);
       logger.info("create vehicle with label = {}", typeLabel);
       return createVehicle(prototype);
    }

    
    public double requiredTimestep(){
    	return requiredTimestep;
    }

    @SuppressWarnings("unchecked")
    private boolean checkForReactionTimes(){
        Iterator it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = (String)it.next();
            final VehiclePrototype prototype = prototypes.get(key);
            if( prototype.hasReactionTime() ){
                return true;
            }
        }
        return false;
    }

}
