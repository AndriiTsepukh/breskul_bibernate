DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS student;
CREATE TABLE products
(
    id   INT,
    name VARCHAR(255)
);

INSERT INTO products
VALUES (1, 'Orange');

CREATE TABLE student
(
    id   int primary key ,
    name VARCHAR(255)
);

INSERT INTO student
VALUES (1, 'Miha');