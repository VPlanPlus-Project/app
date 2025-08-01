package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbHoliday
import kotlin.uuid.Uuid

@Dao
interface HolidayDao {

    @Upsert
    suspend fun upsert(holiday: DbHoliday)

    @Transaction
    suspend fun upsert(holidays: List<DbHoliday>) {
        holidays.forEach { upsert(it) }
    }

    @Transaction
    @Query("SELECT * FROM holidays WHERE school_id = :schoolId")
    fun getBySchoolId(schoolId: Uuid): Flow<List<DbHoliday>>

    @Query("DELETE FROM holidays WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}