package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.foreign_key.FKGroupProfileDisabledSubjectInstances
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.embedded.EmbeddedProfile
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
    suspend fun insertDisabledSubjectInstances(profileId: Uuid, subjectInstanceId: Int)

    @Query("DELETE FROM fk_group_profile_disabled_subject_instances WHERE subject_instance_id IN (:subjectInstanceIds) AND profile_id = :profileId")
    suspend fun deleteDisabledSubjectInstances(profileId: Uuid, subjectInstanceIds: List<Int>)

    @Query("UPDATE profiles SET display_name = :displayName WHERE id = :id")
    suspend fun updateDisplayName(id: Uuid, displayName: String?)

    @Query("UPDATE profiles_group SET vpp_id = :vppId WHERE profile_id = :id")
    suspend fun updateVppId(id: Uuid, vppId: Int?)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Uuid)
}