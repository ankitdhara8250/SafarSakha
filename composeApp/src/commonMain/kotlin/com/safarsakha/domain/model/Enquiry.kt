package com.safarsakha.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Enquiry(
    val enquiryId: String = "",
    val tourPackageId: String = "",
    val tourPackageName: String = "",
    val userId: String = "",
    val userName: String = "",
    val enquiryMessage: String = "",
    val adminReply: String? = null,
    val enquiryStatus: EnquiryStatus = EnquiryStatus.PENDING,
    val createdAt: Instant = Clock.System.now(),
    val repliedAt: Instant? = null
)

enum class EnquiryStatus {
    PENDING, REPLIED
}