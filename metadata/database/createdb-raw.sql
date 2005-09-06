
-- Run this script to create the Roller database tables in your database.
-- Make sure you run the correct version of this script.
--
-- * For MySQL run the script createdb.sql found in the mysql directory.
-- * For PostgreSQL run the script createdb.sql found in the postgresql directory.
-- * For HSQLDB run the script createdb.sql found in the hsqldb directory.
-- 
-- For those who grabbed Roller source from CVS, don't try to run the script 
-- named createdb-raw.sql, it is the source from which the above scripts are 
-- created.

-- *****************************************************
-- Create the tables and indices

create table rolleruser (
    id              varchar(48) not null primary key,
    username        varchar(255) not null,
    passphrase      varchar(255) not null,
    fullname        varchar(255) not null,
    emailaddress    varchar(255) not null,
    datecreated     timestamp not null,
    locale          varchar(20),  
    timezone        varchar(50),    
    isenabled       @BOOLEAN_SQL_TYPE_TRUE@ not null
);
alter table rolleruser add constraint rolleruser_username_uq unique ( username@INDEXSIZE@ );

create table userrole (
    id               varchar(48) not null primary key,
    rolename         varchar(255) not null,
    username         varchar(255) not null,
    userid           varchar(48) not null
);
create index userrole_userid_index on userrole( userid );
create index userrole_username_index on userrole( username@INDEXSIZE@ );

-- User permissions within a website
-- permission_mask: bitmask 001 limited, 011 author, 100 admin
-- pending: pending user acceptance of invitation to join website
create table roller_user_permissions (
    id              varchar(48) not null primary key,
    website_id      varchar(48) not null,
    user_id         varchar(48) not null,
    permission_mask integer not null, 
    pending         @BOOLEAN_SQL_TYPE_TRUE@ not null
);

-- Audit log records time and comment about change
-- user_id: user that made change
-- object_id: id of associated object, if any
-- object_class: name of associated object class (e.g. WeblogEntryData)
-- comment: description of change
-- change_time: time that change was made
create table roller_audit_log (
    id              varchar(48) not null primary key,
    user_id         varchar(48) not null,  
    object_id       varchar(48),           
    object_class    varchar(255),          
    comment         varchar(255) not null, 
    change_time     timestamp              
);

create table usercookie (
    id              varchar(48) not null primary key,
    username        varchar(255) not null,
    cookieid        varchar(100) not null,
    datecreated     timestamp not null
);
create index usercookie_username_index on usercookie( username@INDEXSIZE@ );
create index usercookie_cookieid_index on usercookie( cookieid@INDEXSIZE@ );

create table webpage (
    id              varchar(48)  not null primary key,
    name            varchar(255)  not null,
    description     varchar(255),
    link            varchar(255),
    websiteid       varchar(48)  not null,
    template        @TEXT_SQL_TYPE@ not null,
    updatetime      timestamp     not null
);
create index webpage_name_index on webpage( name@INDEXSIZE@ );
create index webpage_link_index on webpage( link@INDEXSIZE@ );
create index webpage_id_index on webpage( websiteid );

create table website (
    id                varchar(48) not null primary key,
    name              varchar(255) not null,
    handle            varchar(255) not null,
    description       varchar(255) not null,
    userid            varchar(48) not null,
    defaultpageid     varchar(48) default '' not null,
    weblogdayid       varchar(48) not null,
    ignorewords       @TEXT_SQL_TYPE@,
    enablebloggerapi  @BOOLEAN_SQL_TYPE_FALSE@ not null,
    editorpage        varchar(255),
    bloggercatid      varchar(48),
    defaultcatid      varchar(48),
    allowcomments     @BOOLEAN_SQL_TYPE_TRUE@ not null,
    emailcomments     @BOOLEAN_SQL_TYPE_FALSE@ not null,
    emailfromaddress  varchar(255),
    emailaddress      varchar(255) not null,
    editortheme       varchar(255),
    locale            varchar(20), 
    timezone          varchar(50),  
    defaultplugins    varchar(255),
    pinnedtomain      @BOOLEAN_SQL_TYPE_FALSE@ not null,
    isenabled         @BOOLEAN_SQL_TYPE_TRUE@ not null,
    datecreated     timestamp not null
);
create index website_userid_index    on website(userid);
create index website_isenabled_index on website(isenabled);
create index website_handle_index    on website(handle);
alter table website add constraint website_handle_uq unique (handle@INDEXSIZE@);

