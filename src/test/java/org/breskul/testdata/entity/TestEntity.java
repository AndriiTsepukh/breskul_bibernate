package org.breskul.testdata.entity;

import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

@Table("table_test")
public class TestEntity {
    @Id("id")
    public Long id;

    @Column(name="first_name")
    public String firstName;

    @Column(name="last_name")
    public String lastName;
}