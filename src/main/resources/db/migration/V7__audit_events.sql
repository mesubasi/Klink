-- Append-only audit trail: who did what, to what, when and from where.
create table audit_events (occurred_at bigint not null, id uuid not null, workspace_id uuid, actor_username varchar(50), actor_role varchar(20), action varchar(60) not null, outcome varchar(10) not null, target_type varchar(30), target_id varchar(100), ip varchar(64), forwarded_for varchar(255), user_agent varchar(255), details varchar(1000), primary key (id));
create index idx_audit_time on audit_events (occurred_at);
create index idx_audit_workspace_time on audit_events (workspace_id, occurred_at);
create index idx_audit_actor_time on audit_events (actor_username, occurred_at);
create index idx_audit_action_time on audit_events (action, occurred_at);

-- Rows can be added and (by the retention job) expired, but never rewritten.
create function audit_events_reject_update() returns trigger as $$
begin
    raise exception 'audit_events is append-only';
end;
$$ language plpgsql;

create trigger audit_events_no_update before update on audit_events
    for each row execute function audit_events_reject_update();
