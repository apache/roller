
-- Upgrading from 1.2 to 1.3 requires one small change. The website table
-- column defaultpageid is now allowed to be null. Unfortunately, MySQL
-- requires non-standard alter table syntax for this.

-- So if you're on MySQL do this:
alter table website modify defaultpageid varchar(48);

-- And for all other databases do this:
alter table website alter column defaultpageid drop not null;