package org.movsim.consumption.input;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.movsim.consumption.ConsumptionMain;
import org.movsim.consumption.autogen.MovsimConsumption;
import org.movsim.xml.FileUnmarshaller;
import org.xml.sax.SAXException;

public class ConsumptionXmlLoader {

    private static final Class<?> CONSUMPTION_FACTORY = org.movsim.consumption.autogen.MovsimConsumption.class;

    private static final String CONSUMPTION_XML_SCHEMA = "/schema/MovsimConsumption.xsd";

    private static final URL CONSUMPTION_XSD_URL = ConsumptionMain.class.getResource(CONSUMPTION_XML_SCHEMA);

    public MovsimConsumption validateAndLoadOpenConsumptionInput(final File xmlFile) throws JAXBException, SAXException {
        return new FileUnmarshaller<MovsimConsumption>().load(xmlFile, MovsimConsumption.class, CONSUMPTION_FACTORY,
                CONSUMPTION_XSD_URL);
    }

}
