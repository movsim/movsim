/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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
package org.movsim.io;

import com.google.common.base.Preconditions;
import org.movsim.shutdown.ShutdownHooks;
import org.movsim.shutdown.SimulationShutDown;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;

public class FileOutputBase implements SimulationShutDown {

    // logger for all sub-classes
    public final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static final String COMMENT_CHAR = "#";
    public static final String SEPARATOR_CHAR = ",";
    public static final String NEWLINE = "%n";

    protected final String path;
    protected final String baseFilename;
    protected String filename;
    protected PrintWriter writer;

    public FileOutputBase(String path, String baseFilename) {
        this.path = path;
        this.baseFilename = baseFilename;
    }

    public PrintWriter createWriter(String extension) {
        filename = getFilename(extension);
        Preconditions.checkArgument(filename.length() > 0);
        ShutdownHooks.INSTANCE.addCallback(this);
        return FileUtils.getWriter(filename);
    }

    private String getFilename(String extension) {
        return path + File.separator + baseFilename + extension;
    }

    public void write(String format, Object... args) {
        writer.printf(format, args);
        writer.flush();
    }

    @Override
    public void onShutDown() {
        if (writer != null) {
            LOG.info("close writer for file={}", filename);
            writer.close();
        }
    }
}
