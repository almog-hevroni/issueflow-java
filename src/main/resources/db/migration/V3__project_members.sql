create table project_members
(
    project_id bigint not null references projects (id),
    user_id    bigint not null references users (id),
    created_at timestamp with time zone not null default now(),
    primary key (project_id, user_id)
);

create index idx_project_members_user_id on project_members (user_id);

merge into project_members (project_id, user_id, created_at)
key (project_id, user_id)
select p.id, p.owner_id, now()
from projects p
join users u on u.id = p.owner_id
where u.role = 'DEVELOPER';

merge into project_members (project_id, user_id, created_at)
key (project_id, user_id)
select t.project_id, t.assignee_id, min(t.created_at)
from tickets t
join users u on u.id = t.assignee_id
where t.assignee_id is not null
  and u.role = 'DEVELOPER'
group by t.project_id, t.assignee_id;
