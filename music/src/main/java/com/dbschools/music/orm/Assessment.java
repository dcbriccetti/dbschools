package com.dbschools.music.orm;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.dbschools.music.assess.SummaryRecordDependency;
import java.util.Collection;
import java.util.TreeSet;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
@Table(name="assessment")
public class Assessment extends AbstractPersistentObject
        implements SummaryRecordDependency, Comparable<Assessment> {

    private static final long serialVersionUID = 182261640419171614L;

    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="assessment_id")
    private Integer id;

    @ManyToOne(optional=false)
    @JoinColumn(name="musician_id")
    private Musician musician;

    @ManyToOne(optional=false)
    @JoinColumn(name="instrument_id")
    private Instrument instrument;

    @ManyToOne(optional=true)
    @JoinColumn(name="subinstrument_id")
    private Subinstrument subinstrument;
    
    @ManyToOne(optional=false)
    private User user;

    @Column(name="assessment_time", nullable=false)
    private Date assessmentTime;

    @ManyToOne(optional=false)
    @JoinColumn(name="piece_id")
    private Piece piece;
    
    @Column(nullable=false)
    private boolean pass;

    @Column(length=10000)
    private String notes;

    @ManyToMany(targetEntity=PredefinedComment.class, fetch=FetchType.EAGER)
    @JoinTable(name="assessment_tag", 
        joinColumns=@JoinColumn(name="assessment_id"))    
    private Set<PredefinedComment> predefinedComments;

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Assessment other) {
        final int c = musician.compareTo(other.musician);
        if (c != 0) {
            return c;
        }
        return assessmentTime.compareTo(other.assessmentTime);
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Musician getMusician() {
        return musician;
    }

    public void setMusician(Musician musician) {
        this.musician = musician;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getAssessmentTime() {
        return assessmentTime;
    }

    public void setAssessmentTime(Date assessmentTime) {
        this.assessmentTime = assessmentTime;
    }

    public Piece getMusicPiece() {
        return piece;
    }

    public void setMusicPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set<PredefinedComment> getPredefinedComments() {
        return predefinedComments;
    }

    public void setPredefinedComments(Collection<PredefinedComment> predefinedComments) {
        this.predefinedComments = new TreeSet<PredefinedComment>(predefinedComments);
    }

    public Instrument getMusicInstrument() {
        return instrument;
    }

    public void setMusicInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Subinstrument getMusicSubinstrument() {
        return subinstrument;
    }

    public void setMusicSubinstrument(Subinstrument subinstrument) {
        this.subinstrument = subinstrument;
    }
    
    @Override
    public String toString() {
        return getToStringBuilder()
                .append("musician.name", musician.getName())
                .append("piece", piece)
                .append("pass", pass)
                .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Assessment other = (Assessment) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
