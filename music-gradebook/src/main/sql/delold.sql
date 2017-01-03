delete from assessment_tag at where at.assessment_id in
(select id from assessment where assessment_time < '2013-07-01');

delete from assessment where assessment_time < '2013-07-01';