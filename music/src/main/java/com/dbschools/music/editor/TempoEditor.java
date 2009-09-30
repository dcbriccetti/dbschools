package com.dbschools.music.editor;

import com.dbschools.gui.ActionUtil;
import com.dbschools.gui.PopupListener;
import com.dbschools.gui.TableUtil;
import com.dbschools.music.Constants;
import com.dbschools.music.Tempos;
import com.dbschools.music.events.Event;
import com.dbschools.music.ui.NamedItemDisplayAdapter;
import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.events.EventObserver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;

import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.Tempo;
import java.awt.event.ActionEvent;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 * A tempo editor, allowing the setting of tempos for all instruments, as well
 * as custom tempos for selected instruments.
 * 
 * @author David C. Briccetti
 */
public final class TempoEditor extends javax.swing.JPanel {
    private static final long serialVersionUID = 8739535560325204938L;

    public static class AllItem {
        private static AllItem singleItem = new AllItem();
        
        public static AllItem instance() {
            return singleItem;
        }
        
        @Override
        public String toString() {
            return "All";
        }
    }
    
    static private class TempoTableModel extends javax.swing.table.AbstractTableModel {
        private static final int NUM_STATIC_COLS = 3;
        private static final long serialVersionUID = 5297410961493149714L;
        private final List<Instrument> instrumentsWithCustomTemposList = new ArrayList<Instrument>();
        private final Collection<Instrument> instrumentsManuallyAddedToTable = new HashSet<Instrument>();
        private final List<Piece> pieces  = Collections.synchronizedList(new ArrayList<Piece>());
        private final RemoteDao remoteDao;
        
        public TempoTableModel(RemoteDao remoteDao) {
            this.remoteDao = remoteDao;
        }

        public int getColumnCount() {
            return NUM_STATIC_COLS + (instrumentsWithCustomTemposList == null ? 
                0 : instrumentsWithCustomTemposList.size());
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Sequence";
            }
            if (column == 1) {
                return "Piece";
            }
            if (column == 2) {
                return "All";
            }
            return instrumentForColumn(column).getName();
        }

        private Instrument instrumentForColumn(int column) {
            return column < NUM_STATIC_COLS ? null : 
                instrumentsWithCustomTemposList.get(column - NUM_STATIC_COLS);
        }

        public Object getValueAt(int row, int column) {
            final Piece piece = pieces.get(row);
            if (column == 0) {
                return piece.getTestOrder();
            }
            if (column == 1) {
                return piece.toString();
            }
            final Set<Tempo> tempos = piece.getTempos();
            if (tempos == null) {
                return "Missing";
            }
            final Tempo tempo = findTempo(column, tempos);
            return tempo == null ? null : tempo.getTempo();
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            final Piece piece = pieces.get(row);
            if (column == 0) {
                piece.setTestOrder((Integer) aValue);
                saveOrUpdateObject(piece, piece.getId());
                return;
            }
            if (column == 1) {
                piece.setName(((String) aValue));
                saveOrUpdateObject(piece, piece.getId());
                return;
            }
            final Set<Tempo> tempos = piece.getTempos();
            Tempo tempo = findTempo(column, tempos);
            final String string = aValue == null ? "" : aValue.toString();
            if (tempo == null) {
                tempo = new Tempo();
                tempo.setMusicPiece(piece);
                tempo.setMusicInstrument(instrumentForColumn(column));
            }
            
            if (string.length() > 0 && StringUtils.isNumeric(string)) {
                tempo.setTempo(Integer.valueOf(string));
                tempos.add(tempo);
                saveOrUpdateObject(tempo, tempo.getId());
            } else if (column > 1) { // Can't delete default value
                removeTempo(tempo, tempos);
            }
        }

        private void saveOrUpdateObject(Object obj, Integer id) {
            if (id == null) {
                remoteDao.save(obj);
            } else {
                remoteDao.update(obj);
            }
        }
    
