/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

package org.movsim.simulator.roadnetwork.regulator;

import com.google.common.base.Preconditions;
import org.movsim.autogen.NotifyObjectType;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadSegmentDirection;
import org.movsim.simulator.roadnetwork.SignalPoint;
import org.movsim.simulator.vehicles.Vehicle;

import java.util.Collection;

public class NotifyObject {

    private final NotifyObjectType parameter;

    private final SignalPoint signalPoint;

    private final RoadSegment roadSegment;

    public NotifyObject(NotifyObjectType notifyObjectType, RoadSegment roadSegment) {
        this.parameter = Preconditions.checkNotNull(notifyObjectType);
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        Preconditions.checkArgument(notifyObjectType.isSetPosition() || notifyObjectType.isSetS(),
                "no position or s-coordinate provided in NotifyObject=" + notifyObjectType.getId());
        double position;

        if (notifyObjectType.isSetS()) {
            position = roadSegment.directionType() == RoadSegmentDirection.FORWARD ?
                    notifyObjectType.getS() :
                    roadSegment.roadLength() - notifyObjectType.getS();
        } else {
            position = notifyObjectType.getPosition();
        }
        signalPoint = new SignalPoint(position, roadSegment);
        // roadNetwork already constructed: adding of signalPoint to roadSegments possible here
        roadSegment.signalPoints().add(signalPoint);
        if (notifyObjectType.isSetId() && !Regulators.addNotifyObjectId(notifyObjectType.getId())) {
            throw new IllegalArgumentException("NotifyObject id=" + notifyObjectType.getId() + " not unique!");
        }

    }

    public Collection<Vehicle> getPassedVehicles() {
        return signalPoint.passedVehicles();
    }

    public String getName() {
        return parameter.isSetName() ? parameter.getName() : "-";
    }

    public String getId() {
        return parameter.isSetId() ? parameter.getId() : "-";
    }

    public double getPosition() {
        return signalPoint.position();
    }

    public RoadSegment getRoadSegment() {
        return roadSegment;
    }

    public String getRoadId() {
        return roadSegment.userId();
    }

    @Override
    public String toString() {
        return "NotifyObject [name=" + getName() + ", id=" + getId() + ", position=" + getPosition()
                + ", roadSegmentId=" + roadSegment.userId() + "]";
    }

    public NotifyObjectType getParameter() {
        return parameter;
    }

}
