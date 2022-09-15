package org.breskul.testdata.entity;

import lombok.Data;
import org.breskul.connectivity.annotation.Column;
import org.breskul.connectivity.annotation.Id;
import org.breskul.connectivity.annotation.Table;

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
