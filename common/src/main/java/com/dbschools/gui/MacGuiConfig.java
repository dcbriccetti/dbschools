package com.dbschools.gui;

public class MacGuiConfig {
    public static void config(String title) {
        System.getProperties().setProperty("apple.laf.useScreenMenuBar", "true");
        System.getProperties().setProperty(
                "com.apple.mrj.application.apple.menu.about.name",
                title);
    }
}