
create table testrolleruser (
    id              varchar(48) not null primary key,
    username        varchar(255) not null,
    passphrase      varchar(255) not null,
    screenname      varchar(255) not null,
    fullname        varchar(255) not null,
    emailaddress    varchar(255) not null,
    activationcode	varchar(48),
    datecreated     datetime not null,
    locale          varchar(20),  
    timezone        varchar(50),    
    isenabled       tinyint(1) default 1 not null
);
alter table testrolleruser add constraint tru_username_uq unique ( username(40) );

create table testuserrole (
    id               varchar(48) not null primary key,
    rolename         varchar(255) not null,
    username         varchar(255) not null,
    userid           varchar(48) not null
);
create index tur_userid_idx on testuserrole( userid );
create index tur_username_idx on testuserrole( username(40) );

