/*
 * DBSchools
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

package com.dbschools.music.assess.ui;

import com.dbschools.music.Metronome;
import com.dbschools.music.ui.NamedItemDisplayAdapter;
import com.dbschools.music.dao.RemoteDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.DateFormatter;

import com.google.common.base.Nullable;
import org.apache.log4j.Logger;

import com.dbschools.gui.CustomDialog;
import com.dbschools.gui.ErrorHandler;
import com.dbschools.music.Constants;
import com.dbschools.music.Tempos;
import com.dbschools.music.Utils;
import com.dbschools.music.assess.NextPieceFinder;
import com.dbschools.music.orm.Assessment;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.NamedItem;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.Subinstrument;
import com.dbschools.music.orm.Tempo;
import com.dbschools.music.orm.User;

/**
 * Musician test dialog.
 */
public final class AssessmentRecordDialog extends CustomDialog {
    
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(AssessmentRecordDialog.class);
    private static final long serialVersionUID = -7380508514757715L;
    private Assessment assessment;
    private final Musician musician;
    private Piece selectedPiece;
    private transient final RemoteDao remoteDao;
    private transient final CommentManager commentManager;
    private final Collection<Assessment> assessments;
    private final User user;
    private final boolean editingExisting;
    private final NextPieceFinder nextPieceFinder;
    private final Collection<Piece> musicPieces;

