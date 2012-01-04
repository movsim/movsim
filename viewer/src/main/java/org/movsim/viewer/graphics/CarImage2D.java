/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <movsim@akesting.de>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.viewer.graphics;

import java.awt.Color;
import java.awt.Graphics;

public class CarImage2D {

    static final boolean CAR2D_DRAWBLACKLINES = true;

    static final boolean CAR2D_DRAW_MOREBLACKLINES = true; // also roof etc

    private static final double MAX_PKW_LENGTH_M = 9.; // 11

    double length_m;

    double width_m;

    double scale; // pixel units/m

    final String vehtype;

    Color color;

    // int xsize_pix, ysize_pix;
    double posx_m, posy_m;

    int posx_pix, posy_pix;

    double phiDeg; // phi=0 => car drives from left to right (x direction)

    // phi=90 => car drives in y direction
    double thetaDeg; // theta=0 => view from vertically above (z axis)

    // theta=0 => view from horizontal in direction of y axis

    public CarImage2D(double vehWidth, double length_m, Color color, double scale) {
        // scale=size in pixel / (size in m)

        this.length_m = length_m;
        this.width_m = vehWidth;

        this.color = color;
        this.scale = scale;

        this.vehtype = (length_m <= MAX_PKW_LENGTH_M) ? "car" : "truck";

        // this.xsize_pix= xsize_pix;
        // this.ysize_pix= ysize_pix;

    }

    static int myround(double x) {
        return (int) Math.floor(x + 0.5);
    }

    private int getx(double x_m) {
        return (myround(scale * x_m));
    }

    private int gety(double y_m) {
        return (myround(scale * y_m));
    }

    private int getdx(double dx_m) {
        return (myround(scale * dx_m));
    }

