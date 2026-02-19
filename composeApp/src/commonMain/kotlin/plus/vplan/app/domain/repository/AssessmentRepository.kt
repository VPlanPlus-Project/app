@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.data.repository.AssessmentGetResponse
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface AssessmentRepository: WebEntityRepository<Assessment> {

    /**
     * @return List of assessment DTOs
     */
    suspend fun download(
        schoolApiAccess: VppSchoolAuthentication,
        subjectInstanceAliases: List<Alias>
    ): Response<List<AssessmentGetResponse>>

    suspend fun createAssessmentOnline(
        vppId: VppId.Active,
        date: LocalDate,
        type: Assessment.Type,
        subjectInstanceId: Int,
        isPublic: Boolean,
        content: String
    ): Response<Int>

    suspend fun linkFileToAssessmentOnline(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    ): Response.Error?

    suspend fun linkFileToAssessment(
        assessmentId: Int,
        fileId: Int
    )

    suspend fun unlinkFileFromAssessment(assessmentId: Int, fileId: Int)

    fun getAll(): Flow<List<Assessment>>

    fun getByDate(date: LocalDate): Flow<List<Assessment>>
    fun getByProfile(profileId: Uuid, date: LocalDate? = null): Flow<List<Assessment>>

    suspend fun deleteAssessment(assessment: Assessment, profile: Profile.StudentProfile): Response.Error?

    suspend fun getIdForNewLocalAssessment(): Int

    suspend fun upsertLocally(
        assessmentId: Int,
        subjectInstanceId: Uuid,
        date: LocalDate,
        isPublic: Boolean?,
        createdAt: Instant,
        createdBy: Int?,
        createdByProfile: Uuid?,
        description: String,
        type: Assessment.Type,
        associatedFileIds: List<Int>
    )

    suspend fun changeType(
        assessment: Assessment,
        type: Assessment.Type,
        profile: Profile.StudentProfile,
    )

    suspend fun changeDate(
        assessment: Assessment,
        date: LocalDate,
        profile: Profile.StudentProfile
    )

    suspend fun changeVisibility(
        assessment: Assessment,
        isPublic: Boolean,
        profile: Profile.StudentProfile
    )

    suspend fun changeContent(
        assessment: Assessment,
        profile: Profile.StudentProfile,
        content: String
    )

    suspend fun clearCache()

    suspend fun dropIndicesForProfile(profileId: Uuid)
    suspend fun createCacheForProfile(profileId: Uuid, assessmentIds: Collection<Int>)

    suspend fun deleteById(id: Int)
}