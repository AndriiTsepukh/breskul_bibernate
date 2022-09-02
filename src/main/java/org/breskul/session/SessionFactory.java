package org.breskul.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.breskul.pool.PooledDataSource;

@Getter
@Setter
@AllArgsConstructor
public class SessionFactory {

    private PooledDataSource dataSource;
    private boolean showSql = false;

    public SessionFactory(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session createSession() {
        return new Session(dataSource, showSql);
    }
}
