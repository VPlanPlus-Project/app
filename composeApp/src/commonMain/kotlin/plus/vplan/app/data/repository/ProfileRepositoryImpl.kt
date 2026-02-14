package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.core.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileRepositoryImpl(
    private val vppDatabase: VppDatabase
) : ProfileRepository {
    override fun getById(id: Uuid): Flow<Profile?> {
        return vppDatabase.profileDao.getById(id).map { it?.toModel() }.distinctUntilChanged()
    }

    override fun getAll(): Flow<List<Profile>> {
        return vppDatabase.profileDao.getAll().map { it.mapNotNull { profile -> profile.toModel() } }.distinctUntilChanged()
    }

    override suspend fun upsert(
        group: Group,
        disabledSubjectInstances: List<SubjectInstance>
    ): Profile.StudentProfile {
        val id = Uuid.random()
        vppDatabase.profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = group.schoolId,
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
        disabledSubjectInstances.forEach {
            vppDatabase.profileDao.insertDisabledSubjectInstances(
                profileId = id,
                subjectInstanceId = it.id
            )
        }

        return getById(id).first() as Profile.StudentProfile
    }

    override suspend fun upsert(teacher: Teacher): Profile.TeacherProfile {
        val id = Uuid.random()
        vppDatabase.profileDao.upsert(
            DbProfile(
                id = id,
                schoolId = teacher.schoolId,
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

    override suspend fun setSubjectInstancesEnabled(
        profileId: Uuid,
        subjectInstanceIds: List<Uuid>,
        enable: Boolean
    ) {
        if (enable) vppDatabase.profileDao.deleteDisabledSubjectInstances(profileId, subjectInstanceIds)
        else vppDatabase.profileDao.upsertGroupProfileDisabledSubjectInstances(subjectInstanceIds.map {
            FKGroupProfileDisabledSubjectInstances(
                profileId = profileId,
                subjectInstanceId = it
            )
        })
    }

    override suspend fun deleteProfile(profileId: Uuid) {
        vppDatabase.profileDao.deleteById(profileId)
    }

    override suspend fun updateDisplayName(id: Uuid, displayName: String) {
        vppDatabase.profileDao.updateDisplayName(id, displayName.ifBlank { null })
    }

    override suspend fun updateVppId(id: Uuid, vppId: Int?) {
        vppDatabase.profileDao.updateVppId(id, vppId)
    }
}