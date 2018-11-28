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


create table if not exists comment (
    id      varchar(255) not null primary key,
    entryid varchar(255) not null,
    name    varchar(255) null,
    email   varchar(255) null,
    url     varchar(255) null,
    content longvarchar null,
    posttime timestamp   not null
);
create index comment_entryid_index on comment( entryid );


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
    excerpt   longvarchar null,
    dayhits   integer default 0 not null,
    totalhits integer default 0 not null,    
    visible   bit default 0 not null,
    duplicate bit default 0 not null
);
create index referer_websiteid_index on referer( websiteid );
create index referer_entryid_index on referer( entryid );

  
alter table website add column (allowcomments bit default 1 not null);

alter table website add column (ignorewords longvarchar null);

alter table comment add constraint comment_entryid_fk 
    foreign key ( entryid ) references weblogentry( id );

alter table folder add constraint folder_websiteid_fk 
    foreign key ( websiteid ) references website( id );
    
alter table folder add constraint folder_entryid_fk 
    foreign key ( entryid ) references weblogentry( id );
    
alter table referer add constraint referer_websiteid_fk 
    foreign key ( websiteid ) references website( id );
    
alter table referer add constraint referer_entryid_fk 
    foreign key ( entryid ) references weblogentry( id );

  