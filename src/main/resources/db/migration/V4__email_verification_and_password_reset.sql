-- Existing accounts are treated as verified; new registrations start unverified.
alter table users add column email_verified boolean not null default true;
create table auth_tokens (created_at bigint not null, expires_at bigint not null, used_at bigint, id uuid not null, user_id uuid not null, type varchar(30) not null check (type in ('EMAIL_VERIFICATION','PASSWORD_RESET')), token_hash varchar(64) not null unique, primary key (id));
create index idx_auth_tokens_user on auth_tokens (user_id, type);
alter table auth_tokens add constraint fk_auth_tokens_user foreign key (user_id) references users;
