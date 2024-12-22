package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbIndiwareHasTimetableInWeek

@Dao
interface IndiwareDao {

    @Upsert
    suspend fun upsert(indiwareHasTimetableInWeek: DbIndiwareHasTimetableInWeek)

    @Query("SELECT has_data FROM indiware_has_timetable_in_week WHERE week_id = :weekId")
    suspend fun getHasTimetableInWeek(weekId: String): Boolean?
}