create table folder (
    id               varchar(48) not null primary key,
    name             varchar(255) not null,
    description      varchar(255),
    websiteid        varchar(48) not null,
    parentid        varchar(48)
);
create index folder_websiteid_index on folder( websiteid );

create table folderassoc (
    id               varchar(48) not null primary key,
    folderid         varchar(48) not null,
    ancestorid       varchar(40),
    relation         varchar(20) not null
);
create index folderassoc_folderid_index on folderassoc( folderid );
create index folderassoc_ancestorid_index on folderassoc( ancestorid );
create index folderassoc_relation_index on folderassoc( relation );

create table bookmark (
    id               varchar(48) not null primary key,
    folderid         varchar(48) not null,
    name             varchar(255) not null,
    description      varchar(255),
    url              varchar(255) not null,
    weight           integer default 0 not null,
    priority         integer default 100 not null,
    image            varchar(255),
    feedurl          varchar(255)
);
create index bookmark_folderid_index on bookmark( folderid );

create table weblogcategory (
    id               varchar(48)  not null primary key,
    name             varchar(255) not null,
    description      varchar(255),
    websiteid        varchar(48)  not null,
    image            varchar(255)
);
create index weblogcategory_websiteid_index on weblogcategory( websiteid );
-- alter table weblogcategory add unique category_nameparentid_uq (parentid, name(20));

create table weblogcategoryassoc (
    id               varchar(48) not null primary key,
    categoryid       varchar(48) not null,
    ancestorid       varchar(40),
    relation         varchar(20) not null
);
create index weblogcategoryassoc_categoryid_index on weblogcategoryassoc( categoryid );
create index weblogcategoryassoc_ancestorid_index on weblogcategoryassoc( ancestorid );
create index weblogcategoryassoc_relation_index on weblogcategoryassoc( relation );

create table weblogentry (
    id              varchar(48)  not null primary key,
    userid          varchar(48) not null,
    anchor          varchar(255)  not null,
    title           varchar(255)  not null,
    text            @TEXT_SQL_TYPE@ not null,
    pubtime         timestamp     not null,
    updatetime      timestamp     not null,
    websiteid       varchar(48)  not null,
    categoryid      varchar(48)  not null,
    publishentry    @BOOLEAN_SQL_TYPE_TRUE@ not null,
    link            varchar(255),
    plugins         varchar(255),
    allowcomments   @BOOLEAN_SQL_TYPE_FALSE@ not null, 
    commentdays     integer default 7 not null,
    rightToLeft     @BOOLEAN_SQL_TYPE_FALSE@ not null,
    pinnedtomain    @BOOLEAN_SQL_TYPE_FALSE@ not null,
    locale          varchar(20),
    status          varchar(20) not null
);
create index weblogentry_websiteid_index on weblogentry( websiteid );
create index weblogentry_categoryid_index on weblogentry( categoryid );
create index weblogentry_pubtime_index on weblogentry( pubtime,publishentry,websiteid );
create index weblogentry_pinnedtomain_index on weblogentry(pinnedtomain);
create index weblogentry_publishentry_index on weblogentry(publishentry);
create index weblogentry_userid_index on weblogentry(userid);

create table newsfeed (
    id              varchar(48) not null primary key,
    name            varchar(255) not null,
    description     varchar(255) not null,
    link            varchar(255) not null,
    websiteid       varchar(48) not null
);
create index newsfeed_websiteid_index on newsfeed( websiteid );


