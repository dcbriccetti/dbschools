package com.dbschools.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class WindowLauncher {
    
    private int defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE;

    public final WindowLauncher setDefaultCloseOperation(int defaultCloseOperation) {
        this.defaultCloseOperation = defaultCloseOperation;
        return this;
    }

    public void launch(Component window, final String title) {
        JFrame frame = new JFrame(title);
        frame.getContentPane().add(window, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(defaultCloseOperation);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
