DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS customer_file_info;

CREATE TABLE customer (
    id bigint NOT NULL AUTO_INCREMENT primary key,
    firstName VARCHAR(255) NULL,
    lastName VARCHAR(255) NULL,
    birthdate VARCHAR(255) NULL,
    insertDate TIMESTAMP NULL
);

CREATE TABLE customer_file_info (
    id bigint NOT NULL AUTO_INCREMENT primary key,
    jobId VARCHAR(255) NULL,
    fileName VARCHAR(255) NULL,
    insertDate TIMESTAMP NULL
);