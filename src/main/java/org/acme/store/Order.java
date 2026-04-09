package org.acme.store;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_order")
@Getter
@Setter
@NoArgsConstructor
@RegisterForReflection
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "store_order_seq")
  @SequenceGenerator(name = "store_order_seq", sequenceName = "store_order_seq", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private Long petId;

  @Column(nullable = false)
  private Integer quantity;

  @Column(nullable = false)
  private String shipDate;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private Boolean complete;
}
