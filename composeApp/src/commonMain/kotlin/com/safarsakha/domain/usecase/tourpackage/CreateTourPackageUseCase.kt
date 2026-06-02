package com.safarsakha.domain.usecase.tourpackage


import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.core.utils.Resource

class CreateTourPackageUseCase(
    private val repository: TourPackageRepository
) {
    suspend operator fun invoke(tourPackage: TourPackage): Resource<TourPackage> {
        val validation = tourPackage.validate()
        if (!validation.isValid) {
            return Resource.Error("Validation failed: ${validation.errors.values.joinToString()}")
        }

        return try {
            repository.createTourPackage(tourPackage)
        } catch (e: Exception) {
            Resource.Error("Failed to create tour package: ${e.message}")
        }
    }
}