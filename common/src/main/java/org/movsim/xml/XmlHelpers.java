package org.movsim.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlHelpers {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(XmlHelpers.class);

    /**
     * Writes the internal xml after validation to file.
     * 
     * @param localDoc
     *            the local doc
     * @param outFilename
     *            the output file name
     */
    public static void writeInternalXmlToFile(Document localDoc, String outFilename) {
        final PrintWriter writer = FileUtils.getWriter(outFilename);
        final XMLOutputter outputter = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setIndent("    ");
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        outputter.setFormat(format);
        try {
            logger.info("  write internal xml after validation to file \"" + outFilename + "\"");
            outputter.output(localDoc, writer);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates the Inputsource.
     * 
     * @param inputSource
     *            the input source
     */
    public static boolean validate(final InputSource inputSource, final InputStream is) {
        // global flag !!! also used in errorHandler
        boolean isValid = true;

        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {

                try {
                    logger.debug("validate input ... ");
                    final XMLReader myXMLReader = XMLReaderFactory.createXMLReader();
                    myXMLReader.setFeature("http://xml.org/sax/features/validation", true);
                    final DefaultHandler handler = new MyErrorHandler();
                    myXMLReader.setErrorHandler(handler);

                    // overriding dtd source from xml file with internal dtd
                    // from jar

                    myXMLReader.setEntityResolver(new EntityResolver() {

                        @Override
                        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
                                IOException {
                            final InputSource input = new InputSource(is);
                            return input;
                        }
                    });

                    myXMLReader.parse(inputSource);
                } catch (final SAXException e) {
                    logger.error(e.getMessage());
                    // isValid = false;
                } catch (final Exception e) {
                    logger.error(e.getMessage());
                    // isValid = false;
                }

                return null;
            }

            class MyErrorHandler extends DefaultHandler {

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException )
                 */
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    logger.warn(getInfo(e));
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException )
                 */
                @Override
                public void error(SAXParseException e) throws SAXException {
                    logger.error(getInfo(e));
                    System.exit(-1);
                    // isValid = false;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax. SAXParseException)
                 */
                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    logger.error(getInfo(e));
                    System.exit(-1);
                    // isValid = false;
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
        });
        return isValid;
    }
}
