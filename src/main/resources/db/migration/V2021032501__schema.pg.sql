drop table if exists snapshots CASCADE
;

drop table if exists domain_events CASCADE
;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- required by uuid_generate_v1

create table domain_events (
    event_type varchar(255) not null,
    aggregate_id varchar(36) not null,
    occurred_at timestamp,
    sequence_number SERIAL PRIMARY KEY,
    name varchar(255),
    expire_at timestamp,
    door_id varchar(255)
)
;

create table snapshots (
    id SERIAL PRIMARY KEY,
    version BIGINT,
    aggregate_id varchar(36) not null,
    occurred_at timestamp,
    sequence_number BIGINT,
    name varchar(255),
    expire_at timestamp,
    delivered_at timestamp,
    last_door_id varchar(255),
    last_door_entered_at timestamp
)
;
