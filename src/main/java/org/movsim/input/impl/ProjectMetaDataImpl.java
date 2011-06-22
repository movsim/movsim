package org.movsim.input.impl;

import org.movsim.input.ProjectMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectMetaDataImpl implements ProjectMetaData {
    
    private static ProjectMetaDataImpl singleton = new ProjectMetaDataImpl();
    
    private String projectName; // for testing
    private String pathToProjectXmlFile;
    private String outputPath;
    private boolean instantaneousFileOutput=true;
    private boolean onlyValidation = false;
    private boolean writeInternalXml = false;
    
    /** Needed for Applet */
    private boolean xmlFromResources = false;
    
    
    // private constructor: singleton pattern
    private ProjectMetaDataImpl() {
        
    }
    
    // package restricted access
    static ProjectMetaDataImpl getInstanceImpl() {
        return singleton;
    }
    
    // could be made public. Isn't needed, because getters are available through inputData
    static ProjectMetaData getInstance() {
        return singleton;
    }
    
    public String getProjectName() {
        return projectName;
    }


    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public String getPathToProjectXmlFile() {
        return pathToProjectXmlFile;
    }


    public void setPathToProjectXmlFile(String pathToProjectXmlFile) {
        this.pathToProjectXmlFile = pathToProjectXmlFile;
    }


    public String getOutputPath() {
        return outputPath;
    }


    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }


    public boolean isInstantaneousFileOutput() {
        return instantaneousFileOutput;
    }


    public void setInstantaneousFileOutput(boolean instantaneousFileOutput) {
        this.instantaneousFileOutput = instantaneousFileOutput;
    }


    public boolean isOnlyValidation() {
        return onlyValidation;
    }


    public void setOnlyValidation(boolean onlyValidation) {
        this.onlyValidation = onlyValidation;
    }


    public boolean isWriteInternalXml() {
        return writeInternalXml;
    }


    public void setWriteInternalXml(boolean writeInternalXml) {
        this.writeInternalXml = writeInternalXml;
    }


    public boolean isXmlFromResources() {
        return xmlFromResources;
    }


    public void setXmlFromResources(boolean xmlFromResources) {
        this.xmlFromResources = xmlFromResources;
    }

}
