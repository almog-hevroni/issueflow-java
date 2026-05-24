insert into users (username, email, full_name, role, password_hash)
select
    'admin',
    'admin@issueflow.local',
    'Initial Admin',
    'ADMIN',
    '$2a$10$60jvVCT0AVRe8YPf/GzeCuSrLnhivAyoB6I6Sy7RYnFcqXitoLIc2'
where not exists (
    select 1
    from users
    where lower(username) = lower('admin')
);
