package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbWeek

@Dao
interface WeekDao {

    @Upsert
    suspend fun upsert(week: DbWeek)
}