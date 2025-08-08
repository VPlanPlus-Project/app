package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbStundenplan24TimetableMetadata

@Dao
interface Stundenplan24Dao {

    @Upsert
    suspend fun upsert(indiwareHasTimetableInWeek: DbStundenplan24TimetableMetadata)

    @Query("SELECT * FROM stundenplan24_timetable_metadata WHERE week_id = :weekId AND stundenplan24_school_id = :indiwareSchoolId")
    suspend fun getHasTimetableInWeek(weekId: String, indiwareSchoolId: String): DbStundenplan24TimetableMetadata?
}