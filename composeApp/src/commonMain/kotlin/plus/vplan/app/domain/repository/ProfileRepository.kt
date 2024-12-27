package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.Teacher
import kotlin.uuid.Uuid

interface ProfileRepository {
    fun getById(id: Uuid): Flow<Profile?>
    fun getAll(): Flow<List<Profile>>
    suspend fun upsert(
        group: Group,
        disabledDefaultLessons: List<DefaultLesson>
    ): Profile.StudentProfile

    suspend fun upsert(
        teacher: Teacher
    ): Profile.TeacherProfile

    suspend fun upsert(
        room: Room
    ): Profile.RoomProfile

    suspend fun updateDisplayName(id: Uuid, displayName: String)
    suspend fun updateVppId(id: Uuid, vppId: Int?)

    suspend fun setDefaultLessonEnabled(
        profileId: Uuid,
        defaultLessonId: String,
        enable: Boolean
    )
}