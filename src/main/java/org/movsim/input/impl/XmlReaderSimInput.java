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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.impl.OutputInputImpl;
import org.movsim.input.model.impl.SimulationInputImpl;
import org.movsim.input.model.impl.VehicleInputImpl;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class XmlReaderSimInput.
 * @author Arne Kesting, Ralph Germ
 */
public class XmlReaderSimInput {

    final static Logger logger = LoggerFactory.getLogger(XmlReaderSimInput.class);

    private boolean isValid;

    private final InputDataImpl inputData;

    private final String xmlFilename;

    private Document doc;

    /**
     * Instantiates a new xml reader to parse and validate the simulation input.
     * 
     * @param xmlFilename
     *            the xml filename
     * @param inputData
     *            the input data
     */
    public XmlReaderSimInput(String xmlFilename, InputDataImpl inputData) {
        this.xmlFilename = xmlFilename;
        this.inputData = inputData;

        if (!FileUtils.fileExists(xmlFilename)) {
            logger.error("XML File does not exist. Exit Simulation.");
            System.exit(1);
        }

        logger.info("Begin Parsing: " + xmlFilename);
        readAndValidateXml();

        fromDomToInternalDatastructure();

        logger.info("End XmlReaderSimInput.");
    }

    /**
     * From dom to internal data structure.
     */
    @SuppressWarnings("unchecked")
    private void fromDomToInternalDatastructure() {

        inputData.setProjectName(xmlFilename.substring(0, xmlFilename.indexOf(".xml")));

        final Element root = doc.getRootElement();

        // -------------------------------------------------------

        final List<VehicleInput> vehicleInputData = new ArrayList<VehicleInput>();

        final List<Element> vehicleElements = root.getChild("DRIVER_VEHICLE_UNITS").getChildren();
        for (final Element vehElem : vehicleElements) {
            vehicleInputData.add(new VehicleInputImpl(vehElem));
        }
        inputData.setVehicleInputData(vehicleInputData);

        // -------------------------------------------------------

        final OutputInput outputInput = new OutputInputImpl(root.getChild("OUTPUT"));
        inputData.setOutputInput(outputInput);

        // -------------------------------------------------------

        final SimulationInput simInput = new SimulationInputImpl(root.getChild("SIMULATION"));
        inputData.setSimulationInput(simInput);
    }

    /**
     * Read and validate xml.
     */
    private void readAndValidateXml() {
        
        doc = getDocument(getInput(xmlFilename));
        validate(getInput(xmlFilename));
        
        if (!isValid) {
            logger.error("xml input file {} is not well-formed or invalid ...", xmlFilename);
//            System.exit(0);
        }

    }

    /**
     * Gets the Document
     * 
     * @param inputSource
     *            the input source
     * @return the document
     */
    private Document getDocument(InputSource inputSource) {
        try {
            final SAXBuilder builder = new SAXBuilder();
            // builder.setEntityResolver(new LocalDtdResolver(dtdFilename));
            // builder.setValidation(true); // requires a "dtd" file !
            builder.setIgnoringElementContentWhitespace(true);
            final Document doc = builder.build(inputSource);
            return doc;
        } catch (final JDOMException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validates the Inputsource
     * 
     * @param inputSource
     *            the input source
     */
    private void validate(InputSource inputSource) {
        /**
         * global flag !!! also used in errorHandler
         */
        isValid = true; 
        try {
            logger.debug("validate input ... ");
            final XMLReader myXMLReader = XMLReaderFactory.createXMLReader();
            myXMLReader.setFeature("http://xml.org/sax/features/validation", true);
            final DefaultHandler handler = new MyErrorHandler();
            myXMLReader.setErrorHandler(handler);
            myXMLReader.parse(inputSource);
        } catch (final SAXException e) {
            isValid = false;
        } catch (final IOException e) {
            isValid = false;
        }
    }

    /**
     * Gets the inputsource from filename.
     * 
     * @param filename
     *            the filename
     * @return the input
     */
    private InputSource getInput(String filename) {
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


    /**
     * The Inner Class MyErrorHandler.
     * 
     * uses global isValid flag 
     */
    class MyErrorHandler extends DefaultHandler {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException
         * )
         */
        @Override
        public void warning(SAXParseException e) throws SAXException {
            logger.warn(getInfo(e));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException
         * )
         */
        @Override
        public void error(SAXParseException e) throws SAXException {
            logger.error(getInfo(e));
            isValid = false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.
         * SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            logger.error(getInfo(e));
            isValid = false;
        }

        
        /**
         * Gets the info to the corresponding exception
         * 
         * @param e
         *            the exception
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

}