        private void removeTempo(Tempo removeTempo, Collection<Tempo> tempos) {
            for (Tempo tempo : tempos) {
                if (tempo.equals(removeTempo)) {
                    tempos.remove(removeTempo);
                    remoteDao.delete(removeTempo);
                    break;
                }
            }
        }

        private Tempo findTempo(int column, final Iterable<Tempo> tempos) {
            return Tempos.tempoForInstrument(tempos, column < NUM_STATIC_COLS ? null : 
                    instrumentForColumn(column), false);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 1 ? String.class : Integer.class;
        }

        public void setMusicPieces(List<Piece> pieces) {
            if (pieces == null) {
                throw new IllegalArgumentException("pieces is null");
            }
            this.pieces.clear();
            this.pieces.addAll(pieces);

            buildInstrumentsWithCustomTemposList();
        }

        private void buildInstrumentsWithCustomTemposList() {
            SortedSet<Instrument> instrumentsWithCustomTempos = new 
                    TreeSet<Instrument>(instrumentsManuallyAddedToTable);
            
            for (Piece mp : pieces) {
                for (Tempo tempo : mp.getTempos()) {
                    Instrument tempoInstru = tempo.getMusicInstrument();
                    if (tempoInstru != null) {
                        instrumentsWithCustomTempos.add(tempoInstru);
                    }
                }
            }
            instrumentsWithCustomTemposList.clear();
            instrumentsWithCustomTemposList.addAll(instrumentsWithCustomTempos);
            fireTableStructureChanged();
        }

        public void setInstruments(Collection<Instrument> instruments) {
            fireTableStructureChanged();
        }

        public void manuallyAddInstrument(Instrument instrument) {
            instrumentsManuallyAddedToTable.add(instrument);
            buildInstrumentsWithCustomTemposList();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public int getRowCount() {
            return pieces.size();
        }

        public void insertRow() {
            final Piece piece = new Piece(0, "untitled");
            piece.setTempos(new HashSet<Tempo>());
            pieces.add(piece);
            fireTableRowsInserted(pieces.size(), pieces.size());
        }

        public void deleteRows(int[] selectedRows) {
            for (int i : selectedRows) {
                Object obj = pieces.get(i);
                remoteDao.delete(obj);
            }
        }

    }
    private final TempoTableModel tableModel;
    private Collection<Instrument> allInstruments;
    private Set<Instrument> instrumentsWithTempos;
    
    /** Creates new form TempoEditor 
     * @param remoteDao */
    public TempoEditor(final RemoteDao remoteDao) {
        tableModel = new TempoTableModel(remoteDao);
        initComponents();
        temposTable.setModel(tableModel);
        temposTable.setHighlighters(HighlighterFactory.createSimpleStriping());
        temposTable.setSortable(false);
        loadDataFromDao(remoteDao);
        remoteDao.addEventObserver(new EventObserver() {
            public void notify(Event event) {
                if (event.getDetails() instanceof Piece || event.getDetails() instanceof Tempo) {
                    loadDataFromDao(remoteDao);
                }
            }
        });
        createActions();
    }

    private void createActions() {
        JPopupMenu popup = new JPopupMenu();
        temposTable.addMouseListener(new PopupListener(temposTable, popup));

        Action deleteAction = new AbstractAction("Delete...") {
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = temposTable.getSelectedRows();
                if (selectedRows.length > 0) {
                    if (TableUtil.isOkToDelete(TempoEditor.this, selectedRows.length)) {
                        tableModel.deleteRows(selectedRows);
                    }
                }
            }
        };
        popup.add(new JMenuItem(deleteAction));
        ActionUtil.attachDeleteAction(temposTable, deleteAction);