    private int getdy(double dy_m) {
        return (myround(-scale * dy_m));
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setPosition(double posx_m, double posy_m) {
        this.posx_m = posx_m;
        this.posy_m = posy_m;
        posx_pix = getx(posx_m);
        posy_pix = gety(posy_m);

    }

    public void setPositionPix(int posx_pix, int posy_pix) {
        this.posx_pix = posx_pix;
        this.posy_pix = posy_pix;
    }

    public void setDirection(double phiDeg) {
        // phi=0 => car drives from left to right (x direction)
        // phi=90 => car drives in y direction
        this.phiDeg = phiDeg;
    }

    public void setPerspective(double thetaDeg) {
        // theta=0 => view from vertically above (z axis)
        // theta=0 => view from horizontal in direction of y axis
        this.thetaDeg = thetaDeg;
    }

    public void draw(Graphics g) {

        // System.out.println("CarImage2D.draw: " + " posx_pix = " + posx_pix +
        // " posy_pix = " + posy_pix + " length_m = " +
        // length_m);

        // #########################################################
        // points: all relative to center at (posx_m,posy_m)
        // #########################################################

        // projection of front and back windows and roof

        // vehtype.equals("car")
        double xFrontWindow2 = 0.25 * length_m; // 0.20 (var 0.20, LKW)
        double xFrontWindow1 = 0.05 * length_m; // 0.05 (var 0.05, LKW)
        double xBackWindow1 = -0.22 * length_m; // -0.22 (var-0.35, LKW)
        double xBackWindow2 = -0.40 * length_m; // -0.35 (var-0.48, LKW-0.50)
        double yRoofLeft = 0.40 * width_m; // 0.40 (LKW 0.50)

        if (vehtype.equals("variant")) {
            xFrontWindow2 = 0.25 * length_m; // 0.20 (var 0.20, LKW)
            xFrontWindow1 = 0.05 * length_m; // 0.05 (var 0.05, LKW)
            xBackWindow1 = -0.25 * length_m; // -0.22 (var-0.35, LKW)
            xBackWindow2 = -0.48 * length_m; // -0.35 (var-0.48, LKW-0.50)
            yRoofLeft = 0.40 * width_m; // 0.40 (LKW 0.50)
        }

        if (vehtype.equals("truck")) {
            xFrontWindow2 = 0.30 * length_m; // 0.20 (var 0.20, LKW)
            xFrontWindow1 = 0.25 * length_m; // 0.05 (var 0.05, LKW)
            xBackWindow1 = -0.48 * length_m; // -0.22 (var-0.35, LKW)
            xBackWindow2 = -0.49 * length_m; // -0.35 (var-0.48, LKW-0.50)
            yRoofLeft = 0.50 * width_m; // 0.40 (LKW 0.50)
        }

        // left "hood line"

        final double xHood1 = xFrontWindow2 + 0.05 * length_m;
        final double xHood2 = 0.45 * length_m;
        final double yHood1 = 0.35 * width_m;
        final double yHood2 = 0.25 * width_m;

        // left mirror (front side of mirror attached at xFrontWindow2)

        final double xMirr1 = xFrontWindow2 - 0.04 * length_m; // tip of mirror
        final double xMirr2 = xFrontWindow2 - 0.03 * length_m; // "mirror" side
        final double yMirr1 = 0.60 * width_m;
        final double yMirr2 = 0.50 * width_m;

        // ###### end "design interface" #################

        // rel coord vehicle at posx=posy=angle=0 (driving in pos. x direction)

        final double xBackLeft = -0.5 * length_m;
        final double xBackRight = -0.5 * length_m;
        final double xFrontLeft = +0.5 * length_m;
        final double xFrontRight = +0.5 * length_m;

        final double yBackLeft = +0.5 * width_m;
        final double yBackRight = -0.5 * width_m;
        final double yFrontLeft = +0.5 * width_m;
        final double yFrontRight = -0.5 * width_m;

        final double xCorners[] = { xBackLeft, xBackRight, xFrontRight, xFrontLeft };
        final double yCorners[] = { yBackLeft, yBackRight, yFrontRight, yFrontLeft };

        final double xFrontw[] = { xFrontWindow1, xFrontWindow1, xFrontWindow2, xFrontWindow2 };
        final double yFrontw[] = { yRoofLeft, -yRoofLeft, -0.5 * width_m, 0.5 * width_m };

        final double xBackw[] = { xBackWindow1, xBackWindow1, xBackWindow2, xBackWindow2 };
        final double yBackw[] = { yRoofLeft, -yRoofLeft, -0.5 * width_m, 0.5 * width_m };

        final double xRoof[] = { xBackWindow1, xBackWindow1, xFrontWindow1, xFrontWindow1 };
        final double yRoof[] = { yRoofLeft, -yRoofLeft, -yRoofLeft, yRoofLeft };

        final double xHood[] = { xHood1, xHood2, xHood1, xHood2 };
        final double yHood[] = { yHood1, yHood2, -yHood1, -yHood2 };
        final double xMirrL[] = { xFrontWindow2, xMirr1, xMirr2 };
        final double yMirrL[] = { 0.50 * width_m, yMirr1, yMirr2 };
        final double xMirrR[] = { xFrontWindow2, xMirr1, xMirr2 };
        final double yMirrR[] = { -0.50 * width_m, -yMirr1, -yMirr2 };

        // rotate + scale to pixels

        final double phi = phiDeg * Math.PI / 180.;
        final double theta = thetaDeg * Math.PI / 180.;
        final double cp = Math.cos(phi);
        final double sp = Math.sin(phi);

        final int xCornersPix[] = new int[4];
        final int yCornersPix[] = new int[4];
        final int xFrontwPix[] = new int[4];
        final int yFrontwPix[] = new int[4];
        final int xBackwPix[] = new int[4];
        final int yBackwPix[] = new int[4];
        final int xRoofPix[] = new int[4];
        final int yRoofPix[] = new int[4];
        final int xHoodPix[] = new int[4];
        final int yHoodPix[] = new int[4];
        final int xMirrLPix[] = new int[3];
        final int yMirrLPix[] = new int[3];
        final int xMirrRPix[] = new int[3];
        final int yMirrRPix[] = new int[3];

        for (int i = 0; i < 4; i++) {
            xCornersPix[i] = posx_pix + getdx(cp * xCorners[i] - sp * yCorners[i]);
            yCornersPix[i] = posy_pix + getdy(sp * xCorners[i] + cp * yCorners[i]);

            xFrontwPix[i] = posx_pix + getdx(cp * xFrontw[i] - sp * yFrontw[i]);
            yFrontwPix[i] = posy_pix + getdy(sp * xFrontw[i] + cp * yFrontw[i]);

            xBackwPix[i] = posx_pix + getdx(cp * xBackw[i] - sp * yBackw[i]);
            yBackwPix[i] = posy_pix + getdy(sp * xBackw[i] + cp * yBackw[i]);

            xRoofPix[i] = posx_pix + getdx(cp * xRoof[i] - sp * yRoof[i]);
            yRoofPix[i] = posy_pix + getdy(sp * xRoof[i] + cp * yRoof[i]);
            xHoodPix[i] = posx_pix + getdx(cp * xHood[i] - sp * yHood[i]);
            yHoodPix[i] = posy_pix + getdy(sp * xHood[i] + cp * yHood[i]);
        }

        for (int i = 0; i < 3; i++) {
            xMirrLPix[i] = posx_pix + getdx(cp * xMirrL[i] - sp * yMirrL[i]);
            yMirrLPix[i] = posy_pix + getdy(sp * xMirrL[i] + cp * yMirrL[i]);
            xMirrRPix[i] = posx_pix + getdx(cp * xMirrR[i] - sp * yMirrR[i]);
            yMirrRPix[i] = posy_pix + getdy(sp * xMirrR[i] + cp * yMirrR[i]);
        }

        // draw r

        g.setColor(color);
        g.fillPolygon(xCornersPix, yCornersPix, 4);
        g.setColor(color.brighter().brighter());
        g.fillPolygon(xFrontwPix, yFrontwPix, 4);
        g.setColor(color.darker().darker());
        g.fillPolygon(xBackwPix, yBackwPix, 4);

        final int wPix = Math.abs(xCornersPix[2] - xCornersPix[0]);
        boolean draw_blacklines = (CAR2D_DRAWBLACKLINES && (wPix > 8));
        boolean draw_more_blacklines = (CAR2D_DRAW_MOREBLACKLINES && (wPix > 12));

        // apr05 draw_blacklines>8 and draw_more_blacklines>12 changed
        // apr05 apply changes only to non-VLA vehicles
        // vw 2010: apply also to VLA vehicles
        // if (color.equals(new Color(CAR_COLOR[0], CAR_COLOR[1],
        // CAR_COLOR[2]))) {
        draw_blacklines = (CAR2D_DRAWBLACKLINES && (wPix > 3));
        draw_more_blacklines = (CAR2D_DRAW_MOREBLACKLINES && (wPix > 8));
        // }

        // System.out.println("xCornersPix[2]=" + xCornersPix[2] +
        // " CornersPix[0]=" + xCornersPix[0]);

        if (draw_blacklines) {
            g.setColor(Color.black);
            g.drawPolygon(xCornersPix, yCornersPix, 4);
            if (draw_more_blacklines) {
                g.drawPolygon(xFrontwPix, yFrontwPix, 4);
                g.drawPolygon(xBackwPix, yBackwPix, 4);
                g.drawPolygon(xRoofPix, yRoofPix, 4);
                g.drawLine(xHoodPix[0], yHoodPix[0], xHoodPix[1], yHoodPix[1]);
                g.drawLine(xHoodPix[2], yHoodPix[2], xHoodPix[3], yHoodPix[3]);
                final boolean draw_mirror = false;
                if (draw_mirror) {
                    g.drawPolygon(xMirrLPix, yMirrLPix, 3);
                    g.drawPolygon(xMirrRPix, yMirrRPix, 3);
                }
            }
        }
    }

}
