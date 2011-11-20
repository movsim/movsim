/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.input;


public class ProjectMetaData{

    private static ProjectMetaData singleton = new ProjectMetaData();

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
    private ProjectMetaData() {

    }

    // package restricted access
    /**
     * Gets the instance impl.
     * 
     * @return the instance impl
     */
    public static ProjectMetaData getInstanceImpl() {
        return singleton;
    }

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
