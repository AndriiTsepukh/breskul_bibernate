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


    public static SettingsForSession createSettingsWithOffDirtyChecker() {
        return new SettingsForSession(true, false);
    }

    public static SettingsForSession createSettingsWithOffDirtyCheckerAndOffShowSql() {
        return new SettingsForSession(false, false);
    }

    public static SettingsForSession createSettingsWithOffShowSql() {
        return new SettingsForSession(false, true);
    }

}
