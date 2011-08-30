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

import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.decortrs.AssessmentDecorator;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.dbschools.gui.ErrorHandler;
import com.dbschools.gui.JwsFileSaveUtil;
import com.dbschools.gui.PromptUtils;
import com.dbschools.gui.TableSorter;
import com.dbschools.gui.TableUtil;
import com.dbschools.gui.TextUtil;
import com.dbschools.music.NonPersistentPreferences;
import com.dbschools.music.TermUtils;
import com.dbschools.music.decortrs.RejectionDecorator;
import com.dbschools.music.events.Event;
import com.dbschools.music.events.EventObserver;
import com.dbschools.music.events.TypeCode;
import static com.dbschools.music.events.TypeCode.SAVE_OBJECT;
import static com.dbschools.music.events.TypeCode.UPDATE_OBJECT;
import static com.dbschools.music.events.TypeCode.DELETE_OBJECT;
import com.dbschools.music.orm.Assessment;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.MusicianGroup;
import com.dbschools.music.orm.Rejection;
import com.dbschools.music.orm.RejectionReason;
import com.dbschools.music.orm.User;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TreeSet;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.log4j.Logger;

/**
 * A panel showing the testing and rejection history for a student.
 * 
 * @author David C. Briccetti
 */
public final class StudentDetailsPanel extends javax.swing.JPanel {
    private static final long serialVersionUID = 3993719650024115950L;
    private final static Logger log = Logger
            .getLogger(StudentDetailsPanel.class);
    private final AssessmentsModel assessmentsModel = new AssessmentsModel();
    private final RejectionsModel rejectionsModel = new RejectionsModel();
    private final Action deleteAssessmentsAction = new DeleteAssessmentsAction();
    private final Action deleteRejectionsAction = new DeleteRejectionsAction();
    private final Musician musician;
    private final User loggedInUser;
    private final Collection<RejectionReason> rejectionReasons;
    private final RemoteDao remoteDao;
    private final NonPersistentPreferences nonPersistentPreferences;
    private transient TableSorter tableSorter;
    
    private class EventObserverImpl implements EventObserver {
        public void notify(Event event) {
            final TypeCode t = event.getTypeCode();
            final Object details = event.getDetails();

            if (details instanceof Assessment) {
                final Assessment assessment = (Assessment) details;
                if (assessment.getMusician().getId().equals(musician.getId())) {
                    if ((t == SAVE_OBJECT || t == UPDATE_OBJECT)) {
                        assessmentsModel.addOrUpdate(assessment);
                    } else if (t == DELETE_OBJECT) {
                        assessmentsModel.deleteAssessment(assessment);
                    }
                }
            } else if (details instanceof Rejection) {
                final Rejection rejection = (Rejection) details;
                if (rejection.getMusician().getId().equals(musician.getId())) {
                    if (t == SAVE_OBJECT) {
                        rejectionsModel.addRejection(rejection);
                        showLastRejectionRow();
                    } else if (t == DELETE_OBJECT) {
                        rejectionsModel.deleteRejection(rejection);
                    }
                }
            }
        }
    }

    public StudentDetailsPanel(RemoteDao remoteDao, Musician musician,
            NonPersistentPreferences nonPersistentPreferences) {
        this.remoteDao = remoteDao;
        this.musician = musician;
        this.nonPersistentPreferences = nonPersistentPreferences;
        initComponents();
        loggedInUser = remoteDao.getUser();
        rejectionReasons = remoteDao.getRejectionReasons();
        weeksToShowLabel.setPreferredSize(
                TextUtil.getTextDimensions(Integer.toString(weeksToShowSlider.getMaximum()), 
                        weeksToShowLabel.getFont()));
        enableStudentActionButtons(false);
        setUpAssessmentHistoryTable();
        setUpRejectionsHistoryTable();
        setWeeksToShow(nonPersistentPreferences.getWeeksToShow());
        initKeys();
        remoteDao.addEventObserver(new EventObserverImpl());
        processSelectedStudent();
    }

    private void initKeys() {
        final String deleteActionName = "delete";
        final KeyStroke deleteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        final KeyStroke backspaceKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);

        assessmentsTable.getActionMap().put(deleteActionName, deleteAssessmentsAction);
        addDeleteToInputMap(assessmentsTable.getInputMap(), deleteActionName, 
                deleteKeyStroke, backspaceKeyStroke);