    public AssessmentRecordDialog(RemoteDao remoteDao, 
            Musician musician, Collection<Assessment> assessments,
            @Nullable Assessment assessment) {
        super(null, true);
        this.remoteDao = remoteDao;
        this.musician = musician;
        this.assessments = assessments;
        this.assessment = assessment;
        editingExisting = assessment != null;
        user = remoteDao.getUser();
        commentManager = new CommentManager(remoteDao);
        musicPieces = remoteDao.getMusicPieces();
        nextPieceFinder = new NextPieceFinder(musicPieces);
        initComponents();
        if (editingExisting) {
            initializeForEditExisting();
        } else {
            initializeForNewAssessment();
        }
        tweakPreferredSizes();
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeForNewAssessment() {
        setUpInstruments(remoteDao.getInstruments());
        setUpDateField(new Date());
        try {
            commentManager.setUpComments(null, scrollableCommentsPanel);
            setUpPieceSelector(musicPieces, 
                    nextPieceFinder.nextPiece(assessments, null, null));
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    private void initializeForEditExisting() {
        setUpInstruments(remoteDao.getInstruments());
        setUpDateField(assessment.getAssessmentTime());
        dateCheckBox.setVisible(false);
        dateFormattedTextField.setVisible(true);
        cancelButton.setText("Cancel");
        commentManager.setUpComments(assessment.getPredefinedComments(),
                scrollableCommentsPanel);
        commentsTextArea.setText(assessment.getNotes());
        setUpPieceSelector(musicPieces, 
                assessment.getMusicPiece());
        final Tempo tempoForInstrument = Tempos.tempoForInstrument(
                assessment.getMusicPiece().getTempos(),
                assessment.getMusicInstrument(), 
                true);
        setTempoLabelAndValue(tempoForInstrument == null ? null : tempoForInstrument.getTempo());
        passCheckBox.setSelected(assessment.isPass());
    }

    private void tweakPreferredSizes() {
        Dimension preferredSize = commentsCheckPanel.getPreferredSize();
        preferredSize.height = scrollableCommentsPanel.getPreferredSize().height + 10; // Fudge for what? Insets? Borders?
        commentsCheckPanel.setPreferredSize(preferredSize);
    }

    private void setUpInstruments(Collection<Instrument> instruments) {
        instrumentComboBox.removeAllItems();
        subInstrumentComboBox.removeAllItems();
        NamedItemDisplayAdapter selectedItem = null;
        Instrument selectedInstr = assessment != null ? 
                assessment.getMusicInstrument() :
                Utils.primaryInstrumentForMusician( 
                remoteDao.getMusicianGroupsMap().get(musician.getId()));
        for (Instrument inst : instruments) {
            if (inst.getName().equals(Constants.UNASSIGNED)) {
                continue;
            }
            final NamedItemDisplayAdapter item = new NamedItemDisplayAdapter(inst);
            instrumentComboBox.addItem(item);
            if (inst.equals(selectedInstr)) {
                selectedItem = item;
            }
        }
        if (selectedItem != null) {
            instrumentComboBox.setSelectedItem(selectedItem);
        }
    }

    private void setUpPieceSelector(Iterable<Piece> pieces, Piece selectedPiece) {
        pieceComboBox.removeAllItems();
        int selectedIndex = 0;
        int pieceIndex = 0;
        for (Piece piece : pieces) {
            if (selectedPiece != null && selectedPiece.equals(piece)) {
                selectedIndex = pieceIndex;
            }
            pieceComboBox.addItem(piece);
            ++pieceIndex;
        }
        pieceComboBox.setSelectedIndex(selectedIndex);
    }
    
    private void setUpDateField(Date date) {
        dateFormattedTextField.setValue(date);
        final DateFormatter fmt = (DateFormatter)dateFormattedTextField.getFormatter();
        fmt.setFormat(new SimpleDateFormat("M/d/yyyy hh:mm:ss a")); //$NON-NLS-1$
        dateFormattedTextField.setValue(dateFormattedTextField.getValue());
        dateFormattedTextField.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        buttonPanel = new JPanel();
        passCheckBox = new JCheckBox();
        saveButton = new JButton();
        cancelButton = new JButton();
        jLabel2 = new JLabel();
        controllsPanel = new JPanel();
        filler1 = new JLabel();
        metronome = new Metronome();
        selectors = new JPanel();
        instrumentComboBox = new JComboBox();
        subInstrumentComboBox = new JComboBox();
        pieceComboBox = new JComboBox();
        datePanel = new JPanel();
        dateCheckBox = new JCheckBox();
        dateFormattedTextField = new JFormattedTextField();
        jScrollPane2 = new JScrollPane();
        commentsTextArea = new JTextArea();
        statusLabel = new JLabel();
        commentsCheckPanel = new JPanel();
        commentsScrollPane = new JScrollPane();
        scrollableCommentsPanel = new JPanel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("");
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new GridBagLayout());

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        passCheckBox.setMnemonic('P');
        passCheckBox.setText("Pass");
        passCheckBox.setToolTipText("Indicate whether the student passed the test");
        passCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passCheckBox.setMargin(new Insets(0, 0, 0, 0));
        buttonPanel.add(passCheckBox);

        saveButton.setMnemonic('S');
        saveButton.setText("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(saveButton);

        cancelButton.setMnemonic('C');
        cancelButton.setText("Close");
        cancelButton.setToolTipText("Close this dialog when you are finished with this student");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        jLabel2.setText("Comments");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        getContentPane().add(jLabel2, gridBagConstraints);

        controllsPanel.setAlignmentX(0.0F);
        controllsPanel.setAlignmentY(0.0F);
        controllsPanel.setLayout(new GridBagLayout());

        filler1.setText(" ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        controllsPanel.add(filler1, gridBagConstraints);

        metronome.setToolTipText("Press to toggle the metronome");
        metronome.setMinimumSize(null);
        metronome.setPreferredSize(null);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        controllsPanel.add(metronome, gridBagConstraints);

        selectors.setLayout(new GridBagLayout());

        instrumentComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                instrumentComboBoxActionPerformed(evt);
            }
        });
        selectors.add(instrumentComboBox, new GridBagConstraints());

        subInstrumentComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                subInstrumentComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        selectors.add(subInstrumentComboBox, gridBagConstraints);

        pieceComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pieceComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        selectors.add(pieceComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        controllsPanel.add(selectors, gridBagConstraints);

        datePanel.setLayout(new GridBagLayout());

        dateCheckBox.setText("Set Date");
        dateCheckBox.setToolTipText("Set a date/time other than the present for this record");
        dateCheckBox.setPreferredSize(null);
        dateCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dateCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        datePanel.add(dateCheckBox, gridBagConstraints);

        dateFormattedTextField.setMinimumSize(new Dimension(170, 19));
        dateFormattedTextField.setPreferredSize(new Dimension(170, 19));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        datePanel.add(dateFormattedTextField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        controllsPanel.add(datePanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        getContentPane().add(controllsPanel, gridBagConstraints);

        jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setMinimumSize(new Dimension(100, 100));
        jScrollPane2.setPreferredSize(new Dimension(400, 100));

        commentsTextArea.setLineWrap(true);
        commentsTextArea.setToolTipText("Enter additional free-form comments here");
        commentsTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(commentsTextArea);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        statusLabel.setText(" ");
        statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        getContentPane().add(statusLabel, gridBagConstraints);

        commentsCheckPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        commentsCheckPanel.setToolTipText("Check all the comments that apply");
        commentsCheckPanel.setMinimumSize(new Dimension(200, 200));
        commentsCheckPanel.setPreferredSize(new Dimension(200, 500));
        commentsCheckPanel.setLayout(new BorderLayout());

        org.jdesktop.layout.GroupLayout scrollableCommentsPanelLayout = new org.jdesktop.layout.GroupLayout(scrollableCommentsPanel);
        scrollableCommentsPanel.setLayout(scrollableCommentsPanelLayout);
        scrollableCommentsPanelLayout.setHorizontalGroup(
            scrollableCommentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 196, Short.MAX_VALUE)
        );
        scrollableCommentsPanelLayout.setVerticalGroup(
            scrollableCommentsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 296, Short.MAX_VALUE)
        );

        commentsScrollPane.setViewportView(scrollableCommentsPanel);

        commentsCheckPanel.add(commentsScrollPane, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        getContentPane().add(commentsCheckPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pieceComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pieceComboBoxActionPerformed
        setTempoForPieceAndInstrument();
    }//GEN-LAST:event_pieceComboBoxActionPerformed

    private void subInstrumentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subInstrumentComboBoxActionPerformed
        setUpPieceComboFromInstAndSubInst();
    }//GEN-LAST:event_subInstrumentComboBoxActionPerformed

    private void instrumentComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instrumentComboBoxActionPerformed
        setUpPieceComboFromInstAndSubInst();
        setTempoForPieceAndInstrument();
        setSubinstrument();
    }//GEN-LAST:event_instrumentComboBoxActionPerformed

    private void setUpPieceComboFromInstAndSubInst() {
        if (editingExisting) {
            return; // Only dynamically change the piece for a new test
        }
        Instrument instrument = null;
        Subinstrument subinstrument = null;
        final Object selectedInstrument = 
                instrumentComboBox.getSelectedItem();
        if (selectedInstrument != null) {
            instrument = (Instrument) 
                    ((NamedItemDisplayAdapter) selectedInstrument).getNamedItem();
            final Object selectedSubinstrument = 
                    subInstrumentComboBox.getSelectedItem();
            if (selectedSubinstrument != null) {
                final NamedItem namedItem = ((NamedItemDisplayAdapter) 
                        selectedSubinstrument).getNamedItem();
                if (namedItem instanceof Subinstrument) {
                    subinstrument = (Subinstrument) namedItem;
                }
            }
        }
        setUpPieceSelector(musicPieces, 
                nextPieceFinder.nextPiece(assessments, instrument, subinstrument));
    }
    

    private void setSubinstrument() {
        subInstrumentComboBox.removeAllItems();
        final Instrument selectedInstrument = getSelectedInstrument();
        if (selectedInstrument == null) {
            return;
        }
        final List<Subinstrument> subinstruments = selectedInstrument.getMusicSubinstruments();
        if (subinstruments == null || subinstruments.isEmpty()) {
            subInstrumentComboBox.setEnabled(false);
        } else {
            NamedItemDisplayAdapter selectedItem = null; 
            subInstrumentComboBox.addItem(new NamedItemDisplayAdapter(new UnspecifiedNamedItem()));
            for (Subinstrument si : subinstruments) {
                final NamedItemDisplayAdapter namedItemDisplayAdapter = new NamedItemDisplayAdapter(si);
                subInstrumentComboBox.addItem(namedItemDisplayAdapter);
                if (assessment != null) {
                    final Subinstrument assSubInst = assessment.getMusicSubinstrument();
                    if (assSubInst != null && si.equals(assSubInst)) {
                        selectedItem = namedItemDisplayAdapter;
                    }
                }
            }
            subInstrumentComboBox.setEnabled(true);
            if (selectedItem != null) {
                subInstrumentComboBox.setSelectedItem(selectedItem);
            }
        }
    }

    private void saveButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        handleSaveButtonPress();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void dateCheckBoxActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateCheckBoxActionPerformed
        dateFormattedTextField.setVisible(dateCheckBox.isSelected());
        validate();
    }//GEN-LAST:event_dateCheckBoxActionPerformed

    private void setTempoForPieceAndInstrument() {
        selectedPiece = (Piece) pieceComboBox.getSelectedItem();
        if (selectedPiece == null) {
            return;
        }
        Integer tempo = null;
        final Instrument selectedInstrument = getSelectedInstrument();
        final Set<Tempo> tempos = selectedPiece.getTempos();
        if (tempos != null && ! tempos.isEmpty() && selectedInstrument != null) {
            final Tempo tempoForInstrument = Tempos.tempoForInstrument(
                    tempos, selectedInstrument, true);
            if (tempoForInstrument != null) {
                tempo = tempoForInstrument.getTempo();
            }
        }
        setTempoLabelAndValue(tempo);
    }

    private void setTempoLabelAndValue(Integer tempo) {
        metronome.setVisible(tempo != null);
        if (tempo != null) {
            metronome.setTempo(tempo);
        }
    }

    private void cancelButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        metronome.stop();
        exitDialog(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    metronome.stop();
}//GEN-LAST:event_formWindowClosed

    private void handleSaveButtonPress() {
        try {
            saveRecord(passCheckBox.isSelected());
            resetControls();
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void resetControls() {
        commentManager.resetRatingControls();
        commentsTextArea.setText("");
        dateFormattedTextField.setValue(new Date());
        dateCheckBox.setSelected(false);
        passCheckBox.setSelected(false);
    }

    private final class UnspecifiedNamedItem implements NamedItem {
        public String getName() {
            return "Unspecified";
        }
    }
    
    private void saveRecord(boolean pass) {
        statusLabel.setText(" "); //$NON-NLS-1$
        final boolean newRecord = assessment == null;
        if (assessment == null) {
            assessment = new Assessment();
        }
        
        if (newRecord) {
            assessment.setAssessmentTime(dateCheckBox.isSelected() ? 
                (Date) dateFormattedTextField.getValue() : new Date());
        } else {
            assessment.setAssessmentTime((Date) dateFormattedTextField.getValue());
        }

        if (assessment.getUser() == null) {
            assessment.setUser(user);
        }
        assessment.setMusician(musician);
        assessment.setMusicPiece(selectedPiece);
        assessment.setMusicInstrument(getSelectedInstrument());
        assessment.setMusicSubinstrument(getSelectedSubinstrument());
        assessment.setNotes(commentsTextArea.getText());
        assessment.setPass(pass);
        
        assessment.setPredefinedComments(
                commentManager.getSelectedComments());
        
        if (newRecord) {
            remoteDao.save(assessment);
        } else {
            remoteDao.update(assessment);
        }

        statusLabel.setText("Record saved: " + pieceComboBox.getSelectedItem());
        
        if (! newRecord) {
            exitDialog(true);
        } else {
            if (assessment.isPass()) {
                final int index = pieceComboBox.getSelectedIndex();
                if (index + 1 < pieceComboBox.getItemCount()) {
                    pieceComboBox.setSelectedIndex(index + 1);
                }
            }
        }
        assessment = null;
    }

    Instrument getSelectedInstrument() {
        return (Instrument) getSelectedItem(instrumentComboBox);
    }

    Subinstrument getSelectedSubinstrument() {
        final NamedItem selectedItem = getSelectedItem(subInstrumentComboBox);
        return selectedItem instanceof Subinstrument ? 
                (Subinstrument) selectedItem : null;
    }

    private NamedItem getSelectedItem(JComboBox box) {
        final Object selectedItem = box.getSelectedItem();
        if (selectedItem == null || ! (selectedItem instanceof NamedItemDisplayAdapter)) {
            return null;
        }
        return ((NamedItemDisplayAdapter)selectedItem).getNamedItem();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JPanel commentsCheckPanel;
    private JScrollPane commentsScrollPane;
    private JTextArea commentsTextArea;
    private JPanel controllsPanel;
    private JCheckBox dateCheckBox;
    private JFormattedTextField dateFormattedTextField;
    private JPanel datePanel;
    private JLabel filler1;
    private JComboBox instrumentComboBox;
    private JLabel jLabel2;
    private JScrollPane jScrollPane2;
    private Metronome metronome;
    private JCheckBox passCheckBox;
    private JComboBox pieceComboBox;
    private JButton saveButton;
    private JPanel scrollableCommentsPanel;
    private JPanel selectors;
    private JLabel statusLabel;
    private JComboBox subInstrumentComboBox;
    // End of variables declaration//GEN-END:variables

}
