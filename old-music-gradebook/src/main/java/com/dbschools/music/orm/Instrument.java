package com.dbschools.music.orm;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@javax.persistence.Entity
@Table(name="instrument")
public class Instrument extends AbstractPersistentObject implements NamedItem,
        Comparable<Instrument> {

    private static final long serialVersionUID = 182261640419171614L;

    private static final Logger LOG = Logger.getLogger(Instrument.class);
    
    private Integer id;
    private String name;
    private Integer sequence;
    private List<Subinstrument> subinstruments;
      
    public Instrument() {
        super();
    }

    public Instrument(String name, Integer sequence) {
        super();
        this.name = name;
        this.sequence = sequence;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Instrument other) {
        final int seqComp = sequence.compareTo(other.getSequence());
        if (seqComp != 0) {
            return seqComp;
        }
        return name.compareTo(other.name);
    }
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="instrument_id")
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(final Integer sequence) {
        this.sequence = sequence;
    }

    @OneToMany(targetEntity=Subinstrument.class,fetch=FetchType.EAGER)
    @JoinColumn(name="instrument_id")
    public List<Subinstrument> getMusicSubinstruments() {
        return subinstruments;
    }

    public void setMusicSubinstruments(final List<Subinstrument> subinstruments) {
        this.subinstruments = subinstruments;
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
        final Instrument other = (Instrument) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getToStringBuilder().append("name", name)
                .append("sequence", sequence).toString();
    }

}
