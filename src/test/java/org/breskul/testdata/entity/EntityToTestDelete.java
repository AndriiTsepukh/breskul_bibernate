package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;

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
