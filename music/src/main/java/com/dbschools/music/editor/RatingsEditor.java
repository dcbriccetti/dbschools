package com.dbschools.music.editor;

import com.dbschools.gui.ActionUtil;
import com.dbschools.music.dao.RemoteDao;
import com.dbschools.music.orm.AbstractPersistentObject;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.dbschools.music.events.Event;
import com.dbschools.music.events.EventObserver;
import com.dbschools.gui.PopupListener;
import com.dbschools.gui.TableUtil;
import com.dbschools.music.orm.PredefinedComment;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.table.TableColumnModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 * An editor for the ratings (predefinedComments) that apply to an assessment.
 * 
 * @author David C. Briccetti
 */
public class RatingsEditor extends JPanel {
    private static final long serialVersionUID = -2101066653562218512L;
    private final Model model;
    private JXTable table;
    
    private static class Model extends AbstractTableModel {
        private static final long serialVersionUID = -1092772169342005467L;
        private final List<PredefinedComment> predefinedComments = 
                Collections.synchronizedList(new ArrayList<PredefinedComment>());
        private final Map<Integer, Integer> commentCounts;
        
        public Model(RemoteDao remoteDao) {
            super(remoteDao, new String[] {"Rating", "Times Used"}, 
                    new Class<?>[] {String.class, Integer.class}, 
                    new boolean[] {true, false});
            super.setEntities(predefinedComments);
            predefinedComments.addAll(remoteDao.getComments());
            this.commentCounts = remoteDao.getCommentsCounts();
            remoteDao.addEventObserver(new EventObserver() {
                public void notify(Event event) {
                    if (event.getDetails() instanceof PredefinedComment) {
                        load();
                    }
                }
            });
        }

        private void load() {
            predefinedComments.clear();
            predefinedComments.addAll(remoteDao.getComments());
            fireTableDataChangedFromSwing();
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (predefinedComments == null || predefinedComments.size() == 0) {
                return null;
            }
            
            PredefinedComment predefinedComment = predefinedComments.get(rowIndex);
            switch (columnIndex) {
            case 0: return predefinedComment.getText();
            case 1: {
                final Integer id = predefinedComment.getId();
                if (id == null) {
                    return 0;
                }
                Integer count = commentCounts.get(id);
                return count == null ? 0 : count;
            }
            default: return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            PredefinedComment predefinedComment = predefinedComments.get(rowIndex);
            switch (columnIndex) {
            case 0:
                predefinedComment.setText((String) aValue);
                break;
            default:
                break;
            }
            saveOrUpdateObject(predefinedComment, predefinedComment.getId());
        }

        List<Integer> selectedUnusedRows(int[] selectedRowIndexes) {
            ArrayList<Integer> selUnusedIndexes = new ArrayList<Integer>();
            for (int i : selectedRowIndexes) {
                PredefinedComment predefinedComment = predefinedComments.get(i);
                if (! isCommentUsed(predefinedComment)) {
                    selUnusedIndexes.add(i);
                }
            }
            return selUnusedIndexes;
        }

        private boolean isCommentUsed(PredefinedComment predefinedComment) {
            if (predefinedComment.getId() == null) {
                return false;
            }
            return commentCounts.containsKey(predefinedComment.getId());
        }

        public void deleteRows(List<Integer> rows) {
            Collection<PredefinedComment> delComs = new ArrayList<PredefinedComment>();
            for (Integer row : rows) {
                delComs.add(predefinedComments.get(row));
            }
            remoteDao.delete(delComs);
            for (PredefinedComment pc : delComs) {
                predefinedComments.remove(pc);
            }
            fireTableDataChanged();
        }

        @Override
        protected AbstractPersistentObject getNewEntity() {
            return new PredefinedComment("untitled");
        }
        
    }

    private void setColumnWidths() {
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(250);
        columnModel.getColumn(1).setPreferredWidth(70);
    }

    public RatingsEditor(final RemoteDao remoteDao) {
        model = new Model(remoteDao);
        table = new JXTable(model);
        table.setSortable(false);
        table.setHighlighters(HighlighterFactory.createSimpleStriping());
        setColumnWidths();
        add(new JScrollPane(table), BorderLayout.CENTER);
        createActions();
    }

    private void createActions() {
        JPopupMenu popup = new JPopupMenu();
        table.addMouseListener(new PopupListener(table, popup));

        Action deleteAction = new AbstractAction("Delete selected unused ratings...") {
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                List<Integer> selectedUnusedRows = model.selectedUnusedRows(selectedRows);
                if (selectedUnusedRows.size() > 0) {
                    if (TableUtil.isOkToDelete(RatingsEditor.this, selectedUnusedRows.size())) {
                        model.deleteRows(selectedUnusedRows);
                    }
                }
            }
        };
        popup.add(new JMenuItem(deleteAction));
        ActionUtil.attachDeleteAction(table, deleteAction);

        Action insertAction = new AbstractAction("Insert") {
            public void actionPerformed(ActionEvent e) {
                model.insertRow();
            }
        };
        popup.add(new JMenuItem(insertAction));
    }
    
}
