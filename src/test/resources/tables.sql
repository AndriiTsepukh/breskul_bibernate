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

CREATE TABLE table_delete_test
(
    id   LONG primary key ,
    name VARCHAR(255),
    second_name VARCHAR(255)
);

INSERT INTO table_delete_test
VALUES (1, 'Test1', 'User1');

INSERT INTO table_delete_test
VALUES (2, 'Test2', 'User2');