package com.dbschools.music.orm;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

@Entity
@Table(name="musician")
public final class Musician extends AbstractPersistentObject implements Comparable<Musician> {
    private static final long serialVersionUID = 1714492557883608102L;

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(Musician.class);
    
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="musician_id")
    private Integer id;

    @Column(name="student_id", unique=true, nullable=false)
    private Long studentId;

    @Column(name="first_name",nullable=false)
    private String firstName;

    @Column(name="last_name",nullable=false)
    private String lastName;

    /** The school year of graduation (what year it is in the fall of the school year) */
    @Column(name="graduation_year", nullable=false)
    private Integer graduationYear;

    @Column(length=1)
    private String sex;

    @OneToMany(cascade=CascadeType.REMOVE, mappedBy="musician")
    @JoinColumn(name="musician_id")
    private Set<Assessment> assessments;
    
    @OneToMany(cascade=CascadeType.REMOVE, mappedBy="musician")
    @JoinColumn(name="musician_id")
    private Set<Rejection> rejections;

    @OneToMany(cascade=CascadeType.REMOVE, mappedBy="musician")
    @JoinColumn(name="musician_id")
    private Set<MusicianGroup> musicianGroups;
      
    public Musician() {
        super();
    }

    public Musician(Long studentId, String firstName, 
            String lastName, Integer graduationYear, String sex) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.graduationYear = graduationYear;
        this.sex = sex;
    }

    @Override
    public int compareTo(final Musician other) {
        int c = lastName.compareTo(other.lastName);
        return c == 0 ? firstName.compareTo(other.firstName) : c;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Transient
    public String getName() {
        return lastName + ", " + firstName;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(final Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public Set<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(final Set<Assessment> assessments) {
        this.assessments = assessments;
    }

    public Set<Rejection> getRejections() {
        return rejections;
    }

    public void setRejections(final Set<Rejection> rejections) {
        this.rejections = rejections;
    }

    public Set<MusicianGroup> getMusicianGroups() {
        return musicianGroups;
    }

    public void setMusicianGroups(final Set<MusicianGroup> musicianGroups) {
        this.musicianGroups = musicianGroups;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return getToStringBuilder()
            .append("id", id)
            .append("last name", lastName)
            .append("first name", firstName)
            .append("graduation year", graduationYear)
            .append("sex", sex).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Musician other = (Musician) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

}
