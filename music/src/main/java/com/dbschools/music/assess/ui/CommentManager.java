package com.dbschools.music.assess.ui;

import com.dbschools.music.dao.RemoteDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.dbschools.music.orm.PredefinedComment;
import com.google.common.base.Nullable;

/**
 * Manager for the details of the comments.
 */
final class CommentManager {
    private final RemoteDao remoteDao;
    private List<CommentCheckBox> commentCheckBoxes;

    CommentManager(RemoteDao remoteDao) {
        super();
        this.remoteDao = remoteDao;
    }

    void setUpComments(@Nullable Collection<PredefinedComment> selectedComments, JPanel scrollableCommentsPanel) {
        final Collection<PredefinedComment> predefinedComments = remoteDao.getComments();
        int row = 0;
        commentCheckBoxes = new ArrayList<CommentCheckBox>();

        for (PredefinedComment predefinedComment : predefinedComments) {
            final CommentCheckBox commentControl = new CommentCheckBox(predefinedComment.getText(), predefinedComment.getId(), 
                    isCommentSelected(selectedComments, predefinedComment)); 
            commentCheckBoxes.add(commentControl);

            if (row == 0) {
                scrollableCommentsPanel.setLayout(
                        new BoxLayout(scrollableCommentsPanel, BoxLayout.Y_AXIS));
            }
            scrollableCommentsPanel.add(commentControl);
        }
    }

    private boolean isCommentSelected(@Nullable Collection<PredefinedComment> selectedComments,
            PredefinedComment predefinedComment) {
        return selectedComments != null && selectedComments.contains(predefinedComment);
    }

    Collection<PredefinedComment> getSelectedComments() {
        Collection<PredefinedComment> comments = new ArrayList<PredefinedComment>();
        final Collection<Integer> commentList = new ArrayList<Integer>();
        for (CommentCheckBox box : commentCheckBoxes) {
            if (box.isSelected()) {
                commentList.add(box.getId());
            }
        }
        for (PredefinedComment predefinedComment: remoteDao.getComments()) {
            if (commentList.contains(predefinedComment.getId())) {
                comments.add(predefinedComment);
            }
        }
        return comments;
    }
    
    void resetRatingControls() {
        for (CommentCheckBox box : commentCheckBoxes) {
            box.setSelected(false);
        }
    }

}
