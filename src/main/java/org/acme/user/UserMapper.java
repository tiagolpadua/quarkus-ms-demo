package org.acme.user;

import org.acme.user.dtos.UserData;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  User toEntity(UserData source);

  UserData toData(User source);

  void updateEntity(UserData source, @MappingTarget User target);
}
