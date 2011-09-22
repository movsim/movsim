package org.movsim.facades;

import java.util.LinkedList;
import java.util.List;

import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.UpstreamBoundary;

public class MovsimControllers {

    private static MovsimControllers instance = null;
    
    private List<Long> inflowControlMainroads;
    private double initFlowPerLaneMainroads;
    
    private List<Long> inflowControlOnramps;
    private double initFlowPerLaneOnramps;
    
    private MovsimViewerFacade movsimViewerFacade;
    
    private MovsimControllers(){
        // singleton 
        movsimViewerFacade = MovsimViewerFacade.getInstance();
    }
    
    public static MovsimControllers getInstance(){
        if(instance == null){
            instance = new MovsimControllers();
        }
        return instance;
    }
    
    public void registerInflowControlMain(List<Long> ids){
        inflowControlMainroads = new LinkedList<Long>();
        initFlowPerLaneMainroads = registerInflowControl(ids, inflowControlMainroads);
    }
    
    public void registerInflowControlOnramp(List<Long> ids){
        inflowControlOnramps = new LinkedList<Long>();
        initFlowPerLaneOnramps = registerInflowControl(ids, inflowControlOnramps);
    }
    
    private double registerInflowControl(List<Long> ids, List<Long> listToRegister){
        double initFlowPerLane = -1;
        for(Long id : ids){
            final RoadSection road = movsimViewerFacade.findRoadById(id);
            if(road!=null){
                final UpstreamBoundary upBoundary = road.getUpstreamBoundary();
                if(upBoundary!=null){
                    if(initFlowPerLane == -1){
                        initFlowPerLane = upBoundary.getFlowPerLane(0);
                        System.out.println("initFlowPerLane="+initFlowPerLane);
                    }
                    System.out.println("register road id="+id);
                    listToRegister.add(new Long(id));  
                }
            }
        }
        return initFlowPerLane;
    }
    
    
    
    public void setInflowControlMainroads(double newFlowPerLane){
        setInflow(newFlowPerLane, inflowControlMainroads);
    }
    
    public void setInflowControlOnramps(double newFlowPerLane){
        setInflow(newFlowPerLane, inflowControlOnramps);
    }
    
    private void setInflow(double newFlowPerLane, List<Long> listRegisteredRoads){
        for(Long id : listRegisteredRoads){
            final RoadSection road = movsimViewerFacade.findRoadById(id);
            road.getUpstreamBoundary().setFlowPerLane(newFlowPerLane);
        }
    }
    
    
    public double getInitFlowPerLaneMainroads(){
        return initFlowPerLaneMainroads;
    }
    
    public double getInitFlowPerLaneOnramps(){
        return initFlowPerLaneOnramps;
    }
    
}
