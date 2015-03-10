package org.movsim.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import org.junit.Test;

public class TimeUtilitiesTest {

    private static final double DELTA = 0.00001;

    @Test
    public void testConvertToSecondsStringString() {
        assertThat(TimeUtilities.convertToSeconds("1000", ""), closeTo(1000d, DELTA));
        assertThat(TimeUtilities.convertToSeconds("01:40", "mm:ss"), closeTo(100d, DELTA));
        assertThat(TimeUtilities.convertToSeconds("01:01:40", "HH:mm:ss"), closeTo(3700d, DELTA));
        assertThat(TimeUtilities.convertToSeconds("2015-10-03T10:00:00.000", "yyyy-MM-dd'T'HH:mm:ss.SSS"),
                closeTo(1443866400d, DELTA));
    }

    @Test
    public void testConvertToSecondsStringStringLong() {
        assertThat(TimeUtilities.convertToSeconds("1000", "", 1000), closeTo(999d, DELTA));
    }

}
