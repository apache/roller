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

-- Eliminate use of reserved keywords

-- role -> userrole
CREATE TABLE temprole AS SELECT * FROM role;
DROP TABLE role;
CREATE table userrole (
    id               varchar(48) not null primary key,
    rolename         varchar(255) not null,
    username         varchar(255) not null,
    userid           varchar(48) not null
);
INSERT INTO userrole 
    (id, rolename, username, userid) 
    SELECT id, role, username, userid 
        FROM temprole;
ALTER table userrole add constraint userrole_userid_fk 
    foreign key ( userid ) references rolleruser( id );
create index userrole_userid_index on userrole( userid );


-- password -> passphrase
CREATE TABLE tempuser AS SELECT * FROM rolleruser;
DROP TABLE rolleruser;  
CREATE table rolleruser ( 
    id              varchar(48) not null primary key,
    username        varchar(255) not null,
    passphrase      varchar(255) not null,
    fullname        varchar(255) not null,
    emailaddress    varchar(255) not null,
    datecreated     timestamp not null,
    userenabled     bit default 1 not null
);
-- Populate the new table, filling datecreated using least pubtime of any of the user's weblog entries.
INSERT INTO rolleruser
  (id, username, passphrase, fullname, emailaddress, datecreated)
  SELECT t.id, t.username, t.password, t.fullname, t.emailaddress, MIN(e.pubtime)
    FROM tempuser t, website w, weblogentry e
    WHERE t.id = w.userid and w.id = e.websiteid
    GROUP BY t.id;

ALTER table rolleruser add constraint rolleruser_username_uq unique ( username );


-- page -> webpage
CREATE TABLE temppage AS SELECT * FROM page;
DROP TABLE page;  
create table webpage AS SELECT * FROM temppage;


-- Drop bad indexes: some were poorly named, others just wrong
-- these may or may not exist
-- alter table website drop index userid_index;
-- alter table folder drop index webisteid_index;
-- alter table folder drop index parentid_index;
-- alter table bookmark drop index folderid_index;
-- alter table weblogcategory drop index websiteid_index;
-- alter table weblogentry drop index websiteid_index;
-- alter table weblogentry drop index categoryid_index;
-- alter table newsfeed drop index websiteid_index;
-- alter table comment drop index entryid_index;
-- alter table referer drop index websiteid_index;
-- alter table referer drop index entryid_index;

-- Add good indexes to replace the bad ones
create index website_userid_index on website( userid );
create index folder_websiteid_index on folder( websiteid );
create index folder_parentid_index on folder( parentid );
create index bookmark_folderid_index on bookmark( folderid );
create index weblogcategory_websiteid_index on weblogcategory( websiteid );
create index weblogentry_websiteid_index on weblogentry( websiteid );
create index weblogentry_categoryid_index on weblogentry( categoryid );
create index newsfeed_websiteid_index on newsfeed( websiteid );
create index comment_entryid_index on comment( entryid );
create index referer_websiteid_index on referer( websiteid );
create index referer_entryid_index on referer( entryid );

-- Only use these when you are certain of the upgrade
-- drop table temprole;
-- drop table tempuser;
-- drop table temppage;
