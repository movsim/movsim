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
package org.movsim.viewer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class SwingHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SwingHelper.class);

    private SwingHelper() {
        // do not invoke
    }

    public static void setComponentSize(JComponent comp, int width, int height) {
        comp.setPreferredSize(new Dimension(width, height));
        comp.setMinimumSize(comp.getPreferredSize());
        comp.setMaximumSize(comp.getPreferredSize());
    }

    public static ImageIcon createImageIcon(Class<?> referencingClass, String path) {
        final URL imgURL = referencingClass.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        LOG.error("Couldn't find file={}", path);
        return null;
    }

    public static ImageIcon createImageIcon(Class<?> referencingClass, String path, int width, int height) {
        final URL imgURL = referencingClass.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
        LOG.error("Couldn't find file={}", path);
        return null;
    }

    public static void activateWindowClosingAndSystemExitButton(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evnt) {
                evnt.getWindow().setVisible(false);
                evnt.getWindow().dispose();
                System.exit(0);
            }
        });
    }

    public static void activateWindowClosingButton(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evnt) {
                evnt.getWindow().setVisible(false);
                evnt.getWindow().dispose();
            }
        });
    }

    public static void closeWindow(Window w) {
        w.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
    }

    public static Frame getFrame(Component c) {
        Component ret = c;
        while (ret != null && !(ret instanceof Frame)) {
            ret = ret.getParent();
        }
        return (Frame) ret;
    }

    /**
     * hue values (see, e.g., http://help.adobe.com/en_US/Photoshop/11.0/images/wc_HSB.png): h=0:red, h=0.2: yellow, h=0.35: green, h=0.5:
     * blue, h=0.65: violet, then a long magenta region
     **/
    public static Color getColorAccordingToSpectrum(double vmin, double vmax, double v) {
        assert vmax > vmin;
        // tune following values if not satisfied
        // (the floor function of any hue value >=1 will be subtracted by HSBtoRGB)

        final double hue_vmin = 1.00; // hue value for minimum speed value; red
        final double hue_vmax = 1.84; // hue value for max speed (1 will be subtracted); violetblue

        // possibly a nonlinear hue(speed) function looks nicer;
        // first try this truncuated-linear one

        float vRelative = (vmax > vmin) ? (float) ((v - vmin) / (vmax - vmin)) : 0;
        vRelative = Math.min(Math.max(0, vRelative), 1);
        final float h = (float) (hue_vmin + vRelative * (hue_vmax - hue_vmin));

        // use max. saturation
        final float s = (float) 1.0;

        // possibly a reduction of brightness near h=0.5 looks nicer;
        // first try max brightness (0-1)
        final float b = (float) 0.92;

        final int rgb = Color.HSBtoRGB(h, s, b);
        return v > 0.1 ? new Color(rgb) : Color.BLACK;
    }

}
