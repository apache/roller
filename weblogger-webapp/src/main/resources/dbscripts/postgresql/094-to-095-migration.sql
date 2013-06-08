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

-- we will drop & recreate every table so
-- that the new named constraints can be used
-- for HSQLDB replace text datatypes with longvarchar
-- for Postresql replace bit datatype with boolean

CREATE TABLE tempuser AS SELECT * FROM user;    
DROP TABLE user;
create table rolleruser ( 
    id              varchar(255) not null primary key,
    username        varchar(255) not null,
    password        varchar(255) not null,
    fullname        varchar(255) not null,
    emailaddress    varchar(255) not null
);
INSERT INTO rolleruser SELECT * FROM tempuser;

CREATE TABLE temprole AS SELECT * FROM role;    
DROP TABLE role;
create table role (
    id               varchar(255) not null primary key,
    role             varchar(255) not null,
    username         varchar(255) not null
);
INSERT INTO role SELECT * FROM temprole;

CREATE TABLE temppage AS SELECT * FROM page;    
DROP TABLE page;
create table page (
    id              varchar(255)  not null primary key,
    name            varchar(255)  not null,
    description     varchar(255)  null,
    link            varchar(255)  null,
    websiteid       varchar(255)  not null,
    template        text not null,
    updatetime      timestamp     not null
);
INSERT INTO page 
    (id, name, description, websiteid, template, updatetime) 
    SELECT id, name, description, websiteid, template, updatetime FROM temppage;

CREATE TABLE tempwebsite AS SELECT * FROM website;    
DROP TABLE website;
create table website (
    id               varchar(255) not null primary key,
    name             varchar(255) not null,
    description      varchar(255) not null,
    userid           varchar(255) not null,
    defaultpageid    varchar(255) default 0 not null,
    weblogdayid      varchar(255) not null,
    enablebloggerapi boolean default false not null,
    bloggercatid     varchar(255) null
); 
INSERT INTO website SELECT * FROM tempwebsite;

CREATE TABLE tempfolder AS SELECT * FROM folder;    
DROP TABLE folder;
create table folder (
    id               varchar(255) not null primary key,
    name             varchar(255) not null,
    description      varchar(255) null,
    parentid         varchar(255) null,
    websiteid        varchar(255) not null
);
INSERT INTO folder SELECT id,name,description,parentid,websiteid 
    FROM tempfolder;
UPDATE folder SET parentid = NULL WHERE name='root' AND parentid=0;

CREATE TABLE tempbookmark AS SELECT * FROM bookmark;    
DROP TABLE bookmark;
create table bookmark (
    id               varchar(255) not null primary key,
    folderid         varchar(255) not null,
    name             varchar(255) not null,
    description      varchar(255) null,
    url              varchar(255) not null,
    priority         integer default 100 not null,
    image            varchar(255) null
);
INSERT INTO bookmark SELECT * FROM tempbookmark;
ALTER TABLE bookmark ADD COLUMN (weight integer default 0 not null);
ALTER TABLE bookmark ADD COLUMN (feedurl varchar(255) null);

CREATE TABLE tempweblogcategory AS SELECT * FROM weblogcategory;    
DROP TABLE weblogcategory;
create table weblogcategory (
    id               varchar(255) not null primary key,
    name             varchar(255) not null,
    description      varchar(255) null,
    websiteid        varchar(255) not null,
    image            varchar(255) null
);
INSERT INTO weblogcategory SELECT * FROM tempweblogcategory;

CREATE TABLE tempweblogentry AS SELECT * FROM weblogentry;    
DROP TABLE weblogentry;
create table weblogentry (
    id              varchar(255)  not null primary key,
    anchor          varchar(255)  not null,
    title           varchar(255)  not null,
    text            text not null,
    pubtime         timestamp     not null,
    updatetime      timestamp     not null,
    websiteid       varchar(255)  not null,
    categoryid      varchar(255)  not null
);
INSERT INTO weblogentry SELECT * FROM tempweblogentry;

CREATE TABLE tempnewsfeed AS SELECT * FROM newsfeed;    
DROP TABLE newsfeed;
create table newsfeed (
    id              varchar(255) not null primary key,
    name            varchar(255) not null,
    description     varchar(255) not null,
    link            varchar(255) not null,
    websiteid       varchar(255) not null
);
INSERT INTO newsfeed SELECT * FROM tempnewsfeed;

-- Now add the constraints
alter table rolleruser add constraint rolleruser_username_uq unique ( username );

alter table website add constraint website_userid_fk foreign key ( userid ) references rolleruser ( id );

alter table folder add constraint folder_websiteid_fk foreign key ( websiteid ) references website( id );
alter table folder add constraint folder_parentid_fk foreign key ( parentid ) references folder( id );

alter table bookmark add constraint bookmark_folderid_fk foreign key ( folderid ) references folder( id );

alter table weblogcategory add constraint weblogcategory_websiteid_fk foreign key ( websiteid ) references website( id );

alter table weblogentry add constraint weblogentry_websiteid_fk foreign key ( websiteid ) references website( id );
alter table weblogentry add constraint weblogentry_categoryid_fk foreign key ( categoryid ) references weblogcategory( id );

alter table newsfeed add constraint newsfeed_websiteid_fk foreign key ( websiteid ) references website( id );
