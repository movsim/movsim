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

package org.movsim.input.file.opendrive;

import java.util.ArrayList;

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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// obstacle in movsim: /**
// Sets the obstacle at end of lane.
//
// private void setObstacleAtEndOfLane() {
// final Vehicle obstacle = vehGenerator.createVehicle(MovsimConstants.OBSTACLE_KEY_NAME);
// final double posInit = roadLength;
// final double speedInit = 0;
// vehContainers.get(0).add(obstacle, posInit, speedInit);
// logger.debug("set obstacle at pos={} with length={}", posInit, obstacle.getLength());
// }

/**
 * SAX XML handler for OpenDRIVE format file, see: http://www.opendrive.org/docs/OpenDRIVEFormatSpecRev1.3D.pdf
 */
@SuppressWarnings("nls")
public class OpenDriveHandler extends DefaultHandler {
    private final ArrayList<String> stack = new ArrayList<String>();
    private int elementIndex = 0;
    private final int limit = 100000;
    private String inElement;

    private final RoadNetwork roadNetwork;

    static class Road {
        String id;
        String name;
        String junction;
        double length;

        static class Link {
            String predecessorId;
            String predecessorType;
            String predecessorContact;
            String successorId;
            String successorType;
            String successorContact;
        }

        Link link;

        static class PlanView {
            static class Geometry {
                double s;
                double x;
                double y;
                double hdg; // heading
                double length;

                static enum Type {
                    LINE, SPIRAL, ARC, POLY3
                }

                Type type;
                double curvature; // for arcs
                double curvStart; // for spirals
                double curvEnd; // for spirals
                double a; // for cubic polynomial
                double b;
                double c;
                double d;
            }

            ArrayList<Geometry> geometries = new ArrayList<Geometry>();
        }

        PlanView planView;

        static class Lanes {
            static class Lane {
                static class Link {
                    int predecessorId;
                    int successorId;
                }

                Link link;
                int id;
                String type;
                String level;
                double width;
            }

            static class LaneSection {
                static enum LaneType {
                    NONE, LEFT, CENTER, RIGHT
                }

                LaneType laneType = LaneType.NONE;
                ArrayList<Lane> left = new ArrayList<Lane>();
                Lane center;
                ArrayList<Lane> right = new ArrayList<Lane>();
            }

            LaneSection laneSection;
        }

        Lanes lanes;

        static class Objects {
            static class Tunnel {
                double s;
                double length;
                String name;
                String id;
                String type;
                double lighting;
                double daylight;
            }

            ArrayList<Tunnel> tunnels = new ArrayList<Tunnel>();
        }

        Objects objects;
    }

    static class Junction {
        String id;
        String name;

        static class Connection {
            int id;
            String incomingRoad;
            String connectingRoad;
            String contactPoint;

            static class LaneLink {
                int from;
                int to;
            }

            ArrayList<LaneLink> laneLinks = new ArrayList<LaneLink>();
        }

        ArrayList<Connection> connections = new ArrayList<Connection>();
    }

    private Road road;
    private Road.Lanes.Lane lane;
    private Road.PlanView.Geometry geometry;
    private ArrayList<Road> roads;

    private Road findByUserId(ArrayList<Road> roads, String id) {
        for (final Road road : roads) {
            if (road.id.equals(id)) {
                return road;
            }
        }
        return null;
    }

    private Junction junction;
    private Junction.Connection connection;
    private Junction.Connection.LaneLink laneLink;
    private ArrayList<Junction> junctions;
    private Road.Objects.Tunnel tunnel;

