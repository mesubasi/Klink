-- Speeds up the paginated "my links" listing and search.
create index if not exists idx_url_mappings_user_created on url_mappings (user_id, created_at desc);
create index if not exists idx_url_mappings_workspace on url_mappings (workspace_id);
