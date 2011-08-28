package com.dbschools.music.admin.ui;

import com.dbschools.music.ui.NamedItemDisplayAdapter;
import com.dbschools.music.*;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;

import com.dbschools.music.orm.Group;
import com.dbschools.music.orm.Musician;
import com.dbschools.music.orm.NamedItem;

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
    }

    public Musician getMusician() {
        musician.setStudentId((Long) idTf.getValue());
        musician.setFirstName(firstNameTf.getText());
        musician.setLastName(lastNameTf.getText());
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
        lastNameTf = new JTextField();
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
        firstNameTf = new JTextField();
        jLabel7 = new JLabel();

        jLabel1.setFont(new Font("Lucida Grande", 1, 13));
        jLabel1.setText("Student ID:");

        jLabel2.setFont(new Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel2.setText("First Name:");

        lastNameTf.setColumns(40);
        lastNameTf.setText(" ");

        jLabel3.setFont(new Font("Lucida Grande", 1, 13)); // NOI18N
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

        jLabel7.setFont(new Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel7.setText("Last Name:");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel7))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                            .addComponent(idTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lastNameTf, GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                            .addComponent(firstNameTf)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(gradeTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(maleRb)
                                .addGap(24, 24, 24)
                                .addComponent(femaleRb))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(group2Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(group1Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(group3Cb, GroupLayout.PREFERRED_SIZE, 278, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(instrument3Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(instrument2Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(instrument1Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {group1Cb, group2Cb, group3Cb, instrument1Cb, instrument2Cb, instrument3Cb});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(idTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lastNameTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(firstNameTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(gradeTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(maleRb)
                    .addComponent(femaleRb))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(group1Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(instrument1Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(group2Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(instrument2Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(instrument3Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(group3Cb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    // End of variables declaration//GEN-END:variables
    
}
