package org.movsim.output.fileoutput;

import java.io.PrintWriter;

import org.movsim.input.ProjectMetaData;

public class FileOutputBase {

	public static final String COMMENT_CHAR = "#";

    final String path;
    final String baseFilename;
    PrintWriter writer;

    /**
     * Constructor, sets the path and base filename.
     */
    public FileOutputBase() {
        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        path = projectMetaData.getOutputPath();
        baseFilename = projectMetaData.getProjectName();
    }
}
