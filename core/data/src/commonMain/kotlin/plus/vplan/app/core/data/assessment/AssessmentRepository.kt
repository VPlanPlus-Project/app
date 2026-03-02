package plus.vplan.app.core.data.assessment

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import kotlin.uuid.Uuid

interface AssessmentRepository {
    fun getAll(): Flow<List<Assessment>>
    fun getAllForProfile(profile: Profile): Flow<List<Assessment>>
    fun getById(id: Int): Flow<Assessment?>
    fun getByDate(date: LocalDate): Flow<List<Assessment>>
    fun getByProfile(profileId: Uuid, date: LocalDate? = null): Flow<List<Assessment>>

    suspend fun save(assessment: Assessment)
    suspend fun delete(assessment: Assessment)
    suspend fun deleteById(id: Int)
    suspend fun deleteAssessment(assessment: Assessment, profile: Profile.StudentProfile): Response.Error?

    suspend fun sync(schoolApiAccess: VppSchoolAuthentication, subjectInstanceAliases: List<Alias>)
    
    suspend fun syncById(schoolApiAccess: VppSchoolAuthentication, assessmentId: Int, forceReload: Boolean = false): Boolean

    suspend fun getIdForNewLocalAssessment(): Int

    suspend fun updateAssessmentMetadata(
        assessment: Assessment,
        date: Optional<LocalDate> = Optional.Absent,
        type: Optional<Assessment.Type> = Optional.Absent,
        isPublic: Optional<Boolean> = Optional.Absent,
        content: Optional<String> = Optional.Absent,
        profile: Profile.StudentProfile
    )

    suspend fun createAssessmentOnline(
        vppId: VppId.Active,
        date: LocalDate,
        type: Assessment.Type,
        subjectInstanceId: Int,
        isPublic: Boolean,
        content: String
    ): Response<Int>

    suspend fun linkFile(vppId: VppId.Active?, assessmentId: Int, fileId: Int): Response<Unit>
    suspend fun unlinkFile(vppId: VppId.Active?, assessmentId: Int, fileId: Int): Response<Unit>

    suspend fun clearCache()
    suspend fun dropIndexForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, assessmentIds: Collection<Int>)
}
