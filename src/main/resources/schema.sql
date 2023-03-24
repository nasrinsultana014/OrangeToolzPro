create table if not exists customers (
    id bigint NOT NULL AUTO_INCREMENT,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    city varchar(50) not null,
    country_code varchar(50) not null,
    zip_code varchar(10) not null,
    phone varchar(16) not null,
    email varchar(50) not null,
    ip varchar(3) not null,
    PRIMARY KEY (id)
);

create table if not exists invalid_customers (
    id bigint NOT NULL AUTO_INCREMENT,
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    city varchar(50) not null,
    country_code varchar(50) not null,
    zip_code varchar(10) not null,
    phone varchar(16) not null,
    email varchar(50) not null,
    ip varchar(3) not null,
    PRIMARY KEY (id)
);