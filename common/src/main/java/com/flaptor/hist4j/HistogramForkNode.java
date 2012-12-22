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

import java.util.ArrayList;

/**
 * The HistogramForkNode splits the data range in two at a given value, pointing to two subtrees, 
 * one for values smaller than the split value, and one for values larger than the split value. 
 * It implements the recursive calls necesary to obtain the data from the tree structure.
 * @author Jorge Handl
 */

public class HistogramForkNode extends HistogramNode {

	private static final long serialVersionUID = -1L;

    // Attributes of a fork node.
    private float splitValue;
    private HistogramNode left = null;
    private HistogramNode right = null;

    /**
     * Creates a fork node with the given split value and subtrees.
     * @param splitValue the value that splits both subtrees.
     * @param left the left subtree.
     * @param right the right subtree.
     */
    public HistogramForkNode (float splitValue, HistogramNode left, HistogramNode right) {
        this.splitValue = splitValue;
        this.left = left;
        this.right = right;
    }
    
    /**
     * Clears the fork node, recursively erasing the subtrees.
     */
    public void reset () {
        if (null != left) {
            left.reset();
            left = null;
        }
        if (null != right) {
            right.reset();
            right = null;
        }
        splitValue = 0;
    }

    /**
     * Adds a value to the histogram by recursively adding the value to either subtree, depending on the split value.
     * @param root a reference to the adaptive histogram instance that uses this structure.
     * @param value the value for which the count is to be incremented.
     * @return A reference to itself.
     */
    public HistogramNode addValue (AdaptiveHistogram root, float value) {
        // The data node addValue implementation returns a reference to itself if there was no structural change needed,
        // or a reference to a new fork node if the data node had to be split in two. By assigning the returned reference
        // to the corresponding subtree variable (left or right), the subtree can replace itself with a new structure,
        // eliminating the need for a node to manipulate its subtree, for which it would need to know a lot about what 
        // happens at the lower level.
        if (value > splitValue) {
            right = right.addValue(root, value);
        } else {
            left = left.addValue(root, value);
        }
        return this;
    }

    /**
     * Returns the number of data points stored in the same bucket as a given value.
     * @param value the reference data point.
     * @return the number of data points stored in the same bucket as the reference point.
     */
    public long getCount (float value) {
        // The fork node recursively calls the appropriate subtree depending on the split value.
        long count = 0;
        if (value > splitValue) {
            count = right.getCount(value);
        } else {
            count = left.getCount(value);
        }
        return count;
    }
    
    /**
     * Returns the cumulative density function for a given data point.
     * @param value the reference data point.
     * @return the cumulative density function for the reference point.
     */
    public long getAccumCount (float value) {
        // The fork node recursively calls the appropriate subtree depending on the split value.
        long count = left.getAccumCount(value);
        if (value > splitValue) {
            count += right.getAccumCount(value);
        }
        return count;
    }

    /**
     * Returns the data point where the running cumulative count reaches the target cumulative count.
     * @param accumCount
	 *		accumCount[0] the running cumulative count.
     *		accumCount[1] the target cumulative count.
     * @return the data point where the running cumulative count reaches the target cumulative count.
     */
    public Float getValueForAccumCount (long[] accumCount) {
        Float val = left.getValueForAccumCount(accumCount);
        if (null == val) {
            val = right.getValueForAccumCount(accumCount);
        }
        return val;
    }

    /**
     * Applies a convertion function to the values stored in the histogram.
     * @param valueConversion a class that defines a function to convert the value.
     */
    public void apply (AdaptiveHistogram.ValueConversion valueConversion) {
        left.apply(valueConversion);
        right.apply(valueConversion);
        splitValue = valueConversion.convertValue(splitValue);
    }


    /**
     * Prints the data for the nodes in its subtrees.
     * @param level the level of this node in the tree.
     */
    public void show (int level) {
        left.show(level+1);
        margin(level);
        System.out.println("Fork at: " + splitValue);
        right.show(level+1);
    }
    /**
     * Build the table representing the histogram data adding the data from each subtree.
     */
    public void toTable (ArrayList<Cell> table) {
    	left.toTable(table);
    	right.toTable(table);
    }

}

