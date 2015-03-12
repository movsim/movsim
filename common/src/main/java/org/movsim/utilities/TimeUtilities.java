package org.movsim.utilities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TimeUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(TimeUtilities.class);

    private TimeUtilities() {
        throw new IllegalStateException("do not instanciate");
    }

    public static double convertToSeconds(String time, String timeFormat) {
        return convertToSeconds(time, timeFormat, 0L);
    }

    public static double convertToSeconds(String time, String timeFormat, long timeOffsetMillis) {
        if (timeFormat.isEmpty()) {
            return Double.parseDouble(time) - timeOffsetMillis / 1000.;
        }

        DateTime dateTime = LocalDateTime.parse(time, DateTimeFormat.forPattern(timeFormat)).toDateTime(DateTimeZone.UTC);

        double timeInSeconds = (dateTime.getMillis() - timeOffsetMillis) / 1000.;
        LOG.debug("time={} --> dateTime={} --> seconds with offset=" + timeInSeconds, time, dateTime);
        return timeInSeconds;
    }
}
