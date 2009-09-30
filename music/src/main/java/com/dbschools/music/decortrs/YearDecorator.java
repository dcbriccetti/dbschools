package com.dbschools.music.decortrs;

/**
 * Decorator for a school year, which presents a year in school year format. 
 * For example 2007 displays as 2007-08.
 * @author David C. Briccetti
 */
public class YearDecorator {
    private final Integer year;

    public YearDecorator(final Integer year) {
        super();
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    @Override
    public String toString() {
        return String.format("%d\u2013%s", year, Integer.toString(year + 1).substring(2));
    }
    
}