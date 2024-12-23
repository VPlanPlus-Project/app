package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbDay

@Dao
interface DayDao {

    @Upsert
    suspend fun upsert(day: DbDay)
}