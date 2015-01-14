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

-- Running local tests in eclipse.

-- Script is used to clear old data prior to running local tests

-- Copy this file to app/src/test/resources/dbscripts/junit-cleartables-mysql.sql

-- Any changes here should also be in docs/testing/junit-cleartables-mysql.sql.

delete mt from weblog w, roller_mediafile mf, roller_mediafiletag mt where w.id = mf.weblogid and mt.mediafile_id = mf.id and w.creator like 'junit_%';
delete mf from weblog w, roller_mediafile mf where w.id = mf.weblogid and w.creator like 'junit_%';
delete md from weblog w, roller_mediafiledir md where w.id = md.websiteid and w.creator like 'junit_%';
delete from roller_mediafile WHERE creator like 'junit_%';
delete from roller_mediafiledir;

delete r from weblog w, referer r WHERE w.id = r.websiteid and w.creator like 'junit_%';

delete from weblog WHERE creator like 'junit_%';

delete b from weblog w, bookmark_folder f, bookmark b where f.id = b.folderid and w.id = f.websiteid and w.creator like 'junit_%';
delete from bookmark_folder WHERE websiteid like 'junit_%';

delete from userrole WHERE username like 'junit_%';
delete from roller_permission WHERE username like 'junit_%';
delete from roller_user WHERE username like 'junit_%';
delete from roller_userattribute WHERE username like 'junit_%';
delete from roller_oauthconsumer WHERE username like 'junit_%';
delete from roller_oauthaccessor WHERE username like 'junit_%';
delete from roller_tasklock WHERE name = 'TestTask';

