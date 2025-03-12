package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbProfileSubstitutionPlanCache
import kotlin.uuid.Uuid

@Dao
interface ProfileSubstitutionPlanCacheDao {
    @Query("DELETE FROM profile_substitution_plan_cache WHERE profile_id = :profileId")
    suspend fun deleteCacheForProfile(profileId: Uuid)

    @Upsert
    suspend fun upsert(entries: List<DbProfileSubstitutionPlanCache>)
}