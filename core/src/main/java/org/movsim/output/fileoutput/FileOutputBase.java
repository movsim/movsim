/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;

import org.movsim.input.ProjectMetaData;
import org.movsim.utilities.FileUtils;

public class FileOutputBase {
    
    // TODO check if buffered writing is faster; if so make it tunable 
    public static final boolean FLUSH_IMMEDIATELY = true; 
    
    public static final String COMMENT_CHAR = "#";

    protected final String path;
    protected final String baseFilename;
    protected PrintWriter writer;

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

    public void write(String format, Object... args){
        writer.printf(format, args);
        if(FLUSH_IMMEDIATELY){
            writer.flush();
        }
    }
}
