DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS student;
CREATE TABLE products
(
    id   INT,
    name VARCHAR(255)
);

INSERT INTO products
VALUES (1, 'Orange');

INSERT INTO products
VALUES (2, 'Beer');

CREATE TABLE student
(
    id   bigint primary key ,
    name VARCHAR(255),
    second_name VARCHAR(255)
);

INSERT INTO student
VALUES (1, 'Miha', 'Beheka');


CREATE TABLE persons
(
    id   bigint primary key ,
    person_name VARCHAR(255),
    person_surname VARCHAR(255)
);

INSERT INTO persons
VALUES (1, 'Miha', 'Beheka');