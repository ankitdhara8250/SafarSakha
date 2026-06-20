package com.safarsakha.domain.repository

import com.safarsakha.core.utils.Resource
import com.safarsakha.domain.model.Enquiry
import kotlinx.coroutines.flow.Flow

interface EnquiryRepository {
    suspend fun submitEnquiry(enquiry: Enquiry): Resource<Enquiry>
    suspend fun getAllEnquiries(): Resource<List<Enquiry>>
    fun observeAllEnquiries(): Flow<Resource<List<Enquiry>>>
    suspend fun getEnquiriesByUserId(userId: String): Resource<List<Enquiry>>
    fun observeEnquiriesByUserId(userId: String): Flow<Resource<List<Enquiry>>>
    suspend fun replyToEnquiry(enquiryId: String, adminReply: String): Resource<Unit>
}