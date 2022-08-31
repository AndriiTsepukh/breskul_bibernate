package org.breskul.pool;

import org.breskul.pool.PooledDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class InMemoryDbAvailabilityTest {

    private static final String url = "jdbc:h2:file:./target/db/testdb";
    private static final String username = "sa";
    private static final String password = "Abcd1234";

    @Test
    public void pooledConnectionTest() {
        try {
            var connection = new PooledDataSource(url, username, password).getConnection();

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
