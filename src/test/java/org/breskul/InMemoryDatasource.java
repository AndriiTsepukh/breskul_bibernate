package org.breskul;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public abstract class InMemoryDatasource {

    protected DataSource getDatasource() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:file:./target/db/testdb");
        dataSource.setUser("sa");
        dataSource.setPassword("Abcd1234");
        return dataSource;
    }

}
