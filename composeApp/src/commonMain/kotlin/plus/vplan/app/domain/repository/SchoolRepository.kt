package plus.vplan.app.domain.repository

import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.base.AliasedItemRepository
import kotlin.uuid.Uuid

interface SchoolRepository : AliasedItemRepository<SchoolDbDto, School> {
    suspend fun downloadSchools(): Response<List<VppSchoolDto>>
    suspend fun downloadById(identifier: String): Response<VppSchoolDto>
    suspend fun setSp24Access(
        schoolId: Uuid,
        sp24Id: Int,
        username: String,
        password: String,
        daysPerWeek: Int,
    )
    suspend fun setSp24CredentialValidity(schoolId: Uuid, valid: Boolean)

    suspend fun deleteSchool(schoolId: Uuid)
}

class SchoolDbDto(
    val name: String,
    val aliases: List<Alias>,
    val creationReason: CreationReason
)

class VppSchoolDto(
    val id: Int,
    val name: String,
    val aliases: List<Alias>
)