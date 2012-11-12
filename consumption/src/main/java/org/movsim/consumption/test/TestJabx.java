package org.movsim.consumption.test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.movsim.consumption.autogen.GreetingListType;
import org.movsim.consumption.autogen.GreetingType;
import org.movsim.consumption.autogen.ObjectFactory;

public class TestJabx {

    private ObjectFactory of;

    private GreetingListType grList;

    public TestJabx() {
        of = new ObjectFactory();
        grList = of.createGreetingListType();
    }

    public void make(String t, String l) {
        GreetingType g = of.createGreetingType();
        g.setText(t);
        g.setLanguage(l);
        grList.getGreeting().add(g);
    }

    public void marshal() {
        try {
            JAXBElement<GreetingListType> gl = of.createGreetings(grList);
            JAXBContext jc = JAXBContext.newInstance("hello");
            Marshaller m = jc.createMarshaller();
            m.marshal(gl, System.out);
        } catch (JAXBException jbe) {
            // ...
        }
    }

}
