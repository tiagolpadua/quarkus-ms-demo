package org.acme.rest.json;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Client extends PanacheEntity {

  @Column(nullable = false)
  public String name;

  @Column(nullable = false, unique = true)
  public String email;
}
