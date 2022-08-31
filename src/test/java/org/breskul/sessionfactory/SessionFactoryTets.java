//package org.breskul.sessionfactory;
//
//
//import org.breskul.pool.PooledDataSource;
//import org.breskul.session.Session;
//import org.breskul.session.SessionFactory;
//import org.breskul.sessionfactory.entity.Products;
//import org.junit.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class SessionFactoryTets {
//    @Test
//    public void existAnnotationTable() {
//
//        var pooledDataSource = PooledDataSource.getInstance();
//        var sessionFactory = new SessionFactory(pooledDataSource);
//        var session = sessionFactory.createSession();
//        session.find(Products.class, 1);
//    }
//}
