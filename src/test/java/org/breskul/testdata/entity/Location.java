package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.annotation.Column;
import org.breskul.annotation.Id;
import org.breskul.annotation.Table;

@Table("location")
@Data
public class Location {
    @Id
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "rooms_qty")
    private Integer roomsQty;

    @Column(name = "price")
    private Long price;
}
