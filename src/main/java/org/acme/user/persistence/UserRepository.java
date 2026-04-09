package org.acme.user.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

  private static final String USERNAME = "username";
  private static final String USER_STATUS = "userStatus";
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", USERNAME, "firstName", "lastName", "email", USER_STATUS);

  public Optional<User> findByUsername(String username) {
    return find(USERNAME, username).firstResultOptional();
  }

  public boolean deleteByUsername(String username) {
    return delete(USERNAME, username) > 0;
  }

  public List<User> listPaged(int page, int size, String sortBy, String direction) {
    String field = sanitizeSortField(sortBy);
    Sort sort = isDescending(direction) ? Sort.descending(field) : Sort.ascending(field);
    return findAll(sort).page(Page.of(page, size)).list();
  }

  public List<User> findByStatusNamedQuery(Integer userStatus) {
    return getEntityManager()
        .createNamedQuery("User.findByStatusNamed", User.class)
        .setParameter(USER_STATUS, userStatus)
        .getResultList();
  }

  public List<User> findByEmailDomainNativeQuery(String emailDomain) {
    return getEntityManager()
        .createNamedQuery("User.findByEmailDomainNative", User.class)
        .setParameter("emailDomain", emailDomain.toLowerCase())
        .getResultList();
  }

  public List<User> findByCriteria(
      String usernamePrefix, Integer userStatus, String emailDomainFragment) {
    var builder = getEntityManager().getCriteriaBuilder();
    var query = builder.createQuery(User.class);
    var root = query.from(User.class);
    List<Predicate> predicates = new ArrayList<>();

    if (usernamePrefix != null && !usernamePrefix.isBlank()) {
      predicates.add(builder.like(root.get(USERNAME), usernamePrefix + "%"));
    }
    if (userStatus != null) {
      predicates.add(builder.equal(root.get(USER_STATUS), userStatus));
    }
    if (emailDomainFragment != null && !emailDomainFragment.isBlank()) {
      predicates.add(
          builder.like(
              builder.lower(root.get("email")), "%" + emailDomainFragment.toLowerCase() + "%"));
    }

    query
        .select(root)
        .where(predicates.toArray(Predicate[]::new))
        .orderBy(builder.asc(root.get(USERNAME)));
    return getEntityManager().createQuery(query).getResultList();
  }

  private String sanitizeSortField(String sortBy) {
    if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
      return USERNAME;
    }
    return sortBy;
  }

  private boolean isDescending(String direction) {
    return direction != null && "desc".equalsIgnoreCase(direction);
  }
}
