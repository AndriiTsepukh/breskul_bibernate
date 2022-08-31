package org.breskul.pool;


import org.breskul.pool.PooledDataSource;
import org.breskul.session.Session;
import org.breskul.session.SessionFactory;
import org.breskul.sessionfactory.entity.Products;
import org.breskul.sessionfactory.entity.Student;
import org.junit.jupiter.api.Test;
import org.breskul.pool.PooledDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class SessionFactoryTets {

    private static final String url = "jdbc:h2:file:./target/db/testdb";
    private static final String username = "sa";
    private static final String password = "Abcd1234";

    @Test
    public void findElement() {

        var pooledDataSource = new PooledDataSource(url, username, password);
        var sessionFactory = new SessionFactory(pooledDataSource);
        var session = sessionFactory.createSession();
        var products = session.find(Products.class, 1);
        assertNotNull(products);
    }

    @Test
    public void checkCash() {

        var pooledDataSource = new PooledDataSource(url, username, password);
        var sessionFactory = new SessionFactory(pooledDataSource);
        var session = sessionFactory.createSession();
        var products = session.find(Products.class, 1);
        var productsSecond = session.find(Products.class, 1);
        assertEquals(products, productsSecond);
    }

    @Test
    public void findElementWithCustomColumnName() {

        var pooledDataSource = new PooledDataSource(url, username, password);
        var sessionFactory = new SessionFactory(pooledDataSource);
        var session = sessionFactory.createSession();
        var student = session.find(Student.class, 1);
        assertNotNull(student);
    }
}
