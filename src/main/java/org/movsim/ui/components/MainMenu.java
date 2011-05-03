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
package org.movsim.ui.components;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class MainMenu.
 */
public class MainMenu extends JPanel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(MainMenu.class);
    
    /** The menu bar. */
    private final JMenuBar menuBar;

    /**
     * Gets the menu bar.
     * 
     * @return the menu bar
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Instantiates a new main menu.
     */
    public MainMenu() {
        menuBar = new JMenuBar();

        final JMenu menuFile = new JMenu("File");
        final JMenu menuEdit = new JMenu("Edit");
        final JMenu menuHelp = new JMenu("Help");

        menuFile.setMnemonic('F');
        menuEdit.setMnemonic('E');
        menuHelp.setMnemonic('H');

        final JMenuItem menuEditItemSettings = new JMenuItem("Settings");
        final JMenuItem menuFileItemImport = new JMenuItem("Import...");
        final JMenuItem menuFileItemExport = new JMenuItem("Export...");

        final Action exitAction = new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };

        final Action helpInfoAction = new AbstractAction("About ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // appInfoDialog();
            }
        };

        menuFile.add(menuFileItemImport);
        menuFile.add(menuFileItemExport);
        menuFile.addSeparator();
        menuFile.add(exitAction);

        menuEdit.add(menuEditItemSettings);

        menuHelp.add(helpInfoAction);

        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuHelp);

        // setJMenuBar(menuBar);
    }
}
