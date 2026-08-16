package com.casacrew.dto;

import java.math.BigDecimal;

public record UserResponseDTO(
        Long id,
        String username,
        String fullName,
        String email,
        String role,
        String roomName,
        String phoneNumber,
        String emergencyPhoneNumber,
        String studyOrWork,
        String parentsAddress,
        String favoriteMeal,
        String socialPreference,
        String mealPreference,
        String availabilityStatus,
        boolean statusToggle,
        String profileImagePath,
        String contractFile,
        BigDecimal rentAmount
) {}