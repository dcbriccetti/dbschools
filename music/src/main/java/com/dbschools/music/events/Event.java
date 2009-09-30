package com.dbschools.music.events;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Indicates that an event has occurred.
 * @author David C. Briccetti
 */
public class Event implements Serializable {
    private static final long serialVersionUID = -4742235669924190638L;
    private final TypeCode typeCode;
    private final Object details;

    public Event(TypeCode typeCode) {
        super();
        this.typeCode = typeCode;
        this.details = null;
    }

    public Event(TypeCode typeCode, Object details) {
        super();
        this.typeCode = typeCode;
        this.details = details;
    }

    public TypeCode getTypeCode() {
        return typeCode;
    }

    public Object getDetails() {
        return details;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type code", typeCode).append("details", details).toString();
    }
    
}
