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

    public Session createSession() {
        return new Session(dataSource);
    }
}
