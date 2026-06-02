package com.safarsakha.data.repository.impl

import com.safarsakha.data.mapper.TourPackageMapper
import com.safarsakha.data.remote.firebase.FirebaseTourPackageDataSource
import com.safarsakha.domain.model.TourPackage
import com.safarsakha.domain.repository.TourPackageRepository
import com.safarsakha.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class TourPackageRepositoryImpl(
    private val dataSource: FirebaseTourPackageDataSource
) : TourPackageRepository {

    override suspend fun getAllTourPackages(): Resource<List<TourPackage>> {
        return try {
            val dtos = dataSource.getAllTourPackages()
            val packages = dtos.map { TourPackageMapper.toDomain(it) }
            Resource.Success(packages)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    override fun observeAllTourPackages(): Flow<Resource<List<TourPackage>>> {
        return dataSource.observeTourPackages().map { dtos ->
            try {
                val packages = dtos.map { TourPackageMapper.toDomain(it) }
                Resource.Success(packages)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Mapping error occurred")
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Firestore observer error"))
        }
    }

    override suspend fun getTourPackageById(id: String): Resource<TourPackage> {
        return try {
            val dto = dataSource.getTourPackageById(id)
            if (dto != null) {
                Resource.Success(TourPackageMapper.toDomain(dto))
            } else {
                Resource.Error("Tour package not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getActiveTourPackages(): Resource<List<TourPackage>> {
        return try {
            val result = getAllTourPackages()
            when (result) {
                is Resource.Success -> {
                    val activePackages = result.data!!.filter { it.isActive }
                    Resource.Success(activePackages)
                }
                is Resource.Error -> {
                    Resource.Error(result.message ?: "Failed to fetch active packages")
                }
                else -> {
                    Resource.Error("Unknown error occurred")
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun createTourPackage(tourPackage: TourPackage): Resource<TourPackage> {
        return try {
            val dto = TourPackageMapper.toDTO(tourPackage)
            val id = dataSource.createTourPackage(dto)
            val createdPackage = tourPackage.copy(id = id)
            Resource.Success(createdPackage)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create tour package")
        }
    }

    override suspend fun updateTourPackage(tourPackage: TourPackage): Resource<TourPackage> {
        return try {
            val dto = TourPackageMapper.toDTO(tourPackage)
            dataSource.updateTourPackage(tourPackage.id, dto)
            Resource.Success(tourPackage)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update tour package")
        }
    }

    override suspend fun deleteTourPackage(id: String): Resource<Unit> {
        return try {
            dataSource.deleteTourPackage(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete tour package")
        }
    }

    override suspend fun uploadPackageImage(imageBytes: ByteArray, fileName: String): Resource<String> {
        return try {
            val url = dataSource.uploadImage(imageBytes, fileName)
            if (url != null) {
                Resource.Success(url)
            } else {
                Resource.Error("Failed to get image URL")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload image")
        }
    }
}