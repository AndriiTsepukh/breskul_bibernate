package org.breskul.session;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Table;
import org.breskul.exception.ErrorWithEnyColumn;
import org.breskul.exception.TableNameNotCorrect;
import org.breskul.pool.PropertyResolver;
import org.h2.jdbc.JdbcSQLNonTransientException;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static org.breskul.exception.ExceptionMessage.COLUMN_ERROR;
import static org.breskul.exception.ExceptionMessage.TABLE_NAME_ERROR;
import static org.breskul.util.StringUtils.camelToSnake;

@Data
@Slf4j
public class Session {

    private final DataSource dataSource;
    private static String sqlSelect;
    private final HashMap<EntityKey<?>, Object> entityList = new HashMap<>();
    private boolean showSql;


    public Session(DataSource dataSource, boolean showSql) {
        this.dataSource = dataSource;
        final var properties = new PropertyResolver("application.db.properties").getProperties();
        sqlSelect = properties.getProperty("sql_select");
        this.showSql = showSql;
    }

    @SneakyThrows
    public <T> T find(final Class<T> classType, final Object id) {
        Objects.requireNonNull(classType);
        Objects.requireNonNull(id);

        final var entityKeyForObject = new EntityKey<>(classType, id);
        if (entityList.containsKey(entityKeyForObject))
            return classType.cast(entityList.get(entityKeyForObject));
        try (var connection = dataSource.getConnection()) {
            final var sql = createSql(classType);
            if (showSql) {
                log.info(sql);
            }
            try (var preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setObject(1, id);
                final var resultSet = preparedStatement.executeQuery();
                final var obj = createObj(entityKeyForObject, resultSet);
                entityList.put(entityKeyForObject, obj);
                return obj;
            } catch (SQLSyntaxErrorException exception) {
                log.error("Table {} dose not exist ", getTableName(classType));
                throw new TableNameNotCorrect(TABLE_NAME_ERROR);
            } catch (JdbcSQLNonTransientException e){
                log.error("Object with id {} not found ", id);
                return null;
            }
        }
    }

    private <T> String createSql(final Class<T> aClass) {
        var value = getTableName(aClass);
        return String.format(sqlSelect, value);
    }

    @SneakyThrows
    public <T> T createObj(final EntityKey<T> entityKey, final ResultSet resultSet) {
        resultSet.next();
        final var entity = entityKey.type();
        final var entityObj = entity.getConstructor().newInstance();
        final var declaredFields = Arrays.stream(entity.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);

        for (int i = 0; i < declaredFields.length; i++) {
            final var field = declaredFields[i];
            final var fieldName = getFieldName(field);
            field.setAccessible(true);
                final var fieldValue = resultSet.getObject(fieldName);
                field.set(entityObj, fieldValue);
        }

        return entity.cast(entityObj);
    }


    private String getFieldName(final Field field) {

        if (field.isAnnotationPresent(Column.class))
            return field.getDeclaredAnnotation(Column.class).name();
        else
            return camelToSnake(field.getName());
    }

    private <T> String getTableName(final Class<T> aClass) {

        var value = aClass.getDeclaredAnnotation(Table.class).value();
        if (value.equals("")) {
            return camelToSnake(aClass.getSimpleName());
        }
        return value;
    }

    public void close() {

    }
}
