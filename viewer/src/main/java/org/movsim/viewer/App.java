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
package org.movsim.viewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.movsim.input.MovsimCommandLine;
import org.movsim.input.ProjectMetaData;
import org.movsim.logging.Logger;
import org.movsim.viewer.ui.AppFrame;
import org.movsim.viewer.ui.LogWindow;
import org.movsim.viewer.ui.ViewProperties;
import org.movsim.viewer.util.LocalizationStrings;

public class App {

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException, IOException {

        Locale.setDefault(Locale.US);

        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName(),
                Locale.getDefault());

        LogWindow.setupLog4JAppender();

        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();

        Logger.initializeLogger();

        // parse the command line, putting the results into projectMetaData
        MovsimCommandLine.parse(args);

        Properties properties = ViewProperties.loadProperties(projectMetaData);

        AppFrame appFrame = new AppFrame(resourceBundle, projectMetaData, properties);
    }

}
