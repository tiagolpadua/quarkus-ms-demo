package org.acme.pet.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Category(
    @Column(name = "category_id") Long id, @Column(name = "category_name") String name) {}
