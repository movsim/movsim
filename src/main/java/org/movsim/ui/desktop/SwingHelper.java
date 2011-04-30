/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
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
package org.movsim.ui.desktop;

import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

// TODO: Auto-generated Javadoc
/**
 * The Class SwingHelper.
 */
public class SwingHelper {

    /**
     * Sets the component size.
     * 
     * @param comp
     *            the comp
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public static void setComponentSize(JComponent comp, int width, int height) {
        comp.setPreferredSize(new Dimension(width, height));
        comp.setMinimumSize(comp.getPreferredSize());
        comp.setMaximumSize(comp.getPreferredSize());
    }

    /**
     * Creates the image icon.
     * 
     * @param bezugsklasse
     *            the bezugsklasse
     * @param path
     *            the path
     * @return the image icon
     */
    public static ImageIcon createImageIcon(Class bezugsklasse, String path) {
        // Nutzung des klasseneigenen ClassLoaders f�r die Suche nach dem Bild
        // System.out.println(bezugsklasse);
        final URL imgURL = bezugsklasse.getResource(path);
        if (imgURL != null)
            return new ImageIcon(imgURL);
        else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Creates the image icon.
     * 
     * @param bezugsklasse
     *            the bezugsklasse
     * @param path
     *            the path
     * @param width
     *            the width
     * @param height
     *            the height
     * @return the image icon
     */
    public static ImageIcon createImageIcon(Class bezugsklasse, String path, int width, int height) {
        // Nutzung des klasseneigenen ClassLoaders f�r die Suche nach dem Bild

        final URL imgURL = bezugsklasse.getResource(path);
        // System.out.println(bezugsklasse);
        if (imgURL != null)
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Creates the image icon.
     * 
     * @param path
     *            the path
     * @param width
     *            the width
     * @param height
     *            the height
     * @return the image icon
     */
    public static ImageIcon createImageIcon(String path, int width, int height) {
        // Nutzung des klasseneigenen ClassLoaders f�r die Suche nach dem Bild

        return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));

    }
}
