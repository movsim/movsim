package org.movsim.simulator.observer;

import org.movsim.utilities.MyRandom;

public class Noise {

    private static final double SQRT12 = Math.sqrt(12.);

    /** Flag variable for Ornstein-Uhlenbeck process (producing temporally correlated values centered around zero) */
    private final boolean isOrnsteinUhlenbeckProcess;

    private final double tau;
    private final double fluctuationStrength;
    private double xiTime;

    public Noise(double tau, double fluctuationStrength) {
        this.fluctuationStrength = fluctuationStrength;
        this.tau = tau;
        xiTime = 0;
        isOrnsteinUhlenbeckProcess = tau != 0;
    }

    public void update(double dt, double xiTime) {
        final double randomMu0Sigma1 = getUniformlyDistributedRealization();
        if (isOrnsteinUhlenbeckProcess) {
            final double betaTime = Math.exp(-dt / tau);
            this.xiTime = betaTime * xiTime + fluctuationStrength * Math.sqrt(2 * dt / tau) * randomMu0Sigma1;
        }
    }

    /**
     * Calculates uniform distribution with mean=0 and variance=1.
     *
     * @return random variable realization
     */
    private static double getUniformlyDistributedRealization() {
        final double randomVar = MyRandom.nextDouble();
        return SQRT12 * (randomVar - 0.5);
    }

    public double getTimeError() {
        return xiTime;
    }

}
