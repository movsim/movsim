/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.input.impl;

import org.movsim.input.ProjectMetaData;
/**
 * The Class ProjectMetaDataImpl.
 */
public class ProjectMetaDataImpl implements ProjectMetaData {

    private static ProjectMetaDataImpl singleton = new ProjectMetaDataImpl();

    private String projectName = ""; //"/file/src/test/resources/" + "onramp_IDM" + ".xml";
    private String pathToProjectXmlFile;
    private String outputPath;
    private boolean instantaneousFileOutput = true;
    private boolean onlyValidation = false;
    private boolean writeInternalXml = false;

    /** Needed for Applet */
    private boolean xmlFromResources = false;

    /**
     * private constructor: singleton pattern.
     */
    private ProjectMetaDataImpl() {

    }

    // package restricted access
    /**
     * Gets the instance impl.
     * 
     * @return the instance impl
     */
    public static ProjectMetaDataImpl getInstanceImpl() {
        return singleton;
    }

    // could be made public. Isn't needed, because getters are available through
    // inputData
    /**
     * Gets the single instance of ProjectMetaDataImpl.
     * 
     * @return single instance of ProjectMetaDataImpl
     */
    static ProjectMetaData getInstance() {
        return singleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#getProjectName()
     */
    @Override
    public String getProjectName() {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#getPathToProjectXmlFile()
     */
    @Override
    public String getPathToProjectXmlFile() {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#getOutputPath()
     */
    @Override
    public String getOutputPath() {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#isInstantaneousFileOutput()
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#isOnlyValidation()
     */
    @Override
    public boolean isOnlyValidation() {
        return onlyValidation;
    }

    /**
     * Sets the only validation.
     * 
     * @param onlyValidation
     *            the new only validation
     */
    public void setOnlyValidation(boolean onlyValidation) {
        this.onlyValidation = onlyValidation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#isWriteInternalXml()
     */
    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#isXmlFromResources()
     */
    @Override
    public boolean isXmlFromResources() {
        return xmlFromResources;
    }

    /**
     * Sets the xml from resources.
     * 
     * @param xmlFromResources
     *            the new xml from resources
     */
    public void setXmlFromResources(boolean xmlFromResources) {
        this.xmlFromResources = xmlFromResources;
    }

}
