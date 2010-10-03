

-- Roller 3.0 schema changes

-- add new column which holds the list of custom models for a weblog
    alter table website add pagemodels varchar(512) default null;

-- add new columns which hold the multi-lang settings for a weblog
    alter table website add enablemultilang number(1) default 0 not null;  
    alter table website add showalllangs number(1) default 1 not null;  

-- add new column which holds the hidden status for a page, default is false
    alter table webpage add hidden number(1) default 0 not null;  

-- add new column which holds the hidden status for a page, default is false
    alter table webpage add navbar number(1) default 0 not null;  
update webpage set navbar=1;
update webpage set navbar=0 where name like '\_%';

-- add new column which holds the template language used for a page
-- then set template language to velocity for all templates
    alter table webpage add templatelang varchar(20) default null;
update webpage set templatelang = 'velocity';

-- add new column which holds the decorator for a page
-- then set value to _decorator for all templates except decorators
    alter table webpage add decorator varchar(255) default null;
update webpage set decorator = '_decorator' where name <> '_decorator';