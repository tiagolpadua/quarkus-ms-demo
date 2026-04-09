package org.acme.pet.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PetRepository implements PanacheRepository<Pet> {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "status");

  public List<Pet> listAll() {
    return findAll().list();
  }

  public List<Pet> findByStatus(
      List<String> statuses, int page, int size, String sortBy, String direction) {
    return find("status in ?1", toSort(sortBy, direction), statuses)
        .page(Page.of(page, size))
        .list();
  }

  public List<Pet> findByTags(
      List<String> tagNames, int page, int size, String sortBy, String direction) {
    String sortField = sanitizeSortField(sortBy);
    boolean descending = isDescending(direction);
    return getEntityManager()
        .createQuery(
            "select distinct p from Pet p join p.tags t where t.name in :tagNames order by p."
                + sortField
                + (descending ? " desc" : " asc"),
            Pet.class)
        .setParameter("tagNames", tagNames)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  public Optional<Pet> findOptionalById(Long id) {
    return findByIdOptional(id);
  }

  public Pet save(Pet pet) {
    if (pet.getId() == null) {
      persist(pet);
      return pet;
    } else {
      return getEntityManager().merge(pet);
    }
  }

  public Optional<Pet> updatePartial(Long petId, String name, String status) {
    return findByIdOptional(petId)
        .map(
            pet -> {
              if (name != null && !name.isBlank()) {
                pet.setName(name);
              }
              if (status != null && !status.isBlank()) {
                pet.setStatus(status);
              }
              return pet;
            });
  }

  public boolean delete(Long petId) {
    return deleteById(petId);
  }

  private Sort toSort(String sortBy, String direction) {
    String field = sanitizeSortField(sortBy);
    return isDescending(direction) ? Sort.descending(field) : Sort.ascending(field);
  }

  private String sanitizeSortField(String sortBy) {
    if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
      return "id";
    }
    return sortBy;
  }

  private boolean isDescending(String direction) {
    return direction != null && "desc".equalsIgnoreCase(direction);
  }
}
