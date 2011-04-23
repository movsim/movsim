package org.movsim.input.model.output;

public interface TrajectoriesInput {

	
	double getDt();
	double getStartTime();
	double getEndTime();
	double getStartPosition();
	double getEndPosition();
	boolean isInitialized();

}
