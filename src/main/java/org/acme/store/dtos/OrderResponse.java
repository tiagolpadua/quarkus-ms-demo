package org.acme.store.dtos;

public record OrderResponse(
    Long id, Long petId, Integer quantity, String shipDate, String status, Boolean complete) {}
