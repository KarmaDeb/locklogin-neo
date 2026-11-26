create table users
(
    id                 bigserial
        constraint pk_users
            primary key,
    uuid               uuid        not null,
    username           varchar(16) not null,
    last_disconnect_at timestamp,
    last_seen_at       timestamp   not null,
    first_seen_at      timestamp   not null,
    registered_at      timestamp,
    secrets            json
);