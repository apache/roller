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

-- planet tables
drop table rag_entry;
drop table rag_group_subscription;
drop table rag_subscription;
drop table rag_group;
drop table rag_planet;
drop table rag_properties;
drop table rag_config;

-- non-associated tables
drop table newsfeed;
drop table usercookie;
drop table rollerconfig;
drop table roller_properties;
drop table roller_audit_log;
drop table roller_tasklock;

-- supplemental services tables
drop table pingqueueentry;
drop table pingcategory;
drop table autoping;
drop table pingtarget;
drop table referer;
drop table bookmark;
drop table folder;
drop table folderassoc;

-- core services tables
drop table roller_hitcounts;
drop table roller_comment;
drop table roller_weblogentrytag;
drop table roller_weblogentrytagagg;
drop table entryattribute;
drop table weblogentry;
drop table weblogcategoryassoc;
drop table weblogcategory;
drop table webpage;

-- core platform tables
drop table roller_user_permissions;
drop table website;
drop table userrole;
drop table rolleruser;

