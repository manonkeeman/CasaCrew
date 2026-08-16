package com.casacrew.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organization_slug", columnNames = "slug")
        }
)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name may not exceed 120 characters")
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 60, message = "Slug may not exceed 60 characters")
    @Column(nullable = false, length = 60)
    private String slug;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Size(max = 80, message = "Bunq.me-gebruikersnaam mag maximaal 80 tekens zijn")
    @Column(name = "bunq_me_username", length = 80)
    private String bunqMeUsername;

    @Size(max = 34, message = "IBAN mag maximaal 34 tekens zijn")
    @Column(name = "iban", length = 34)
    private String iban;

    @Size(max = 120, message = "Naam rekeninghouder mag maximaal 120 tekens zijn")
    @Column(name = "account_holder_name", length = 120)
    private String accountHolderName;

    public Organization() {
    }

    public Organization(String name, String slug) {
        this.name = require(name, "name");
        this.slug = require(slug, "slug");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = require(name, "name");
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = require(slug, "slug");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getBunqMeUsername() {
        return bunqMeUsername;
    }

    public void setBunqMeUsername(String bunqMeUsername) {
        this.bunqMeUsername = blankToNull(bunqMeUsername);
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = blankToNull(iban);
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = blankToNull(accountHolderName);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String require(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organization other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
