package org.acme.shared.pagination;

import java.util.List;

public record PagedResponse<T>(List<T> items, PageMetadata page, SortMetadata sort) {}
