package org.acme.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @Positive(message = "id must be greater than zero") Long id,
    @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must have between 3 and 50 characters")
        String username,
    @NotBlank(message = "firstName is required")
        @Size(max = 50, message = "firstName must have at most 50 characters")
        String firstName,
    @NotBlank(message = "lastName is required")
        @Size(max = 50, message = "lastName must have at most 50 characters")
        String lastName,
    @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
    @NotBlank(message = "phone is required")
        @Pattern(regexp = "[0-9+()\\-\\s]{3,30}", message = "phone format is invalid")
        String phone,
    @Min(value = 0, message = "userStatus must be at least 0")
        @Max(value = 10, message = "userStatus must be at most 10")
        Integer userStatus) {}
