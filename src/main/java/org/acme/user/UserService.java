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

  @Transactional
  public void create(UserData userData) {
    repository.persist(toEntity(userData, new User()));
  }

  @Transactional
  public void createMany(List<UserData> users) {
    for (UserData userData : users) {
      create(userData);
    }
  }

  public Optional<UserData> getByUsername(String username) {
    return repository.findByUsername(username).map(this::toData);
  }

  @Transactional
  public Optional<UserData> update(String username, UserData userData) {
    return repository
        .findByUsername(username)
        .map(
            user -> {
              user.setUsername(userData.getUsername());
              user.setFirstName(userData.getFirstName());
              user.setLastName(userData.getLastName());
              user.setEmail(userData.getEmail());
              user.setPhone(userData.getPhone());
              user.setUserStatus(userData.getUserStatus());
              return toData(user);
            });
  }

  @Transactional
  public boolean delete(String username) {
    return repository.deleteByUsername(username);
  }

  public List<UserData> list() {
    return repository.listAll().stream().map(this::toData).collect(Collectors.toList());
  }

  private User toEntity(UserData source, User target) {
    target.setId(source.getId());
    target.setUsername(source.getUsername());
    target.setFirstName(source.getFirstName());
    target.setLastName(source.getLastName());
    target.setEmail(source.getEmail());
    target.setPhone(source.getPhone());
    target.setUserStatus(source.getUserStatus());
    return target;
  }

  private UserData toData(User source) {
    UserData user = new UserData();
    user.setId(source.getId());
    user.setUsername(source.getUsername());
    user.setFirstName(source.getFirstName());
    user.setLastName(source.getLastName());
    user.setEmail(source.getEmail());
    user.setPhone(source.getPhone());
    user.setUserStatus(source.getUserStatus());
    return user;
  }
}
