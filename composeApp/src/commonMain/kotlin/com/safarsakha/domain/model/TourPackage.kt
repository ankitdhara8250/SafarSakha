package com.safarsakha.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class TourPackage(
    val id: String = "",
    val title: String,
    val description: String,
    val location: String,
    val duration: String,
    val price: Double,
    val imageUrl: String? = null,
    val includedServices: List<String>,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val isActive: Boolean = true
) {
    fun validate(): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (title.isBlank()) errors["title"] = "Title is required"
        if (title.length < 3) errors["title"] = "Title must be at least 3 characters"
        if (description.isBlank()) errors["description"] = "Description is required"
        if (description.length < 20) errors["description"] = "Description must be at least 20 characters"
        if (location.isBlank()) errors["location"] = "Location is required"
        if (duration.isBlank()) errors["duration"] = "Duration is required"
        if (price <= 0) errors["price"] = "Price must be greater than 0"
        if (includedServices.isEmpty()) errors["includedServices"] = "At least one service is required"

        return ValidationResult(errors.isEmpty(), errors)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String>
)
