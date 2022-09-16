package org.breskul.tests;

import org.breskul.exception.BoboException;
import org.breskul.model.SettingsForSession;
import org.breskul.pool.PooledDataSource;
import org.breskul.session.SessionFactory;
import org.breskul.testdata.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertTrue(products == productsSecond);
    }

    @Test
    public void checkCashAfterCloseSession() {

        final var pooledDataSource = PooledDataSource.getInstance(url, username, password, 5);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        var session = sessionFactory.createSession();
        final var products = session.find(Products.class, 1);
        session = sessionFactory.createSession();
        var productsSecond = session.find(Products.class, 1);
        assertTrue(products != productsSecond);
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
        var settingsForSession = new SettingsForSession(true, true);
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource, settingsForSession);
        final var session = sessionFactory.createSession();
        final var student = session.find(Student.class, 333);
        final var connectionSizeAfterFirstConnection = pooledDataSource.checkConnectionPoolSize();
        assertNull(student);
        assertEquals(DEFAULT_POOL_SIZE, connectionSizeAfterFirstConnection);
    }

    @Test
    public void tableNotFoundTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();
        assertThrows(BoboException.class, () -> session.find(TableNotFound.class, 3));
    }

    @Test
    public void persistActionTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();

        TestEntity testEntity = new TestEntity();
        testEntity.firstName = "TestFirstName";
        testEntity.lastName = "TestLastName";

        session.persist(testEntity);
        session.flush();

        var id = testEntity.id;

        var foundEntity = session.find(TestEntity.class, id);

        assertEquals("TestFirstName", foundEntity.firstName);
        assertEquals("TestLastName", foundEntity.lastName);
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
        final var session = sessionFactory.createSessionWithProperties(SettingsForSession.builder()
                .enableDirtyChecker(false)
                .build());

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

    @Test
    public void deleteActionTest() {
        final var pooledDataSource =  PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();

        Student student = new Student();
        student.setId(2L);
        session.remove(student);
        session.flush();
        final var deletedStudent = session.find(Student.class, 2);
        assertNull(deletedStudent);
        final var existingStudent = session.find(Student.class, 1);
        assertNotNull(existingStudent);
    }


    @Test
    public void check() {

        final var pooledDataSource = PooledDataSource.getInstance(url, username, password, DEFAULT_POOL_SIZE);
        final var sessionFactory = new SessionFactory(pooledDataSource);
        final var session = sessionFactory.createSession();

        session.remove(findStudent(3L));
        session.remove(findStudent(4L));
        session.remove(findStudent(5L));
        session.remove(findStudent(6L));
        session.remove(findStudent(7L));

        var studentSecond = new Student();
        studentSecond.setSecondName("Gomenyuk");
        studentSecond.setName("Gleb");
        session.persist(studentSecond);

        session.flush();
    }


    Student findStudent(Long id) {
        var student = new Student();
        student.setId(id);
        return student;
    }

}
