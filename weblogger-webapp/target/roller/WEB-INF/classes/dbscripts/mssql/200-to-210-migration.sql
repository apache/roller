

-- Add to roller_comment table: approved and pending fields
update roller_comment set approved=1, pending=0, posttime=posttime;

-- Add to website table: commentmod, blacklist, defaultallowcomments and defaultcommentdays 
    alter table website add column blacklist text default null;

update website set commentmod=0, defaultallowcomments=1, defaultcommentdays=7, blacklist='', datecreated=datecreated;

-- Add weblog displaydays column
