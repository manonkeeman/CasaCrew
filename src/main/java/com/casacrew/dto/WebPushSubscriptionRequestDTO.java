package com.casacrew.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WebPushSubscriptionRequestDTO(
        @NotBlank(message = "endpoint is verplicht")
        String endpoint,

        @NotNull(message = "keys is verplicht")
        @Valid
        Keys keys
) {
    public record Keys(
            @NotBlank(message = "p256dh is verplicht")
            String p256dh,

            @NotBlank(message = "auth is verplicht")
            String auth
    ) {}
}
