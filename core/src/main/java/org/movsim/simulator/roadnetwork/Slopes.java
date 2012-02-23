package org.movsim.simulator.roadnetwork;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.movsim.input.model.simulation.SlopeDataPoint;
import org.movsim.utilities.Tables;

/**
 * The Class Slopes.
 */
public class Slopes implements Iterable<Slope> {

    // final static Logger logger = LoggerFactory.getLogger(Slopes.class);

    private double[] positions;
    private double[] gradients;
    private final Collection<Slope> slopes;

    /**
     * Constructor.
     */
    public Slopes(List<SlopeDataPoint> slopesInputDataPoints) {
        slopes = new LinkedList<Slope>();
        generateSpaceSeriesData(slopesInputDataPoints);
    }

    /**
     * Generate space series data.
     * 
     * @param data
     *            the data
     */
    private void generateSpaceSeriesData(List<SlopeDataPoint> data) {
        final int size = data.size() + 1;
        positions = new double[size];
        gradients = new double[size];
        positions[0] = 0;
        gradients[0] = 0;
        for (int i = 1; i < size; i++) {
            final double pos = data.get(i - 1).getPosition();
            positions[i] = pos;
            final double gradient = data.get(i - 1).getGradient();
            gradients[i] = gradient;
            slopes.add(new Slope(pos, gradient));
        }
    }

    /**
     * Checks if is empty.
     * 
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return gradients.length == 0;
    }

    public double calcSlope(double position) {
        return gradients.length == 0 ? 0 :
            Tables.stepExtrapolation(positions, gradients, position);
    }

    @Override
    public Iterator<Slope> iterator() {
        return slopes.iterator();
    }
}