

create table roller_weblogentrytag (
    id              varchar(48)   not null primary key,
    entryid         varchar(48)   not null,
    websiteid       varchar(48)   not null,    
    userid		    varchar(48)  not null,
    name            varchar(255)  not null,
    time            datetime 	not null
);

create index wet_entryid_idx on roller_weblogentrytag( entryid );
create index wet_websiteid_idx on roller_weblogentrytag( websiteid );
create index wet_userid_idx on roller_weblogentrytag( userid );
create index wet_name_idx on roller_weblogentrytag( name );

create table roller_weblogentrytagagg (
    id              varchar(48)   not null primary key,
    websiteid       varchar(48) ,    
    name            varchar(255)  not null,
    total           integer		  not null,
    lastused        datetime 	not null
);

create index weta_websiteid_idx on roller_weblogentrytagagg( websiteid );
create index weta_name_idx on roller_weblogentrytagagg( name );
create index weta_lastused_idx on roller_weblogentrytagagg( lastused );

create table roller_tasklock (
    id              varchar(48)   not null primary key,
    name            varchar(255)  not null,
    islocked        tinyint(1) default 0,
    timeacquired    datetime NULL,
    timeleased	    integer,
    lastrun         datetime NULL
);
alter table roller_tasklock add constraint rtl_name_uq unique ( name(40) );

create table roller_hitcounts (
    id              varchar(48) not null primary key,
    websiteid       varchar(48) not null,
    dailyhits	    integer
);
create index rhc_websiteid_idx on roller_hitcounts(websiteid);

-- The rag_group_subscription table has been simplified and no longer needs a 
-- primary key but we don't want to drop the field just yet because that would 
-- make it impossible to abort a 3.1 upgrade and return to 3.0. 
    alter table rag_group_subscription drop primary key;
    alter table rag_group_subscription modify id varchar(48) null;







