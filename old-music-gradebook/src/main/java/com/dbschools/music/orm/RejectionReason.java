package com.dbschools.music.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@Entity
@Table(name="rejection_reason")
public final class RejectionReason extends AbstractPersistentObject implements Comparable<RejectionReason> {

    private static final long serialVersionUID = 4897469653235669035L;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(RejectionReason.class);
    
    private Integer id;
    private String text;
      
    public RejectionReason() {
        super();
    }

    public RejectionReason(String text) {
        this.text = text;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final RejectionReason other) {
        return id.compareTo(other.getId());
    }
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="rejection_reason_id")
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @Column(name="rejection_reason_text")
    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
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
        final RejectionReason other = (RejectionReason) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
