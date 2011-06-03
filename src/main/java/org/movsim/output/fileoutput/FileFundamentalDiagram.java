package org.movsim.output.fileoutput;

import java.util.HashMap;
import java.util.Iterator;

import org.movsim.simulator.vehicles.VehiclePrototype;

public class FileFundamentalDiagram {

    private FileFundamentalDiagram(){
    }
    
    public static void writeFundamentalDiagrams(String projectName, HashMap<String, VehiclePrototype> prototypes) {
        final Iterator<String> it = prototypes.keySet().iterator();
        while (it.hasNext()) {
            final String key = it.next();
            final String filename = projectName + ".fund_" + key + ".csv";
            final VehiclePrototype proto = prototypes.get(key);
            if (proto.fraction() > 0) {
                // avoid writing fundDia of "obstacles"
                proto.writeFundamentalDiagram(filename);
            }
        }
    }
}
