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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A HistogramNode is the building block for the histogram data structure and
 * provides a common inteface for both the data and the fork nodes.
 * This is an abstract class, and it is instantiated as either a fork node or a data node.
 * 
 * @author Jorge Handl
 */
public abstract class HistogramNode implements Serializable {

    private static final long serialVersionUID = -1L;

    /** Abstract method for clearing the node */
    public abstract void reset();

    /** Abstract method for adding a value to the histogram */
    public abstract HistogramNode addValue(AdaptiveHistogram root, float value);

    /** Abstract method for getting the number of values stored in the same bucket as a reference value */
    public abstract long getCount(float value);

    /** Abstract method for getting the cumulative density function for a given value */
    public abstract long getAccumCount(float value);

    /** Abstract method for getting the value for which the cumulative density function reaches the desired value */
    public abstract Float getValueForAccumCount(long[] accumCount);

    /** Abstract method for applying a convertion function to the values stored in the histogram */
    public abstract void apply(AdaptiveHistogram.ValueConversion valueConversion);

    /** Abstract method for getting a table representing the histogram data */
    public abstract void toTable(ArrayList<Cell> table);

    /** Abstract method for showing the data structure */
    public abstract void show(int level);

    /** Prints a margin corresponding to the provided tree level */
    protected void margin(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
    }
}
