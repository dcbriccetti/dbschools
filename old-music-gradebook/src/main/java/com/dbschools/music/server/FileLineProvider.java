package com.dbschools.music.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * Provides an Iterable for the String contents of a file.
 * @author David C. Briccetti
 */
class FileLineProvider implements Iterable<String> {
    private String line;
    final BufferedReader reader;
    
    public FileLineProvider(final String filename) throws IOException {
        reader = getReaderForFile(filename);
        getNextNonCommentLine();
    }

    public Iterator<String> iterator() {
        return new Iterator<String>() {

            public boolean hasNext() {
                return line != null;
            }

            public String next() {
                String result = line; // Get the value
                
                // Read ahead
                try {
                    getNextNonCommentLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    line = null;
                }
                return result;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }};
    }
    
    private void getNextNonCommentLine() throws IOException {
        do {
            line = reader.readLine();
            if (line != null) {
                line = line.trim();
            }
        } while (line != null && (line.length() == 0 || line.startsWith("#")));
    }

    private static BufferedReader getReaderForFile(final String filename) {
        return new BufferedReader(new InputStreamReader(
                DefaultDataCreator.class.getResourceAsStream(
                        filename)));
    }
    
}