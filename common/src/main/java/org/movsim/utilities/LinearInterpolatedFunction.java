package org.movsim.utilities;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import com.google.common.base.Preconditions;

/**
 * Linearly interpolated univariate function of non-equidistant pairs of (x,y) data. The function is extrapolated with constant values at
 * the boundaries.
 * 
 * <p>
 * In case of only one data point no splineFunction is created.
 */
public class LinearInterpolatedFunction {

    private PolynomialSplineFunction splineFunction;

    private final XYDataPoint start;
    private final XYDataPoint end;

    /**
     * @throws IllegalArgumentException
     *             , DimensionMismatchException, NumberIsTooSmallException, NonMonotonicSequenceException
     */
    public LinearInterpolatedFunction(double[] x, double[] y) {
        Preconditions.checkArgument(x.length == y.length, "dimensions mismatch");
        Preconditions.checkArgument(x.length != 0, "dimension zero");

        if (x.length > 1) {
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            splineFunction = linearInterpolator.interpolate(x, y);
        }

        start = new XYDataPoint(x[0], y[0]);
        end = new XYDataPoint(x[x.length - 1], y[y.length - 1]);
    }

    public double value(double x0) {
        if (splineFunction != null && splineFunction.isValidPoint(x0)) {
            return splineFunction.value(x0);
        }
        if (x0 <= start.getX()) {
            return start.getY();
        }
        if (x0 >= end.getX()) {
            return end.getY();
        }
        throw new IllegalStateException("should not reach undefined function range=" + x0);
    }

}
