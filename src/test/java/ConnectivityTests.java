import org.breskul.connectivity.PooledDriverManager;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class ConnectivityTests {

    private static final String url = "jdbc:postgresql://localhost:5432/postgres";
    private static final String username = "postgres";
    private static final String password = "Abcd1234";

//    TODO need to be refactored after adding in-memory DB setup
    @Test
    public void pooledConnectionTest() {
        try {
            var connection = new PooledDriverManager(url, username, password).getConnection();

            var prepareStatement= connection.prepareStatement("SELECT * FROM products;");
            var resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int price = resultSet.getInt("price");

                System.out.printf("Product with: id=%d, name=%s, price=%d\r\n", id, name, price);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
