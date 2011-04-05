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


public class XmlReaderSimInput {

    final static Logger logger = LoggerFactory.getLogger(XmlReaderSimInput.class);

    // private String xsdFileName = "sim/validateXml.xsd";
    private final String dtdFilename = "multiModelTrafficSimulatorInput.dtd";

    private boolean isValid = false;

    private InputDataImpl inputData;

    private String xmlFilename;

    private Document doc;

    public XmlReaderSimInput(String xmlFilename, InputDataImpl inputData) {
        this.xmlFilename = xmlFilename;
        this.inputData = inputData;

        if (!FileUtils.fileExists(xmlFilename)) {
            logger.error("XML File does not exist. Exit Simulation.");
            System.exit(1);
        }

        logger.info("Begin Parsing: " + xmlFilename);
        readAndValidateXml();
        
        // System.exit(-1);

        logger.info("begin");
        fromDomToBean();

        logger.info("end XmlReaderSimInput.");
    }

    @SuppressWarnings("unchecked")
    private void fromDomToBean() {

        inputData.setProjectName(xmlFilename.substring(0, xmlFilename.indexOf(".xml")));

        Element root = doc.getRootElement();

        // -------------------------------------------------------

        List<VehicleInput> vehicleInputData = new ArrayList<VehicleInput>();

        List<Element> vehicleElements = root.getChild("DRIVER_VEHICLE_UNITS").getChildren();
        for (Element vehElem : vehicleElements) {
            vehicleInputData.add(new VehicleInputImpl(vehElem));
        }
        inputData.setVehicleInputData(vehicleInputData);

        // -------------------------------------------------------

        // Output Tag
        OutputInput outputInput = new OutputInputImpl(root.getChild("OUTPUT"));
        inputData.setOutputInput(outputInput);

        // -------------------------------------------------------

        SimulationInput simInput = new SimulationInputImpl(root.getChild("SIMULATION"));
        inputData.setSimulationInput(simInput);
    }

    /**
	 * 
	 */
    private void readAndValidateXml() {

// final boolean doValidation = false; // arne !!
// SAXBuilder builder = new SAXBuilder(doValidation);

        // validation against internal dtd file
        // change doctype if necessary

        isValid = true;
        
        // TODO dtd validation !!
        //validate(getInput(xmlFilename));

        if (!isValid) {
            logger.error("xml input file {} is not well-formed or invalid ... exit!\n", xmlFilename);
            System.exit(0);
        }

        System.out.println("now parse input file ...");

        doc = getDocument(getInput(xmlFilename));
        validate(getInput(xmlFilename));

// try {
// if(doValidation){
// // turns on Schema Validation
// builder.setFeature("http://apache.org/xml/features/validation/schema", true);
//            
// // This gives the XML Schema to be used.
// builder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", xsdFileName);
// }
// SimpleErrorHandler errorHandler = new SimpleErrorHandler();
// builder.setErrorHandler(errorHandler);
//
// doc = builder.build(new File(xmlFilename));
// if(doValidation) logger.info((xmlFilename + " was parsed and verified against " + xsdFileName + "!"));
// if (errorHandler.isError()) {
// logger.error(("The XML File is not valid. Exit Simulation."));
// System.exit(1);
// }
// } catch (Exception cause) {
// logger.error((cause.toString()));
// System.exit(1);
// }
    }

    private Document getDocument(InputSource inputSource) {
        try {
            SAXBuilder builder = new SAXBuilder();
            // builder.setEntityResolver(new LocalDtdResolver(dtdFilename));
            // builder.setValidation(true); // requires a "dtd" file !
            builder.setIgnoringElementContentWhitespace(true);
            // Document doc = builder.build(inputFile);
            Document doc = builder.build(inputSource);
            System.out.println();
            return doc;
        } catch (JDOMException e) {
            // System.err.println(sFileName + " is not valid .... ");
            e.printStackTrace();
            System.exit(-1);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            // msgError("Error loading XML-File "+sFileName);
            return null;
        }
    }

    private void validate(InputSource inputSource) {
        isValid = true; // global flag !!! also used in errorHandler
        // no abort if file is invalid ...
        try {
            logger.debug("validate input ... ");
            XMLReader myReader = XMLReaderFactory.createXMLReader();
            myReader.setFeature("http://xml.org/sax/features/validation", true);
            DefaultHandler handler = new MyErrorHandler();
            myReader.setErrorHandler(handler);
//            myReader.setEntityResolver(new LocalDtdResolver(dtdFilename));
            // File inputFile = new File(sFileName);
            // InputSource inputSource = new InputSource(new java.io.FileInputStream(inputFile) );
            myReader.parse(inputSource);
        } catch (SAXException e) {
            System.err.println(e.getMessage());
            isValid = false;
        } catch (IOException e) {
            System.out.println(e.toString() + "\n");
            isValid = false;
        }
    }

    private InputSource getInput(String filename) {
        final File inputFile = new File(filename);
        InputSource inputSource = null;
        try {
            inputSource = new InputSource(new FileInputStream(inputFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return inputSource;
    }

// inner class uses "isValid" flag

    class MyErrorHandler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            System.out.println("Warning: ");
            printInfo(e);
        }

        public void error(SAXParseException e) throws SAXException {
            System.out.println("Error: ");
            printInfo(e);
        }

        public void fatalError(SAXParseException e) throws SAXException {
            System.out.println("Fatal error: ");
            printInfo(e);
        }

        private void printInfo(SAXParseException e) {
            isValid = false;
            System.out.println("   Public ID: " + e.getPublicId());
            System.out.println("   System ID: " + e.getSystemId());
            System.out.println("   Line number: " + e.getLineNumber());
            System.out.println("   Column number: " + e.getColumnNumber());
            System.out.println("   Message: " + e.getMessage());
        }
    }

}