package org.movsim.output.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.movsim.input.model.output.TravelTimesInput;
import org.movsim.input.model.output.impl.TravelTimeRouteInput;
import org.movsim.output.TravelTimes;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.utilities.impl.ExponentialMovingAverage;
import org.movsim.utilities.impl.ObservableImpl;
import org.movsim.utilities.impl.XYDataPoint;

public class TravelTimesImpl extends ObservableImpl implements TravelTimes {

    private List<TravelTimeRoute> routes;

    private final Map<Long,RoadSection> roadSectionsMap;
    
    // configure update interval
    private long updateIntervalCount = 100;   // init
    
    
    public TravelTimesImpl(final TravelTimesInput travelTimesInput, final Map<Long,RoadSection> roadSectionsMap){
        this.roadSectionsMap = roadSectionsMap;
        routes = new LinkedList<TravelTimeRoute>();
        for(final TravelTimeRouteInput routeInput : travelTimesInput.getRoutes()){
            routes.add(new TravelTimeRoute(routeInput));
        }
        
    }
    
    
    public void update(long iterationCount, double time, double timestep){
        
        final boolean doNotificationUpdate = (iterationCount%updateIntervalCount == 0);
        for(TravelTimeRoute route : routes){
            route.update(iterationCount, time, timestep, roadSectionsMap);
            if(doNotificationUpdate){
                route.calcEMA(time);
            }
        }
        
        if(doNotificationUpdate){
            notifyObservers(time);
            //System.out.println("n observers registered = "+ getObserversInTimeSize()+ " ... and notify them now: time="+time);
        }
    }
    
    
    
    public List<List<XYDataPoint>> getTravelTimeEmas(){
        List<List<XYDataPoint>> listOfEmas = new LinkedList<List<XYDataPoint>>();
        for(TravelTimeRoute route : routes){
            listOfEmas.add(route.getEmaPoints());
        }
        return listOfEmas;
    }
    
    public List<List<XYDataPoint>> getTravelTimeDataRoutes(){
        List<List<XYDataPoint>> listOfRoutes = new LinkedList<List<XYDataPoint>>();
        for(TravelTimeRoute route : routes){
            listOfRoutes.add(route.getDataPoints());
        }
        return listOfRoutes;
    }
    
    
    
    public void setUpdateInterval(long updateIntervalCount){
        this.updateIntervalCount = updateIntervalCount;
        
    }
    
    
    
    public List<Double> getTravelTimesEMA(double time, double tauEMA){
        final int N_DATA = 10;  // cut-off parameter 
        final ExponentialMovingAverage ema = new ExponentialMovingAverage(tauEMA);
        
        List<Double> ttEMAs = new LinkedList<Double>();
        for(TravelTimeRoute route : routes){
            //System.out.println("calc ema with size()="+route.getDataPoints().size());
            List<XYDataPoint> routeTravelTimes = route.getDataPoints();
            final int size = routeTravelTimes.size();
            ttEMAs.add( ema.calcEMA(time, routeTravelTimes.subList(Math.max(0, size-N_DATA), size) ));
        }
        return ttEMAs;
    }

    
    
    
    
}
