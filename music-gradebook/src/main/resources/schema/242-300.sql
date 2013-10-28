alter table music_user add column epassword varchar not null default '';
alter table music_user add column enabled boolean not null default 't';

alter table assessment rename column assessment_id to id;
alter table music_group rename column group_id to id;
alter table instrument rename column instrument_id to id;
alter table subinstrument rename column subinstrument_id to id;
alter table musician rename column musician_id to id;
alter table piece rename column piece_id to id;
alter table predefined_comment rename column comment_id to id;
alter table rejection_reason rename column rejection_reason_id to id;
alter table tempo rename column tempo_id to id;

alter table musician drop column sex;
