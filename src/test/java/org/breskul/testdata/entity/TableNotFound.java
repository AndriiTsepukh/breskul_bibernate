package org.breskul.testdata.entity;

import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;

@Table
public class TableNotFound {
    @Id
    private Long id;
    private String name;
}
