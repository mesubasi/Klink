-- Bumping this number invalidates every JWT issued to the user earlier (password reset, sign out everywhere).
alter table users add column token_version integer not null default 0;
