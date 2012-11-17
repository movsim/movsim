package org.movsim.consumption.test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.movsim.consumption.jaxb.Car;
import org.movsim.consumption.jaxb.Consumption;
import org.movsim.consumption.jaxb.ObjectFactory;


public class TestJabx {

    private ObjectFactory of;

    private Consumption consumption;

    public TestJabx() {
        of = new ObjectFactory();
        consumption = of.createConsumption();
    }

    public void make(String t, String l) {
        Car car = of.createCar();
        car.setCdValue("0.3");
        //grList.getCONSUMPTIONMODEL().a.getGreeting().add(g);
    }

    public void marshal() {
        try {
            //JAXBElement<Consumption> consumption = of.createConsumption();
            JAXBContext jc = JAXBContext.newInstance("hello");
            Marshaller m = jc.createMarshaller();
            m.marshal(consumption, System.out);
        } catch (JAXBException jbe) {
            // ...
        }
    }

}
