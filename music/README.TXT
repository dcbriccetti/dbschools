


Installation Instructions

Modify the data files in the resources/data directory

Group.txt contains a list of your music groups (Concert Band, Orchestra, 
etc.). Instrument.txt contains a list of instruments. Piece.txt contains 
a list of the pieces that your musicians will play during testing, and 
a tempo for each. See the comments at the beginning of each file for more 
information.

Create a database

The application was developed and tested using PostgreSQL, but since it
uses Hibernate, other databases such as MySQL should also work. If you
aren't using PostgreSQL, you'll need to modify hibernate.cfg.xml and
spring-config.xml.

create user dbschools password 'dbschools';
create database dbsmusic with owner=dbschools encoding='UTF8';

Modify Spring configuration file

Edit spring-config.xml and change the database name, user and password
if necessary.

Run the DBSchools Music Server

com.dbschools.music.server.MusicServerImpl

The server should create the tables, which you can confirm:

dbsmusic=# \d
                 List of relations
 Schema |        Name        |   Type   |   Owner   
--------+--------------------+----------+-----------
 public | assessment         | table    | dbschools
 public | assessment_comment | table    | dbschools
 public | hibernate_sequence | sequence | dbschools
 public | instrument         | table    | dbschools
 public | music_group        | table    | dbschools
 public | music_user         | table    | dbschools
 public | musician           | table    | dbschools
 public | musician_group     | table    | dbschools
 public | piece              | table    | dbschools
 public | predefined_comment | table    | dbschools
 public | rejection          | table    | dbschools
 public | rejection_reason   | table    | dbschools
 public | subinstrument      | table    | dbschools
 public | tempo              | table    | dbschools
 public | term               | table    | dbschools
(15 rows)

Some of the tables should have rows, which you can see:

dbsmusic=# select * from music_group;
 group_id | does_testing |         name          | sequence 
----------+--------------+-----------------------+----------
       22 | t            | Beginning Instruments |        0
       23 | t            | Cadet Band            |        0
       24 | t            | Concert Band          |        0
       25 | t            | Symphonic Band        |        0
(4 rows)

Run the DBSchools Musicians Editor

com.dbschools.music.MusiciansEditorApp

Log in as one of the users you created in the data file User.txt.
You should see a selection dialog showing all your groups
and instruments. Push OK.

Now you should see an empty list of musicians. Try creating a 
musician using the New button. You can also import a batch
of musicians into a group with the Import button. Here
is some sample data you can try:

101001	Smith	William	7	M
101002	Baker	David	6	M
101007	Jones	Debra	7	F
102200	Walker	Susan	6	F

Select the group and push import to import the students,
assign them to the group and dismiss the dialog. Push
refresh to show the updates. (Using refresh will not
be required in a future version.)

To assign instruments to the students, select one or more
students, then right click to bring up a context menu. Use
Move To Instrument.

Run the DBSchools Music Testing Program

com.dbschools.music.TestingApp

Push OK at the Student Selection Dialog. You should see a list of 
the students you created, all showing the first piece (see Piece.txt)
as the next piece to test on. With filled with real data and a
lot of testing activity, the display becomes more interesting,
making it easy to see where everybody is with their testing, and
who should next be selected for testing.

Double click on one of the students. A history display appears,
showing all the tests and rejections for the student. It 
will be empty. Push the Test button. The Test Record Dialog 
appears. Imagine you are testing this student on the first
piece and that he has trouble with articulation and dynamics.
Check those checkboxes, then push Save. The dialog remains 
open in case you want to give another test. Imagine the 
student plays the piece again, this time so well that you
want record some positive notes. Type "Beautiful tone" into
the text area to the right of the checkboxes, check the 
Pass checkbox, and then push Save. The dialog closes.

Now you see that the student is beginning to develop a bit
of a testing history. You can who the tester was, whether
the attempt was a success, and any criteria checked or 
comments entered. Close this window, then push Refresh on
the main window. 

Click on the "Last Assessment" column heading, and the student
you just tested will sort to the bottom. Students who haven't 
been testing in the longest time will sort to the top.



