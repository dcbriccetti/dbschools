-- Changes
SELECT
  g1.name "2015-16 Group",
  g2.name "2016-17 Group",
  count(*)
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
GROUP BY g1.name, g2.name
ORDER BY g1.name, g2.name;

-- Leaving
SELECT
  g1.name                  "2015-16 Group",
  m.graduation_year = 2015 "Graduated",
  count(*)
FROM musician m
  LEFT JOIN musician_group mg1 ON mg1.musician_id = m.id AND mg1.school_year = 2015
  LEFT JOIN musician_group mg2 ON mg2.musician_id = m.id AND mg2.school_year = 2016
  LEFT JOIN music_group g1 ON mg1.group_id = g1.id
  LEFT JOIN music_group g2 ON mg2.group_id = g2.id
WHERE
  mg1.school_year IS NOT NULL
  AND mg2.school_year IS NULL
  AND g1.name <> 'Jazz Bands'
GROUP BY g1.name, m.graduation_year
ORDER BY g1.name, m.graduation_year;

-- Incoming
SELECT
  g2.name "2016-17 Group",
  m.graduation_year,
  count(*)
FROM musician m
  LEFT JOIN musician_group mg1 ON mg1.musician_id = m.id AND mg1.school_year = 2015
  LEFT JOIN musician_group mg2 ON mg2.musician_id = m.id AND mg2.school_year = 2016
  LEFT JOIN music_group g1 ON mg1.group_id = g1.id
  LEFT JOIN music_group g2 ON mg2.group_id = g2.id
WHERE
  mg1.school_year IS NULL
  AND mg2.school_year IS NOT NULL
GROUP BY g2.name, m.graduation_year
ORDER BY g2.name, m.graduation_year;