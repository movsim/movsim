package org.movsim.input;

public interface ProjectMetaData {

    String getProjectName();

    String getPathToProjectXmlFile();

    String getOutputPath();

    boolean isInstantaneousFileOutput();

    boolean isOnlyValidation();

    boolean isWriteInternalXml();

    boolean isXmlFromResources();
}