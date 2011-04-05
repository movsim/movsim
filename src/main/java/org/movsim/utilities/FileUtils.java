/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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
package org.movsim.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtils {
    
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	public static PrintWriter getWriter(String filename){
		try{
		    logger.debug("open file {} for writing", filename);
			PrintWriter fstr = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
			return fstr;
		} catch (java.io.IOException e) {
			//e.printStackTrace();
		    logger.error("cannot open file {} for writing", filename);
		}
		return null;
	}
	
	
	public static BufferedReader getReader(String filename){
		try {
		    logger.debug("open file {} for reading", filename);
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			return reader;
		}catch (Exception e) {
			//e.printStackTrace();
			logger.error("cannot open file {} for reading", filename);
		}
		return null;
	}
	
	public static String currentDirectory(){
		return System.getProperty("user.dir");
	}
	
	// achtung, ist z.B. auf der V40 NICHT gesetzt !!!
	// muss man ggf. per hand abfangen .... oder besser nicht benutzen.
	public static String homeDirectory(){
		String home = System.getProperty("user.home");
		if( home.equalsIgnoreCase("?")){
//			Logger.err(true, "!!! Environmental variable getProperty(\"user.home\")= "+home+" not set correctly!!!");
		}
		return home;
	}
	
	// check for existing file
	public static boolean fileExists(String filename, String msg) {
		File file = new File(filename);
		if (file.exists() && file.isFile() ) {
			//Logger.log(msg + ": \"" + file.getName() + "\" exists!");
			return (true);
		}
		return (false);
	}
	
	// check for existing file
    public static boolean fileExists(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile() ) {
            logger.debug("{} file exists!", filename);
            return (true);
        }
        return (false);
    }

	// check if directory exists (in fact the same as file)
	public static boolean dirExists(String path, String msg) {
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			//Logger.log(msg + ": \"" + file.getName() + "\" exists!");
			return (true);
		}
		return (false);
	}

	// create directory
	public static void createDir(String path, String msg) {
		File file = new File(path);
		if (dirExists(path, msg)) {
			return;
		}
//		Logger.log(msg + ": create directory \"" + path + "\"");
		boolean success = file.mkdir();
		if (!success) {
//			Logger.err("createDir: cannot create directory " + path);
//			Logger.err("msg from calling class" + msg);
//			Logger.err("exit now!!!");
			System.exit(-5);
		}
	}

	// delete existing file
	public static void deleteFile(String filename, String msg) {
		File file = new File(filename);
		if (file.exists()) {
		    System.out.println(msg + ": file\"" + file.getName() + "\" exists!");
			boolean success = file.delete();
			if (success) System.out.println("file " + filename + " successfully deleted ...");
		}
	}
	
  // Deletes all files and subdirectories under dir.
  // Returns true if all deletions were successful.
  // If a deletion fails, the method stops attempting to delete and returns false.
  // Deletes all files and subdirectories under dir.
  // Returns true if all deletions were successful.
  // If a deletion fails, the method stops attempting to delete and returns false.
  private static boolean deleteDir(File dir) {
  	if (dir.isDirectory()) {
  		String[] children = dir.list();
  		for (int i=0; i<children.length; i++) {
  			boolean success = deleteDir(new File(dir, children[i]));
  			if (!success) {
  				return false;
  			}
  		}
  	}
  	// The directory is now empty so delete it
  	return dir.delete();
  }
	
	
  public static void deleteDir(String dirName) {
  	if(!dirExists(dirName,"FileUtils...deleteDir...")) return;
  	File dir = new File(dirName);
  	boolean success = deleteDir(dir);
  	if (!success) {
//			Logger.err("deleteDir: cannot delete directory " + dirName);
//			Logger.err("exit now!!!");
			System.exit(-5);
		}
  }

  // returns a String[] of files found in the path applying to the filter string
	public static String[] getFileList(String path, String regex) {
		File dir = new File(path);

		class PatternFilter implements FilenameFilter {
			String regex;

			public PatternFilter(String regex) {
				this.regex = regex;
				//Logger.log("PatternFilter has regex  = " + regex);
			}

			public boolean accept(File f, String name) {
				//String fString = f.toString().toLowerCase();
				// Logger.log("fString = "+fString);
				// pattern consists of two pattern given by user und ends with a
				// number!!!
				Pattern patternRegex = Pattern.compile(regex);
				Matcher matcher = patternRegex.matcher(name);
				boolean matches = matcher.matches();

//				if (matches) {
//					System.out.println("regex: \"" + regex + "\" matches in  \"" + name + "\"");
//				}
				return (matches);
			}
		}

		String[] fileNames = dir.list(new PatternFilter(regex));

		if (fileNames != null) {
		    for (int i = 0; i < fileNames.length; i++) {
		        fileNames[i] = path+fileNames[i];
				//System.out.println("filename = " + fileNames[i]);
			}
		}
		return (fileNames);
	}
	
	
	public static void deleteFileList(String path, String regex) {
		String[] file=getFileList(path, regex);
		for(int i=0; i<file.length; i++){
			//System.out.println("********* test = "+file[i]);
			deleteFile(file[i], "deleteFileList with regex = "+regex);
		}
	}

	public static void writeStreamToFile(String filename, InputStream is){
		PrintWriter writer = getWriter(filename);
		try { 
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while (null != (line = br.readLine())) {
				writer.println(line);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		writer.close();
	}
	
}