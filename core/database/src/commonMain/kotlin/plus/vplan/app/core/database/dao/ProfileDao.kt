package plus.vplan.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.database.model.database.DbGroupProfile
import plus.vplan.app.core.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbRoomProfile
import plus.vplan.app.core.database.model.database.DbTeacherProfile
import plus.vplan.app.core.database.model.embedded.EmbeddedProfile
import plus.vplan.app.core.model.Profile
import kotlin.uuid.Uuid

@Dao
interface ProfileDao {

    @Transaction
    @Query("SELECT * FROM profiles")
    fun getAll(): Flow<List<EmbeddedProfile>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: DbProfile): Long

    @Transaction
    @Upsert
    suspend fun upsertGroupProfile(profile: DbGroupProfile)

    @Upsert
    suspend fun upsertTeacherProfile(profile: DbTeacherProfile)

    @Upsert
    suspend fun upsertRoomProfile(profile: DbRoomProfile)

    @Transaction
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getById(id: Uuid): Flow<EmbeddedProfile?>

    @Upsert
    suspend fun upsertGroupProfileDisabledSubjectInstances(config: List<FKGroupProfileDisabledSubjectInstances>)

    @Query("INSERT OR REPLACE INTO fk_group_profile_disabled_subject_instances (profile_id, subject_instance_id) VALUES (:profileId, :subjectInstanceId)")
    suspend fun insertDisabledSubjectInstances(profileId: Uuid, subjectInstanceId: Uuid)

    @Query("DELETE FROM fk_group_profile_disabled_subject_instances WHERE subject_instance_id IN (:subjectInstanceIds) AND profile_id = :profileId")
    suspend fun deleteDisabledSubjectInstances(profileId: Uuid, subjectInstanceIds: List<Uuid>)

    @Transaction
    suspend fun replaceSubjectInstanceConfiguration(profile: Profile.StudentProfile) {
        val existing = getById(profile.id).first()?.toModel() as? Profile.StudentProfile
        val subjectInstanceConfigurationsToBeDeleted = existing
            ?.subjectInstanceConfiguration.orEmpty()
            .filter { (sid, active) ->
                val newActive = profile.subjectInstanceConfiguration[sid] ?: true
                active != newActive
            }

        deleteDisabledSubjectInstances(profile.id, subjectInstanceConfigurationsToBeDeleted.keys.toList())
        profile.subjectInstanceConfiguration.forEach { (sid, active) ->
            if (!active) insertDisabledSubjectInstances(profile.id, sid)
        }
    }

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Uuid)
}