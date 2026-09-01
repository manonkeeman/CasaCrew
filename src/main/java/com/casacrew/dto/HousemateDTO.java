package com.casacrew.dto;

// Privacy-getrimde variant van UserResponseDTO voor huisgenoten onderling:
// geen e-mail, telefoonnummer, noodnummer, adres ouders, huurbedrag of
// contractbestand -- alleen wat relevant is om elkaar te leren kennen.
public record HousemateDTO(
        Long id,
        String username,
        String fullName,
        String roomName,
        String studyOrWork,
        String favoriteMeal,
        String socialPreference,
        String availabilityStatus,
        String profileImagePath
) {}
