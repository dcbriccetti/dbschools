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

drop table "GroupTerm";
create table "GroupTerm" (
    "groupId" integer not null,
    "term" integer not null,
    "period" integer not null
  );
alter table "GroupTerm" add constraint "GroupTermFK1" foreign key ("groupId") references "music_group"("id");
create unique index "GroupTerm_groupId_term" on "GroupTerm" ("groupId","term");

insert into "GroupTerm" values(58562, 2013, 4);
insert into "GroupTerm" values(8, 2013, 5);
insert into "GroupTerm" values(6, 2013, 1);
insert into "GroupTerm" values(5, 2013, 3);
insert into "GroupTerm" values(13, 2013, 0);
insert into "GroupTerm" values(11, 2013, 0);
insert into "GroupTerm" values(7, 2013, 2);

alter table music_group add column "shortName" varchar(128);
update music_group set "shortName" = 'Green' where id=5;
update music_group set "shortName" = 'Gold' where id=6;
update music_group set "shortName" = 'Symphonic' where id=7;
update music_group set "shortName" = 'Cadet 4' where id=58562;
update music_group set "shortName" = 'Cadet 5' where id=8;
update music_group set "shortName" = 'Messengers' where id=11;
update music_group set "shortName" = 'Crusaders' where id=13;
