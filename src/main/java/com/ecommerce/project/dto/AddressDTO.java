package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {

    @NotBlank(message = "Street address cannot be blank")
    private String streetAddress;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "State cannot be blank")
    private String state;

    @Pattern(regexp = "\\d{6}", message = "Enter a valid postal code")
    private String postalCode;

    @NotBlank(message = "Country cannot be null")
    private String country;

}
