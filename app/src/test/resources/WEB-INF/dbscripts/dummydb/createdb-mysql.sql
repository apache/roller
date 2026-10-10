-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  The ASF licenses this file to You
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


create table testrolleruser (
    id              varchar(48) not null primary key,
    username        varchar(255) not null,
    passphrase      varchar(255) not null,
    screenname      varchar(255) not null,
    fullname        varchar(255) not null,
    emailaddress    varchar(255) not null,
    activationcode	varchar(48),
    datecreated     datetime not null,
    locale          varchar(20),  
    timezone        varchar(50),    
    isenabled       tinyint(1) default 1 not null
);
alter table testrolleruser add constraint tru_username_uq unique ( username(40) );

create table testuserrole (
    id               varchar(48) not null primary key,
    rolename         varchar(255) not null,
    username         varchar(255) not null,
    userid           varchar(48) not null
);
create index tur_userid_idx on testuserrole( userid );
create index tur_username_idx on testuserrole( username(40) );

