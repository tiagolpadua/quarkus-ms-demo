package org.acme.pet.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Tag(@Column(name = "tag_id") Long id, @Column(name = "tag_name") String name) {}
