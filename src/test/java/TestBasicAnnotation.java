
import classfortest.Person;
import classfortest.StudentBreskulTeam;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Entity;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class TestBasicAnnotation {

    @Test
    public void existAnnotationTable() {

        final var annotationEntityIsPresent = Person.class.isAnnotationPresent(Entity.class);
        assert (annotationEntityIsPresent);
        final var annotationTableIsPresent = Person.class.isAnnotationPresent(Table.class);
        assert (annotationTableIsPresent);
        final var tableName = Person.class.getDeclaredAnnotation(Table.class).value();
        assert (tableName.equals("persons"));
    }

    @Test
    public void emptyAnnotationTable() {

        final var annotationEntityIsPresent = StudentBreskulTeam.class.isAnnotationPresent(Entity.class);
        assert (annotationEntityIsPresent);
        final var annotationTableIsPresent = StudentBreskulTeam.class.isAnnotationPresent(Table.class);
        assert (!annotationTableIsPresent);
        final var nameToLowerSnakeCase = lowerSnakeCase(StudentBreskulTeam.class.getSimpleName());
        assert (nameToLowerSnakeCase.equals("student_breskul_team"));
    }

    @Test
    public void existAnnotationColumn() {


        final var personName = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.getName().equals("name") && field.isAnnotationPresent(Column.class))
                .peek(field -> field.setAccessible(true))
                .map(columnValue -> columnValue.getDeclaredAnnotation(Column.class).name())
                .findFirst()
                .orElseThrow(RuntimeException::new);
        assert (personName.equals("person_name"));

        final var checkIgnoreDirty = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.getName().equals("surname") && field.isAnnotationPresent(Column.class))
                .peek(field -> field.setAccessible(true))
                .map(columnValue -> columnValue.getDeclaredAnnotation(Column.class).ignoreDirtyCheck())
                .findFirst()
                .orElseThrow(RuntimeException::new);

        assert (checkIgnoreDirty);
    }

    @Test
    public void emptyAnnotationId() {
        Field field1 = Arrays.stream(StudentBreskulTeam.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElse(null);

        assert (Objects.isNull(field1));
    }

    @Test
    public void existAnnotationId() {


        final var personName = Arrays.stream(Person.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(RuntimeException::new);

        assert (Objects.nonNull(personName));

    }


    private String lowerSnakeCase(final String tableName) {
        var lowerSnakeCase = new StringBuilder();
        var toCharArray = tableName.toCharArray();
        for (int i = 0; i < toCharArray.length; i++) {
            if (i == 0)
                lowerSnakeCase.append(Character.toLowerCase(toCharArray[i]));
            else if (Character.toLowerCase(toCharArray[i]) != toCharArray[i])
                lowerSnakeCase.append("_").append(Character.toLowerCase(toCharArray[i]));
            else
                lowerSnakeCase.append(toCharArray[i]);
        }

        return lowerSnakeCase.toString();
    }
}






