package com.ecommerce.project.jasperreports.invoice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDataset {

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double stotal;

}
