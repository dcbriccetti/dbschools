package com.dbschools.music;

import java.awt.Component;

import com.dbschools.gui.WindowLauncher;

public final class TestMusiciansEditor {
    public static void main(String[] args) {
        new WindowLauncher().launch(
                (Component) SpringAccess.getContext()
                .getBean("musiciansEditor"), "Musicians Editor");
    }
}
