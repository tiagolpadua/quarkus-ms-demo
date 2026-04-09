package org.acme.pet.resources.dtos;

import java.util.List;

public record PetResponse(
    Long id,
    CategoryResponse category,
    String name,
    List<String> photoUrls,
    List<TagResponse> tags,
    String status) {}
