package org.breskul.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsForSession {

    private boolean showSql = true;
    private boolean enableDirtyChecker = true;

    public SettingsForSession( SettingsForSessionBuilder settingsForSessionBuilder) {
        this.showSql = settingsForSessionBuilder.showSql;
        this.enableDirtyChecker = settingsForSessionBuilder.enableDirtyChecker;
    }

    public static class SettingsForSessionBuilder {
        private boolean showSql;
        private boolean enableDirtyChecker;

        private SettingsForSessionBuilder() {
        }

        public static SettingsForSessionBuilder aSettingsForSession() {
            return new SettingsForSessionBuilder();
        }

        public SettingsForSessionBuilder withShowSql(boolean showSql) {
            this.showSql = showSql;
            return this;
        }

        public SettingsForSessionBuilder withEnableDirtyChecker(boolean enableDirtyChecker) {
            this.enableDirtyChecker = enableDirtyChecker;
            return this;
        }

        public SettingsForSessionBuilder but() {
            return aSettingsForSession().withShowSql(showSql).withEnableDirtyChecker(enableDirtyChecker);
        }

        public SettingsForSession build() {
            SettingsForSession settingsForSession = new SettingsForSession();
            settingsForSession.setShowSql(showSql);
            settingsForSession.setEnableDirtyChecker(enableDirtyChecker);
            return settingsForSession;
        }
    }
}
