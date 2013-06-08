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


create table entryattribute (
    id       varchar(48) not null primary key,
    entryid  varchar(48) not null,
    name     varchar(255) not null,
    value    text not null
);
create index entryattribute_entryid_index on entryattribute( entryid );
alter table entryattribute add constraint entryattribute_name_uq unique ( entryid, name );

alter table entryattribute add constraint att_entryid_fk
    foreign key ( entryid ) references weblogentry( id );
