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
package org.movsim.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.VehiclesInput;
import org.movsim.input.model.vehicle.consumption.ConsumptionInput;
import org.movsim.utilities.FileNameUtils;
import org.movsim.utilities.FileUtils;
import org.movsim.xml.XmlHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Parse the MovSim XML file to add the simulation components eg network filename, vehicles and vehicle models,
 * traffic composition, traffic sources etc.
 * 
 */
public class XmlReaderSimInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(XmlReaderSimInput.class);

    private final InputData inputData;

    private final File xmlFile;

    private Document doc;

    // dtd from resources. do *not* use the File.separator character
    private final String dtdFilename;

    private InputStream appletinputstream;

    private InputSource appletresource;

    private final ProjectMetaData projectMetaData;

    public static void parse(ProjectMetaData projectMetaData, InputData inputData) {
        XmlReaderSimInput xmlReaderSimInput = new XmlReaderSimInput(projectMetaData, inputData);

        if (projectMetaData.isWriteInternalXml()) {
            final String outFilename = projectMetaData.getProjectName() + "_internal.xml";
            XmlHelpers.writeInternalXmlToFile(xmlReaderSimInput.doc, outFilename);
            logger.info("internal xml output written to file {}. Exit.", outFilename);
            System.exit(0);
        }
    }

    /**
     * Instantiates a new xml reader to parse and validate the simulation input.
     * 
     * @param inputData
     *            the input data
     */
    private XmlReaderSimInput(ProjectMetaData projectMetaData, InputData inputData) {
        this.dtdFilename = projectMetaData.getDtdFilenameWithPath();
        this.projectMetaData = projectMetaData;
        this.inputData = inputData;
        this.xmlFile = projectMetaData.getXmlInputFile();

        if (!projectMetaData.isParseFromInputstream() && !projectMetaData.isXmlFromResources() && !xmlFile.exists()) {
            logger.error("XML file {} does not exist. Exit Simulation.", xmlFile.getAbsoluteFile());
            System.exit(1);
        }

        logger.info("Begin parsing: " + xmlFile);

        if (projectMetaData.isParseFromInputstream()) {
            readXmlFromInputstream();
        } else if (projectMetaData.isXmlFromResources()) {
            readAndValidateXmlFromResources();
        } else {
            readAndValidateXmlFromFileName();
        }

        if (projectMetaData.isOnlyValidation()) {
            logger.info("xml input file is well-formed and valid. Exit Simulation as requested.");
            System.exit(0);
        }

        fromDomToInternalDatastructure();
        logger.info("End XmlReaderSimInput.");

    }

    private void fromDomToInternalDatastructure() {
        final Element root = doc.getRootElement();

        if (!projectMetaData.isParseFromInputstream()) {
            parseNetworkFilename(root, "network_filename");
            parseConsumptionFilename(root, "consumption_filename");
        }

        final SimulationInput simInput = new SimulationInput(root.getChild(XmlElementNames.Simulation));
        inputData.setSimulationInput(simInput);

        final Element vehiclesElem = root.getChild(XmlElementNames.DriverVehicleUnits);
        final VehiclesInput vehiclesInput = new VehiclesInput(vehiclesElem);
        inputData.setVehiclesInput(vehiclesInput);

        final ConsumptionInput fuelConsumptionInput = new ConsumptionInput(root.getChild(XmlElementNames.Consumption));

        inputData.setFuelConsumptionInput(fuelConsumptionInput);

        if (projectMetaData.hasConsumptionFilename()) {
            System.out.println("todo: parse consumption  = " + projectMetaData.getConsumptionFilename());
        }
    }

    // TODO refactor and extract common functions
    private void parseNetworkFilename(Element root, String attributeName) {
        String filename = root.getAttributeValue(attributeName);
        if (projectMetaData.isXmlFromResources()) {
            projectMetaData.setXodrNetworkFilename(filename.substring(filename.lastIndexOf("/") + 1));
            projectMetaData.setXodrPath(filename.substring(0, filename.lastIndexOf("/") + 1));
        } else {
            String relativePath;
            relativePath = checkIfAttributeHasPath(filename);

            if (relativePath.equals("")) {
                filename = checkIfFileIsInTheSameDirectoryAsTheMovsimXml(filename);
            }

            if (!FileUtils.fileExists(filename)) {
                logger.error("Problem with file {}. Please check. Exit.", filename);
                System.exit(-1); // TODO check from resources
            }

            projectMetaData.setXodrNetworkFilename(FileNameUtils.getName(filename));
            projectMetaData.setXodrPath(FileUtils.getCanonicalPathWithoutFilename(filename));
        }
    }

    private void parseConsumptionFilename(Element root, String attributeName) {
        String filename = root.getAttributeValue(attributeName);
        if (filename == null) {
            return;
        }
        if (projectMetaData.isXmlFromResources()) {
            projectMetaData.setConsumptionFilename(filename.substring(filename.lastIndexOf("/") + 1));
            projectMetaData.setConsumptionPath(filename.substring(0, filename.lastIndexOf("/") + 1));
        } else {
            String relativePath;
            relativePath = checkIfAttributeHasPath(filename);

            if (relativePath.equals("")) {
                filename = checkIfFileIsInTheSameDirectoryAsTheMovsimXml(filename);
            }

            if (!FileUtils.fileExists(filename)) {
                logger.error("Problem with file {}. Please check. Exit.", filename);
                System.exit(-1); // TODO check from resources
            }

            projectMetaData.setConsumptionFilename(FileNameUtils.getName(filename));
            projectMetaData.setConsumptionPath(FileUtils.getCanonicalPathWithoutFilename(filename));
        }
    }

    private String checkIfFileIsInTheSameDirectoryAsTheMovsimXml(String filename) {
        final String fullFile = projectMetaData.getPathToProjectXmlFile() + filename;
        logger.info("check path : {}", fullFile);
        if (FileUtils.fileExists(fullFile)) {
            logger.info("network file {} exists!", filename);
        } else {
            logger.error(
                    "Please provide the corresponding file={}. Either in the same directory as the movsim xml or provide the path and file name in the attribute 'network_filename' in the root tag. If you have done that, check the spelling!",
                    filename);
            System.exit(-1);
        }
        return fullFile;
    }

    /**
     * @param networkFileName
     * @return relativePath
     */
    private static String checkIfAttributeHasPath(String networkFileName) {
        String relativePath;
        if (networkFileName.lastIndexOf(File.separator) == -1) {
            relativePath = "";
        } else {
            relativePath = networkFileName.substring(0, networkFileName.lastIndexOf(File.separator));
            System.out.println("relative path: " + relativePath);
        }
        return relativePath;
    }

    private void validate(InputSource inputSource) {
        InputStream inputStream = XmlReaderSimInput.class.getResourceAsStream(dtdFilename);
        boolean valid = XmlHelpers.validate(inputSource, inputStream);
        if (!valid) {
            logger.error("xml input file {} is not well-formed or invalid ...Exit Simulation.", xmlFile);
            System.exit(0);
        }
    }

    /**
     * Read and validate xml.
     */
    private void readAndValidateXmlFromFileName() {
        validate(FileUtils.getInputSourceFromFilename(xmlFile));
        doc = getDocument(FileUtils.getInputSourceFromFilename(xmlFile), dtdFilename);
    }

    private void readXmlFromInputstream() {
        InputSource inputSource = new InputSource(projectMetaData.getMovsimXml());
        validate(inputSource);
        doc = getDocument(inputSource, dtdFilename);
    }

    /**
     * Validates and reads xml from resources.
     */
    private void readAndValidateXmlFromResources() {
        appletinputstream = XmlReaderSimInput.class.getResourceAsStream(xmlFile.getAbsolutePath());
        appletresource = new InputSource(appletinputstream);
        validate(appletresource);
        doc = getDocument(appletresource, dtdFilename);
    }

    /**
     * Gets the Document.
     * 
     * @param inputSource
     *            the input source
     * @return the document
     */
    private static Document getDocument(final InputSource inputSource, final String dtdFilename) {

        final Document doc = (Document) AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {

                try {
                    final SAXBuilder builder = new SAXBuilder();
                    builder.setIgnoringElementContentWhitespace(true);
                    builder.setEntityResolver(new EntityResolver() {

                        @Override
                        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                                IOException {
                            final InputStream is = XmlReaderSimInput.class.getResourceAsStream(dtdFilename);
                            final InputSource input = new InputSource(is);
                            return input;
                        }
                    });
                    final Document document = builder.build(inputSource);
                    return document;
                } catch (final JDOMException e) {
                    e.printStackTrace();
                    System.exit(-1);
                    return null;
                } catch (final Exception e) {
                    e.printStackTrace();
                    return null;
                }

            }
        });

        return doc;
    }

    // /**
    // * The Inner Class MyErrorHandler.
    // *
    // * uses global isValid flag
    // */
    // class MyErrorHandler extends DefaultHandler {
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException )
    // */
    // @Override
    // public void warning(SAXParseException e) throws SAXException {
    // logger.warn(getInfo(e));
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException )
    // */
    // @Override
    // public void error(SAXParseException e) throws SAXException {
    // logger.error(getInfo(e));
    // isValid = false;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax. SAXParseException)
    // */
    // @Override
    // public void fatalError(SAXParseException e) throws SAXException {
    // logger.error(getInfo(e));
    // isValid = false;
    // }
    //
    // /**
    // * Gets the info to the corresponding exception.
    // *
    // * @param e
    // * the exception
    // * @return the info
    // */
    // private String getInfo(SAXParseException e) {
    // final StringBuilder stringb = new StringBuilder();
    // stringb.append("   Public ID: " + e.getPublicId());
    // stringb.append("   System ID: " + e.getSystemId());
    // stringb.append("   Line number: " + e.getLineNumber());
    // stringb.append("   Column number: " + e.getColumnNumber());
    // stringb.append("   Message: " + e.getMessage());
    // return stringb.toString();
    // }
    // }

}