package com.dbschools.gui;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.jnlp.FileSaveService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;

/**
 * Methods for saving files with JWS
 * @author Dave Briccetti
 */
public class JwsFileSaveUtil {
    private final static Logger log = Logger
            .getLogger(JwsFileSaveUtil.class);
    
    public static void saveStringToFile(Component parent, 
            String contents, final String[] extensions, String suggestedFilename) {
        try {
            FileSaveService fss = (FileSaveService) ServiceManager.lookup(FileSaveService.class.getName());
            ByteArrayInputStream is = new ByteArrayInputStream(contents.getBytes());
            try {
                fss.saveFileDialog(null, extensions, is,suggestedFilename);
            } catch (IOException ex) {
                log.error(ex);
            }
        } catch (UnavailableServiceException ex) {
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".html");
                }

                @Override
                public String getDescription() {
                    return "HTML";
                }
            });
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    String path = fc.getSelectedFile().getPath();
                    if (! path.endsWith(".html")) {
                        path += ".html";
                    }
                    FileWriter fw = new FileWriter(path);
                    fw.write(contents);
                    fw.close();
                } catch (IOException ex1) {
                    log.error(ex1);
                }
            }
        }
        
    }
}
