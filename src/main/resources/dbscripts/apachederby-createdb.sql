
-- Below script creates the TightBlog tables for database apachederby

create table weblogger_user (
    id              varchar(48) not null primary key,
    username        varchar(48) not null,
    screenname      varchar(48) not null,
    emailaddress    varchar(255) not null,
    global_role     varchar(16) not null,
    status          varchar(20) not null,
    datecreated     timestamp not null,
    lastlogin       timestamp,
    activationcode	varchar(48),
    mfa_secret      varchar(96),
    encr_password   varchar(255)
);
alter table weblogger_user add constraint wu_username_uq unique (username);

alter table weblogger_user add constraint wu_screenname_uq unique (screenname);

create table weblog (
    id                varchar(48) not null primary key,
    name              varchar(255) not null,
    handle            varchar(255) not null,
    tagline           varchar(255),
    about             varchar(255),
    locale            varchar(20),
    timezone          varchar(50),
    visible           smallint default 1 not null,
    theme             varchar(255) not null,
    entriesperpage    integer default 15 not null,
    editformat        varchar(20) not null,
    creatorid         varchar(48) not null,
    datecreated       timestamp not null,
    lastmodified      timestamp not null,
    allowcomments     varchar(20) not null,
    commentdays       integer default 7 not null,
    analyticscode     clob(102400),
    blacklist         clob(102400),
    hitstoday	      integer default 0 not null
);
create index ws_visible_idx on weblog(visible);
alter table weblog add constraint wlog_handle_uq unique (handle);

alter table weblog add constraint wlog_creatorid_fk
    foreign key ( creatorid ) references weblogger_user( id ) ;

create table user_weblog_role (
   id              varchar(48) not null primary key,
   userid          varchar(48) not null,
   weblogid        varchar(48) not null,
   weblog_role     varchar(48) not null,
   email_comments  smallint default 1 not null
);

alter table user_weblog_role add constraint uwr_userid_fk
    foreign key ( userid ) references weblogger_user( id ) ;

alter table user_weblog_role add constraint uwr_weblogid_fk
    foreign key ( weblogid ) references weblog( id ) ;


create table weblog_template (
    id              varchar(48) not null primary key,
    weblogid        varchar(48) not null,
    role            varchar(20) not null,
    name            varchar(255) not null,
    description     varchar(255),
    template        clob(102400) not null,
    updatetime      timestamp not null
);
create index wt_name_idx on weblog_template(name);

alter table weblog_template add constraint wt_weblogid_fk
    foreign key ( weblogid ) references weblog( id ) ;

alter table weblog_template add constraint wt_name_uq unique (weblogid, name);


create table blogroll_link (
    id               varchar(48) not null primary key,
    weblogid         varchar(48) not null,
    name             varchar(128) not null,
    url              varchar(128) not null,
    description      varchar(128),
    position         integer not null
);

alter table blogroll_link add constraint bl_weblogid_fk
    foreign key ( weblogid ) references weblog( id ) ;


create table weblog_category (
    id               varchar(48) not null primary key,
    name             varchar(255) not null,
    weblogid         varchar(48) not null,
    position         integer not null
);

alter table weblog_category add constraint wc_name_uq unique (weblogid, name);

alter table weblog_category add constraint wc_weblogid_fk
    foreign key ( weblogid ) references weblog( id ) ;


create table weblog_entry (
    id              varchar(48)  not null primary key,
    anchor          varchar(255)  not null,
    creatorid       varchar(48) not null,
    title           varchar(255)  not null,
    text            clob(102400) not null,
    pubtime         timestamp,
    updatetime      timestamp not null,
    weblogid        varchar(48)  not null,
    categoryid      varchar(48)  not null,
    editformat      varchar(20) not null,
    commentdays     integer default 7 not null,
    status          varchar(20) not null,
    summary         clob(102400),
    notes           clob(102400),
    search_description varchar(255),
    enclosure_url   varchar(255),
    enclosure_type  varchar(48),
    enclosure_length integer
);

alter table weblog_entry add constraint we_weblogid_fk
    foreign key ( weblogid ) references weblog( id ) ;

