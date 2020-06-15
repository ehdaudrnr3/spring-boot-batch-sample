DROP TABLE IF EXISTS customer;

CREATE TABLE customer (
    id bigint NOT NULL AUTO_INCREMENT primary key,
    firstName VARCHAR(255) NULL,
    lastName VARCHAR(255) NULL,
    birthdate VARCHAR(255) NULL,
    insertDate TIMESTAMP NULL
);