package org.movsim.input.network.opendrive;

import java.io.File;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.movsim.network.OpenDriveNetwork;
import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Junction;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Junction.Connection;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Junction.Connection.LaneLink;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Lanes.LaneSection;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;
import org.movsim.roadmappings.RoadMappingArc;
import org.movsim.roadmappings.RoadMappingLine;
import org.movsim.roadmappings.RoadMappingPoly;
import org.movsim.simulator.roadnetwork.Lane;
import org.movsim.simulator.roadnetwork.Link;
import org.movsim.simulator.roadnetwork.RoadMapping;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.TrafficSink;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class OpenDriveHandlerJaxb {
    final static Logger logger = LoggerFactory.getLogger(OpenDriveHandlerJaxb.class);

    private OpenDriveHandlerJaxb() {
    }

    /**
     * Reads an OpenDrive format file, creating a road network.
     * 
     * @param roadNetwork
     * @param filename
     * @return true if the road network file exists and was successfully parsed, false otherwise.
     * @throws JAXBException
     * @throws SAXException
     */
    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, String filename) throws JAXBException, SAXException {
        File networkFile = new File(filename);
        OpenDRIVE openDriveNetwork = OpenDriveNetwork.loadNetwork(networkFile);
        return create(filename, openDriveNetwork, roadNetwork);
    }

    private static boolean create(String filename, OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork)
            throws IllegalArgumentException {
        for (Road road : openDriveNetwork.getRoad()) {
            final RoadMapping roadMapping = createRoadMapping(road);
            final RoadSegment roadSegment = createRoadSegment(roadMapping, road);
            roadNetwork.add(roadSegment);
        }

        joinRoads(openDriveNetwork, roadNetwork);
        handleJunctions(openDriveNetwork, roadNetwork);
        addDefaultSinksForUnconnectedRoad(roadNetwork);
        return true;
    }

    private static Road findByUserId(Collection<Road> roads, String id) {
        for (final Road road : roads) {
            if (road.getId().equals(id)) {
                return road;
            }
        }
        return null;
    }

    public static RoadMapping createRoadMapping(Road road) throws IllegalArgumentException {
        Preconditions.checkArgument(road.getLanes().getLaneSection().size() == 1,
                "exactly one <laneSection> needs to be defined!");

        int laneCount = 0;
        if (road.getLanes().getLaneSection().get(0).isSetRight()) {
            laneCount += road.getLanes().getLaneSection().get(0).getRight().getLane().size();
        }
        if (road.getLanes().getLaneSection().get(0).isSetLeft()) {
            laneCount += road.getLanes().getLaneSection().get(0).getLeft().getLane().size();
        }
        double laneWidth = 0.0;
        if (!road.getLanes().getLaneSection().get(0).getRight().getLane().isEmpty()) {
            laneWidth = road.getLanes().getLaneSection().get(0).getRight().getLane().get(0).getWidth().get(0).getA();
        } else if (!road.getLanes().getLaneSection().get(0).getLeft().getLane().isEmpty()) {
            laneWidth = road.getLanes().getLaneSection().get(0).getLeft().getLane().get(0).getWidth().get(0).getA();
        }

        final RoadMapping roadMapping;
        if (road.getPlanView().getGeometry().size() == 1) {
            Geometry geometry = road.getPlanView().getGeometry().get(0);
            if (geometry.isSetLine()) {
                roadMapping = RoadMappingLine.create(laneCount, geometry, laneWidth);
            } else if (geometry.isSetArc()) {
                roadMapping = RoadMappingArc.create(laneCount, geometry, laneWidth);
            } else if (geometry.isSetPoly3()) {
                throw new IllegalArgumentException("POLY3 geometry not yet supported (in road: " + road + " )");
            } else if (geometry.isSetSpiral()) {
                throw new IllegalArgumentException("SPIRAL geometry not yet supported (in road: " + road + " )");
            } else {
                throw new IllegalArgumentException("Unknown geometry for road: " + road);
            }
        } else {
            roadMapping = new RoadMappingPoly(laneCount, laneWidth);
            final RoadMappingPoly roadMappingPoly = (RoadMappingPoly) roadMapping;
            for (Geometry geometry : road.getPlanView().getGeometry()) {
                if (geometry.isSetLine()) {
                    roadMappingPoly.addLine(geometry);
                } else if (geometry.isSetArc()) {
                    roadMappingPoly.addArc(geometry);
                } else if (geometry.isSetPoly3()) {
                    throw new IllegalArgumentException("POLY3 geometry not yet supported (in road: " + road + " )");
                } else if (geometry.isSetSpiral()) {
                    throw new IllegalArgumentException("SPIRAL geometry not yet supported (in road: " + road + " )");
                } else {
                    throw new IllegalArgumentException("Unknown geometry for road: " + road);
                }
            }
        }
        return roadMapping;
    }

    private static RoadSegment createRoadSegment(RoadMapping roadMapping, Road road) {
        final RoadSegment roadSegment = new RoadSegment(roadMapping);

        roadSegment.setUserId(road.getId());
        roadSegment.setUserRoadname(road.getName());

        if (road.isSetElevationProfile()) {
            roadSegment.setElevationProfile(road.getElevationProfile());
        }

        // TODO reduce redunancy here
        for (final LaneSection laneSection : road.getLanes().getLaneSection()) {
            if (laneSection.isSetLeft()) {
                for (final org.movsim.network.autogen.opendrive.Lane leftLane : laneSection.getLeft().getLane()) {
                    final int laneIndex = OpenDriveHandlerUtils.leftLaneIdToLaneIndex(roadSegment, leftLane.getId());
                    setLaneType(laneIndex, leftLane, roadSegment);
                    // speed is definied lane-wise, but movsim handles speed limits on road segment level, further
                    // entries overwrite previous entry
                    if (leftLane.isSetSpeed()) {
                        roadSegment.setSpeedLimits(leftLane.getSpeed());
                    }
                }
            }
            if (laneSection.isSetRight()) {
                for (final org.movsim.network.autogen.opendrive.Lane rightLane : laneSection.getRight().getLane()) {
                    final int laneIndex = OpenDriveHandlerUtils.rightLaneIdToLaneIndex(roadSegment, rightLane.getId());
                    setLaneType(laneIndex, rightLane, roadSegment);
                    // speed is definied lane-wise, but movsim handles speed limits on road segment level, further
                    // entries overwrite previous entry
                    if (rightLane.isSetSpeed()) {
                        roadSegment.setSpeedLimits(rightLane.getSpeed());
                    }
                }
            }
        }
        if (road.isSetObjects()) {
            for (final OpenDRIVE.Road.Objects.Tunnel tunnel : road.getObjects().getTunnel()) {
                roadMapping.addClippingRegion(tunnel.getS(), tunnel.getLength());
            }
        }
        return roadSegment;
    }

    private static void setLaneType(int laneIndex, org.movsim.network.autogen.opendrive.Lane lane,
            RoadSegment roadSegment) {
        if (lane.getType().equals(Lane.Type.TRAFFIC.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneIndex, Lane.Type.TRAFFIC);
        } else if (lane.getType().equals(Lane.Type.ENTRANCE.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneIndex, Lane.Type.ENTRANCE);
            Vehicle obstacle = new Vehicle(roadSegment.roadLength(), 0.0, laneIndex, 1.0, 1.0);
            obstacle.setType(Vehicle.Type.OBSTACLE);
            roadSegment.addObstacle(obstacle);
        } else if (lane.getType().equals(Lane.Type.EXIT.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneIndex, Lane.Type.EXIT);
        } else if (lane.getType().equals(Lane.Type.SHOULDER.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneIndex, org.movsim.simulator.roadnetwork.Lane.Type.SHOULDER);
        } else {
            logger.warn("lane type " + lane + " not (yet) supported.");
        }
    }

    private static void joinRoads(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        // iterate through all the roads joining them up according to the links
        for (Road road : openDriveNetwork.getRoad()) {
            final RoadSegment roadSegment = roadNetwork.findByUserId(road.getId());
            if (roadSegment == null) {
                throw new IllegalArgumentException("cannot find roadSegment in network for road: " + road);
            }
            if (!road.isSetLink()) {
                roadSegment.addDefaultSink();
            } else {
                if (road.getLink().isSetPredecessor()
                        && road.getLink().getPredecessor().getElementType().equals("road")) {
                    final RoadSegment sourceRoadSegment = roadNetwork.findByUserId(road.getLink().getPredecessor()
                            .getElementId());
                    if (sourceRoadSegment == null) {
                        throw new IllegalArgumentException("Cannot find predecessor link:"
                                + road.getLink().getPredecessor());
                    }
                    for (final LaneSection laneSection : road.getLanes().getLaneSection()) {
                        if (laneSection.isSetLeft()) {
                            for (final org.movsim.network.autogen.opendrive.Lane lane : laneSection.getLeft().getLane()) {
                                if (lane.isSetLink() && lane.getLink().isSetPredecessor()) {
                                    final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(sourceRoadSegment,
                                            lane.getLink().getPredecessor().getId());
                                    final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(roadSegment,
                                            lane.getId());
                                    Link.addLanePair(fromLane, sourceRoadSegment, toLane, roadSegment);
                                }
                            }
                        }
                        if (laneSection.isSetRight()) {
                            for (final org.movsim.network.autogen.opendrive.Lane lane : laneSection.getRight()
                                    .getLane()) {
                                if (lane.isSetLink() && lane.getLink().isSetPredecessor()) {
                                    final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(sourceRoadSegment,
                                            lane.getLink().getPredecessor().getId());
                                    final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(roadSegment,
                                            lane.getId());
                                    Link.addLanePair(fromLane, sourceRoadSegment, toLane, roadSegment);
                                }
                            }
                        }
                    }
                }
                if (road.getLink().isSetSuccessor() && road.getLink().getSuccessor().getElementType().equals("road")) {
                    final RoadSegment sinkRoadSegment = roadNetwork.findByUserId(road.getLink().getSuccessor()
                            .getElementId());
                    for (final LaneSection laneSection : road.getLanes().getLaneSection()) {
                        if (laneSection.isSetLeft()) {
                            for (final org.movsim.network.autogen.opendrive.Lane lane : laneSection.getLeft().getLane()) {
                                if (lane.isSetLink() && lane.getLink().isSetSuccessor()) {
                                    final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(roadSegment,
                                            lane.getId());
                                    final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(sinkRoadSegment, lane
                                            .getLink().getSuccessor().getId());
                                    Link.addLanePair(fromLane, roadSegment, toLane, sinkRoadSegment);
                                }
                            }
                        }
                        if (laneSection.isSetRight()) {
                            for (final org.movsim.network.autogen.opendrive.Lane lane : laneSection.getRight()
                                    .getLane()) {
                                if (lane.isSetLink() && lane.getLink().isSetSuccessor()) {
                                    final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(roadSegment,
                                            lane.getId());
                                    final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(sinkRoadSegment, lane
                                            .getLink().getSuccessor().getId());
                                    Link.addLanePair(fromLane, roadSegment, toLane, sinkRoadSegment);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleJunctions(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        // iterate through all the junctions
        for (final Junction junction : openDriveNetwork.getJunction()) {
            for (final Connection connection : junction.getConnection()) {
                final RoadSegment incomingRoadSegment = roadNetwork.findByUserId(connection.getIncomingRoad());
                if (incomingRoadSegment == null) {
                    throw new IllegalStateException("Cannot find incoming road: " + connection.getIncomingRoad());
                }
                final RoadSegment connenctingRoadSegment = roadNetwork.findByUserId(connection.getConnectingRoad());
                if (connenctingRoadSegment == null) {
                    throw new IllegalStateException("Cannot find connecting road: " + connection.getConnectingRoad());
                }
                final Road road = findByUserId(openDriveNetwork.getRoad(), connection.getConnectingRoad());
                if (road.getLink().isSetPredecessor()
                        && road.getLink().getPredecessor().getElementType().equals("junction")
                        && road.getLink().getPredecessor().getElementId().equals(junction.getId())) {
                    for (final LaneLink laneLink : connection.getLaneLink()) {
                        final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(incomingRoadSegment,
                                laneLink.getFrom());
                        final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(connenctingRoadSegment,
                                laneLink.getTo());
                        logger.debug("lanepair from:" + laneLink.getFrom() + ",to:" + laneLink.getTo());
                        Link.addLanePair(fromLane, incomingRoadSegment, toLane, connenctingRoadSegment);
                    }
                } else if (road.getLink().isSetSuccessor()
                        && road.getLink().getSuccessor().getElementType().equals("junction")
                        && road.getLink().getSuccessor().getElementId().equals(junction.getId())) {
                    for (final LaneLink laneLink : connection.getLaneLink()) {
                        final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(connenctingRoadSegment,
                                laneLink.getFrom());
                        final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(incomingRoadSegment,
                                laneLink.getTo());
                        logger.debug("lanepair from:" + laneLink.getFrom() + ",to:" + laneLink.getTo());
                        Link.addLanePair(fromLane, connenctingRoadSegment, toLane, incomingRoadSegment);
                    }
                } else {
                    throw new IllegalArgumentException("Incorrect junction: id=" + junction.getId());
                }
            }
        }
    }

    private static void addDefaultSinksForUnconnectedRoad(RoadNetwork roadNetwork) {
        // finally iterate through all the road segments assigning a default sink to
        // any road segment with no sink connections
        for (final RoadSegment roadSegment : roadNetwork) {
            final int laneCount = roadSegment.laneCount();
            boolean hasSink = false;
            for (int lane = 0; lane < laneCount; ++lane) {
                if (roadSegment.sinkRoadSegment(lane) != null) {
                    hasSink = true;
                    break;
                }
            }
            if (hasSink == false) {
                roadSegment.setSink(new TrafficSink(roadSegment));
            }
        }
    }
}
