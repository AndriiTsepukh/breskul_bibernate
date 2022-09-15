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
    id   LONG NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    second_name VARCHAR(255)
);

INSERT INTO student (name, second_name)
VALUES ('Miha', 'Beheka');

INSERT INTO student (name, second_name)
VALUES ('Gleb', 'Gomenyuk');

INSERT INTO student (name, second_name)
VALUES ('Alexander', 'Kharchenko');

INSERT INTO student (name, second_name)
VALUES ('Andrii', 'Tsepukh');

INSERT INTO student (name, second_name)
VALUES ('Vanya', 'Kulyk');

INSERT INTO student (name, second_name)
VALUES ('Vladislav', 'Solopov');

INSERT INTO student (name, second_name)
VALUES ('Volodymyr', 'Holichenko');

INSERT INTO student (name, second_name)
VALUES ('Станислав', 'Хижняк');

CREATE TABLE persons
(
    id   bigint primary key ,
    person_name VARCHAR(255),
    person_surname VARCHAR(255)
);

INSERT INTO persons
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

CREATE TABLE table_test
(
    id   LONG NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255)
);

CREATE TABLE location
(
    id   LONG NOT NULL AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(255),
    rooms_qty INT,
    price LONG
);