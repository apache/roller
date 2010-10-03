

-- Add to roller_comment table: approved and pending fields
    alter table roller_comment add column approved boolean;
    alter table roller_comment alter approved set default true;
    update roller_comment set approved=true;
    alter table roller_comment alter approved set not null;
    alter table roller_comment add column pending boolean;
    alter table roller_comment alter pending set default false;
    update roller_comment set pending=false;
    alter table roller_comment alter pending set not null;
update roller_comment set approved=true, pending=false, posttime=posttime;

-- Add to website table: commentmod, blacklist, defaultallowcomments and defaultcommentdays 
    alter table website add column commentmod boolean;
    alter table website alter commentmod set default false;
    update website set commentmod=false;
    alter table website alter commentmod set not null;
    alter table website add column defaultallowcomments boolean;
    alter table website alter defaultallowcomments set default true;
    update website set defaultallowcomments=true;
    alter table website alter defaultallowcomments set not null;
    alter table website add column defaultcommentdays integer;
    alter table website alter defaultcommentdays set default 7;
    update website set defaultcommentdays=7;
    alter table website alter defaultcommentdays set not null;
    alter table website add column blacklist text default null;

update website set commentmod=false, defaultallowcomments=true, defaultcommentdays=7, blacklist='', datecreated=datecreated;

-- Add weblog displaydays column
    alter table website add column displaycnt integer;
    alter table website alter displaycnt set default 15;
    update website set displaycnt=15;
    alter table website alter displaycnt set not null;
