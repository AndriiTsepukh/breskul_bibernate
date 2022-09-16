package org.breskul.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingsForSession {

    private boolean showSql = true;
    private boolean enableDirtyChecker = true;

}
