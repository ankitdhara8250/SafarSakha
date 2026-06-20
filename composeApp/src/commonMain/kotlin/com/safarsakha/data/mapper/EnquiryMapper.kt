package com.safarsakha.data.mapper

import com.safarsakha.data.remote.firebase.EnquiryDTO
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.model.EnquiryStatus
import kotlinx.datetime.Instant

object EnquiryMapper {

    fun toDomain(dto: EnquiryDTO): Enquiry {
        return Enquiry(
            enquiryId = dto.enquiryId,
            tourPackageId = dto.tourPackageId,
            tourPackageName = dto.tourPackageName,
            userId = dto.userId,
            userName = dto.userName,
            enquiryMessage = dto.enquiryMessage,
            adminReply = dto.adminReply,
            enquiryStatus = if (dto.enquiryStatus == "REPLIED") EnquiryStatus.REPLIED else EnquiryStatus.PENDING,
            createdAt = runCatching { Instant.parse(dto.createdAt) }.getOrDefault(kotlinx.datetime.Clock.System.now()),
            repliedAt = dto.repliedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
        )
    }

    fun toDTO(enquiry: Enquiry): EnquiryDTO {
        return EnquiryDTO(
            enquiryId = enquiry.enquiryId,
            tourPackageId = enquiry.tourPackageId,
            tourPackageName = enquiry.tourPackageName,
            userId = enquiry.userId,
            userName = enquiry.userName,
            enquiryMessage = enquiry.enquiryMessage,
            adminReply = enquiry.adminReply,
            enquiryStatus = enquiry.enquiryStatus.name,
            createdAt = enquiry.createdAt.toString(),
            repliedAt = enquiry.repliedAt?.toString()
        )
    }
}