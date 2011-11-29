package org.movsim.output.fileoutput;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.vehicles.VehiclePrototype;

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
     * @param prototypes
     *            the prototypes
     */
    public static void writeFundamentalDiagrams(HashMap<String, VehiclePrototype> prototypes) {
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();

            final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
            final String outputPath = projectMetaData.getOutputPath();
            final String filename = outputPath + File.separator + projectMetaData.getProjectName() + ".fund_" + key
                    + ".csv";
            final VehiclePrototype proto = prototypes.get(key);
            if (proto.fraction() > 0) {
                // avoid writing fundDia of "obstacles"
                proto.writeFundamentalDiagram(filename);
            }
        }
    }
}
