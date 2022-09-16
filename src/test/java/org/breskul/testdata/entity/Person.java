package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

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
