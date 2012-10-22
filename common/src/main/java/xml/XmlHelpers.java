package xml;

import java.io.PrintWriter;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
