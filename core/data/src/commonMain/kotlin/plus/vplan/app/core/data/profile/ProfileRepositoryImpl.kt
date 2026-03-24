package plus.vplan.app.core.data.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.ProfileDao
import plus.vplan.app.core.database.model.database.DbGroupProfile
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbTeacherProfile
import plus.vplan.app.core.model.Profile
import kotlin.uuid.Uuid

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao,
) : ProfileRepository {

    override fun getAll(): Flow<List<Profile>> {
        return profileDao.getAll()
            .map { items -> items.mapNotNull { it.toModel() } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun getById(id: Uuid): Flow<Profile?> {
        return profileDao.getById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override suspend fun delete(profile: Profile) {
        profileDao.deleteById(profile.id)
    }

    override suspend fun save(profile: Profile) {
        val id = profile.id

        profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = profile.school.id,
                displayName = profile.name
            )
        )

        when (profile) {
            is Profile.StudentProfile -> {
                profileDao.upsertGroupProfile(DbGroupProfile(
                    profileId = id,
                    groupId = profile.group.id,
                    vppId = profile.vppId?.id
                ))

                profileDao.replaceSubjectInstanceConfiguration(profile)
            }

            is Profile.TeacherProfile -> {
                profileDao.upsertTeacherProfile(
                    DbTeacherProfile(
                        profileId = id,
                        teacherId = profile.teacher.id
                    )
                )
            }
        }
    }
}