        Action insertAction = new AbstractAction("Insert") {
            public void actionPerformed(ActionEvent e) {
                tableModel.insertRow();
            }
        };
        popup.add(new JMenuItem(insertAction));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        temposTable = new org.jdesktop.swingx.JXTable();
        controls = new javax.swing.JPanel();
        addInstrumentButton = new javax.swing.JButton();
        instrumentsComboBox = new javax.swing.JComboBox();
        instructions = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        temposTable.setModel(tableModel);
        temposTable.setMinimumSize(new java.awt.Dimension(300, 100));
        jScrollPane1.setViewportView(temposTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        controls.setLayout(new java.awt.GridBagLayout());

        addInstrumentButton.setText("Add Instrument");
        addInstrumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInstrumentButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        controls.add(addInstrumentButton, gridBagConstraints);

        instrumentsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        controls.add(instrumentsComboBox, gridBagConstraints);

        instructions.setText("Select an instrument to customize tempos for and push \"Add Instrument\"");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        controls.add(instructions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(controls, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addInstrumentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInstrumentButtonActionPerformed
        addInstrument();
    }//GEN-LAST:event_addInstrumentButtonActionPerformed
    
    private void loadDataFromDao(RemoteDao remoteDao) {
        Collection<Instrument> instruments = remoteDao.getInstruments();
        this.allInstruments = instruments;
        tableModel.setInstruments(instruments);

        List<Piece> pieces = new ArrayList<Piece>(remoteDao.getMusicPieces());
        tableModel.setMusicPieces(pieces);
        
        instrumentsWithTempos = TempoEditor.getInstrumentsWithTempos(pieces);
        loadInstrumentsComboBox();
        
        setColumnWidths();
    }

    private void setColumnWidths() {
        TableColumnModel columnModel = temposTable.getColumnModel();
        
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(1).setPreferredWidth(150);
        for (int i = 2; i < columnModel.getColumnCount(); ++i) {
            final TableColumn column = columnModel.getColumn(i);
            column.setPreferredWidth(50);
            column.setMaxWidth(50);
        }
    }

    public void manuallyAddInstrument(Instrument instrument) {
        tableModel.manuallyAddInstrument(instrument);
        setColumnWidths();
    }
    
    public static Set<Instrument> getInstrumentsWithTempos(
            List<Piece> pieces) {
        Set<Instrument> instrumentsWithTempos = new HashSet<Instrument>();
        for (Piece piece : pieces) {
            for (Tempo tempo : piece.getTempos()) {
                instrumentsWithTempos.add(tempo.getMusicInstrument());
            }
        }
        return instrumentsWithTempos;
    }
    
    public static List<Instrument> getInstrumentAddList(
            final Collection<Instrument> allInstruments,
            Set<Instrument> instrumentsWithTempos) {
        
        ArrayList<Instrument> instrumentAddList = new ArrayList<Instrument>();
        for (Instrument inst : allInstruments) {
            if (! inst.getName().equals(Constants.UNASSIGNED) && ! instrumentsWithTempos.contains(inst)) {
                instrumentAddList.add(inst);
            }
        }
        return instrumentAddList;
    }

    private void loadInstrumentsComboBox() {
        TempoEditor.addComboBoxItems(NamedItemDisplayAdapter.getItemList(TempoEditor.getInstrumentAddList(
                allInstruments, instrumentsWithTempos)), 
                instrumentsComboBox, false);
    }

    private void addInstrument() {
        manuallyAddInstrument((Instrument) 
                ((NamedItemDisplayAdapter)instrumentsComboBox.getSelectedItem()).getNamedItem());
        instrumentsComboBox.removeItemAt(instrumentsComboBox.getSelectedIndex());
        if (instrumentsComboBox.getItemCount() == 0) {
            addInstrumentButton.setEnabled(false);
        }
        
    }
    
    public static void addComboBoxItems(Iterable<NamedItemDisplayAdapter> items, JComboBox box, boolean addAllItem) {
        box.removeAllItems();
        if (addAllItem) {
            box.addItem(AllItem.instance());
        }
        for (NamedItemDisplayAdapter item : items) {
            box.addItem(item);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addInstrumentButton;
    private javax.swing.JPanel controls;
    private javax.swing.JLabel instructions;
    private javax.swing.JComboBox instrumentsComboBox;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTable temposTable;
    // End of variables declaration//GEN-END:variables

}
