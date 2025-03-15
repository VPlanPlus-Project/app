package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.index.DbProfileTimetableIndex
import kotlin.uuid.Uuid

@Dao
interface ProfileTimetableCacheDao {
    @Query("DELETE FROM profile_timetable_index WHERE profile_id = :profileId")
    suspend fun deleteCacheForProfile(profileId: Uuid)

    @Upsert
    suspend fun upsert(entries: List<DbProfileTimetableIndex>)
}