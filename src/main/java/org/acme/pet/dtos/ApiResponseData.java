package org.acme.pet.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponseData {
  private Integer code;
  private String type;
  private String message;
}
