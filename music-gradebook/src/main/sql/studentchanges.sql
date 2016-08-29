SELECT
  last_name,
  first_name,
  nickname,
  r1.rank - r2.rank "Steps",
  g1.name           "2015-16 Group",
  g2.name           "2016-17 Group",
  i1.name           "Instrument"
FROM musician m
  LEFT JOIN musician_group mg1 ON mg1.musician_id = m.id AND mg1.school_year = 2015
  LEFT JOIN musician_group mg2 ON mg2.musician_id = m.id AND mg2.school_year = 2016
  LEFT JOIN music_group g1 ON mg1.group_id = g1.id
  LEFT JOIN music_group g2 ON mg2.group_id = g2.id
  LEFT JOIN "GroupRank" r1 ON g1.name LIKE r1."starts_with" || '%'
  LEFT JOIN "GroupRank" r2 ON g2.name LIKE r2."starts_with" || '%'
  LEFT JOIN instrument i1 ON mg1.instrument_id = i1.id
WHERE
  mg1.school_year IS NOT NULL
  AND mg2.school_year IS NOT NULL
  AND g1.name <> 'Jazz Bands'
ORDER BY last_name, first_name;