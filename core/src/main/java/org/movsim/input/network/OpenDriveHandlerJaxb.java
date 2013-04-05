package org.movsim.input.network;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.movsim.network.autogen.opendrive.Lane;
import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller.Control;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Junction;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Junction.Connection;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Junction.Connection.LaneLink;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Lanes.LaneSection;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.PlanView.Geometry;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.roadmappings.RoadMappingArc;
import org.movsim.roadmappings.RoadMappingLine;
import org.movsim.roadmappings.RoadMappingPoly;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.Lanes.LaneSectionType;
import org.movsim.simulator.roadnetwork.Lanes.RoadLinkElementType;
import org.movsim.simulator.roadnetwork.Link;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.TrafficSink;
import org.movsim.simulator.trafficlights.TrafficLightLocation;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.xml.NetworkLoadAndValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class OpenDriveHandlerJaxb {
    private static final Logger LOG = LoggerFactory.getLogger(OpenDriveHandlerJaxb.class);

    /** Mapping of signal-ids of single trafficlights to controller. */
    private final Map<String, Controller> signalIdsToController = new HashMap<>();
    /** Checks uniqueness of signal ids in <road> definitions. */
    private final Set<String> uniqueTrafficLightIdsInRoads = new HashSet<>();

    OpenDriveHandlerJaxb() {
    }

    /**
     * Reads an OpenDrive format file, creating a road network.
     * 
     * @param roadNetwork
     * @param filename
     * @return true if the road network file exists and was successfully parsed, false otherwise.
     * @throws JAXBException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static boolean loadRoadNetwork(RoadNetwork roadNetwork, String filename) throws JAXBException, SAXException,
            IllegalArgumentException {
        OpenDRIVE openDriveNetwork = NetworkLoadAndValidation.validateAndLoadOpenDriveNetwork(new File(filename));
        OpenDriveHandlerJaxb openDriveHandlerJaxb = new OpenDriveHandlerJaxb();
        return openDriveHandlerJaxb.create(filename, openDriveNetwork, roadNetwork);
    }

    private boolean create(String filename, OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork)
            throws IllegalArgumentException {
        createControllerMapping(openDriveNetwork);

        for (Road road : openDriveNetwork.getRoad()) {
            final RoadMapping roadMapping = createRoadMapping(road);
            for (Lanes.LaneSectionType laneSectionType : LaneSectionType.values()) {
                if (hasLaneSectionType(road, laneSectionType)) {
                    RoadSegment roadSegmentRight = createRoadSegment(laneSectionType, roadMapping, road);
                    if (roadSegmentRight != null) {
                        roadNetwork.add(roadSegmentRight);
                    }
                }
            }
        }
        LOG.info("created {} roadSegments.", roadNetwork.size());

        joinRoads(openDriveNetwork, roadNetwork);
        handleJunctions(openDriveNetwork, roadNetwork);
        addDefaultSinksForUnconnectedRoad(roadNetwork);
        return true;
    }

    private void createControllerMapping(OpenDRIVE openDriveNetwork) {
        for (Controller controller : openDriveNetwork.getController()) {
            for (Control control : controller.getControl()) {
                if (signalIdsToController.put(control.getSignalId(), controller) != null) {
                    throw new IllegalArgumentException("trafficlight id=" + control.getSignalId()
                            + " is referenced more than once in xodr <controller> definitions.");
                }
            }
        }
    }

    private static boolean hasLaneSectionType(Road road, LaneSectionType laneType) {
        if (!road.isSetLanes()) {
            return false;
        }
        if (laneType == Lanes.LaneSectionType.LEFT) {
            return road.getLanes().getLaneSection().get(0).isSetLeft();
        }
        if (laneType == Lanes.LaneSectionType.RIGHT) {
            return road.getLanes().getLaneSection().get(0).isSetRight();
        }
        return false; // CENTER lane not supported
    }

    private static Road findByUserId(Collection<Road> roads, String id) {
        for (Road road : roads) {
            if (road.getId().equals(id)) {
                return road;
            }
        }
        return null;
    }

    private static RoadMapping createRoadMapping(Road road) throws IllegalArgumentException {
        Preconditions.checkArgument(road.getLanes().getLaneSection().size() == 1,
                "exactly one <laneSection> needs to be defined, more <laneSection>s cannot be handled!");

        int roadLaneCount = 0; // total number of lanes in both driving directions
        if (road.getLanes().getLaneSection().get(0).isSetLeft()) {
            roadLaneCount += road.getLanes().getLaneSection().get(0).getLeft().getLane().size();
        }
        if (road.getLanes().getLaneSection().get(0).isSetRight()) {
            roadLaneCount += road.getLanes().getLaneSection().get(0).getRight().getLane().size();
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
                roadMapping = RoadMappingLine.create(roadLaneCount, geometry, laneWidth);
            } else if (geometry.isSetArc()) {
                roadMapping = RoadMappingArc.create(roadLaneCount, geometry, laneWidth);
            } else if (geometry.isSetPoly3()) {
                throw new IllegalArgumentException("POLY3 geometry not yet supported (in road: " + road + " )");
            } else if (geometry.isSetSpiral()) {
                throw new IllegalArgumentException("SPIRAL geometry not yet supported (in road: " + road + " )");
            } else {
                throw new IllegalArgumentException("Unknown geometry for road: " + road);
            }
        } else {
            roadMapping = new RoadMappingPoly(roadLaneCount, laneWidth);
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

    private RoadSegment createRoadSegment(LaneSectionType laneType, RoadMapping roadMapping, Road road) {
        // TODO cstr not working for bidirectional case !!
        final RoadSegment roadSegment = new RoadSegment(roadMapping);

        // only one laneSection can be handled
        List<Lane> lanes = (laneType == Lanes.LaneSectionType.LEFT) ? Preconditions.checkNotNull(road.getLanes()
                .getLaneSection().get(0).getLeft().getLane()) : Preconditions.checkNotNull(road.getLanes()
                .getLaneSection().get(0).getRight().getLane());

        if (laneType == Lanes.LaneSectionType.LEFT) {
            LOG.error("left lane section not yet impl. Will be ignored!");
            return null;
        }
        // TODO Left/right handling
        roadSegment.setUserId(road.getId());
        roadSegment.setUserRoadname(road.getName());

        if (road.isSetElevationProfile()) {
            roadSegment.setElevationProfile(road.getElevationProfile());
        }

        checkLaneIndexConventions(laneType, road.getId(), lanes);

        for (Lane lane : lanes) {
            int laneIndex = Math.abs(lane.getId()); // OpenDriveHandlerUtils.rightLaneIdToLaneIndex(roadSegment, laneIndex.getId());
            setLaneType(laneIndex, lane, roadSegment);
            // speed is definied lane-wise, but movsim handles speed limits on road segment level, further
            // entries overwrite previous entry
            if (lane.isSetSpeed()) {
                roadSegment.setSpeedLimits(lane.getSpeed());
            }
        }

        if (road.isSetSignals()) {
            for (Signal signal : road.getSignals().getSignal()) {
                // assure uniqueness of signal id for whole network

                boolean added = uniqueTrafficLightIdsInRoads.add(signal.getId());
                if (!added) {
                    throw new IllegalArgumentException("trafficlight signal with id=" + signal.getId()
                            + " is not unique in xodr network definition.");
                }
                Controller controller = signalIdsToController.get(signal.getId());
                if (controller == null) {
                    throw new IllegalArgumentException("trafficlight signal with id=" + signal.getId()
                            + " is not referenced in xodr <controller> definition.");
                }
                roadSegment.addTrafficLightLocation(new TrafficLightLocation(signal, controller));
            }
        }

        if (road.isSetObjects()) {
            for (OpenDRIVE.Road.Objects.Tunnel tunnel : road.getObjects().getTunnel()) {
                roadMapping.addClippingRegion(tunnel.getS(), tunnel.getLength());
            }
        }
        return roadSegment;
    }

    private static void checkLaneIndexConventions(LaneSectionType laneType, String roadId, List<Lane> lanes) {
        int minIndex = Integer.MAX_VALUE;
        int maxIndex = Integer.MIN_VALUE;
        for (Lane lane : lanes) {
            minIndex = Math.min(minIndex, lane.getId());
            maxIndex = Math.max(maxIndex, lane.getId());
            if (lane.getId() == 0) {
                throw new IllegalArgumentException(
                        "usage of the laneIndex index={} for a normal lane in xodr. 0 is reserved for a <center> lane-section. roadId="
                                + roadId);
            }
            if (laneType == Lanes.LaneSectionType.LEFT && lane.getId() < 0) {
                throw new IllegalArgumentException("lane indices of a <laneSection><left> must be positive in roadId="
                        + roadId);
            }
            if (laneType == Lanes.LaneSectionType.RIGHT && lane.getId() > 0) {
                LOG.warn("lane indices of a <laneSection><right> must be negative in roadId=" + roadId);
            }
        }
        if (Math.abs(minIndex) != 1 && Math.abs(maxIndex) != 1) {
            System.out.println("minIndex=" + minIndex + ", maxIndex=" + maxIndex);
            throw new IllegalArgumentException("minimum lane index must start with 1 or -1 in roadId=" + roadId);
        }
        if (Math.abs(Math.abs(maxIndex) - Math.abs(minIndex)) != lanes.size() - 1) {
            LOG.info("minIndex={}, maxIndex={}", minIndex, maxIndex);
            LOG.info("lanes.size={}", lanes.size());
            throw new IllegalArgumentException("lane indices not continuous in road id=" + roadId);
        }
    }

    private static void setLaneType(int laneNumber, Lane lane, RoadSegment roadSegment) {
        LOG.debug("laneNumber={}, roadSegmentId={}", laneNumber, roadSegment.userId());
        if (lane.getType().equals(Lanes.Type.TRAFFIC.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneNumber, Lanes.Type.TRAFFIC);
        } else if (lane.getType().equals(Lanes.Type.ENTRANCE.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneNumber, Lanes.Type.ENTRANCE);
            Vehicle obstacle = new Vehicle(roadSegment.roadLength(), 0.0, laneNumber, 1.0, 1.0);
            obstacle.setType(Vehicle.Type.OBSTACLE);
            roadSegment.addObstacle(obstacle);
        } else if (lane.getType().equals(Lanes.Type.EXIT.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneNumber, Lanes.Type.EXIT);
        } else if (lane.getType().equals(Lanes.Type.SHOULDER.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneNumber, org.movsim.simulator.roadnetwork.Lanes.Type.SHOULDER);
        } else {
            LOG.warn("laneIndex type " + lane + " not supported.");
        }
    }

    private static void joinRoads(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        // iterate through all the roads joining them up according to the links
        for (Road road : openDriveNetwork.getRoad()) {
            RoadSegment roadSegment = roadNetwork.findByUserId(road.getId());
            if (roadSegment == null) {
                throw new IllegalArgumentException("cannot find roadSegment in network for road: " + road);
            }

            if (!road.isSetLink()) {
                roadSegment.addDefaultSink();
                continue;
            }

            if (hasRoadPredecessor(road)) {
                RoadSegment sourceRoadSegment = getSourceRoadSegment(roadNetwork, road);
                for (LaneSection laneSection : road.getLanes().getLaneSection()) {
                    if (laneSection.isSetCenter()) {
                        LOG.warn("cannot handle center lane");
                        continue;
                    }
                    List<org.movsim.network.autogen.opendrive.Lane> lanes = laneSection.isSetLeft() ? laneSection
                            .getLeft().getLane() : laneSection.getRight().getLane();
                    for (Lane lane : lanes) {
                        if (lane.isSetLink() && lane.getLink().isSetPredecessor()) {
                            int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(sourceRoadSegment, lane.getLink()
                                    .getPredecessor().getId());
                            int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(roadSegment, lane.getId());
                            Link.addLanePair(fromLane, sourceRoadSegment, toLane, roadSegment);
                        }
                    }
                }
            }

            if (hasRoadSuccessor(road)) {
                RoadSegment sinkRoadSegment = getRoadSuccessor(roadNetwork, road);
                for (LaneSection laneSection : road.getLanes().getLaneSection()) {
                    if (laneSection.isSetCenter()) {
                        LOG.warn("cannot handle center lane");
                        continue;
                    }
                    List<Lane> lanes = laneSection.isSetLeft() ? laneSection.getLeft().getLane() : laneSection
                            .getRight().getLane();
                    addLaneLinkage(roadSegment, sinkRoadSegment, lanes);
                }
            }
        }
    }

    private static void addLaneLinkage(RoadSegment roadSegment, RoadSegment sinkRoadSegment, List<Lane> lanes) {
        for (Lane lane : lanes) {
            if (lane.isSetLink() && lane.getLink().isSetSuccessor()) {
                int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(roadSegment, lane.getId());
                int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(sinkRoadSegment, lane.getLink().getSuccessor()
                        .getId());
                Link.addLanePair(fromLane, roadSegment, toLane, sinkRoadSegment);
            }
        }
    }

    private static RoadSegment getRoadSuccessor(RoadNetwork roadNetwork, Road road) {
        return Preconditions.checkNotNull(roadNetwork.findByUserId(road.getLink().getSuccessor().getElementId()),
                "Cannot find successor link:" + road.getLink().getSuccessor());
    }

    private static boolean hasRoadSuccessor(Road road) {
        return road.getLink().isSetSuccessor()
                && road.getLink().getSuccessor().getElementType().equals(RoadLinkElementType.ROAD.xodrIdentifier());
    }

    private static RoadSegment getSourceRoadSegment(RoadNetwork roadNetwork, Road road) {
        return Preconditions.checkNotNull(roadNetwork.findByUserId(road.getLink().getPredecessor().getElementId()),
                "Cannot find predecessor link:" + road.getLink().getPredecessor());
    }

    private static boolean hasRoadPredecessor(Road road) {
        return road.getLink().isSetPredecessor()
                && road.getLink().getPredecessor().getElementType().equals(RoadLinkElementType.ROAD.xodrIdentifier());
    }

    private static void handleJunctions(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        // iterate through all the junctions
        for (Junction junction : openDriveNetwork.getJunction()) {
            for (Connection connection : junction.getConnection()) {
                RoadSegment incomingRoadSegment = Preconditions.checkNotNull(
                        roadNetwork.findByUserId(connection.getIncomingRoad()), "Cannot find incoming road: "
                                + connection.getIncomingRoad());
                RoadSegment connenctingRoadSegment = Preconditions.checkNotNull(
                        roadNetwork.findByUserId(connection.getConnectingRoad()), "Cannot find connecting road: "
                                + connection.getConnectingRoad());
                Road road = findByUserId(openDriveNetwork.getRoad(), connection.getConnectingRoad());
                if (roadPredecessorIsJunction(junction, road)) {
                    for (final LaneLink laneLink : connection.getLaneLink()) {
                        final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(incomingRoadSegment,
                                laneLink.getFrom());
                        final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(connenctingRoadSegment,
                                laneLink.getTo());
                        LOG.debug("lanepair from={} to={}", laneLink.getFrom(), laneLink.getTo());
                        Link.addLanePair(fromLane, incomingRoadSegment, toLane, connenctingRoadSegment);
                    }
                } else if (roadSuccessorIsJunction(junction, road)) {
                    for (final LaneLink laneLink : connection.getLaneLink()) {
                        final int fromLane = OpenDriveHandlerUtils.laneIdToLaneIndex(connenctingRoadSegment,
                                laneLink.getFrom());
                        final int toLane = OpenDriveHandlerUtils.laneIdToLaneIndex(incomingRoadSegment,
                                laneLink.getTo());
                        LOG.debug("lanepair from={} to={}", laneLink.getFrom(), laneLink.getTo());
                        Link.addLanePair(fromLane, connenctingRoadSegment, toLane, incomingRoadSegment);
                    }
                } else {
                    throw new IllegalArgumentException("Incorrect junction: id=" + junction.getId());
                }
            }
        }
    }

    private static boolean roadSuccessorIsJunction(Junction junction, Road road) {
        return road.getLink().isSetSuccessor()
                && road.getLink().getSuccessor().getElementType().equals(RoadLinkElementType.JUNCTION.xodrIdentifier())
                && road.getLink().getSuccessor().getElementId().equals(junction.getId());
    }

    private static boolean roadPredecessorIsJunction(Junction junction, Road road) {
        return road.getLink().isSetPredecessor()
                && road.getLink().getPredecessor().getElementType()
                        .equals(RoadLinkElementType.JUNCTION.xodrIdentifier())
                && road.getLink().getPredecessor().getElementId().equals(junction.getId());
    }

    private static void addDefaultSinksForUnconnectedRoad(RoadNetwork roadNetwork) {
        // finally iterate through all the road segments assigning a default sink to
        // any road segment with no sink connections
        int countSinks = 0;
        for (RoadSegment roadSegment : roadNetwork) {
            int laneCount = roadSegment.laneCount();
            boolean hasSink = false;
            for (int lane = Lanes.MOST_INNER_LANE; lane <= laneCount; ++lane) {
                if (roadSegment.sinkRoadSegment(lane) != null) {
                    hasSink = true;
                    break;
                }
            }
            if (!hasSink) {
                countSinks++;
                roadSegment.setSink(new TrafficSink(roadSegment));
            }
        }
        LOG.info("added {} default sinks to unconnected roads.", countSinks);
    }
}
