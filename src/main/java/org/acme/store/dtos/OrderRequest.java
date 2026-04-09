package org.acme.store.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
    @Positive(message = "id must be greater than zero") Long id,
    @NotNull(message = "petId is required") @Positive(message = "petId must be greater than zero")
        Long petId,
    @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        Integer quantity,
    @NotBlank(message = "shipDate is required") String shipDate,
    @NotBlank(message = "status is required") String status,
    @NotNull(message = "complete is required") Boolean complete) {}
