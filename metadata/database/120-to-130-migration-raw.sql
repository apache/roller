
-- when using a shared theme, website need not track defauld page id
alter table website alter column defaultpageid drop not null;