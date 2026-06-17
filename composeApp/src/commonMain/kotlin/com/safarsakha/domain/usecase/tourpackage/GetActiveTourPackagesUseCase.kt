package com.safarsakha.domain.usecase.tourpackage

import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.core.utils.Resource

class GetActiveTourPackagesUseCase(
    private val repository: TourPackageRepository
) {
    suspend operator fun invoke(): Resource<List<TourPackage>> {
        return repository.getActiveTourPackages()
    }
}