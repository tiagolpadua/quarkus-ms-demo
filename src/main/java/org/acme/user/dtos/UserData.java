package org.acme.user.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserData {
  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private Integer userStatus;
}