        rejectionsTable.getActionMap().put(deleteActionName, deleteRejectionsAction);
        addDeleteToInputMap(rejectionsTable.getInputMap(), deleteActionName, 
                deleteKeyStroke, backspaceKeyStroke);
    }

    private void addDeleteToInputMap(final InputMap ttInputMap, 
            final String deleteActionName, final KeyStroke deleteKeyStroke, 
            final KeyStroke backspaceKeyStroke) {
        ttInputMap.put(deleteKeyStroke, deleteActionName);
        ttInputMap.put(backspaceKeyStroke, deleteActionName);
    }

    private void setTestDialogTitle(AssessmentRecordDialog dialog, final String musicianName) {
        dialog.setTitle(musicianName + " - DBSchools Music Gradebook: Assessment");
    }

    private void setUpAssessmentHistoryTable() {
        tableSorter = TableUtil.setUpSortingTable(assessmentsTable, assessmentsModel);
        assessmentsTable.setShowGrid(true);
        AssessmentHistoryTableCustomizer.customize(assessmentsTable);
        assessmentsModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                    // Run this after the table gets notified about the structure change
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            customizeColumnWidths();
                        }});
                }
                keywordFrequencyLabels.setText(assessmentsModel.getKeywordCountsAsString());
                tabbedPanel.setTitleAt(0, "Assessments (" +
                        Integer.toString(assessmentsModel.getRowCount()) +
                        " of " + assessmentsModel.getAllAssessmentsCount() + ")");
            }});
    }

    private void setUpRejectionsHistoryTable() {
        rejectionsTable.setModel(rejectionsModel);
        RejectionHistoryTableCustomizer.customize(rejectionsTable);
        rejectionsModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                tabbedPanel.setTitleAt(1, "Rejections (" + 
                        Integer.toString(rejectionsModel.getRowCount()) + ")");
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonsPanel = new javax.swing.JPanel();
        testButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        reportButton = new javax.swing.JButton();
        groupMembershipsLabel = new javax.swing.JLabel();
        tabbedPanel = new javax.swing.JTabbedPane();
        assPane = new javax.swing.JPanel();
        weeksToShowPanel = new javax.swing.JPanel();
        weeksToShowHeading = new javax.swing.JLabel();
        weeksToShowLabel = new javax.swing.JLabel();
        weeksToShowSlider = new javax.swing.JSlider();
        assjScrollPane = new javax.swing.JScrollPane();
        assessmentsTable = new javax.swing.JTable();
        keywordFrequencyLabels = new javax.swing.JLabel();
        rejPane = new javax.swing.JPanel();
        rejScrollPane = new javax.swing.JScrollPane();
        rejectionsTable = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        testButton.setMnemonic('T');
        testButton.setText("Test...");
        testButton.setToolTipText("Create a new test record for this student");
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(testButton);

        rejectButton.setText("Reject...");
        rejectButton.setToolTipText("Reject a student from testing because of equipment or other problem");
        rejectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(rejectButton);

        reportButton.setText("Save Report...");
        reportButton.setToolTipText("Saves an assessment report to a file");
        reportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(reportButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(buttonsPanel, gridBagConstraints);

        groupMembershipsLabel.setText("Group memberships");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(groupMembershipsLabel, gridBagConstraints);

        assPane.setLayout(new java.awt.GridBagLayout());

        weeksToShowPanel.setFont(new java.awt.Font("Lucida Grande", 0, 12));
        weeksToShowPanel.setLayout(new java.awt.GridBagLayout());

        weeksToShowHeading.setText("Weeks to Show:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        weeksToShowPanel.add(weeksToShowHeading, gridBagConstraints);

        weeksToShowLabel.setText("999");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        weeksToShowPanel.add(weeksToShowLabel, gridBagConstraints);

        weeksToShowSlider.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        weeksToShowSlider.setMajorTickSpacing(50);
        weeksToShowSlider.setMaximum(200);
        weeksToShowSlider.setMinimum(1);
        weeksToShowSlider.setMinorTickSpacing(10);
        weeksToShowSlider.setValue(1);
        weeksToShowSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                weeksToShowSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        weeksToShowPanel.add(weeksToShowSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        assPane.add(weeksToShowPanel, gridBagConstraints);

        assessmentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        assessmentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                assessmentsTableMouseClicked(evt);
            }
        });
        assjScrollPane.setViewportView(assessmentsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 0, 4);
        assPane.add(assjScrollPane, gridBagConstraints);

        keywordFrequencyLabels.setText("Keyword frequencies");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        assPane.add(keywordFrequencyLabels, gridBagConstraints);

        tabbedPanel.addTab("Assessments", assPane);

        rejPane.setLayout(new java.awt.GridBagLayout());

        rejectionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        rejScrollPane.setViewportView(rejectionsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 0, 4);
        rejPane.add(rejScrollPane, gridBagConstraints);

        tabbedPanel.addTab("Rejections", rejPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(tabbedPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void weeksToShowSliderStateChanged(@SuppressWarnings("unused") javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_weeksToShowSliderStateChanged
        final int value = weeksToShowSlider.getValue();
        setWeeksToShow(value);
    }//GEN-LAST:event_weeksToShowSliderStateChanged

    private void assessmentsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assessmentsTableMouseClicked
        if (evt.getClickCount() == 2) {
            editSelectedAssessments();
        }
        
}//GEN-LAST:event_assessmentsTableMouseClicked

    private void editSelectedAssessments() {
        final int[] selectedRows = TableUtil.selectedSortedModelRows(assessmentsTable);

        for (int rowIndex : selectedRows) {
            Assessment assessment = assessmentsModel.getMusicAssessment(rowIndex);
            final Musician selectedMusician = assessment.getMusician();
            AssessmentRecordDialog dialog = new AssessmentRecordDialog(remoteDao,
                    selectedMusician, assessmentInfo().getAssessments(), 
                    assessment);
            setTestDialogTitle(dialog, selectedMusician.getName());
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }

    class DeleteAssessmentsAction extends AbstractAction {
        private static final long serialVersionUID = -2033602384501517417L;

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
            final int[] selectedRows = 
                TableUtil.selectedSortedModelRows(assessmentsTable);
    
            if (selectedRows.length > 0) {
                if (isOkToDelete(selectedRows, "assessment")) {
                    deleteAssessments(selectedRows);
                }
            }
        }
       
    }

    class DeleteRejectionsAction extends AbstractAction {
        private static final long serialVersionUID = -9021788615280514281L;

        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e) {
            final int[] selectedRejRows = 
                    TableUtil.selectedSortedModelRows(rejectionsTable);
    
            if (selectedRejRows.length > 0) {
                if (isOkToDelete(selectedRejRows, "rejection")) {
                    deleteRejections(selectedRejRows);
                }
            }
        }
       
    }

    private void deleteAssessments(final int[] selectedRows) {
        Collection<Assessment> delAsses = new ArrayList<Assessment>(selectedRows.length);
        for (int selectedRow : selectedRows) {
            delAsses.add(assessmentsModel.getMusicAssessment(selectedRow));
        }
        remoteDao.delete(delAsses);
    }

    private void deleteRejections(final int[] selectedRejRows) {
        final List<Rejection> delRej = new ArrayList<Rejection>(selectedRejRows.length);
        for (int selectedRow : selectedRejRows) {
            delRej.add(rejectionsModel.getRecord(selectedRow));
        }
        remoteDao.delete(delRej);
    }

    private boolean isOkToDelete(final int[] selectedRows, String type) {
        final StringBuffer buf = new StringBuffer("Are you sure you want to delete the ");
        PromptUtils.addMessageToPrompt(buf, selectedRows.length, "selected " + type + " record");
        buf.append('?');
        
        final int choice = JOptionPane.showConfirmDialog(this, buf.toString());
        final boolean okToDelete = choice == JOptionPane.YES_OPTION;
        return okToDelete;
    }

    private void rejectButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectButtonActionPerformed
        try {
            final RejectionReason rejectionReason = (RejectionReason) 
                JOptionPane.showInputDialog(this, 
                "Select a reason for rejection",
                "Reject " + musician.getName(), 
                JOptionPane.QUESTION_MESSAGE,
                null, rejectionReasons.toArray(new RejectionReason[rejectionReasons.size()]), 
                null);
            if (rejectionReason != null) {
                final Rejection rejection = new Rejection(new Date(),
                    loggedInUser, musician, rejectionReason, null);
                remoteDao.save(rejection);
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }//GEN-LAST:event_rejectButtonActionPerformed

    private void testButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        AssessmentRecordDialog dialog = new AssessmentRecordDialog(remoteDao, 
                musician, assessmentInfo().getAssessments(), null);
        setTestDialogTitle(dialog, musician.getName());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (isSortedOnAssessmentDate()) {
            showLastAssessmentRow();
        }
    }//GEN-LAST:event_testButtonActionPerformed

    private void reportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportButtonActionPerformed
        StringTemplateGroup group = new StringTemplateGroup("myGroup");
        StringTemplate report = group.getInstanceOf("templates/report-html");
        final Musician maInfo = 
                remoteDao.getMusicianAndAssessmentInfo(musician.getId());
        report.setAttribute("musician", maInfo);
        report.setAttribute("groupMemberships", groupMembershipsLabel.getText());
        final DateFormat df = new SimpleDateFormat(com.dbschools.music.decortrs.Constants.DATE_TIME_FORMAT);
        report.setAttribute("datetime", df.format(new Date()));
        report.setAttribute("keywordCounts", assessmentsModel.getKeywordCountsAsString());
        prepareReportAssessments(assessmentsModel.getSelectedAssessments(), report);
        prepareReportRejections(maInfo.getRejections(), report);
        final String reportString = report.toString();
        JwsFileSaveUtil.saveStringToFile(this,reportString, 
                new String[]{"html"}, "Testing-Report.html");
}//GEN-LAST:event_reportButtonActionPerformed

    private void prepareReportAssessments(final Collection<Assessment> assessments, StringTemplate report) {
        List<AssessmentDecorator> adecs = new 
                ArrayList<AssessmentDecorator>(assessments.size());
        final TreeSet<Assessment> sortedAssessments = new TreeSet<Assessment>(assessments);
        for (Assessment as : sortedAssessments) {
            adecs.add(new AssessmentDecorator(as));
        }
        report.setAttribute("assessments", adecs);
    }

    private void prepareReportRejections(final Collection<Rejection> rejections, StringTemplate report) {
        List<RejectionDecorator> rdecs = new 
                ArrayList<RejectionDecorator>(rejections.size());
        final TreeSet<Rejection> sortedRejections = new TreeSet<Rejection>(rejections);
        for (Rejection rej : sortedRejections) {
            rdecs.add(new RejectionDecorator(rej));
        }
        report.setAttribute("rejections", rdecs);
    }

    private void processSelectedStudent() {
        enableStudentActionButtons(false);
        
        final Musician assessmentInfo = assessmentInfo();
        assessmentsModel.setMusicAssessments(assessmentInfo.getAssessments());
        customizeColumnWidths();
        sortOnAssessmentDate();
        rejectionsModel.setMusicRejections(assessmentInfo.getRejections());
        positionToLastRowInTables();
        enableStudentActionButtons(true);
        final StringBuilder sb = new StringBuilder();
        for (MusicianGroup mg : assessmentInfo.getMusicianGroups()) {
            if (mg.getSchoolYear().equals(TermUtils.getCurrentTerm())) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(mg.getInstrument().getName() + " in " + mg.getGroup().getName());
            }
        }
        groupMembershipsLabel.setText(sb.toString());
    }

    private boolean isSortedOnAssessmentDate() {
        return tableSorter.getSortingStatus(0) == 1;
    }

    private void sortOnAssessmentDate() {
        tableSorter.setSortingStatus(0, 1); 
    }

    private Musician assessmentInfo() {
        return remoteDao.getMusicianAndAssessmentInfo(musician.getId());
    }

    private void customizeColumnWidths() {
        AssessmentHistoryTableCustomizer.setColumnWidths(assessmentsTable);
    }

    private void showLastAssessmentRow() {
        positionToLastRowInTable(assjScrollPane, assessmentsTable);
    }

    private void showLastRejectionRow() {
        positionToLastRowInTable(rejScrollPane, rejectionsTable);
    }

    private void positionToLastRowInTables() {
        showLastAssessmentRow();
        showLastRejectionRow();
    }

    private void positionToLastRowInTable(JScrollPane scrollPane, JTable table) {
        final Rectangle cellRect = table.getCellRect(
                        table.getModel().getRowCount() - 1, 0, true);
        scrollPane.getViewport().scrollRectToVisible(cellRect);
    }

    private void enableStudentActionButtons(boolean enabled) {
        testButton.setEnabled(enabled);
        rejectButton.setEnabled(enabled);
    }

    private void setWeeksToShow(int weeksToShow) {
        nonPersistentPreferences.setWeeksToShow(weeksToShow);
        weeksToShowSlider.setValue(weeksToShow);
        weeksToShowLabel.setText(Integer.toString(weeksToShow));
        assessmentsModel.setWeeksToShow(weeksToShow);
    }

    public int getAssessmentsCount() {
        return assessmentsModel.getRowCount();
    }
    
    public int getRejectionsCount() {
        return rejectionsModel.getRowCount();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel assPane;
    private javax.swing.JTable assessmentsTable;
    private javax.swing.JScrollPane assjScrollPane;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JLabel groupMembershipsLabel;
    private javax.swing.JLabel keywordFrequencyLabels;
    private javax.swing.JPanel rejPane;
    private javax.swing.JScrollPane rejScrollPane;
    private javax.swing.JButton rejectButton;
    private javax.swing.JTable rejectionsTable;
    private javax.swing.JButton reportButton;
    private javax.swing.JTabbedPane tabbedPanel;
    private javax.swing.JButton testButton;
    private javax.swing.JLabel weeksToShowHeading;
    private javax.swing.JLabel weeksToShowLabel;
    private javax.swing.JPanel weeksToShowPanel;
    private javax.swing.JSlider weeksToShowSlider;
    // End of variables declaration//GEN-END:variables

}
