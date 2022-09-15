package org.breskul.actions;

import lombok.AllArgsConstructor;
import org.breskul.exception.BoboException;
import org.breskul.model.SettingsForSession;
import org.breskul.util.AnnotationsHelper;
import org.breskul.util.EntityUtil;
import org.breskul.util.LogQueryUtil;
import org.breskul.util.SqlUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class DeleteAction implements Action {
    private DataSource dataSource;
    private Object entity;
    private SettingsForSession settings;

    @Override
    public void execute() {
        String tableName = EntityUtil.readTableName(entity.getClass());
        Long id = AnnotationsHelper.getId(entity);
        String idColumnName = AnnotationsHelper.getIdColumnName(entity.getClass());

        try (Connection connection = dataSource.getConnection()) {
            String deleteQuery = String.format(SqlUtil.DELETE_QUERY_TEMPLATE, tableName, idColumnName);
            LogQueryUtil.log(deleteQuery, settings);

            try (var preparedStatement = connection.prepareStatement(deleteQuery)) {
                preparedStatement.setLong(1, id);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new BoboException("Failed to execute DeleteAction", e);
            }
        } catch (SQLException e) {
            throw new BoboException("Failed to execute DeleteAction", e);
        }
    }

    @Override
    public int priority() {
        return 3;
    }
}