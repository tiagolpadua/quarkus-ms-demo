package org.acme.store;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Order(
    Long id, Long petId, Integer quantity, String shipDate, String status, Boolean complete) {}
