package connectivity.datasource;

import org.breskul.connectivity.datasource.DataSourcePropertyResolver;
import org.breskul.connectivity.datasource.FileConfigurableDataSource;
import org.breskul.exception.BoboException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DataSourceTest {
    private static final String url = "jdbc:postgresql://localhost:5432/postgres";
    private static final String username = "postgres";
    private static final String password = "Abcd1234";

    @Test
    @DisplayName("Initialize data source from file")
    void shouldInitializeDataSourceFromFile() {
        DataSourcePropertyResolver dataSourcePropertyResolver = new DataSourcePropertyResolver();
        FileConfigurableDataSource instance = FileConfigurableDataSource.getInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Throws BobocodeException when file doesn't exists")
    void shouldThrowExceptionWhenFileDoesNotExists() {
        assertThrows(BoboException.class, () -> new DataSourcePropertyResolver("application-throw-exception.properties"));
    }

    @Test
    @DisplayName("DataSource is null when properties doesn't specified")
    void shouldBeNullIfPropertiesDoesNotSpecified() {
        DataSourcePropertyResolver dataSourcePropertyResolver = new DataSourcePropertyResolver("application-test.properties");
        assertNull(FileConfigurableDataSource.getInstance());
    }

    @Test
    @DisplayName("Connect via provided properties")
    void shouldConnectUsingProvidedProperties() throws SQLException {
        DataSource dataSource = FileConfigurableDataSource.getInstance(url, username, password);
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());
    }
}
