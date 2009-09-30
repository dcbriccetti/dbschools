/*
 * DBSchools
 * Copyright (C) 2005 David C. Briccetti
 * www.davebsoft.com
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.dbschools.music;


/**
 * Exports the current group to a file.
 */
final class Exporter {
    /**
     * Exports data to an HTML file.
     * @param groupName 
     * @param summaryTableModel 
     * @param studentRecordsCache 
     */
    
    //FIXME
/*    public static void export(String groupName, 
            SummaryTableModel summaryTableModel,
            StudentRecordsCache studentRecordsCache) {
	    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    PrintWriter out = new PrintWriter(stream);
        writeOutput(groupName, summaryTableModel, studentRecordsCache, out);
        out.close();
   		
        try {
            stream.close();
            final byte[] bs = stream.toByteArray();
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bs);
            try {
            	// Try to get the data using JNLP's FileOpenService
                final FileSaveService fss = 
                        (FileSaveService) ServiceManager.lookup
                        (FileSaveService.class.getName());
    	    	fss.saveFileDialog(null, new String[] {"html"}, //$NON-NLS-1$
    	    	        inputStream, groupName + ".html"); //$NON-NLS-1$
            } catch (UnavailableServiceException e) {
            	// If we're not running under JNLP, we must be standalone testing
        		final FileWriter writer = new FileWriter(groupName + ".html"); //$NON-NLS-1$
        		out = new PrintWriter(writer, true);
        		final BufferedReader br = new BufferedReader(
                        new InputStreamReader(inputStream));
        		String line;
        		while ((line = br.readLine()) != null) {
        		    out.println(line);
        		}
    		}
        } catch (IOException e) {
            e.printStackTrace();
        }
   }

    private static void writeOutput(String groupName, 
            SummaryTableModel summaryTableModel, 
            StudentRecordsCache studentRecordsCache, PrintWriter out) {
        out.println("<html>");
        out.println("<h1>Stanley Music Test Tracking System</h1>");
        out.println("<h2>" + groupName + "</h2>");
        out.println("<h3>" + new Date() + "</h3>");
        out.println("<h4>Summaries</h4>");
        out.println("<table border='1'>");
        out.println("<tr><th>Student</th><th>Gr</th><th>Instrument</th><th>Next Piece</th><th>Last Assessment</th><th>Days Ago</th><th>Num</th><th>Pass</th><th>Reject</th></tr>");
        final DateFormat dateFormatNormal = new SimpleDateFormat("MMM dd hh:mm a"); //$NON-NLS-1$
        String lastStudent = "";
        
        for (int r = 0; r < summaryTableModel.getRowCount(); ++r) {
            final SummaryRecord rec = summaryTableModel.getRecordAt(r);
            out.println("<tr>");
            out.println("<td>");
            lastStudent = displayStudentIfChanged(out, lastStudent, rec);
            out.println("</td>");
            out.println("<td>" + rec.getMusician().getGrade() + "</td>");
            out.println("<td>" + rec.getInstrument() + "</td>");
            
            out.println("<td>");
            
            if (rec.getNextPiece() != null) {
                try {
                    out.println(rec.getNextPiece().toStringWithBook());
                } catch (TorqueException e1) {
                    e1.printStackTrace();
                }
            }
            out.println("</td>");
            out.println("<td>");
            
            if (rec.getLastAssessment() != null) {
                out.println(makeSpacesNonBreakable(dateFormatNormal
                        .format(rec.getLastAssessment())));
            }
            out.println("</td>");
            out.println("<td>");
            
            if (rec.getDaysSinceLastAssessment() != null) { 
                out.println(rec.getDaysSinceLastAssessment());
            }
            out.println("</td>");
            out.println("<td>");
            out.println(rec.getNumAssessments());
            out.println("</td>");
            out.println("<td>");
            out.println(rec.getNumPasses());
            out.println("</td>");
            out.println("<td>");
            out.println(rec.getNumRejections());
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");
      
        out.println("<h4>Assessments</h4>");
        out.println("<table border='1'>");
        out.println("<tr><th>Student</th>");
        for (int i = 0; i < AssessmentHistoryTableModel.TITLES.length; ++i) {
            out.println("<th>" + AssessmentHistoryTableModel.TITLES[i] + "</th>"); 
        }
        out.println("</tr>");
        
        for (int r = 0; r < summaryTableModel.getRowCount(); ++r) {
            final SummaryRecord rec = summaryTableModel.getRecordAt(r);
            final Integer musicianId = rec.getMusician().getId();
            try {
                final StudentRecords studentRecords = studentRecordsCache
                        .getRecordsForMember(musicianId);
                final Collection assessments = studentRecords.getAssessments();
                for (final Iterator it = assessments.iterator(); it.hasNext(); ) {
                    final AssessmentReportRecord arr = 
                            (AssessmentReportRecord) it.next();
                    
                    out.println("<tr>");
                    out.println("<td>");
                    lastStudent = displayStudentIfChanged(out, lastStudent, rec);
                    out.println("</td>");
                    out.println("<td>" + makeSpacesNonBreakable(dateFormatNormal
                            .format(arr.getAssessment().getAssessmentTime())) + 
                            "</td>");
                    out.println("<td>" + arr.getAssessment().getUserName() + 
                            "</td>");
                    try {
                        out.println("<td>" + makeSpacesNonBreakable(
                                arr.getPiece().toStringWithBook()) + "</td>");
                    } catch (TorqueException e) {
                        e.printStackTrace();
                    }
                    out.println("<td>" + 
                            (arr.getAssessment().getPass() ? "Y" : "&nbsp;") + 
                            "</td>");
                    out.println("<td>" + arr.getComments() + "</td>");
                    out.println("</tr>");
                }
            } catch (DatabaseAccessException e1) {
                e1.printStackTrace();
            }
        }
        
        out.println("</table>");
        
        out.println("<h4>Unpreparedness Rejections</h4>");
        out.println("<table border='1'>");
        out.println("<tr><th>Student</th>");
        for (int i = 0; i < RejectionHistoryTableModel.TITLES.length; ++i) {
            out.println("<th>" + RejectionHistoryTableModel.TITLES[i] + "</th>"); 
        }
        out.println("</tr>");
        
        for (int r = 0; r < summaryTableModel.getRowCount(); ++r) {
            final SummaryRecord rec = summaryTableModel.getRecordAt(r);
            final Integer musicianId = rec.getMusician().getId();
            try {
                final StudentRecords studentRecords = studentRecordsCache
                        .getRecordsForMember(musicianId);
                final Collection rejections = studentRecords.getRejections();
                for (final Iterator it = rejections.iterator(); it.hasNext(); ) {
                    final RejectionReportRecord rrr = (RejectionReportRecord) it.next();
                    out.println("<tr>");
                    out.println("<td>");
                    lastStudent = displayStudentIfChanged(out, lastStudent, rec);
                    out.println("</td>");
                    out.println("<td>" + makeSpacesNonBreakable(dateFormatNormal
                            .format(rrr.getRejectionTime())) + "</td>");
                    out.println("<td>" + rrr.getTester() + "</td>");
                    out.println("<td>" + rrr.getRejectionReason() + "</td>");
                    out.println("</tr>");
                }
            } catch (DatabaseAccessException e1) {
                e1.printStackTrace();
            }
        }
        
        
        out.println("</table>");
        out.println("</html>");
    }
*/
//	private static String makeSpacesNonBreakable(String s) {
//	    return s.replaceAll(" ", "&nbsp;");
//	}

//    private static String displayStudentIfChanged(final PrintWriter out, String lastStudent, final SummaryRecord rec) {
//        final String thisStudent = rec.getMusician().toString(); 
//        if (! thisStudent.equals(lastStudent)) {
//            out.println(thisStudent);
//            lastStudent = thisStudent;
//        }
//        return lastStudent;
//    }
}
