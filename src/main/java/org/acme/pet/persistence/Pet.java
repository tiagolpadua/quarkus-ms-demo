package org.acme.pet.persistence;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acme.pet.dtos.CategoryData;
import org.acme.pet.dtos.TagData;

@Data
@NoArgsConstructor
@RegisterForReflection
public class Pet {
  private Long id;
  private CategoryData category;
  private String name;
  private List<String> photoUrls = new ArrayList<>();
  private List<TagData> tags = new ArrayList<>();
  private String status;
}
