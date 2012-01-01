package org.movsim.output.fileoutput;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.longitudinalmodel.equilibrium.EquilibriumProperties;
import org.movsim.utilities.FileUtils;

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
        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        final String path = projectMetaData.getOutputPath();
        final String baseFilename = projectMetaData.getProjectName();
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            final VehiclePrototype proto = prototypes.get(key);
            if (proto.fraction() > 0) {
                // avoid writing fundDia of "obstacles"
                final String filename = path + File.separator + baseFilename + ".fund_" + key + ".csv";
                final EquilibriumProperties equilibriumProperties = proto.getEquilibriumProperties();
                writeFundamentalDiagram(equilibriumProperties, filename);
            }
        }
    }

    /**
     * Write output.
     * 
     * @param filename
     *            the filename
     */
    private static void writeFundamentalDiagram(EquilibriumProperties equilibriumProperties, String filename) {
        final PrintWriter fstr = FileUtils.getWriter(filename);
        fstr.printf(FileOutputBase.COMMENT_CHAR + " rho at max Q = %8.3f%n", 1000 * equilibriumProperties.getRhoQMax());
        fstr.printf(FileOutputBase.COMMENT_CHAR + " max Q        = %8.3f%n", 3600 * equilibriumProperties.getQMax());
        fstr.printf(FileOutputBase.COMMENT_CHAR + " rho[1/km],  s[m],vEq[km/h], Q[veh/h]%n");
        final int count = equilibriumProperties.getVEqCount();
        for (int i = 0; i < count; i++) {
            final double rho = equilibriumProperties.getRho(i);
            final double s = equilibriumProperties.getNetDistance(rho);
            final double vEq = equilibriumProperties.getVEq(i);
            fstr.printf("%8.2f, %8.2f, %8.2f, %8.2f%n", 1000 * rho, s, 3.6 * vEq, 3600 * rho * vEq);
        }
        fstr.close();
    }
}
