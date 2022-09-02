package org.breskul.model;

import lombok.Data;
import org.breskul.pool.PropertyResolver;

@Data
public class SqlHelper {

    private String sqlSelect;
    private String whereParam;
    private String updateSet;
    private String fieldId;

    public SqlHelper(String propertiesName) {
        final var properties = new PropertyResolver(propertiesName).getProperties();

        this.sqlSelect = properties.getProperty("sql_select");
        this.whereParam = properties.getProperty("where");
        this.updateSet = properties.getProperty("update");
        this.fieldId = properties.getProperty("fieldId");
    }
}
