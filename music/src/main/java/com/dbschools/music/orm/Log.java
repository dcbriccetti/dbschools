package com.dbschools.music.orm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.dbschools.music.events.TypeCode;
import java.util.Date;

@Entity
@Table(name="log")
public class Log extends AbstractPersistentObject implements Comparable<Log> {
    private static final long serialVersionUID = 171449255788608102L;

    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="log_id")
    private Integer id;

    @Column(nullable=false)
    private Date timestamp;
    
    @Column(nullable=false)
    private TypeCode typeCode;

    @Column(nullable=false)
    private String userId;
    
    @Column(length=10000, nullable=true)
    private String details;

    public Log() {
        super();
        timestamp = new Date();
    }

    public Log(TypeCode typeCode, String userId, String details) {
        this();
        this.typeCode = typeCode;
        this.userId = userId;
        this.details = details;
    }

    public Integer getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public TypeCode getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(TypeCode typeCode) {
        this.typeCode = typeCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public int compareTo(Log other) {
        return id.compareTo(other.id);
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
        final Log other = (Log) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("type code", typeCode)
            .append("details", details).toString();
    }

}
