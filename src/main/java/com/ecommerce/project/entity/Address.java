package com.ecommerce.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean deleted;

    public String getFullAddress() {
        return streetAddress + ", " + city + ", " + state + ", " + postalCode;
    }

}

