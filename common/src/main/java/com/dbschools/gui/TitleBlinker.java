/*
 * QuickQuiz
 * Copyright (C) 2005 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.dbschools.gui;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * Blinks the title in a border to call attention to it.
 * 
 * @author David C. Briccetti
 */
public final class TitleBlinker {

    private static final int DEFAULT_BLINK_INTERVAL = 250;
    private static final int DEFAULT_BLINKING_DURATION = 2000;
    private static final Color DEFAULT_BLINKING_COLOR = Color.red;
    private static Set<JPanel> currentBlinkers =
            Collections.synchronizedSet(new HashSet<JPanel>());
    private final TitledBorder titledBorder;
    private final JPanel borderedPanel;
    private final int blinkInterval = DEFAULT_BLINK_INTERVAL;
    private final int blinkingDuration = DEFAULT_BLINKING_DURATION;
    private Color blinkingColor = DEFAULT_BLINKING_COLOR;

    public TitleBlinker(final JPanel borderedPanel) {
        this.borderedPanel = borderedPanel;
        titledBorder = (TitledBorder) borderedPanel.getBorder();
        if (titledBorder == null) {
            throw new IllegalArgumentException(
                    "panel must contain a TitledBorder");
        }
    }

    public void blink() {
        /* Blink this control if we are not already blinking it */
        if (!currentBlinkers.contains(borderedPanel)) {
            currentBlinkers.add(borderedPanel);
            new Thread(new Runnable() {

                public void run() {
                    final Color normalColor = titledBorder.getTitleColor();
                    int timePassed = 0;
                    final Runnable blinkOn = new Runnable() {

                        public final void run() {
                            setTitleColor(blinkingColor);
                        }
                    };
                    final Runnable blinkOff = new Runnable() {

                        public final void run() {
                            setTitleColor(normalColor);
                        }
                    };

                    try {
                        while (timePassed < blinkingDuration) {
                            SwingUtilities.invokeAndWait(blinkOn);
                            Thread.sleep(blinkInterval);
                            SwingUtilities.invokeAndWait(blinkOff);
                            Thread.sleep(blinkInterval);

                            timePassed += blinkInterval * 2;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    currentBlinkers.remove(borderedPanel);
                }
            }, "TitleBlinker").start();
        }
    }

    private void setTitleColor(final Color color) {
        titledBorder.setTitleColor(color);
        borderedPanel.repaint();
    }
}
