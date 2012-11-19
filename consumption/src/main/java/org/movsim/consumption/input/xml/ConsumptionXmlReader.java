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
package org.movsim.consumption.input.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.movsim.consumption.input.ConsumptionMetadata;
import org.movsim.utilities.FileUtils;
import org.movsim.xml.XmlHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ConsumptionXmlReader {

    final static Logger logger = LoggerFactory.getLogger(ConsumptionXmlReader.class);

    private final ConsumptionInputData inputData;

    private Document doc;

    // dtd from resources. do *not* use the File.separator character
    private final String dtdFilename;

    public static void parse(ConsumptionMetadata metaData, ConsumptionInputData inputData) {
        ConsumptionXmlReader xmlReader = new ConsumptionXmlReader(inputData);

        if (metaData.isWriteInternalXml()) {
            final String outFilename = metaData.getProjectName() + "_internal.xml";
            XmlHelpers.writeInternalXmlToFile(xmlReader.doc, outFilename);
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
    private ConsumptionXmlReader(ConsumptionInputData inputData) {
        this.dtdFilename = ConsumptionMetadata.getInstance().getDtdFilenameWithPath();
        this.inputData = inputData;

        File xmlFile = ConsumptionMetadata.getInstance().getXmlInputFile();
        if (!ConsumptionMetadata.getInstance().isParseFromInputstream() && !ConsumptionMetadata.getInstance().isXmlFromResources() && !xmlFile.exists()) {
            logger.error("XML file {} does not exist. ", xmlFile.getAbsoluteFile());
            System.exit(1);
        }

        logger.info("Begin parsing: " + xmlFile);

        if (ConsumptionMetadata.getInstance().isParseFromInputstream()) {
            throw new IllegalStateException("not yet handled");
        } else if (ConsumptionMetadata.getInstance().isXmlFromResources()) {
            throw new IllegalStateException("not yet handled");
        } else {
            readAndValidateXmlFromFileName();
        }

        if (ConsumptionMetadata.getInstance().isOnlyValidation()) {
            logger.info("xml input file is well-formed and valid. Exit Simulation as requested.");
            System.exit(0);
        }

        fromDomToInternalDatastructure();
        logger.info("End ConsumptionXmlReader.");

    }

    private void fromDomToInternalDatastructure() {
        final Element root = doc.getRootElement();
        System.out.println("root=" + root.toString());
        final ConsumptionInput consumptionInput = new ConsumptionInput(root.getChild(XmlElementNames.Consumption));

        inputData.setConsumptionInput(consumptionInput);
    }

  
    private String checkIfFileIsInTheSameDirectoryAsTheMovsimXml(String filename) {
        final String fullFile = ConsumptionMetadata.getInstance().getPathToConsumptionFile() + filename;
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

    
    private void validate(InputSource inputSource, InputStream dtdInputStream) {
        boolean valid = XmlHelpers.validate(inputSource, dtdInputStream);
        if (!valid) {
            File xmlFile = ConsumptionMetadata.getInstance().getXmlInputFile();
            logger.error("xml input file {} is not well-formed or invalid ...Exit Simulation.", xmlFile);
            System.exit(0);
        }
    }

    /**
     * Read and validate xml.
     */
    private void readAndValidateXmlFromFileName() {
        File xmlFile = ConsumptionMetadata.getInstance().getXmlInputFile();
        InputStream dtdStream = ConsumptionXmlReader.class.getResourceAsStream(dtdFilename);
        validate(FileUtils.getInputSourceFromFilename(xmlFile), dtdStream);
        doc = getDocument(FileUtils.getInputSourceFromFilename(xmlFile), dtdFilename);
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
                            final InputStream is = ConsumptionXmlReader.class.getResourceAsStream(dtdFilename);
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