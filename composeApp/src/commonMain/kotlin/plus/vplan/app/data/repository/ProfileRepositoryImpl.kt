package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
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

    override suspend fun upsert(
        group: Group,
        disabledDefaultLessons: List<DefaultLesson>
    ): Profile.StudentProfile {
        val id = Uuid.random()
        vppDatabase.profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = group.school.id,
                displayName = group.name
            )
        )
        vppDatabase.profileDao.upsertGroupProfile(
            DbGroupProfile(
                profileId = id,
                groupId = group.id
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
                schoolId = teacher.school.id,
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
                schoolId = room.school.id,
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
}