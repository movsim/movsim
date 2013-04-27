package org.movsim.xml;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

public class FileUnmarshaller<T> {

    private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

    /** Assure that only one loading/jaxb operation is active. */
    private static final Object SYNC_OBJECT = new Object();

    public final T load(StreamSource source, Class<T> clazz, Class<?> factory, URL xsdFile) throws JAXBException,
            SAXException {
        T result;
        synchronized (SYNC_OBJECT) {
            // TODO creating a JaxbContext is expensive, consider pooling.
            Unmarshaller unmarshaller = createUnmarshaller(factory, xsdFile);
            unmarshaller.setEventHandler(new XmlValidationEventHandler());
            result = unmarshaller.unmarshal(source, clazz).getValue();
        }
        return result;
    }
    
//    public final T load(InputSource source, Class<T> clazz, Class<?> factory, URL xsdFile) throws JAXBException,
//            SAXException, ParserConfigurationException {
//        T result;
//        synchronized (SYNC_OBJECT) {
//            SAXParserFactory spf = SAXParserFactory.newInstance();
//            spf.setXIncludeAware(true);
//            spf.setNamespaceAware(true);
//            XMLReader xr = spf.newSAXParser().getXMLReader();
//            SAXSource src = new SAXSource(xr, source);
//            Unmarshaller unmarshaller = createUnmarshaller(factory, xsdFile);
//            unmarshaller.setEventHandler(new XmlValidationEventHandler());
//            result = unmarshaller.unmarshal(src, clazz).getValue();
//        }
//        return result;
//    }

    public final T load(File file, Class<T> clazz, Class<?> factory, URL xsdFile) throws JAXBException, SAXException {
        Preconditions.checkNotNull(xsdFile);
        return load(new StreamSource(file), clazz, factory, xsdFile);
    }
    
//    public final T load(File file, Class<T> clazz, Class<?> factory, URL xsdFile) throws JAXBException, SAXException, ParserConfigurationException {
//        Preconditions.checkNotNull(file);
//        Preconditions.checkNotNull(xsdFile);
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(file);
//            InputSource inputSource = new InputSource(new InputStreamReader(fileInputStream));
//            inputSource.setEncoding("UTF-8");
//            return load(inputSource, clazz, factory, xsdFile);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private final Unmarshaller createUnmarshaller(final Class<?> objectFactoryClass, final URL xsdFile)
            throws JAXBException, SAXException {
        JAXBContext context = JAXBContext.newInstance(objectFactoryClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        if (unmarshaller == null) {
            throw new JAXBException("Created unmarshaller is null.");
        }
        unmarshaller.setSchema(getSchema(xsdFile));
        return unmarshaller;
    }

    private static Schema getSchema(final URL xsdFile) throws SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        return sf.newSchema(xsdFile);
    }

}
