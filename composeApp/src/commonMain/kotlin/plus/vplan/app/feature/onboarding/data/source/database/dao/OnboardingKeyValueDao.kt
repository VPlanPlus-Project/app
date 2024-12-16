package plus.vplan.app.feature.onboarding.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OnboardingKeyValueDao {

    @Query("INSERT OR REPLACE INTO onboarding_key_value (`key`, value) VALUES (:key, :value)")
    suspend fun insert(key: String, value: String)

    @Query("SELECT value FROM onboarding_key_value WHERE `key` = :key")
    fun get(key: String): Flow<String?>

    @Query("DELETE FROM onboarding_key_value WHERE `key` = :key")
    suspend fun delete(key: String)
}