package com.safarsakha.domain.repository

import com.safarsakha.domain.model.TourPackage
import com.safarsakha.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface TourPackageRepository {
    suspend fun getAllTourPackages(): Resource<List<TourPackage>>
    fun observeAllTourPackages(): Flow<Resource<List<TourPackage>>>
    suspend fun getTourPackageById(id: String): Resource<TourPackage>
    suspend fun getActiveTourPackages(): Resource<List<TourPackage>>
    suspend fun createTourPackage(tourPackage: TourPackage): Resource<TourPackage>
    suspend fun updateTourPackage(tourPackage: TourPackage): Resource<TourPackage>
    suspend fun deleteTourPackage(id: String): Resource<Unit>
    suspend fun uploadPackageImage(imageBytes: ByteArray, fileName: String): Resource<String>
}