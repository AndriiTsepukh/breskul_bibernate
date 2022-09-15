package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

@Data
@Table("table_delete_test")
public class EntityToTestDelete {
    @Id("id")
    public Long id;

    @Column(name = "name")
    public String name;

    @Column(name = "second_name")
    public String secondName;
}
