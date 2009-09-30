package com.dbschools.music.assess;

public class KeywordCounts implements Comparable<KeywordCounts> {

    private final String keyword;
    private final Integer count;

    public KeywordCounts(String keyword, Integer count) {
        super();
        this.keyword = keyword;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public String getKeyword() {
        return keyword;
    }

    public int compareTo(KeywordCounts o) {
        if (count.equals(o.count)) {
            return keyword.compareTo(o.keyword);
        }
        return o.count.compareTo(count);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KeywordCounts other = (KeywordCounts) obj;
        if (this.keyword != other.keyword && (this.keyword == null || !this.keyword.equals(other.keyword))) {
            return false;
        }
        if (this.count != other.count && (this.count == null || !this.count.equals(other.count))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.keyword != null ? this.keyword.hashCode() : 0);
        hash = 79 * hash + (this.count != null ? this.count.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return keyword + ": " + count;
    }
}
