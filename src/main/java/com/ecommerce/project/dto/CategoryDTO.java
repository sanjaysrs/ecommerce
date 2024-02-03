package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {

    private int id;

    @NotBlank(message = "Category name is required")
    private String name;

}
