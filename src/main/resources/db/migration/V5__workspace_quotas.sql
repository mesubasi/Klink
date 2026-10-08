-- NULL means unlimited, which keeps the behaviour of existing workspaces unchanged.
alter table workspaces add column max_members integer;
alter table workspaces add column max_links integer;
