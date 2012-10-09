package com.dbschools.music.admin.ui;

import com.dbschools.music.decortrs.YearDecorator;
import com.dbschools.music.ui.NamedItemDisplayAdapter;
import com.dbschools.music.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.NamedItem;
import org.jdesktop.layout.LayoutStyle;

/**
 * An editor for a musician and the instruments he plays in what groups.
 * 
 * @author David C. Briccetti
 */
public final class MusicianEditor extends javax.swing.JPanel {
    private static final long serialVersionUID = -3462725811049461771L;
    private JComboBox[] groupCbs;
    private JComboBox[] instrumentCbs;
    private final NamedItemDisplayAdapter unusedMusicGroupDisplayAdapter = 
            new NamedItemDisplayAdapter(new UnusedMusicGroup());
    private Musician musician;
    private Integer schoolYear;
    
    private static final class UnusedMusicGroup extends Group {
        private static final long serialVersionUID = -8846703090548285751L;
    }
    
    public static class GroupAndInstrument {
        private final NamedItem musicGroup;
        private final NamedItem musicInstrument;
        
        public GroupAndInstrument(NamedItem musicGroup,
                NamedItem musicInstrument) {
            super();
            this.musicGroup = musicGroup;
            this.musicInstrument = musicInstrument;
        }
        public NamedItem getMusicGroup() {
            return musicGroup;
        }
        public NamedItem getMusicInstrument() {
            return musicInstrument;
        }
    }
    
    /** Creates new form MusicianEditor */
    public MusicianEditor() {
        initComponents();
        idTf.setValue((long) 0);
        gradeTf.setValue(0);
        createControlArrays();
    }
    
    public void setGroups(Collection<? extends NamedItem> namedItems) {
        final Vector<NamedItemDisplayAdapter> data = vectorWithUnusedAndAll(namedItems);
        for (JComboBox cb : groupCbs) {
            cb.setModel(new DefaultComboBoxModel(data));
        }
    }

    public void setInstruments(Collection<? extends NamedItem> namedItems) {
        final Vector<NamedItemDisplayAdapter> data = vectorWithUnusedAndAll(namedItems);
        for (JComboBox cb : instrumentCbs) {
            cb.setModel(new DefaultComboBoxModel(data));
        }
    }
    
    private Vector<NamedItemDisplayAdapter> vectorWithUnusedAndAll(
            Collection<? extends NamedItem> namedItems) {
        final Vector<NamedItemDisplayAdapter> vector = new Vector<NamedItemDisplayAdapter>();
        vector.add(unusedMusicGroupDisplayAdapter);
        vector.addAll(NamedItemDisplayAdapter.getItemList(namedItems));
        return vector;
    }

    private void createControlArrays() {
        groupCbs = new JComboBox[] {group1Cb, group2Cb, group3Cb};
        instrumentCbs = new JComboBox[] {instrument1Cb, instrument2Cb, instrument3Cb};
    }
    
    public void setSchoolYear(int schoolYear) {
        this.schoolYear = schoolYear;
        year.setText(new YearDecorator(schoolYear).toString());
    }

    public Musician getMusician() {
        Long studentId = (Long) idTf.getValue();
        if (studentId == null || studentId == 0) throw new IllegalStateException("Invalid student ID");
        musician.setStudentId(studentId);

        String lastName = lastNameTf.getText();
        if (StringUtils.isBlank(lastName)) throw new IllegalStateException("Missing last name");
        musician.setLastName(lastName);

        String firstName = firstNameTf.getText();
        if (StringUtils.isBlank(firstName)) throw new IllegalStateException("Missing first name");
        musician.setFirstName(firstName);

        musician.setGraduationYear(TermUtils.gradeAsGraduationYear(((Integer) gradeTf.getValue()), schoolYear));
        musician.setSex(maleRb.isSelected() ? "M" : "F");
        return musician;
    }

    public void setMusician(Musician musician) {
        assert schoolYear != null;
        this.musician = musician;
        idTf.setValue(musician.getStudentId());
        firstNameTf.setText(musician.getFirstName());
        lastNameTf.setText(musician.getLastName());
        final Integer graduationYear = musician.getGraduationYear();
        gradeTf.setValue(graduationYear == null ? 0 : 
                TermUtils.graduationYearAsGrade(
                graduationYear, schoolYear));
        if (StringUtils.equals(musician.getSex(), "F")) {
            femaleRb.setSelected(true);
        } else {
            maleRb.setSelected(true);
        }
    }

    public void setGroupAndInstrumentAssignments(Collection<GroupAndInstrument> assignments) {
        int i = 0;
        for (GroupAndInstrument gi : assignments) {
            groupCbs[i].setSelectedItem(
                    new NamedItemDisplayAdapter(gi.getMusicGroup()));
            instrumentCbs[i].setSelectedItem(
                    new NamedItemDisplayAdapter(gi.getMusicInstrument()));
            ++i;
        }
        
        clearUnusedAssignments(i);
    }

