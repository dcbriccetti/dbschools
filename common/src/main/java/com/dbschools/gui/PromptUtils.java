/**
 * 
 */
package com.dbschools.gui;

public class PromptUtils {
    /**
     * Adds an number and a string to an existing message. For example,
     * assume buf contains "Do you want to delete the ", numItems contains
     * 2, and msg contains "item". After calling this method, buf would
     * contain "Do you want to delete the 2 items". 
     * @param buf buffer containing the beginning of the prompt, and into
     * which the rest of the prompt will be placed
     * @param numItems the number of items
     * @param messageEnd the end of the message
     */
    public static void addMessageToPrompt(StringBuffer buf, int numItems,
            String messageEnd) {
        if (numItems > 0) {
            buf.append(numItems);
            buf.append(' ');
            buf.append(messageEnd);
            if (numItems > 1) {
                buf.append('s');
            }
        }
    }
}