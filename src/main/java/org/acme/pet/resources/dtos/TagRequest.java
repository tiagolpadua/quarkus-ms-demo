package org.acme.pet.resources.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TagRequest(
    @Positive(message = "tag.id must be greater than zero") Long id,
    @NotBlank(message = "tag.name is required")
        @Size(max = 100, message = "tag.name must have at most 100 characters")
        String name) {}
