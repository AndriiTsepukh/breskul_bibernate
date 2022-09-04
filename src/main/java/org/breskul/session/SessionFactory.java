package org.breskul.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.breskul.model.SettingsForSession;
import org.breskul.pool.PooledDataSource;

@Getter
@Setter
@AllArgsConstructor
public class SessionFactory {

    private PooledDataSource dataSource;
    private SettingsForSession settingsForSession;

    public SessionFactory(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session createSession() {
        settingsForSession = new SettingsForSession();
        return new Session(dataSource, settingsForSession);
    }

    public Session createSessionWithProperties(SettingsForSession settingsForSession) {
        return new Session(dataSource, settingsForSession);
    }
}
