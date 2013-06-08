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


ALTER TABLE weblogentry ADD COLUMN (publishentry @BOOLEAN_SQL_TYPE2@ not null);

ALTER TABLE website ADD COLUMN (editorpage varchar(255) not null);
UPDATE website SET editorpage = 'editor-ekit.jsp';

CREATE TABLE temprole AS SELECT * FROM role;
DROP TABLE role;
create table role (
    id          varchar(255) not null primary key,
    role        varchar(255) not null,
    username    varchar(255) not null,
    userid      varchar(255) not null
);
INSERT INTO role 
    (id, role, username, userid) 
    SELECT temprole.id, role, temprole.username, rolleruser.id 
        FROM temprole, rolleruser
        WHERE temprole.username = rolleruser.username;
ALTER TABLE role ADD CONSTRAINT role_userid_fk 
    foreign key ( userid ) references rolleruser( id );
