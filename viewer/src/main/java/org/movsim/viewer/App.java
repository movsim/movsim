package org.movsim.viewer;

import java.util.Locale;
import java.util.ResourceBundle;

import org.movsim.viewer.ui.MainFrame;
import org.movsim.viewer.util.LocalizationStrings;

public class App {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName(),
                Locale.getDefault());
        
        new MainFrame(resourceBundle);
    }
}
