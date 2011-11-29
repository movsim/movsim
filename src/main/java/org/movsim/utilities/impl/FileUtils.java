/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.utilities.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.movsim.MovsimMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * The Class FileUtils.
 */
public class FileUtils {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Gets the writer.
     * 
     * @param filename
     *            the filename
     * @return the writer
     */
    public static PrintWriter getWriter(String filename) {
        try {
            logger.debug("open file {} for writing", filename);
            final PrintWriter fstr = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
            return fstr;
        } catch (final java.io.IOException e) {
            // e.printStackTrace();
            logger.error("cannot open file {} for writing", filename);
        }
        return null;
    }

    /**
     * Gets the reader.
     * 
     * @param filename
     *            the filename
     * @return the reader
     */
    public static BufferedReader getReader(String filename) {
        try {
            logger.debug("open file {} for reading", filename);
            final BufferedReader reader = new BufferedReader(new FileReader(filename));
            return reader;
        } catch (final Exception e) {
            // e.printStackTrace();
            logger.error("cannot open file {} for reading", filename);
        }
        return null;
    }

    /**
     * Current directory.
     * 
     * @return the string
     */
    public static String currentDirectory() {
        return System.getProperty("user.dir");
    }

    // achtung, ist z.B. auf der V40 NICHT gesetzt !!!
    // muss man ggf. per hand abfangen .... oder besser nicht benutzen.
    /**
     * Home directory.
     * 
     * @return the string
     */
    public static String homeDirectory() {
        final String home = System.getProperty("user.home");
        if (home.equalsIgnoreCase("?")) {
            // Logger.err(true,
            // "!!! Environmental variable getProperty(\"user.home\")= "+home+" not set correctly!!!");
        }
        return home;
    }

    // check for existing file
    /**
     * File exists.
     * 
     * @param filename
     *            the filename
     * @param msg
     *            the msg
     * @return true, if successful
     */
    public static boolean fileExists(String filename, String msg) {
        final File file = new File(filename);
        if (file.exists() && file.isFile())
            // Logger.log(msg + ": \"" + file.getName() + "\" exists!");
            return (true);
        return (false);
    }

    // check for existing file
    /**
     * File exists.
     * 
     * @param filename
     *            the filename
     * @return true, if successful
     */
    public static boolean fileExists(String filename) {
        final File file = new File(filename);
        if (file.exists() && file.isFile()) {
            logger.debug("{} file exists!", filename);
            return (true);
        }
        return (false);
    }

    /**
     * Dir exists. check if directory exists (in fact the same as file)
     * 
     * @param path
     *            the path
     * @param msg
     *            the additional message
     * @return true, if successful
     */
    public static boolean dirExists(String path, String msg) {
        final File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            logger.info("{}: {} exists!", msg, file.getName());
            return (true);
        }
        return (false);
    }

    /**
     * Creates the directory, if it does not exit already. Elsewise, does
     * nothing.
     * 
     * @param path
     *            the path
     * @param msg
     *            the msg
     */
    public static void createDir(String path, String msg) {
        final File file = new File(path);
        if (dirExists(path, msg))
            return;
        // Logger.log(msg + ": create directory \"" + path + "\"");
        final boolean success = file.mkdir();
        if (!success) {
            // Logger.err("createDir: cannot create directory " + path);
            // Logger.err("msg from calling class" + msg);
            // Logger.err("exit now!!!");
            System.exit(-5);
        }
    }

    // delete existing file
    /**
     * Delete file.
     * 
     * @param filename
     *            the filename
     * @param msg
     *            the msg
     */
    public static void deleteFile(String filename, String msg) {
        final File file = new File(filename);
        if (file.exists()) {
            System.out.println(msg + ": file\"" + file.getName() + "\" exists!");
            final boolean success = file.delete();
            if (success) {
                System.out.println("file " + filename + " successfully deleted ...");
            }
        }
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns
    // false.
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns
    // false.
    /**
     * Delete dir.
     * 
     * @param dir
     *            the dir
     * @return true, if successful
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Delete dir.
     * 
     * @param dirName
     *            the dir name
     */
    public static void deleteDir(String dirName) {
        if (!dirExists(dirName, "FileUtils...deleteDir..."))
            return;
        final File dir = new File(dirName);
        final boolean success = deleteDir(dir);
        if (!success) {
            // Logger.err("deleteDir: cannot delete directory " + dirName);
            // Logger.err("exit now!!!");
            System.exit(-5);
        }
    }

    // returns a String[] of files found in the path applying to the filter
    // string
    /**
     * Gets the file list.
     * 
     * @param path
     *            the path
     * @param regex
     *            the regex
     * @return the file list
     */
    public static String[] getFileList(String path, String regex) {
        final File dir = new File(path);

        class PatternFilter implements FilenameFilter {
            String regex;

            public PatternFilter(String regex) {
                this.regex = regex;
                // Logger.log("PatternFilter has regex  = " + regex);
            }

            @Override
            public boolean accept(File f, String name) {
                // String fString = f.toString().toLowerCase();
                // Logger.log("fString = "+fString);
                // pattern consists of two pattern given by user und ends with a
                // number!!!
                final Pattern patternRegex = Pattern.compile(regex);
                final Matcher matcher = patternRegex.matcher(name);
                final boolean matches = matcher.matches();

                // if (matches) {
                // System.out.println("regex: \"" + regex + "\" matches in  \""
                // + name + "\"");
                // }
                return (matches);
            }
        }

        final String[] fileNames = dir.list(new PatternFilter(regex));

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = path + fileNames[i];
                // System.out.println("filename = " + fileNames[i]);
            }
        }
        return (fileNames);
    }

    /**
     * Delete file list.
     * 
     * @param path
     *            the path
     * @param regex
     *            the regex
     */
    public static void deleteFileList(String path, String regex) {
        final String[] file = getFileList(path, regex);
        for (int i = 0; i < file.length; i++) {
            // System.out.println("********* test = "+file[i]);
            deleteFile(file[i], "deleteFileList with regex = " + regex);
        }
    }

    /**
     * Write stream to file.
     * 
     * @param filename
     *            the filename
     * @param is
     *            the is
     */
    public static void writeStreamToFile(String filename, InputStream is) {
        final PrintWriter writer = getWriter(filename);
        try {
            String line;
            final BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (null != (line = br.readLine())) {
                writer.println(line);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        writer.close();
    }

    /**
     * Resource to file.
     * 
     * @param res
     *            the res
     * @param filename
     *            the filename
     */
    public static void resourceToFile(String res, String filename) {
        try {
            InputStream resourceAsStream = MovsimMain.class.getResourceAsStream(res);

            if (resourceAsStream == null) {
                logger.debug("resource {} not included!", res);
                return;
            }

            PrintWriter writer = FileUtils.getWriter(filename);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                writer.println(line);
            }

            bufferedReader.close();
            writer.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Gets the inputsource from filename.
     * 
     * @param filename
     *            the filename
     * @return the input
     */
    public static InputSource getInputSourceFromFilename(String filename) {
        final File inputFile = new File(filename);
        InputSource inputSource = null;
        try {
            inputSource = new InputSource(new FileInputStream(inputFile));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return inputSource;
    }
    
    public static String getCanonicalPathWithoutFilename (String filename) {
        final File file = new File(filename);
        String string = null;
        try {
            string = file.getCanonicalPath().substring(0, file.getCanonicalPath().indexOf(file.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }
}