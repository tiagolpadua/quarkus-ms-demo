package org.acme.pet.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PetRepository implements PanacheRepository<Pet> {

  public List<Pet> listAll() {
    return findAll().list();
  }

  public List<Pet> findByStatus(List<String> statuses) {
    return list("status in ?1", statuses);
  }

  public List<Pet> findByTags(List<String> tagNames) {
    return getEntityManager()
        .createQuery(
            "select distinct p from Pet p join p.tags t where t.name in :tagNames", Pet.class)
        .setParameter("tagNames", tagNames)
        .getResultList();
  }

  public Optional<Pet> findOptionalById(Long id) {
    return findByIdOptional(id);
  }

  @Transactional
  public Pet save(Pet pet) {
    if (pet.getId() == null) {
      persist(pet);
      return pet;
    } else {
      return getEntityManager().merge(pet);
    }
  }

  @Transactional
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

  @Transactional
  public boolean delete(Long petId) {
    return deleteById(petId);
  }
}
