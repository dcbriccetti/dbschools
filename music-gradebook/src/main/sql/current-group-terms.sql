select * from "GroupTerm" gt left join music_group mg on gt."groupId" = mg.id where term = 2016 order by period;
