package org.movsim.input.impl;

public class ProjectMetaDataImpl {

    private static String projectName = "onramp_IDM";
    private static String pathToProjectXmlFile;
    private static String outputPath;
    private static boolean instantaneousFileOutput;
    private static boolean onlyValidation = false;
    private static boolean writeInternalXml = false;
    
    /** Needed for Applet */
    private static boolean xmlFromResources = false;
    
    
    // private constructor: singleton pattern
    private ProjectMetaDataImpl() {
        
    }

    public static String getProjectName() {
        return projectName;
    }

    public static void setProjectName(String projectName) {
        ProjectMetaDataImpl.projectName = projectName;
    }

    public static String getPathToProjectXmlFile() {
        return pathToProjectXmlFile;
    }

    public static void setPathToProjectXmlFile(String pathToProjectXmlFile) {
        ProjectMetaDataImpl.pathToProjectXmlFile = pathToProjectXmlFile;
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static void setOutputPath(String outputPath) {
        ProjectMetaDataImpl.outputPath = outputPath;
    }

    public static boolean isInstantaneousFileOutput() {
        return instantaneousFileOutput;
    }

    public static void setInstantaneousFileOutput(boolean instantaneousFileOutput) {
        ProjectMetaDataImpl.instantaneousFileOutput = instantaneousFileOutput;
    }

    public static void setOnlyValidation(boolean onlyValidation) {
        ProjectMetaDataImpl.onlyValidation = onlyValidation;
    }

    public static boolean isOnlyValidation() {
        return onlyValidation;
    }

    public static void setWriteInternalXml(boolean writeInternalXml) {
        ProjectMetaDataImpl.writeInternalXml = writeInternalXml;
    }

    public static boolean isWriteInternalXml() {
        return writeInternalXml;
    }

    public static void setXmlFromResources(boolean xmlFromResources) {
        ProjectMetaDataImpl.xmlFromResources = xmlFromResources;
    }

    public static boolean isXmlFromResources() {
        return xmlFromResources;
    }


}
