package com.safarsakha.domain.usecase.tourpackage

import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.core.utils.Resource

class GetTourPackageByIdUseCase(
    private val repository: TourPackageRepository
) {
    suspend operator fun invoke(id: String): Resource<TourPackage> {
        return repository.getTourPackageById(id)
    }
}
