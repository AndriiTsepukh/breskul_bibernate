package org.breskul.util;

import lombok.extern.slf4j.Slf4j;
import org.breskul.model.SettingsForSession;

@Slf4j
public class LogQueryUtil {

    public static void log(String query, SettingsForSession settings) {
        if (settings.isShowSql()) {
            log.info(query);
        }
    }
}
