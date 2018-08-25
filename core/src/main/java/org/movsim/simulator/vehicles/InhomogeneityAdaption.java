package org.movsim.simulator.vehicles;

public class InhomogeneityAdaption {

    private double alphaT;
    private double alphaV0;

    InhomogeneityAdaption() {
        reset();
    }

    public void reset() {
        alphaT = 1;
        alphaV0 = 1;
    }

    public double alphaT() {
        return alphaT;
    }

    public double alphaV0() {
        return alphaV0;
    }

    public void setAlphaT(double alphaT) {
        assert alphaT > 0;
        this.alphaT = alphaT;

    }

    public void setAlphaV0(double alphaV0) {
        assert alphaV0 > 0;
        this.alphaV0 = alphaV0;
    }

    @Override
    public String toString() {
        return "InhomogeneityAdaption [alphaT=" + alphaT + ", alphaV0=" + alphaV0 + "]";
    }

}
