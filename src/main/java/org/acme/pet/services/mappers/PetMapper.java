package org.acme.pet.services.mappers;

import java.util.List;
import org.acme.pet.persistence.Category;
import org.acme.pet.persistence.Pet;
import org.acme.pet.persistence.Tag;
import org.acme.pet.resources.dtos.CategoryRequest;
import org.acme.pet.resources.dtos.CategoryResponse;
import org.acme.pet.resources.dtos.PetRequest;
import org.acme.pet.resources.dtos.PetResponse;
import org.acme.pet.resources.dtos.TagRequest;
import org.acme.pet.resources.dtos.TagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PetMapper {

  Pet toEntity(PetRequest source);

  PetResponse toResponse(Pet source);

  List<PetResponse> toResponseList(List<Pet> source);

  Category toCategory(CategoryRequest source);

  Tag toTag(TagRequest source);

  CategoryResponse toCategoryResponse(Category source);

  TagResponse toTagResponse(Tag source);
}
