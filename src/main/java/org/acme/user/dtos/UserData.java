package org.acme.user.dtos;

public record UserData(
    Long id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phone,
    Integer userStatus) {}
