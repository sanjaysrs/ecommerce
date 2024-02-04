package com.ecommerce.project.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private int categoryId;

    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private double price;

    @PositiveOrZero(message = "Quantity must be greater than or equal to 0")
    private long quantity;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    private String imageName;

}
