package com.safarsakha.domain.usecase.tourpackage



import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.core.utils.Resource

class DeleteTourPackageUseCase(
    private val repository: TourPackageRepository
) {
    suspend operator fun invoke(id: String): Resource<Unit> {
        return try {
            repository.deleteTourPackage(id)
        } catch (e: Exception) {
            Resource.Error("Failed to delete tour package: ${e.message}")
        }
    }
}