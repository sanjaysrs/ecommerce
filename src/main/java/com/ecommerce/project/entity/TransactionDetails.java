package com.ecommerce.project.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TransactionDetails {

    private String orderId;
    private String currency;
    private Integer amount;

}
