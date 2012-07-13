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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.movsim.input.ProjectMetaData;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.ui.AppFrame;
import org.movsim.viewer.ui.LogWindow;
import org.movsim.viewer.util.LocalizationStrings;
import org.movsim.viewer.util.ViewerCommandLine;

public class App {

    final static String defaultPropertyName = "/config/defaultviewerconfig.properties";
    /**
     * @param args
     */
    public static void main(String[] args) {

        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName(),
                Locale.getDefault());

        LogWindow.setupLog4JAppender();

        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        
        // parse the command line, putting the results into projectMetaData
        ViewerCommandLine.parse(projectMetaData, args);
        
        Properties properties = loadProperties(projectMetaData.getProjectName(), projectMetaData.getPathToProjectXmlFile());

        AppFrame appFrame = new AppFrame(resourceBundle, projectMetaData, properties);
    }
    
    /**
     * Load default properties from the {code /config/defaultviewerconfig.properties} path. Needed for applet initialization.
     * 
     * @return the properties
     */
    public static Properties loadDefaultProperties() {
        Properties defaultProperties = new Properties();
        try {
            // create and load default properties
            final InputStream is = TrafficCanvas.class.getResourceAsStream(defaultPropertyName);
            defaultProperties.load(is);
            is.close();
            defaultProperties = new Properties(defaultProperties);
        } catch (FileNotFoundException e) {
            // ignore exception.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultProperties;
    }
    
    /**
     * Load default properties and overwrites them with project specific properties if available
     *
     * @param projectName
     * @param path
     * @return properties
     */
    public static Properties loadProperties(String projectName, String path) {
        Properties applicationProps = loadDefaultProperties();
        try {
            final File file = new File(path + projectName + ".properties");
            System.out.println("try to read from file="+file.getName()+", path="+file.getAbsolutePath());
            if (ProjectMetaData.getInstance().isXmlFromResources()) {
                final InputStream inputStream = App.class.getResourceAsStream(file.toString());
                if (inputStream != null) {
                    applicationProps.load(inputStream);
                    inputStream.close();
                }
            } else {
                final InputStream in = new FileInputStream(file);
                applicationProps.load(in);
                in.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();  // do not ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicationProps;
    }
}
