package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbIndiwareTimetableMetadata

@Dao
interface IndiwareDao {

    @Upsert
    suspend fun upsert(indiwareHasTimetableInWeek: DbIndiwareTimetableMetadata)

    @Query("SELECT * FROM indiware_timetable_metadata WHERE week_id = :weekId")
    suspend fun getHasTimetableInWeek(weekId: String): DbIndiwareTimetableMetadata?
}