create table comment (
    id      varchar(48) not null primary key,
    entryid varchar(48) not null,
    name    varchar(255),
    email   varchar(255),
    url     varchar(255),
    content @TEXT_SQL_TYPE@,
    posttime timestamp   not null,
    spam    @BOOLEAN_SQL_TYPE_FALSE@ not null,
    notify  @BOOLEAN_SQL_TYPE_FALSE@ not null,
    remotehost varchar(128)
);
create index comment_entryid_index on comment( entryid );

-- Ping Feature Tables
-- name: short descriptive name of the ping target
-- pingurl: URL to receive the ping
-- websiteid:  if not null, this is a custom target defined by the associated website
-- conditioncode:
-- lastsuccess:
create table pingtarget (
    id           varchar(48) not null primary key,
    name         varchar(255) not null,
    pingurl      varchar(255) not null,
    websiteid    varchar(48),
    conditioncode    integer default 0 not null,
    lastsuccess  timestamp
);
create index pingtarget_websiteid_index on pingtarget( websiteid );

-- auto ping configurations
-- websiteid:  fk reference to website for which this auto ping configuration applies
-- pingtargetid: fk reference to the ping target to be pinged when the website changes
create table autoping (
    id            varchar(48) not null primary key,
    websiteid     varchar(48) not null,
    pingtargetid  varchar(48) not null 
);
create index autoping_websiteid_index on autoping( websiteid );
create index autoping_pingtargetid_index on autoping( pingtargetid );

-- autopingid: fk reference to ping configuration
-- categoryid: fk reference to category
create table pingcategory (
    id            varchar(48) not null primary key,
    autopingid  varchar(48) not null, 
    categoryid    varchar(48) not null 
);
create index pingcategory_autopingid_index on pingcategory( autopingid );
create index pingcategory_categoryid_index on pingcategory( categoryid );

-- entrytime: timestamp of original entry onto the ping queue
-- pingtargetid: weak fk reference to ping target (not constrained)
-- websiteid: weak fk reference to website originating the ping (not constrained)
-- attempts:  number of ping attempts that have been made for this entry
create table pingqueueentry (
    id             varchar(48) not null primary key,
    entrytime      timestamp not null, 
    pingtargetid   varchar(48) not null,  
    websiteid      varchar(48) not null,  
    attempts       integer not null
);
create index pingqueueentry_entrytime_index on pingqueueentry( entrytime );
create index pingqueueentry_pingtargetid_index on pingqueueentry( pingtargetid );
create index pingqueueentry_websiteid_index on pingqueueentry( websiteid );


-- Referer tracks URLs that refer to websites and entries
create table referer (
    id        varchar(48) not null primary key,
    websiteid varchar(48) not null,
    entryid   varchar(48),
    datestr   varchar(10),
    refurl    varchar(255) not null,
    refpermalink varchar(255),
    reftime   timestamp,
    requrl    varchar(255),
    title     varchar(255),
    excerpt   @TEXT_SQL_TYPE@,
    dayhits   integer default 0 not null,
    totalhits integer default 0 not null,
    visible   @BOOLEAN_SQL_TYPE_FALSE@ not null,
    duplicate @BOOLEAN_SQL_TYPE_FALSE@ not null
);
create index referer_websiteid_index on referer( websiteid );
create index referer_entryid_index on referer( entryid );
create index referer_refurl_index on referer( refurl@INDEXSIZE@ );
create index referer_requrl_index on referer( requrl@INDEXSIZE@ );
create index referer_datestr_index on referer( datestr );
create index referer_refpermalink_index on referer( refpermalink@INDEXSIZE@ );
create index referer_duplicate_index on referer( duplicate );

