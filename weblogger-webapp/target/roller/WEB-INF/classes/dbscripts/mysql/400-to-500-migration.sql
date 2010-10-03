


-- NEW PERMISSIONS SYSTEM

-- We are replacing roller_user_permissions with this new table
-- actions:    comma separated list of actions permitted by permission
-- objectid:   for now this will always store weblogid
-- objectType: for now this will always be 'Weblog'
create table roller_permission (
   id              varchar(48) not null primary key,
   username        varchar(255) not null,
   actions         varchar(255), 
   objectid        varchar(48),           
   objecttype      varchar(255),          
   pending         tinyint(1) default 1,         
   datecreated     timestamp not null
);

insert into roller_permission (id,username,actions,objectid,objecttype,pending,datecreated) 
   select   concat(w.id, u.username)  , u.username, 'edit_draft', w.handle, 'Weblog', 0, current_timestamp
      from rolleruser as u, website as w, roller_user_permissions as p
      where p.user_id = u.id and p.website_id = w.id and permission_mask = 1;

insert into roller_permission (id,username,actions,objectid,objecttype,pending,datecreated) 
   select   concat(w.id, u.username)  , u.username, 'author', w.handle, 'Weblog', 0, current_timestamp
      from rolleruser as u, website as w, roller_user_permissions as p
      where p.user_id = u.id and p.website_id = w.id and permission_mask = 2;

insert into roller_permission (id,username,actions,objectid,objecttype,pending,datecreated) 
   select   concat(w.id, u.username)  , u.username, 'admin', w.handle, 'Weblog', 0, current_timestamp
      from rolleruser as u, website as w, roller_user_permissions as p
      where p.user_id = u.id and p.website_id = w.id and permission_mask = 3;

-- User management can now be exernalized, so no more relations with user table

    alter table userrole modify userid varchar(48) null;

    alter table website modify userid varchar(48) null;

    alter table website add column creator varchar(255) default null;
update website as w set 
    lastmodified = lastmodified,
    datecreated = datecreated,
    creator = (select u.username from rolleruser as u where u.id = w.userid);

    alter table weblogentry modify userid varchar(48) null;
    alter table weblogentry add column creator varchar(255) default null;
update weblogentry as w set
    pubtime = pubtime,
    updatetime = updatetime, 
    creator = (select u.username from rolleruser as u where u.id = w.userid);

    alter table roller_weblogentrytag modify userid varchar(48) null;
    alter table roller_weblogentrytag add column creator varchar(255) default null;
update roller_weblogentrytag as w set 
    time = time,
    creator = (select u.username from rolleruser as u where u.id = w.userid);


-- USER ATTRIBUTE

create table  roller_userattribute(
    id        varchar(48) not null primary key,
    username  varchar(255) not null,
    attrname  varchar(255) not null,
    attrvalue varchar(255) not null
);    
create index ua_username_idx  on roller_userattribute( username(40) );
create index ua_attrname_idx  on roller_userattribute( attrname(40) );
create index ua_attrvalue_idx on roller_userattribute( attrvalue(40) );


-- OAUTH SUPPORT

-- each record is an OAuth consumer key and secret, can be tied to just one user
create table roller_oauthconsumer (
    consumerkey    varchar(48) not null primary key,
    consumersecret varchar(48) not null,
    username       varchar(48)
);
create index oc_username_idx  on roller_oauthconsumer( username(40) );
create index oc_consumerkey_idx  on roller_oauthconsumer( consumerkey(40) );

-- each record is an OAuth accessor, always tied to just one user
create table roller_oauthaccessor (
    consumerkey  varchar(48) not null primary key,
    requesttoken varchar(48),
    accesstoken  varchar(48),
    tokensecret  varchar(48),
    created      datetime not null,
    updated      datetime not null,
    username     varchar(48),
    authorized   tinyint(1) default 0
);


-- MEDIA BLOGGING

create table roller_mediafile (
    id              varchar(48) not null primary key,
    name            varchar(255) not null,
    description     varchar(255),
    origpath        varchar(255),
    content_type    varchar(50)  not null,
    copyright_text  varchar(1023),
    directoryid     varchar(48) not null,
    weblogid        varchar(48) not null,
    size_in_bytes   integer,
    width           integer,
    height          integer,
    date_uploaded   datetime not null,
    last_updated    datetime,
    anchor          varchar(255),
    creator         varchar(255),
    is_public       tinyint(1) default 0 not null
);

create table roller_mediafiletag (
    id              varchar(48) not null primary key,
    mediafile_id    varchar(48) not null,
    name            varchar(30) not null
);

create table roller_mediafiledir (
    id               varchar(48) not null primary key,
    name             varchar(255) not null,
    description      varchar(255),
    websiteid        varchar(48) not null,
    parentid         varchar(48),
    path             varchar(255)
);

-- media files
alter table roller_mediafile add constraint roller_mediafiledir_id_fk
    foreign key (directoryid) references roller_mediafiledir(id)  ;

alter table roller_mediafiletag add constraint roller_mediafile_id_tag_fk
    foreign key (mediafile_id) references roller_mediafile(id)  ;

alter table roller_mediafiledir add constraint mf_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

alter table roller_mediafiledir add constraint mf_parentid_fk
    foreign key ( parentid ) references roller_mediafiledir( id )   ;


-- Fix for https://issues.apache.org/jira/browse/ROL-1760

