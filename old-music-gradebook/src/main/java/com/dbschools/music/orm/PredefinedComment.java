package com.dbschools.music.orm;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@javax.persistence.Entity
@Table(name="predefined_comment")
public class PredefinedComment extends AbstractPersistentObject
        implements Comparable<PredefinedComment> {

    private static final long serialVersionUID = 182261640419171614L;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(PredefinedComment.class);
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="comment_id")
    private Integer id;

    @Column(name="comment_text")
    private String text;
      
    public PredefinedComment() {
        super();
    }

    public PredefinedComment(String text) {
        this.text = text;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final PredefinedComment other) {
        return id.compareTo(other.getId());
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(final String commentText) {
        this.text = commentText;
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
        final PredefinedComment other = (PredefinedComment) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getToStringBuilder().append(text).toString();
    }

}
