package org.movsim.input.model.output.impl;

import org.jdom.Element;
import org.movsim.input.model.output.TrajectoriesInput;

public class TrajectoriesInputImpl implements TrajectoriesInput{

	private double dt;
	private double startTime;
	private double endTime;
	private double startPosition;
	private double endPosition;
	
	private boolean isInitialized;
	
	
	
	 public TrajectoriesInputImpl(Element elem) {
		 if (elem == null) {
			 isInitialized = false;
			 return;
		 }
		 
		 dt = Double.parseDouble(elem.getAttributeValue("dt"));
		 startTime = 60*Double.parseDouble(elem.getAttributeValue("t_start_min"));
		 endTime = 60*Double.parseDouble(elem.getAttributeValue("t_end_min"));
		 startPosition = 1000*Double.parseDouble(elem.getAttributeValue("x_start_km"));
		 endPosition = 1000*Double.parseDouble(elem.getAttributeValue("x_end_km"));
		 isInitialized = true;
	 }



	/**
	 * @return the dt
	 */
	public double getDt() {
		return dt;
	}



	/**
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
	}



	/**
	 * @return the endTime
	 */
	public double getEndTime() {
		return endTime;
	}



	/**
	 * @return the startPosition
	 */
	public double getStartPosition() {
		return startPosition;
	}



	/**
	 * @return the endPosition
	 */
	public double getEndPosition() {
		return endPosition;
	}



	/**
	 * @return the isInitialized
	 */
	public boolean isInitialized() {
		return isInitialized;
	}




}
