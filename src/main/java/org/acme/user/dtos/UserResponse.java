package org.acme.user.dtos;

public record UserResponse(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone,
    Integer userStatus) {}
