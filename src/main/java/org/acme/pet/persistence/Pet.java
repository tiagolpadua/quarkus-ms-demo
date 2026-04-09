package org.acme.pet.persistence;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.pet.dtos.Category;
import org.acme.pet.dtos.Tag;

@Entity
@Table(name = "pet")
@Getter
@Setter
@NoArgsConstructor
@RegisterForReflection
public class Pet {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pet_seq")
  @SequenceGenerator(name = "pet_seq", sequenceName = "pet_seq", allocationSize = 1)
  private Long id;

  @Embedded private Category category;

  @Column(nullable = false)
  private String name;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "pet_photo_urls", joinColumns = @JoinColumn(name = "pet_id"))
  @OrderColumn(name = "sort_order")
  @Column(name = "photo_url", nullable = false)
  private List<String> photoUrls = new ArrayList<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "pet_tags", joinColumns = @JoinColumn(name = "pet_id"))
  @OrderColumn(name = "sort_order")
  private List<Tag> tags = new ArrayList<>();

  @Column(nullable = false)
  private String status;
}
