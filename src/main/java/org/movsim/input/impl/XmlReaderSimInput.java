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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.movsim.input.XmlElementNames;
import org.movsim.input.commandline.SimCommandLine;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.impl.OutputInputImpl;
import org.movsim.input.model.impl.SimulationInputImpl;
import org.movsim.input.model.impl.VehicleInputImpl;
import org.movsim.utilities.impl.FileUtils;
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
 * 
 * @author Arne Kesting, Ralph Germ
 */
public class XmlReaderSimInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(XmlReaderSimInput.class);

    /** The is valid. */
    private boolean isValid;

    /** The input data. */
    private final InputDataImpl inputData;

    /** The xml filename. */
    private final String xmlFilename;

    /** The xml filename. */
    private final String filenameEnding = ".xml";

    /** The doc. */
    private Document doc;

    private SimCommandLine cmdline;

    /**
     * Instantiates a new xml reader to parse and validate the simulation input.
     * 
     * @param xmlFilename
     *            the xml filename
     * @param cmdline
     * @param inputData
     *            the input data
     */
    public XmlReaderSimInput(String xmlFilename, SimCommandLine cmdline, InputDataImpl inputData) {
        this.xmlFilename = xmlFilename;
        this.cmdline = cmdline;
        this.inputData = inputData;

        if (!FileUtils.fileExists(xmlFilename)) {
            logger.error("XML File does not exist. Exit Simulation.");
            System.exit(1);
        }

        logger.info("Begin parsing: " + xmlFilename);
        readAndValidateXml();

        // write internal xml file:
        if (cmdline.isWriteInternalXml()) {
            String outFilename = xmlFilename + ".internal_xml";
            writeInternalXmlToFile(doc, outFilename);
            logger.info("internal xml output written to file. Exit.");
            System.exit(0);
        }

        fromDomToInternalDatastructure();
        logger.info("End XmlReaderSimInput.");
    }

    /**
     * Writes the internal xml after validation to file
     * 
     * @param doc2
     * @param outFilename
     *            the output file name
     */
    private void writeInternalXmlToFile(Document doc2, String outFilename) {
        PrintWriter writer = FileUtils.getWriter(outFilename);
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        try {
            logger.info("  write internal xml after validation to file \"" + outFilename + "\"");
            outputter.output(doc, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * From dom to internal data structure.
     */
    @SuppressWarnings("unchecked")
    private void fromDomToInternalDatastructure() {

        inputData.setProjectName(xmlFilename.substring(0, xmlFilename.indexOf(filenameEnding)));

        final Element root = doc.getRootElement();

        // -------------------------------------------------------

        final List<VehicleInput> vehicleInputData = new ArrayList<VehicleInput>();

        final List<Element> vehicleElements = root.getChild(XmlElementNames.DriverVehicleUnits).getChildren();
        for (final Element vehElem : vehicleElements) {
            vehicleInputData.add(new VehicleInputImpl(vehElem));
        }
        inputData.setVehicleInputData(vehicleInputData);

        // -------------------------------------------------------

        final SimulationInput simInput = new SimulationInputImpl(root.getChild(XmlElementNames.Simulation));
        inputData.setSimulationInput(simInput);
        
       
        
    }

    /**
     * Read and validate xml.
     */
    private void readAndValidateXml() {
        validate(getInputSourceFromFilename(xmlFilename));
        doc = getDocument(getInputSourceFromFilename(xmlFilename));
    }

    /**
     * Gets the Document.
     * 
     * @param inputSource
     *            the input source
     * @return the document
     */
    private Document getDocument(final InputSource inputSource) {
        try {
            final SAXBuilder builder = new SAXBuilder();
            builder.setIgnoringElementContentWhitespace(true);
            //TODO dtd from resources
//            builder.setEntityResolver(new EntityResolver() {
//                
//                @Override
//                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//                    InputStream is = XmlReaderSimInput.class.getResourceAsStream("/sim/multiModelTrafficSimulatorInput.dtd");
//                    InputSource input = new InputSource(is);
//                    return input;
//                }
//            });
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
     * Validates the Inputsource.
     * 
     * @param inputSource
     *            the input source
     */
    private void validate(InputSource inputSource) {
        // global flag !!! also used in errorHandler
        isValid = true;
        try {
            logger.debug("validate input ... ");
            final XMLReader myXMLReader = XMLReaderFactory.createXMLReader();
            myXMLReader.setFeature("http://xml.org/sax/features/validation", true);
            final DefaultHandler handler = new MyErrorHandler();
            myXMLReader.setErrorHandler(handler);
//            myXMLReader.setEntityResolver(new EntityResolver() {
//                
//                @Override
//                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//                    InputStream is = XmlReaderSimInput.class.getResourceAsStream("/sim/multiModelTrafficSimulatorInput.dtd");
//                    InputSource input = new InputSource(is);
//                    return input;
//                }
//            });
            myXMLReader.parse(inputSource);
        } catch (final SAXException e) {
            isValid = false;
        } catch (final IOException e) {
            isValid = false;
        }
        
        if (!isValid) {
            logger.error("xml input file {} is not well-formed or invalid ...Exit Simulation.", xmlFilename);
            System.exit(0);
        } else if (cmdline.isOnlyValidation()) {
            logger.info("xml input file is well-formed and valid. Exit Simulation as requested.");
            System.exit(0);
        }
    }

    /**
     * Gets the inputsource from filename.
     * 
     * @param filename
     *            the filename
     * @return the input
     */
    private InputSource getInputSourceFromFilename(String filename) {
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
         * Gets the info to the corresponding exception.
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