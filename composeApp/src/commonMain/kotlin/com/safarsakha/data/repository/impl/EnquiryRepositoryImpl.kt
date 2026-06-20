package com.safarsakha.data.repository.impl

import com.safarsakha.core.utils.Resource
import com.safarsakha.data.mapper.EnquiryMapper
import com.safarsakha.data.remote.firebase.FirebaseEnquiryDataSource
import com.safarsakha.domain.model.Enquiry
import com.safarsakha.domain.repository.EnquiryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class EnquiryRepositoryImpl(
    private val dataSource: FirebaseEnquiryDataSource
) : EnquiryRepository {

    override suspend fun submitEnquiry(enquiry: Enquiry): Resource<Enquiry> {
        return try {
            val dto = EnquiryMapper.toDTO(enquiry)
            val id = dataSource.submitEnquiry(dto)
            Resource.Success(enquiry.copy(enquiryId = id))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit enquiry")
        }
    }

    override suspend fun getAllEnquiries(): Resource<List<Enquiry>> {
        return try {
            val dtos = dataSource.getAllEnquiries()
            Resource.Success(dtos.map { EnquiryMapper.toDomain(it) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch enquiries")
        }
    }

    override fun observeAllEnquiries(): Flow<Resource<List<Enquiry>>> {
        return dataSource.observeAllEnquiries().map { dtos ->
            try {
                Resource.Success(dtos.map { EnquiryMapper.toDomain(it) })
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Mapping error")
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Firestore observer error"))
        }
    }

    override suspend fun getEnquiriesByUserId(userId: String): Resource<List<Enquiry>> {
        return try {
            val dtos = dataSource.getEnquiriesByUserId(userId)
            Resource.Success(dtos.map { EnquiryMapper.toDomain(it) })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch your enquiries")
        }
    }

    override fun observeEnquiriesByUserId(userId: String): Flow<Resource<List<Enquiry>>> {
        return dataSource.observeEnquiriesByUserId(userId).map { dtos ->
            try {
                Resource.Success(dtos.map { EnquiryMapper.toDomain(it) })
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Mapping error")
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Firestore observer error"))
        }
    }

    override suspend fun replyToEnquiry(enquiryId: String, adminReply: String): Resource<Unit> {
        return try {
            dataSource.replyToEnquiry(enquiryId, adminReply)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reply")
        }
    }
}