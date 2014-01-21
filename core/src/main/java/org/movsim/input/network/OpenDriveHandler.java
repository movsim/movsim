package org.movsim.input.network;

import java.io.File;
import java.util.ArrayList;
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
import org.movsim.roadmappings.LaneGeometries;
import org.movsim.roadmappings.LaneGeometries.LaneGeometry;
import org.movsim.roadmappings.RoadGeometry;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.roadmappings.RoadMappingPeer;
import org.movsim.roadmappings.RoadMappingUtils;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.Lanes.LaneSectionType;
import org.movsim.simulator.roadnetwork.Lanes.RoadLinkElementType;
import org.movsim.simulator.roadnetwork.Link;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadSegmentDirection;
import org.movsim.simulator.roadnetwork.RoadTypeSpeeds;
import org.movsim.simulator.roadnetwork.controller.GradientProfile;
import org.movsim.simulator.roadnetwork.controller.RoadObject;
import org.movsim.simulator.roadnetwork.controller.SpeedLimit;
import org.movsim.simulator.roadnetwork.controller.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.xml.NetworkLoadAndValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class OpenDriveHandler {
    private static final Logger LOG = LoggerFactory.getLogger(OpenDriveHandler.class);

    /** Mapping of signal-ids of single trafficlights to controller. */
    private final Map<String, Controller> signalIdsToController = new HashMap<>();
    /** Checks uniqueness of signal ids in <road> definitions. */
    private final Set<String> uniqueTrafficLightIdsInRoads = new HashSet<>();

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
        OpenDriveHandler openDriveHandlerJaxb = new OpenDriveHandler();
        return openDriveHandlerJaxb.create(filename, openDriveNetwork, roadNetwork);
    }

    private boolean create(String filename, OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork)
            throws IllegalArgumentException {
        createControllerMapping(openDriveNetwork, roadNetwork);
        createRoadSegments(openDriveNetwork, roadNetwork);
        joinRoads(openDriveNetwork, roadNetwork);
        handleJunctions(openDriveNetwork, roadNetwork);
        addDefaultSinksToUnconnectedRoad(roadNetwork);
        checkIfAllLanesAreConnected(roadNetwork);
        return true;
    }

    private void createControllerMapping(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        Preconditions.checkArgument(roadNetwork.size() == 0, "parse controllers first");
        for (Controller controller : openDriveNetwork.getController()) {
            for (Control control : controller.getControl()) {
                if (signalIdsToController.put(control.getSignalId(), controller) != null) {
                    throw new IllegalArgumentException("trafficlight id=" + control.getSignalId()
                            + " is referenced more than once in xodr <controller> definitions.");
                }
            }
        }
        LOG.info("registered {} traffic light signals in road network.", signalIdsToController.size());
    }

    private void createRoadSegments(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        for (Road road : openDriveNetwork.getRoad()) {
            boolean hasPeer = hasPeer(road);
            if (hasPeer) {
                LOG.info("road={} consists of peers", road.getId());
            }
            final RoadMapping roadMapping = createRoadMappings(road);
            for (LaneSectionType laneType : Lanes.LaneSectionType.values()) {
                if (hasLaneSectionType(road, laneType)) {
                    RoadSegment roadSegment = createRoadSegment(laneType, road, hasPeer, roadMapping);
                    if (roadSegment == null) {
                        throw new IllegalStateException("could not create roadSegment for road=" + road.getId());
                    }
                    roadNetwork.add(roadSegment);
                    LOG.info("created roadSegment={} with laneCount={}", roadSegment.userId(), roadSegment.laneCount());
                }
            }
            if (hasPeer) {
                RoadSegment roadSegmentRight = getRoadSegment(roadNetwork, road.getId(), LaneSectionType.RIGHT);
                RoadSegment roadSegmentLeft = getRoadSegment(roadNetwork, road.getId(), LaneSectionType.LEFT);
                roadSegmentLeft.setPeerRoadSegment(roadSegmentRight);
                roadSegmentRight.setPeerRoadSegment(roadSegmentLeft);
            }
        }
        LOG.info("created {} roadSegments.", roadNetwork.size());
    }

    private static boolean hasPeer(Road road) {
        for (LaneSectionType laneType : Lanes.LaneSectionType.values()) {
            if (!hasLaneSectionType(road, laneType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPeer(boolean hasPeer, LaneSectionType laneSectionType) {
        return (hasPeer && laneSectionType == Lanes.LaneSectionType.LEFT);
    }

    private static boolean hasLaneSectionType(Road road, LaneSectionType laneType) {
        if (!road.isSetLanes()) {
            throw new IllegalArgumentException("road=" + road.getId() + " defined without lanes.");
        }
        if (laneType == Lanes.LaneSectionType.LEFT) {
            return road.getLanes().getLaneSection().get(0).isSetLeft();
        }
        if (laneType == Lanes.LaneSectionType.RIGHT) {
            return road.getLanes().getLaneSection().get(0).isSetRight();
        }
        return false; // xodr CENTER lane not supported
    }

    private RoadSegment createRoadSegment(LaneSectionType laneType, Road road, boolean hasPeer, RoadMapping roadMapping) {

        Preconditions.checkArgument(road.getLanes().getLaneSection().size() == 1,
                "cannot handle more than one laneSection in roadId=" + road.getId());
        LaneSection laneSection = road.getLanes().getLaneSection().get(0);
        Preconditions.checkArgument(
                laneType == Lanes.LaneSectionType.LEFT ? laneSection.isSetLeft() : laneSection.isSetRight(), "road="
                        + road.getId() + " has no " + laneType.toString() + " lane defined.");
        List<Lane> lanes = (laneType == Lanes.LaneSectionType.LEFT) ? laneSection.getLeft().getLane() : laneSection
                .getRight().getLane();
        Preconditions.checkArgument(lanes.size() > 0, "no lanes in laneSection=" + laneType.toString() + " on road="
                + road.getId() + " defined.");

        // final RoadMapping roadMapping = createRoadMapping(laneType, road);

        final RoadSegment roadSegment = laneType.isReverseDirection() ? new RoadSegment(roadMapping.roadLength(),
                lanes.size(), new RoadMappingPeer(roadMapping), RoadSegmentDirection.BACKWARD) : new RoadSegment(
                roadMapping.roadLength(), lanes.size(), roadMapping, RoadSegmentDirection.FORWARD);

        roadSegment.setUserId(getRoadSegmentId(road.getId(), laneType, hasPeer));
        roadSegment.setUserRoadname(road.getName());
        if (road.isSetType() && !road.getType().isEmpty()) {
            if (road.getType().size() > 1) {
                LOG.error("Movsim considers only first entry of the road.type and ignores the others defined for road="
                        + road.getId());
            }
            double freeFlowSpeed = RoadTypeSpeeds.INSTANCE.getFreeFlowSpeed(road.getType().iterator().next().getType());
            roadSegment.setFreeFlowSpeed((int) freeFlowSpeed);
        }

        if (road.isSetElevationProfile()) {
            GradientProfile elevationProfile = new GradientProfile(road.getElevationProfile(), roadSegment);
            roadSegment.roadObjects().add(elevationProfile);
        }

        checkLaneIndexConventions(laneType, road.getId(), lanes);

        for (Lane lane : lanes) {
            int laneIndex = laneIdToLaneIndex(lane.getId());
            setLaneType(laneIndex, lane, roadSegment);
            // speed is definied lane-wise, but movsim handles speed limits on road segment level, further
            // entries overwrite previous entry
            if (lane.isSetSpeed()) {
                LOG.error("speed limit for lane has been replaced by road.object, see building block example.");
            }
        }

        if (road.isSetObjects()) {
            for (OpenDRIVE.Road.Objects.Object roadObject : road.getObjects().getObject()) {
                // FIXME handling of both directions requires a clone copy of the roadObject which is added to the movsim object. Such copy
                // methods can be generated by jaxb but this seems to slow down the eclipse build performance significantly
                Preconditions.checkArgument(roadObject.isSetOrientation(), "no orientation set for xodr roadObject="
                        + roadObject.getId()
                        + " (this is not required by the xodr-xsd but currently for consistent movsim input).");

                if (hasPeer && roadObject.isSetOrientation()
                        && !laneType.idAppender().equals(roadObject.getOrientation())) {
                    // ignore object in other driving direction
                    continue;
                }

                if (laneType.isReverseDirection()) {
                    double originalS = roadObject.getS();
                    roadObject.setS(roadSegment.roadLength() - originalS);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                                "Transform road object position for backward link: roadObject={}, originalPosition={}, roadSegment position="
                                        + roadObject.getS(), roadObject.getId(), originalS);
                    }
                }

                String roadObjectType = roadObject.getType();
                if (roadObjectType.equals(RoadObject.XodrRoadObjectType.SPEEDLIMIT.xodrIdentifier())) {
                    SpeedLimit speedLimit = new SpeedLimit(roadObject, roadSegment);
                    LOG.info("try adding speed limit={}", speedLimit);
                    roadSegment.roadObjects().add(speedLimit);
                    if (roadObject.isSetValidLength()) {
                        if (roadObject.getValidLength() <= 0) {
                            throw new IllegalArgumentException("validLength=" + roadObject.getValidLength()
                                    + " but movsim's speedlimit expects a nontrivial length > 0.");
                        }
                        double endPosition = roadObject.getS() + roadObject.getValidLength();
                        if (endPosition > roadSegment.roadLength()) {
                            throw new IllegalArgumentException("speedlimit validity range="
                                    + roadObject.getValidLength() + " results in=" + endPosition
                                    + " which exceeds the roadlength of roadSegment=" + roadSegment.userId());
                        }
                        // adds the cancelation
                        roadSegment.roadObjects().add(
                                new SpeedLimit(endPosition, MovsimConstants.MAX_VEHICLE_SPEED, roadSegment));
                    }
                } else {
                    LOG.error("road object type " + roadObjectType + " not supported.");
                }

            }
        }
        if (road.isSetSignals()) {
            for (Signal signal : road.getSignals().getSignal()) {
                if (hasPeer && !signal.isSetOrientation()) {
                    throw new IllegalArgumentException("road=" + road.getId()
                            + " is bidirectional but signal orientation not set in signal=" + signal.getId());
                }
                if (hasPeer
                        && !(signal.getOrientation().equals(LaneSectionType.LEFT.idAppender()) || signal
                                .getOrientation().equals(LaneSectionType.RIGHT.idAppender()))) {
                    throw new IllegalArgumentException("signal.orientation= \"" + signal.getOrientation()
                            + "\" does not match the expected values (+,-).");
                }
                if (hasPeer && !laneType.idAppender().equals(signal.getOrientation())) {
                    // ignore signal for other driving direction
                    continue;
                }
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
                if (laneType.isReverseDirection()) {
                    double originalS = signal.getS();
                    signal.setS(roadSegment.roadLength() - originalS);
                    LOG.debug(
                            "Transform signal position from reverse direction: signal={}, originalPosition={}, roadSegment position="
                                    + signal.getS(), signal.getId(), originalS);
                }
                // roadSegment.addTrafficLight(new TrafficLight(signal, controller, roadSegment));
                roadSegment.roadObjects().add(new TrafficLight(signal, controller, roadSegment));
            }
        }

        if (road.isSetObjects()) {
            for (OpenDRIVE.Road.Objects.Tunnel tunnel : road.getObjects().getTunnel()) {
                if (laneType.isReverseDirection()) {
                    double originalS = tunnel.getS();
                    tunnel.setS(roadSegment.roadLength() - originalS);
                    LOG.debug(
                            "Transform tunnel position for reverse direction: tunnel={}, originalPosition={}, roadSegment position="
                                    + tunnel.getS(), tunnel.getId(), originalS);
                }
                roadMapping.addClippingRegion(tunnel.getS(), tunnel.getLength());
            }
        }

        return roadSegment;
    }

    private static RoadMapping createRoadMappings(Road road) throws IllegalArgumentException {
        Preconditions.checkArgument(road.getLanes().getLaneSection().size() == 1,
                "cannot handle more than one laneSection in roadId=" + road.getId());
        LaneSection firstLaneSection = road.getLanes().getLaneSection().get(0);
        Preconditions.checkState(firstLaneSection.isSetRight(),
                "expect always at least a <right> lanesection (development stage)");

        LaneGeometries laneGeometries = new LaneGeometries();
        if(firstLaneSection.isSetLeft()){
            int laneCount = firstLaneSection.getLeft().getLane().size();
            double laneWidth = firstLaneSection.getLeft().getLane().get(0).getWidth().get(0).getA();
            laneGeometries.setLeft(new LaneGeometry(laneCount, laneWidth));
        }

        int laneCount = firstLaneSection.getRight().getLane().size();
        double laneWidth = firstLaneSection.getRight().getLane().get(0).getWidth().get(0).getA();
        laneGeometries.setRight(new LaneGeometry(laneCount, laneWidth));

        List<RoadGeometry> roadGeometries = createRoadGeometries(road.getPlanView().getGeometry(), laneGeometries);
        return RoadMappingUtils.create(roadGeometries);
        // if (road.getLanes().getLaneSection().get(0).isSetLeft()) {
        // laneCount += road.getLanes().getLaneSection().get(0).getLeft().getLane().size();
        // // laneWidth = road.getLanes().getLaneSection().get(0).getLeft().getLane().get(0).getWidth().get(0).getA();
        // }

        // if (laneSection.isSetLeft()) {
        // this.laneCountPeer = laneSection.getLeft().getLane().size();
        // this.laneWidthPeer = laneSection.getLeft().getLane().get(0).getWidth().get(0).getA();
        // Preconditions.checkArgument(laneCountPeer > 0);
        // Preconditions.checkArgument(laneWidthPeer > 0);
        // }
        // List<RoadGeometry> roadGeometries = createRoadGeometries(road.getPlanView().getGeometry(), firstLaneSection);
        // return RoadMappingUtils.create(roadGeometries);
    }
    
    private static List<RoadGeometry> createRoadGeometries(List<Geometry> geometries, LaneGeometries laneGeometries) {
        List<RoadGeometry> roadGeometries = new ArrayList<>(geometries.size());
        for (Geometry geometry : geometries) {
            RoadGeometry roadGeometry = new RoadGeometry(geometry, laneGeometries);
            roadGeometries.add(roadGeometry);
        }
        // if (isPeer) {
        // // CHECK if reserved order if correct here. Not tested yet
        // Collections.reverse(roadGeometries);
        // }
        return roadGeometries;
    }

    private static String getRoadSegmentId(String roadId, LaneSectionType laneType, boolean hasPeer) {
        if (hasPeer) {
            return roadId + laneType.idAppender();
        }
        return roadId; // backwards compatibility
    }

    private static RoadSegment getRoadSegment(RoadNetwork roadNetwork, String roadId, LaneSectionType type) {
        RoadSegment roadSegment = roadNetwork.findByUserId(roadId);
        if (roadSegment == null) {
            roadSegment = roadNetwork.findByUserId(roadId
                    + (type == LaneSectionType.LEFT ? Lanes.LaneSectionType.LEFT.idAppender()
                            : Lanes.LaneSectionType.RIGHT.idAppender()));
        }
        if (roadSegment == null) {
            throw new IllegalArgumentException("Cannot find road:" + roadId);
        }
        return roadSegment;
    }

    private static RoadSegment getRoadSegment(RoadNetwork roadNetwork, String roadId, int lane) {
        return getRoadSegment(roadNetwork, roadId,
                (lane > 0 ? Lanes.LaneSectionType.LEFT : Lanes.LaneSectionType.RIGHT));
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
        } else if (lane.getType().equals(Lanes.Type.RESTRICTED.getOpenDriveIdentifier())) {
            roadSegment.setLaneType(laneNumber, Lanes.Type.RESTRICTED);
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

    /**
     * Iterates through all the roads joining them up according to the links
     * 
     * @param openDriveNetwork
     * @param roadNetwork
     */
    private static void joinRoads(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        Preconditions.checkArgument(roadNetwork.size() > 0, "no roads defined in roadNetwork");
        for (Road road : openDriveNetwork.getRoad()) {
            if (!road.isSetLink()) {
                LOG.info("road=" + road.getId() + " without links to other roads");
                // addDefaultSinks(roadNetwork, road);
                continue;
            }
            joinByLanes(roadNetwork, road);
        }
    }

    private static void joinByLanes(RoadNetwork roadNetwork, Road road) {
        Preconditions.checkArgument(road.isSetLink());
        Preconditions.checkArgument(road.getLanes().getLaneSection().size() == 1,
                "cannot handle more than one laneSection in roadId=" + road.getId());
        LaneSection laneSection = road.getLanes().getLaneSection().get(0);
        if (laneSection.isSetCenter()) {
            LOG.warn("cannot handle center lane");
        }
        // TODO quick hack here, think of better way to formulate this
        if (laneSection.isSetLeft()) {
            joinByLanes(roadNetwork, road, laneSection.getLeft().getLane(),
                    Lanes.LaneSectionType.LEFT.isReverseDirection());
        }
        if (laneSection.isSetRight()) {
            joinByLanes(roadNetwork, road, laneSection.getRight().getLane(),
                    Lanes.LaneSectionType.RIGHT.isReverseDirection());
        }
    }

    private static void joinByLanes(RoadNetwork roadNetwork, Road road, List<Lane> lanes, boolean isReverse) {
        Preconditions.checkArgument(lanes.size() > 0);
        for (Lane lane : lanes) {
            if (!lane.isSetLink()) {
                LOG.debug("no link defined for lane={} on road={} -- handled by junctions.", lane.getId(), road.getId());
                continue;
            }
            if (lane.getLink().isSetPredecessor()) {
                if (!hasRoadPredecessor(road)) {
                    throw new IllegalArgumentException("predecessor lane link but no road link defined for road="
                            + road.getId());
                }
                String sourceId = road.getLink().getPredecessor().getElementId();
                int fromLane = lane.getLink().getPredecessor().getId();
                RoadSegment sourceRoadSegment = getRoadSegment(roadNetwork, sourceId, fromLane);
                int toLane = lane.getId();
                RoadSegment roadSegment = getRoadSegment(roadNetwork, road.getId(), toLane);
                if (isReverse) {
                    Link.addLanePair(laneIdToLaneIndex(toLane), roadSegment, laneIdToLaneIndex(fromLane),
                            sourceRoadSegment);
                } else {
                    Link.addLanePair(laneIdToLaneIndex(fromLane), sourceRoadSegment, laneIdToLaneIndex(toLane),
                            roadSegment);
                }
            }
            if (lane.getLink().isSetSuccessor()) {
                if (!hasRoadSuccessor(road)) {
                    throw new IllegalArgumentException("successor lane link but no road link defined for road="
                            + road.getId());
                }
                int fromLane = lane.getId();
                RoadSegment roadSegment = getRoadSegment(roadNetwork, road.getId(), fromLane);
                int toLane = lane.getLink().getSuccessor().getId();
                String sinkId = road.getLink().getSuccessor().getElementId();
                RoadSegment sinkRoadSegment = getRoadSegment(roadNetwork, sinkId, toLane);
                if (isReverse) {
                    Link.addLanePair(laneIdToLaneIndex(toLane), sinkRoadSegment, laneIdToLaneIndex(fromLane),
                            roadSegment);
                } else {
                    Link.addLanePair(laneIdToLaneIndex(fromLane), roadSegment, laneIdToLaneIndex(toLane),
                            sinkRoadSegment);
                }
            }
        }
    }

    private static boolean hasRoadSuccessor(Road road) {
        return road.getLink().isSetSuccessor()
                && road.getLink().getSuccessor().getElementType().equals(RoadLinkElementType.ROAD.xodrIdentifier());
    }

    private static boolean hasRoadPredecessor(Road road) {
        return road.getLink().isSetPredecessor()
                && road.getLink().getPredecessor().getElementType().equals(RoadLinkElementType.ROAD.xodrIdentifier());
    }

    private static void handleJunctions(OpenDRIVE openDriveNetwork, RoadNetwork roadNetwork) {
        Map<String, Road> roadById = createLookupMap(openDriveNetwork.getRoad());
        for (Junction junction : openDriveNetwork.getJunction()) {
            for (Connection connection : junction.getConnection()) {
                for (LaneLink laneLink : connection.getLaneLink()) {
                    Road road = roadById.get(connection.getConnectingRoad());
                    RoadSegment incomingRoadSegment = getRoadSegment(roadNetwork, connection.getIncomingRoad(),
                            laneLink.getFrom());
                    RoadSegment connectingRoadSegment = getRoadSegment(roadNetwork, connection.getConnectingRoad(),
                            laneLink.getTo());
                    // FIXME bug: connections are not correctly set in all connection cases
                    // example: features/bidirectional/intersection_highway.xodr when the Road=10 is defined reverse
                    final boolean isReverse = laneLink.getTo() > 0 || laneLink.getFrom() > 0;
                    LOG.info("junction={}, road={}", junction.getId(), road.getId());
                    LOG.info("incomingRS={}, connectingRoadSegment={}", incomingRoadSegment.userId(),
                            connectingRoadSegment.userId());
                    LOG.info("lanepair from={} to={}", laneLink.getFrom(), laneLink.getTo());
                    LOG.info("isReverse={}, roadPredecessorIsJunction={}", isReverse,
                            roadPredecessorIsJunction(junction, road));
                    if (roadPredecessorIsJunction(junction, road)) {
                        if (isReverse) {
                            Link.addLanePair(laneIdToLaneIndex(laneLink.getTo()), connectingRoadSegment,
                                    laneIdToLaneIndex(laneLink.getFrom()), incomingRoadSegment);
                        } else {
                            Link.addLanePair(laneIdToLaneIndex(laneLink.getFrom()), incomingRoadSegment,
                                    laneIdToLaneIndex(laneLink.getTo()), connectingRoadSegment);
                        }
                    } else if (roadSuccessorIsJunction(junction, road)) {
                        if (isReverse) {
                            Link.addLanePair(laneIdToLaneIndex(laneLink.getTo()), connectingRoadSegment,
                                    laneIdToLaneIndex(laneLink.getFrom()), incomingRoadSegment);
                        } else {
                            Link.addLanePair(laneIdToLaneIndex(laneLink.getFrom()), connectingRoadSegment,
                                    laneIdToLaneIndex(laneLink.getTo()), incomingRoadSegment);
                        }
                    } else {
                        throw new IllegalArgumentException("Incorrect junction id=" + junction.getId());
                    }
                }
            }
        }
    }

    private static void checkIfAllLanesAreConnected(RoadNetwork roadNetwork) {
        boolean valid = true;
        for (RoadSegment roadSegment : roadNetwork) {
            if (roadSegment.hasSink()) {
                continue;
            }
            for (LaneSegment laneSegment : roadSegment.laneSegments()) {
                if (laneSegment.sinkLaneSegment() == null
                        && (laneSegment.type() != Lanes.Type.ENTRANCE && laneSegment.type() != Lanes.Type.RESTRICTED)) {
                    LOG.error("no sinklane for lane={} on RoadSegment={}", laneSegment.lane(), laneSegment
                            .roadSegment().userId());
                    valid = false;
                }
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("network file defines unconnected lanes. See error log messages above.");
        }
    }

    private static Map<String, Road> createLookupMap(Collection<Road> roads) {
        Map<String, Road> idToRoad = new HashMap<>();
        for (Road road : roads) {
            Road other = idToRoad.put(road.getId(), road);
            if (other != null) {
                throw new IllegalArgumentException("road with ID=" + road.getId() + " not unique in xodr!");
            }
        }
        return idToRoad;
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

    /**
     * Iterates finally through all the road segments assigning a default sink to
     * any road segment with no sink connections
     * 
     * @param roadNetwork
     */
    private static void addDefaultSinksToUnconnectedRoad(RoadNetwork roadNetwork) {
        int countSinks = 0;
        for (RoadSegment roadSegment : roadNetwork) {
            if (!roadSegment.hasDownstreamConnection()) {
                countSinks++;
                // roadSegment.setSink(new TrafficSink(roadSegment));
                roadSegment.addDefaultSink();
                LOG.info("added default sink to roadSegment=" + roadSegment.userId());
            }
        }
        LOG.info("added {} default sinks to unconnected roads.", countSinks);
    }

    /**
     * Returns the lane used in {@link RoadSegment}s (positive integer) from the xodr convention (using laneId>0 and laneId<0 for left and
     * right driving directions.
     * 
     * @param xodrLaneId
     * @return lane defined as positive integer.
     */
    private static int laneIdToLaneIndex(int xodrLaneId) {
        return Math.abs(xodrLaneId);
    }

}
