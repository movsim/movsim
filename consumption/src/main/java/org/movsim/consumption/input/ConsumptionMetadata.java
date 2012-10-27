package org.movsim.consumption.input;

import java.io.File;


public class ConsumptionMetadata {

    private static final String CONSUMPTION_DTD_FILENAME = "movsimConsumptionModel.dtd";

    private static final String CONSUMPTION_DTD_PATH = "config";

    private static final String CONFIG_FILE_ENDING = ".xml";
    
    private static final String LOG4J_FILENAME = "log4j.properties";

    private String outputPath;
    
    private String projectName;
    
    private String pathToConsumptionFile;
    
    private String consumptionPath;

    private boolean onlyValidation = false;
    
    private boolean writeInternalXml = false;
    
    
    private static ConsumptionMetadata singleton = new ConsumptionMetadata();
    
    public static ConsumptionMetadata getInstance() {
        return singleton;
    }
    
    private ConsumptionMetadata() {
    }


    static String getDtdFilename() {
        return CONSUMPTION_DTD_FILENAME;
    }

    private static String getDtdPath() {
        return CONSUMPTION_DTD_PATH;
    }

    public String getDtdFilenameWithPath() {
        return File.separator + getDtdPath() + File.separator + getDtdFilename(); 
        //getDtdPath()+getDtdFilename();
    }

    public static String getConfigFileEnding() {
        return CONFIG_FILE_ENDING;
    }


    public String getOutputPath() {
        return outputPath;
    }


    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }


    public String getConsumptionFilename() {
        return projectName+ CONFIG_FILE_ENDING;
    }


    public String getConsumptionPath() {
        return consumptionPath;
    }


    public void setConsumptionPath(String consumptionPath) {
        this.consumptionPath = consumptionPath;
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


    public String getPathToConsumptionFile() {
        return pathToConsumptionFile;
    }


    public void setPathToConsumptionFile(String pathToConsumptionFile) {
        this.pathToConsumptionFile = pathToConsumptionFile;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
       this.projectName = projectName;
    }

    public boolean isParseFromInputstream() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isXmlFromResources() {
        // TODO Auto-generated method stub
        return false;
    }

    public File getXmlInputFile() {
        return new File(getPathToConsumptionFile(), getProjectName() + CONFIG_FILE_ENDING);
    }

    public static String getLog4jFilename() {
        return LOG4J_FILENAME;
    }

    public static String getLog4jFilenameWithPath() {
        return File.separator + getDtdPath() + File.separator + getLog4jFilename(); 
    }



}
