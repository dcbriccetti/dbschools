package com.dbschools.music;

import com.dbschools.music.orm.Group;
import com.dbschools.music.ui.StudentSelectorDialog;
import com.dbschools.music.ui.AbstractGroupAndInstrumentFilteredTableModel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.util.Collection;

import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.MusicianGroup;
import javax.swing.JLabel;

/**
 * Miscellaneous utility methods that eventually may be moved elsewhere.
 * 
 * @author David C. Briccetti
 */
public class Utils {

    public static Instrument primaryInstrumentForMusician(
            Collection<MusicianGroup> musicianGroups) {
        if (musicianGroups != null) {
            for (MusicianGroup mmg : musicianGroups) {
                if (mmg.getSchoolYear().equals(TermUtils.getCurrentTerm()) && 
                        mmg.getGroup().isDoesTesting()) {
                    return mmg.getInstrument();
                }
            }
        }
        return null;
    }

    /**
     * Displays and processes the student selector dialog.
     * @param dlg
     * @param model
     * @return Whether the user selected OK
     */
    public static boolean processStudentSelectorDialog(final StudentSelectorDialog dlg,
            AbstractGroupAndInstrumentFilteredTableModel model) {
        dlg.setVisible(true);
        if (! dlg.isCanceled()) {
            model.setGroupFilter(dlg.getSelectedGroups());
            model.setInstrumentFilter(dlg.getSelectedInstruments());
        }
        return ! dlg.isCanceled();
    }

    public static Frame getDialogParent(Component component) {
        Container parent = component.getParent();
        return (Frame) (parent instanceof Frame ? parent : null);
    }

    public static void buildShowingLabelValue(StudentSelectorDialog studentSelectorDialog,
            JLabel nowShowingLabel) {
        StringBuilder sb = new StringBuilder("Showing ");
        final Collection<Instrument> selInst = studentSelectorDialog.getSelectedInstruments();
        if (selInst.size() == 1) {
            sb.append(selInst.iterator().next().getName());
        } else if (selInst.size() == 0) {
            sb.append("no instruments");
        } else if (selInst.size() == studentSelectorDialog.getInstrumentsCount()) {
            sb.append("all instruments");
        } else {
            sb.append(selInst.size());
            sb.append(" instruments");
        }
        sb.append(" in ");
        final Collection<Group> selGrps = studentSelectorDialog.getSelectedGroups();
        if (selGrps.size() == 1) {
            sb.append(selGrps.iterator().next().getName());
        } else if (selGrps.size() == 0) {
            sb.append("no groups");
        } else if (selGrps.size() == studentSelectorDialog.getGroupsCount()) {
            sb.append("all groups");
        } else {
            sb.append(selGrps.size());
            sb.append(" groups");
        }
        nowShowingLabel.setText(sb.toString());
    }

}
