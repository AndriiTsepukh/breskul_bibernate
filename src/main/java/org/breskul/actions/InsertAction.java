package org.breskul.actions;

import lombok.AllArgsConstructor;
import org.breskul.exception.BoboException;
import org.breskul.model.SettingsForSession;
import org.breskul.util.AnnotationsHelper;
import org.breskul.util.EntityUtil;
import org.breskul.util.LogQueryUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Collectors;

import static org.breskul.util.SqlUtil.INSERT_QUERY_TEMPLATE;

@AllArgsConstructor
public class InsertAction implements Action {
    private DataSource dataSource;
    private Object entity;

    private SettingsForSession settings;


    @Override
    public void execute() {
        String tableName = EntityUtil.readTableName(entity.getClass());
        var columns = AnnotationsHelper.getColumns(entity);

        String columnNames = String.join(",", columns.keySet());
        String valuesPlaceholders = columns.keySet().stream().map(v -> "?").collect(Collectors.joining(","));

        try (Connection connection = dataSource.getConnection()) {

            String insertQuery = String.format(INSERT_QUERY_TEMPLATE, tableName, columnNames, valuesPlaceholders);
            LogQueryUtil.log(insertQuery, settings);

            try (var preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                int i = 1;
                for (Map.Entry<String, Object> entry : columns.entrySet()) {
                    preparedStatement.setObject(i, entry.getValue());
                    i++;
                }
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    throw new BoboException("INSERT operation failed. Number of affected rows: 0");
                }
                var keys = preparedStatement.getGeneratedKeys();
                Long id;
                if (keys.next()) {
                    id = keys.getLong(1);
                } else {
                    throw new BoboException("INSERT operation failed. No id obtained.");
                }
                var idField = AnnotationsHelper.getIdColumn(entity.getClass());
                idField.setAccessible(true);
                idField.set(entity, id);
                connection.commit();
            } catch (SQLException | IllegalAccessException e) {
                connection.rollback();
                throw new BoboException("Failed to execute InsertAction", e);
            }
        } catch (SQLException e) {
            throw new BoboException("Failed to execute InsertAction", e);
        }
    }

    @Override
    public int priority() {
        return 1;
    }
}