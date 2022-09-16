package org.breskul.testdata.entity;

import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

@Table
public class TableNotFound {
    @Id
    private Long id;
    private String name;
}
