

create table rag_properties (
    name     varchar(255) not null primary key,
    value    text
);

create table rag_planet (
    id              varchar(48) not null primary key,
    handle          varchar(32) not null,
    title           varchar(255) not null,
    description     varchar(255)
);
alter table rag_planet add constraint ragp_handle_uq unique ( handle );

-- ensure that every weblog entry has a valid locale

update weblogentry e set 
   e.pubtime=pubtime, 
   e.updatetime=updatetime, 
   e.locale=(select locale from website where website.id=e.websiteid) 
   where e.locale is null or length(e.locale)=0;

-- add new planet_id column to planet group table
    alter table rag_group add column planet_id varchar(48) default null;

-- upgrade old planet users to work with the new Roller Planet code
-- all groups must have a planet now, so provide a default planet and
-- put all existing groups in the new default planet
insert into rag_planet (id,title,handle) values ('zzz_default_planet_zzz','Default Planet','default');
update rag_group set planet_id='zzz_default_planet_zzz';


-- upgrade the way hierarchical objects are modeled

-- add new parentid column to weblogcategory table
    alter table weblogcategory add column parentid varchar(48) default null;
create index ws_parentid_idx on weblogcategory( parentid );

-- add new path column to weblogcategory table
    alter table weblogcategory add column path varchar(255) default null;
create index ws_path_idx on weblogcategory( path );

-- need to add this index for existing folder.parentid
create index fo_parentid_idx on folder( parentid );

-- add new path column to folder table
    alter table folder add column path varchar(255) default null;
create index fo_path_idx on folder( path );


-- update comment handling

-- add new fields to comment table to support CommentValidators
    alter table roller_comment add column referrer varchar(255) default null;
    alter table roller_comment add column useragent varchar(255) default null;

-- add new field to support comment plugins and content-type
    alter table roller_comment add column plugins varchar(255) default null;
    alter table roller_comment add column contenttype varchar(128) default 'text/plain' not null;  

-- add new status field to comment table to simplify queries
    alter table roller_comment add column status varchar(20) default 'APPROVED' not null;  

-- new status column needs an index
create index co_status_idx on roller_comment(status);

-- update existing data to use new status column
update roller_comment set status = 'APPROVED', posttime=posttime where approved=1;
update roller_comment set status = 'PENDING', posttime=posttime where pending=1;
update roller_comment set status = 'SPAM', posttime=posttime where spam=1;

update roller_comment set status = 'DISAPPROVED', posttime=posttime 
   where approved=0 and spam=0 and pending=0;


-- better support for doing scheduled entries

-- add new status option 'SCHEDULED' for future published entries
update weblogentry set status = 'SCHEDULED', pubtime=pubtime, updatetime=updatetime where pubtime > current_timestamp;

-- add new client column to roller_tasklock table
    alter table roller_tasklock add column client varchar(255) default null;

-- new column to support account activation by email
    alter table rolleruser add column activationcode varchar(48) default null;

-- new column to support screen name and populate with user names
    alter table rolleruser add column screenname varchar(255) default 'unspecified' not null;  
update rolleruser set screenname = username;

-- new column to allow setting of path to icon for website
    alter table website add column icon varchar(255) default null;

-- new column to allow setting of short website about text
    alter table website add column about varchar(255) default null;

-- new column to allow setting of page template content-type
    alter table webpage add column outputtype varchar(48) default null;

-- add new action column to webpage table, default value is custom
    alter table webpage add column action varchar(16) default 'custom' not null;  
update webpage set action = 'weblog' where name = 'Weblog';

-- add new custom stylesheet column to website table
    alter table website add column customstylesheet varchar(128) default null;

-- fix blogs which have unchecked showalllangs but did not check enablemultilang
update website set enablemultilang=1, datecreated=datecreated where showalllangs=0;


-- some missing foreign key constraints
alter table roller_user_permissions add constraint up_userid_fk
    foreign key ( user_id ) references rolleruser( id )  ;

alter table roller_user_permissions add constraint up_websiteid_fk
    foreign key ( website_id ) references website( id )  ;


-- some various indexes to improve performance
create index rhc_dailyhits_idx on roller_hitcounts( dailyhits );
create index we_combo1_idx on weblogentry(status, pubtime, websiteid);
create index we_combo2_idx on weblogentry(websiteid, pubtime, status);
create index co_combo1_idx on roller_comment(status, posttime);


-- remove old indexes that are no longer of value
drop index we_pubentry_idx on weblogentry;


-- fix wacky indexs which ended up with a size constraint
drop index rage_sid_idx on rag_entry;
create index rage_sid_idx on rag_entry(subscription_id);

drop index raggs_gid_idx on rag_group_subscription;
create index raggs_gid_idx on rag_group_subscription(group_id);

drop index raggs_sid_idx on rag_group_subscription;
create index raggs_sid_idx on rag_group_subscription(subscription_id);


-- remove no-longer-used needed tables

-- remove old rollerconfig table which has been deprecated since 1.2
-- NOTE: since this breaks the pre-1.2 -> 4.0+ direct upgrade path then
-- maybe we want to attempt to fix that by doing that upgrade via sql?
drop table if exists rollerconfig;

-- remove old id column of group subscription table
-- alter table rag_group_subscription drop column id;

-- remove old approved, spam, pending columns from comment table
alter table roller_comment drop column approved;
alter table roller_comment drop column spam;
alter table roller_comment drop column pending;

-- remove bastard columns and indexes (optional)
-- drop index index_we_pubtime_idx on weblogentry;
-- drop index co_pending_idx on roller_comment;
-- drop index co_approved_idx on roller_comment;
-- alter table website drop column userid;
-- alter table website drop column weblogdayid;
-- alter table weblogentry drop column publishentry;
-- alter table weblogentry drop column link;
-- drop table if exists usercookie;
-- drop table if exists rag_config;
-- drop table if exists folderassoc;
-- drop table if exists weblogcategoryassoc;

