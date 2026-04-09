package org.acme.user.services.mappers;

import java.util.List;
import org.acme.user.persistence.User;
import org.acme.user.resources.dtos.UserRequest;
import org.acme.user.resources.dtos.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface UserMapper {

  User toEntity(UserRequest source);

  @Mapping(target = "id", ignore = true)
  void updateEntity(UserRequest source, @MappingTarget User target);

  UserResponse toResponse(User source);

  List<UserResponse> toResponseList(List<User> source);
}
