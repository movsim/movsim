package org.movsim.simulator.vehicles;

import java.util.List;

public interface MoveableContainer {

    
    List<Moveable> getMoveables();

    
    Moveable getMoveable(int index);
    
    /**
     * Size.
     * 
     * @return the int
     */
    int size();


    /**
     * Gets the leader.
     * 
     * @param veh
     *            the veh
     * @return the leader
     */
    Moveable getLeader(Moveable veh);

    
}
