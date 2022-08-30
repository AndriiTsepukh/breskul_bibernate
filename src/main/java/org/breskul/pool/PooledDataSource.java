package org.breskul.pool;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.breskul.exception.BoboException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.breskul.util.Constants.*;

/**
 * Custom implementation of @{@link javax.sql.DataSource} interface with base method implemented.
 * It is initialized once the @{{@link PooledDataSource#getInstance()}}
 * or @{{@link PooledDataSource#getInstance(String, String, String, int)}} is called and always returning the same instance for the following calls.
 * Uses @{{@link Queue}} data structure in order to provide a pool of connections and can be used as a driver-agnostic.
 */
@Slf4j
public class PooledDataSource extends BaseDataSource {
    private final Queue<Connection> connectionPool;
    private final String url;
    private final String username;
    private final String password;
    private final int poolSize;
    private static final int DEFAULT_POOL_SIZE = 10;
    private static PooledDataSource singletonInstance;

    public static PooledDataSource getInstance(String url, String username, String password, int poolSize) {
        if (Objects.nonNull(singletonInstance)) {
            return singletonInstance;
        }
        singletonInstance = createDataSourceInstance(url, username, password, poolSize);
        return singletonInstance;
    }


    public static PooledDataSource getInstance() {
        if (Objects.isNull(singletonInstance)) {
            singletonInstance = createDataSourceInstanceFromProperties();
        }
        return singletonInstance;
    }

    // TODO: think about wait mechanism
    @Override
    public Connection getConnection() {
        return connectionPool.poll();
    }

    @Override
    public Connection getConnection(String username, String password) {
        return this.getConnection();
    }

    public String getDriverName() {
        try {
            return DriverManager.getDriver(this.url).toString();
        } catch (SQLException e) {
            throw new BoboException("Exception occurred when trying to retrieve driverName", e);
        }
    }

    @VisibleForTesting
    PooledDataSource(String url, String username, String password) {
        log.trace("Creating instance of {} with default pool size: {}", getClass().getSimpleName(), DEFAULT_POOL_SIZE);
        checkArguments(url, username, password, DEFAULT_POOL_SIZE);
        this.connectionPool = new ConcurrentLinkedQueue<>();
        this.url = url;
        this.username = username;
        this.password = password;
        this.poolSize = DEFAULT_POOL_SIZE;
        initializePool();
    }

    private PooledDataSource(String url, String username, String password, int poolSize) {
        checkArguments(url, username, password, poolSize);
        log.trace("Creating instance of {} with pool size: {}", getClass().getSimpleName(), DEFAULT_POOL_SIZE);
        this.connectionPool = new ConcurrentLinkedQueue<>();
        this.url = url;
        this.username = username;
        this.password = password;
        this.poolSize = poolSize;
        initializePool();
    }

    private void initializePool() {
        String driverName = Optional.ofNullable(getDriverName()).orElseThrow(() -> new IllegalArgumentException("Cannot retrieve the driver"));
        log.trace("Initializing connection pool. Size: {}. Driver: {}", poolSize, driverName);
        for (int i = 0; i < poolSize; i++) {
            try {
                var connection = new ConnectionProxy(DriverManager.getConnection(url, username, password), connectionPool);
                connectionPool.offer(connection);
            } catch (SQLException e) {
                throw new BoboException("Exception occurred when trying to get connection from DriverManager", e);
            }
        }
    }

    private void checkArguments(String url, String username, String password, int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size cannot be less than 1");
        }

        if (Objects.isNull(url) || Objects.isNull(username) || Objects.isNull(password)) {
            throw new IllegalArgumentException(
                    "Properties: %s, %s, %s must not be null. DataSource has not been initialized.".formatted(URL, USERNAME, PASSWORD));
        }
    }

    private static PooledDataSource createDataSourceInstance(String url, String username, String password, int poolSize) {
        return new PooledDataSource(url, username, password, poolSize);
    }

    private static PooledDataSource createDataSourceInstanceFromProperties() {
        log.trace("Creating data source using {} file", DEFAULT_PROPERTIES_FILE_NAME);
        Properties properties = new PropertyResolver().getProperties();
        int poolSize = Integer.parseInt(properties.getProperty(POOL_SIZE, String.valueOf(DEFAULT_POOL_SIZE)));
        String url = properties.getProperty(URL);
        String username = properties.getProperty(USERNAME);
        String password = properties.getProperty(PASSWORD);
        return new PooledDataSource(url, username, password, poolSize);
    }

}
