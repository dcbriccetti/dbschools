alter table music_user drop column password;
alter table music_user rename column epassword to password;
alter table music_user add column metronome integer not null default 1;