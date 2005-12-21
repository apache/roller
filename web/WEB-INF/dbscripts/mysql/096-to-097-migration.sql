
create table if not exists comment (
    id      varchar(255) not null primary key,
    entryid varchar(255) not null,
    name    varchar(255) null,
    email   varchar(255) null,
    url     varchar(255) null,
    content text null,
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
    excerpt   text null,
    dayhits   integer default 0 not null,
    totalhits integer default 0 not null,    
    visible   bit default 0 not null,
    duplicate bit default 0 not null
);
create index referer_websiteid_index on referer( websiteid );
create index referer_entryid_index on referer( entryid );

  
alter table website add column (allowcomments bit default 1 not null);

alter table website add column (ignorewords text null);

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

  