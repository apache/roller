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


-- User permissions within a website
-- permission_mask: bitmask 000 limited, 001 author, 011 admin
-- pending: pending user acceptance of invitation to join website
create table roller_user_permissions (
    id              varchar(48) not null primary key,
    website_id      varchar(48) not null,
    user_id         varchar(48) not null,
    permission_mask integer not null, 
    pending         bit default 1 not null
);

-- Add new handle field to uniquely identify websites in URLs

alter table website add column handle varchar(255);
alter table website alter handle varchar(255) default '';
update website set handle='';
alter table website alter handle varchar(255) not null ;

alter table website add column datecreated timestamp;
alter table website alter datecreated timestamp default '20050101';
update website set datecreated='20050101';
alter table website alter datecreated timestamp not null;

alter table website add column emailaddress varchar(255);
alter table website alter emailaddress varchar(255) default '';
update website set emailaddress='';
alter table website alter handle varchar(255) not null;

create index website_handle_index on website(handle);

-- this constraint won't work for upgrades until the handle column is
-- populated with data, otherwise all columns are '' which will not
-- satisfy the 'unique' condition
-- alter table website add constraint website_handle_uq unique (handle);

-- Add userid to weblogentry so we can track original creator of entry
alter table weblogentry add column userid varchar(48);
alter table weblogentry alter userid varchar(48) default '';
update weblogentry set userid='';
alter table weblogentry alter userid varchar(48) not null;

alter table weblogentry add column status varchar(20);
alter table weblogentry alter status varchar(20) default '';
update weblogentry set status='';
alter table weblogentry alter status varchar(20) not null;

create index we_status_idx on weblogentry(status);
create index weblogentry_userid_index on weblogentry(userid);

alter table rolleruser add column isenabled bit;
alter table rolleruser alter isenabled boolean default true;
update rolleruser set isenabled=true;
alter table rolleruser alter isenabled boolean not null;

alter table rolleruser add column locale varchar(50);
alter table rolleruser alter locale varchar(50) default '';
update rolleruser set locale='';
alter table rolleruser alter locale varchar(50) not null;

alter table rolleruser add column timezone varchar(50);
alter table rolleruser alter timezone varchar(50) default '';
update rolleruser set timezone='';
alter table rolleruser alter timezone varchar(50) not null;

create index user_isenabled_index on rolleruser( isenabled );

-- -----------------------------------------------------

-- Audit log records time and comment about change
-- user_id: user that made change
-- object_id: id of associated object, if any
-- object_class: name of associated object class (e.g. WeblogEntryData)
-- comment: description of change
-- change_time: time that change was made
create table roller_audit_log (
    id              varchar(48) not null primary key,
    user_id         varchar(48) not null,  
    object_id       varchar(48),           
    object_class    varchar(255),          
    comment_text    varchar(255) not null, 
    change_time     timestamp              
);


-- -----------------------------------------------------

-- make "pubtime" use NULL for default values.  this allows us to leave
-- the "pubtime" for an entry unset until the entry is actually published.
-- 
-- sadly this needs to be done in a specific manner for each db, so check
-- the db_*.properties file for each db to see how it's done.
;


-- -----------------------------------------------------
-- For ROL-754. MySQL 5.x introduced a new keyword "condition"
-- which made the use of "condition" as a column name in the "pingtarget" table illegal.
-- This renames the column to "conditioncode".   There is a corresponding change in the
-- Hibernate mapping metadata.

-- Create the new column.  If your database will not autopopulate new columns with default values, you may
-- have to remove the "not null" clause here.
alter table pingtarget add column conditioncode integer;
alter table pingtarget alter conditioncode set default 0;
update pingtarget set conditioncode=0;
alter table pingtarget alter conditioncode set not null;

-- Transfer old column data to the new column.  This is not critical as currently it is not used, and
-- later the data will be generated by usage in the ping processor.
update pingtarget set conditioncode=condition;

-- Drop the old column 
-- Don't do this until you're sure you don't need to back-off to Roller 1.2
-- alter table pingtarget drop column condition;

-- -----------------------------------------------------

-- Removing all indexes, foreign key with long names to support DB2

