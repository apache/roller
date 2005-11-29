
alter table roller_comment add column approved %BOOLEAN_SQL_TYPE_FALSE%;
%ALTER_TABLE_ROLLER_COMMENT_APPROVED_NOT_NULL%
alter table roller_comment add column pending %BOOLEAN_SQL_TYPE_TRUE%;
%ALTER_TABLE_ROLLER_COMMENT_PENDING_NOT_NULL%
update roller_comment set approved=1, pending=0 posttime=posttime;

alter table website add column commentmod %BOOLEAN_SQL_TYPE_FALSE%;
%ALTER_TABLE_WEBSITE_COMMENTMOD_NOT_NULL%
alter table website add column defaultallowcomments %BOOLEAN_SQL_TYPE_TRUE%;
%ALTER_TABLE_WEBSITE_DEFAULTALLOWCOMMENTS_NOT_NULL%
alter table website add column defaultcommentdays integer;
%ALTER_TABLE_WEBSITE_DEFAULTCOMMENTDAYS_NOT_NULL%
update website set commentmod=0, defaultallowcomments=1, defaultcommentdays=0, datecreated=datecreated;




