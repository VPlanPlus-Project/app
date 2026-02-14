package plus.vplan.app.domain.repository

import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.School
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
) {
    companion object {
        fun fromModel(school: School, creationReason: CreationReason) = SchoolDbDto(
            name = school.name,
            aliases = school.aliases.toList(),
            creationReason = creationReason
        )
    }
}

class VppSchoolDto(
    val id: Int,
    val name: String,
    val aliases: List<Alias>
)