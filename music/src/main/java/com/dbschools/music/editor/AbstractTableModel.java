package com.dbschools.music.editor;

import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.orm.AbstractPersistentObject;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * A table model with features common to the editors in the application.
 * @author Dave Briccetti
 */
abstract class AbstractTableModel extends javax.swing.table.AbstractTableModel {
    protected final RemoteDao remoteDao;
    private List entities = Collections.emptyList();
    private final String[] columnNames;
    private final Class<?>[] columnClasses;
    private final boolean[] columnEditable;

    public AbstractTableModel(RemoteDao remoteDao, String[] columnNames, Class<?>[] columnClasses, boolean[] columnEditable) {
        this.remoteDao = remoteDao;
        this.columnNames = columnNames;
        this.columnClasses = columnClasses;
        this.columnEditable = columnEditable;
    }

    public int getRowCount() {
        return entities.size();
    }

    public int getColumnCount() {
        return columnClasses.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnEditable[columnIndex];
    }

    public void setEntities(List entities) {
        this.entities = entities;
    }
    
    protected abstract AbstractPersistentObject getNewEntity();
    
    public void insertRow() {
        entities.add(getNewEntity());
        fireTableRowsInserted(entities.size(), entities.size());
    }
    
    public void deleteRows(int[] selectedRows) {
        for (int i : selectedRows) {
            Object obj = entities.get(i);
            remoteDao.delete(obj);
        }
    }

    protected void saveOrUpdateObject(Object obj, Integer id) {
        if (id == null) {
            remoteDao.save(obj);
        } else {
            remoteDao.update(obj);
        }
    }
    
    protected void fireTableDataChangedFromSwing() {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                fireTableDataChanged();
            }
        });
    }
}
