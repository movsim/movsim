package org.movsim.viewer.ui;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.movsim.viewer.util.SwingHelper;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int INIT_FRAME_SIZE_WIDTH = 1400;
    private static final int INIT_FRAME_SIZE_HEIGHT = 640;
    
    private CanvasPanel canvasPanel;
    StatusPanel statusPanel;

    public MainFrame(ResourceBundle resourceBundle) {
        super(resourceBundle.getString("FrameName"));

        SwingHelper.activateWindowClosingAndSystemExitButton(this);

        initLookAndFeel();
        
        canvasPanel = new CanvasPanel(resourceBundle);
        statusPanel = new StatusPanel(resourceBundle);
        
        final MovSimMenu trafficMenus = new MovSimMenu(this, canvasPanel, resourceBundle);
        trafficMenus.initMenus();
        final MovSimToolBar toolBar = new MovSimToolBar(statusPanel, canvasPanel, resourceBundle);

        add(canvasPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                canvasPanel.resized();
                canvasPanel.repaint();
            }
        });

        setLocation(0, 20);
        setSize(INIT_FRAME_SIZE_WIDTH, INIT_FRAME_SIZE_HEIGHT);
        setVisible(true);
    }
    
    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("set to system LaF");
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.updateComponentTreeUI(this);
    }
}
