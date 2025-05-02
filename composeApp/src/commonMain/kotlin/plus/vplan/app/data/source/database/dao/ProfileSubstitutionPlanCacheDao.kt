@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import plus.vplan.app.data.source.database.model.database.DbProfileSubstitutionPlanCache
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Dao
interface ProfileSubstitutionPlanCacheDao {
    @Query("DELETE FROM profile_substitution_plan_cache WHERE profile_id = :profileId AND (:version IS NULL OR substitution_lesson_id IN (SELECT substitution_lesson_id FROM substitution_plan_lesson WHERE version = :version))")
    suspend fun deleteCacheForProfile(profileId: Uuid, version: String?)

    @Upsert
    suspend fun upsert(entries: List<DbProfileSubstitutionPlanCache>)
}