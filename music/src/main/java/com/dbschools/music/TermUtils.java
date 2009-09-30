package com.dbschools.music;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TermUtils {
    public static Integer getCurrentTerm() {
        Calendar calendar = new GregorianCalendar();
        final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        final int year = calendar.get(Calendar.YEAR);
        return (dayOfYear > 7 * 31 && dayOfYear <= 365) ? year : year - 1;   
    }

    public static Object graduationYearAsGrade(int graduationYear, int schoolYear) {
        return 8 - (graduationYear - schoolYear);
    }

    public static Integer gradeAsGraduationYear(int grade, int schoolYear) {
        return 8 - grade + schoolYear;
    }
    
}