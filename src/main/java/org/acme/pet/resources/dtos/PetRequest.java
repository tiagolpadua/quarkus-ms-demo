package org.acme.pet.resources.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PetRequest(
    @Positive(message = "id must be greater than zero") Long id,
    @Valid CategoryRequest category,
    @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must have at most 100 characters")
        String name,
    @NotEmpty(message = "photoUrls must not be empty")
        List<
                @NotBlank(message = "photoUrls entries must not be blank")
                @Size(max = 500, message = "photoUrls entries must have at most 500 characters")
                String>
            photoUrls,
    List<@Valid TagRequest> tags,
    @NotBlank(message = "status is required")
        @Pattern(
            regexp = "available|pending|sold",
            message = "status must be one of: available, pending, sold")
        String status) {}
