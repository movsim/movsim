package org.movsim.simulator.vehicles;


// converts scaled to physical SI units needed for CAs 

public class PhysicalQuantities {

    private final double tscale = 1;
    
    private final double xScale;
    private final double vScale;
    private final double accScale;
    
    private final double rhoScale;
    
    private final Vehicle me;
    
    public PhysicalQuantities(final Vehicle veh){
        this.me = veh;
        
        xScale = veh.getAccelerationModel().getScalingLength();
        
        vScale=xScale/tscale;
        accScale= Math.pow(xScale/tscale, 2);
        rhoScale=1./xScale;
    }
    
    public double getLength(){
        return xScale*me.getLength();
    }
    
    public double getPosition(){
        return xScale * me.getPosition();
    }

    public double posFrontBumper(){
        return xScale * me.posFrontBumper();
    }

    public double posRearBumper(){
        return xScale * me.posRearBumper();
    }

    public double getPositionOld(){
        return xScale * me.getPositionOld();
    }


    public double getSpeed(){
        return vScale * me.getSpeed();
    }
    
    
    public double getAcc(){
        return accScale * me.getAcc();
    }
    
    public double getNetDistance(final Moveable vehFront){
        return xScale * me.getNetDistance(vehFront);
    }

    public double getRelSpeed(final Moveable vehFront){
        return vScale * me.getRelSpeed(vehFront);
    }

    
}