    private void clearUnusedAssignments(int firstUnusedIndex) {
        for (; firstUnusedIndex < groupCbs.length; ++firstUnusedIndex) {
            groupCbs[firstUnusedIndex].setSelectedItem(unusedMusicGroupDisplayAdapter);
            instrumentCbs[firstUnusedIndex].setSelectedItem(unusedMusicGroupDisplayAdapter);
        }
    }

    public Collection<GroupAndInstrument> getGroupAndInstrumentAssignments() {
        List<GroupAndInstrument> assignments = new ArrayList<GroupAndInstrument>();
        for (int i = 0; i < groupCbs.length; ++i) {
            final NamedItemDisplayAdapter nida = (NamedItemDisplayAdapter) groupCbs[i].getSelectedItem();
            if (nida != unusedMusicGroupDisplayAdapter) {
                final NamedItem group = nida.getNamedItem();
                NamedItem instrument = ((NamedItemDisplayAdapter) 
                        instrumentCbs[i].getSelectedItem()).getNamedItem();
                GroupAndInstrument gi = new GroupAndInstrument(group, instrument);
                assignments.add(gi);
            }
        }
        return assignments;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new ButtonGroup();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        firstNameTf = new JTextField();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        maleRb = new JRadioButton();
        femaleRb = new JRadioButton();
        group1Cb = new JComboBox();
        instrument1Cb = new JComboBox();
        group2Cb = new JComboBox();
        instrument2Cb = new JComboBox();
        group3Cb = new JComboBox();
        instrument3Cb = new JComboBox();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        idTf = new JFormattedTextField();
        gradeTf = new JFormattedTextField();
        lastNameTf = new JTextField();
        jLabel7 = new JLabel();
        year = new JLabel();

        setMaximumSize(new Dimension(800, 400));

        jLabel1.setFont(new Font("Lucida Grande", 1, 13));
        jLabel1.setText("Student ID:");

        jLabel2.setFont(new Font("Lucida Grande", 1, 13));
        jLabel2.setText("First Name:");

        firstNameTf.setColumns(40);
        firstNameTf.setText(" ");

        jLabel3.setFont(new Font("Lucida Grande", 1, 13));
        jLabel3.setText("Grade:");

        jLabel4.setFont(new Font("Lucida Grande", 1, 13));
        jLabel4.setText("Sex:");

        buttonGroup1.add(maleRb);
        maleRb.setText("Male");
        maleRb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        maleRb.setMargin(new Insets(0, 0, 0, 0));

        buttonGroup1.add(femaleRb);
        femaleRb.setText("Female");
        femaleRb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        femaleRb.setMargin(new Insets(0, 0, 0, 0));

        group1Cb.setMaximumRowCount(20);

        instrument1Cb.setMaximumRowCount(20);

        group2Cb.setMaximumRowCount(20);

        instrument2Cb.setMaximumRowCount(20);

        group3Cb.setMaximumRowCount(20);

        instrument3Cb.setMaximumRowCount(20);

        jLabel5.setFont(new Font("Lucida Grande", 1, 13));
        jLabel5.setText("Member of Group");

        jLabel6.setFont(new Font("Lucida Grande", 1, 13));
        jLabel6.setText("Playing Instrument");

        idTf.setColumns(8);
        idTf.setText("0");

        gradeTf.setColumns(2);
        gradeTf.setText("6");

        jLabel7.setFont(new Font("Lucida Grande", 1, 13));
        jLabel7.setText("Last Name:");

        year.setHorizontalAlignment(SwingConstants.RIGHT);
        year.setText("year");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(12, 12, 12)
                                .add(idTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(year, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 156, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel3)
                                    .add(jLabel4))
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(gradeTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(maleRb)
                                        .add(24, 24, 24)
                                        .add(femaleRb))))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(group2Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(group1Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(group3Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 278, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel5))
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel6)
                                    .add(instrument3Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(instrument2Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(instrument1Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(20, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel7)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(lastNameTf, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                        .add(330, 330, 330))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(firstNameTf, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                        .add(281, 281, 281))))
        );

        layout.linkSize(new Component[] {group1Cb, group2Cb, group3Cb, instrument1Cb, instrument2Cb, instrument3Cb}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(idTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(year))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lastNameTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(firstNameTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(gradeTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(maleRb)
                    .add(femaleRb))
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jLabel5))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(group1Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(instrument1Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(group2Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(instrument2Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(instrument3Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(group3Cb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup buttonGroup1;
    private JRadioButton femaleRb;
    private JTextField firstNameTf;
    private JFormattedTextField gradeTf;
    private JComboBox group1Cb;
    private JComboBox group2Cb;
    private JComboBox group3Cb;
    private JFormattedTextField idTf;
    private JComboBox instrument1Cb;
    private JComboBox instrument2Cb;
    private JComboBox instrument3Cb;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JTextField lastNameTf;
    private JRadioButton maleRb;
    private JLabel year;
    // End of variables declaration//GEN-END:variables
    
}
