package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;

import org.movsim.input.ProjectMetaData;
import org.movsim.utilities.FileUtils;

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

    public PrintWriter createWriter(String extension) {
        final String filename = path + File.separator + baseFilename + extension;
        return FileUtils.getWriter(filename);
    }
}
