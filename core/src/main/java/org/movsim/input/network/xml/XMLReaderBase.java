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

package org.movsim.input.network.xml;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.movsim.input.ProjectMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base class for XML file readers. Checks if the file exists and then invokes the SAX parser.
 * 
 * @author martinbudden
 * 
 */
public class XMLReaderBase {

    final static Logger logger = LoggerFactory.getLogger(XMLReaderBase.class);

    /**
     * Parses the XML format file. Checks if the file exists and then invokes the SAX parser with the given handler.
     * 
     * @param fullFilename
     * @param handler
     * @return true if file parsed without error, false otherwise
     */
    protected static boolean parse(String fullFilename, DefaultHandler handler) {
        logger.info("parsing file: " + fullFilename);
        ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        if (projectMetaData.isParseFromInputstream()) {
            InputStream is = projectMetaData.getNetworkXml();
            try {
                final SAXParserFactory factory = SAXParserFactory.newInstance();
                final SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(is, handler);
            } catch (final Exception e) {
                e.printStackTrace();
                logger.error("parsing failed");
                logger.error(e.getLocalizedMessage());
                return false;
            }

        } else if (projectMetaData.isXmlFromResources()) {
            final InputStream inputstream = XMLReaderBase.class.getResourceAsStream(fullFilename);
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser;
            try {
                saxParser = factory.newSAXParser();
                saxParser.parse(inputstream, handler);
            } catch (final Exception e) {
                e.printStackTrace();
                logger.error("parsing from resources failed");
                logger.error(e.getLocalizedMessage());
                return false;
            }
        } else {
            final File file = new File(fullFilename);
            if (file.exists() == false) {
                logger.warn("file {} does not exist. Try parsing from resources.", fullFilename);
                final InputStream inputstream = XMLReaderBase.class.getResourceAsStream(fullFilename);
                final SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser;
                try {
                    saxParser = factory.newSAXParser();
                    saxParser.parse(inputstream, handler);
                } catch (final Exception e) {
                    e.printStackTrace();
                    logger.error("parsing from resources failed");
                    logger.error(e.getLocalizedMessage());
                    return false;
                }
            } else {
                try {
                    final SAXParserFactory factory = SAXParserFactory.newInstance();
                    final SAXParser saxParser = factory.newSAXParser();
                    saxParser.parse(fullFilename, handler);
                } catch (final Exception e) {
                    e.printStackTrace();
                    logger.error("parsing failed");
                    logger.error(e.getLocalizedMessage());
                    return false;
                }
            }
        }

        return true;
    }
}
