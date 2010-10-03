

-- Roller 2.2 schema changes

    alter table website add column isactive bit default 1 not null; 
update website set isactive=1, datecreated=datecreated;

-- Roller 2.3 schema changes

    alter table weblogentry add column summary longvarchar default null;
    alter table weblogentry add column content_type varchar(48) default null;
    alter table weblogentry add column content_src varchar(255) default null;


