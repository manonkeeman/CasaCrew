package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmSubscriptionRequestDTO(
        @NotBlank(message = "token is verplicht")
        String token
) {}
