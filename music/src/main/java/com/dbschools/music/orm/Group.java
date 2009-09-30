package com.dbschools.music.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@Entity
@Table(name="music_group")
public class Group extends AbstractPersistentObject implements NamedItem,
        Comparable<Group> {

    private static final long serialVersionUID = 5089863901934890340L;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(Group.class);
    
    private Integer id;
    private String name;
    private Integer sequence;
    private boolean doesTesting;
    
    public Group() {
        super();
    }

    public Group(String name, Integer sequence, boolean doesTesting) {
        super();
        this.name = name;
        this.sequence = sequence;
        this.doesTesting = doesTesting;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Group other) {
        final int sequenceCompareResult = sequence.compareTo(other.sequence);
        if (sequenceCompareResult == 0) {
            return name.compareTo(other.name);
        }
        return sequenceCompareResult;
    }
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="group_id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer entityMemberId) {
        this.id = entityMemberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    @Column(name="does_testing")
    public boolean isDoesTesting() {
        return doesTesting;
    }

    public void setDoesTesting(boolean doesTesting) {
        this.doesTesting = doesTesting;
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
        final Group other = (Group) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name; // This is used in a combo box
    }

}
