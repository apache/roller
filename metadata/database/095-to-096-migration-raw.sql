
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
