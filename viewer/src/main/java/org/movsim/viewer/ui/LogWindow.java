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
package org.movsim.viewer.ui;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.movsim.viewer.util.SwingHelper;
import org.movsim.viewer.util.SwingLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * @author ralph
 */
public class LogWindow extends JFrame {

    private static final long serialVersionUID = 7260621844785396283L;
    private static final Logger LOG = LoggerFactory.getLogger(LogWindow.class);
    private static JTextArea jTextArea = null;

    public LogWindow(ResourceBundle resourceBundle, final JCheckBoxMenuItem cbMenu) {
        super(resourceBundle.getString("LogWindowTitle"));
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evnt) {
                cbMenu.setSelected(false);
                evnt.getWindow().setVisible(false);
                evnt.getWindow().dispose();
            }
        });

        LOG.debug("Constuctor LogWindow");

        setLocation(10, 700);

        final JPanel logPanel = new JPanel();
        logPanel.setPreferredSize(new Dimension(800, 300));
        logPanel.setLayout(new BorderLayout());

        final JScrollPane scrollPane = new JScrollPane(jTextArea);

        final JPanel logLevelChooserPanel = new JPanel();
        final String debug = resourceBundle.getString("LogDebug");
        final String info = resourceBundle.getString("LogInfo");
        final String off = resourceBundle.getString("LogOff");
        final String warn = resourceBundle.getString("LogWarn");
        final String error = resourceBundle.getString("LogError");
        final String[] logLevels = { debug, info, warn, error, off };
        final JComboBox<String> logLevel = new JComboBox<>(logLevels);
        logLevel.setSelectedItem("info");
        SwingHelper.setComponentSize(logLevel, 120, 22);

        logLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JComboBox<?> comboBox = (JComboBox<?>) e.getSource();
                final String chosenLogLevel = (String) comboBox.getSelectedItem();
                LOG.info("Changed loglevel to {}", chosenLogLevel);
                if (chosenLogLevel.equals(debug)) {
                    changeLevelOfAllLoggersTo(Level.DEBUG);
                } else if (chosenLogLevel.equals(info)) {
                    changeLevelOfAllLoggersTo(Level.INFO);
                } else if (chosenLogLevel.equals(warn)) {
                    changeLevelOfAllLoggersTo(Level.WARN);
                } else if (chosenLogLevel.equals(error)) {
                    changeLevelOfAllLoggersTo(Level.ERROR);
                } else {
                    turnOffAllLoggers();
                }
            }
        });

        final JLabel lblLogLevel = new JLabel("LogLevel: ");
        logLevelChooserPanel.add(lblLogLevel);
        logLevelChooserPanel.add(logLevel);

        logPanel.add(logLevelChooserPanel, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);

        add(logPanel, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    public static void setupLog4JAppender() {
        LogWindow.jTextArea = new JTextArea();
        SwingLogAppender.setTextArea(LogWindow.jTextArea);
    }

    public static void turnOffAllLoggers() {
        changeLevelOfAllLoggersTo(Level.OFF);
    }

    @SuppressWarnings("unchecked")
    public static void changeLevelOfAllLoggersTo(Level level) {
        final Enumeration<org.apache.log4j.Logger> loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            final org.apache.log4j.Logger l = loggers.nextElement();
            l.setLevel(level);
        }
    }
}
