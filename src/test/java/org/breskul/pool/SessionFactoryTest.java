package org.breskul.pool;


import entity.Person;
import org.breskul.exception.TableNameNotCorrect;
import org.breskul.model.SettingsForSession;
import org.breskul.session.SessionFactory;
import org.breskul.sessionfactory.entity.Products;
import org.breskul.sessionfactory.entity.Student;
import org.breskul.sessionfactory.entity.TableNotFound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class SessionFactoryTest {

    private static final String url = "jdbc:h2:file:./target/db/testdb";
    private static final String username = "sa";
    private static final String password = "Abcd1234";
    private static final int DEFAULT_POOL_SIZE = 10;

    @Test
    public void findElement() {

        final var pooledDataSource = new PooledDataSource(url, username, password);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        assertNotNull(products);
    }

    @Test
    public void checkCash() {

        final var pooledDataSource = new PooledDataSource(url, username, password);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        final var productsSecond = session.find(Products.class, 1);
        assertEquals(products, productsSecond);
    }

    @Test
    public void checkDifferentObject() {

        final var pooledDataSource = new PooledDataSource(url, username, password);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        final var productsSecond = session.find(Products.class, 2);
        assertNotEquals(products, productsSecond);
    }

    @Test
    public void findElementWithCustomColumnName() {

        final var pooledDataSource = new PooledDataSource(url, username, password);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        final var student = session.find(Student.class, 1);
        assertNotNull(student);
    }

    @Test
    public void checkConnectionSize() {

        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        session.find(Student.class, 1);
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertEquals(DEFAULT_POOL_SIZE, connectionSizeAfterFirstConnection);
    }

    @Test
    public void elementNotFound() {
        var settingsForSession = new SettingsForSession(true, true);
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource, settingsForSession);
        final var session = sessionFactory.createSession();
        final var student = session.find(Student.class, 3);
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertNull(student);
        assertEquals(connectionSizeAfterFirstConnection, DEFAULT_POOL_SIZE);
    }

    @Test
    public void tableNotFound() {

        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        try {
            session.find(TableNotFound.class, 3);
        } catch (TableNameNotCorrect e) {

        }
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertEquals(connectionSizeAfterFirstConnection, DEFAULT_POOL_SIZE);
    }

    @Test
    public void dirtyCheckerSave() {

        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        var student = session.find(Student.class, 1);
        student.setName("Test");
        session.close();
        var studentAfterDirtyCheck = session.find(Student.class, 1);

        assertEquals("Test", studentAfterDirtyCheck.getName());
    }

    @Test
    public void dirtyCheckerSaveWithOffDirtyCheck() {

        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSessionWithProperties(SettingsForSession.createSettingsWithOffDirtyChecker());
        var student = session.find(Student.class, 1);
        student.setName("Mihail");
        session.close();
        var studentAfterDirtyCheck = session.find(Student.class, 1);

        assertEquals("Test", studentAfterDirtyCheck.getName());
    }


    @Test
    public void dirtyCheckerSaveWithOffDirtyCheckColumn() {

        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        var person = session.find(Person.class, 1);
        person.setSurname("Testovich");
        person.setName("Elon");
        session.close();
        var personAfterDirtyCheck = session.find(Person.class, 1);

        assertNotEquals("Testovich", personAfterDirtyCheck.getSurname());
        assertEquals("Elon", personAfterDirtyCheck.getName());
    }
}
