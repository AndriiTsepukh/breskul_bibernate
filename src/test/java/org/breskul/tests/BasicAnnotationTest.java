package org.breskul.tests;

import org.breskul.pool.PooledDataSource;
import org.breskul.testdata.entity.Person;
import org.breskul.testdata.entity.StudentBreskulTeam;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class BasicAnnotationTest {

    @BeforeEach
    public void setup() {
        PooledDataSource.reset();
    }
    @Test
    public void existAnnotationTableTest() {
        final var annotationTableIsPresent = Person.class.isAnnotationPresent(Table.class);
        assertTrue(annotationTableIsPresent);
        final var tableName = Person.class.getDeclaredAnnotation(Table.class).value();
        assertEquals(tableName, "persons");
    }

    @Test
    public void emptyAnnotationTableTest() {
        final var annotationTableIsPresent = StudentBreskulTeam.class.isAnnotationPresent(Table.class);
        assertTrue(annotationTableIsPresent);
        var valueIsEmpty = StudentBreskulTeam.class.getDeclaredAnnotation(Table.class).value();
        assertEquals(valueIsEmpty, "");
        final var nameToLowerSnakeCase = camelToSnake(StudentBreskulTeam.class.getSimpleName());
        assertEquals(nameToLowerSnakeCase, "student_breskul_team");
    }

    @Test
    public void existAnnotationColumnTest() {
        final var personName = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.getName().equals("name") && field.isAnnotationPresent(Column.class))
                .peek(field -> field.setAccessible(true))
                .map(columnValue -> columnValue.getDeclaredAnnotation(Column.class).name())
                .findFirst()
                .orElseThrow(RuntimeException::new);
        assertEquals(personName, "person_name");

        final var checkIgnoreDirty = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.getName().equals("surname") && field.isAnnotationPresent(Column.class))
                .peek(field -> field.setAccessible(true))
                .map(columnValue -> columnValue.getDeclaredAnnotation(Column.class).ignoreDirtyCheck())
                .findFirst()
                .orElseThrow(RuntimeException::new);

        assertTrue(checkIgnoreDirty);
    }

    @Test
    public void emptyAnnotationIdTest() {
        Field field1 = Arrays.stream(StudentBreskulTeam.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElse(null);

        assertTrue(Objects.isNull(field1));
    }

    @Test
    public void existAnnotationIdTest() {
        final var personName = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(RuntimeException::new);

        assertTrue(Objects.nonNull(personName));
    }

    private String camelToSnake(String camelString) {
        return camelString.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}






