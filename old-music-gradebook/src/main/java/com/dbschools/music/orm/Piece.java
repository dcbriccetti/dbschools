package com.dbschools.music.orm;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="piece")
public class Piece extends AbstractPersistentObject 
        implements Comparable<Piece> {

    private static final long serialVersionUID = 2091949681516455483L;

    private Integer id;
    private Integer testOrder;
    private String name;
    private Set<Tempo> tempos;
    
    public Piece() {
        super();
    }

    public Piece(Integer testOrder, String name) {
        this.testOrder = testOrder;
        this.name = name;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final Piece other) {
        final int orderCmp = testOrder.compareTo(other.testOrder);
        if (orderCmp != 0) {
            return orderCmp;
        }
        return id.compareTo(other.id);
    }
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="piece_id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    @Column(name="test_order",nullable=false)
    public Integer getTestOrder() {
        return testOrder;
    }
    
    public void setTestOrder(Integer testOrder) {
        this.testOrder = testOrder;
    }

    @Column(nullable=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch=FetchType.EAGER,targetEntity=Tempo.class)
    @JoinColumn(name="piece_id")
    public Set<Tempo> getTempos() {
        return tempos;
    }

    public void setTempos(Set<Tempo> tempos) {
        this.tempos = tempos;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null) // This line added by hand for some Swing combo box notifications
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Piece other = (Piece) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