-- Configuration options for Roller, should only ever be one row
-- Deprecated in 1.2: configuration now stored in roller_properties table
create table rollerconfig (
    id              varchar(48) not null primary key,
    sitedescription varchar(255),
    sitename        varchar(255),
    emailaddress    varchar(255),
    absoluteurl     varchar(255),
    adminusers      varchar(255),
    encryptpasswords @BOOLEAN_SQL_TYPE_TRUE@ not null,
    algorithm       varchar(10),
    newuserallowed  @BOOLEAN_SQL_TYPE_FALSE@ not null,
    editorpages     varchar(255),
    userthemes      varchar(255) not null,
    indexdir        varchar(255),
    memdebug        @BOOLEAN_SQL_TYPE_FALSE@ not null,
    autoformatcomments @BOOLEAN_SQL_TYPE_FALSE@ not null,
    escapecommenthtml @BOOLEAN_SQL_TYPE_TRUE@ not null,
    emailcomments   @BOOLEAN_SQL_TYPE_FALSE@ not null,
    enableaggregator @BOOLEAN_SQL_TYPE_FALSE@ not null,
    enablelinkback  @BOOLEAN_SQL_TYPE_FALSE@ not null,
    rsscachetime    integer default 3000 not null,
    rssusecache     @BOOLEAN_SQL_TYPE_TRUE@ not null,
    uploadallow     varchar(255),
    uploadforbid    varchar(255),
    uploadenabled   @BOOLEAN_SQL_TYPE_TRUE@ not null,
    uploaddir       varchar(255) not null,
    uploadpath      varchar(255) not null,
    uploadmaxdirmb  decimal(5,2) default 4.0 not null,
    uploadmaxfilemb decimal(5,2) default 1.5 not null,
    dbversion       varchar(10),
    refspamwords    @TEXT_SQL_TYPE@
);

create table roller_properties (
    name     varchar(255) not null primary key,
    value    @TEXT_SQL_TYPE@
);

-- Entry attribute: metadata for weblog entries
create table entryattribute (
    id       varchar(48) not null primary key,
    entryid  varchar(48) not null,
    name     varchar(255) not null,
    value    @TEXT_SQL_TYPE@ not null
);
create index entryattribute_entryid_index on entryattribute( entryid );
alter table entryattribute add constraint entryattribute_name_uq unique ( entryid, name@INDEXSIZE@ );

create table rag_group_subscription (
    id               varchar(48) not null primary key,
    group_id         varchar(48) not null,
    subscription_id  varchar(48) not null
);
create index rag_group_subscription_gid on rag_group_subscription(group_id@INDEXSIZE@); 
create index rag_group_subscription_sid on rag_group_subscription(subscription_id@INDEXSIZE@); 

create table rag_config (
    id               varchar(48) not null primary key,
    default_group_id varchar(48),
    title            varchar(255) not null,
    description      varchar(255),
    site_url         varchar(255),
    output_dir       varchar(255),
    cache_dir        varchar(255) not null,
    template_dir     varchar(255),
    main_page        varchar(255),
    admin_name       varchar(255),
    admin_email      varchar(255) not null,
    group_page       varchar(255),
    proxy_host       varchar(255),
    proxy_port       integer default -1
);

create table rag_group (
    id               varchar(48) not null primary key,
    handle           varchar(255) not null,
    title            varchar(255) not null,
    description      varchar(255),
    cat_restriction  @TEXT_SQL_TYPE@,
    group_page       varchar(255),
    max_page_entries integer default 30,
    max_feed_entries integer default 30
);
alter table rag_group add constraint rag_group_handle_uq unique ( handle@INDEXSIZE@ );

create table rag_subscription (
    id               varchar(48) not null primary key,
    title            varchar(255),
    feed_url         varchar(255) not null,
    site_url         varchar(255),
    author           varchar(255),
    last_updated     timestamp,
    inbound_links    integer default -1,
    inbound_blogs    integer default -1
);
alter table rag_subscription add constraint rag_feed_url_uq unique ( feed_url@INDEXSIZE_LARGE@ );

