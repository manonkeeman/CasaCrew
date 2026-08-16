package com.casacrew.dto;

public record PushUnsubscribeRequestDTO(
        String endpoint,
        String token
) {}
