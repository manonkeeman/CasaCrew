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
