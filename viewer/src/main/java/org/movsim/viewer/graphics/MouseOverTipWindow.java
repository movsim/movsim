/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.viewer.graphics;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Point;
import java.awt.Window;
import java.util.Timer;
import java.util.TimerTask;

import org.movsim.simulator.vehicles.PhysicalQuantities;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.MyRandom;
import org.movsim.utilities.Units;

import com.google.common.base.Preconditions;

class MouseOverTipWindow extends Window {

    private static final long serialVersionUID = 1L;

    private final TrafficCanvas trafficCanvas;

    private long currentPopupId = 0;

    private final Frame owner;

    class PopupTimer {

        private static final int POPUP_TIME_MS = 3000; // milliseconds

        private final Timer timer = new Timer();

        long timedPopupId;

        void start(long id) {
            timedPopupId = id;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (timedPopupId == currentPopupId) {
                        setVisible(false);
                        MouseOverTipWindow.this.trafficCanvas.vehiclePopup = null;
                    }
                }
            }, POPUP_TIME_MS);
        }
    }

    public MouseOverTipWindow(TrafficCanvas trafficCanvas, Frame owner) {
        super(owner);
        this.trafficCanvas = Preconditions.checkNotNull(trafficCanvas);
        this.owner = Preconditions.checkNotNull(owner);
    }

    protected void show(Point point, Vehicle vehicle) {
        String exitString;
        if (vehicle.exitRoadSegmentId() == Vehicle.ROAD_SEGMENT_ID_NOT_SET) {
            exitString = this.trafficCanvas.popupStringExitEndRoad;
        } else {
            exitString = this.trafficCanvas.roadNetwork.findById(vehicle.exitRoadSegmentId()).userId();
        }
        final PhysicalQuantities vehiclePhysical = vehicle.physicalQuantities();
        final String string =
                String.format(this.trafficCanvas.popupString, vehicle.getId(), vehicle.getLabel(), vehicle.type(),
                        vehicle.lane(), vehiclePhysical.getFrontPosition(), vehiclePhysical.getSpeed() * Units.MS_TO_KMH,
                        vehiclePhysical.getAcc(), vehicle.totalTravelDistance(), exitString);
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
    }

    protected void show(Point point, String info) {
        final Label label = new Label(info, Label.LEFT);
        label.setBackground(new Color(200, 220, 240));
        removeAll();
        add(label);
        pack();
        final Point screenLocation = owner.getLocationOnScreen();
        setLocation(point.x + screenLocation.x + 15, point.y + screenLocation.y + 90);
        currentPopupId = MyRandom.nextInt();
        setVisible(true);
        final PopupTimer popupTimer = new PopupTimer();
        popupTimer.start(currentPopupId);
    }
}