package org.acme.store.resources.dtos;

import java.util.Map;

public record InventoryResponse(Map<String, Integer> inventory) {}
