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
--
-- Source file modified from the original ASF source; all changes made
-- are also under Apache License.

-- planet tables
drop table planet_subscription_entry;
drop table planet_subscription;
drop table planet;

-- non-associated tables
drop table weblogger_properties;

-- supplemental services tables
drop table weblog_ping_target;
drop table ping_target;
drop table blogroll_link;
drop table media_file_tag;
drop table media_file;
drop table media_directory;

-- core services tables
drop table weblog_entry_comment;
drop view weblog_entry_tag_agg;
drop table weblog_entry_tag;
drop table weblog_entry;
drop table weblog_category;
drop table weblog_custom_template;
drop table custom_template_rendition;

-- core platform tables
drop table user_weblog_role;
drop table weblog;
drop table roller_user;
