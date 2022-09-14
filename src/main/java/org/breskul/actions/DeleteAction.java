package org.breskul.actions;

import lombok.extern.slf4j.Slf4j;
import org.breskul.exception.BoboException;
import org.breskul.util.AnnotationsHelper;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DeleteAction implements Action {
    private Connection connection;
    private Object object;
    public DeleteAction(Connection connection, Object object) {
        this.connection = connection;
        this.object = object;
    }

    @Override
    public void execute(boolean showSql) {
        String tableName = AnnotationsHelper.getTableName(object.getClass());
        Long id = AnnotationsHelper.getId(object);
        String idColumnName = AnnotationsHelper.getIdColumnName(object.getClass());

        try {
            var sqlDelete = "DELETE FROM " + tableName + " WHERE " + idColumnName + " = ?";
            var preparedStatement = connection.prepareStatement(sqlDelete);
            log(sqlDelete, showSql);
            preparedStatement.setLong(1, id);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new BoboException(e.getMessage());
        }
    }

    @Override
    public ActionPriority getActionPriority() {
        return ActionPriority.DELETE;
    }

    private void log(final String message, final boolean showSql) {
        if (showSql)
            log.info(message);
    }
}