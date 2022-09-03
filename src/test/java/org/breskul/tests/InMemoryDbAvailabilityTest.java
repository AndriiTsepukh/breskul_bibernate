package org.breskul.tests;

import org.breskul.pool.PooledDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

public class InMemoryDbAvailabilityTest {

    private static final String url = "jdbc:h2:file:./target/db/testdb";
    private static final String username = "sa";
    private static final String password = "Abcd1234";

    @BeforeEach
    public void setup() {
        PooledDataSource.reset();
    }
    @Test
    public void pooledConnectionTest() {
        try {
            DataSource dataSource = PooledDataSource.getInstance(url, username, password, 5);
            var connection = dataSource.getConnection();

            var prepareStatement= connection.prepareStatement("SELECT * FROM products;");
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