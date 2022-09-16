package org.breskul.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.breskul.exception.BoboException;
import org.breskul.model.SettingsForSession;
import org.breskul.util.LogQueryUtil;
import org.breskul.util.SqlUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.breskul.util.EntityUtil.*;

@Slf4j
@AllArgsConstructor
public class UpdateAction implements Action {

    private DataSource dataSource;
    private Object entity;
    private SettingsForSession settings;

    @Override
    public void execute() {
        log.trace("Updating entity {}", entity);
        var entityType = entity.getClass();

        var tableName = readTableName(entityType);
        log.trace("Resolved table name -> {}", tableName);

        var updatableColumns = SqlUtil.commaSeparatedUpdatableColumns(entityType);
        var idColumn = getIdFieldName(entityType).concat(" = ?");

        var updateQuery = String.format(SqlUtil.UPDATE_QUERY_TEMPLATE, tableName, updatableColumns, idColumn);
        LogQueryUtil.log(updateQuery, settings);

        try (Connection connection = dataSource.getConnection()) {
            try (var updateStatement = connection.prepareStatement(updateQuery)) {
                fillUpdateStatementParams(updateStatement, entity);
                var idParamIndex = getUpdatableFields(entityType).length + 1;
                updateStatement.setObject(idParamIndex, getIdValue(entity));
                updateStatement.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw new BoboException("Failed to execute UpdateAction", e);
            }
        } catch (SQLException e) {
            throw new BoboException("Failed to execute UpdateAction", e);
        }
    }

    private <T> void fillUpdateStatementParams(PreparedStatement updateStatement, T entity) {
        var updatableFields = getUpdatableFields(entity.getClass());
        setParamsFromFields(updateStatement, entity, updatableFields);
    }

    private void setParamsFromFields(PreparedStatement statement, Object entity, Field[] fields) {
        try {
            for (int i = 0; i < fields.length; i++) {
                var field = fields[i];
                field.setAccessible(true);
                var columnValue = field.get(entity);
                statement.setObject(i + 1, columnValue);
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new BoboException("Failed to set parameters for UpdateAction", e);
        }
    }

    @Override
    public ActionPriority getActionPriority() {
        return ActionPriority.UPDATE_PRIORITY;
    }
}
