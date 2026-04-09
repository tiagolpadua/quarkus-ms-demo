package org.acme.store;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class Order {
  private Long id;
  private Long petId;
  private Integer quantity;
  private String shipDate;
  private String status;
  private Boolean complete;
}
