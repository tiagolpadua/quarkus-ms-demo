package org.acme.user.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.acme.user.dtos.UserRequest;
import org.acme.user.dtos.UserResponse;
import org.acme.user.mappers.UserMapper;
import org.acme.user.persistence.User;
import org.acme.user.persistence.UserRepository;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;

  @Transactional
  public UserResponse create(UserRequest userRequest) {
    User user = UserMapper.toEntity(userRequest);
    repository.persist(user);
    return UserMapper.toResponse(user);
  }

  @Transactional
  public List<UserResponse> createMany(List<UserRequest> users) {
    return users.stream().map(this::create).toList();
  }

  public Optional<UserResponse> getByUsername(String username) {
    return repository.findByUsername(username).map(UserMapper::toResponse);
  }

  @Transactional
  public Optional<UserResponse> update(String username, UserRequest userRequest) {
    return repository
        .findByUsername(username)
        .map(
            user -> {
              UserMapper.updateEntity(userRequest, user);
              return UserMapper.toResponse(user);
            });
  }

  @Transactional
  public boolean delete(String username) {
    return repository.deleteByUsername(username);
  }

  public List<UserResponse> list(int page, int size, String sortBy, String direction) {
    return UserMapper.toResponseList(repository.listPaged(page, size, sortBy, direction));
  }

  public List<UserResponse> listByStatusNamedQuery(Integer userStatus) {
    return UserMapper.toResponseList(repository.findByStatusNamedQuery(userStatus));
  }

  public List<UserResponse> listByEmailDomainNativeQuery(String emailDomain) {
    return UserMapper.toResponseList(repository.findByEmailDomainNativeQuery(emailDomain));
  }

  public List<UserResponse> listByCriteria(
      String usernamePrefix, Integer userStatus, String emailDomainFragment) {
    return UserMapper.toResponseList(
        repository.findByCriteria(usernamePrefix, userStatus, emailDomainFragment));
  }
}
