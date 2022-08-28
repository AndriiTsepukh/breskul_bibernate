package org.breskul.connectivity.datasource;

import org.breskul.exception.BoboException;
import org.breskul.exception.ExceptionMessage;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

public class FileConfigurableDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;
    private static FileConfigurableDataSource singletonInstance;

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(singletonInstance.url, singletonInstance.username, singletonInstance.password);
        } catch (SQLException e) {
            throw new BoboException(ExceptionMessage.GET_CONNECTION_EXCEPTION);
        }
    }

    @Override
    public Connection getConnection(String username, String password) {
        try {
            return DriverManager.getConnection(singletonInstance.url, username, password);
        } catch (SQLException e) {
            throw new BoboException(ExceptionMessage.GET_CONNECTION_EXCEPTION);
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException();

    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    public static FileConfigurableDataSource getInstance(String url, String username, String password) {
        if (Objects.nonNull(singletonInstance)) {
            return singletonInstance;
        }
        singletonInstance = new FileConfigurableDataSource(url, username, password);
        return singletonInstance;
    }

    public static FileConfigurableDataSource getInstance() {
        return singletonInstance;
    }

    private FileConfigurableDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}
