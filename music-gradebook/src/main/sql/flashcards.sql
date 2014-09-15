SELECT
  '<img src="' || perm_student_id || '.jpg"/>' picture,
  CASE WHEN nickname IS NULL THEN first_name
  ELSE first_name || ' (' || nickname || ')' END || ' ' ||
  last_name || ', ' ||
  CASE WHEN g."shortName" IS NULL THEN g.name
  ELSE g."shortName" END || ', ' ||
  i.name student
FROM musician_group mg LEFT JOIN musician m ON mg.musician_id = m.id
  LEFT JOIN music_group g ON mg.group_id = g.id
  LEFT JOIN instrument i ON mg.instrument_id = i.id
  left join "GroupTerm" t on t.term = school_year and t."groupId" = g.id
WHERE school_year = 2014
order by t.period, m.last_name;
