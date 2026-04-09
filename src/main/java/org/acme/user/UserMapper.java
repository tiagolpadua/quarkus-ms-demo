package org.acme.user;

import org.acme.user.dtos.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  User toEntity(UserDto source);

  UserDto toDto(User source);

  void updateEntity(UserDto source, @MappingTarget User target);
}
