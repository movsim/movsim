package org.movsim.consumption.output;

import java.util.Locale;

import org.movsim.consumption.model.CarModel;
import org.movsim.consumption.model.Consumption;
import org.movsim.consumption.model.EngineModel;

// TODO refactoring of fuel consumption code base and corresponding file output
public class FileFuelConsumptionModel extends FileOutputBase {

    private static final String extensionFormatJante = ".jante_%s.csv";
    private static final String extensionFormatZeroAcceleration = ".constAccel_%s.csv";
    private static final String extensionFormatSpecificConsumption = ".specCons_%s.csv";

    private static final String outputHeadingJante = COMMENT_CHAR
            + "v(km/h),   acc(m/s^2),  forceMech(N), powMech(kW), fuelFlow(l/h), consump(liters/100km), Gear\n";
    private static final String outputHeadingZeroAcceleration = COMMENT_CHAR
            + "v(km/h),   acc(m/s^2),  forceMech(N), powMech(kW), fuelFlow(l/h), consump(liters/100km), Gear\n";
    private static final String outputHeadingSpecificConsumption = COMMENT_CHAR
            + "f(1/min); powerMech(kW); consRate(l/h); moment(Nm); specCons(g/kWh)\n";

    private static final String outputFormatJante = "%.2f, %.2f, %.2f, %.6f, %.5f, %.5f, %d%n";
    private static final String outputFormatZeroAcceleration = "%.3f, %.8f,  %.8f,  %d,  %.8f%n";

    private final String keyLabel;
    private final Consumption fuelConsumption;

    public FileFuelConsumptionModel(String keyLabel, Consumption fuelConsumption) {
        this.keyLabel = keyLabel;
        this.fuelConsumption = fuelConsumption;
    }

    public void writeZeroAccelerationTest(CarModel carModel, EngineModel engineModel) {
        writer = createWriter(String.format(extensionFormatZeroAcceleration, keyLabel));
        writer.printf(outputHeadingZeroAcceleration);
        writer.flush();
        writer.printf("# veh mass = %.1f%n", carModel.getMass());
        writer.printf("# number of gears = %d%n", engineModel.getNumberOfGears());
        writer.printf("# v[m/s], accFreeWheeling[m/s^2], fuelFlow[l/h], gear, c100[l/100km]%n");
        final double vMax = 200 / 3.6;
        final double dv = 0.2;
        double v = dv;
        while (v <= vMax) {
            final double accFreeWheeling = carModel.getFreeWheelingDecel(v);
            final double acc = 0.0;
            final double[] fuelFlow = fuelConsumption.getMinFuelFlow(v, acc, 0, true);
            final int optGear = (int) fuelFlow[1]; // !! not a gearIndex
            final double c100 = fuelConsumption.getInstConsumption100km(v, 0, optGear, true);
            writer.printf(outputFormatZeroAcceleration, v, accFreeWheeling, 3.6e6 * fuelFlow[0], optGear, c100);
            writer.flush();
            v += dv;
        }
        writer.close();
    }

    public void writeJante(int gearTest, CarModel carModel) {
        writer = createWriter(String.format(extensionFormatJante, keyLabel));
        writer.printf(outputHeadingJante);
        writer.flush();

        final boolean determineOptimalGear = (gearTest == 0) ? true : false;
        final double accmin = -1;
        final double accmax = 3;
        final double vmin_kmh = 0;
        final double vmax_kmh = 200;
        final int NV = 101;
        final int NACC = 61;
        final double dv_kmh = (vmax_kmh - vmin_kmh) / (NV - 1);
        final double dacc = (accmax - accmin) / (NACC - 1);

        for (int iv = 0; iv < NV; iv++) {
            for (int iacc = 0; iacc < NACC; iacc++) {
                final double v_kmh = vmin_kmh + iv * dv_kmh;
                final double v = v_kmh / 3.6;
                final double acc = 0.01 * (int) (100 * (accmin + iacc * dacc));
                final double forceMech = carModel.getForceMech(v, acc, 0);
                final double powMechEl = Math.max(v * forceMech + carModel.getElectricPower(), 0.);
                double fuelFlow = 100000;
                int gear = gearTest;
                if (determineOptimalGear) {
                    // v=const => min(consump)=min(fuelFlow)
                    final double[] res = fuelConsumption.getMinFuelFlow(v, acc, 0, true);
                    fuelFlow = res[0];
                    gear = (int) res[1];
                } else {
                    final int gearIndex = gear - 1;
                    fuelFlow = fuelConsumption.getFuelFlow(v, acc, 0, gearIndex, true);
                }
                final double consump_100km = 1e8 * fuelFlow / Math.max(v, 0.001);
                final double fuelFlow_lh = 3.6e6 * fuelFlow;
                writer.printf(outputFormatJante, v_kmh, acc, forceMech, (0.001 * powMechEl), fuelFlow_lh,
                        consump_100km, gear);
            }
            writer.println();
        }
        writer.close();
    }

    public void writeSpecificConsumption(EngineModel engineModel) {
        writer = createWriter(String.format(extensionFormatSpecificConsumption, keyLabel));
        writer.printf(outputHeadingSpecificConsumption);
        writer.flush();

        // fstr.printf("# power in idle mode = %f kW%n", 0.001*powIdle);
        writer.printf("# c_spec0 in idle mode = %f kg/kWh = %f Liter/kWh %n", 3.6e6 * engineModel.cSpec0Idle, 3.6e6
                / FuelConstants.RHO_FUEL_PER_LITER * engineModel.cSpec0Idle);

        final int N_FREQ = 40;
        final double df = (engineModel.maxFrequency - engineModel.minFrequency) / (N_FREQ - 1);
        final int N_POW = 80;
        final double powMin = -20000; // range
        final double dPow = (engineModel.maxPower - powMin) / (N_POW - 1);

        for (int i = 0; i < N_FREQ; i++) {
            final double f = engineModel.minFrequency + i * df;
            for (int j = 0; j <= N_POW; j++) {
                final double pow = powMin + j * dPow;
                final double dotC = engineModel.getFuelFlow(f, pow);
                final double indMoment = EngineModel.getMoment(pow, f); // + getModelLossMoment(f);
                final double cSpec = engineModel.cSpecific0ForMechMoment(f, indMoment);
                // factor 3.6e6 for converting from m^3/s to liter/h
                writer.printf(Locale.US, "%.1f, %.3f, %.9f, %.9f, %.9f%n", f * 60, pow / 1000., 3.6e6 * dotC,
                        indMoment, cSpec * 3.6e9);
            }
            writer.println(); // gnuplot block
        }
        writer.close();
    }
}
