package org.breskul.connectivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class PooledDriverManager {

    Queue<Connection> connectionQueue = new LinkedList<>();
    private final int MAX_CONNECTIONS = 8;

    public PooledDriverManager(String url, String username, String password) {
        for (int i=0; i < MAX_CONNECTIONS; i++){
            try {
                var connection = DriverManager.getConnection(url, username, password);
                ConnectionProxy connectionProxy = new ConnectionProxy(connection, connectionQueue);
                connectionQueue.add(connectionProxy);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    TODO Think about wait mechanism when connection are not available in a queue
    public Connection getConnection() {
        return connectionQueue.peek();
    }
}
