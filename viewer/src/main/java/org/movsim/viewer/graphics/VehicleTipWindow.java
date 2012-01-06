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
package org.movsim.viewer.graphics;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Point;
import java.awt.Window;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.movsim.simulator.vehicles.PhysicalQuantities;
import org.movsim.simulator.vehicles.Vehicle;

class VehicleTipWindow extends Window {
    /**
     * 
     */
    private final TrafficCanvas trafficCanvas;

    private static final long serialVersionUID = 1L;

    long currentPopupId = 0;

    final Frame owner;

    class PopupTimer {
        private final Timer timer = new Timer();

        private static final int popupTime = 3000; // milliseconds

        long timedPopupId;

        void start(long id) {
            // System.out.println("PopupTimer.start:"+id);//$NON-NLS-1$
            timedPopupId = id;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // System.out.println("PopupTimer.run:"+timedPopup);//$NON-NLS-1$
                    if (timedPopupId == currentPopupId) {
                        // System.out.println("PopupTimer.hide:"+timedPopup);//$NON-NLS-1$
                        setVisible(false);
                        VehicleTipWindow.this.trafficCanvas.vehiclePopup = null;
                    }
                }
            }, popupTime);
        }
    }

    public VehicleTipWindow(TrafficCanvas trafficCanvas, Frame owner) {
        super(owner);
        this.trafficCanvas = trafficCanvas;
        this.owner = owner;
    }

    @SuppressWarnings({ "boxing" })
    public void show(Point point, Vehicle vehicle) {
        String string;
        // if (vehicle.exitRoadSegmentId() ==
        // Vehicle.ROAD_SEGMENT_ID_NOT_SET) {
        // string = String.format(popupStringExitEndRoad, vehicle.id(),
        // vehicle.lane() + 1,
        // vehicle.position(), vehicle.velocity() * 3.6,
        // vehicle.acceleration(),
        // vehicle.distanceTravelled());
        // } else {
        final PhysicalQuantities vehiclePhysical = vehicle.physicalQuantities();
        string = String.format(this.trafficCanvas.popupString, vehicle.getId(), vehicle.getLabel(),
                vehicle.getLane() + 1, 
                vehiclePhysical.getMidPosition(),
                vehiclePhysical.getSpeed() * 3.6,
                vehiclePhysical.getAcc(),
                vehiclePhysical.getMidPosition(), 1, 1, 1);
        // }
        final Label label = new Label(string, Label.LEFT);
        label.setBackground(new Color(200, 220, 240));
        removeAll();
        add(label);
        pack();
        final Point screenLocation = owner.getLocationOnScreen();
        setLocation(point.x + screenLocation.x + 15, point.y + screenLocation.y + 90);
        currentPopupId = vehicle.getId();
        setVisible(true);
        final PopupTimer popupTimer = new PopupTimer();
        popupTimer.start(currentPopupId);
        if (TrafficCanvas.DEBUG) {
            final DecimalFormat twoDP = new DecimalFormat("#.##"); //$NON-NLS-1$
            System.out.println("Vehicle id:" + vehicle.getId());//$NON-NLS-1$
            System.out.println("  pos:" + (int) vehicle.physicalQuantities().getMidPosition());//$NON-NLS-1$
            //                System.out.println("  dis:" + (int)vehicle.distanceTravelled());//$NON-NLS-1$
            //                System.out.println("  energyUsed:" + (int)vehicle.energyUsed());//$NON-NLS-1$
            //                // System.out.println("  energyUsedAccelerating:" + (int)vehicle.energyUsedAccelerating());//$NON-NLS-1$
            //                // System.out.println("  energyUsedMoving:" + (int)vehicle.energyUsedMoving());//$NON-NLS-1$
            //                System.out.println("  fuel:" + twoDP.format(vehicle.fuelUsed()));//$NON-NLS-1$
            //                System.out.println("  fuelEconomy:" + twoDP.format(vehicle.fuelEconomy()));//$NON-NLS-1$
        }
    }
}