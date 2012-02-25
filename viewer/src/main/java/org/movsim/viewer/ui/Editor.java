/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.movsim.viewer.util.SwingHelper;

/**
 * @author ralph
 * 
 */
public class Editor extends JFrame {
    private static final long serialVersionUID = 2162057086169952290L;
    private final JTextArea textarea;
    private final File file;

    public Editor(ResourceBundle resourceBundle, File file) {
        super(resourceBundle.getString("TitleEditor"));
        this.file = file;
        setLayout(new BorderLayout());
        SwingHelper.activateWindowClosingButton(this);

        final JLabel lblFile = new JLabel(file.getName());
        final JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());

        textarea = new JTextArea("", 24, 80);
        textarea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        textarea.setEditable(false);

        final JScrollPane scrollPane = new JScrollPane(textarea);

        FileReader reader;
        try {
            reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            textarea.read(br, null);
            br.close();
            textarea.requestFocus();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        textarea.setCaretPosition(0); // Go to start of file

        textPanel.add(lblFile, BorderLayout.NORTH);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        add(textPanel);
        pack();
        setVisible(true);

    }
}
