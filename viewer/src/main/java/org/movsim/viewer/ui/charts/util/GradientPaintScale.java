package org.movsim.viewer.ui.charts.util;
///**
// * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
// *                             Ralph Germ, Martin Budden
// *                             <movsim@akesting.de>
// * ----------------------------------------------------------------------
// * 
// *  This file is part of 
// *  
// *  MovSim - the multi-model open-source vehicular-traffic simulator 
// *
// *  MovSim is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  MovSim is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
// *  <http://www.movsim.org>.
// *  
// * ----------------------------------------------------------------------
// */
//package org.movsim.viewer.graphics.charts.util;
//
//import java.awt.Color;
//import java.awt.Paint;
//
//import org.jfree.chart.renderer.GrayPaintScale;
//
//public class GradientPaintScale extends GrayPaintScale {
//
//    public GradientPaintScale(double lowerBound, double upperBound) {
//        super(lowerBound, upperBound);
//    }
//
//    @Override
//    public Paint getPaint(double v) {
//
//        /**
//         * hue values (see, e.g., http://help.adobe.com/en_US/Photoshop/11.0/images/wc_HSB.png): h=0:red, h=0.2: yellow, h=0.35: green,
//         * h=0.5: blue, h=0.65: violet, then a long magenta region
//         **/
//
//        // tune following values if not satisfied
//        // (the floor function of any hue value >=1 will be subtracted by
//        // HSBtoRGB)
//
//        final double hue_vmin = 1.00; // hue value for minimum speed value; red
//        final double hue_vmax = 1.76; // hue value for max speed (1 will be
//        // subtracted); violetblue
//
//        // possibly a nonlinear hue(speed) function looks nicer;
//        // first try this truncuated-linear one
//
//        final double vmax = getUpperBound();
//        final double vmin = getLowerBound();
//
//        float vRelative = (vmax > vmin) ? (float) ((v - vmin) / (vmax - vmin)) : 0;
//        vRelative = Math.min(Math.max(0, vRelative), 1);
//        final float h = (float) (hue_vmin + vRelative * (hue_vmax - hue_vmin));
//
//        // use max. saturation
//        final float s = (float) 1.0;
//
//        // possibly a reduction of brightness near h=0.5 looks nicer;
//        // first try max brightness (0-1)
//        final float b = (float) 0.82;
//
//        final int rgb = Color.HSBtoRGB(h, s, b);
//        return v >= 0 ? new Color(rgb) : Color.white;
//    }
//
//    // public Paint getPaint(double value) {
//    //
//    // //Green,red,blue gradient
//    // double upperBound = getUpperBound();
//    // double lowerBound = getLowerBound();
//    //
//    // if (value < lowerBound)
//    // return new Color(0, 0, 0);
//    // if (value > upperBound)
//    // return new Color(255, 255, 255);
//    //
//    // double scaledValue = (value - lowerBound) / (upperBound - lowerBound) *
//    // 6.0;
//    // // scaledValue > 6.0 --> value > upperBound --> very dark red
//    // // 6.0 > scaledValue > 5.0 --> Color between (127, 0, 0) and (255, 0,
//    // // 0), from dark red to red
//    // if (scaledValue > 5) {
//    // return new Color(255 - (int) (128 * (scaledValue - 5.0)), 0, 0);
//    // }
//    // // 5.0 > scaledValue > 4.0 --> Color between (255, 0, 0) and (255, 127,
//    // // 0), from red to orange
//    // if (scaledValue > 4) {
//    // return new Color(255, (int) (128 * (5.0 - scaledValue)), 0);
//    // }
//    // // 4.0 > scaledValue > 3.0 --> Color between (255, 127, 0) and (255,
//    // // 255, 0), from orange to yellow
//    // if (scaledValue > 3) {
//    // return new Color(255, 127 + (int) (128 * (4.0 - scaledValue)), 0);
//    // }
//    // // 3.0 > scaledValue > 2.0 --> Color between (255, 255, 0) and (127,
//    // // 255, 0), from yellow to yellow-green
//    // if (scaledValue > 2) {
//    // return new Color(127 + (int) (128 * (scaledValue - 2.0)), 255, 0);
//    // }
//    // // 2.0 > scaledValue > 1.0 --> Color between (127, 255, 0) and (0, 255,
//    // // 0), from yellow-green to green
//    // if (scaledValue > 1) {
//    // return new Color((int) (127 * (scaledValue - 1.0)), 255, 0);
//    // }
//    // // 1.0 > scaledValue > 0.0 --> Color between (0, 255, 0) and (0, 127,
//    // // 0), from green to dark green
//    // return new Color(0, 127 + (int) (scaledValue * 128.0), 0);
//    // // scaledValue < 0.0 --> value < lowerBound --> very dark green
//    // }
//}
