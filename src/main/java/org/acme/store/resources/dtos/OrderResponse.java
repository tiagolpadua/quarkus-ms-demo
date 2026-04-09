package org.acme.store.resources.dtos;

import java.time.OffsetDateTime;

public record OrderResponse(
    Long id,
    Long petId,
    Integer quantity,
    OffsetDateTime shipDate,
    String status,
    Boolean complete) {}
