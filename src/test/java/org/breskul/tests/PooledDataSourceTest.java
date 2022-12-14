package org.breskul.tests;

import org.breskul.pool.PooledDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PooledDataSourceTest {
    private static final String url = "jdbc:h2:file:./target/db/testdb";
    private static final String username = "sa";
    private static final String password = "Abcd1234";

    @BeforeEach
    public void setup() {
        PooledDataSource.reset();
    }

    @Test
    @DisplayName("Connect via provided properties")
    void connectUsingProvidedProperties() throws SQLException {
        DataSource dataSource = PooledDataSource.getInstance(url, username, password, 5);
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());
    }


    @Test
    @DisplayName("Connect via properties file")
    void connectUsingPropertyFile() throws SQLException {
        DataSource dataSource = PooledDataSource.getInstance();
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());
    }

    @Test
    @DisplayName("Initializes connection pool")
    public void pooledConnectionTest() {
        try {
            PooledDataSource dataSource = PooledDataSource.getInstance();
            Connection connection = dataSource.getConnection();
            var prepareStatement = connection.prepareStatement("SELECT * FROM products;");
            var resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");

                System.out.printf("Product with: id=%d, name=%s\r\n", id, name);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
