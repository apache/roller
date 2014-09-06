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

-- non-associated tables
drop table newsfeed;
drop table roller_properties;
drop table roller_audit_log;
drop table roller_tasklock;

-- supplemental services tables
drop table pingqueueentry;
drop table autoping;
drop table pingtarget;
drop table bookmark;
drop table bookmark_folder;
drop table roller_mediafiletag;
drop table roller_mediafile;
drop table roller_mediafiledir;


-- core services tables
drop table roller_hitcounts;
drop table roller_comment;
drop table roller_weblogentrytag;
drop table roller_weblogentrytagagg;
drop table entryattribute;
drop table weblogentry;
drop table weblogcategory;
drop table weblog_custom_template;
drop table custom_template_rendition;

-- core platform tables
drop table roller_permission;
drop table weblog;
drop table userrole;
drop table roller_user;

-- oauth tables
drop table roller_oauthconsumer;
drop table roller_oauthaccessor;
