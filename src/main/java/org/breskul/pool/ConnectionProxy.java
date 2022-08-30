package org.breskul.pool;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.sql.Connection;
import java.util.Queue;

/**
 * Overrides method @{@link Connection#close()} so that connection is returned to custom connection pool (queue)
 */
@RequiredArgsConstructor
public class ConnectionProxy implements Connection {

    @Delegate(excludes = Exclude.class)
    private final Connection connection;
    private final Queue<Connection> connectionPool;


    @Override
    public void close() {
        connectionPool.add(this);
    }

    private interface Exclude {
        void close();
    }
}
