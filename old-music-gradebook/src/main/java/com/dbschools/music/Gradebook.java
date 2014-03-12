package com.dbschools.music;

import com.dbschools.music.editor.GroupsEditor;
import com.dbschools.music.admin.ui.MusiciansEditor;
import com.dbschools.music.assess.ui.AssessmentSummariesPanel;
import com.dbschools.music.editor.RatingsEditor;
import com.dbschools.music.editor.TempoEditor;
import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.editor.InstrumentsEditor;
import com.dbschools.music.ui.AbstractMusicApp;
import java.awt.Component;
import javax.swing.JTabbedPane;

public class Gradebook extends AbstractMusicApp {
    private static final long serialVersionUID = 1370023559060131938L;

    public static void main(

    ) {
        beginApp(new Gradebook(), args);
    }

    @Override
    protected String getApplicationTitle() {
        return "DBSchools Music Gradebook";
    }
    
    @Override
    protected Component createApplicationComponent() {
        JTabbedPane tpm = new JTabbedPane();
        JTabbedPane tpa = new JTabbedPane();
        final RemoteDao remoteDao = getRemoteDao();
        int ixm = 0;
        tpm.add("Testing", new AssessmentSummariesPanel(remoteDao,
                TermUtils.getCurrentTerm()));
        tpm.setToolTipTextAt(ixm++, "Assess and review students");
        tpm.add("Administration", tpa);
        tpm.setToolTipTextAt(ixm++, "Set up students, groups, instruments, pieces, and criteria");
        
        int ix = 0;
        tpa.add("Students", new MusiciansEditor(remoteDao));
        tpa.setToolTipTextAt(ix++, "Administer students: add, delete, move");
        tpa.add("Groups", new GroupsEditor(remoteDao));
        tpa.setToolTipTextAt(ix++, "Define Groups");
        tpa.add("Instruments", new InstrumentsEditor(remoteDao));
        tpa.setToolTipTextAt(ix++, "Define Instruments");
        tpa.add("Pieces", new TempoEditor(remoteDao));
        tpa.setToolTipTextAt(ix++, "Customize tempos");
        tpa.add("Criteria", new RatingsEditor(remoteDao));
        tpa.setToolTipTextAt(ix++, "Add and remove testing criteria");
        return tpm;
    }

}
