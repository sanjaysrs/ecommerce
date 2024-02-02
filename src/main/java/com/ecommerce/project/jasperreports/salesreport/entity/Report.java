package com.ecommerce.project.jasperreports.salesreport.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class Report {

    private Long orderId;

    private LocalDate orderDate;

    private String customer;

    private Double totalAmount;

    private String paymentMethod;

    private String products;

    private String orderStatus;

}
