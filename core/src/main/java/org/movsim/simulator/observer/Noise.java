package org.movsim.simulator.observer;

import org.movsim.utilities.MyRandom;

public class Noise {

    static final double SQRT12 = Math.sqrt(12.);

    /** Flag variable for wiener process or not. */
    private final boolean isWienerProcess;

    private final double tau;

    private final double fluctStrength;

    private double xiTime;

    public Noise(double tau, double fluctStrength) {
        xiTime = 0;
        this.fluctStrength = fluctStrength;
        this.tau = tau;
        isWienerProcess = (tau != 0) ? true : false;
    }

    public void update(double dt, double xiTime) {

        final double randomMu0Sigma1 = getUniformlyDistributedRealization();

        if (isWienerProcess) {
            final double betaTime = Math.exp(-dt / tau);
            this.xiTime = betaTime * xiTime + fluctStrength * Math.sqrt(2 * dt / tau) * randomMu0Sigma1;
        }
    }

    /**
     * calculates uniform distribution with mean=0 and variance=1.
     * 
     * @return random variable realization
     */
    private static double getUniformlyDistributedRealization() {
        final double randomVar = MyRandom.nextDouble();
        final double randomMu0Sigma1 = SQRT12 * (randomVar - 0.5);
        return randomMu0Sigma1;
    }

    public double getTimeError() {
        return xiTime;
    }

}
