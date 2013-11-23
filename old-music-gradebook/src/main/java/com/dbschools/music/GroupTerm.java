package com.dbschools.music;

import java.io.Serializable;

/**
 * A group for a specific term.
 */
public class GroupTerm implements Serializable {
    private static final long serialVersionUID = -1847966382508836490L;

    private final int groupId;
    private final int termId;

    public GroupTerm(final int groupId, final int termId) {
        super();
        this.groupId = groupId;
        this.termId = termId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getTermId() {
        return termId;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = PRIME * groupId;
        result = PRIME * result + termId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final GroupTerm other = (GroupTerm) obj;
        if (groupId != other.groupId)
            return false;
        if (termId != other.termId)
            return false;
        return true;
    }
    
}