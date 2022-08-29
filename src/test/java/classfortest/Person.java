package classfortest;

import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Entity;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;

@Entity
@Table("persons")
public class Person {

    @Id
    private Long id;

    @Column(name = "person_name")
    private String name;

    @Column(name = "person_surname", ignoreDirtyCheck = true)
    private String surname;
}
