package connectivity;

import org.breskul.connectivity.PooledDriverManager;
import org.breskul.connectivity.datasource.FileConfigurableDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class ConnectivityTests {


    //    TODO need to be refactored after adding in-memory DB setup
    @Test
    public void pooledConnectionTest() {
        try {
            var connection = new PooledDriverManager(FileConfigurableDataSource.getInstance()).getConnection();

            var prepareStatement = connection.prepareStatement("SELECT * FROM products;");
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
