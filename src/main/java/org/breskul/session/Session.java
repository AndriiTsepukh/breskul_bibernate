package org.breskul.session;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.breskul.actions.Action;
import org.breskul.actions.DeleteAction;
import org.breskul.actions.InsertAction;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;
import org.breskul.exception.BoboException;
import org.breskul.exception.TableNameNotCorrect;
import org.breskul.model.SettingsForSession;
import org.breskul.model.SqlHelper;
import org.h2.jdbc.JdbcSQLNonTransientException;
import org.h2.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static org.breskul.exception.ExceptionMessage.TABLE_NAME_ERROR;
import static org.breskul.util.StringUtils.camelToSnake;

@Data
@Slf4j
public class Session {

    private final DataSource dataSource;
    private final HashMap<EntityKey<?>, Object> entityList = new HashMap<>();
    private final HashMap<EntityKey<?>, Object[]> entityName = new HashMap<>();
    private final HashMap<EntityKey<?>, HashMap<String, String>> fieldToUpdate = new HashMap<>();
    private SettingsForSession settingsForSession;
    private final SqlHelper sqlFields;

    Connection connection;

    Queue<Action> actionQueue = new LinkedList<>();


    public Session(DataSource dataSource, SettingsForSession settingsForSession) {
        this.dataSource = dataSource;
        this.settingsForSession = settingsForSession;
        this.sqlFields = new SqlHelper("application.db.properties");
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new BoboException("Error during getting Session: " + e.getMessage());
        }
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
            log(sql, settingsForSession.isShowSql());
            try (var preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setObject(1, id);
                final var resultSet = preparedStatement.executeQuery();
                final var obj = createObj(entityKeyForObject, resultSet);
                entityList.put(entityKeyForObject, obj);
                return obj;
            } catch (SQLSyntaxErrorException exception) {
                log.error(exception.getMessage());
                throw new TableNameNotCorrect(TABLE_NAME_ERROR);
            } catch (JdbcSQLNonTransientException e) {
                log.error("Object with id {} not found ", id);
                return null;
            }
        } finally {
            connection.close();
        }
    }

    public void persist(Object entity) {
        //        TODO add create or update depends on ID availability
        actionQueue.add(new InsertAction(connection, entity));

    }

    public void remove(Object entity) {
        actionQueue.add(new DeleteAction(connection, entity));
    }

    public void flush() {
        actionQueue.stream().forEach(Action::execute);
    }

    private <T> String createSql(final Class<T> aClass) {
        var value = getTableName(aClass);
        return String.format(sqlFields.getSqlSelect(), value);
    }

    @SneakyThrows
    public <T> T createObj(final EntityKey<T> entityKey, final ResultSet resultSet) {
        resultSet.next();
        final var entity = entityKey.type();
        final var entityObj = entity.getConstructor().newInstance();
        final var declaredFields = Arrays.stream(entity.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);

        var arrFields = new Object[declaredFields.length];

        for (int i = 0; i < declaredFields.length; i++) {
            final var field = declaredFields[i];
            final var fieldName = getFieldName(field);
            field.setAccessible(true);
            final var fieldValue = resultSet.getObject(fieldName);
            field.set(entityObj, fieldValue);
            arrFields[i] = fieldValue;
        }

        entityName.put(entityKey, arrFields);
        return entity.cast(entityObj);
    }


    private String getFieldName(final Field field) {

        if (field.isAnnotationPresent(Column.class))
            return field.getDeclaredAnnotation(Column.class).name();
        else
            return camelToSnake(field.getName());
    }

    private <T> String getTableName(final Class<T> aClass) {

        final var value = aClass.getDeclaredAnnotation(Table.class).value();
        if (StringUtils.isWhitespaceOrEmpty(value)) {
            return camelToSnake(aClass.getSimpleName());
        }
        return value;
    }

    private void updateEntity(final Map.Entry<EntityKey<?>, Object> entityKeyObjectEntry) {
        try (var connection = dataSource.getConnection()) {
            prepare(connection, entityKeyObjectEntry.getKey());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepare(final Connection connection, final EntityKey<?> key) {
        final var sqlUpdate = generatorSql(key);
        try (Statement preparedStatement = connection.createStatement()) {
            preparedStatement.execute(sqlUpdate);
            log(sqlUpdate, settingsForSession.isShowSql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String generatorSql(final EntityKey<?> key) {

        final var entityType = key.type();
        final var tableName = getTableName(entityType);
        var sqlForUpdate = String.format(sqlFields.getUpdateSet(), tableName);
        final var stringStringMap = fieldToUpdate.get(key);

        for (var stringStringEntry : stringStringMap.entrySet()) {
            if (stringStringEntry.getKey().equals(sqlFields.getFieldId())) {
                continue;
            }
            sqlForUpdate += stringStringEntry.getKey() + " = " + "'" + stringStringEntry.getValue() + "' ,";
        }
        sqlForUpdate = sqlForUpdate.substring(0, sqlForUpdate.length() - 1);
        sqlForUpdate += sqlFields.getWhereParam() + stringStringMap.get(sqlFields.getFieldId());

        return sqlForUpdate;
    }

    @SneakyThrows
    private boolean checkChange(final Map.Entry<EntityKey<?>, Object> en) {
        var readyToUpdate = false;
        final var stringStringMap = new HashMap<String, String>();
        final var entity = en.getValue();
        final var entityType = entity.getClass();
        final var currentFields = Arrays.stream(entityType.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);

        final var snapFields = entityName.get(en.getKey());
        for (int i = 0; i < currentFields.length; i++) {
            final var currentField = currentFields[i];
            currentField.setAccessible(true);
            if (currentField.isAnnotationPresent(Id.class)) {
                stringStringMap.put(sqlFields.getFieldId(), currentField.get(entity).toString());
            }

            if (!currentField.get(entity).equals(snapFields[i]) && checkParamInAnnotationColumn(currentField)) {
                readyToUpdate = true;
                stringStringMap.put(getFieldName(currentField), currentField.get(entity).toString());
            }
        }

        if (readyToUpdate)
            fieldToUpdate.put(en.getKey(), stringStringMap);

        return readyToUpdate;
    }

    private boolean checkParamInAnnotationColumn(final Field currentField) {
        if (!currentField.isAnnotationPresent(Column.class)) {
            return true;
        } else return !currentField.getDeclaredAnnotation(Column.class).ignoreDirtyCheck();
    }


    public void close() {
        flush();
        if (settingsForSession.isEnableDirtyChecker()) {
            entityList.entrySet().stream()
                    .filter(this::checkChange)
                    .forEach(this::updateEntity);
        }
        sessionClear();
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(final String message, final boolean showSql) {
        if (showSql)
            log.info(message);
    }

    private void sessionClear() {
        entityList.clear();
        entityName.clear();
        fieldToUpdate.clear();
    }
}
