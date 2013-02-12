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
// package org.movsim.consumption.input.xml.model;
//
// import java.util.List;
// import java.util.Map;
//
// import org.jdom.Element;
// import org.movsim.consumption.model.ConsumptionConstants;
// import org.movsim.utilities.XmlUtils;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// public class ConsumptionEngineModelInput {
//
// /** The Constant logger. */
// final static Logger logger = LoggerFactory.getLogger(ConsumptionEngineModelInput.class);
//
// /** in kW */
// private final double maxPower;
// /** in liter */
// private final double cylinderVolume;
// /** in liter per second */
// private final double idleConsumptionRateLiterPerSecond;
// /** in kg/Ws */
// private final double minSpecificConsumption;
// /** in Pascal */
// private final double effectivePressureMinimum;
// /** in Pascal */
// private final double effectivePressureMaximum;
//
// private List<Double> gearRatios;
//
// @SuppressWarnings("unchecked")
// public ConsumptionEngineModelInput(Element elem) {
//
// final Map<String, String> engineDataMap = XmlUtils.putAttributesInHash(elem);
// this.maxPower = 1000 * Double.parseDouble(engineDataMap.get("max_power_kW"));
// this.cylinderVolume = 0.001 * Double.parseDouble(engineDataMap.get("cylinder_vol_l")); // in liter
// this.idleConsumptionRateLiterPerSecond = Double.parseDouble(engineDataMap.get("idle_cons_rate_linvh")) / 3600.;
// this.minSpecificConsumption = Double.parseDouble(engineDataMap.get("cspec_min_g_per_kwh")) / 3.6e9;
// this.effectivePressureMinimum = ConsumptionConstants.CONVERSION_BAR_TO_PASCAL
// * Double.parseDouble(engineDataMap.get("pe_min_bar"));
// this.effectivePressureMaximum = ConsumptionConstants.CONVERSION_BAR_TO_PASCAL
// * Double.parseDouble(engineDataMap.get("pe_max_bar"));
//
// }
//
// public double getMaxPower() {
// return maxPower;
// }
//
// public double getCylinderVolume() {
// return cylinderVolume;
// }
//
// public double getIdleConsumptionRateLiterPerSecond() {
// return idleConsumptionRateLiterPerSecond;
// }
//
// public double getEffectivePressureMinimum() {
// return effectivePressureMinimum;
// }
//
// public double getEffectivePressureMaximum() {
// return effectivePressureMaximum;
// }
//
// public double getMinSpecificConsumption() {
// return minSpecificConsumption;
// }
// }
