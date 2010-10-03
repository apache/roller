

-- Roller 2.2 schema changes

    alter table website add column isactive boolean;
    alter table website alter isactive set default true;
    update website set isactive=true;
    alter table website alter isactive set not null;
update website set isactive=true, datecreated=datecreated;

-- Roller 2.3 schema changes

    alter table weblogentry add column summary text default null;
    alter table weblogentry add column content_type varchar(48) default null;
    alter table weblogentry add column content_src varchar(255) default null;


