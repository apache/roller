
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
