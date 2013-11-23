package com.dbschools.music.admin.ui;

import java.io.Serializable;

/**
 * A batch of musician import records.
 * @author Dave
 */
public class MusicianImportBatch implements Serializable {
    private static final long serialVersionUID = 832977817783859091L;
    
    private final String[] importText;
    private final int groupId;
    private final int termId;
    
    public MusicianImportBatch(final String[] importText, final int groupId, final int termId) {
        super();
        this.importText = importText;
        this.groupId = groupId;
        this.termId = termId;
    }

    public int getGroupId() {
        return groupId;
    }
    
    public String[] getImportText() {
        return importText;
    }
    
    public int getTermId() {
        return termId;
    }
    
}