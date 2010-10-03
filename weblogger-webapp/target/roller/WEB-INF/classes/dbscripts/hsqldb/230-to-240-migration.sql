

-- Roller 2.4 schema changes

    alter table pingtarget add column autoenabled bit default 0 not null; 

    alter table website add column lastmodified timestamp default null;
