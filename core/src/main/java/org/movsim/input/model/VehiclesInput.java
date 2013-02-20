// /*
// * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
// * <movsim.org@gmail.com>
// * -----------------------------------------------------------------------------------------
// *
// * This file is part of
// *
// * MovSim - the multi-model open-source vehicular-traffic simulator.
// *
// * MovSim is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * MovSim is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// * See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with MovSim. If not, see <http://www.gnu.org/licenses/>
// * or <http://www.movsim.org>.
// *
// * -----------------------------------------------------------------------------------------
// */
//
// package org.movsim.input.model;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// import org.jdom.Element;
// import org.movsim.input.model.vehicle.VehicleInput;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// public class VehiclesInput {
//
// /** The Constant logger. */
// final static Logger logger = LoggerFactory.getLogger(VehiclesInput.class);
//
// private final Map<String, VehicleInput> vehicleInputMap = new HashMap<String, VehicleInput>();
//
// private final boolean isWriteFundamentalDiagrams;
//
// public VehiclesInput(Element elem){
// this.isWriteFundamentalDiagrams = elem.getAttributeValue("write_fund_diagrams").equals("true") ? true
// : false;
//
// @SuppressWarnings("unchecked")
// final List<Element> vehicleElements = elem.getChildren();
// for (final Element vehElem : vehicleElements) {
// final VehicleInput vehicleInput = new VehicleInput(vehElem);
// final String labelName = vehicleInput.getLabel();
// if (vehicleInputMap.containsKey(labelName)) {
// logger.error(
// "vehicle label={} already exists. Please check input file for duplicate label identifier.",
// labelName);
// System.exit(-1);
// }
// vehicleInputMap.put(labelName, vehicleInput);
// }
// }
//
// public Map<String, VehicleInput> getVehicleInputMap() {
// return vehicleInputMap;
// }
//
// public boolean isWriteFundamentalDiagrams() {
// return isWriteFundamentalDiagrams;
// }
//
// }
