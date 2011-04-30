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
package org.movsim.input.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalDtdResolver.
 * @author Arne Kesting
 */
public class LocalDtdResolver implements EntityResolver {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LocalDtdResolver.class);

    /** The dtd filename. */
    private final String dtdFilename;

    /** The dtd source. */
    private InputSource dtdSource;
    
    /** The input stream. */
    private InputStream inputStream;

    /**
     * Gets the input stream.
     * 
     * @return the input stream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Input source.
     * 
     * @return the input source
     */
    public InputSource inputSource() {
        return dtdSource;
    }

    /**
     * Instantiates a new local dtd resolver.
     * 
     * @param resName
     *            the res name
     */
    public LocalDtdResolver(String resName) {
        dtdFilename = resName;
        dtdSource = null;

        try {
            dtdSource = resolveEntity();
        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resolve entity.
     * 
     * @return the input source
     * @throws SAXException
     *             the sAX exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private InputSource resolveEntity() throws SAXException, IOException {
        logger.info("try loading resource file \"{}\" from class path.", dtdFilename);
        final Properties prop = System.getProperties();
        logger.info("classpath:\n" + prop.getProperty("java.class.path", null));
        logger.info("getResource via classLoader:"+ this.getClass().getClassLoader().getResource(dtdFilename));
        logger.info("getResourceAsStream via classLoader:");
        if (inputStream == null) {
            logger.error("no resource found (must be on classpath) !!! Exit ");
            System.exit(-1);
        }
        final InputSource isrc = new InputSource(inputStream);
        return isrc;
    }

    // implemementation very simple without consideration of ids !!!
    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     * java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        return dtdSource;
    }

}
