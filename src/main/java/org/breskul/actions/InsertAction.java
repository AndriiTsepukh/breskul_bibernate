package org.breskul.actions;

import org.breskul.exception.BoboException;
import org.breskul.util.AnnotationsHelper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class InsertAction implements Action {
    Connection connection;
    Object object;

    public InsertAction(Connection connection, Object object) {
        this.connection = connection;
        this.object = object;
    }

    @Override
    public void execute() {
        String tableName = AnnotationsHelper.getTableName(object);
        var columns = AnnotationsHelper.getColumns(object);
        String columnNames = String.join(",", columns.keySet());
        String valuesPlaceholders = columns.keySet().stream().map(v -> "?").collect(Collectors.joining(","));
        try {
            var preparedStatement = connection.prepareStatement("INSERT INTO " + tableName
                    + " (" + columnNames +") VALUES (" + valuesPlaceholders  + ")", Statement.RETURN_GENERATED_KEYS);
            int i=1;
            for (Map.Entry<String, Object> entry: columns.entrySet()){
                var fieldClass = entry.getValue().getClass();
                if (fieldClass.equals(String.class)) {
                    preparedStatement.setString(i, (String) entry.getValue());
                } else if (fieldClass.equals(Integer.class)) {
                    preparedStatement.setInt(i, (Integer) entry.getValue());
                } else if (fieldClass.equals(Long.class)) {
                    preparedStatement.setLong(i, (Long) entry.getValue());
                } else if (fieldClass.equals(Float.class)) {
                    preparedStatement.setFloat(i, (Float) entry.getValue());
                } else if (fieldClass.equals(Double.class)) {
                    preparedStatement.setDouble(i, (Double) entry.getValue());
                } else if (fieldClass.equals(BigDecimal.class)) {
                    preparedStatement.setBigDecimal(i, (BigDecimal) entry.getValue());
                } else if (fieldClass.equals(Byte.class)) {
                    preparedStatement.setByte(i, (Byte) entry.getValue());
                } else if (fieldClass.equals(byte[].class)) {
                    preparedStatement.setBytes(i, (byte[]) entry.getValue());
                } else if (fieldClass.equals(Boolean.class)) {
                    preparedStatement.setBoolean(i, (Boolean) entry.getValue());
                } else if (fieldClass.equals(Short.class)) {
                    preparedStatement.setShort(i, (Short) entry.getValue());
                } else if (fieldClass.equals(LocalDateTime.class)) {
                    preparedStatement.setDate(i, java.sql.Date.valueOf(((LocalDateTime) entry.getValue()).toLocalDate()));
                } else if (fieldClass.equals(LocalDate.class)) {
                    preparedStatement.setDate(i, java.sql.Date.valueOf((LocalDate) entry.getValue()));
                } else if (fieldClass.equals(Date.class)) {
                    Date date = (Date) entry.getValue();
                    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    preparedStatement.setDate(i, java.sql.Date.valueOf(localDate));
                } else {
                    throw new BoboException("Field with type " + entry.getValue().getClass().getSimpleName() +
                            " and name " + entry.getKey() + " not supported");
                }
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

            var idField = AnnotationsHelper.getIdColumn(object);
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