create table rag_entry (
    id               varchar(48) not null primary key,
    subscription_id  varchar(48) not null,
    handle           varchar(255),
    title            varchar(255),
    guid             varchar(255),
    permalink        @TEXT_SQL_TYPE@ not null,
    author           varchar(255),
    content          @TEXT_SQL_TYPE@,
    categories       @TEXT_SQL_TYPE@,
    published        timestamp not null,
    updated          timestamp    
);
create index rag_entry_sid on rag_entry(subscription_id@INDEXSIZE@); 

-- *****************************************************
-- Now add the foreign key relationships

-- user, role and website

alter table website add constraint website_userid_fk
    foreign key ( userid ) references rolleruser ( id ) @ADDL_FK_PARAMS@ ;

alter table userrole add constraint userrole_userid_fk
    foreign key ( userid ) references rolleruser( id ) @ADDL_FK_PARAMS@ ;

-- page, entry, category, comment

alter table webpage add constraint weblogpage_websiteid_fk
    foreign key ( websiteid ) references website( id ) @ADDL_FK_PARAMS@ ;

alter table weblogentry add constraint weblogentry_websiteid_fk
    foreign key ( websiteid ) references website( id ) @ADDL_FK_PARAMS@ ;

alter table weblogentry add constraint weblogentry_categoryid_fk
    foreign key ( categoryid ) references weblogcategory( id ) @ADDL_FK_PARAMS@ ;

alter table weblogcategory add constraint weblogcategory_websiteid_fk
    foreign key ( websiteid ) references website( id ) @ADDL_FK_PARAMS@ ;

alter table comment add constraint comment_entryid_fk
    foreign key ( entryid ) references weblogentry( id ) @ADDL_FK_PARAMS@ ;

alter table entryattribute add constraint att_entryid_fk
    foreign key ( entryid ) references weblogentry( id ) @ADDL_FK_PARAMS@ ;

-- referer

alter table referer add constraint referer_entryid_fk
    foreign key ( entryid ) references weblogentry( id ) @ADDL_FK_PARAMS@ ;

alter table referer add constraint referer_websiteid_fk
    foreign key ( websiteid ) references website( id ) @ADDL_FK_PARAMS@ ;

-- folder and bookmark

alter table folder add constraint folder_websiteid_fk
    foreign key ( websiteid ) references website( id ) @ADDL_FK_PARAMS@ ;

-- alter table folder add constraint folder_parentid_fk
--     foreign key ( parentid ) references folder( id );

alter table bookmark add constraint bookmark_folderid_fk
    foreign key ( folderid ) references folder( id ) @ADDL_FK_PARAMS@ ;

-- newsfeed

alter table newsfeed add constraint newsfeed_websiteid_fk
    foreign key ( websiteid ) references website( id ) @ADDL_FK_PARAMS@ ;

-- pingtarget, autoping, pingcategory

alter table pingtarget add constraint pingtarget_websiteid_fk
    foreign key (websiteid) references website(id) @ADDL_FK_PARAMS@ ;

alter table autoping add constraint autoping_websiteid_fk
    foreign key (websiteid) references website(id) @ADDL_FK_PARAMS@ ;

alter table autoping add constraint autoping_pingtargetid_fk
    foreign key (pingtargetid) references pingtarget(id) @ADDL_FK_PARAMS@ ;

alter table pingcategory add constraint pingcategory_autopingid_fk
    foreign key (autopingid) references autoping(id) @ADDL_FK_PARAMS@ ;

alter table pingcategory add constraint pingcategory_categoryid_fk
    foreign key (categoryid) references weblogcategory(id) @ADDL_FK_PARAMS@ ;


-- THE FOLLOWING CONSTRAINTS CAN NOT BE SUPPORTED FOR IMPORTING new-user.xml
-- alter table website add constraint website_defaultpageid_fk foreign key ( defaultpageid ) references webpage ( id );
-- alter table website add constraint website_weblogdayid_fk foreign key ( weblogdayid ) references webpage ( id );
-- alter table webpage add constraint webpage_websiteid_fk foreign key ( websiteid ) references website( id );














