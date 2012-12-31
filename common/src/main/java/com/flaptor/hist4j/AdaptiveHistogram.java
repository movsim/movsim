/*
Copyright 2007 Flaptor (flaptor.com) 
Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 
http://www.apache.org/licenses/LICENSE-2.0 
Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
 */
package com.flaptor.hist4j;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class implements a histogram that adapts to an unknown data distribution.
 * It keeps a more or less constant resolution throughout the data range by increasing 
 * the resolution where the data is more dense.  For example, if the data has such
 * such a distribution that most of the values lie in the 0-5 range and only a few are 
 * in the 5-10 range, the histogram would adapt and assign more counting buckets to
 * the 0-5 range and less to the 5-10 range.
 * This implementation provides a method to obtain the accumulative density function 
 * for a given data point, and a method to obtain the data point that splits the 
 * data set at a given percentile. 
 * @author Jorge Handl
 */
public class AdaptiveHistogram implements Serializable {

    private static final long serialVersionUID = -1L;
    private long totalCount;     // total number of data points
    private HistogramNode root;  // root of the tree

    /**
     * Class constructor.
     */
    public AdaptiveHistogram() {
        root = null;
        reset();
    }

    /**
     * Erases all data from the histogram.
     */
    public void reset() {
        if (null != root) {
            root.reset();
            root = null;
        }
        totalCount = 0;
    }

    /**
     * Adds a data point to the histogram.
     * @param value the data point to add.
     */
    public synchronized void addValue(float value) {
        totalCount++;
        if (null == root) {
            root = new HistogramDataNode();
        }
        root = root.addValue(this, value);
    }

    /**
     * Returns the number of data points stored in the same bucket as a given value.
     * @param value the reference data point.
     * @return the number of data points stored in the same bucket as the reference point.
     */
    public long getCount(float value) {
        long count = 0;
        if (null != root) {
            count = root.getCount(value);
        }
        return count;
    }

    /**
     * Returns the cumulative density function for a given data point.
     * @param value the reference data point.
     * @return the cumulative density function for the reference point.
     */
    public long getAccumCount(float value) {
        long count = 0;
        if (null != root) {
            count = root.getAccumCount(value);
        }
        return count;
    }

    /**
     * Returns the data point that splits the data set at a given percentile.
     * @param percentile the percentile at which the data set is split.
     * @return the data point that splits the data set at the given percentile.
     */
    public float getValueForPercentile(int percentile) {
        long targetAccumCount = (totalCount * percentile) / 100;
        Float value = new Float(0);
        if (null != root) {
            value = root.getValueForAccumCount(new long[]{0, targetAccumCount});
        }
        return (null != value) ? value.floatValue() : 0;
    }

    /**
     * This method is used by the internal data structure of the histogram to get the
     * limit of data points that should be counted at one bucket.
     * @return the limit of data points to store a one bucket.
     */
    protected int getCountPerNodeLimit() {
        int limit = (int) (totalCount / 10);
        if (0 == limit) {
            limit = 1;
        }
        return limit;
    }

    /**
     * Auxiliary interface for inline functor object.
     */
    protected interface ValueConversion {
        /**
         * This method should implement the conversion function.
         * @param value the input value.
         * @return the resulting converted value.
         */
        float convertValue(float value);
    }

    /**
     * Normalizes all the values to the desired range.
     * @param targetMin the target new minimum value.
     * @param targetMax the target new maximum value.
     */
    public void normalize(float targetMin, float targetMax) {
        if (null != root) {
            final float min = getValueForPercentile(0);
            final float max = getValueForPercentile(100);
            final float m = (targetMax - targetMin) * ((max > min) ? 1 / (max - min) : 1);
            final float b = targetMin;
            root.apply(new ValueConversion() { public float convertValue(float value) { return m * (value - min) + b; } });
        }
    }

    /**
     * Shows the histograms' underlying data structure.
     */
    public void show() {
        System.out.println("Histogram has " + totalCount + " values:");
        if (null != root) {
            root.show(0);
        }
    }

    /**
     * Return a table representing the data in this histogram.
     * Each element is a table cell containing the range limit values and the count for that range.
     */
    public ArrayList<Cell> toTable() {
        ArrayList<Cell> table = new ArrayList<Cell>();
        if (null != root) {
            root.toTable(table);
        }
        return table;
    }

}

