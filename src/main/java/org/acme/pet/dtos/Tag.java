package org.acme.pet.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

  @Column(name = "tag_id")
  private Long id;

  @Column(name = "tag_name")
  private String name;
}
