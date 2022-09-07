package org.breskul.actions;

import org.breskul.exception.BoboException;
import org.breskul.util.AnnotationsHelper;

import java.sql.Connection;
import java.sql.SQLException;

public class DeleteAction implements Action {
    private Connection connection;
    private Object object;
    public DeleteAction(Connection connection, Object object) {
        this.connection = connection;
        this.object = object;
    }

    @Override
    public void execute() {
        String tableName = AnnotationsHelper.getTableName(object.getClass());
        Long id = AnnotationsHelper.getId(object);
        String idColumnName = AnnotationsHelper.getIdColumnName(object.getClass());

        try {
            var preparedStatement = connection.prepareStatement("DELETE FROM " + tableName +" WHERE " + idColumnName + " = ?");
            preparedStatement.setLong(1, id);
        } catch (SQLException e) {
            throw new BoboException(e.getMessage());
        }
    }
}