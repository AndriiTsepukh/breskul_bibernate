package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

@Table
@Data
public class Student {

    @Id
    private Long id;
    private String name;
    private String secondName;
}
