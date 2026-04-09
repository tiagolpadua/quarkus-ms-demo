package org.acme.pet.resources.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @Positive(message = "category.id must be greater than zero") Long id,
    @NotBlank(message = "category.name is required")
        @Size(max = 100, message = "category.name must have at most 100 characters")
        String name) {}
