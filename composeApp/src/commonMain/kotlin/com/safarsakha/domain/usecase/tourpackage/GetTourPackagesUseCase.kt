package com.safarsakha.domain.usecase.tourpackage

import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.core.utils.Resource
import kotlinx.coroutines.flow.Flow

class GetTourPackagesUseCase(
    private val repository: TourPackageRepository
) {
    suspend operator fun invoke(): Resource<List<TourPackage>> {
        return repository.getAllTourPackages()
    }

    fun observe(): Flow<Resource<List<TourPackage>>> {
        return repository.observeAllTourPackages()
    }
}