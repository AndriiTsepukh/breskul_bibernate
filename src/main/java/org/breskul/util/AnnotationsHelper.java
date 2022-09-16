package org.breskul.util;

import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;
import org.breskul.exception.BoboException;

import javax.swing.text.Utilities;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AnnotationsHelper {
    public static String getTableName(Class<?> clazz) {
        checkIfTableAnnotationPresent(clazz);
        String tableName = clazz.getAnnotation(Table.class).value();
        if (tableName.isBlank()) tableName = clazz.getSimpleName().toLowerCase();
        return tableName;
    }

    private static void checkIfTableAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new BoboException("Annotation @Table is not present in correspondent class: "
                    + clazz.getSimpleName());
        }
    }

    public static Long getId(Object object) {
        checkIfTableAnnotationPresent(object.getClass());
        var fields = object.getClass().getDeclaredFields();
        var annotatedFieldsById = Stream.of(fields)
                .filter(field -> field.isAnnotationPresent(Id.class)).toList();
        if (annotatedFieldsById.size() == 0) throw new BoboException("@Id annotation not found for class: " + object.getClass().getSimpleName());
        if (annotatedFieldsById.size() > 1) throw new BoboException("@Id annotation found on more than 1 field for class: " + object.getClass().getSimpleName());
        var idField = annotatedFieldsById.get(0);
        idField.setAccessible(true);
        try {
            return (Long)idField.get(object);
        } catch (IllegalAccessException e) {
            throw new BoboException(e.getMessage());
        }
    }

    public static String getIdColumnName(Class<?> clazz) {
        checkIfTableAnnotationPresent(clazz);
        var fields = clazz.getDeclaredFields();
        List<Field> annotatedFieldsById = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                annotatedFieldsById.add(field);
            }
        }
        if (annotatedFieldsById.size() == 0) throw new BoboException("@Id annotation not found for class: " + clazz.getSimpleName());
        if (annotatedFieldsById.size() > 1) throw new BoboException("@Id annotation found on more than 1 field for class: " + clazz.getSimpleName());
        var idField = annotatedFieldsById.get(0);
        var idFieldName = idField.getAnnotation(Id.class).value();
        if (idFieldName.isBlank()) idFieldName = idField.getName().toLowerCase();
        return idFieldName;
    }

    public static Field getIdColumn(Class<?> clazz) {
        checkIfTableAnnotationPresent(clazz);
        var fields = clazz.getDeclaredFields();
        List<Field> annotatedFieldsById = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                annotatedFieldsById.add(field);
            }
        }
        if (annotatedFieldsById.size() == 0) throw new BoboException("@Id annotation not found for class: " + clazz.getSimpleName());
        if (annotatedFieldsById.size() > 1) throw new BoboException("@Id annotation found on more than 1 field for class: " + clazz.getSimpleName());
        return annotatedFieldsById.get(0);
    }

    public static Map<String, Object> getColumns(Object object){
        Map<String, Object> resultMap = new HashMap<>();
        var fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                var columnName = field.getAnnotation(Column.class).name();
                if (columnName.isBlank()) columnName = field.getName().toLowerCase();
                field.setAccessible(true);
                try {
                    resultMap.put(columnName, field.get(object));
                } catch (IllegalAccessException e) {
                    throw new BoboException(e.getMessage());
                }
            } else {
                field.setAccessible(true);
                var columnName = StringUtils.camelToSnake(field.getName());
                try {
                    if(!field.isAnnotationPresent(Id.class)) {
                        resultMap.put(columnName, field.get(object));
                    }
                } catch (IllegalAccessException e) {
                    throw new BoboException(e.getMessage());
                }
            }
        }
        return resultMap;
    }
}
