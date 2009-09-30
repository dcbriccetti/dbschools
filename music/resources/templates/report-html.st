<html>
<head>
    <style>
        .nobreak {
            white-space: nowrap
        }
    </style>
</head>
<body>
<h1>Testing Report for $musician.name$</h1>
$groupMemberships$<br/>
Prepared $datetime$<br/>
<h3>Assessment Criteria Counts</h3>
<p>These are the total number of times each assessment criterion was
checked in an assessment.</p>
<p>$keywordCounts$</p>
<h3>Assessments</h3>
<p>An assessment consists of the student playing a piece, and the teacher 
recording feedback and whether the student passed the test.</p>
<table border="1">
<tr>
<th>Date</th>
<th>Tester</th>
<th>Piece</th>
<th>Passed</th>
<th>Instrument</th>
<th>Comments</th>
</tr>
$assessments:{a|
<tr>
<td class='nobreak'>$a.assessmentTime$</td>
<td class='nobreak'>$a.user.displayName$</td>
<td class='nobreak'>$a.musicPiece$</td>
<td>$a.pass$</td>
<td class='nobreak'>$a.musicInstrument.name$</td>
<td>$a.notes$</td>
</tr>
}; separator="\n"$ 
</table>
<h3>Rejections</h3>
<p>A rejection occurs when a student is unable to be assessed because of
something like a missing book or mouthpiece.</p>
<table border="1">
<tr>
<th>Date</th>
<th>Tester</th>
<th>Reason</th>
</tr>
$rejections:{r|
<tr><td>$r.rejectionTime$</td>
<td>$r.user.displayName$</td>
<td>$r.rejectionReason$</td>
</tr>
}; separator="\n"$ 
</table>
<body>
</html>
