package com.dbschools.music.admin.ui;

import com.dbschools.gui.ActionUtil;
import com.dbschools.music.events.Event;
import com.dbschools.music.ui.StudentSelectorDialog;
import com.dbschools.music.*;
import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.decortrs.YearDecorator;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import com.dbschools.gui.PopupListener;
import com.dbschools.gui.TableUtil;
import com.dbschools.music.admin.ui.MusicianEditor.GroupAndInstrument;
import com.dbschools.music.events.EventObserver;
import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.orm.NamedItem;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.util.TreeSet;
import javax.swing.KeyStroke;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 * Editor for a set of musicians.
 * 
 * @author David C. Briccetti
 */
public class MusiciansEditor extends javax.swing.JPanel {
    
    private static final long serialVersionUID = -4337059976717627682L;
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger(MusiciansEditor.class);
    private final MusicianTableModel musicianTableModel;
    private final StudentSelectorDialog studentSelectorDialog;
    private final RemoteDao remoteDao;
    private final MusicianMover musicianMover;
    private final Collection<Instrument> instruments;
    private Integer selectedTerm;
    private final Frame dialogParent = Utils.getDialogParent(this);
    private final AbstractAction deleteAction;
    private final JMenu moveToGroupMenu = new JMenu("Move to group");
    private final JMenu moveToInstrumentMenu = new JMenu("Move to instrument");

