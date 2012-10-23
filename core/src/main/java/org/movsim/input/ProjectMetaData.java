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
package org.movsim.input;

import java.io.InputStream;

public class ProjectMetaData {

    private static final String MOVSIM_DTD_FILENAME = "multiModelTrafficSimulatorInput.dtd";
    private static final String MOVSIM_DTD_PATH = "/config/";
    final String dtdFilename = "/config/multiModelTrafficSimulatorInput.dtd";
    
    private static ProjectMetaData singleton = new ProjectMetaData();

    private String projectName;
    private String pathToProjectXmlFile;
    private String outputPath;
    private String xodrNetworkFilename;
    private String xodrPath;
    private boolean instantaneousFileOutput = true;
    private boolean onlyValidation = false;
    private boolean writeInternalXml = false;

    /**
     * Needed for Applet. Change to true, if you cannot access the file system.
     * Allows to read the config files from resources instead.
     */
    private boolean xmlFromResources = false;
    
    /**
     * For Android client: xmlFromResources does work, but using the file system is more convenient.
     */
    private boolean parseFromInputstream = false;

    private InputStream movsimXml;
    private InputStream networkXml;
    private InputStream projectProperties;

    /**
     * private constructor: singleton pattern.
     */
    private ProjectMetaData() {
    }

    /**
     * Gets the single instance of ProjectMetaData.
     * 
     * @return single instance of ProjectMetaData
     */
    public static ProjectMetaData getInstance() {
        return singleton;
    }

    public boolean hasProjectName() {
        return projectName != null && !projectName.isEmpty();
    }

    public String getProjectName() {
        if (!hasProjectName()) {
            throw new IllegalStateException("project name not set. Check in advance using \"hasProjectName()\"");
        }
        return projectName;
    }

    /**
     * Sets the project name.
     * 
     * @param projectName
     *            the new project name
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean hasPathToProjectXmlFile() {
        return pathToProjectXmlFile != null && !pathToProjectXmlFile.isEmpty();
    }

    public String getPathToProjectXmlFile() {
        if (!hasPathToProjectXmlFile()) {
            throw new IllegalStateException(
                    "path to project file not set. Check in advance using \"hasPathToProjectXmlFile()\"");
        }
        return pathToProjectXmlFile;
    }

    /**
     * Sets the path to project xml file.
     * 
     * @param pathToProjectXmlFile
     *            the new path to project xml file
     */
    public void setPathToProjectXmlFile(String pathToProjectXmlFile) {
        this.pathToProjectXmlFile = pathToProjectXmlFile;
    }

    public boolean hasOutputPath() {
        return outputPath != null && !outputPath.isEmpty();
    }

    public String getOutputPath() {
        if (!hasOutputPath()) {
            throw new IllegalStateException("output path not set. Check in advance using \"hasOutputPath()\"");
        }
        return outputPath;
    }

    /**
     * Sets the output path.
     * 
     * @param outputPath
     *            the new output path
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public boolean hasNetworkFilename() {
        return xodrNetworkFilename != null && !xodrNetworkFilename.isEmpty();
    }

    public String getXodrNetworkFilename() {
        if (!hasNetworkFilename()) {
            throw new IllegalStateException(
                    "network filename not yet set. Check in advance using \"hasNetworkFilename()\"");
        }
        return xodrNetworkFilename;
    }

    public void setXodrNetworkFilename(String xodrFilename) {
        this.xodrNetworkFilename = xodrFilename;
    }

    public void setXodrPath(String xodrPath) {
        this.xodrPath = xodrPath;
    }

    public String getXodrPath() {
        return xodrPath;
    }

    public boolean isInstantaneousFileOutput() {
        return instantaneousFileOutput;
    }

    /**
     * Sets the instantaneous file output.
     * 
     * @param instantaneousFileOutput
     *            the new instantaneous file output
     */
    public void setInstantaneousFileOutput(boolean instantaneousFileOutput) {
        this.instantaneousFileOutput = instantaneousFileOutput;
    }

    /**
     * Commandline option 'only validation' of input xml file against dtd.
     * 
     * @return
     */
    public boolean isOnlyValidation() {
        return onlyValidation;
    }

    /**
     * Sets the only validation. Commandline option 'only validation' of input xml file against dtd.
     * 
     * @param onlyValidation
     *            the new only validation
     */
    public void setOnlyValidation(boolean onlyValidation) {
        this.onlyValidation = onlyValidation;
    }

    public boolean isWriteInternalXml() {
        return writeInternalXml;
    }

    /**
     * Sets the write internal xml.
     * 
     * @param writeInternalXml
     *            the new write internal xml
     */
    public void setWriteInternalXml(boolean writeInternalXml) {
        this.writeInternalXml = writeInternalXml;
    }

    public boolean isXmlFromResources() {
        return xmlFromResources;
    }

    /**
     * Sets the xml from resources. Xml config files are read from resources.
     * 
     * @param xmlFromResources
     *            the new xml from resources
     */
    public void setXmlFromResources(boolean xmlFromResources) {
        this.xmlFromResources = xmlFromResources;
    }
    
    /**
     * @return the parseFromInputstream
     */
    public boolean isParseFromInputstream() {
        return parseFromInputstream;
    }

    /**
     * @param parseFromInputstream the parseFromInputstream to set
     */
    public void setParseFromInputstream(boolean parseFromInputstream) {
        this.parseFromInputstream = parseFromInputstream;
    }
    /**
     * @return the movsimXml
     */
    public InputStream getMovsimXml() {
        return movsimXml;
    }

    /**
     * @param movsimXml the movsimXml to set
     */
    public void setMovsimXml(InputStream movsimXml) {
        this.movsimXml = movsimXml;
    }

    /**
     * @return the networkXml
     */
    public InputStream getNetworkXml() {
        return networkXml;
    }

    /**
     * @param networkXml the networkXml to set
     */
    public void setNetworkXml(InputStream networkXml) {
        this.networkXml = networkXml;
    }

    /**
     * @return the projectProperties
     */
    public InputStream getProjectProperties() {
        return projectProperties;
    }

    /**
     * @param projectProperties the projectProperties to set
     */
    public void setProjectProperties(InputStream projectProperties) {
        this.projectProperties = projectProperties;
    }

    public String getDtdFilenameWithPath() {
        return MOVSIM_DTD_PATH + MOVSIM_DTD_FILENAME;
    }

    public String getDtdPath() {
        return MOVSIM_DTD_PATH;
    }

    public String getDdtFilename() {
        return MOVSIM_DTD_FILENAME;
    }

    
}
