/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.roadmappings;

import org.movsim.simulator.Lane;
import org.movsim.simulator.Link;
//import org.movsim.traffic.Obstacle;
import org.movsim.simulator.RoadMapping;
import org.movsim.simulator.RoadNetwork;
import org.movsim.simulator.RoadSegment;
import org.movsim.simulator.TrafficSource;
import org.movsim.simulator.vehicles.Vehicle;

/**
 * Static utility functions that can be used in setting up road networks.
 */
public class RoadUtilities {

    /**
     * Joins two road segments together, given their ids.
     * 
     * @param roadNetwork
     * @param sourceId
     *            id of source road segment
     * @param sinkId
     *            id of sink road segment
     * @return the sink road segment (for convenience)
     */
    public static RoadSegment join(RoadNetwork roadNetwork, int sourceId, int sinkId) {

        final RoadSegment sourceRoad = roadNetwork.findById(sourceId);
        final RoadSegment sinkRoad = roadNetwork.findById(sinkId);
        // sourceRoad.roadMapping().setRoadColor(0xff00ff); // magentaColor
        // sinkRoad.roadMapping().setRoadColor(0x00ffff); // //cyanColor
        return Link.addJoin(sourceRoad, sinkRoad);
    }

    /**
     * Convenience function to create a TrafficSource with the given parameters.
     * 
     * @param inflow inflow, vehicles per hour
     * @param colorCar
     * @param colorTruck
     * @param proportionTrucks
     * @return the new traffic source
     */
    public static TrafficSource newTrafficSource(double inflow, int colorCar, int colorTruck, double proportionTrucks) {
        final TrafficSource source = new TrafficSource();
        source.setInflowVehiclesPerHour(inflow);
//        source.setColor(TrafficSource.CAR, colorCar);
//        source.setColor(TrafficSource.TRUCK, colorTruck);
        source.setProportionTrucks(proportionTrucks);
        return source;
    }

    /**
     * Creates a RoadSegment with the given roadMapping and adds it to the road network.
     * 
     * @param roadNetwork
     * @param roadMapping
     * @param source
     * 
     * @return the road segment added
     */
    public static RoadSegment addRoadSegment(RoadNetwork roadNetwork, RoadMapping roadMapping,
            TrafficSource source) {
        assert roadNetwork != null;
        assert roadMapping != null;
        assert roadMapping.roadLength() != 0.0;
        assert roadMapping.laneCount() > 0;
        assert roadMapping.roadWidth() > 0.0;
        assert source != null;

        final RoadSegment roadSegment = new RoadSegment(roadMapping, source);
        assert roadSegment.roadLength() == roadMapping.roadLength();
        assert roadSegment.laneCount() == roadMapping.laneCount();
        roadNetwork.add(roadSegment);
        return roadSegment;
    }

    /**
     * Adds a lane closure to a road segment by adding a number of obstacles to the road segment.
     * 
     * @param roadSegment
     * @param lane
     * @param position
     * @param length
     * @param obstacleColor
     */
    public static void addLaneClosure(RoadSegment roadSegment, int lane, double position,
            double length, int obstacleColor) {
        final int count = 12;
        final double elementLength = length / count;
        for (int i = 0; i < count; ++i) {
            final double pos = position - i * elementLength;
//            roadSegment.addObstacle(new Obstacle(pos, lane, elementLength + 1.0, 5.0));
        }
    }

    /**
     * Add the initial vehicles to a circular road segment.
     * 
     * @param roadSegment
     * @param vehicleCount
     *            the number of vehicles to add
     * @param testCarColor
     */
    public static void addCircleVehicles(RoadSegment roadSegment, int vehicleCount, int testCarColor) {

        final TrafficSource source = roadSegment.source();

        final double roadLength = roadSegment.roadLength();
        final double distance = roadLength / vehicleCount;
        final double gap = roadSegment.laneCount() * distance;
        int lane = Lane.LANE1;
        for (int i = 0; i < vehicleCount; ++i) {
            final Vehicle vehicle = source.newVehicleAtEquilibriumVelocity(lane, roadLength - (i + 1) * distance, gap);
            ++lane;
            if (lane >= roadSegment.laneCount()) {
                lane = Lane.LANE1;
            }
            if (vehicle.type() == Vehicle.Type.TEST_CAR) {
//                vehicle.setColor(testCarColor);
            }
            roadSegment.addVehicle(vehicle);
        }
    }

