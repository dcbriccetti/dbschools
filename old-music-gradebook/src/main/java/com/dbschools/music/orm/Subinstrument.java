package com.dbschools.music.orm;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@javax.persistence.Entity
@Table(name="subinstrument")
public final class Subinstrument extends AbstractPersistentObject implements NamedItem,
        Comparable<Subinstrument> {

    private static final long serialVersionUID = -7077329436607097927L;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(Subinstrument.class);
    
    private Integer id;
    private Instrument instrument;
    private String name;
    private Integer sequence;
      
    public Subinstrument() {
        super();
    }

    public Subinstrument(Instrument instrument, String name, Integer sequence) {
        super();
        this.instrument = instrument;
        this.name = name;
        this.sequence = sequence;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Subinstrument other) {
        return sequence.compareTo(other.getSequence());
    }
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="subinstrument_id")
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name="instrument_id")
    public Instrument getMusicInstrument() {
        return instrument;
    }

    public void setMusicInstrument(Instrument instrument) {
        this.instrument = instrument;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Subinstrument other = (Subinstrument) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

}
