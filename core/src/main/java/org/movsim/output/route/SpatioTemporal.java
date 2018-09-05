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
package org.movsim.output.route;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.Vehicle.Type;
import org.movsim.utilities.LinearInterpolatedFunction;

import java.util.Comparator;
import java.util.TreeSet;

public class SpatioTemporal extends OutputOnRouteBase {

    private final double dxOutput;
    private final double dtOutput;

    private final double[] macroSpeed;
    private final double[] macroAcceleration;

    private double lastTimeOutput;

    private final FileSpatioTemporal fileWriter;

    public SpatioTemporal(double dxOut, double dtOut, RoadNetwork roadNetwork, Route route, boolean writeOutput) {
        super(roadNetwork, route);
        this.dxOutput = dxOut;
        this.dtOutput = dtOut;

        lastTimeOutput = 0;
        int size = (int) (route.getLength() / dxOut) + 1;
        macroSpeed = new double[size];
        macroAcceleration = new double[size];

        fileWriter = writeOutput ? new FileSpatioTemporal(route.getName()) : null;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if ((simulationTime - lastTimeOutput) >= dtOutput) {
            lastTimeOutput = simulationTime;
            calcData();
            if (fileWriter != null) {
                fileWriter.writeOutput(this, simulationTime);
            }
        }
    }

    private void calcData() {
        TreeSet<SpatialTemporal> dataPoints = gatherData();
        if (!dataPoints.isEmpty()) {
            interpolateGridData(dataPoints);
        }
    }

    private void interpolateGridData(TreeSet<SpatialTemporal> dataPoints) {
        final int size = dataPoints.size();
        final double[] xMicro = new double[size];
        final double[] vMicro = new double[size];
        final double[] aMicro = new double[size];
        int j = 0;
        for (SpatialTemporal dp : dataPoints) {
            LOG.debug("data point for interpolation={}", dp);
            vMicro[j] = dp.speed;
            xMicro[j] = dp.position;
            aMicro[j] = dp.acceleration;
            ++j;
        }

        LinearInterpolatedFunction speeds = new LinearInterpolatedFunction(xMicro, vMicro);
        LinearInterpolatedFunction accelerations = new LinearInterpolatedFunction(xMicro, aMicro);

        for (int i = 0; i < macroSpeed.length; ++i) {
            final double x = i * dxOutput;
            macroSpeed[i] = speeds.value(x);
            macroAcceleration[i] = accelerations.value(x);
        }
    }

    /**
     * Returns sorted set with increasing vehicle positions along the route. Not efficient but robust.
     */
    private TreeSet<SpatialTemporal> gatherData() {
        TreeSet<SpatialTemporal> dataPoints = new TreeSet<>(new Comparator<SpatialTemporal>() {
            @Override
            public int compare(SpatialTemporal o1, SpatialTemporal o2) {
                return (new Double(o1.position)).compareTo(new Double(o2.position));
            }
        });

        double positionOnRoute = 0;
        for (final RoadSegment roadSegment : route) {
            for (Vehicle veh : roadSegment) {
                if (veh.type() == Type.OBSTACLE) {
                    continue;
                }
                double position = positionOnRoute + veh.getFrontPosition();
                dataPoints.add(new SpatialTemporal(position, veh.getSpeed(), veh.getLength(), veh.getAcc()));
            }
            positionOnRoute += roadSegment.roadLength();
        }
        return dataPoints;
    }

    public double getDtOutput() {
        return dtOutput;
    }

    public double getDxOutput() {
        return dxOutput;
    }

    public int size() {
        return macroSpeed.length;
    }

    public double getAverageSpeed(int index) {
        return macroSpeed[index];
    }

    public double getAverageAcceleration(int index) {
        return macroAcceleration[index];
    }

    public double getTimeOffset() {
        return lastTimeOutput;
    }

    private static final class SpatialTemporal {
        final double position;
        final double speed;
        final double length;
        final double acceleration;

        SpatialTemporal(double position, double speed, double length, double acceleration) {
            this.position = position;
            this.speed = speed;
            this.length = length;
            this.acceleration = acceleration;
        }

        @Override
        public String toString() {
            return "SpatialTemporal [position=" + position + ", speed=" + speed + ", length=" + length
                    + ", acceleration=" + acceleration + "]";
        }
    }

}