    /**
     * Add a roadblock consisting of an obstacle in each lane of the road segment.
     * 
     * @param roadSegment
     * @param position
     * @param obstacleColor
     */
    public static void addRoadBlock(RoadSegment roadSegment, double position, int obstacleColor) {
        for (int lane = Lane.LANE1; lane < roadSegment.laneCount(); ++lane) {
//            roadSegment.addObstacle(new Obstacle(position, lane, 10.0, 5.0, obstacleColor));
        }
    }

    /**
     * Remove a roadblock previously created using <code>addRoadBlock</code>.
     * 
     * @param roadSegment
     */
    public static void removeRoadBlock(RoadSegment roadSegment) {
        for (int lane = Lane.LANE1; lane < roadSegment.laneCount(); ++lane) {
            assert roadSegment.frontVehicleOnLane(lane).type() == Vehicle.Type.OBSTACLE;
            roadSegment.removeFrontVehicleOnLane(lane);
        }
    }

    private static final double LOOP_STRAIGHT = 120;
    private static final double LOOP_RADIUS = 140.0;
    private static final double ARC_RADIUS = 6.25 * LOOP_RADIUS; // minimum size that will fit loops
    private static final double ARC_STRAIGHT = 250;
    /**
     * The number of road segments in the main (straight) part of a cloverleaf junction.
     */
    public static final int CLOVERLEAF_CARRIAGEWAY_ROAD_SEGMENT_COUNT = 9;
    private static final double OBSTACLE_LENGTH = 1.0;
    private static final double OBSTACLE_WIDTH = 3.0;
    private static final int OBSTACLE_COLOR = RoadMapping.defaultRoadColor();
    // private static final int OBSTACLE_COLOR = 0xffffff; // white

    /**
     * Creates a cloverleaf interchange and adds it to the road network. Adds a source and sink to
     * each carriageway in the cloverleaf.
     * 
     * @param roadNetwork
     * @param source
     * @param laneCount
     * @param cX
     * @param cY
     * @param length
     * @return the id of the northbound carriageway
     */
    public static int addCloverleaf(RoadNetwork roadNetwork, TrafficSource source, int laneCount,
            double cX, double cY, double length) {
        assert source != null;
//        assert source.laneChangeModel(Vehicle.Type.CAR) != null;
//        assert source.laneChangeModel(Vehicle.Type.TRUCK) != null;
//        assert source.longitudinalDriverModel(Vehicle.Type.CAR) != null;
//        assert source.longitudinalDriverModel(Vehicle.Type.TRUCK) != null;
        final int northId = addCloverleaf(roadNetwork, laneCount, cX, cY, length);
        final int[] ids = { northId, northId + 9, northId + 18, northId + 27 };
        for (int id : ids) {
            final int[] exitIds = new int[] { id + 1, id + 5 };
//            roadNetwork.findById(id).setSource(new TrafficSourceRandomExit(source, exitIds, 0.8));
            roadNetwork.findById(id + CLOVERLEAF_CARRIAGEWAY_ROAD_SEGMENT_COUNT - 1).addDefaultSink();
        }
        return northId;
    }

