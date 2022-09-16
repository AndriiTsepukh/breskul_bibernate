package org.breskul.session;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.breskul.actions.Action;
import org.breskul.actions.DeleteAction;
import org.breskul.actions.InsertAction;
import org.breskul.actions.UpdateAction;
import org.breskul.exception.BoboException;
import org.breskul.model.SettingsForSession;
//import org.breskul.model.SqlHelper;
import org.breskul.util.EntityUtil;
import org.breskul.util.LogQueryUtil;
import org.breskul.util.SqlUtil;
import org.h2.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

import static java.util.Comparator.comparing;
import static org.breskul.util.EntityUtil.getIdValue;
import static org.breskul.util.StringUtils.camelToSnake;

@Data
@Slf4j
public class Session {

    private final DataSource dataSource;
    private final Map<EntityKey<?>, Object> entitiesByKey = new HashMap<>();
    private final Map<EntityKey<?>, Object[]> entitiesSnapshot = new HashMap<>();
    private Queue<Action> actionQueue = new PriorityQueue<>(comparing(Action::priority));
    private SettingsForSession settingsForSession;
    private final SqlHelper sqlFields;

    public Session(DataSource dataSource, SettingsForSession settingsForSession) {
        this.dataSource = dataSource;
        this.settingsForSession = settingsForSession;
    }

    @SneakyThrows
    public <T> T find(Class<T> entityType, Object id) {
        Objects.requireNonNull(entityType);
        Objects.requireNonNull(id);

        log.info("Finding entity {} by id = {}", entityType.getSimpleName(), id);
        var entityKey = new EntityKey<>(entityType, id);

        if (entitiesByKey.containsKey(entityKey)) {
            Object entity = entitiesByKey.get(entityKey);
            log.trace("Returning cached instance of entity from the context {}", entity);
            return entityType.cast(entity);
        }

        log.trace("No cached entity found. Loading entity from the DB...");
        return Optional.ofNullable(findByKey(entityKey))
                .map(entity -> {
                    entitiesByKey.put(entityKey, entity);
                    putEntitySnapshot(entityKey, entity);
                    return entity;
                })
                .orElse(null);
    }

    public void persist(Object entity) {
        Optional.ofNullable(getIdValue(entity))
                .ifPresentOrElse(id -> actionQueue.add(new UpdateAction(dataSource, entity, settingsForSession)), () -> actionQueue.add(new InsertAction(dataSource, entity, settingsForSession)));
    }

    public void remove(Object entity) {
        actionQueue.add(new DeleteAction(dataSource, entity, settingsForSession));
    }

    public void flush() {
        log.trace("Session flush...");
        dirtyCheck();
        flushActionQueue();
    }

    public void flush() {
        actionQueue.stream()
                .sorted(Comparator.comparing(x -> x.getActionPriority().priority))
                .forEach(action -> action.execute(settingsForSession.isShowSql()));
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
        log.info("Closing session...");
        flush();
        sessionClear();
    }

    private void dirtyCheck() {
        if (settingsForSession.isEnableDirtyChecker()) {
            List<UpdateAction> updateActions = getDirtyEntities().stream()
                    .peek(entity -> log.trace("Creating UpdateAction for entity {}", entity))
                    .map(entity -> new UpdateAction(dataSource, entity, settingsForSession))
                    .toList();
            actionQueue.addAll(updateActions);
        }
    }


    private void putEntitySnapshot(EntityKey<?> entityKey, Object entity) {
        Object[] values = EntityUtil.entityToSnapshot(entity);
        entitiesSnapshot.put(entityKey, values);
    }

    public List<?> getDirtyEntities() {
        log.trace("Searching for dirty entities...");
        var list = new ArrayList<>();
        entitiesByKey.entrySet().stream()
                .forEach(entry -> {
                    var currentEntity = entry.getValue();
                    var currentEntitySnapshot = EntityUtil.entityToSnapshot(currentEntity);
                    var initialSnapshot = entitiesSnapshot.get(entry.getKey());
                    log.trace("Comparing snapshots: {} <=> {}", initialSnapshot, currentEntitySnapshot);
                    if (!Arrays.equals(currentEntitySnapshot, initialSnapshot)) {
                        log.trace("Found dirty entity {}", currentEntity);
                        log.trace("Initial snapshot {}", initialSnapshot);
                        list.add(currentEntity);
                    }
                });
        return list;
    }

    private void flushActionQueue() {
        log.info("Flushing ActionQueue...");
        while (!actionQueue.isEmpty()) {
            var entityAction = actionQueue.poll();
            entityAction.execute();
        }
    }

    private <T> T findByKey(EntityKey<T> entityKey) {
        String tableName = EntityUtil.readTableName(entityKey.type());
        String idField = EntityUtil.getIdFieldName(entityKey.type());
        String query = String.format(SqlUtil.SELECT_QUERY_TEMPLATE, tableName, idField);
        LogQueryUtil.log(query, settingsForSession);
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.setObject(1, entityKey.id());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                log.trace("Not found entity {} by id: {}", entityKey.type().getSimpleName(), entityKey.id());
                return null;
            }
            return EntityUtil.createEntityFromResultSet(entityKey.type(), resultSet);
        } catch (SQLException e) {
            throw new BoboException("Exception occurred when trying to execute SELECT query", e);
        }
    }


    private void sessionClear() {
        entitiesByKey.clear();
        entitiesSnapshot.clear();
    }
}
