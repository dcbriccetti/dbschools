package com.dbschools.quickquiz.client;

import java.util.ResourceBundle;

/**
 * Providider of static resources, such as strings.
 * @author David C. Briccetti
 */
public class Resources {

    private static final ResourceBundle strings = ResourceBundle.getBundle("quickquiz");

    public static String getString(String key) {
        return strings.getString(key);
    }
}
