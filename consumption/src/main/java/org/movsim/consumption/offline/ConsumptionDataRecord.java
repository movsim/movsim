package org.movsim.consumption.offline;

public class ConsumptionDataRecord {

    /** index counter for convenience */
    private final int index;
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

    public ConsumptionDataRecord(int index, double time, double speed, double acceleration, double grade) {
        this.index = index;
        this.time = time;
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

    public double getAcceleration() {
        return acceleration;
    }

    public boolean hasAcceleration() {
        return !Double.isNaN(acceleration);
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

    public static String[] csvHeader(final String separator) {
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

    @Override
    public String toString() {
        return "ConsumptionDataRecord [index=" + index + ", time=" + time + ", speed=" + speed + ", acceleration="
                + acceleration + ", grade=" + grade + ", consumptionRate=" + consumptionRate
                + ", cumulatedConsumption=" + cumulatedConsumption + ", gear=" + gear + "]";
    }

    public double getNormalizedTime() {
        return normalizedTime;
    }

    public void setNormalizedTime(double normalizedTime) {
        this.normalizedTime = normalizedTime;
    }

}
