/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.input;

// TODO: Auto-generated Javadoc
/**
 * The Interface ProjectMetaData.
 */
public interface ProjectMetaData {

    /**
     * Gets the project name.
     * 
     * @return the project name
     */
    String getProjectName();

    /**
     * Gets the path to project xml file.
     * 
     * @return the path to project xml file
     */
    String getPathToProjectXmlFile();

    /**
     * Gets the output path.
     * 
     * @return the output path
     */
    String getOutputPath();

    /**
     * Checks if is instantaneous file output.
     * 
     * @return true, if is instantaneous file output
     */
    boolean isInstantaneousFileOutput();

    /**
     * Checks if is only validation.
     * 
     * @return true, if is only validation
     */
    boolean isOnlyValidation();

    /**
     * Checks if is write internal xml.
     * 
     * @return true, if is write internal xml
     */
    boolean isWriteInternalXml();

    /**
     * Checks if is xml from resources.
     * 
     * @return true, if is xml from resources
     */
    boolean isXmlFromResources();
}