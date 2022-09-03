package org.breskul.tests;


import org.breskul.exception.TableNameNotCorrect;
import org.breskul.pool.PooledDataSource;
import org.breskul.session.SessionFactory;
import org.breskul.testdata.entity.EntityToTestDelete;
import org.breskul.testdata.entity.Products;
import org.breskul.testdata.entity.Student;
import org.breskul.testdata.entity.TableNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;


public class SessionFactoryTest {

    private static final String url = "jdbc:h2:file:./target/db/testdb";
    private static final String username = "sa";
    private static final String password = "Abcd1234";
    private static final int DEFAULT_POOL_SIZE = 10;

    @BeforeEach
    public void setup() {
        PooledDataSource.reset();
    }

    @Test
    public void findElementTest() {
        final var pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        assertNotNull(products);
    }

    @Test
    public void checkCashTest() {
        final var pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        final var productsSecond = session.find(Products.class, 1);
        assertEquals(products, productsSecond);
    }

    @Test
    public void checkDifferentObjectTest() {
        final var pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        final var productsSecond = session.find(Products.class, 2);
        assertNotEquals(products, productsSecond);
    }

    @Test
    public void findElementWithCustomColumnNameTest() {
        final var pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var student = session.find(Student.class, 1);
        assertNotNull(student);
    }

    @Test
    public void checkConnectionSizeTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        session.find(Student.class, 1);
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertEquals(DEFAULT_POOL_SIZE, connectionSizeAfterFirstConnection);
    }

    @Test
    public void elementNotFoundTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource, true);
        final var session = sessionFactory.createSession();
        final var student = session.find(Student.class, 3);
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertNull(student);
        assertEquals(DEFAULT_POOL_SIZE, connectionSizeAfterFirstConnection);
    }

    @Test
    public void tableNotFoundTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        try {
            session.find(TableNotFound.class, 3);
        } catch (TableNameNotCorrect e) {

        }
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertEquals(DEFAULT_POOL_SIZE, connectionSizeAfterFirstConnection);
    }

    @Test
    public void persistActionTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();

        EntityToTestDelete entityToTestDelete = new EntityToTestDelete();
        entityToTestDelete.id = 2L;
        session.remove(entityToTestDelete);
        session.flush();
        final var deletedStudent = session.find(Student.class, 2);
        assertNull(deletedStudent);
        final var existingStudent = session.find(Student.class, 1);
        assertNotNull(existingStudent);
    }
}
