package com.dbschools.music.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@Entity
@Table(name="tempo")
public final class Tempo extends AbstractPersistentObject 
        implements Comparable<Tempo> {

    private static final long serialVersionUID = 2091949681516455483L;

    private static final Logger LOG = Logger.getLogger(Tempo.class);
    
    private Integer id;
    private Piece piece;
    private Instrument instrument;
    private Integer tempo;
  
    public Tempo() {
        super();
    }

    public Tempo(Piece piece, Integer tempo) {
        this.piece = piece;
        this.tempo = tempo;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final Tempo other) {
        return id.compareTo(other.id);
    }
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="tempo_id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne(targetEntity=Piece.class)
    @JoinColumn(name="piece_id")
    public Piece getMusicPiece() {
        return piece;
    }

    public void setMusicPiece(Piece piece) {
        this.piece = piece;
    }

    @OneToOne(targetEntity=Instrument.class)
    @JoinColumn(name="instrument_id")
    public Instrument getMusicInstrument() {
        return instrument;
    }

    public void setMusicInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Integer getTempo() {
        return tempo;
    }

    public void setTempo(Integer tempo) {
        this.tempo = tempo;
    }

    @Override
    public String toString() {
        return tempo.toString();
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
        final Tempo other = (Tempo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