alter table website drop foreign key website_userid_fk;
alter table userrole drop foreign key userrole_userid_fk;
alter table webpage drop foreign key weblogpage_websiteid_fk;
alter table weblogentry drop foreign key weblogentry_websiteid_fk;
alter table weblogentry drop foreign key weblogentry_categoryid_fk;
alter table weblogcategory drop foreign key weblogcategory_websiteid_fk;
alter table comment drop foreign key comment_entryid_fk;
alter table entryattribute drop foreign key att_entryid_fk;
alter table referer drop foreign key referer_entryid_fk;
alter table referer drop foreign key referer_websiteid_fk;
alter table folder drop foreign key folder_websiteid_fk;
alter table bookmark drop foreign key bookmark_folderid_fk;
alter table newsfeed drop foreign key newsfeed_websiteid_fk;
alter table pingtarget drop foreign key pingtarget_websiteid_fk;
alter table autoping drop foreign key autoping_websiteid_fk;
alter table autoping drop foreign key autoping_pingtargetid_fk;
alter table pingcategory drop foreign key pingcategory_autopingid_fk;
alter table pingcategory drop foreign key pingcategory_categoryid_fk;


alter table userrole drop index userrole_userid_index;
alter table userrole drop index userrole_username_index;
alter table usercookie drop index usercookie_username_index;
alter table usercookie drop index usercookie_cookieid_index;
alter table webpage drop index webpage_name_index;
alter table webpage drop index webpage_link_index;
alter table webpage drop index webpage_id_index;
alter table website drop index website_id_index;
alter table website drop index website_userid_index;
alter table website drop index website_isenabled_index;
alter table folder drop index folder_websiteid_index;
alter table folderassoc drop index folderassoc_folderid_index;
alter table folderassoc drop index folderassoc_ancestorid_index;
alter table folderassoc drop index folderassoc_relation_index;
alter table bookmark drop index bookmark_folderid_index;
alter table weblogcategory drop index weblogcategory_websiteid_index;
alter table weblogcategoryassoc drop index weblogcategoryassoc_categoryid_index;
alter table weblogcategoryassoc drop index weblogcategoryassoc_ancestorid_index;
alter table weblogcategoryassoc drop index weblogcategoryassoc_relation_index;
alter table weblogentry drop index weblogentry_websiteid_index;
alter table weblogentry drop index weblogentry_categoryid_index;
alter table weblogentry drop index weblogentry_pubtime_index;
alter table weblogentry drop index weblogentry_pinnedtomain_index;
alter table weblogentry drop index weblogentry_publishentry_index;
alter table newsfeed drop index newsfeed_websiteid_index;
alter table comment drop index comment_entryid_index;
alter table pingtarget drop index pingtarget_websiteid_index;
alter table autoping drop index autoping_websiteid_index;
alter table autoping drop index autoping_pingtargetid_index;
alter table pingcategory drop index pingcategory_autopingid_index;
alter table pingcategory drop index pingcategory_categoryid_index;
alter table pingqueueentry drop index pingqueueentry_entrytime_index;
alter table pingqueueentry drop index pingqueueentry_pingtargetid_index;
alter table pingqueueentry drop index pingqueueentry_websiteid_index;
alter table referer drop index referer_websiteid_index;
alter table referer drop index referer_entryid_index;
alter table referer drop index referer_refurl_index;
alter table referer drop index referer_requrl_index;
alter table referer drop index referer_datestr_index;
alter table referer drop index referer_refpermalink_index;
alter table referer drop index referer_duplicate_index;
alter table entryattribute drop index entryattribute_entryid_index;
alter table rag_group_subscription drop index rag_group_subscription_gid; 
alter table rag_group_subscription drop index rag_group_subscription_sid; 
alter table rag_group drop index rag_group_handle; 
alter table rag_subscription drop index rag_subscription_feed_url; 
alter table rag_entry drop index rag_entry_sid;


-- Adding all new indexes with short names

