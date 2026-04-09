package org.acme.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.acme.user.dtos.UserData;

@ApplicationScoped
public class UserService {

  @Inject UserRepository repository;
  @Inject UserMapper mapper;

  @Transactional
  public void create(UserData userData) {
    repository.persist(mapper.toEntity(userData));
  }

  @Transactional
  public void createMany(List<UserData> users) {
    for (UserData userData : users) {
      create(userData);
    }
  }

  public Optional<UserData> getByUsername(String username) {
    return repository.findByUsername(username).map(mapper::toData);
  }

  @Transactional
  public Optional<UserData> update(String username, UserData userData) {
    return repository
        .findByUsername(username)
        .map(
            user -> {
              mapper.updateEntity(userData, user);
              return mapper.toData(user);
            });
  }

  @Transactional
  public boolean delete(String username) {
    return repository.deleteByUsername(username);
  }

  public List<UserData> list() {
    return repository.listAll().stream().map(mapper::toData).collect(Collectors.toList());
  }
}
