package org.breskul.session;

import lombok.Data;
import lombok.SneakyThrows;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Table;
import org.breskul.pool.PropertyResolver;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

@Data
public class Session {

    private final DataSource dataSource;
    private static String sqlSelect;
    private final HashMap<EntityKey<?>, Object> entityList = new HashMap<>();


    public Session(DataSource dataSource) {
        this.dataSource = dataSource;
        Properties properties = new PropertyResolver("application.db.properties").getProperties();
        sqlSelect = properties.getProperty("sql_select");
    }


    @SneakyThrows
    public <T> T find(final Class<T> classType, final Object id) {
        Objects.requireNonNull(classType);
        Objects.requireNonNull(id);
        final var entityKeyForObject = new EntityKey<>(classType, id);
        if (entityList.containsKey(entityKeyForObject))
            return classType.cast(entityList.get(entityKeyForObject));

        final var connection = dataSource.getConnection();
        final var sql = createSql(classType);
        var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setObject(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        final var obj = createObj(entityKeyForObject, resultSet);
        entityList.put(entityKeyForObject, obj);
        connection.close();
        return obj;
}

    private <T> String createSql(Class<T> aClass) {
        String value = aClass.getDeclaredAnnotation(Table.class).value();
        return String.format(sqlSelect, value);
    }

    public <T> T createObj(EntityKey<T> entityKey, ResultSet resultSet) throws InvocationTargetException, InstantiationException, IllegalAccessException, SQLException, NoSuchMethodException {
        resultSet.next();
        Class<T> entity = entityKey.type();
        Object entityObj = entity.getConstructor().newInstance();
        Field[] declaredFields = Arrays.stream(entity.getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .toArray(Field[]::new);
        Object[] arr = new Object[declaredFields.length];

        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            String declaredAnnotation = field.getDeclaredAnnotation(Column.class).name();
            field.setAccessible(true);
            Object fieldValue = resultSet.getObject(declaredAnnotation);
            field.set(entityObj, fieldValue);
            arr[i] = fieldValue;
        }

        return entity.cast(entityObj);
    }
}
