package org.movsim.simulator.roadnetwork;

import com.google.common.base.Preconditions;

public class NodeImpl implements Node {

    private long nodeId = Long.MAX_VALUE;
    private final String type;

    NodeImpl(String type) {
        this.type = Preconditions.checkNotNull(type);
    }

    @Override
    public boolean hasId() {
        return nodeId != Long.MAX_VALUE;
    }

    @Override
    public long getId() {
        return nodeId;
    }

    @Override
    public void setId(long id) {
        if (hasId() && this.nodeId != id) {
            throw new IllegalArgumentException("nodetype=" + this + " already set with value=" + this.nodeId);
        }
        this.nodeId = id;
    }

    @Override
    public String toString() {
        return "Node[type=" + type + ", nodeId=" + (this.hasId() ? this.getId() : "") + "]";
    }

}
