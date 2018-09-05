package org.movsim.viewer.javafx;

import org.movsim.simulator.SimulationRunnable;
import org.movsim.simulator.roadnetwork.boundaries.AbstractTrafficSource;
import org.movsim.simulator.roadnetwork.boundaries.TrafficSink;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Units;

import java.awt.*;

// TODO MouseHover
public class MouseHover {

    private SimulationRunnable simulationRunnable;

    public MouseHover(SimulationRunnable simulationRunnable) {
        this.simulationRunnable = simulationRunnable;
    }

    /**
     * vehicle mouse-over support
     */
    private String popupString;
    private String popupStringExitEndRoad;
    private Vehicle vehiclePopup;
    private long lastVehicleViewed = -1;

    public void setMessageStrings(String popupString, String popupStringExitEndRoad) {
        this.popupString = popupString;
        this.popupStringExitEndRoad = popupStringExitEndRoad;
    }

    public void setMessageStrings(String popupString, String popupStringExitEndRoad, String trafficInflowString,
                                  String perturbationRampingFinishedString, String perturbationAppliedString, String simulationFinished) {
        setMessageStrings(popupString, popupStringExitEndRoad);
    }

    public void showSinkMouseOverInfo(Point point, TrafficSink sink) {
        StringBuilder sb = new StringBuilder();
        sb.append("outflow: ");
        sb.append((int) (Units.INVS_TO_INVH * sink.measuredOutflow()));
        sb.append(" veh/h");

//        mouseOverTipWindow.setVisible(false);
//        mouseOverTipWindow.show(point, sb.toString());
    }

    public void showSourceMouseOverInfo(Point point, AbstractTrafficSource source) {
        StringBuilder sb = new StringBuilder();
        sb.append("set inflow: ");
        sb.append((int) (Units.INVS_TO_INVH * source.getTotalInflow(simulationRunnable.simulationTime())));
        sb.append(" veh/h, actual inflow: ");
        sb.append((int) (Units.INVS_TO_INVH * source.measuredInflow()));
        sb.append(" veh/h, queue: ");
        sb.append(source.getQueueLength());

//        mouseOverTipWindow.setVisible(false);
//        mouseOverTipWindow.show(point, sb.toString());
    }

    public void showVehicleMouseOverInfo(Point point, Vehicle vehicle) {
        if (vehiclePopup == null || vehiclePopup.getId() != vehicle.getId()) {
            lastVehicleViewed = vehicle.getId();
//            mouseOverTipWindow.setVisible(false);
//            mouseOverTipWindow.show(point, vehicle);
        }

    }

}
