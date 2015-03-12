package org.movsim.utilities;

public final class ProbabilityUtils {

    private static final double GAUSS_NORM = 1 / Math.sqrt(2 * Math.PI);

    private ProbabilityUtils() {
        throw new IllegalStateException("do not instanciate");
    }

    /**
     * returns the normalized Gaussian density.
     * @param z
     * @return the normalized Gaussian density
     */
    public static double getGaussDensity(double z) {
        return GAUSS_NORM * Math.exp(-0.5 * z * z);
    }
}
