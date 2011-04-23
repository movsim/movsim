package org.movsim.output.impl;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.output.Trajectories;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrajectoriesImpl implements Trajectories {

	
	final static Logger logger = LoggerFactory.getLogger(TrajectoriesImpl.class);
	
    //defaults for optional user input:
    
    private double dtOut = 1.0; // write output all dtOut seconds
    
    private double t_start_interval;
    
    private double t_end_interval; 
    
    private double x_start_interval;
    
    private double x_end_interval;

    private HashMap<Long, PrintWriter> fileHandles;
    
    private double time = 0;
    
    private String path;

    private String endingFile = ".traj_jsim.csv";

    private char commentChar = '#';

    private double lastUpdateTime = 0;
    
    private RoadSection roadSection; 
    
    private String projectName;

    public TrajectoriesImpl(String projectName, TrajectoriesInput trajectoriesInput, RoadSection roadSection) {
    	logger.info("Constructor");

        this.projectName = projectName;
        
        dtOut = trajectoriesInput.getDt();
        t_start_interval = trajectoriesInput.getStartPosition();
        t_end_interval = trajectoriesInput.getEndPosition();
        x_start_interval = trajectoriesInput.getStartTime();
        x_end_interval = trajectoriesInput.getEndTime();
        
        this.roadSection = roadSection;
        
        fileHandles = new HashMap<Long, PrintWriter>();
        logger.info("path = {}", path);
        logger.info("interval for output: timeStart={}, timeEnd={}", t_start_interval, t_end_interval);
    }

    private void createFileHandles(){

        final String filenameMainroad = projectName+".main_1"+endingFile;
        logger.info("filenameMainroad={}, id={}", filenameMainroad, roadSection.id());
        fileHandles.put(roadSection.id(), FileUtils.getWriter(filenameMainroad));
        
        /*
        // onramps
        int counter = 1;
        for(IOnRamp rmp : mainroad.onramps()){
            final String filename = projectName+".onr_"+Integer.toString(counter)+endingFile;
            fileHandles.put(rmp.roadIndex(), FileUtils.getWriter(filename));
            counter++;
        }
        // offramps
        counter = 1;
        for(IStreet rmp : mainroad.offramps()){
            final String filename = projectName+".offr_"+Integer.toString(counter)+endingFile;
            fileHandles.put(rmp.roadIndex(), FileUtils.getWriter(filename));
            counter++;
        }
        */
        
        
        // write headers
        Iterator<Long>  it = fileHandles.keySet().iterator();
        while (it.hasNext()) {
            Long id= (Long)it.next();
            final PrintWriter fstr = fileHandles.get(id);
            fstr.println(commentChar + "t[s]  id  x[m]  lane[1]  gap[m]  v[m/s]   dv[m/s]  accReal[m/s^2]   label  vehClass");
            fstr.flush();
        }
    }

    
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // update
    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    public void update(int iTime, double time, double timestep) {
        
        if( fileHandles.isEmpty() ){
            // cannot initialize earlier because onramps and offramps are constructed after constructing mainroad
            createFileHandles();
        }
        
        this.time = time;
        //check time interval for output:
        if(time >= t_start_interval &&  time<=t_end_interval){
        	
            if(iTime%1000==0){
            	 logger.info("time = {}, timestep= {}", time, timestep);
            }
            
            if ( (time - lastUpdateTime + Constants.SMALL_VALUE) >= dtOut) {
                lastUpdateTime = time;
            
                // mainroad 
                writeTrajectories(fileHandles.get(roadSection.id()), roadSection.vehContainer());
                /*
                // onramps
                for(IOnRamp rmp : mainroad.onramps()){
                    writeTrajectories(fileHandles.get(rmp.roadIndex()), rmp.vehContainer());  
                }
                // offramps
                for(IStreet rmp : mainroad.offramps()){
                    writeTrajectories(fileHandles.get(rmp.roadIndex()), rmp.vehContainer());
                }*/
            } //of if
        }  
    }



    private void writeTrajectories(PrintWriter fstr, VehicleContainer vehicles ) {
        for (int i = 0, N = vehicles.size() ; i < N; i++) {
            Vehicle me = vehicles.get(i);
            if( (me.position() >= x_start_interval && me.position() <= x_end_interval) ){
            	writeCarData(fstr, i, me);
            } 
        } //of for
    }
    

            
    private void writeCarData(PrintWriter fstr, int index, Vehicle me) {
        final Vehicle frontVeh = roadSection.vehContainer().getLeader(me); 
        final double s = (frontVeh == null) ? 0 : me.netDistance(frontVeh);
        final double dv = (frontVeh == null) ? 0 : me.relSpeed(frontVeh);
        fstr.printf("%8.2f; %18d; %10.3f; %6.3f; %9.5f; %9.5f; %9.5f; %11.8f; %s%n", 
                time, me.id(), me.position(),  me.getLane(), s, me.speed(), dv, me.acc(), me.getLabel());
        fstr.flush();
    }


}


