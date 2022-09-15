package org.breskul.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Collectors;

@UtilityClass
public class SqlUtil {

    public static final String SELECT_QUERY_TEMPLATE = "SELECT * FROM %s WHERE %s = ?;";
    public static final String UPDATE_QUERY_TEMPLATE = "UPDATE %s SET %s WHERE %s;";
    public static final String INSERT_QUERY_TEMPLATE = "INSERT INTO %s(%s) VALUES(%s);";
    public static final String DELETE_QUERY_TEMPLATE = "DELETE FROM %s WHERE %s = ?;";
    public static String commaSeparatedUpdatableColumns(Class<?> entityType) {
        return Arrays.stream(EntityUtil.getUpdatableFields(entityType))
                .map(EntityUtil::resolveColumnName)
                .map(column -> column + " = ?")
                .collect(Collectors.joining(", "));
    }
}
