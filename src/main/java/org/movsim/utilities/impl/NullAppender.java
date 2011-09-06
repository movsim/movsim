/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.utilities.impl;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class NullAppender.
 */
public class NullAppender extends AppenderSkeleton {

    /**
     * Instantiates a new null appender.
     */
    public NullAppender() {
    }

    /**
     * Instantiates a new null appender.
     * 
     * @param layout
     *            the layout
     */
    public NullAppender(Layout layout) {
        this.layout = layout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.log4j.AppenderSkeleton#doAppend(org.apache.log4j.spi.LoggingEvent
     * )
     */
    @Override
    public void doAppend(LoggingEvent event) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent
     * )
     */
    @Override
    public void append(LoggingEvent event) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public void close() {
    }
}