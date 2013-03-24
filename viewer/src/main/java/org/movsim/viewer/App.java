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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

//        final String path = "sim/buildingBlocks/"; 
//        String[] resourceListing = getResourceListing(App.class, path);
//        System.out.println("size of files="+resourceListing.length);
//        for(String str : resourceListing){
//            System.out.println("file = "+str);
//        }
//        URL project = App.class.getClassLoader().getResource(path+"onramp.xml");
//        URL projectPath = App.class.getClassLoader().getResource(path);
//        File file = new File(project.getFile());
//        System.out.println("file exists = "+file.exists());
        System.out.println("project = "+projectMetaData.getProjectName());
        
        
        AppFrame appFrame = new AppFrame(resourceBundle, projectMetaData, properties);
    }
    
    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     * 
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException 
     * @throws IOException 
     */
    static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
          /* A file path: easy enough */
          return new File(dirURL.toURI()).list();
        } 

        if (dirURL == null) {
          /* 
           * In case of a jar file, we can't actually find a directory.
           * Have to assume the same jar as clazz.
           */
          String me = clazz.getName().replace(".", "/")+".class";
          dirURL = clazz.getClassLoader().getResource(me);
        }
        
        if (dirURL.getProtocol().equals("jar")) {
          /* A JAR path */
          String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
          JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
          Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
          Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
          while(entries.hasMoreElements()) {
            String name = entries.nextElement().getName();
            if (name.startsWith(path)) { //filter according to the path
              String entry = name.substring(path.length());
              int checkSubdir = entry.indexOf("/");
              if (checkSubdir >= 0) {
                // if it is a subdirectory, we just return the directory name
                entry = entry.substring(0, checkSubdir);
              }
              result.add(entry);
            }
          }
          return result.toArray(new String[result.size()]);
        } 
          
        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }
    
}
