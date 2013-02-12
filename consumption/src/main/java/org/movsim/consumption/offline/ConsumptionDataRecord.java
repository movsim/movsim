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

public class ConsumptionDataRecord {

    /** index counter for convenience */
    private final int index;
    /** in meters */
    private final double position;
    /** in seconds */
    private final double time;
    /** in m/s */
    private final double speed;
    /** in m/s^2 */
    private final double acceleration;
    /** in radian */
    private final double grade;

    /** liter per second ? */
    private double consumptionRate;

    private double normalizedTime;

    /** liter ? */
    private double cumulatedConsumption;

    /** chosen gear */
    private int gear;

    public ConsumptionDataRecord(int index, double time, double position, double speed, double acceleration,
            double grade) {
        this.index = index;
        this.time = time;
        this.position = position;
        this.speed = speed;
        this.acceleration = acceleration;
        this.grade = grade;
        this.normalizedTime = time;
    }

    public double getTime() {
        return time;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean hasSpeed() {
        return !Double.isNaN(speed);
    }

    public double getAcceleration() {
        return acceleration;
    }

    public boolean hasAcceleration() {
        return !Double.isNaN(acceleration);
    }

    public boolean hasPosition() {
        return !Double.isNaN(position);
    }

    public double getGrade() {
        return grade;
    }

    public double getConsumptionRate() {
        return consumptionRate;
    }

    public void setConsumptionRate(double consumptionRate) {
        this.consumptionRate = consumptionRate;
    }

    public String[] csvHeader(final String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append("#Index").append(separator);
        sb.append("time(s)").append(separator);
        sb.append("normTime(s)").append(separator);
        sb.append("v(m/s)").append(separator);
        sb.append("acc(m/s^2)").append(separator);
        sb.append("grade(rad)").append(separator);
        sb.append("rate(l/s)").append(separator);
        sb.append("cumRate(l)").append(separator);
        sb.append("gear").append(separator);
        if (hasPosition()) {
            sb.append("position(m)").append(separator);
        }
        return sb.toString().split(separator);
    }

    public String[] toCsv(final String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d", index)).append(separator);
        sb.append(String.format("%.2f", time)).append(separator);
        sb.append(String.format("%.2f", normalizedTime)).append(separator);
        sb.append(String.format("%.4f", speed)).append(separator);
        sb.append(String.format("%.6f", acceleration)).append(separator);
        sb.append(String.format("%.6f", grade)).append(separator);
        sb.append(String.format("%.6f", consumptionRate)).append(separator);
        sb.append(String.format("%.6f", cumulatedConsumption)).append(separator);
        sb.append(String.format("%d", gear)).append(separator);
        if (hasPosition()) {
            sb.append(String.format("%.2f", position)).append(separator);
        }
        return sb.toString().split(separator);
    }

    public double getCumulatedConsumption() {
        return cumulatedConsumption;
    }

    public void setCumulatedConsumption(double cumulatedConsumption) {
        this.cumulatedConsumption = cumulatedConsumption;
    }

    public int getIndex() {
        return index;
    }

    public int getGear() {
        return gear;
    }

    public void setGear(int gear) {
        this.gear = gear;
    }

    public double getNormalizedTime() {
        return normalizedTime;
    }

    public void setNormalizedTime(double normalizedTime) {
        this.normalizedTime = normalizedTime;
    }

    public double getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "ConsumptionDataRecord [index=" + index + ", position=" + position + ", time=" + time + ", speed="
                + speed + ", acceleration=" + acceleration + ", grade=" + grade + ", consumptionRate="
                + consumptionRate + ", normalizedTime=" + normalizedTime + ", cumulatedConsumption="
                + cumulatedConsumption + ", gear=" + gear + "]";
    }

}
