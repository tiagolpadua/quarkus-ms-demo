package org.acme.pet.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.acme.pet.dtos.CategoryData;
import org.acme.pet.dtos.TagData;

@ApplicationScoped
public class PetRepository {

  private final Map<Long, Pet> pets = new LinkedHashMap<>();
  private final AtomicLong sequence = new AtomicLong();

  public PetRepository() {
    save(seedPet("doggie", "available", "dogs", "friendly"));
    save(seedPet("catty", "pending", "cats", "indoor"));
  }

  public List<Pet> listAll() {
    return new ArrayList<>(pets.values());
  }

  public List<Pet> findByStatus(List<String> statuses) {
    return pets.values().stream()
        .filter(pet -> statuses.contains(pet.getStatus()))
        .collect(Collectors.toList());
  }

  public List<Pet> findByTags(List<String> tagNames) {
    return pets.values().stream()
        .filter(
            pet ->
                pet.getTags() != null
                    && pet.getTags().stream().map(TagData::getName).anyMatch(tagNames::contains))
        .collect(Collectors.toList());
  }

  public Optional<Pet> findById(Long id) {
    return Optional.ofNullable(pets.get(id));
  }

  public Pet save(Pet pet) {
    Pet copy = copyOf(pet);
    if (copy.getId() == null) {
      copy.setId(sequence.incrementAndGet());
    } else {
      sequence.accumulateAndGet(copy.getId(), Math::max);
    }
    pets.put(copy.getId(), copy);
    return copyOf(copy);
  }

  public Optional<Pet> updatePartial(Long petId, String name, String status) {
    Pet pet = pets.get(petId);
    if (pet == null) {
      return Optional.empty();
    }
    if (name != null && !name.isBlank()) {
      pet.setName(name);
    }
    if (status != null && !status.isBlank()) {
      pet.setStatus(status);
    }
    return Optional.of(copyOf(pet));
  }

  public boolean delete(Long petId) {
    return pets.remove(petId) != null;
  }

  private Pet seedPet(String name, String status, String categoryName, String tagName) {
    long id = sequence.incrementAndGet();
    Pet pet = new Pet();
    pet.setId(id);
    pet.setCategory(new CategoryData(id, categoryName));
    pet.setName(name);
    pet.getPhotoUrls().add("https://example.com/" + name + ".jpg");
    pet.getTags().add(new TagData(id, tagName));
    pet.setStatus(status);
    return pet;
  }

  private Pet copyOf(Pet source) {
    Pet copy = new Pet();
    copy.setId(source.getId());
    copy.setName(source.getName());
    copy.setStatus(source.getStatus());
    copy.setPhotoUrls(
        source.getPhotoUrls() == null ? new ArrayList<>() : new ArrayList<>(source.getPhotoUrls()));
    copy.setTags(
        source.getTags() == null
            ? new ArrayList<>()
            : source.getTags().stream()
                .map(t -> new TagData(t.getId(), t.getName()))
                .collect(Collectors.toList()));
    copy.setCategory(
        source.getCategory() == null
            ? null
            : new CategoryData(source.getCategory().getId(), source.getCategory().getName()));
    return copy;
  }
}
