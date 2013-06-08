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

create table roller_properties (
    name     varchar(255) not null primary key,
    value    text
);
insert into roller_properties (name,value) values ('roller.database.version','120');

create table rag_group_subscription (
    id               varchar(48) not null primary key,
    group_id         varchar(48) not null,
    subscription_id  varchar(48) not null
);
create index rag_group_subscription_gid on rag_group_subscription(group_id); 
create index rag_group_subscription_sid on rag_group_subscription(subscription_id); 

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
    cat_restriction  text,
    group_page       varchar(255),
    max_page_entries integer default 30,
    max_feed_entries integer default 30
);
alter table rag_group add constraint rag_group_handle_uq unique ( handle );
create index rag_group_handle on rag_group(handle); 

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
alter table rag_subscription add constraint rag_feed_url_uq unique ( feed_url );
create index rag_subscription_feed_url on rag_subscription(feed_url); 

create table rag_entry (
    id               varchar(48) not null primary key,
    subscription_id  varchar(48) not null,
    handle           varchar(255),
    title            varchar(255),
    guid             varchar(255),
    permalink        text not null,
    author           varchar(255),
    content          text,
    categories       text,
    published        timestamp not null,
    updated          timestamp    
);
create index rag_entry_sid on rag_entry(subscription_id);


-- Ping Feature Tables

create table pingtarget (
    id           varchar(48) not null primary key,
    name         varchar(255) not null, -- short descriptive name of the ping target
    pingurl      varchar(255) not null,  -- URL to receive the ping
    websiteid    varchar(48) null, -- if not null, this is a custom target defined by the associated website
    condition    integer default 0 not null, -- condition code
    lastsuccess  timestamp null -- last successful use
);
create index pingtarget_websiteid_index on pingtarget( websiteid );

-- auto ping configurations
create table autoping (
    id            varchar(48) not null primary key,
    websiteid     varchar(48) not null, -- fk reference to website for which this auto ping configuration applies
    pingtargetid  varchar(48) not null -- fk reference to the ping target to be pinged when the website changes
);
create index autoping_websiteid_index on autoping( websiteid );
create index autoping_pingtargetid_index on autoping( pingtargetid );

create table pingcategory (
    id            varchar(48) not null primary key,
    autopingid  varchar(48) not null, -- fk reference to auto ping configuration
    categoryid    varchar(48) not null -- fk reference to category
);
create index pingcategory_autopingid_index on pingcategory( autopingid );
create index pingcategory_categoryid_index on pingcategory( categoryid );

create table pingqueueentry (
    id             varchar(48) not null primary key,
    entrytime      timestamp not null, -- timestamp of original entry onto the ping queue
    pingtargetid   varchar(48) not null,  -- weak fk reference to ping target (not constrained)
    websiteid      varchar(48) not null,  -- weak fk reference to website originating the ping (not constrained)
    attempts       integer not null -- number of ping attempts that have been made for this entry
);
create index pingqueueentry_entrytime_index on pingqueueentry( entrytime );
create index pingqueueentry_pingtargetid_index on pingqueueentry( pingtargetid );
create index pingqueueentry_websiteid_index on pingqueueentry( websiteid );

-- and Ping constraints

alter table pingtarget add constraint pingtarget_websiteid_fk
    foreign key (websiteid) references website(id);

alter table autoping add constraint autoping_websiteid_fk
    foreign key (websiteid) references website(id);

alter table autoping add constraint autoping_pingtargetid_fk
    foreign key (pingtargetid) references pingtarget(id);

alter table pingcategory add constraint pingcategory_autopingid_fk
    foreign key (autopingid) references autoping(id);

alter table pingcategory add constraint pingcategory_categoryid_fk
    foreign key (categoryid) references weblogcategory(id);

 
