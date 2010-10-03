

-- Roller 3.0 schema changes

-- add new column which holds the list of custom models for a weblog
    alter table website add column pagemodels varchar(512) default null;

-- add new columns which hold the multi-lang settings for a weblog
    alter table website add column enablemultilang boolean;
    alter table website alter enablemultilang set default false;
    update website set enablemultilang=false;
    alter table website alter enablemultilang set not null;
    alter table website add column showalllangs boolean;
    alter table website alter showalllangs set default true;
    update website set showalllangs=true;
    alter table website alter showalllangs set not null;

-- add new column which holds the hidden status for a page, default is false
    alter table webpage add column hidden boolean;
    alter table webpage alter hidden set default false;
    update webpage set hidden=false;
    alter table webpage alter hidden set not null;

-- add new column which holds the hidden status for a page, default is false
    alter table webpage add column navbar boolean;
    alter table webpage alter navbar set default false;
    update webpage set navbar=false;
    alter table webpage alter navbar set not null;
update webpage set navbar=true;
update webpage set navbar=false where name like '\_%';

-- add new column which holds the template language used for a page
-- then set template language to velocity for all templates
    alter table webpage add column templatelang varchar(20) default null;
update webpage set templatelang = 'velocity';

-- add new column which holds the decorator for a page
-- then set value to _decorator for all templates except decorators
    alter table webpage add column decorator varchar(255) default null;
update webpage set decorator = '_decorator' where name <> '_decorator';