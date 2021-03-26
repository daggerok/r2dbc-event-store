drop table if exists domain_events CASCADE
;

drop table if exists snapshots CASCADE
;

create table domain_events (
    event_type varchar(255) not null,
    aggregate_id varchar(36) not null,
    occurred_at timestamp,
    sequence_number BIGINT IDENTITY PRIMARY KEY,
    name varchar(255),
    expire_at timestamp,
    door_id varchar(255)
)
;

create table snapshots (
    id BIGINT IDENTITY PRIMARY KEY,
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

insert into domain_events (event_type, aggregate_id, name)
                   values ('VisitorRegisteredEvent', '0-0-0-0-1', 'test')
;

insert into snapshots (aggregate_id, name)
               values ('0-0-0-0-1', 'test')
;
