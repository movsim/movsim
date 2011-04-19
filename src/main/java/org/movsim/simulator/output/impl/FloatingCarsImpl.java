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
package org.movsim.simulator.output.impl;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.output.FloatingCars;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class FloatingCarsImpl.
 */
public class FloatingCarsImpl implements FloatingCars {
    final static Logger logger = LoggerFactory.getLogger(FloatingCarsImpl.class);

    private final String path = "./";
    private final String projectName;
    private final String endingFile = ".car";

    private final DecimalFormatSymbols us = new DecimalFormatSymbols(Locale.US);
    private final DecimalFormat fileFormat = new DecimalFormat("000000", us);

    private final HashMap<Integer, PrintWriter> hashMap;

    private final int nDtOut;

    // private double timeOffset;

    // TODO geeignete Datenstruktur fuer GUI

    /**
     * Instantiates a new floating cars impl.
     * 
     * @param projectName
     *            the project name
     * @param writeOutput
     *            the write output
     * @param input
     *            the input
     */
    public FloatingCarsImpl(String projectName, boolean writeOutput, FloatingCarInput input) {
        logger.debug("Cstr. FloatingCars");
        this.projectName = projectName;

        this.nDtOut = input.getNDt();
        // timeOffset = -nDtOut; // write t==0

        // TODO: not yet implemented.
        // int dn = input.getDn();
        // double percOut = input.getPercOut();
        //

        final String regex = projectName + "[.]\\d+" + endingFile;
        FileUtils.deleteFileList(path, regex);

        hashMap = new HashMap<Integer, PrintWriter>(149, 0.75f);

        final List<Integer> fcdList = input.getFloatingCars();
        for (final Integer i : fcdList) {
            addFCD(i);
        }

    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // update
    // write output in each update step
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.output.FloatingCars#update(int, double, double,
     * org.movsim.simulator.vehicles.VehicleContainer)
     */
    @Override
    public void update(int itime, double time, double timestep, VehicleContainer vehContainer) {

        //
        //
        // if ((time - timeOffset + Constants.SMALL_VALUE) >= dtOut) {
        // timeOffset = time;
        // }
        // else{
        // return; // no update in this call
        // }
        //

        if (itime % nDtOut != 0)
            return; // no update

        // logger.debug("update FloatingCars: itime={}", itime);

        final List<Vehicle> vehicles = vehContainer.getVehicles();

        for (final Vehicle veh : vehicles) {
            if (!veh.isFromOnramp()) {
                // only mainroad vehicles
                final int vehNumber = veh.getVehNumber();
                // TODO: further floating cars ...
                // if( !hashMap.containsKey(vehNumber) ){
                // addFCD(vehNumber);
                // }
                // update existing floating cars
                if (hashMap.containsKey(vehNumber)) {
                    final Vehicle frontVeh = vehContainer.getLeader(veh);
                    writeData(time, veh, frontVeh, hashMap.get(vehNumber));
                }
            }
        } // of for
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.output.FloatingCars#closeAllFiles()
     */
    @Override
    public void closeAllFiles() {
        if (!hashMap.isEmpty()) {
            final Iterator<Integer> it = hashMap.keySet().iterator();
            while (it.hasNext()) {
                final Integer id = it.next();
                hashMap.get(id).close();
                it.remove();
            }
        }
    }

    /**
     * Creates the file name.
     * 
     * @param i
     *            the i
     * @param ending
     *            the ending
     * @return the string
     */
    private String createFileName(int i, String ending) {
        final String filename = path + projectName + "." + fileFormat.format(i) + ending;
        return (filename);
    }

    /**
     * Write data.
     * 
     * @param time
     *            the time
     * @param veh
     *            the veh
     * @param frontVeh
     *            the front veh
     * @param fstr
     *            the fstr
     */
    private void writeData(double time, Vehicle veh, Vehicle frontVeh, PrintWriter fstr) {
        fstr.printf("%6.2f %6.3f %6.4f %7.5f %7.5f %7.5f %7.5f %6.2f %4d%n", time, veh.position(), veh.speed(),
                veh.acc(), veh.netDistance(frontVeh), veh.relSpeed(frontVeh), veh.accModel(),
                veh.distanceToTrafficlight(), veh.getIntLane());
        fstr.flush();
    }

    // header information
    /**
     * Adds the fcd.
     * 
     * @param vehNumber
     *            the veh number
     */
    private void addFCD(int vehNumber) {
        final String filename = createFileName(vehNumber, endingFile);
        final PrintWriter fstr = FileUtils.getWriter(filename);
        hashMap.put(vehNumber, fstr);
        fstr.println(Constants.COMMENT_CHAR
                + " t[s] x[m] v[m/s] acc[m/s^2] s[m] dv[m/s] accModel[m/s^2] distToTrafficlight[m]");
        fstr.flush();
    }

}