    OpenDriveHandler(RoadNetwork roadNetwork) {
        this.roadNetwork = roadNetwork;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        System.out.println("startElement: " + qName);
        ++elementIndex;
        if (elementIndex > limit && limit > 0) {
            throw new SAXException("Reached limit count"); // stop parsing
        }
        inElement = qName.toLowerCase();
        stack.add(inElement);
        if (inElement.equals("opendrive")) {
            roads = new ArrayList<Road>();
            junctions = new ArrayList<Junction>();
        } else if (inElement.equals("road")) {
            road = new Road();
            road.name = attributes.getValue("name");
            road.id = attributes.getValue("id");
            road.junction = attributes.getValue("junction");
            road.length = getDouble(attributes, "length");
        } else if (inElement.equals("objects")) {
            road.objects = new Road.Objects();
        } else if (inElement.equals("tunnel")) {
            tunnel = new Road.Objects.Tunnel();
            tunnel.name = attributes.getValue("name");
            tunnel.id = attributes.getValue("id");
            tunnel.type = attributes.getValue("type");
            tunnel.s = getDouble(attributes, "s");
            tunnel.length = getDouble(attributes, "length");
            tunnel.lighting = getDouble(attributes, "lighting");
            tunnel.daylight = getDouble(attributes, "daylight");
        } else if (inElement.equals("link")) {
            assert stack.get(stack.size() - 1).equals("link");
            final String owner = stack.get(stack.size() - 2);
            if (owner.equals("road")) {
                road.link = new Road.Link();
            } else if (owner.equals("lane")) {
                lane.link = new Road.Lanes.Lane.Link();
            }
        } else if (inElement.equals("planview")) {
            road.planView = new Road.PlanView();
        } else if (inElement.equals("geometry")) {
            geometry = new Road.PlanView.Geometry();
            // <geometry s=0.0" x="0.0" y="1.0" hdg="0.0" length="8.0">
            geometry.s = getDouble(attributes, "s");
            geometry.x = getDouble(attributes, "x");
            geometry.y = getDouble(attributes, "y");
            geometry.hdg = getDouble(attributes, "hdg");
            geometry.length = getDouble(attributes, "length");
        } else if (inElement.equals("line")) {
            geometry.type = Road.PlanView.Geometry.Type.LINE;
        } else if (inElement.equals("spiral")) {
            geometry.type = Road.PlanView.Geometry.Type.SPIRAL;
            geometry.curvStart = getDouble(attributes, "curvstart");
            geometry.curvEnd = getDouble(attributes, "curvend");
        } else if (inElement.equals("arc")) {
            geometry.type = Road.PlanView.Geometry.Type.ARC;
            geometry.curvature = getDouble(attributes, "curvature");
        } else if (inElement.equals("poly3")) {
            geometry.type = Road.PlanView.Geometry.Type.POLY3;
            geometry.a = getDouble(attributes, "a");
            geometry.b = getDouble(attributes, "b");
            geometry.c = getDouble(attributes, "c");
            geometry.d = getDouble(attributes, "d");
        } else if (inElement.equals("lanes")) {
            road.lanes = new Road.Lanes();
        } else if (inElement.equals("lanesection")) {
            road.lanes.laneSection = new Road.Lanes.LaneSection();
        } else if (inElement.equals("left")) {
            road.lanes.laneSection.laneType = Road.Lanes.LaneSection.LaneType.LEFT;
        } else if (inElement.equals("center")) {
            road.lanes.laneSection.laneType = Road.Lanes.LaneSection.LaneType.CENTER;
        } else if (inElement.equals("right")) {
            road.lanes.laneSection.laneType = Road.Lanes.LaneSection.LaneType.RIGHT;
        } else if (inElement.equals("lane")) {
            lane = new Road.Lanes.Lane();
            lane.id = getInt(attributes, "id");
            lane.type = attributes.getValue("type");
            lane.level = attributes.getValue("level");
        } else if (inElement.equals("successor")) {
            assert stack.get(stack.size() - 1).equals("successor");
            assert stack.get(stack.size() - 2).equals("link");
            final String owner = stack.get(stack.size() - 3);
            if (owner.equals("road")) {
                road.link.successorType = attributes.getValue("elementType");
                road.link.successorId = attributes.getValue("elementId");
                road.link.successorContact = attributes.getValue("contactPoint");
            } else if (owner.equals("lane")) {
                lane.link.successorId = getInt(attributes, "id");
            } else {
                assert false;
            }
        } else if (inElement.equals("predecessor")) {
            assert stack.get(stack.size() - 1).equals("predecessor");
            assert stack.get(stack.size() - 2).equals("link");
            final String owner = stack.get(stack.size() - 3);
            if (owner.equals("road")) {
                road.link.predecessorType = attributes.getValue("elementType");
                road.link.predecessorId = attributes.getValue("elementId");
                road.link.predecessorContact = attributes.getValue("contactPoint");
            } else if (owner.equals("lane")) {
                lane.link.predecessorId = getInt(attributes, "id");
            } else {
                assert false;
            }
        } else if (inElement.equals("width")) {
            lane.width = getDouble(attributes, "a");
        } else if (inElement.equals("junction")) {
            junction = new Junction();
            junction.name = attributes.getValue("name");
            junction.id = attributes.getValue("id");
        } else if (inElement.equals("connection")) {
            connection = new Junction.Connection();
            connection.id = getInt(attributes, "id");
            connection.incomingRoad = attributes.getValue("incomingRoad");
            connection.connectingRoad = attributes.getValue("connectingRoad");
            connection.contactPoint = attributes.getValue("contactPoint");
        } else if (inElement.equals("lanelink")) {
            laneLink = new Junction.Connection.LaneLink();
            laneLink.from = getInt(attributes, "from");
            laneLink.to = getInt(attributes, "to");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        System.out.println("endElement: " + qName);
        stack.remove(stack.size() - 1); // pop element
        final String element = qName.toLowerCase();
        if (element.equals("road")) {
            // create a RoadSegment from the parsed road element and add it to the road network
            final int laneCount = road.lanes.laneSection.right.size() + road.lanes.laneSection.left.size();
            final RoadMapping roadMapping;
            if (road.planView.geometries.size() == 1) {
                final Road.PlanView.Geometry geometry = road.planView.geometries.get(0);
                switch (geometry.type) {
                case LINE:
                    roadMapping = new RoadMappingLine(laneCount, geometry.s, geometry.x, geometry.y, geometry.hdg,
                            geometry.length);
                    break;
                case ARC:
                    roadMapping = new RoadMappingArc(laneCount, geometry.s, geometry.x, geometry.y, geometry.hdg,
                            geometry.length, geometry.curvature);
                    break;
                case POLY3:
                    throw new SAXException("POLY3 geometry not yet supported (in road: " + road.name + " )");
                case SPIRAL:
                    throw new SAXException("SPIRAL geometry not yet supported (in road: " + road.name + " )");
                default:
                    throw new SAXException("Unknown geometry for road: " + road.name);
                }
            } else {
                roadMapping = new RoadMappingPoly(laneCount);
                final RoadMappingPoly roadMappingPoly = (RoadMappingPoly) roadMapping;
                for (int i = 0; i < road.planView.geometries.size(); ++i) {
                    final Road.PlanView.Geometry geometry = road.planView.geometries.get(i);
                    switch (geometry.type) {
                    case LINE:
                        roadMappingPoly.addLine(geometry.s, geometry.x, geometry.y, geometry.hdg, geometry.length);
                        break;
                    case ARC:
                        roadMappingPoly.addArc(geometry.s, geometry.x, geometry.y, geometry.hdg, geometry.length,
                                geometry.curvature);
                        break;
                    case POLY3:
                        throw new SAXException("POLY3 geometry not yet supported (in road: " + road.name + " )");
                        // roadMappingPoly.addPoly3(geometry.s, geometry.x, geometry.y, geometry.hdg, geometry.length,
                        // geometry.a, geometry.b, geometry.c, geometry.d);
                    case SPIRAL:
                        throw new SAXException("SPIRAL geometry not yet supported (in road: " + road.name + " )");
                    default:
                        throw new SAXException("Unknown geometry for road: " + road.name);
                    }
                }
            }
            final RoadSegment roadSegment = new RoadSegment(roadMapping);
            roadSegment.setUserId(road.id);
            for (final Road.Lanes.Lane lane : road.lanes.laneSection.left) {
                final int laneIndex = leftLaneIdToLaneIndex(roadSegment, lane.id);
                if (lane.type.equals("driving")) {
                    roadSegment.setLaneType(laneIndex, Lane.Type.TRAFFIC);
                } else if (lane.type.equals("mwyEntry")) {
                    roadSegment.setLaneType(laneIndex, Lane.Type.ENTRANCE);
                    roadSegment.addObstacle(new Vehicle(roadSegment.roadLength(), 0.0, laneIndex, 1.0, 1.0));
                } else if (lane.type.equals("mwyExit")) {
                    roadSegment.setLaneType(laneIndex, Lane.Type.EXIT);
                } else if (lane.type.equals("shoulder")) {
                    roadSegment.setLaneType(laneIndex, org.movsim.simulator.roadnetwork.Lane.Type.SHOULDER);
                }
            }
            for (final Road.Lanes.Lane lane : road.lanes.laneSection.right) {
                final int laneIndex = rightLaneIdToLaneIndex(roadSegment, lane.id);
                if (lane.type.equals("driving")) {
                    roadSegment.setLaneType(laneIndex, org.movsim.simulator.roadnetwork.Lane.Type.TRAFFIC);
                } else if (lane.type.equals("mwyEntry")) {
                    roadSegment.setLaneType(laneIndex, org.movsim.simulator.roadnetwork.Lane.Type.ENTRANCE);
                    roadSegment.addObstacle(new Vehicle(roadSegment.roadLength(), 0.0, laneIndex, 1.0, 1.0));
                } else if (lane.type.equals("mwyExit")) {
                    roadSegment.setLaneType(laneIndex, org.movsim.simulator.roadnetwork.Lane.Type.EXIT);
                } else if (lane.type.equals("shoulder")) {
                    roadSegment.setLaneType(laneIndex, org.movsim.simulator.roadnetwork.Lane.Type.SHOULDER);
                }
            }
            if (road.objects != null) {
                for (final Road.Objects.Tunnel tunnel : road.objects.tunnels) {
                    roadMapping.addClippingRegion(tunnel.s, tunnel.length);
                }
            }
            roadNetwork.add(roadSegment);
            roads.add(road);
            road = null;
        } else if (element.equals("geometry")) {
            // add the geometry to the planView
            road.planView.geometries.add(geometry);
            geometry = null;
        } else if (element.equals("lane")) {
            // add the geometry to the planView
            assert lane != null;
            if (road.lanes.laneSection.laneType == Road.Lanes.LaneSection.LaneType.LEFT) {
                road.lanes.laneSection.left.add(lane);
            } else if (road.lanes.laneSection.laneType == Road.Lanes.LaneSection.LaneType.RIGHT) {
                road.lanes.laneSection.right.add(lane);
            }
            lane = null;
        } else if (element.equals("junction")) {
            junctions.add(junction);
            junction = null;
        } else if (element.equals("tunnel")) {
            assert tunnel != null;
            road.objects.tunnels.add(tunnel);
            tunnel = null;
        } else if (element.equals("connection")) {
            // add the geometry to the planView
            junction.connections.add(connection);
            connection = null;
        } else if (element.equals("lanelink")) {
            // add the geometry to the planView
            connection.laneLinks.add(laneLink);
            laneLink = null;
        } else if (element.equals("opendrive")) {
            // iterate through all the roads joining them up according to the links
            for (final Road road : roads) {
                final RoadSegment roadSegment = roadNetwork.findByUserId(road.id);
                assert roadSegment != null;
                if (road.link == null) {
                    roadSegment.addDefaultSink();
                } else {
                    if (road.link.predecessorType != null && road.link.predecessorType.equals("road")) {
                        final RoadSegment sourceRoadSegment = roadNetwork.findByUserId(road.link.predecessorId);
                        if (sourceRoadSegment == null) {
                            throw new SAXException("Cannot find predecessor link:" + road.link.predecessorId); // stop
                                                                                                               // parsing
                        }
                        for (final Road.Lanes.Lane lane : road.lanes.laneSection.left) {
                            if (lane.link != null) {
                                final int fromLane = laneIdToLaneIndex(sourceRoadSegment, lane.link.predecessorId);
                                final int toLane = laneIdToLaneIndex(roadSegment, lane.id);
                                Link.addLanePair(fromLane, sourceRoadSegment, toLane, roadSegment);
                            }
                        }
                        for (final Road.Lanes.Lane lane : road.lanes.laneSection.right) {
                            if (lane.link != null) {
                                final int fromLane = laneIdToLaneIndex(sourceRoadSegment, lane.link.predecessorId);
                                final int toLane = laneIdToLaneIndex(roadSegment, lane.id);
                                Link.addLanePair(fromLane, sourceRoadSegment, toLane, roadSegment);
                            }
                        }
                    }
                    if (road.link.successorType != null && road.link.successorType.equals("road")) {
                        final RoadSegment sinkRoadSegment = roadNetwork.findByUserId(road.link.successorId);
                        for (final Road.Lanes.Lane lane : road.lanes.laneSection.left) {
                            if (lane.link != null) {
                                final int fromLane = laneIdToLaneIndex(roadSegment, lane.id);
                                final int toLane = laneIdToLaneIndex(sinkRoadSegment, lane.link.successorId);
                                Link.addLanePair(fromLane, roadSegment, toLane, sinkRoadSegment);
                            }
                        }
                        for (final Road.Lanes.Lane lane : road.lanes.laneSection.right) {
                            if (lane.link != null) {
                                final int fromLane = laneIdToLaneIndex(roadSegment, lane.id);
                                final int toLane = laneIdToLaneIndex(sinkRoadSegment, lane.link.successorId);
                                Link.addLanePair(fromLane, roadSegment, toLane, sinkRoadSegment);
                            }
                        }
                    }
                }
            }
            // iterate through all the junctions
            for (final Junction junction : junctions) {
                for (final Junction.Connection connection : junction.connections) {
                    // System.out.println("Junction id:" + junction.id);
                    final RoadSegment incomingRoadSegment = roadNetwork.findByUserId(connection.incomingRoad);
                    if (incomingRoadSegment == null) {
                        throw new SAXException("Cannot find incoming road: " + connection.incomingRoad);
                    }
                    final RoadSegment connenctingRoadSegment = roadNetwork.findByUserId(connection.connectingRoad);
                    if (connenctingRoadSegment == null) {
                        throw new SAXException("Cannot find connecting road: " + connection.connectingRoad);
                    }
                    final Road road = findByUserId(roads, connection.connectingRoad);
                    if (road.link.predecessorType.equals("junction") && road.link.predecessorId.equals(junction.id)) {
                        for (final Junction.Connection.LaneLink laneLink : connection.laneLinks) {
                            final int fromLane = laneIdToLaneIndex(incomingRoadSegment, laneLink.from);
                            final int toLane = laneIdToLaneIndex(connenctingRoadSegment, laneLink.to);
                            // System.out.println("lanepair from:" + laneLink.from + ",to:" + laneLink.to);
                            Link.addLanePair(fromLane, incomingRoadSegment, toLane, connenctingRoadSegment);
                        }
                    } else if (road.link.successorType.equals("junction") && road.link.successorId.equals(junction.id)) {
                        for (final Junction.Connection.LaneLink laneLink : connection.laneLinks) {
                            final int fromLane = laneIdToLaneIndex(connenctingRoadSegment, laneLink.from);
                            final int toLane = laneIdToLaneIndex(incomingRoadSegment, laneLink.to);
                            // System.out.println("lanepair from:" + laneLink.from + ",to:" + laneLink.to);
                            Link.addLanePair(fromLane, connenctingRoadSegment, toLane, incomingRoadSegment);
                        }
                    } else {
                        throw new SAXException("Incorrect junction:" + junction.id); // stop parsing
                    }
                }
            }
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

    // @Override
    // public void characters(char ch[], int start, int length) throws SAXException {
    // final String string = new String(ch, start, length);
    // System.out.println("characters:" + string);
    // }

    private double getDouble(Attributes attributes, String qName) throws SAXException {
        final String value = attributes.getValue(qName);
        if (value == null) {
            throw new SAXException("No value for attribute " + qName);
        }
        return Double.parseDouble(value);
    }

    // private double getDouble(Attributes attributes, String qName, double defaultValue) {
    // final String value = attributes.getValue(qName);
    // return value == null ? defaultValue : Double.parseDouble(value);
    // }

    private int getInt(Attributes attributes, String qName) throws SAXException {
        final String value = attributes.getValue(qName);
        if (value == null) {
            throw new SAXException("No value for attribute " + qName);
        }
        return Integer.parseInt(value);
    }

    // private int getInt(Attributes attributes, String qName, int defaultValue) {
    // final String value = attributes.getValue(qName);
    // return value == null ? defaultValue : Integer.parseInt(value);
    // }

    // private boolean getBoolean(Attributes attributes, String qName, boolean defaultValue) throws SAXException {
    // final String value = attributes.getValue(qName);
    // if (value == null) {
    // return defaultValue;
    // } else if (value.equals("true")) {
    // return true;
    // } else if (value.equals("false")) {
    // return false;
    // } else {
    // throw new SAXException("Invalid boolean value for attribute " + qName);
    // }
    // }

    static int rightLaneIdToLaneIndex(RoadSegment roadSegment, int rightLaneId) {
        assert rightLaneId < 0;
        final int lane = roadSegment.laneCount() + rightLaneId;
        assert lane >= org.movsim.simulator.roadnetwork.Lane.LANE1;
        return lane;
    }

    static int leftLaneIdToLaneIndex(RoadSegment roadSegment, int leftLaneId) {
        assert leftLaneId >= 0;
        final int lane = leftLaneId;
        assert lane >= org.movsim.simulator.roadnetwork.Lane.LANE1;
        return lane;
    }

    static int laneIdToLaneIndex(RoadSegment roadSegment, int laneId) {
        if (laneId >= 0) {
            return laneId;
        }
        return roadSegment.laneCount() + laneId;
    }
}
