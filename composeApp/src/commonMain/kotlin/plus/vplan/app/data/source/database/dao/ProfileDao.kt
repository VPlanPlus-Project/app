package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.data.source.database.model.embedded.EmbeddedProfile
import plus.vplan.app.domain.model.ProfileType
import kotlin.uuid.Uuid

@Dao
interface ProfileDao {

    @Transaction
    @Query("SELECT * FROM profiles")
    fun getAll(): Flow<List<EmbeddedProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: DbProfile): Long

    @Upsert
    suspend fun upsertGroupProfile(profile: DbGroupProfile)

    @Upsert
    suspend fun upsertTeacherProfile(profile: DbTeacherProfile)

    @Upsert
    suspend fun upsertRoomProfile(profile: DbRoomProfile)

    @Transaction
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getById(id: Uuid): Flow<EmbeddedProfile>

    @Transaction
    suspend fun insertProfile(
        schoolId: Int,
        displayName: String,
        type: ProfileType,
        reference: Int
    ): Uuid {
        val id = Uuid.random()
        upsert(
            DbProfile(
                id = id,
                schoolId = schoolId,
                displayName = displayName
            )
        ).toInt()
        when (type) {
            ProfileType.STUDENT -> {
                upsertGroupProfile(
                    DbGroupProfile(
                        profileId = id,
                        groupId = reference
                    )
                )
            }
            ProfileType.TEACHER -> {
                upsertTeacherProfile(
                    DbTeacherProfile(
                        profileId = id,
                        teacherId = reference
                    )
                )
            }
            ProfileType.ROOM -> {
                upsertRoomProfile(
                    DbRoomProfile(
                        profileId = id,
                        roomId = reference
                    )
                )
            }
        }
        return id
    }
}