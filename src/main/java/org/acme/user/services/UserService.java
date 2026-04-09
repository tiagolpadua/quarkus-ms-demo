package org.acme.user.services;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.acme.shared.pagination.PageMetadata;
import org.acme.shared.pagination.PageResult;
import org.acme.shared.pagination.PagedResponse;
import org.acme.shared.pagination.SortMetadata;
import org.acme.user.persistence.User;
import org.acme.user.persistence.UserRepository;
import org.acme.user.resources.dtos.UserRequest;
import org.acme.user.resources.dtos.UserResponse;
import org.acme.user.services.mappers.UserMapper;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;
  private final MeterRegistry meterRegistry;
  private final UserMapper mapper;

  @Transactional
  public UserResponse create(UserRequest userRequest) {
    User user = mapper.toEntity(userRequest);
    repository.persist(user);
    meterRegistry.counter("user_create_total").increment();
    return mapper.toResponse(user);
  }

  @Transactional
  public List<UserResponse> createMany(List<UserRequest> users) {
    return users.stream().map(this::create).toList();
  }

  public Optional<UserResponse> getByUsername(String username) {
    return repository.findByUsername(username).map(mapper::toResponse);
  }

  @Transactional
  public Optional<UserResponse> update(String username, UserRequest userRequest) {
    return repository
        .findByUsername(username)
        .map(
            user -> {
              mapper.updateEntity(userRequest, user);
              return mapper.toResponse(user);
            });
  }

  @Transactional
  public boolean delete(String username) {
    return repository.deleteByUsername(username);
  }

  public List<UserResponse> list(int page, int size, String sortBy, String direction) {
    return mapper.toResponseList(repository.listPaged(page, size, sortBy, direction));
  }

  public PagedResponse<UserResponse> listPaged(
      int page, int size, String sortBy, String direction) {
    PageResult<User> result = repository.listPageResult(page, size, sortBy, direction);
    List<UserResponse> items = mapper.toResponseList(result.items());
    int totalPages =
        result.totalElements() == 0
            ? 0
            : Math.toIntExact((result.totalElements() + result.size() - 1) / result.size());
    PageMetadata pageMetadata =
        new PageMetadata(
            result.page(),
            result.size(),
            result.totalElements(),
            totalPages,
            result.page() == 0,
            totalPages == 0 || result.page() >= totalPages - 1,
            result.page() + 1 < totalPages,
            result.page() > 0);
    SortMetadata sortMetadata = new SortMetadata(result.sortBy(), result.direction());
    return new PagedResponse<>(items, pageMetadata, sortMetadata);
  }

  public List<UserResponse> listByStatusNamedQuery(Integer userStatus) {
    return mapper.toResponseList(repository.findByStatusNamedQuery(userStatus));
  }

  public List<UserResponse> listByEmailDomainNativeQuery(String emailDomain) {
    return mapper.toResponseList(repository.findByEmailDomainNativeQuery(emailDomain));
  }

  public List<UserResponse> listByCriteria(
      String usernamePrefix, Integer userStatus, String emailDomainFragment) {
    return mapper.toResponseList(
        repository.findByCriteria(usernamePrefix, userStatus, emailDomainFragment));
  }
}
