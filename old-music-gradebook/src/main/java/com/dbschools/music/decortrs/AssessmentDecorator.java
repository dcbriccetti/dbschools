package com.dbschools.music.decortrs;

import com.dbschools.music.orm.Assessment;
import com.dbschools.music.orm.Instrument;
import com.dbschools.music.orm.Piece;
import com.dbschools.music.orm.PredefinedComment;
import com.dbschools.music.orm.Subinstrument;
import com.dbschools.music.orm.User;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Wraps an assessment for display formatting.
 * @author Dave Briccetti
 */
public class AssessmentDecorator {
    static Logger log = Logger.getLogger(AssessmentDecorator.class);
    private static final String RATING_SEPARATOR = ", ";
    private static final String NOTE_SEPARATOR = "; ";

    private final Assessment assessment;
    private static DateFormat dateFormatNormal = new SimpleDateFormat(
            Constants.DATE_TIME_FORMAT);

    public AssessmentDecorator(Assessment assessment) {
        this.assessment = assessment;
    }

    public String getNotes() {
        return formatNotes(assessment);
    }

    public String getAssessmentTime() {
        return dateFormatNormal.format(assessment.getAssessmentTime());
    }

    public String getPass() {
        return assessment.isPass() ? "Pass" : "Fail";
    }

    public User getUser() {
        return assessment.getUser();
    }

    public Subinstrument getMusicSubinstrument() {
        return assessment.getMusicSubinstrument();
    }

    public Piece getMusicPiece() {
        return assessment.getMusicPiece();
    }

    public Instrument getMusicInstrument() {
        return assessment.getMusicInstrument();
    }
    
    public static String formatNotes(Assessment assess) {
        StringBuilder sb = new StringBuilder();
        final Collection<PredefinedComment> assessmentComments = assess.getPredefinedComments();
        if (assessmentComments == null) {
            log.error("Comments null");
            return "null!";
        }
        List<PredefinedComment> comments = new ArrayList<PredefinedComment>
                (assessmentComments);
        Collections.sort(comments, new Comparator<PredefinedComment>(){
            public int compare(PredefinedComment o1,
                    PredefinedComment o2) {
                return o1.getText().compareTo(o2.getText());
            }});
        for (PredefinedComment comment : comments) {
            if (sb.length() > 0) {
                sb.append(RATING_SEPARATOR);
            }
            sb.append(comment.getText());
        }

        final String notes = assess.getNotes();
        if (StringUtils.isNotBlank(notes)) {
            if (sb.length() > 0) {
                sb.append(NOTE_SEPARATOR);
            }
            sb.append(notes);
        }
        return sb.toString();
    }
    
}
