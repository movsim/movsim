package org.movsim.utilities.impl;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.AppenderSkeleton;

public class NullAppender extends AppenderSkeleton {


    public NullAppender() {
    }

    public NullAppender(Layout layout) {
        this.layout = layout;
    }


    public void doAppend(LoggingEvent event) {
    }

    public void append(LoggingEvent event) {
    }

    public boolean requiresLayout() {
        return true;
    }

    public void close() {
    }
}