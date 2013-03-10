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
// package org.movsim.consumption.input;
//
// import java.io.File;
//
//
// public class ConsumptionMetadata {
//
// private static final String CONFIG_FILE_ENDING = ".xml";
//
// private String outputPath;
//
// private String projectName;
//
// private String pathToConsumptionFile;
//
// private String consumptionPath;
//
// private boolean onlyValidation = false;
//
// private boolean writeInternalXml = false;
//
//
// private static ConsumptionMetadata singleton = new ConsumptionMetadata();
//
// public static ConsumptionMetadata getInstance() {
// return singleton;
// }
//
// private ConsumptionMetadata() {
// }
//
// public String getDtdFilenameWithPath() {
// return File.separator + getDtdPath() + File.separator + getDtdFilename();
// //getDtdPath()+getDtdFilename();
// }
//
// public static String getConfigFileEnding() {
// return CONFIG_FILE_ENDING;
// }
//
//
// public String getOutputPath() {
// return outputPath;
// }
//
//
// public void setOutputPath(String outputPath) {
// this.outputPath = outputPath;
// }
//
//
// public String getConsumptionFilename() {
// return projectName+ CONFIG_FILE_ENDING;
// }
//
//
// public String getConsumptionPath() {
// return consumptionPath;
// }
//
//
// public void setConsumptionPath(String consumptionPath) {
// this.consumptionPath = consumptionPath;
// }
//
//
// public boolean isOnlyValidation() {
// return onlyValidation;
// }
//
//
// public void setOnlyValidation(boolean onlyValidation) {
// this.onlyValidation = onlyValidation;
// }
//
//
// public boolean isWriteInternalXml() {
// return writeInternalXml;
// }
//
//
// public void setWriteInternalXml(boolean writeInternalXml) {
// this.writeInternalXml = writeInternalXml;
// }
//
//
// public String getPathToConsumptionFile() {
// return pathToConsumptionFile;
// }
//
//
// public void setPathToConsumptionFile(String pathToConsumptionFile) {
// this.pathToConsumptionFile = pathToConsumptionFile;
// }
//
// public String getProjectName() {
// return projectName;
// }
//
// public void setProjectName(String projectName) {
// this.projectName = projectName;
// }
//
// public boolean isParseFromInputstream() {
// // TODO Auto-generated method stub
// return false;
// }
//
// public boolean isXmlFromResources() {
// // TODO Auto-generated method stub
// return false;
// }
//
// public File getXmlInputFile() {
// return new File(getPathToConsumptionFile(), getProjectName() + CONFIG_FILE_ENDING);
// }
//
// }
