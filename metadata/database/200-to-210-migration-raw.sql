
-- Add to roller_comment table: approved and pending fields

alter table roller_comment add column approved @BOOLEAN_SQL_TYPE@;
@ALTER_TABLE_ROLLER_COMMENT_APPROVED_DEFAULT_FALSE@;
@ALTER_TABLE_ROLLER_COMMENT_APPROVED_NOT_NULL@;

alter table roller_comment add column pending @BOOLEAN_SQL_TYPE@;
@ALTER_TABLE_ROLLER_COMMENT_PENDING_DEFAULT_FALSE@;
@ALTER_TABLE_ROLLER_COMMENT_PENDING_NOT_NULL@;

update roller_comment set approved=1, pending=0, posttime=posttime;


-- Add to website table: commentmod, blacklist, defaultallowcomments and defaultcommentdays 

alter table website add column commentmod @BOOLEAN_SQL_TYPE@;
@ALTER_TABLE_WEBSITE_COMMENTMOD_DEFAULT_FALSE@;
@ALTER_TABLE_WEBSITE_COMMENTMOD_NOT_NULL@;

alter table website add column defaultallowcomments @BOOLEAN_SQL_TYPE@;
@ALTER_TABLE_WEBSITE_DEFAULTALLOWCOMMENTS_DEFAULT_TRUE@;
@ALTER_TABLE_WEBSITE_DEFAULTALLOWCOMMENTS_NOT_NULL@;

alter table website add column defaultcommentdays integer;
@ALTER_TABLE_WEBSITE_DEFAULTCOMMENTDAYS_DEFAULT_7@;
@ALTER_TABLE_WEBSITE_DEFAULTCOMMENTDAYS_NOT_NULL@;

alter table website add column blacklist @TEXT_SQL_TYPE@;

update website set commentmod=0, defaultallowcomments=1, defaultcommentdays=0, blacklist='', datecreated=datecreated;