create index ur_userid_idx on userrole( userid );
create index ur_username_idx on userrole( username );
create index uc_username_idx on usercookie( username );
create index uc_cookieid_idx on usercookie( cookieid );
create index wp_name_idx on webpage( name );
create index wp_link_idx on webpage( link );
create index wp_id_idx on webpage( websiteid );
create index ws_userid_idx    on website(userid);
create index ws_isenabled_idx on website(isenabled);
create index fo_websiteid_idx on folder( websiteid );
create index fa_folderid_idx on folderassoc( folderid );
create index fa_ancestorid_idx on folderassoc( ancestorid );
create index fa_relation_idx on folderassoc( relation );
create index bm_folderid_idx on bookmark( folderid );
create index wc_websiteid_idx on weblogcategory( websiteid );
create index wca_categoryid_idx on weblogcategoryassoc( categoryid );
create index wca_ancestorid_idx on weblogcategoryassoc( ancestorid );
create index wca_relation_idx on weblogcategoryassoc( relation );
create index we_websiteid_idx on weblogentry( websiteid );
create index we_categoryid_idx on weblogentry( categoryid );
create index we_pubtime_idx on weblogentry( pubtime,publishentry,websiteid );
create index we_pinnedtom_idx on weblogentry(pinnedtomain);
create index we_pubentry_idx on weblogentry(publishentry);
create index we_userid_idx on weblogentry(userid);
create index nf_websiteid_idx on newsfeed( websiteid );
create index co_entryid_idx on comment( entryid );
create index pt_websiteid_idx on pingtarget( websiteid );
create index ap_websiteid_idx on autoping( websiteid );
create index ap_pingtid_idx on autoping( pingtargetid );
create index pc_autopingid_idx on pingcategory( autopingid );
create index pc_categoryid_idx on pingcategory( categoryid );
create index pqe_entrytime_idx on pingqueueentry( entrytime );
create index pqe_pingtid_idx on pingqueueentry( pingtargetid );
create index pqe_websiteid_idx on pingqueueentry( websiteid );
create index ref_websiteid_idx on referer( websiteid );
create index ref_entryid_idx on referer( entryid );
create index ref_refurl_idx on referer( refurl );
create index ref_requrl_idx on referer( requrl );
create index ref_datestr_idx on referer( datestr );
create index ref_refpermlnk_idx on referer( refpermalink );
create index ref_duplicate_idx on referer( duplicate );
create index ea_entryid_idx on entryattribute( entryid );
create index raggs_gid_idx on rag_group_subscription(group_id); 
create index raggs_sid_idx on rag_group_subscription(subscription_id); 
create index rage_sid_idx on rag_entry(subscription_id); 

-- Now add the foreign key relationships

-- user, role and website
alter table website add constraint ws_userid_fk
    foreign key ( userid ) references rolleruser ( id )  ;

alter table userrole add constraint ur_userid_fk
    foreign key ( userid ) references rolleruser( id )  ;

-- page, entry, category, comment
alter table webpage add constraint wp_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

alter table weblogentry add constraint we_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

alter table weblogentry add constraint wc_categoryid_fk
    foreign key ( categoryid ) references weblogcategory( id )  ;

alter table weblogcategory add constraint wc_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

alter table comment add constraint co_entryid_fk
    foreign key ( entryid ) references weblogentry( id )  ;

alter table entryattribute add constraint att_entryid_fk
    foreign key ( entryid ) references weblogentry( id )  ;

-- referer
alter table referer add constraint ref_entryid_fk
    foreign key ( entryid ) references weblogentry( id )  ;

alter table referer add constraint ref_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

-- folder and bookmark
alter table folder add constraint fo_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

alter table bookmark add constraint bm_folderid_fk
    foreign key ( folderid ) references folder( id )  ;

-- newsfeed
alter table newsfeed add constraint nf_websiteid_fk
    foreign key ( websiteid ) references website( id )  ;

-- pingtarget, autoping, pingcategory
alter table pingtarget add constraint pt_websiteid_fk
    foreign key (websiteid) references website(id)  ;

alter table autoping add constraint ap_websiteid_fk
    foreign key (websiteid) references website(id)  ;

alter table autoping add constraint ap_pingtargetid_fk
    foreign key (pingtargetid) references pingtarget(id)  ;

alter table pingcategory add constraint pc_autopingid_fk
    foreign key (autopingid) references autoping(id)  ;

alter table pingcategory add constraint pc_categoryid_fk
    foreign key (categoryid) references weblogcategory(id)  ;
    
-- Oracle compatability DDL
alter table comment rename to roller_comment;
