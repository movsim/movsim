/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.facades;

import java.util.List;

public class MovsimControllers {

    private static MovsimControllers instance = null;

    private final MovsimViewerFacade movsimViewerFacade;

    private List<Long> inflowControlMainroads;
    private double initFlowPerLaneMainroads;

    private List<Long> inflowControlOnramps;
    private double initFlowPerLaneOnramps;

    private List<Long> outflowControlMainroads;

    private MovsimControllers() {
        // singleton
        movsimViewerFacade = MovsimViewerFacade.getInstance();
    }

    public static synchronized MovsimControllers getInstance() {
        if (instance == null) {
            instance = new MovsimControllers();
        }
        return instance;
    }

    // public void registerInflowControlMain(List<Long> ids){
    // inflowControlMainroads = new LinkedList<Long>();
    // initFlowPerLaneMainroads = registerInflowControl(ids, inflowControlMainroads);
    // }
    //
    // public void registerInflowControlOnramp(List<Long> ids){
    // inflowControlOnramps = new LinkedList<Long>();
    // initFlowPerLaneOnramps = registerInflowControl(ids, inflowControlOnramps);
    // }

    // private double registerInflowControl(List<Long> ids, List<Long> listToRegister){
    // double initFlowPerLane = -1;
    // for(Long id : ids){
    // final RoadSection road = movsimViewerFacade.findRoadById(id);
    // if(road!=null){
    // final TrafficSource upBoundary = road.getUpstreamBoundary();
    // if(upBoundary!=null){
    // if(initFlowPerLane == -1){
    // initFlowPerLane = upBoundary.getFlowPerLane(0);
    // System.out.println("initFlowPerLane="+initFlowPerLane);
    // }
    // System.out.println("register road id="+id);
    // listToRegister.add(new Long(id));
    // }
    // }
    // }
    // return initFlowPerLane;
    // }
    //

    // public void registerOutflowControl(List<Long> ids){
    // outflowControlMainroads = new LinkedList<Long>();
    // for(Long id : ids){
    // final RoadSection road = movsimViewerFacade.findRoadById(id);
    // if(road!=null){
    // outflowControlMainroads.add(new Long(id));
    // }
    // }
    // }

    // public void setInflowControlMainroads(double newFlowPerLane){
    // setInflow(newFlowPerLane, inflowControlMainroads);
    // }
    //
    // public void setInflowControlOnramps(double newFlowPerLane){
    // setInflow(newFlowPerLane, inflowControlOnramps);
    // }
    //
    // private void setInflow(double newFlowPerLane, List<Long> listRegisteredRoads){
    // for(Long id : listRegisteredRoads){
    // final RoadSection road = movsimViewerFacade.findRoadById(id);
    // road.getUpstreamBoundary().setFlowPerLane(newFlowPerLane);
    // }
    // }
    //
    //
    // public double getInitFlowPerLaneMainroads(){
    // return initFlowPerLaneMainroads;
    // }
    //
    // public double getInitFlowPerLaneOnramps(){
    // return initFlowPerLaneOnramps;
    // }
    //
    // public double getInitFractionToOfframp(){
    // return 0.01; // init. parameter (not in xml input)
    // }
    //
    //
    // public void setOutflowFractionMainroads(double newFraction){
    // for(Long id : outflowControlMainroads){
    // final RoadSection road = movsimViewerFacade.findRoadById(id);
    // road.setFractionOfLeavingVehicles(newFraction);
    // }
    // }
    //
    // public double getInitRelRedPhaseOfTrafficLight() {
    // final RoadSection road = movsimViewerFacade.findRoadById(-1); //TODO quick hack
    // for(TrafficLight tl : road.getTrafficLights()){
    // return tl.getRelativeRedPhase();
    // }
    // return 0; // error
    // }
    //
    // public void setRelativeRedPhaseOfTrafficLight(double initRelativeRedPhase) {
    // final RoadSection road = movsimViewerFacade.findRoadById(-1); //TODO quick hack
    // for(TrafficLight tl : road.getTrafficLights()){
    // tl.setRelativeRedPhase(initRelativeRedPhase);
    // }
    // }
}