    /**
     * Creates a cloverleaf interchange and adds it to the road network.
     * 
     * @param roadNetwork
     * @param laneCount
     * @param cX
     * @param cY
     * @param length
     * @return the id of the northbound carriageway
     */
    public static int addCloverleaf(RoadNetwork roadNetwork, int laneCount, double cX,
            double cY, double length) {
        assert roadNetwork != null;

        final double halfLength = length / 2;
        final double halfCarriagewaySeparation = 0.75 * laneCount * RoadMapping.DEFAULT_LANE_WIDTH;

        // add the vertical roadways, north and south
        double x0 = cX + halfCarriagewaySeparation;
        double y0 = cY + halfLength;
        final RoadSegment north = addCloverleafCarriageWay(roadNetwork, laneCount, x0, y0,
                length, Math.toRadians(90.0));

        x0 = cX - halfCarriagewaySeparation;
        y0 = cY - halfLength;
        final RoadSegment south = addCloverleafCarriageWay(roadNetwork, laneCount, x0, y0,
                length, Math.toRadians(-90.0));

        // add the horizontal roadways east and west
        x0 = cX - halfLength;
        y0 = cY + halfCarriagewaySeparation;
        final RoadSegment east = addCloverleafCarriageWay(roadNetwork, laneCount, x0, y0,
                length, Math.toRadians(0.0));

        x0 = cX + halfLength;
        y0 = cY - halfCarriagewaySeparation;
        final RoadSegment west = addCloverleafCarriageWay(roadNetwork, laneCount, x0, y0,
                length, Math.toRadians(180.0));

        // set clipping regions for the north and south carriageways, since they go 'under' the
        // north and south ones they can share the same clipping region
        final RoadSegment northCenter = roadNetwork.findById(north.id() + 4);
        final RoadSegment southCenter = roadNetwork.findById(south.id() + 4);
        final double halfCenterLength = 0.5 * northCenter.roadLength();
        final float d = (float)(halfCarriagewaySeparation + 0.5 * laneCount
                * RoadMapping.DEFAULT_LANE_WIDTH);
        northCenter.roadMapping().addClippingRegion(halfCenterLength - d, 2 * d);
        southCenter.roadMapping().addClippingRegion(halfCenterLength - d, 2 * d);

        final int rampLaneCount = 1;
        RoadSegment r, sourceRoadSegment, sinkRoadSegment;
        RoadMapping m;
        RoadMapping.PosTheta posTheta;

        // add the four arcs
        double arcAngle = -0.5 * Math.PI; // 90 degrees clockwise
        double radius = ARC_RADIUS;
        final int[] sourceIds = new int[] { north.id(), south.id(), east.id(), west.id() };
        int[] sinkIds = new int[] { east.id(), west.id(), south.id(), north.id() };
        for (int i = 0; i < sourceIds.length; ++i) {
            sourceRoadSegment = roadNetwork.findById(sourceIds[i] + 1);
            sinkRoadSegment = roadNetwork.findById(sinkIds[i] + 7);
            posTheta = sourceRoadSegment.roadMapping().endPosRamp();
            m = new RoadMappingArc(rampLaneCount, posTheta.x, posTheta.y, radius, posTheta.theta(),
                    arcAngle);
            r = new RoadSegment(m);
            Link.addLanePair(Lane.LANE1, sourceRoadSegment, Lane.LANE1, r);
            Link.addLanePair(Lane.LANE1, r, Lane.LANE1, sinkRoadSegment);
            roadNetwork.add(r);
        }

        // add the four loops
        arcAngle = -1.5 * Math.PI; // 270 degrees
        radius = LOOP_RADIUS;
        sinkIds = new int[] { west.id(), east.id(), north.id(), south.id() };
        for (int i = 0; i < sourceIds.length; ++i) {
            sourceRoadSegment = roadNetwork.findById(sourceIds[i] + 5);
            sinkRoadSegment = roadNetwork.findById(sinkIds[i] + 3);
            posTheta = sourceRoadSegment.roadMapping().endPosRamp();
            m = new RoadMappingArc(rampLaneCount, posTheta.x, posTheta.y, radius, posTheta.theta(),
                    arcAngle);
            r = new RoadSegment(m);
            Link.addLanePair(Lane.LANE1, sourceRoadSegment, Lane.LANE1, r);
            Link.addLanePair(Lane.LANE1, r, Lane.LANE1, sinkRoadSegment);
            roadNetwork.add(r);
        }

        return north.id();
    }

