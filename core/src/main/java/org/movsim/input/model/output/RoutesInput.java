// package org.movsim.input.model.output;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
//
// import org.jdom.Element;
// import org.movsim.input.XmlElementNames;
// import org.movsim.input.model.RoadInput;
//
// public class RoutesInput {
//
// private ArrayList<RouteInput> routes;
//
// public RoutesInput(Map<String, RoadInput> roadInputMap, Element elem) {
// routes = new ArrayList<RouteInput>();
//
// @SuppressWarnings("unchecked")
// final List<Element> routeElems = elem.getChildren(XmlElementNames.OutputRoute);
// if (routeElems != null) {
// for (final Element routeElem : routeElems) {
// RouteInput route = new RouteInput();
// route.setName(routeElem.getAttributeValue("label"));
// @SuppressWarnings("unchecked")
// List<Element> roads = routeElem.getChildren(XmlElementNames.OutputRoadId);
// for (Element road : roads) {
// String roadId = road.getAttributeValue("id");
// route.add(roadId);
// }
// routes.add(route);
// }
// }
//
// }
//
// public List<RouteInput> getRoutes() {
// return routes;
// }
// }
