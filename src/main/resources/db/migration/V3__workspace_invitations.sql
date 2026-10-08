create table workspace_invitations (created_at bigint not null, expires_at bigint not null, accepted_at bigint, id uuid not null, workspace_id uuid not null, invited_by_id uuid, role varchar(20) not null check (role in ('ADMIN','MEMBER','VIEWER')), status varchar(20) not null check (status in ('PENDING','ACCEPTED','REVOKED')), email varchar(100) not null, token_hash varchar(64) not null unique, primary key (id));
create index idx_wi_workspace on workspace_invitations (workspace_id);
alter table workspace_invitations add constraint fk_wi_workspace foreign key (workspace_id) references workspaces;
alter table workspace_invitations add constraint fk_wi_invited_by foreign key (invited_by_id) references users;
