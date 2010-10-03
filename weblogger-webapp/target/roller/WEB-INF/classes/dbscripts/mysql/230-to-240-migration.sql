

-- Roller 2.4 schema changes

    alter table pingtarget add column autoenabled tinyint(1) default 0 not null;  

    alter table website add column lastmodified datetime default null;
