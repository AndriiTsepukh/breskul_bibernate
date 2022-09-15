package org.breskul.util;

import lombok.experimental.UtilityClass;
import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;
import org.breskul.exception.BoboException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class EntityUtil {

    public static <T> String readTableName(Class<T> entityType) {
        return Optional.ofNullable(entityType.getAnnotation(Table.class))
                .map(Table::value)
                .filter(Predicate.not(String::isEmpty))
                .orElseGet(() -> StringUtils.camelToSnake(entityType.getSimpleName()));
    }

    public static <T> String getIdFieldName(Class<T> entityType) {
        Field idField = getIdField(entityType);
        return idField.isAnnotationPresent(Column.class) ? idField.getAnnotation(Column.class).name() : idField.getName();
    }

    public static <T> Field getIdField(Class<T> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(EntityUtil::isIdField)
                .findAny()
                .orElseThrow(() -> new BoboException("Cannot find a field marked with @Id in class " + entityType.getSimpleName()));
    }

    public static Object getIdValue(Object entity) {
        var entityType = entity.getClass();
        var idField = getIdField(entityType);
        idField.setAccessible(true);
        return getValueFromField(idField, entity);
    }

    public static String resolveColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .filter(Predicate.not(String::isEmpty))
                .orElseGet(() ->  StringUtils.camelToSnake(field.getName()));
    }

    public static Object[] entityToSnapshot(Object entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .sorted(Comparator.comparing(Field::getName))
                .map(field -> EntityUtil.getValueFromField(field, entity))
                .toArray();
    }

    public static Object getValueFromField(Field field, Object entity){
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new BoboException("Failed to retrieve value from field", e);
        }

    }

    public static Field[] getUpdatableFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(f -> !isIdField(f) && !isIgnoreDirtyCheck(f))
                .toArray(Field[]::new);
    }

    private static boolean isIgnoreDirtyCheck(Field field) {
        return field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).ignoreDirtyCheck();
    }

    public static <T> T createEntityFromResultSet(Class<T> entityType, ResultSet resultSet) {
        try {
            var constructor = entityType.getConstructor();
            T entity = constructor.newInstance();
            for (Field field : entityType.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = resolveColumnName(field);
                Object columnValue = resultSet.getObject(columnName);
                field.set(entity, columnValue);
            }
            return entity;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException |
                 SQLException e) {
            throw new BoboException("Cannot create instance of class %s when parsing result set ".formatted(entityType), e);
        }
    }


    private boolean isIdField(Field field) {
        return field.isAnnotationPresent(Id.class);
    }
}
