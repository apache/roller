-- add new attribute to WeblogEntry
alter table weblogentry add column (autoformatentry BOOLEAN_SQL_TYPE not null);

-- add new attribute to Website
alter table website add column (autoformatdefault BOOLEAN_SQL_TYPE not null);