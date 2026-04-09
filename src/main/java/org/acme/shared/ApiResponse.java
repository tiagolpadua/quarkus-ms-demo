package org.acme.shared;

public record ApiResponse(Integer code, String type, String message) {}
