package com.dbschools.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.jnlp.ClipboardService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

/** 
 * Java Web Start clipboard methods.
 * 
 * @author David C. Briccetti
 */
public final class JwsClipboardUtil {
    
    /**
     * Returns the string contents of the JNLP clipboard, or from the system clipboard
     * if we are not running under Java Web Start.
     * 
     * @return clipboard string contents
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    public static String getStringFromJwsOrSystemClipboard() throws UnsupportedFlavorException, IOException {
        try {
            final ClipboardService cs = (ClipboardService)ServiceManager.lookup
                     (ClipboardService.class.getName());
            return (String) cs.getContents().getTransferData(
                    DataFlavor.stringFlavor);
        } catch (UnavailableServiceException e) {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().
                    getData(DataFlavor.stringFlavor);
        }

    }

    /**
     * Sets the string contents of the JNLP clipboard, or the system clipboard
     * if we are not running under Java Web Start.

     * @param contents the string to be set in the clipboard
     */
    public static void setStringToJwsOrSystemClipboard(String contents) {
        final StringSelection stringSelection = new StringSelection(contents);
        try {
            ((ClipboardService)ServiceManager.lookup
                    (ClipboardService.class.getName())).setContents(stringSelection);
        } catch (UnavailableServiceException e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().
                    setContents(stringSelection, null);
        }
    }
}
