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

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static java.util.Comparator.comparing;
import static org.breskul.util.EntityUtil.getIdValue;

@Data
@Slf4j
public class Session {

    private final DataSource dataSource;
    private final Map<EntityKey<?>, Object> entitiesByKey = new HashMap<>();
    private final Map<EntityKey<?>, Object[]> entitiesSnapshot = new HashMap<>();
    private Queue<Action> actionQueue = new PriorityQueue<>(comparing(Action::priority));
    private SettingsForSession settingsForSession;


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
