package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank(message = "token is verplicht") String token,
        @NotBlank(message = "newPassword is verplicht")
        @Size(min = 8, max = 255, message = "newPassword moet minimaal 8 tekens zijn") String newPassword
) {
}