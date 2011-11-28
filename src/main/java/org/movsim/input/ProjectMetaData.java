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
package org.movsim.input;


public class ProjectMetaData{

    private static ProjectMetaData singleton = new ProjectMetaData();

    private String projectName = ""; //"/file/src/test/resources/" + "onramp_IDM" + ".xml";
    private String pathToProjectXmlFile;
    private String outputPath;
    private String xodrFileName;
    private String xodrPath;
    private boolean instantaneousFileOutput = true;
    private boolean onlyValidation = false;
    private boolean writeInternalXml = false;

    /** Needed for Applet */
    private boolean xmlFromResources = false;

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
    static ProjectMetaData getInstance() {
        return singleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#getProjectName()
     */
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

    public void setXodrFilename(String xodrFilename) {
    	this.xodrFileName = xodrFilename;
    }

    public String getXodrFilename() {
        return xodrFileName;
    }
    
    public void setXodrPath(String xodrPath) {
    	this.xodrPath = xodrPath;
    }
    public String getXodrPath() {
        return xodrPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.ProjectMetaData#isInstantaneousFileOutput()
     */
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
