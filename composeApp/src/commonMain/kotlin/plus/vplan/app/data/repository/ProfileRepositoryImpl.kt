package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileRepositoryImpl(
    private val vppDatabase: VppDatabase
) : ProfileRepository {
    override fun getById(id: Uuid): Flow<Profile?> {
        return vppDatabase.profileDao.getById(id).map { it?.toModel() }
    }

    override fun getAll(): Flow<List<Profile>> {
        return vppDatabase.profileDao.getAll().map { it.mapNotNull { profile -> profile.toModel() } }.distinctUntilChanged()
    }

    override suspend fun upsert(
        group: Group,
        disabledDefaultLessons: List<DefaultLesson>
    ): Profile.StudentProfile {
        val id = Uuid.random()
        vppDatabase.profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = group.school.getItemId().toInt(),
                displayName = group.name
            )
        )
        vppDatabase.profileDao.upsertGroupProfile(
            DbGroupProfile(
                profileId = id,
                groupId = group.id,
                vppId = null
            )
        )
        disabledDefaultLessons.forEach {
            vppDatabase.profileDao.insertDisabledDefaultLesson(
                profileId = id,
                defaultLessonId = it.id
            )
        }

        return getById(id).first() as Profile.StudentProfile
    }

    override suspend fun upsert(teacher: Teacher): Profile.TeacherProfile {
        val id = Uuid.random()
        vppDatabase.profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = teacher.school.getItemId().toInt(),
                displayName = teacher.name
            )
        )

        vppDatabase.profileDao.upsertTeacherProfile(
            DbTeacherProfile(
                profileId = id,
                teacherId = teacher.id
            )
        )

        return getById(id).first { it != null } as Profile.TeacherProfile
    }

    override suspend fun upsert(room: Room): Profile.RoomProfile {
        val id = Uuid.random()
        vppDatabase.profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = room.school.getItemId().toInt(),
                displayName = room.name
            )
        )

        vppDatabase.profileDao.upsertRoomProfile(
            DbRoomProfile(
                profileId = id,
                roomId = room.id
            )
        )

        return getById(id).first { it != null } as Profile.RoomProfile
    }

    override suspend fun setDefaultLessonEnabled(
        profileId: Uuid,
        defaultLessonId: String,
        enable: Boolean
    ) {
        if (enable) vppDatabase.profileDao.deleteDisabledDefaultLesson(profileId, defaultLessonId)
        else vppDatabase.profileDao.insertDisabledDefaultLesson(profileId, defaultLessonId)
    }

    override suspend fun updateDisplayName(id: Uuid, displayName: String) {
        vppDatabase.profileDao.updateDisplayName(id, displayName.ifBlank { null })
    }

    override suspend fun updateVppId(id: Uuid, vppId: Int?) {
        vppDatabase.profileDao.updateVppId(id, vppId)
    }
}