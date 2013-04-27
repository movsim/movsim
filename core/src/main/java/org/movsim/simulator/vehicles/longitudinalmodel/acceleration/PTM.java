package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.autogen.NoiseParameter;
import org.movsim.simulator.vehicles.Noise;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameter;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterPTM;
import org.movsim.utilities.ProbabilityUtils;
import org.movsim.utilities.Tables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO dump out 2D-scans of acceleration
class PTM extends LongitudinalModelBase {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(IDM.class);

    private final IModelParameterPTM param;

    private double delta; // 0.5*(1-gamma)
    private double dw; // 1-wm

    private double dt;

    private Noise wienerProcess;

    final int NTABMAX = 100;
    double[] uPTatab; // [NTABMAX]; // tabulated d(U_PT)/da
    double[] uPTaatab; // [NTABMAX]; // tabulated d^2(U_PT)/da^2

    // double atab[101][inout.NYMAX];
    // int n=101;
    // double smax=60;
    // double sref=20;
    // double delta_s=smax/(n-1);
    // double vmax=v0;
    // double vref=20;
    // double delta_v=vmax/(n-1);
    // double dvmin=-vref;
    // double dvmax=vref;
    // double dvref=0;
    // double delta_dv=(dvmax-dvmin)/(n-1);

    PTM(double simulationTimestep, IModelParameterPTM parameters) {
        super(ModelName.PTM);
        this.param = parameters;
        this.dt = simulationTimestep;
        init();
        initNoise();
        initTables();
    }

    private void initNoise() {
        NoiseParameter noiseParameter = new NoiseParameter();
        noiseParameter.setFluctStrength(1); // standard wiener process
        noiseParameter.setTau(param.getTauCorrelation());
        wienerProcess = new Noise(noiseParameter);
    }

    private void init() {
        delta = 0.5 * (1 - param.getGamma());
        dw = 1 - param.getWeightMinus();
    }

    @Override
    protected IModelParameter getParameter() {
        return param;
    }

    private double get_uPTa(double a) {
        double wm = param.getWeightMinus();
        double bMax = param.getBMax();
        return (a <= -bMax) ? wm * (1 - 2 * delta) * Math.pow(a / param.getA0(), -2 * delta) : (a < bMax) ? Tables
                .intp(uPTatab, a, -bMax, bMax) : (wm + dw) * (1 - 2 * delta) * Math.pow(a / param.getA0(), -2 * delta);
    }

    private double get_uPTaa(double a) {
        double bmax = param.getBMax();
        return (a <= -bmax) ? -param.getWeightMinus() * 2 * delta * (1 - 2 * delta)
                * Math.pow(a / param.getA0(), -2 * delta - 1) : (a < bmax) ? Tables.intp(uPTaatab, NTABMAX, a, -bmax,
                bmax) : -(param.getWeightMinus() + dw) * 2 * delta * (1 - 2 * delta)
                * Math.pow(a / param.getA0(), -2 * delta - 1);
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // space dependencies modeled by speedlimits, alpha's

        // final double localT = alphaT * param.getT();
        // consider external speedlimit
        final double localV0;
        if (me.getSpeedlimit() != 0.0) {
            localV0 = Math.min(alphaV0 * getDesiredSpeed(), me.getSpeedlimit());
        } else {
            localV0 = alphaV0 * getDesiredSpeed();
        }
        // final double localA = alphaA * param.getA();

        // update dynamical variables in class scope
        wienerProcess.update(dt);

        return acc(s, v, dv, alphaT, localV0, 1);
    }

