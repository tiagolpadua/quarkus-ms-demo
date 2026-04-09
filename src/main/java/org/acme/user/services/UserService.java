package org.acme.user.services;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
  @WithSpan("UserService.create")
  public UserResponse create(UserRequest userRequest) {
    User user = mapper.toEntity(userRequest);
    repository.persist(user);
    meterRegistry.counter("user_create_total").increment();
    return mapper.toResponse(user);
  }

  @Transactional
  @WithSpan("UserService.createMany")
  public List<UserResponse> createMany(List<UserRequest> users) {
    return users.stream().map(this::create).toList();
  }

  @WithSpan("UserService.getByUsername")
  public Optional<UserResponse> getByUsername(@SpanAttribute("arg.username") String username) {
    return repository.findByUsername(username).map(mapper::toResponse);
  }

  @Transactional
  @WithSpan("UserService.update")
  public Optional<UserResponse> update(
      @SpanAttribute("arg.username") String username,
      @SpanAttribute("arg.userRequest") UserRequest userRequest) {
    return repository
        .findByUsername(username)
        .map(
            user -> {
              mapper.updateEntity(userRequest, user);
              return mapper.toResponse(user);
            });
  }

  @Transactional
  @WithSpan("UserService.delete")
  public boolean delete(@SpanAttribute("arg.username") String username) {
    return repository.deleteByUsername(username);
  }

  @WithSpan("UserService.list")
  public List<UserResponse> list(
      @SpanAttribute("arg.page") int page,
      @SpanAttribute("arg.size") int size,
      @SpanAttribute("arg.sortBy") String sortBy,
      @SpanAttribute("arg.direction") String direction) {
    return mapper.toResponseList(repository.listPaged(page, size, sortBy, direction));
  }

  @WithSpan("UserService.listPaged")
  public PagedResponse<UserResponse> listPaged(
      @SpanAttribute("arg.page") int page,
      @SpanAttribute("arg.size") int size,
      @SpanAttribute("arg.sortBy") String sortBy,
      @SpanAttribute("arg.direction") String direction) {
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

  @WithSpan("UserService.listByStatusNamedQuery")
  public List<UserResponse> listByStatusNamedQuery(
      @SpanAttribute("arg.userStatus") Integer userStatus) {
    return mapper.toResponseList(repository.findByStatusNamedQuery(userStatus));
  }

  @WithSpan("UserService.listByEmailDomainNativeQuery")
  public List<UserResponse> listByEmailDomainNativeQuery(
      @SpanAttribute("arg.emailDomain") String emailDomain) {
    return mapper.toResponseList(repository.findByEmailDomainNativeQuery(emailDomain));
  }

  @WithSpan("UserService.listByCriteria")
  public List<UserResponse> listByCriteria(
      @SpanAttribute("arg.usernamePrefix") String usernamePrefix,
      @SpanAttribute("arg.userStatus") Integer userStatus,
      @SpanAttribute("arg.emailDomainFragment") String emailDomainFragment) {
    return mapper.toResponseList(
        repository.findByCriteria(usernamePrefix, userStatus, emailDomainFragment));
  }
}
