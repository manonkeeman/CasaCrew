package com.casacrew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record ComplaintStatusUpdateDTO(
        @NotBlank(message = "Status is verplicht")
        @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED", message = "Status moet OPEN, IN_PROGRESS of RESOLVED zijn")
        String status,

        @Size(max = 2000, message = "Reactie mag maximaal 2000 tekens zijn")
        String response
) {}
