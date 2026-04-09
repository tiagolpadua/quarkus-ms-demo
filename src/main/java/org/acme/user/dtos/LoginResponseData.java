package org.acme.user.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponseData {
  private String message;
  private String expiresAfter;
  private Integer rateLimit;
}
