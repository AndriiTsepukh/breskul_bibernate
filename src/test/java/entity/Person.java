package entity;

import lombok.Data;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;

@Table("persons")
@Data
public class Person {

    @Id
    private Long id;

    @Column(name = "person_name")
    private String name;

    @Column(name = "person_surname", ignoreDirtyCheck = true)
    private String surname;
}
