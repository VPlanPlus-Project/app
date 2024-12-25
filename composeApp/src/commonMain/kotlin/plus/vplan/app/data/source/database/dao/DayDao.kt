package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.model.database.DbDay
import plus.vplan.app.data.source.database.model.embedded.EmbeddedDay

@Dao
interface DayDao {

    @Upsert
    suspend fun upsert(day: DbDay)

    @Transaction
    @Query("SELECT * FROM day WHERE date = :date AND school_id = :schoolId")
    fun getBySchool(date: LocalDate, schoolId: Int): Flow<EmbeddedDay?>
}