-- add new attribute to Comment
alter table comment add column notify @BOOLEAN_SQL_TYPE@;
alter table comment add column spam @BOOLEAN_SQL_TYPE@;
alter table comment add column remotehost varchar(128);
update comment set spam=@BOOLEAN_FALSE@, notify=@BOOLEAN_FALSE@, posttime=posttime;

-- add new attribute to WeblogEntry 
alter table weblogentry add column link varchar(255);
alter table weblogentry add column plugins varchar(255);
alter table weblogentry add column allowcomments @BOOLEAN_SQL_TYPE@;
alter table weblogentry add column commentdays integer;
alter table weblogentry add column rightToLeft @BOOLEAN_SQL_TYPE@;
alter table weblogentry add column pinnedtomain @BOOLEAN_SQL_TYPE@;
update weblogentry set pubtime=pubtime, updatetime=updatetime, allowcomments=@BOOLEAN_TRUE@, pinnedtomain=@BOOLEAN_FALSE@;

-- add new attributes to Website
alter table website add column editortheme varchar(255);
alter table website add column locale varchar(20);
alter table website add column timezone varchar(50);
alter table website add column defaultcatid varchar(48);
alter table website add column defaultplugins varchar(255);
alter table website add column emailcomments @BOOLEAN_SQL_TYPE@;
alter table website add column emailfromaddress varchar(255);
alter table website add column isenabled @BOOLEAN_SQL_TYPE@;
update website set emailcomments=@BOOLEAN_FALSE@, isenabled=@BOOLEAN_TRUE@, locale='en', timezone='America/New_York';

-- reset possibly bad bloggercategoryid settings, repairIfNeeded will fix them
update website set bloggercatid=null;

-- weblog categories are now hierarchical
create table weblogcategoryassoc (
    id               varchar(48) not null primary key,
    categoryid       varchar(48) not null,
    ancestorid       varchar(40),
    relation         varchar(20) not null
);
create index weblogcategoryassoc_categoryid_index on weblogcategoryassoc( categoryid );
create index weblogcategoryassoc_ancestorid_index on weblogcategoryassoc( ancestorid );
create index weblogcategoryassoc_relation_index on weblogcategoryassoc( relation );

create table folderassoc (
    id               varchar(48) not null primary key,
    folderid         varchar(48) not null,
    ancestorid       varchar(40),
    relation         varchar(20) not null
);
create index folderassoc_folderid_index on folderassoc( folderid );
create index folderassoc_ancestorid_index on folderassoc( ancestorid );

-- Configuration options for Roller, should only ever be one row
create table rollerconfig (
    id              varchar(48) not null primary key,
    sitedescription varchar(255) null,
    sitename        varchar(255) null,
    emailaddress    varchar(255) null,
    absoluteurl     varchar(255) null,
    adminusers      varchar(255) null,
    encryptpasswords @BOOLEAN_SQL_TYPE_FALSE@ not null,
    algorithm       varchar(10) null,
    newuserallowed  @BOOLEAN_SQL_TYPE_FALSE@ not null,
    editorpages     varchar(255) null,
    userthemes      varchar(255) not null,
    indexdir        varchar(255) null,
    memdebug           @BOOLEAN_SQL_TYPE_FALSE@ not null,
    autoformatcomments @BOOLEAN_SQL_TYPE_FALSE@ not null,
    escapecommenthtml @BOOLEAN_SQL_TYPE_TRUE@ not null,
    emailcomments     @BOOLEAN_SQL_TYPE_FALSE@ not null,
    enableaggregator  @BOOLEAN_SQL_TYPE_FALSE@ not null,
    enablelinkback    @BOOLEAN_SQL_TYPE_FALSE@ not null,
    rsscachetime    integer default 3000 not null,
    rssusecache     @BOOLEAN_SQL_TYPE_TRUE@ not null,
    uploadallow     varchar(255) null,
    uploadforbid    varchar(255) null,
    uploadenabled   @BOOLEAN_SQL_TYPE_TRUE@ not null,
    uploaddir       varchar(255) not null,
    uploadpath      varchar(255) not null,
    uploadmaxdirmb  decimal(5,2) default 4.0 not null,
    uploadmaxfilemb decimal(5,2) default 1.5 not null,
    dbversion       varchar(10) null
);
alter table rollerconfig add column refspamwords @TEXT_SQL_TYPE@;
update rollerconfig set refspamwords='';
 
-- new usercookie table for remember me feature
create table usercookie (
    id              varchar(48) not null primary key,
    username        varchar(255) not null,
    cookieid        varchar(100) not null,
    datecreated     timestamp not null
);
create index usercookie_username_index on usercookie( username );
create index usercookie_cookieid_index on usercookie( cookieid );

create index rolleruser_userenabled_index on rolleruser( userenabled );

create index referer_refurl_index on referer( refurl@INDEXSIZE@ );
create index referer_requrl_index on referer( requrl@INDEXSIZE@ );
create index referer_datestr_index on referer( datestr );
create index referer_refpermalink_index on referer( refpermalink@INDEXSIZE@ );
create index referer_duplicate_index on referer( duplicate );

create index webpage_name_index on webpage( name@INDEXSIZE@ );
create index webpage_link_index on webpage( link@INDEXSIZE@ );

create index website_isenabled_index on website( isenabled );


