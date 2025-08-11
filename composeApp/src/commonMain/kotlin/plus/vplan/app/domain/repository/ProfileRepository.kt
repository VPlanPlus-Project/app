package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.Teacher
import kotlin.uuid.Uuid

interface ProfileRepository {
    fun getById(id: Uuid): Flow<Profile?>
    fun getAll(): Flow<List<Profile>>
    suspend fun upsert(
        group: Group,
        disabledSubjectInstances: List<SubjectInstance>
    ): Profile.StudentProfile

    suspend fun upsert(
        teacher: Teacher
    ): Profile.TeacherProfile

    suspend fun updateDisplayName(id: Uuid, displayName: String)
    suspend fun updateVppId(id: Uuid, vppId: Int?)

    suspend fun setSubjectInstancesEnabled(
        profileId: Uuid,
        subjectInstanceIds: List<Uuid>,
        enable: Boolean
    )

    suspend fun deleteProfile(profileId: Uuid)
}