package org.acme.pet.dtos;

import java.util.List;

public record PetRequest(
    Long id,
    CategoryRequest category,
    String name,
    List<String> photoUrls,
    List<TagRequest> tags,
    String status) {}