alter table weblog_entry add constraint we_categoryid_fk
    foreign key ( categoryid ) references weblog_category( id ) ;

alter table weblog_entry add constraint we_creatorid_fk
    foreign key ( creatorid ) references weblogger_user( id ) ;

create index we_status_idx on weblog_entry(status);
create index we_combo1_idx on weblog_entry(status, pubtime, weblogid);
create index we_combo2_idx on weblog_entry(weblogid, pubtime, status);

-- weblogid available via entryid but duplicated for performance purposes
create table weblog_entry_tag (
    id              varchar(48)   not null primary key,
    weblogid        varchar(48)   not null,
    entryid         varchar(48)   not null,
    name            varchar(255)  not null
);

alter table weblog_entry_tag add constraint wtag_name_uq unique (weblogid, entryid, name);

alter table weblog_entry_tag add constraint wtag_entryid_fk
    foreign key ( entryid ) references weblog_entry( id ) ;

-- below index for single-blog tag clouds
create index wtag_tagsearch_idx on weblog_entry_tag( weblogid, name, entryid );

-- weblogid available via entryid but duplicated for performance purposes
create table weblog_entry_comment (
    id         varchar(48) not null primary key,
    weblogid   varchar(48) not null,
    entryid    varchar(48) not null,
    status     varchar(20) not null,
    bloggerid  varchar(48),
    name       varchar(255) not null,
    email      varchar(255) not null,
    notify     smallint default 0 not null,
    content    clob(102400),
    posttime   timestamp not null,
    url        varchar(255),
    remotehost varchar(128),
    referrer   varchar(255),
    useragent  varchar(255)
);

alter table weblog_entry_comment add constraint co_entryid_fk
    foreign key ( entryid ) references weblog_entry( id ) ;


alter table weblog_entry_comment add constraint co_userid_fk
    foreign key ( bloggerid ) references weblogger_user( id ) ;

create index co_status_idx on weblog_entry_comment( status );

-- for server-wide properties that can be adjusted (in the Admin UI) during runtime
create table weblogger_properties (
    id                     varchar(48) not null primary key,
    database_version       integer not null,
    main_blog_id           varchar(48),
    registration_policy    varchar(24) default 'DISABLED' not null,
    users_create_blogs     smallint default 1 not null,
    blog_html_policy       varchar(24) default 'RELAXED' not null,
    users_customize_themes smallint default 1 not null,
    default_analytics_code clob(102400),
    users_override_analytics_code smallint default 1 not null,
    comment_policy         varchar(24) default 'MUSTMODERATE' not null,
    comment_html_policy    varchar(24) default 'BASIC' not null,
    autodelete_spam        smallint default 0 not null,
    users_comment_notifications smallint default 1 not null,
    comment_spam_filter    clob(102400),
    max_file_uploads_size_mb integer default 20 not null
);

alter table weblogger_properties add constraint wp_weblogid_fk
    foreign key (main_blog_id) references weblog( id ) ;

-- initial row, relying on per-column defaults.
insert into weblogger_properties(id, database_version) values ('1', 200);

create table media_directory (
    id               varchar(48) not null primary key,
    weblogid         varchar(48) not null,
    name             varchar(255) not null
);

alter table media_directory add constraint md_weblogid_fk
    foreign key ( weblogid ) references weblog( id ) ;

create table media_file (
    id              varchar(48) not null primary key,
    directoryid     varchar(48) not null,
    name            varchar(255) not null,
    content_type    varchar(50) not null,
    alt_attr        varchar(255),
    title_attr      varchar(255),
    anchor          varchar(255),
    notes           varchar(255),
    width           integer,
    height          integer,
    size_in_bytes   integer,
    creatorid       varchar(48) not null,
    date_uploaded   timestamp not null,
    last_updated    timestamp not null
);

alter table media_file add constraint mf_directoryid_fk
    foreign key (directoryid) references media_directory(id) ;

alter table media_file add constraint mf_creatorid_fk
    foreign key (creatorid) references weblogger_user( id ) ;
