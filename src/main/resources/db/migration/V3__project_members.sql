create table project_members
(
    project_id bigint not null references projects (id),
    user_id    bigint not null references users (id),
    created_at timestamp with time zone not null default now(),
    primary key (project_id, user_id)
);

create index idx_project_members_user_id on project_members (user_id);

insert into project_members (project_id, user_id, created_at)
select p.id, p.owner_id, now()
from projects p
join users u on u.id = p.owner_id
where u.role = 'DEVELOPER'
  and not exists (
      select 1
      from project_members pm
      where pm.project_id = p.id
        and pm.user_id = p.owner_id
  );

insert into project_members (project_id, user_id, created_at)
select t.project_id, t.assignee_id, min(t.created_at)
from tickets t
join users u on u.id = t.assignee_id
where t.assignee_id is not null
  and u.role = 'DEVELOPER'
  and not exists (
      select 1
      from project_members pm
      where pm.project_id = t.project_id
        and pm.user_id = t.assignee_id
  )
group by t.project_id, t.assignee_id
;
