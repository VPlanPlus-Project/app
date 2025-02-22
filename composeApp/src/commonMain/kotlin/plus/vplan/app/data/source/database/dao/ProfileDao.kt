package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbGroupProfileDisabledDefaultLessons
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
    suspend fun upsertGroupProfileDisabledDefaultLessons(config: List<DbGroupProfileDisabledDefaultLessons>)

    @Query("INSERT OR REPLACE INTO profiles_group_disabled_default_lessons (profile_id, default_lesson_id) VALUES (:profileId, :defaultLessonId)")
    suspend fun insertDisabledDefaultLessons(profileId: Uuid, defaultLessonId: Int)

    @Query("DELETE FROM profiles_group_disabled_default_lessons WHERE default_lesson_id IN (:defaultLessonIds) AND profile_id = :profileId")
    suspend fun deleteDisabledDefaultLessons(profileId: Uuid, defaultLessonIds: List<Int>)

    @Query("UPDATE profiles SET display_name = :displayName WHERE id = :id")
    suspend fun updateDisplayName(id: Uuid, displayName: String?)

    @Query("UPDATE profiles_group SET vpp_id = :vppId WHERE profile_id = :id")
    suspend fun updateVppId(id: Uuid, vppId: Int?)
}