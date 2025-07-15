DROP SCHEMA IF EXISTS droneshop CASCADE;
CREATE SCHEMA IF NOT EXISTS droneshop;
GRANT ALL ON SCHEMA droneshop TO droneshopadmin;
 
create table orders (
    order_id uuid not null,
    location varchar(255),
    loyaltyMemberId varchar(255),
    orderSource varchar(255),
    orderStatus varchar(255),
    timestamp timestamp,
    primary key (order_id)
);

create table lineItems (
    itemId varchar(255) not null,
    item varchar(255),
    lineItemStatus varchar(255),
    name varchar(255),
    price numeric(19, 2),
    order_id uuid not null,
    primary key (itemId)
);

create table OutboxEvent (
    id uuid not null,
    aggregatetype varchar(255) not null,
    aggregateid varchar(255) not null,
    type varchar(255) not null,
    timestamp timestamp not null,
    payload varchar(8000),
    tracingspancontext varchar(256),
    primary key (id)
);