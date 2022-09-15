package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

@Table("products")
@Data
public class Products {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;
}
