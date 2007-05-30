-- Licensed to the Apache Software Foundation (ASF) under one or more
--  contributor license agreements.  The ASF licenses this file to You
-- under the Apache License, Version 2.0 (the "License"); you may not
-- use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.  For additional information regarding
-- copyright in this work, please see the NOTICE file in the top level
-- directory of this distribution.

-- add new attribute to Comment
alter table comment add column notify boolean;
alter table comment add column spam boolean;
alter table comment add column remotehost varchar(128);
update comment set spam=false, notify=false, posttime=posttime;

-- add new attribute to WeblogEntry 
alter table weblogentry add column link varchar(255);
alter table weblogentry add column plugins varchar(255);
alter table weblogentry add column allowcomments boolean;
alter table weblogentry add column commentdays integer;
alter table weblogentry add column rightToLeft boolean;
alter table weblogentry add column pinnedtomain boolean;
update weblogentry set pubtime=pubtime, updatetime=updatetime, allowcomments=true, pinnedtomain=false;

-- add new attributes to Website
alter table website add column editortheme varchar(255);
alter table website add column locale varchar(20);
alter table website add column timezone varchar(50);
alter table website add column defaultcatid varchar(48);
alter table website add column defaultplugins varchar(255);
alter table website add column emailcomments boolean;
alter table website add column emailfromaddress varchar(255);
alter table website add column isenabled boolean;
update website set emailcomments=false, isenabled=true, locale='en', timezone='America/New_York';

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
    encryptpasswords boolean default false not null,
    algorithm       varchar(10) null,
    newuserallowed  boolean default false not null,
    editorpages     varchar(255) null,
    userthemes      varchar(255) not null,
    indexdir        varchar(255) null,
    memdebug           boolean default false not null,
    autoformatcomments boolean default false not null,
    escapecommenthtml boolean default true not null,
    emailcomments     boolean default false not null,
    enableaggregator  boolean default false not null,
    enablelinkback    boolean default false not null,
    rsscachetime    integer default 3000 not null,
    rssusecache     boolean default true not null,
    uploadallow     varchar(255) null,
    uploadforbid    varchar(255) null,
    uploadenabled   boolean default true not null,
    uploaddir       varchar(255) not null,
    uploadpath      varchar(255) not null,
    uploadmaxdirmb  decimal(5,2) default 4.0 not null,
    uploadmaxfilemb decimal(5,2) default 1.5 not null,
    dbversion       varchar(10) null
);
alter table rollerconfig add column refspamwords text;
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

create index referer_refurl_index on referer( refurl );
create index referer_requrl_index on referer( requrl );
create index referer_datestr_index on referer( datestr );
create index referer_refpermalink_index on referer( refpermalink );
create index referer_duplicate_index on referer( duplicate );

create index webpage_name_index on webpage( name );
create index webpage_link_index on webpage( link );

create index website_isenabled_index on website( isenabled );


