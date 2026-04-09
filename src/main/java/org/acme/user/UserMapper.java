package org.acme.user;

import java.util.List;
import org.acme.user.dtos.UserRequest;
import org.acme.user.dtos.UserResponse;

public final class UserMapper {

  private UserMapper() {}

  public static User toEntity(UserRequest source) {
    User target = new User();
    target.setId(source.id());
    target.setUsername(source.username());
    target.setFirstName(source.firstName());
    target.setLastName(source.lastName());
    target.setEmail(source.email());
    target.setPhone(source.phone());
    target.setUserStatus(source.userStatus());
    return target;
  }

  public static void updateEntity(UserRequest source, User target) {
    target.setUsername(source.username());
    target.setFirstName(source.firstName());
    target.setLastName(source.lastName());
    target.setEmail(source.email());
    target.setPhone(source.phone());
    target.setUserStatus(source.userStatus());
  }

  public static UserResponse toResponse(User source) {
    return new UserResponse(
        source.getId(),
        source.getUsername(),
        source.getFirstName(),
        source.getLastName(),
        source.getEmail(),
        source.getPhone(),
        source.getUserStatus());
  }

  public static List<UserResponse> toResponseList(List<User> source) {
    return source.stream().map(UserMapper::toResponse).toList();
  }
}
