package org.movsim.utilities;

import java.util.Arrays;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Linearly interpolated univariate function of non-equidistant pairs of (x,y) data. The function is extrapolated with constant values at
 * the boundaries.
 * 
 * <p>
 * In case of only one data point no splineFunction is created.
 */
public class LinearInterpolatedFunction {
    
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LinearInterpolatedFunction.class);


    private PolynomialSplineFunction splineFunction;

    private final XYDataPoint start;
    private final XYDataPoint end;
    
    private final int numberOfDataPoints;

    public int getNumberOfDataPoints() {
        return numberOfDataPoints;
    }

    /**
     * @throws IllegalArgumentException
     *             , DimensionMismatchException, NumberIsTooSmallException, NonMonotonicSequenceException
     */
    public LinearInterpolatedFunction(double[] x, double[] y) {
        Preconditions.checkArgument(x.length == y.length, "dimensions mismatch");
        Preconditions.checkArgument(x.length != 0, "dimension zero");

        if (x.length > 1) {
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            if(LOG.isDebugEnabled()){
                LOG.debug("x={}", Arrays.toString(x));
                LOG.debug("y={}", Arrays.toString(y));
            }
            splineFunction = linearInterpolator.interpolate(x, y);
        }
        
        numberOfDataPoints = x.length;

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
