package com.dbschools.music.orm;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="musician_group")
public class MusicianGroup extends AbstractPersistentObject
        implements Comparable<MusicianGroup>{

    private static final long serialVersionUID = -7878378674498101472L;

    private Integer id;
    private Musician musician;
    private Integer schoolYear;
    private Group group;
    private Instrument instrument;
    private Integer instrumentRanking;
    
    public MusicianGroup() {
        super();
    }

    public MusicianGroup(Musician musician, Integer schoolYear, Group group, Instrument instrument) {
        super();
        this.musician = musician;
        this.schoolYear = schoolYear;
        this.group = group;
        this.instrument = instrument;
    }

    private static final Comparator<MusicianGroup> 
            groupInstrumentMusicianComparator = new Comparator<MusicianGroup>() {
                public int compare(MusicianGroup o1, MusicianGroup o2) {
                    int c;
                    if ((c = o1.group.compareTo(o2.group)) != 0) {
                        return c;
                    }
                    if ((c = o1.instrument.compareTo(o2.instrument)) != 0) {
                        return c;
                    }
                    return o1.musician.compareTo(o2.musician);
                }};
      
    private static final Comparator<MusicianGroup> 
            musicianYearGroupInstrumentComparator = new Comparator<MusicianGroup>() {
                public int compare(MusicianGroup o1, MusicianGroup o2) {
                    int c;
                    if ((c = o1.musician.compareTo(o2.musician)) != 0) {
                        return c;
                    }
                    if ((c = o1.schoolYear.compareTo(o2.schoolYear)) != 0) {
                        return c;
                    }
                    if ((c = o1.group.compareTo(o2.group)) != 0) {
                        return c;
                    }
                    return o1.instrument.compareTo(o2.instrument);
                }};
      
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final MusicianGroup other) {
        int c;
        if ((c = musician.compareTo(other.musician)) != 0) {
            return c;
        }
        if ((c = group.compareTo(other.group)) != 0) {
            return c;
        }
        return instrument.compareTo(other.instrument);
    }
    
    /**
     * Gets a Comparator that will sort MusicianGroups by group, instrument, and musician.
     * @return a Comparator
     */
    public static Comparator<MusicianGroup> getGroupInstrumentMusicianComparator() {
        return groupInstrumentMusicianComparator;
    }

    /**
     * Gets a Comparator that will sort MusicianGroups by musician, year group, and instrument.
     * @return a Comparator
     */
    public static Comparator<MusicianGroup> getMusicianYearGroupInstrumentComparator() {
        return musicianYearGroupInstrumentComparator;
    }

    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Integer getId() {
        return id;
    }

    public void setId(final Integer entityMemberId) {
        this.id = entityMemberId;
    }

    @ManyToOne(targetEntity=Musician.class, optional=false) @JoinColumn(name="musician_id")
    public Musician getMusician() {
        return musician;
    }

    public void setMusician(final Musician musician) {
        this.musician = musician;
    }

    @Column(name="school_year", nullable=false)
    public Integer getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(Integer term) {
        this.schoolYear = term;
    }

    @ManyToOne(targetEntity=Group.class, optional=false) @JoinColumn(name="group_id")
    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    @ManyToOne(targetEntity=Instrument.class, optional=false) @JoinColumn(name="instrument_id")
    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(final Instrument instrument) {
        this.instrument = instrument;
    }

    @Column(name="instrument_ranking")
    public Integer getInstrumentRanking() {
        return instrumentRanking;
    }

    public void setInstrumentRanking(final Integer instrumentRanking) {
        this.instrumentRanking = instrumentRanking;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((group == null) ? 0 : group.hashCode());
        result = PRIME * result + ((musician == null) ? 0 : musician.hashCode());
        result = PRIME * result + ((schoolYear == null) ? 0 : schoolYear.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final MusicianGroup other = (MusicianGroup) obj;
        if (group == null) {
            if (other.group != null)
                return false;
        } else if (!group.equals(other.group))
            return false;
        if (musician == null) {
            if (other.musician != null)
                return false;
        } else if (!musician.equals(other.musician))
            return false;
        if (schoolYear == null) {
            if (other.schoolYear != null)
                return false;
        } else if (!schoolYear.equals(other.schoolYear))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getToStringBuilder().
                append("schoolYear", schoolYear).
                append("musician", musician).
                append("group", group).
                append("instrument", instrument).toString();
    }

}
