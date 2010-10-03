

-- Roller 2.4 schema changes

    alter table pingtarget add column autoenabled boolean;
    alter table pingtarget alter autoenabled set default false;
    update pingtarget set autoenabled=false;
    alter table pingtarget alter autoenabled set not null;

    alter table website add column lastmodified timestamp(2) with time zone default null;
