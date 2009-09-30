package com.dbschools.music.orm;

import java.io.Serializable;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Abstract class for all persistent objects.
 */
public abstract class AbstractPersistentObject implements Serializable {

    protected ToStringBuilder getToStringBuilder() {
        StandardToStringStyle tss = new StandardToStringStyle();
        tss.setUseShortClassName(true);
        tss.setUseIdentityHashCode(false);
        return new ToStringBuilder(this, tss);
    }

}
