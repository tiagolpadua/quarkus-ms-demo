package org.acme.user;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

  public Optional<User> findByUsername(String username) {
    return find("username", username).firstResultOptional();
  }

  public boolean deleteByUsername(String username) {
    return delete("username", username) > 0;
  }
}
