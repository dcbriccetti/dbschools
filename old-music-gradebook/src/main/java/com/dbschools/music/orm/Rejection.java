package com.dbschools.music.orm;

import com.dbschools.music.assess.SummaryRecordDependency;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name="rejection")
public class Rejection extends AbstractPersistentObject
        implements SummaryRecordDependency, Comparable<Rejection> {

    private static final long serialVersionUID = 182261640419171614L;

    private Integer id;
    private Musician musician;
    private User user;
    private Date rejectionTime;
    private String notes;
    private RejectionReason rejectionReason;

    public Rejection() {
        // Empty
    }

    public Rejection(Date rejectionTime, User user, Musician musician, 
            RejectionReason rejectionReason, String notes) {
        this.rejectionTime = rejectionTime;
        this.user = user;
        this.musician = musician;
        this.rejectionReason = rejectionReason;
        this.notes = notes;
    }

    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="rejection_id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name="musician_id")
    public Musician getMusician() {
        return musician;
    }

    public void setMusician(Musician musician) {
        this.musician = musician;
    }

    @ManyToOne(optional=false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(name="rejection_time")
    public Date getRejectionTime() {
        return rejectionTime;
    }

    public void setRejectionTime(Date rejectionTime) {
        this.rejectionTime = rejectionTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @OneToOne(targetEntity=RejectionReason.class)
    @JoinColumn(name="rejection_reason_id")
    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return getToStringBuilder()
                .append("musician.name", musician.getName())
                .append("rejection reason", rejectionReason)
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
        final Rejection other = (Rejection) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Rejection other) {
        return rejectionTime.compareTo(other.rejectionTime);
    }
    
}
