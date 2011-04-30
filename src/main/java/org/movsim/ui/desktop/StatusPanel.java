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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

// TODO: Auto-generated Javadoc
/**
 * The Class StatusPanel.
 */
public class StatusPanel extends JPanel {

    /**
     * Instantiates a new status panel.
     */
    public StatusPanel() {
        setBorder(BorderFactory.createEtchedBorder());
        final JLabel statusLabel = new JLabel("Status Panel");
        SwingHelper.setComponentSize(statusLabel, 100, 22);
        final JTextArea toDo = new JTextArea("Datenanzeige TextArea");

        add(statusLabel);
        add(toDo);
    }

}
