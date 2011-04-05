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
package org.movsim.input.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LocalDtdResolver implements EntityResolver {


    final static Logger logger = LoggerFactory.getLogger(LocalDtdResolver.class);
    
        private  String dtdFilename;
        
        private InputSource dtdSource;
        private InputStream inputStream;
        public InputStream getInputStream(){ return inputStream;}
        
        public InputSource inputSource(){ return dtdSource; }
        
        
        public LocalDtdResolver(String resName){
            dtdFilename = resName;
            dtdSource = null;
            
            try {
                dtdSource = resolveEntity();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private InputSource resolveEntity() throws SAXException, IOException {
            logger.info("try loading resource file \"{}\" from class path.", dtdFilename);
            Properties prop = System.getProperties();
            System.out.println("classpath:\n"+prop.getProperty("java.class.path", null));
            System.out.println("getResource via classLoader:");
            System.out.println(this.getClass().getClassLoader().getResource(dtdFilename));
            System.out.println("getResourceAsStream via classLoader:");
            //inputStream = this.getClass().getClassLoader().getResourceAsStream(dtdResource);
            //System.out.println(this.getClass().getResource(dtdFilename).toString());
            //inputStream = this.getClass().getResourceAsStream(dtdFilename);
            if(inputStream==null){
                logger.error("no resource found (must be on classpath) !!! Exit ");
                System.exit(-1);
            }
            final InputSource isrc = new InputSource(inputStream);
            return isrc;
        }


        // implemementation very simple without consideration of ids !!! 
        public InputSource resolveEntity (String publicId, String systemId) throws IOException, SAXException{
            //System.out.println(" ... LocalDtdResolver.resolveEntity called ...");
            return dtdSource;
        }

}
