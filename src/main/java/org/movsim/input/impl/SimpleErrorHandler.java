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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleErrorHandler.
 */
public class SimpleErrorHandler implements ErrorHandler {
    final static Logger logger = LoggerFactory.getLogger(SimpleErrorHandler.class);
    private boolean error;

    /**
     * Instantiates a new simple error handler.
     */
    public SimpleErrorHandler() {
        error = false;
    }

    /**
     * Checks if is error.
     * 
     * @return true, if is error
     */
    public boolean isError() {
        return error;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        logger.warn(getInfo(exception));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException exception) throws SAXException {
        logger.error(getInfo(exception));
        error = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        logger.error(getInfo(exception));
        error = true;
    }

    /**
     * Gets the info.
     * 
     * @param e
     *            the e
     * @return the info
     */
    private String getInfo(SAXParseException e) {
        final StringBuilder stringb = new StringBuilder();
        stringb.append("   Public ID: " + e.getPublicId());
        stringb.append("   System ID: " + e.getSystemId());
        stringb.append("   Line number: " + e.getLineNumber());
        stringb.append("   Column number: " + e.getColumnNumber());
        stringb.append("   Message: " + e.getMessage());
        return stringb.toString();
    }
}