    /**
     * Creates one of the four carriageways required for a cloverleaf interchange and adds it to the
     * road segment.
     * 
     * @param roadNetwork
     * @param laneCount
     * @param startX
     * @param startY
     * @param length
     * @param theta
     * @return
     */
    private static RoadSegment addCloverleafCarriageWay(RoadNetwork roadNetwork, int laneCount,
            double startX, double startY, double length, double theta) {
        // A cloverleaf carriageway consists of nine road segments: four road segments for the
        // on-ramps and off-ramps. These consist of (in order): the arc off-ramp, the loop on-ramp,
        // the loop off-ramp and the arc on-ramp. Additionally there are five intermediate road
        // segments that join these road segments together.

        final double halfLength = length / 2;
        final double halfCarriagewaySeparation = 0.75 * laneCount * RoadMapping.DEFAULT_LANE_WIDTH;
        final double offset = ARC_RADIUS + 2 * halfCarriagewaySeparation - 3;
        final double startStraight = halfLength - offset - ARC_STRAIGHT;
        final double centerStraight = 2 * (halfCarriagewaySeparation + laneCount
                * RoadMapping.DEFAULT_LANE_WIDTH) + 20;
        final double interStraight = (length - centerStraight - 2 * (startStraight + ARC_STRAIGHT + LOOP_STRAIGHT)) / 2;

        RoadMapping.PosTheta posTheta;
        final int offRampLaneCount = 1;
        final double cosTheta = Math.cos(theta);
        final double sinTheta = Math.sin(theta);

        // add following: road, arc-off-ramp, road, loop-on-ramp, road, loop-off-ramp, road,
        // arc-on-ramp, road

        // initial road segment
        final RoadMapping m0 = new RoadMappingLine(laneCount, startX, startY, startX
                + startStraight * cosTheta, startY - startStraight * sinTheta);
        final RoadSegment r0 = new RoadSegment(m0);
        roadNetwork.add(r0);

        // intermediate road segment created for arc off-ramp
        posTheta = m0.endPos();
        final RoadMapping m1 = new RoadMappingLine(laneCount + offRampLaneCount, posTheta.x,
                posTheta.y, posTheta.x + ARC_STRAIGHT * cosTheta, posTheta.y
                        - ARC_STRAIGHT * sinTheta);
        final RoadSegment r1 = new RoadSegment(m1);
        r1.setLaneType(Lane.LANE1, Lane.Type.EXIT);
        roadNetwork.add(r1);
        Link.addJoin(r0, r1);

        // next road segment
        posTheta = m1.endPos();
        final RoadMapping m2 = new RoadMappingLine(laneCount, posTheta.x, posTheta.y, posTheta.x
                + interStraight * cosTheta, posTheta.y - interStraight * sinTheta);
        final RoadSegment r2 = new RoadSegment(m2);
        roadNetwork.add(r2);
        Link.addJoin(r1, r2);

        // intermediate road segment created for loop on-ramp
        posTheta = m2.endPos();
        final RoadMapping m3 = new RoadMappingLine(laneCount + offRampLaneCount, posTheta.x,
                posTheta.y, posTheta.x + LOOP_STRAIGHT * cosTheta, posTheta.y
                        - LOOP_STRAIGHT * sinTheta);
        final RoadSegment r3 = new RoadSegment(m3);
        r3.setLaneType(Lane.LANE1, Lane.Type.ENTRANCE);
//        r3.addObstacle(new Obstacle(r3.roadLength() - OBSTACLE_LENGTH, Lane.LANE1, OBSTACLE_LENGTH,
//                OBSTACLE_WIDTH, OBSTACLE_COLOR));
        roadNetwork.add(r3);
        Link.addJoin(r2, r3);

        // next road segment
        posTheta = m3.endPos();
        final RoadMapping m4 = new RoadMappingLine(laneCount, posTheta.x, posTheta.y, posTheta.x
                + centerStraight * cosTheta, posTheta.y - centerStraight * sinTheta);
        // m4.setRoadColor(0x00ffff);
        final RoadSegment r4 = new RoadSegment(m4);
        roadNetwork.add(r4);
        Link.addJoin(r3, r4);

        // intermediate road segment created for loop off-ramp
        posTheta = m4.endPos();
        final RoadMapping m5 = new RoadMappingLine(laneCount + offRampLaneCount, posTheta.x,
                posTheta.y, posTheta.x + LOOP_STRAIGHT * cosTheta, posTheta.y
                        - LOOP_STRAIGHT * sinTheta);
        final RoadSegment r5 = new RoadSegment(m5);
        r5.setLaneType(Lane.LANE1, Lane.Type.EXIT);
        roadNetwork.add(r5);
        Link.addJoin(r4, r5);

        // next road segment
        posTheta = m5.endPos();
        final RoadMapping m6 = new RoadMappingLine(laneCount, posTheta.x, posTheta.y, posTheta.x
                + interStraight * cosTheta, posTheta.y - interStraight * sinTheta);
        final RoadSegment r6 = new RoadSegment(m6);
        roadNetwork.add(r6);
        Link.addJoin(r5, r6);

        // intermediate road segment created for arc on-ramp
        posTheta = m6.endPos();
        final RoadMapping m7 = new RoadMappingLine(laneCount + offRampLaneCount, posTheta.x,
                posTheta.y, posTheta.x + ARC_STRAIGHT * cosTheta, posTheta.y
                        - ARC_STRAIGHT * sinTheta);
        final RoadSegment r7 = new RoadSegment(m7);
        r7.setLaneType(Lane.LANE1, Lane.Type.ENTRANCE);
//        r7.addObstacle(new Obstacle(r7.roadLength() - OBSTACLE_LENGTH, Lane.LANE1, OBSTACLE_LENGTH,
//                OBSTACLE_WIDTH, OBSTACLE_COLOR));
        roadNetwork.add(r7);
        Link.addJoin(r6, r7);

        // final road segment
        posTheta = m7.endPos();
        final RoadMapping m8 = new RoadMappingLine(laneCount, posTheta.x, posTheta.y, posTheta.x
                + startStraight * cosTheta, posTheta.y - startStraight * sinTheta);
        final RoadSegment r8 = new RoadSegment(m8);
        roadNetwork.add(r8);
        Link.addJoin(r7, r8);

        return r0;
    }
}