    public MusiciansEditor(RemoteDao remoteDao) {
        this.remoteDao = remoteDao;
        studentSelectorDialog = new StudentSelectorDialog(
                Utils.getDialogParent(this), remoteDao, false);
        musicianTableModel = new MusicianTableModel(remoteDao);
        initComponents();
        musicianTable.setColumnControlVisible(true);
        musicianTable.setHighlighters(HighlighterFactory.createSimpleStriping());
        final int preferredColWidths[] = { 120, 60, 30, 20, 75, 75 };
        final int maxColWidths[] = { 0, 60, 30, 20, 0, 0 };
        final int minColWidths[] = { 10, 0, 0, 0, 0, 0, 0, 0, 0  };
        TableUtil.setColumnWidths(preferredColWidths, maxColWidths, 
            minColWidths, musicianTable.getColumnModel(), null);
        instruments = new TreeSet<Instrument>(remoteDao.getInstruments());
        termCombo.removeAllItems();
        final int currentTerm = TermUtils.getCurrentTerm();
        for (Integer aTerm : getSortedTerms()) {
            final YearDecorator decoratedYear = new YearDecorator(aTerm);
            termCombo.addItem(decoratedYear);
            if (aTerm == currentTerm) {
                termCombo.setSelectedItem(decoratedYear);
            }
        }
        musicianMover = new MusicianMover(musicianTableModel, remoteDao, instruments.iterator().next());
        deleteAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final int numSel = musicianTable.getSelectedRowCount();
                if (numSel > 0) {
                    if (JOptionPane.showConfirmDialog(Utils.getDialogParent(MusiciansEditor.this), 
                            "Are you sure you want to delete the " +
                            numSel +
                            " student(s), along with any testing history?",
                            "Delete Musicians", JOptionPane.YES_NO_OPTION) 
                            == JOptionPane.OK_OPTION) {
                        deleteSelectedMusicians();
                    }
                }
            }};
        deleteAction.putValue(Action.NAME, "Delete...");
        deleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
            KeyEvent.VK_DELETE, 0));
        setUpPopupMenu();
        ActionUtil.attachDeleteAction(musicianTable, deleteAction);
        Utils.buildShowingLabelValue(studentSelectorDialog, nowShowingLabel);
        remoteDao.addEventObserver(new EventObserver() {

            public void notify(Event event) {
                if (event.getDetails() instanceof Instrument) {
                    buildMoveToInstrumentMenu();
                } else if (event.getDetails() instanceof Group) {
                    buildMoveToGroupMenu();
                }
            }
        });
    }

    private void buildMoveToGroupMenu() {
        moveToGroupMenu.removeAll();
        Collection<Group> groups = remoteDao.getGroups();
        for (Group group : groups) {
            moveToGroupMenu.add(new MoveMenuItem(group, 
                    getMoveToGroupAction(group.getName())));
        }
    }

    private void buildMoveToInstrumentMenu() {
        moveToInstrumentMenu.removeAll();
        instruments.clear();
        instruments.addAll(remoteDao.getInstruments());
        for (Instrument inst : instruments) {
            moveToInstrumentMenu.add(new MoveMenuItem(inst, 
                    getMoveToInstrumentAction(inst.getName())));
        }
    }

    private Collection<Integer> getSortedTerms() {
        List<Integer> sortedTerms = new ArrayList<Integer>(remoteDao.getSchoolYears());
        addThisYearAndNextIfNeeded(sortedTerms);
        Collections.sort(sortedTerms);
        return Collections.unmodifiableList(sortedTerms);
    }

    private void addThisYearAndNextIfNeeded(List<Integer> sortedTerms) {
        final Integer currentYear = TermUtils.getCurrentTerm();
        for (int i = 0; i <= 1; ++i) {
            if (! sortedTerms.contains(currentYear + i)) {
                sortedTerms.add(currentYear + i);
            }
        }
    }

    static class MoveMenuItem extends JMenuItem {
        private static final long serialVersionUID = 5126026060001067612L;
        private final NamedItem namedItem;

        public MoveMenuItem(NamedItem namedItem, Action action) {
            super(action);
            this.namedItem = namedItem;
        }

        public NamedItem getNamedItem() {
            return namedItem;
        }
    }
    
    private Action getMoveToGroupAction(String name) {
        return new AbstractAction(name) {
                public void actionPerformed(ActionEvent e) {
                final Group movingIntoMusicGroup = 
                        (Group) ((MoveMenuItem) e.getSource()).getNamedItem();
                musicianMover.moveSelectedToGroup(selectedTerm, 
                        movingIntoMusicGroup,
                        createSelectedIterator(), false);
                }
            };
    }
    
    private Action getMoveToInstrumentAction(String name) {
        return new AbstractAction(name) {
                public void actionPerformed(ActionEvent e) {
                    final Instrument movingIntoInstrument = 
                            (Instrument) ((MoveMenuItem) e.getSource()).getNamedItem();
                    musicianMover.moveSelectedToInstrument(selectedTerm,
                            movingIntoInstrument,
                            createSelectedIterator());
                }
        };
    }
    
    private void setUpPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        buildMoveToGroupMenu();
        popup.add(moveToGroupMenu);
        buildMoveToInstrumentMenu();
        popup.add(moveToInstrumentMenu);
        popup.add(new JMenuItem(deleteAction));
        
        //Add listener to components that can bring up popup menus.
        musicianTable.addMouseListener(new PopupListener(musicianTable, popup));
        musicianTable.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedMusicians();
                }
            }});
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tableScrollPane = new javax.swing.JScrollPane();
        musicianTable = new org.jdesktop.swingx.JXTable();
        buttonPanel = new javax.swing.JPanel();
        termCombo = new javax.swing.JComboBox();
        showButton = new javax.swing.JButton();
        nowShowingLabel = new javax.swing.JLabel();
        newButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        musicianTable.setModel(musicianTableModel);
        tableScrollPane.setViewportView(musicianTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(tableScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        termCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        termCombo.setToolTipText("Select the term from which current information is to be displayed, and new information is to be added");
        termCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                termComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(termCombo, gridBagConstraints);

        showButton.setMnemonic('S');
        showButton.setText("Show...");
        showButton.setToolTipText("Select the groups and instruments for which musicians are to be displayed");
        showButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(showButton, gridBagConstraints);

        nowShowingLabel.setText("Now Showing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(nowShowingLabel, gridBagConstraints);

        newButton.setMnemonic('N');
        newButton.setText("New...");
        newButton.setToolTipText("Create a new musician");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(newButton, gridBagConstraints);

        copyButton.setMnemonic('C');
        copyButton.setText("Copy");
        copyButton.setToolTipText("Copy all showing musicians to the clipboard sorted by group and instrument");
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(copyButton, gridBagConstraints);

        importButton.setText("Import...");
        importButton.setToolTipText("Paste in records from an Aeries report");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        buttonPanel.add(importButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        doImport();
    }//GEN-LAST:event_importButtonActionPerformed

    private void doImport() {
        ImportDialog id = new ImportDialog(dialogParent, true);
        id.setGroups(remoteDao.getGroups());
        id.pack();
        id.setLocationRelativeTo(this);
        id.setVisible(true);
        if (! id.isCanceled()) {
            String text = id.getText();
            remoteDao.save(new MusicianImportBatch(text.split(System.getProperty("line.separator")),  
                    id.getSelectedGroup().getId(), selectedTerm));
        }
    }

    private void termComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_termComboActionPerformed
        final Object selectedItem = termCombo.getSelectedItem();
        if (selectedItem != null) {
            selectedTerm = ((YearDecorator) selectedItem).getYear();
            musicianTableModel.setTermFilter(Arrays.asList(selectedTerm));
            musicianTableModel.setSchoolYear(selectedTerm);
        }
    }//GEN-LAST:event_termComboActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
        musicianTableModel.copyToClipboard();
    }//GEN-LAST:event_copyButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        editMusician(createEditDialog(), new Musician());
    }//GEN-LAST:event_newButtonActionPerformed

    private void showButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showButtonActionPerformed
        selectStudents();
        Utils.buildShowingLabelValue(studentSelectorDialog, nowShowingLabel);
    }//GEN-LAST:event_showButtonActionPerformed

    private void selectStudents() {
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Utils.processStudentSelectorDialog(studentSelectorDialog, musicianTableModel);
    	setCursor(Cursor.getDefaultCursor());
    }
    
    private SelectedMusicianIterator createSelectedIterator() {
        return new SelectedMusicianIterator(convertTableToModelRows(
                musicianTable.getSelectedRows()), musicianTableModel);
    }

    private void editSelectedMusicians() {
        MusicianEditorDialog dialog = createEditDialog();
        
        for (Musician musician : createSelectedIterator()) {
            editMusician(dialog, musician);
        }
        musicianTableModel.fireTableDataChanged();
    }
    
    private void editMusician(MusicianEditorDialog dialog, Musician musician) {
        dialog.setSchoolYear(selectedTerm);
        dialog.setMusician(musician);
        Collection<GroupAndInstrument> giList = createGroupAndInstrumentList(musician);
        dialog.setGroupAndInstrumentAssignments(giList);
        dialog.setLocationRelativeTo(dialogParent);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            saveMusician(dialog);
        }
    }
    
    private Collection<GroupAndInstrument> createGroupAndInstrumentList(Musician musician) {
        Iterable<MusicianGroup> mgs = musician.getMusicianGroups();
        Collection<GroupAndInstrument> giList = new ArrayList<MusicianEditor.GroupAndInstrument>();
        
        if (mgs != null) {
            for (MusicianGroup mg : mgs) {
                if (mg.getSchoolYear().equals(selectedTerm)) {
                    giList.add(new MusicianEditor.GroupAndInstrument(
                            mg.getGroup(), mg.getInstrument()));
                }
            }
        }
        return giList;
    }

    private MusicianEditorDialog createEditDialog() {
        MusicianEditorDialog dialog = new MusicianEditorDialog(
                dialogParent, true);
        dialog.setGroups(remoteDao.getGroups());
        dialog.setInstruments(remoteDao.getInstruments());
        return dialog;
    }

    private void saveMusician(MusicianEditorDialog dialog) {
        final Iterable<GroupAndInstrument> gis = 
                dialog.getGroupAndInstrumentAssignments();
        final Musician mus = dialog.getMusician();
        
        final Set<MusicianGroup> newMgs = new HashSet<MusicianGroup>();
        
        for (GroupAndInstrument gi : gis) {
            final MusicianGroup newMg = new MusicianGroup();
            newMg.setMusician(mus);
            newMg.setGroup((Group) gi.getMusicGroup());
            newMg.setSchoolYear(selectedTerm);
            newMg.setInstrument((Instrument) gi.getMusicInstrument());
            newMg.setInstrumentRanking(0);
            newMgs.add(newMg);
        }
        if (mus.getId() == null) {
            remoteDao.saveNewMusicianAndMusicGroups(selectedTerm,
                    mus, newMgs);
        } else {
            remoteDao.update(mus);
            remoteDao.saveMusicianMusicGroups(selectedTerm,
                    mus.getId(), newMgs);
        }
    }

    private int[] convertTableToModelRows(int[] rows) {
        int[] mrs = new int[rows.length];
        for (int i = 0; i < rows.length; ++i) {
            mrs[i] = musicianTable.convertRowIndexToModel(rows[i]);
        }
        return mrs;
    }
    
    private void deleteSelectedMusicians() {
        int[] selectedRows = convertTableToModelRows(musicianTable.getSelectedRows());
        List<Musician> selectedMusicians = new ArrayList<Musician>(selectedRows.length);
        for (int index : selectedRows) {
            selectedMusicians.add(musicianTableModel.getRowAt(index));
        }
        remoteDao.delete(selectedMusicians);
        musicianTable.getSelectionModel().clearSelection();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton copyButton;
    private javax.swing.JButton importButton;
    private org.jdesktop.swingx.JXTable musicianTable;
    private javax.swing.JButton newButton;
    private javax.swing.JLabel nowShowingLabel;
    private javax.swing.JButton showButton;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JComboBox termCombo;
    // End of variables declaration//GEN-END:variables
}