    /**
     * acceleration of the PTmodel.
     * Argument parameters:
     * s=net distance (m),
     * v=own velocity (m/s)
     * dv=approaching rate (v-v_front) to the front vehicle (m/s)
     * alpha_v0, alpha_T = multiplicators of v0 and T (flowc bottl)
     */
    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, 1, param.getV0(), 1);
    }

    private double acc(double s, double v, double dv, double alphaT, double v0Local, double aLocal) {

        // locally varying parameters

        double alphaloc = alphaT * param.getAlpha(); // alpha = vel. var. coefficient; the higher, the higher T

        // free acceleration: OVM or Gipps like
        // double amax=v0loc/tauOVM;
        double afree = (v0Local - v) / param.getTau(); // not amax*(1 - v/v0loc) wg. moegl. v0=0!
        // double afree=(v<=v0) ? amax : -amax; // Gipps like
        // if(v<0.5*v0){afree=(0.5+1.0*v/v0)*amax;}

        // ######################################
        // PT acceleration (i): Initial guess for linear generalized PT utility
        // ######################################

        double sloc = Math.max(s - param.getS0(), 0.01);
        double vloc = Math.max(v, 0.01); // not the same as v0loc!

        // double tau=(dv>sloc/taumax) ? sloc/dv : taumax; // TTC limited to taumax
        // double tau=(dv>2*sloc/taumax) ? 2*sloc/dv : taumax; // !!! other tau def
        double bcomf = 2; // !!! third def; avoids high stop-decel for stable param vals
        double taumaxmod = Math.max(param.getTauMax(), 0.5 * dv / bcomf);
        double tau = (dv > sloc / taumaxmod) ? sloc / dv : taumaxmod;

        double za = 0.5 * tau / (alphaloc * vloc); // z'(a)=const, z=arg. of std normal distr.
        double logval = Math.log(param.getA0() * param.getWeightCrash() * za / Math.sqrt(2 * Math.PI));
        if (logval <= 0) {
            System.err
                    .println("PTmodel.accSimple: zstar<0 or no solution => prob of approaching nearer than s0 or crash >1/2!");
            return (-param.getBMax());
        }
        double zstar = (logval > 0) ? -Math.sqrt(2 * logval) : 0; // guess in arg of Gaussian
        double astar = 2 / tau * (sloc / tau - dv + alphaloc * v * zstar);
        if (astar < -param.getBMax()) {
            LOG.error("PTmodel.accSimple: initial guess acc<-bmax; returning -bmax");
            return (-param.getBMax());
        }

        // boolean test=false;
        // if(test){
        // cout <<"PTmodel.accSimple: in testAcc:"<<endl;
        // cout <<" s="<<s<<" v="<<v<<" dv="<<dv<<endl;
        // cout <<" tau="<<tau<<" za="<<za<<" zstar="<<zstar<<endl;
        // cout << " 0th iteration: astar="<<astar<<endl;
        // }

        // PT acceleration (ii): Approximate solution for nonlineasr PT utility
        // two times Newton

        // first iteration (zstar from above)
        double gaussDensity = ProbabilityUtils.getGaussDensity(zstar);
        double ua = get_uPTa(astar) - param.getWeightCrash() * gaussDensity * za; // U'(a)
        double uaa = get_uPTaa(astar) + param.getWeightCrash() * gaussDensity * zstar * za * za; // U'(a)
        astar = (uaa < 0) ? astar - ua / uaa : astar;
        if (uaa >= 0) {
            System.err
                    .println("PTmodel.accSimple: Warning: U''(a)>0 => Newton wants to go to utility minimum instead maximum");
        }
        // if(testAcc){ cout << " 1th iteration: astar="<<astar<<endl;}

        // further iterations (zstar in recursion)
        for (int k = 1; k < 2; k++) {
            zstar = (dv + 0.5 * astar * tau - sloc / tau) / (alphaloc * v);
            gaussDensity = ProbabilityUtils.getGaussDensity(zstar);
            ua = get_uPTa(astar) - param.getWeightCrash() * gaussDensity * za; // U'(a)
            uaa = get_uPTaa(astar) + param.getWeightCrash() * gaussDensity * zstar * za * za; // U'(a)
            astar = (uaa < 0) ? astar - ua / uaa : astar;
            // if(testAcc){ cout <<" " <<(k+1)<<"th iteration: astar="<<astar<<endl;}
        }

        // ######################################
        // standard deviation of correlated errors in acceleration
        // ######################################

        double vara = -1 / (param.getBetaLogit() * uaa);
        double stddeva = (vara > 0) ? Math.sqrt(vara) : 0;

        if (vara <= 0) {
            LOG.error("PTmodel:accSimple:Warning: variance-1/(beta*U''(a))={} negative", vara);
        }

        // Implementing correlations with unit Wiener process
        // Wiener variable updated in next higher-level function PTmodel::acc

        double aPT = astar + stddeva * wienerProcess.getAccError();
        double aVeryNear = -0. / Math.sqrt(sloc); // -0.2/sqrt(sloc); quick hack to introduce s0 effect !!!
        double aWanted = Math.min(afree, aPT + aVeryNear);

        // nur Fehlertest!

        // if (!((aWanted>-10000)&&(aWanted<10))){
        // LOG.error("PTmodel.accSimple: acc={} not in right range!!", aWanted);
        // cerr <<"s="<<s<<" v="<<v<<" dv="<<dv<<endl;
        // //double tau=(dv>sloc/taumax) ? sloc/dv : taumax; // TTC lim. to taumax
        // double tau=(dv>2*sloc/taumax) ? 2*sloc/dv : taumax; // !! other tau def
        //
        // double za=0.5*tau/(alphaloc*max(v,0.01)); // z'(a)=const, z=arg. of standard normal distr.
        // double zstar=-sqrt(2*log(a0*wc*za/sqrt(2*PI))); // gues in arg of Gaussian
        // double astar=2/tau*(sloc/tau-dv+alphaloc*v*zstar);
        // cerr <<"Init. guess:  tau="<<tau<<" za="<<za<<" zstar="<<zstar
        // <<" astar="<<astar<<" wiener="<<wiener<<endl;
        // cerr<<"final value: aPT="<<aPT<<endl;
        // exit(-1);
        // }
        return Math.max(aWanted, -param.getBMax());
    }

    private void initTables() {
        // in File Constructor: Initialize Tables of d(U_PT)/da and d^2(U_PT)/da^2
        for (int i = 0; i < NTABMAX; i++) {
            double a = param.getBMax() * (-1 + 2 * i / ((double) (NTABMAX - 1)));
            double x = a / param.getA0();
            double lorenz = 1 / (1 + x * x);
            double g = x * Math.pow(lorenz, delta);
            double gx = Math.pow(lorenz, delta) - 2 * delta * x * x * Math.pow(lorenz, delta + 1);
            double gxx = -6 * delta * x * Math.pow(lorenz, delta + 1) + 4 * delta * (delta + 1) * Math.pow(x, 3)
                    * Math.pow(lorenz, delta + 2);
            double prefactor = param.getWeightMinus() + 0.5 * dw * (1 + Math.tanh(x));
            double cosh2 = Math.pow(Math.cosh(x), 2);

            uPTatab[i] = 1 / param.getA0() * (prefactor * gx + 0.5 * dw * g / cosh2);
            uPTaatab[i] = 1 / (param.getA0() * param.getA0())
                    * (prefactor * gxx + dw / cosh2 * (gx - Math.tanh(x) * g));
        }
    }

    // sprintf(testfileName,"%s.acctab_s_v",fname);
    // sprintf(titleString,"gap s\t\tv\t\taccSimple(s,v,%f,1,1)", dvref);
    // cout <<"\nCalculating and writing "<<testfileName<<" ..."<<endl;
    //
    // for (int is=0; is<n; is++){
    // for (int iv=0; iv<n; iv++){
    // double s=is*delta_s;
    // double v=iv*delta_v;
    // atab[is][iv]=accSimple(s,v,dvref,1,1);
    // }
    // }
    // inout.write_array2d(testfileName,0,smax,n,0,vmax,n,atab,titleString);

    // acc(s,dv)

    // sprintf(testfileName,"%s.acctab_s_dv",fname);
    // sprintf(titleString,"gap s\t\tdv\t\taccSimple(s,%f,dv,1,1)", vref);
    // cout <<"\nCalculating and writing "<<testfileName<<" ..."<<endl;
    //
    // for (int is=0; is<n; is++){
    // for (int idv=0; idv<n; idv++){
    // double s=0+is*delta_s;
    // double dv=dvmin+idv*delta_dv;
    // atab[is][idv]=accSimple(s,vref,dv,1,1);
    // }
    // }
    // inout.write_array2d(testfileName,0,smax,n, dvmin,dvmax,n, atab,titleString);

    // acc(v,dv)

    // sprintf(testfileName,"%s.acctab_v_dv",fname);
    // sprintf(titleString,"gap s\t\tdv\t\taccSimple(%f,v,dv,1,1)", sref);
    // cout <<"\nCalculating and writing "<<testfileName<<" ..."<<endl;
    //
    // for (int iv=0; iv<n; iv++){
    // for (int idv=0; idv<n; idv++){
    // double v=iv*delta_v;
    // double dv=dvmin+idv*delta_dv;
    // atab[iv][idv]=accSimple(sref,v,dv,1,1);
    // }
    // }
    // inout.write_array2d(testfileName,0,vmax,n, dvmin,dvmax,n, atab,titleString);

}
