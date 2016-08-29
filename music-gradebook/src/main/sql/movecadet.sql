UPDATE musician_group
SET group_id = 58562 -- Cadet 4
WHERE group_id = 3
      AND school_year = 2016
      AND musician_id IN (SELECT id
                          FROM musician
                          WHERE perm_student_id IN (
                            600001460 -- ...
                          ));

UPDATE musician_group
SET group_id = 8 -- Cadet 5
WHERE group_id = 3
      AND school_year = 2016
      AND musician_id IN (SELECT id
                          FROM musician
                          WHERE perm_student_id IN (
                            500002274 -- ...
                          ));
