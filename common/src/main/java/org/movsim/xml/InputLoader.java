/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.movsim.autogen.Movsim;
import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.scenario.boundary.autogen.MovsimMicroscopicBoundaryConditions;
import org.movsim.scenario.initial.autogen.MovsimInitialConditions;
import org.movsim.scenario.vehicle.autogen.MovsimExternalVehicleControl;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InputLoader {

    private static final Logger LOG = LoggerFactory.getLogger(InputLoader.class);

    public enum XmlInput {
        MOVSIM_XPRJ(Movsim.class, "/schema/MovsimScenario.xsd"),

        MICRO_BOUNDARY_CONDITIONS(MovsimMicroscopicBoundaryConditions.class,
                "/schema/MovsimMicroscopicBoundaryConditions.xsd"),

        INITIAL_CONDITIONS(MovsimInitialConditions.class, "/schema/MovsimInitialConditions.xsd"),

        EXTERNAL_VEHICLE_CONTROL(MovsimExternalVehicleControl.class, "/schema/MovsimExternalVehicleControl.xsd"),

        XODR_ROADNETWORK(org.movsim.network.autogen.opendrive.OpenDRIVE.class, "/schema/OpenDRIVE_1.3.xsd");

        private final Class<?> factory;
        private final String xmlSchema;

        private XmlInput(Class<?> factory, String xmlSchema) {
            this.factory = factory;
            this.xmlSchema = xmlSchema;
        }

        public URL getUrl() {
            return XmlInput.class.getResource(xmlSchema);
        }

    }

    /**
     * @throws IllegalStateException
     */
    public static Movsim unmarshallMovsim(File xmlFile) {
        FileUnmarshaller<Movsim> fileUnmarshaller = new FileUnmarshaller<>();
        XmlInput xsdResourcen = XmlInput.MOVSIM_XPRJ;
        return fileUnmarshaller.load(xmlFile, Movsim.class, xsdResourcen.factory, xsdResourcen.getUrl());
    }

    /**
     * @throws IllegalStateException
     */
    public static MovsimInitialConditions unmarshallInitialConditions(File xmlFile) {
        FileUnmarshaller<MovsimInitialConditions> fileUnmarshaller = new FileUnmarshaller<>();
        XmlInput xsdResourcen = XmlInput.INITIAL_CONDITIONS;
        return fileUnmarshaller.load(xmlFile, MovsimInitialConditions.class, xsdResourcen.factory,
                xsdResourcen.getUrl());
    }

    /**
     * @throws IllegalStateException
     */
    public static MovsimMicroscopicBoundaryConditions unmarshallMicroBoundaryConditions(File xmlFile) {
        FileUnmarshaller<MovsimMicroscopicBoundaryConditions> fileUnmarshaller = new FileUnmarshaller<>();
        XmlInput xsdResourcen = XmlInput.MICRO_BOUNDARY_CONDITIONS;
        return fileUnmarshaller.load(xmlFile, MovsimMicroscopicBoundaryConditions.class, xsdResourcen.factory,
                xsdResourcen.getUrl());
    }

    /**
     * @throws IllegalStateException
     */
    public static MovsimExternalVehicleControl unmarshallExternalVehicleControl(File xmlFile) {
        FileUnmarshaller<MovsimExternalVehicleControl> fileUnmarshaller = new FileUnmarshaller<>();
        XmlInput xsdResourcen = XmlInput.EXTERNAL_VEHICLE_CONTROL;
        return fileUnmarshaller.load(xmlFile, MovsimExternalVehicleControl.class, xsdResourcen.factory,
                xsdResourcen.getUrl());
    }

    /**
     * @throws IllegalStateException
     */
    public static OpenDRIVE unmarshallOpenDriveNetwork(final File xmlFile) {
        FileUnmarshaller<org.movsim.network.autogen.opendrive.OpenDRIVE> fileUnmarshaller = new FileUnmarshaller<>();
        XmlInput xsdResourcen = XmlInput.XODR_ROADNETWORK;
        return fileUnmarshaller.load(xmlFile, org.movsim.network.autogen.opendrive.OpenDRIVE.class,
                xsdResourcen.factory, xsdResourcen.getUrl());
    }

    /**
     * writes all movsim xsd files and the xodr xsd to the current working directory.
     * 
     */
    public static void writeXsdToFile() throws IOException {
        for (XmlInput input : XmlInput.values()) {
            String filename = new File(input.xmlSchema).getName();
            FileUtils.writeStreamToFile(filename, input.getUrl().openStream());
            LOG.info("wrote file={}", filename);
        }
    }

}
