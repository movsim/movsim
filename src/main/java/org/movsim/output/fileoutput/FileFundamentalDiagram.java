/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.output.fileoutput;

import java.util.HashMap;
import java.util.Iterator;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.vehicles.VehiclePrototype;

// TODO: Auto-generated Javadoc
/**
 * The Class FileFundamentalDiagram.
 */
public class FileFundamentalDiagram {

    /**
     * Instantiates a new file fundamental diagram.
     */
    private FileFundamentalDiagram() {
    }

    /**
     * Write fundamental diagrams.
     * 
     * @param projectName
     *            the project name
     * @param prototypes
     *            the prototypes
     */
    public static void writeFundamentalDiagrams(String projectName, HashMap<String, VehiclePrototype> prototypes) {
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();

            final String outputPath = ProjectMetaData.getInstance().getOutputPath();
            final String filename = projectName + ".fund_" + key + ".csv";
            System.out.println("projectName: "+ projectName);
            final VehiclePrototype proto = prototypes.get(key);
            if (proto.fraction() > 0) {
                // avoid writing fundDia of "obstacles"
                proto.writeFundamentalDiagram(filename);
            }
        }
    }
}
