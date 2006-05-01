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
create index rag_group_handle on rag_group(handle@INDEXSIZE@); 

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
create index rag_subscription_feed_url on rag_subscription(feed_url@INDEXSIZE@); 

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



