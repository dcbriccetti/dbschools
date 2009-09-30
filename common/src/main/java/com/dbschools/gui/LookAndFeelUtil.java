package com.dbschools.gui;

import javax.swing.UIManager;

import org.apache.log4j.Logger;

public class LookAndFeelUtil {
    private final static Logger logger = Logger.getLogger(LookAndFeelUtil.class);

    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore
            logger.warn(e);
        }
    }
    
}