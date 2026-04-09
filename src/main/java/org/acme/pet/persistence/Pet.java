package org.acme.pet.persistence;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import org.acme.pet.dtos.CategoryData;
import org.acme.pet.dtos.TagData;

@RegisterForReflection
public record Pet(
    Long id,
    CategoryData category,
    String name,
    List<String> photoUrls,
    List<TagData> tags,
    String status) {}
