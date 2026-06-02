package com.safarsakha.data.mapper


import com.safarsakha.data.remote.firebase.TourPackageDTO
import com.safarsakha.domain.model.TourPackage
import kotlinx.datetime.Instant

object TourPackageMapper {

    fun toDomain(dto: TourPackageDTO): TourPackage {
        return TourPackage(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            location = dto.location,
            duration = dto.duration,
            price = dto.price,
            imageUrl = dto.imageUrl,
            includedServices = dto.includedServices,
            createdAt = Instant.parse(dto.createdAt),
            updatedAt = Instant.parse(dto.updatedAt),
            isActive = dto.isActive
        )
    }

    fun toDTO(domain: TourPackage): TourPackageDTO {
        return TourPackageDTO(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            location = domain.location,
            duration = domain.duration,
            price = domain.price,
            imageUrl = domain.imageUrl,
            includedServices = domain.includedServices,
            createdAt = domain.createdAt.toString(),
            updatedAt = domain.updatedAt.toString(),
            isActive = domain.isActive
        )
    }
}