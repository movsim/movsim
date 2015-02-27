/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.consumption.offline;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.movsim.autogen.Columns;
import org.movsim.autogen.Conversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputDataParser {

    static final Logger LOG = LoggerFactory.getLogger(InputDataParser.class);

    // <CONVERSION time="HH:mm:ss" speed="0.2777777" gradient="0.01" />
    private String timeInputPattern; // = ""; // = "HH:mm:ss"; // 10:23:21 AM
    private double speedConversionFactor;
    private double accelerationConversionFactor;
    private double slopeConversionFactor;
    private double positionConversionFactor;

    private final int timeColumn;
    private final int speedColumn;
    private final int accelerationColum;
    private final int gradeColumn;
    private final int positionColumn;

    final static int MIN_COLUMNS = 4;

    public InputDataParser(Columns columns, Conversions conversions) {
        // consider shift from column count to array index
        this.timeColumn = columns.getTime() - 1;
        this.speedColumn = columns.getSpeed() - 1;
        this.accelerationColum = columns.getAcceleration() - 1;
        this.gradeColumn = columns.getGradient() - 1;
        this.positionColumn = columns.getPosition() - 1;

        this.timeInputPattern = conversions.getTime();
        this.speedConversionFactor = conversions.getSpeed();
        this.slopeConversionFactor = conversions.getGradient();
        this.positionConversionFactor = conversions.getPosition();
    }

    public ConsumptionDataRecord parse(int index, String[] line) throws NumberFormatException, IllegalArgumentException {
        if (line.length <= MIN_COLUMNS) {
            throw new NumberFormatException();
        }
        trim(line);
        if (LOG.isDebugEnabled()) {
            LOG.debug("parse={}", Arrays.toString(line));
        }
        double speed = isInputQuantity(speedColumn) ? speedConversionFactor * Double.parseDouble(line[speedColumn])
                : Double.NaN;
        double timeSecondsOfDay = convertToSeconds(line[timeColumn]);
        DateTime timestamp = convertToDateTime(line[timeColumn]);

        double acceleration = isInputQuantity(accelerationColum) ? accelerationConversionFactor
                * Double.parseDouble(line[accelerationColum]) : Double.NaN;

        double grade = isInputQuantity(gradeColumn) ? slopeConversionFactor * Double.parseDouble(line[gradeColumn]) : 0;
        double position = isInputQuantity(positionColumn) ? positionConversionFactor
                * Double.parseDouble(line[positionColumn]) : Double.NaN;

        return new ConsumptionDataRecord(index, timeSecondsOfDay, timestamp, position, speed, acceleration, grade);
    }

    private static void trim(String[] data) {
        for (int i = 0, N = data.length; i < N; i++) {
            data[i] = data[i].trim();
        }
    }

    private double convertToSeconds(String time) throws NumberFormatException, IllegalArgumentException {
        if (timeInputPattern.equalsIgnoreCase("1")) {
            return Double.parseDouble(time);
        }
        return convertToDateTime(time).getSecondOfDay();
    }
    
    private DateTime convertToDateTime(String time) {
        if (timeInputPattern.equalsIgnoreCase("1")) {
            return DateTime.now(DateTimeZone.UTC);
        }
        DateTime dateTime = LocalDateTime.parse(time, DateTimeFormat.forPattern(timeInputPattern)).toDateTime(
                DateTimeZone.UTC);
        LOG.debug("{} --> {}", time, dateTime);
        return dateTime;
    }

    private boolean isInputQuantity(int column) {
        return column >= 0;
    }

}
