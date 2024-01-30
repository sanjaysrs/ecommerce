package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {

    @NotEmpty(message = "First name is required")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "First name should contain only alphabets")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name should contain only alphabets")
    private String lastName;

    @NotEmpty(message = "Email is required")
    @Pattern(regexp="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", message = "Email is not valid")
    private String email;

    @NotEmpty(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters long")
    private String password;

}
