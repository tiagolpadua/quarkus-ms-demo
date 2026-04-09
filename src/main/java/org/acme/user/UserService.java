package org.acme.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.acme.user.dtos.UserDto;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

  private final UserRepository repository;
  private final UserMapper mapper;

  @Transactional
  public void create(UserDto userDto) {
    repository.persist(mapper.toEntity(userDto));
  }

  @Transactional
  public void createMany(List<UserDto> users) {
    for (UserDto userDto : users) {
      create(userDto);
    }
  }

  public Optional<UserDto> getByUsername(String username) {
    return repository.findByUsername(username).map(mapper::toDto);
  }

  @Transactional
  public Optional<UserDto> update(String username, UserDto userDto) {
    return repository
        .findByUsername(username)
        .map(
            user -> {
              mapper.updateEntity(userDto, user);
              return mapper.toDto(user);
            });
  }

  @Transactional
  public boolean delete(String username) {
    return repository.deleteByUsername(username);
  }

  public List<UserDto> list() {
    return repository.listAll().stream().map(mapper::toDto).collect(Collectors.toList());
  }
}
