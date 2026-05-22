create table users
(
    id            bigserial primary key,
    username      varchar(100) not null unique,
    email         varchar(255) not null unique,
    full_name     varchar(255) not null,
    role          varchar(20)  not null check (role in ('ADMIN', 'DEVELOPER')),
    password_hash varchar(255) not null,
    version       bigint       not null default 0,
    created_at    timestamp with time zone not null default now(),
    updated_at    timestamp with time zone not null default now()
);

create table projects
(
    id          bigserial primary key,
    name        varchar(200) not null,
    description varchar(2000),
    owner_id    bigint       not null references users (id),
    deleted_at  timestamp with time zone,
    version     bigint       not null default 0,
    created_at  timestamp with time zone not null default now(),
    updated_at  timestamp with time zone not null default now()
);

create table tickets
(
    id          bigserial primary key,
    project_id  bigint       not null references projects (id),
    title       varchar(255) not null,
    description varchar(4000),
    status      varchar(20)  not null check (status in ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE')),
    priority    varchar(20)  not null check (priority in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    type        varchar(20)  not null check (type in ('BUG', 'FEATURE', 'TECHNICAL')),
    assignee_id bigint references users (id),
    due_date    timestamp with time zone,
    is_overdue  boolean      not null default false,
    deleted_at  timestamp with time zone,
    version     bigint       not null default 0,
    created_at  timestamp with time zone not null default now(),
    updated_at  timestamp with time zone not null default now()
);

create table comments
(
    id         bigserial primary key,
    ticket_id  bigint      not null references tickets (id),
    author_id  bigint      not null references users (id),
    content    varchar(4000) not null,
    version    bigint      not null default 0,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table ticket_dependencies
(
    ticket_id            bigint      not null references tickets (id),
    blocked_by_ticket_id bigint      not null references tickets (id),
    created_at           timestamp with time zone not null default now(),
    primary key (ticket_id, blocked_by_ticket_id),
    constraint chk_ticket_dependency_not_self check (ticket_id <> blocked_by_ticket_id)
);

create table attachments
(
    id             bigserial primary key,
    ticket_id      bigint       not null references tickets (id),
    uploaded_by_id bigint       not null references users (id),
    file_name      varchar(255) not null,
    content_type   varchar(100) not null,
    size_bytes     bigint       not null,
    storage_path   varchar(500) not null,
    created_at     timestamp with time zone not null default now(),
    updated_at     timestamp with time zone not null default now()
);

create table comment_mentions
(
    comment_id         bigint      not null references comments (id),
    mentioned_user_id  bigint      not null references users (id),
    created_at         timestamp with time zone not null default now(),
    primary key (comment_id, mentioned_user_id)
);

create table audit_logs
(
    id           bigserial primary key,
    actor_type   varchar(20)  not null check (actor_type in ('USER', 'SYSTEM')),
    actor_user_id bigint references users (id),
    action       varchar(50)  not null,
    entity_type  varchar(100) not null,
    entity_id    bigint       not null,
    details_json text,
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now()
);

create table revoked_tokens
(
    id          bigserial primary key,
    jti         varchar(255) not null unique,
    token_hash  varchar(255) not null,
    expires_at  timestamp with time zone not null,
    revoked_at  timestamp with time zone not null,
    created_at  timestamp with time zone not null default now(),
    updated_at  timestamp with time zone not null default now()
);

create index idx_projects_deleted_at on projects (deleted_at);
create index idx_tickets_project_id on tickets (project_id);
create index idx_tickets_assignee_id on tickets (assignee_id);
create index idx_tickets_deleted_at on tickets (deleted_at);
create index idx_comments_ticket_id on comments (ticket_id);
create index idx_comment_mentions_user_id on comment_mentions (mentioned_user_id);
create index idx_ticket_dependencies_ticket_id on ticket_dependencies (ticket_id);
create index idx_attachments_ticket_id on attachments (ticket_id);
create index idx_audit_logs_created_at on audit_logs (created_at);
create index idx_audit_logs_entity_type on audit_logs (entity_type);
create index idx_audit_logs_actor_user_id on audit_logs (actor_user_id);
create index idx_revoked_tokens_expires_at on revoked_tokens (expires_at);
