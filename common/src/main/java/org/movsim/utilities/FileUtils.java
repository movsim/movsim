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
package org.movsim.utilities;

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

import org.movsim.input.ProjectMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public final class FileUtils {

    private static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        throw new IllegalStateException("do not instanciate");
    }

    /**
     * Searches a file first in given location {@code filename} and second in path of the inputfile.
     * 
     * @throws IllegalArgumentException
     */
    public static File lookupFilename(String filename) throws IllegalArgumentException {
        File file = new File(filename);
        if (!file.exists() && ProjectMetaData.getInstance().hasPathToProjectFile()) {
            file = new File(ProjectMetaData.getInstance().getPathToProjectFile(), filename);
        }
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("cannot find input file = " + file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Gets the writer.
     * 
     * @param filename
     *            the filename
     * @return the writer
     */
    public static PrintWriter getWriter(String filename) {
        try {
            LOG.info("open file {} for writing", filename);
            final PrintWriter fstr = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
            return fstr;
        } catch (final java.io.IOException e) {
            LOG.error("cannot open file {} for writing", filename);
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
            LOG.debug("open file {} for reading", filename);
            final BufferedReader reader = new BufferedReader(new FileReader(filename));
            return reader;
        } catch (final Exception e) {
            LOG.error("cannot open file {} for reading", filename);
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

    public static boolean fileExists(String filename) {
        final File file = new File(filename);
        if (file.exists() && file.isFile()) {
            LOG.debug("{} file exists!", filename);
            return (true);
        }
        return (false);
    }

    public static boolean dirExists(String path, String msg) {
        final File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            LOG.info("{}: {} exists!", msg, file.getName());
            return (true);
        }
        return (false);
    }

    public static void createDir(String path, String msg) {
        final File file = new File(path);
        if (dirExists(path, msg)) {
            return;
        }
        final boolean success = file.mkdir();
        if (!success) {
            LOG.error("createDir: cannot create directory {}. Exit.", path);
            System.exit(-5);
        }
    }

    public static void deleteFile(String filename, String msg) {
        final File file = new File(filename);
        if (file.exists()) {
            LOG.info(msg + ": file\"" + file.getName() + "\" exists!");
            final boolean success = file.delete();
            if (success) {
                LOG.info("file " + filename + " successfully deleted ...");
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            final String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                final boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void deleteDir(String dirName) {
        if (!dirExists(dirName, "FileUtils...deleteDir...")) {
            return;
        }
        final File dir = new File(dirName);
        final boolean success = deleteDir(dir);
        if (!success) {
            System.exit(-5);
        }
    }

    public static String[] getFileList(String path, String regex) {
        final File dir = new File(path);

        class PatternFilter implements FilenameFilter {
            final String regexExpression;

            public PatternFilter(String regex) {
                regexExpression = regex;
            }

            @Override
            public boolean accept(File f, String name) {
                final Pattern patternRegex = Pattern.compile(regexExpression);
                final Matcher matcher = patternRegex.matcher(name);
                final boolean matches = matcher.matches();
                return (matches);
            }
        }

        final String[] fileNames = dir.list(new PatternFilter(regex));

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = path + fileNames[i];
            }
        }
        return (fileNames);
    }

    public static void deleteFileList(String path, String regex) {
        final String[] files = getFileList(path + File.separator, regex);
        for (int i = 0; i < files.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("filename to delete = " + files[i]);
            }
            deleteFile(files[i], "deleteFileList with regexExpression = " + regex);
        }
    }

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

    public static void resourceToFile(final InputStream resourceAsStream, String filename) {
        try {
            if (resourceAsStream == null) {
                LOG.debug("resource not included!");
                return;
            }

            final PrintWriter writer = FileUtils.getWriter(filename);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                writer.println(line);
            }

            bufferedReader.close();
            writer.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static InputSource getInputSourceFromFilename(String filename) {
        final File inputFile = new File(filename);
        return getInputSourceFromFilename(inputFile);
    }

    public static InputSource getInputSourceFromFilename(File inputFile) {
        InputSource inputSource = null;
        try {
            inputSource = new InputSource(new FileInputStream(inputFile));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return inputSource;
    }

    public static String getCanonicalPathWithoutFilename(String filename) {
        final File file = new File(filename);
        String string = null;
        try {
            string = file.getCanonicalPath().substring(0, file.getCanonicalPath().indexOf(file.getName()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String getCanonicalPathWithoutFilename(File file) {
        String string = null;
        try {
            string = file.getCanonicalPath().substring(0, file.getCanonicalPath().indexOf(file.getName()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String getCanonicalPath(String outputPath) {
        final File file = new File(outputPath);
        String path = null;
        try {
            path = file.getCanonicalPath();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static String getProjectName(File file) {
        String name = null;
        name = file.getName().substring(0, file.getName().indexOf(".xml"));
        return name;
    }
}