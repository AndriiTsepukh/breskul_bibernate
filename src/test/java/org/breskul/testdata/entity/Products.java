package org.breskul.testdata.entity;

import lombok.Data;
import lombok.ToString;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Table;

import java.util.Date;

@Table("products")
@Data
public class Products {

    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;
}
