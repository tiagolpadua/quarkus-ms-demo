package org.acme.pet.mappers;

import java.util.ArrayList;
import java.util.List;
import org.acme.pet.dtos.CategoryRequest;
import org.acme.pet.dtos.CategoryResponse;
import org.acme.pet.dtos.PetRequest;
import org.acme.pet.dtos.PetResponse;
import org.acme.pet.dtos.TagRequest;
import org.acme.pet.dtos.TagResponse;
import org.acme.pet.persistence.Category;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.Tag;

public final class PetMapper {

  private PetMapper() {}

  public static Pet toEntity(PetRequest source) {
    Pet target = new Pet();
    target.setId(source.id());
    target.setCategory(toCategory(source.category()));
    target.setName(source.name());
    target.setPhotoUrls(
        source.photoUrls() == null ? new ArrayList<>() : new ArrayList<>(source.photoUrls()));
    target.setTags(
        source.tags() == null
            ? new ArrayList<>()
            : source.tags().stream().map(PetMapper::toTag).toList());
    target.setStatus(source.status());
    return target;
  }

  public static PetResponse toResponse(Pet source) {
    return new PetResponse(
        source.getId(),
        toCategoryResponse(source.getCategory()),
        source.getName(),
        source.getPhotoUrls() == null ? List.of() : List.copyOf(source.getPhotoUrls()),
        source.getTags() == null
            ? List.of()
            : source.getTags().stream().map(PetMapper::toTagResponse).toList(),
        source.getStatus());
  }

  public static List<PetResponse> toResponseList(List<Pet> source) {
    return source.stream().map(PetMapper::toResponse).toList();
  }

  private static Category toCategory(CategoryRequest source) {
    return source == null ? null : new Category(source.id(), source.name());
  }

  private static Tag toTag(TagRequest source) {
    return new Tag(source.id(), source.name());
  }

  private static CategoryResponse toCategoryResponse(Category source) {
    return source == null ? null : new CategoryResponse(source.id(), source.name());
  }

  private static TagResponse toTagResponse(Tag source) {
    return new TagResponse(source.id(), source.name());
  }
}
