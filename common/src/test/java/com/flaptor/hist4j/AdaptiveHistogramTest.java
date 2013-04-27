/*
 * Copyright 2007 Flaptor (flaptor.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flaptor.hist4j;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import junit.framework.TestCase;

/**
 * Unit test for the AdaptiveHistogram class
 * 
 * @author Jorge Handl
 */
public class AdaptiveHistogramTest extends TestCase {

    Random rnd = new Random(new Date().getTime());

    /**
     * Check the histogram values to see if the expected properties hold.
     * 
     * @param h
     *            the histogram to check.
     * @param min
     *            the minimum value in the data set used to build the histogram.
     * @param max
     *            the maximum value in the data set used to build the histogram.
     * @param totalCount
     *            the total number of data points used to build the histogram.
     * @param skew
     *            true if the data is skewed, false otherwise.
     */
    private void checkValues(AdaptiveHistogram h, float min, float max, long totalCount, boolean skew) {
        float middle = (max + min) / 2f;
        float alittle = (max - min) / 10f;
        boolean flat = false;
        if (0 == alittle) {
            alittle = Math.abs((0 == min) ? 0.1f : min);
            flat = true;
        }
        assertTrue("AccumCount for a value lower than the minimum is not zero", h.getAccumCount(min - alittle) == 0);
        assertTrue("AccumCount for a value higher than the maximum is not the whole data set",
                h.getAccumCount(max + alittle) == totalCount);
        if (!flat)
            assertTrue("AccumCount for the middle of the data set returns the whole data set",
                    h.getAccumCount(middle) < totalCount);
        if (skew)
            assertTrue("AccumCount for the middle of a skewed data set doesn't reflect the skew",
                    h.getAccumCount(middle) > 1.1f * (totalCount / 2));

        long lastCount = -1;
        for (int i = 0; i < 11; i++) {
            float val = (i / 10f) * (max - min) + min;
            long count = h.getAccumCount(val);
            assertTrue("The cumulative count decreases for increasing values", count >= lastCount);
            lastCount = count;
        }

        assertTrue("The value for percentile 0 is not close to the begining of the data set",
                h.getValueForPercentile(0) < min + alittle);
        assertTrue("The value for percentile 100 is not close to the end of the data set",
                h.getValueForPercentile(100) > max - alittle);
        if (skew)
            assertTrue("The value for percentile 50 of a skewed data set doesn't reflect the skew",
                    h.getValueForPercentile(50) < middle - alittle);

        float lastValue = min - alittle;
        for (int i = 0; i < 11; i++) {
            int per = i * 10;
            float value = h.getValueForPercentile(per);
            assertTrue("The value for an increasing percentile decreases", value >= lastValue);
            lastValue = value;
        }

    }

    /**
     * Verify that the histogram features work as expected with the given data.
     * 
     * @param data
     *            an array of numbers to pass on to the histogram.
     * @param min
     *            minimun value in the data set.
     * @param max
     *            maximun value in the data set.
     * @param skew
     *            true if the data is skewed, false otherwise.
     */
    private void verify(float[] data, float min, float max, boolean skew) throws Exception {
        AdaptiveHistogram h = new AdaptiveHistogram();
        int size = data.length;
        for (int i = 0; i < size; i++) {
            h.addValue(data[i]);
        }

        checkValues(h, min, max, size, skew);

        Arrays.sort(data);

        int step = size / 100;
        if (0 == step)
            step = 1;
        for (int i = 0; i < size; i += step) {
            int per = (i * 100) / size;
            float value = h.getValueForPercentile(per);
            float reference = data[i];
            assertTrue("The percentile value does not match the real data", Math.abs(value - reference) < 0.05f);
        }
        if (min < max) {
            int tests = 50;
            for (int i = 0; i < tests; i++) {
                do {
                    min = 1000f * rnd.nextFloat() - 500f;
                    max = 1000f * rnd.nextFloat() - 500f;
                } while (min >= max);
                h.normalize(min, max);
                checkValues(h, min, max, size, skew);
            }
        }
    }

    /**
     * Test storing random values, skewed so the histogram adaptability feature can be tested.
     * 
     * @param tests
     *            the number of data points.
     */
    private void tryRandomValues(int tests) throws Exception {
        float[] data = new float[tests];
        for (int i = 0; i < tests; i++) {
            float val = rnd.nextFloat();
            val = val * val * val; // skew the data set so that most points fall at the begining of the [0-1] range.
            data[i] = val;
        }
        verify(data, 0f, 1f, true);
    }

    /**
     * Test storing a fixed value
     * 
     * @param tests
     *            the number of data points.
     * @param val
     *            the fixed value to use in the test.
     */
    private void tryFixedValue(int tests, float val) throws Exception {
        float[] data = new float[tests];
        for (int i = 0; i < tests; i++) {
            data[i] = val;
        }
        verify(data, val, val, false);
    }

    /**
     * Test various normal and extreme cases.
     */
    public void testHistogram() throws Exception {
        tryRandomValues(10000);
        tryFixedValue(10, 0f);
        tryFixedValue(10, 1E10f);
        tryFixedValue(10, -1E10f);
        tryFixedValue(10, 1E-10f);
        tryFixedValue(10, -1E-10f);
    }

}
