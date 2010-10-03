

-- Roller 2.4 schema changes

    alter table pingtarget add autoenabled number(1) default 0 not null;  

    alter table website add lastmodified timestamp(2) default null;
