package com.ecommerce.project.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletDTO {

    @DecimalMin(value = "1.0", message = "Amount must be greater than or equal to ₹1")
    private double amount;

}
