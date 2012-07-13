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
package org.movsim.viewer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.movsim.input.ProjectMetaData;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.ui.LogWindow;
import org.movsim.viewer.ui.AppFrame;
import org.movsim.viewer.util.LocalizationStrings;
import org.movsim.viewer.util.ViewerCommandLine;

public class App {

    /**
     * @param args
     */
    public static void main(String[] args) {

        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName(),
                Locale.getDefault());

        LogWindow.setupLog4JAppender();

        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        
        Properties properties = loadProperties();
        
        // parse the command line, putting the results into projectMetaData
        ViewerCommandLine.parse(projectMetaData, args);

        AppFrame appFrame = new AppFrame(resourceBundle, projectMetaData, properties);
    }
    
    public static Properties loadProperties() {
        Properties applicationProps = null;
        try {
            // create and load default properties
            final Properties defaultProperties = new Properties();
            final InputStream is = TrafficCanvas.class.getResourceAsStream("/config/defaultviewerconfig.properties");
            defaultProperties.load(is);
            is.close();

            // create application properties with default
            applicationProps = new Properties(defaultProperties);

            // now load specific project properties
            final String path = ProjectMetaData.getInstance().getPathToProjectXmlFile();
            final String projectName = ProjectMetaData.getInstance().getProjectName();
            if (ProjectMetaData.getInstance().isXmlFromResources()) {
                final InputStream inputStream = TrafficCanvas.class.getResourceAsStream(path + projectName
                        + ".properties");
                if (inputStream != null) {
                    defaultProperties.load(inputStream);
                    inputStream.close();
                }
            } else {
                final InputStream in = new FileInputStream(path + projectName + ".properties");
                applicationProps.load(in);
                in.close();
            }

        } catch (FileNotFoundException e) {
            // ignore exception.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicationProps;
    }
}
