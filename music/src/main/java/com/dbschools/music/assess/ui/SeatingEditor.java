package com.dbschools.music.assess.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

public class SeatingEditor extends javax.swing.JPanel {
    private final static Logger log = Logger.getLogger(SeatingEditor.class);
    private Font smallFont = new Font("Sans Serif", Font.PLAIN, 8);
    
    /** Creates new form SeatingEditor */
    public SeatingEditor() {
        initComponents();
        for (int row = 1; row <= 5; ++row) {
            for (int seat = 1; seat < 10; ++seat) {
                seatingPanel.add(new Player(row, seat, "R" + row + "S" + seat));
            }
        }
        MutableTreeNode root = new DefaultMutableTreeNode("Groups");
        MutableTreeNode symph = new DefaultMutableTreeNode("Symphonic Band");
        MutableTreeNode flutes = new DefaultMutableTreeNode("Flutes");
        flutes.insert(new DefaultMutableTreeNode("Jones"), 0);
        flutes.insert(new DefaultMutableTreeNode("Simms"), 0);
        flutes.insert(new DefaultMutableTreeNode("Thames"), 0);
        flutes.insert(new DefaultMutableTreeNode("Abbas"), 0);
        flutes.insert(new DefaultMutableTreeNode("Petral"), 0);
        flutes.insert(new DefaultMutableTreeNode("Jeffries"), 0);
        symph.insert(flutes, 0);
        root.insert(symph, 0);
        TreeModel treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);
        rowTf.setValue(1);
        seatTf.setValue(1);
        
        class RowInfo {
            int numSeats;
        }
        seatingPanel.setLayout(new LayoutManager() {
            private int seatWidth = 50;
            private int maxRows = 7;
            
            public void addLayoutComponent(String string, Component component) {
                // Do nothing
            }

            public void removeLayoutComponent(Component component) {
            }

            public Dimension preferredLayoutSize(Container container) {
                return new Dimension(600, 400);
            }

            public Dimension minimumLayoutSize(Container container) {
                return new Dimension(200, 200);
            }

            public void layoutContainer(Container container) {
                Component[] comps = container.getComponents();
                final Map<Integer, RowInfo> rowInfoMap = getRowInfo(comps);
                
                final double seatWidthDeg = 5.0;
                final double radius = 200;
                final double rowDiff = 30;
                
                for (Component comp : comps) {
                    if (comp instanceof RowAndSeat) {
                        RowAndSeat rs = (RowAndSeat) comp;
                        RowInfo rowInfo = rowInfoMap.get(rs.getRow());
                        double seat1Angle = (rowInfo.numSeats - 1) * -seatWidthDeg / 2;
                        double seatAngle = seat1Angle + (rs.getSeat() - 1) * seatWidthDeg;
                        final double seatAngleTurned = seatAngle + 90.0;
                        final double seatAngleRadians = seatAngleTurned * Math.PI / 180.0;
                        double thisRowRadius = radius + rowDiff * rs.getRow();
                        int seatX = seatingPanel.getWidth() / 2 + (int) (thisRowRadius * Math.cos(seatAngleRadians));
                        int seatY = seatingPanel.getHeight() + (int) radius / 2 - 
                                (int) (thisRowRadius * Math.sin(seatAngleRadians));
                        log.info(seatX + ", " + seatY);
                        comp.setLocation(seatX, seatY);
                        comp.setSize(comp.getPreferredSize());
                    }
                }
            }

            private Map<Integer, RowInfo> getRowInfo(Component[] comps) {
                Map<Integer, RowInfo> rowInfoMap = new HashMap<Integer, RowInfo>();
                for (Component comp : comps) {
                    if (comp instanceof RowAndSeat) {
                        RowAndSeat rs = (RowAndSeat) comp;
                        RowInfo rowInfo = rowInfoMap.get(rs.getRow());
                        if (rowInfo == null) {
                            rowInfo = new RowInfo();
                            rowInfoMap.put(rs.getRow(), rowInfo);
                        }
                        rowInfo.numSeats++;
                    }
                }
                return rowInfoMap;
            }});
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jScrollPane1 = new JScrollPane();
        tree = new JTree();
        seatingPanel = new JPanel();
        jPanel2 = new JPanel();
        insertButton = new JButton();
        jLabel1 = new JLabel();
        rowTf = new JFormattedTextField();
        jLabel2 = new JLabel();
        seatTf = new JFormattedTextField();

        setLayout(new GridBagLayout());

        jScrollPane1.setViewportView(tree);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(6, 6, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        GroupLayout seatingPanelLayout = new GroupLayout(seatingPanel);
        seatingPanel.setLayout(seatingPanelLayout);
        seatingPanelLayout.setHorizontalGroup(
            seatingPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 272, Short.MAX_VALUE)
        );
        seatingPanelLayout.setVerticalGroup(
            seatingPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 222, Short.MAX_VALUE)
        );

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 3.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(6, 6, 0, 6);
        add(seatingPanel, gridBagConstraints);

        insertButton.setText("Insert At");
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Row:");

        rowTf.setText("1");

        jLabel2.setText("Seat:");

        seatTf.setColumns(2);
        seatTf.setText("1");

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(insertButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(rowTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(seatTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(170, 170, 170))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(insertButton)
                    .addComponent(jLabel1)
                    .addComponent(rowTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(seatTf, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    interface RowAndSeat {
        Integer getRow();
        Integer getSeat();
    }
    
    class Player extends JLabel implements RowAndSeat {
        Player(Integer row, Integer seat, String name) {
            super(name);
            this.row = row;
            this.seat = seat;
            setFont(smallFont);
        }
        private Integer row;
        private Integer seat;
        
        public Integer getRow() {
            return row;
        }

        public Integer getSeat() {
            return seat;
        }
        
    }
    
    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        Integer row = (Integer) rowTf.getValue();
        Integer seat = (Integer) seatTf.getValue();
        String s = tree.getSelectionPath().getLastPathComponent().toString();
        int nextSel = tree.getSelectionRows()[0] + 1;
        tree.setSelectionInterval(nextSel, nextSel);
        seatingPanel.add(new Player(row, seat, s));
        seatTf.setValue(++seat);
        seatingPanel.validate();
    }//GEN-LAST:event_insertButtonActionPerformed
    
    public static void main(String[] args) {
        JFrame f = new JFrame("Seating Editor");
        f.setContentPane(new SeatingEditor());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton insertButton;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JFormattedTextField rowTf;
    private JFormattedTextField seatTf;
    private JPanel seatingPanel;
    private JTree tree;
    // End of variables declaration//GEN-END:variables
    
}
