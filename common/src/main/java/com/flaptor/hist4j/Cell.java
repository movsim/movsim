/**
 * 
 */
package com.flaptor.hist4j;

import java.io.Serializable;

/**
 * Contains the data of a histogram node.
 * @author jorge
 */
public class Cell implements Serializable {
	private static final long serialVersionUID = -1L;
    public long count;
    public float minValue, maxValue;
}
