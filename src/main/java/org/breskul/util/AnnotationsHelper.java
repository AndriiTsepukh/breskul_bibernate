package org.breskul.util;

import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;
import org.breskul.exception.BoboException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationsHelper {
    public static String getTableName(Object object) {
        checkIfTableAnnotationPresent(object);
        String tableName = object.getClass().getAnnotation(Table.class).value();
        if (tableName.isBlank()) tableName = object.getClass().getSimpleName().toLowerCase();
        return tableName;
    }

    private static void checkIfTableAnnotationPresent(Object object) {
        if (!object.getClass().isAnnotationPresent(Table.class)) {
            throw new BoboException("Annotation @Table is not present in correspondent class: "
                    + object.getClass().getSimpleName());
        }
    }

    public static Long getId(Object object) {
        checkIfTableAnnotationPresent(object);
        var fields = object.getClass().getFields();
        List<Field> annotatedFieldsById = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                annotatedFieldsById.add(field);
            }
        }
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

    public static String getIdColumnName(Object object) {
        checkIfTableAnnotationPresent(object);
        var fields = object.getClass().getFields();
        List<Field> annotatedFieldsById = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                annotatedFieldsById.add(field);
            }
        }
        if (annotatedFieldsById.size() == 0) throw new BoboException("@Id annotation not found for class: " + object.getClass().getSimpleName());
        if (annotatedFieldsById.size() > 1) throw new BoboException("@Id annotation found on more than 1 field for class: " + object.getClass().getSimpleName());
        var idField = annotatedFieldsById.get(0);
        var idFieldName = idField.getAnnotation(Id.class).value();
        if (idFieldName.isBlank()) idFieldName = idField.getName().toLowerCase();
        return idFieldName;
    }

    public static Field getIdColumn(Object object) {
        checkIfTableAnnotationPresent(object);
        var fields = object.getClass().getFields();
        List<Field> annotatedFieldsById = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                annotatedFieldsById.add(field);
            }
        }
        if (annotatedFieldsById.size() == 0) throw new BoboException("@Id annotation not found for class: " + object.getClass().getSimpleName());
        if (annotatedFieldsById.size() > 1) throw new BoboException("@Id annotation found on more than 1 field for class: " + object.getClass().getSimpleName());
        return annotatedFieldsById.get(0);
    }

    public static Map<String, Object> getColumns(Object object){
        Map<String, Object> resultMap = new HashMap<>();
        var fields = object.getClass().getFields();
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
            }
        }
        return resultMap;
    }
}
