package org.movsim.simulator.roadnetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

public class MicroInflowQueueTest {

    @SuppressWarnings("static-method")
    @Test
    public void testTimeParsing() {
        DateTimeFormatter pattern = DateTimeFormat.forPattern("dd-MM-YYYY'T'HH:mm");

        String time = "02-01-2011T13:52";
        LocalDateTime expected = new LocalDateTime(2011, 1, 2, 13, 52);
        LocalDateTime localDateTime = LocalDateTime.parse(time, pattern);
        System.out.println("localDateTime = " + localDateTime);
        assertTrue(expected.equals(localDateTime));

        DateTime dateTime = localDateTime.toDateTime(DateTimeZone.UTC);
        System.out.println(String.format("time=%s --> dateTime=%s", time, dateTime));
        assertEquals(1293976320000L, dateTime.getMillis());
    }

}
