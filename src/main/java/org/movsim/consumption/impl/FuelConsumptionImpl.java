package org.movsim.consumption.impl;

import java.io.PrintWriter;
import java.util.Locale;

import org.movsim.consumption.CarModel;
import org.movsim.consumption.EngineModel;
import org.movsim.consumption.FuelConstants;
import org.movsim.consumption.FuelConsumption;
import org.movsim.input.model.consumption.ConsumptionModelInput;
import org.movsim.utilities.impl.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FuelConsumptionImpl implements FuelConsumption {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(FuelConsumptionImpl.class);

    // if cons(m^3/(Ws)) higher, point (f,pe) out of Bounds
    private final double LIMIT_SPEC_CONS = 900 * FuelConstants.CONVERSION_GRAMM_PER_KWH_TO_SI; // 900=3-4 times the minimum

    // extremely high flow in motor regimes that cannot be reached
    private final double POW_ERROR = 1e7; // 10000 KW

    private final double FUELFLOW_ERROR = POW_ERROR * LIMIT_SPEC_CONS;

    // ###############################

    private CarModel carModel;

    private EngineModel engineModel;

    public FuelConsumptionImpl(ConsumptionModelInput input) {
        carModel = new CarModelImpl(input.getCarData());
        engineModel = new EngineModelImpl(input.getEngineData(), carModel);

        // TODO
//        final String label = "carConsumption";
//        final String project = "sim/test."+label;
//        writeOutput(true, project);
    }

    public double fuelflowError() {
        return FUELFLOW_ERROR;
    }

    // Instantaneous fuel consumption (l/(100 km))
    private double getInstConsumption100km(double v, double acc, int gear, boolean withJante) {
        final int gearIndex = gear - 1;
        return (1e8 * getFuelFlow(v, acc, gearIndex, withJante) / Math.max(v, 0.001));
    }

    private double getFuelFlow(double v, double acc, int gearIndex, boolean withJante) {

        final double fMot = engineModel.getEngineFrequency(v, gearIndex);

        // final double forceMech=getForceMech(v,acc); // can be <0
        final double powMech = v * carModel.getForceMech(v, acc); // can be <0

        final double powMechEl = powMech + carModel.getElectricPower();// can be <0

        double fuelFlow = FUELFLOW_ERROR;

        if (engineModel.isfMotPossible(v, gearIndex) || gearIndex == 0) {
            fuelFlow = engineModel.getFuelFlow(fMot, powMechEl);
        }

        // ### check if motor regime can be reached; otherwise increase fuelFlow prohibitively

        // indicates that too high power required
        if (powMech > engineModel.getMaxPower()) {
            fuelFlow = FUELFLOW_ERROR;
        }

        // indicates that too high motor frequency
        if (withJante && (fMot > engineModel.getMaxFrequency())) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format(
                        "v_kmh=%f, acc=%f, gear=%d, motor frequency=%d/min too high -- > return fuelErrorConsumption: %.2f",
                        (3.6 * v), acc, gearIndex + 1, (int) (fMot * 60), FUELFLOW_ERROR));
            }
            fuelFlow = FUELFLOW_ERROR;
        }

        // indicates too low motor frequency
        if (withJante && (fMot < engineModel.getMinFrequency())) {
            if (gearIndex == 0) {
                fuelFlow = carModel.getElectricPower() * LIMIT_SPEC_CONS;
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("v=%f, gear=%d, fuelFlow=%f %n", v, gearIndex + 1, fuelFlow));
                }
            } else {
                fuelFlow = FUELFLOW_ERROR;
            }
        }

        return fuelFlow;

    }

    // optimum fuel consumption flow in m^3/s
    // gives also reference to fuel-optimized gear

    public double[] getMinFuelFlow(double v, double acc, boolean withJante) {
        int gear = 1;
        double fuelFlow = FUELFLOW_ERROR;
        for (int testGearIndex = engineModel.getMaxGearIndex(); testGearIndex >= 0; testGearIndex--) {
            double fuelFlowGear = getFuelFlow(v, acc, testGearIndex, withJante);
            if (fuelFlowGear < fuelFlow) {
                gear = testGearIndex + 1;
                fuelFlow = fuelFlowGear;
            }
        }
        final double[] retValue = { fuelFlow, gear };
        return retValue;
    }

    // Output methods

    public void writeOutput(boolean withJante, String projectName) {

        final String filenameConstAccel = projectName + ".carConstAccel";
        writeZeroAccelTest(filenameConstAccel);

        // write Jante data
        if (withJante) {
            final String filenameJanteOpt = projectName + ".JanteOpt";
            writeJanteDataFieldsOptGear(filenameJanteOpt);
            for (int gearIndex = 0; gearIndex < engineModel.getMaxGearIndex(); gearIndex++) {
                final int gear = gearIndex + 1;
                final String strGear = new Integer(gear).toString();
                final String filename = projectName + ".Jante" + strGear;
                writeJanteDataFields(gear, filename);
            }
        }
    }

    private void writeZeroAccelTest(String filename) {
        PrintWriter fstr = FileUtils.getWriter(filename);
        fstr.printf("# veh mass = %.1f%n", carModel.getMass());
        fstr.printf("# number of gears = %d%n", engineModel.getNumberOfGears());
        fstr.printf("# v[m/s]  accFreeWheeling[m/s^2]  fuelFlow[l/h]  gear  c100[l/100km]%n");
        final double vMax = 200 / 3.6;
        final double dv = 0.2;
        double v = dv;
        while (v <= vMax) {
            final double accFreeWheeling = carModel.getFreeWheelingDecel(v);
            final double acc = 0.0;
            final double[] fuelFlow = getMinFuelFlow(v, acc, true);
            final int optGear = (int) fuelFlow[1]; // !! kein gearIndex
            final double c100 = getInstConsumption100km(v, 0, optGear, true);
            fstr.printf("%.3f  %.8f  %.8f  %d  %.8f%n", v, accFreeWheeling, 3.6e6 * fuelFlow[0], optGear, c100);
            fstr.flush();
            v += dv;
        }
        fstr.close();
    }

    private void writeJanteDataFieldsOptGear(String filename) {
        writeJanteDataFields(0, filename);
    }

    private void writeJanteDataFields(int gearTest, String filename) {
        final boolean determineOptimalGear = (gearTest == 0) ? true : false;
        PrintWriter fstr = FileUtils.getWriter(filename);
        fstr.println("#Jante Fuel consumption:");
        fstr.println("# v(km/h)  acc(m/s^2) \tforceMech(N)\tpowMech(kW)\tfuelFlow(l/h)\t" + "consump(liters/100km)\tGear");

        double accmin = -1;
        double accmax = 3;
        double vmin_kmh = 0;
        double vmax_kmh = 200;
        final int NV = 101;
        final int NACC = 61;
        double dv_kmh = (vmax_kmh - vmin_kmh) / (NV - 1);
        double dacc = (accmax - accmin) / (NACC - 1);

        for (int iv = 0; iv < NV; iv++) {
            for (int iacc = 0; iacc < NACC; iacc++) {
                double v_kmh = vmin_kmh + iv * dv_kmh;
                double v = v_kmh / 3.6;
                double acc = 0.01 * (int) (100 * (accmin + iacc * dacc));
                double forceMech = carModel.getForceMech(v, acc);
                double powMechEl = Math.max(v * forceMech + carModel.getElectricPower(), 0.);
                double fuelFlow = 100000;
                int gear = gearTest;
                if (determineOptimalGear) {
                    // v=const => min(consump)=min(fuelFlow)
                    double[] res = getMinFuelFlow(v, acc, true);
                    fuelFlow = res[0];
                    gear = (int) res[1];
                } else {
                    final int gearIndex = gear - 1;
                    fuelFlow = getFuelFlow(v, acc, gearIndex, true);
                }
                double consump_100km = 1e8 * fuelFlow / Math.max(v, 0.001);
                double fuelFlow_lh = 3.6e6 * fuelFlow;
                fstr.printf(Locale.US, "%.2f  %.2f  %.2f  %.6f  %.5f  %.5f %d%n", v_kmh, acc, forceMech,
                        (0.001 * powMechEl), fuelFlow_lh, consump_100km, gear);
            }
            fstr.println();
        }
        fstr.close();
    }

   
}
