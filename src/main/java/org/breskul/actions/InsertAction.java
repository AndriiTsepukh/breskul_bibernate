package org.breskul.actions;

import org.breskul.exception.BoboException;
import org.breskul.util.AnnotationsHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertAction implements Action {
    private Connection connection;
    private Object object;

    public InsertAction(Connection connection, Object object) {
        this.connection = connection;
        this.object = object;
    }

    @Override
    public void execute() {
        String tableName = AnnotationsHelper.getTableName(object.getClass());
        var columns = AnnotationsHelper.getColumns(object);
        String columnNames = String.join(",", columns.keySet());
        String valuesPlaceholders = columns.keySet().stream().map(v -> "?").collect(Collectors.joining(","));
        try {
            var preparedStatement = connection.prepareStatement("INSERT INTO " + tableName
                    + " (" + columnNames +") VALUES (" + valuesPlaceholders  + ")", Statement.RETURN_GENERATED_KEYS);
            int i=1;
            for (Map.Entry<String, Object> entry: columns.entrySet()){
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

            var idField = AnnotationsHelper.getIdColumn(object.getClass());
            idField.setAccessible(true);
            idField.set(object, id);
            connection.commit();
        } catch (SQLException | IllegalAccessException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new BoboException(ex.getMessage());
            }
            throw new BoboException(e.getMessage());
        }
    }
}