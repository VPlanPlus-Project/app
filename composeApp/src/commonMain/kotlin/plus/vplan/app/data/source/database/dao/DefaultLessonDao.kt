package plus.vplan.app.data.source.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.DbDefaultLessonIdentifier
import plus.vplan.app.data.source.database.model.embedded.EmbeddedDefaultLesson
import kotlin.uuid.Uuid

@Dao
interface DefaultLessonDao {

    @Transaction
    @Query("SELECT * FROM default_lessons WHERE group_id = :groupId")
    fun getByGroup(groupId: Uuid): Flow<List<EmbeddedDefaultLesson>>

    @Transaction
    @Query("SELECT * FROM default_lessons WHERE entity_id = :id")
    fun getByAppId(id: Uuid): Flow<EmbeddedDefaultLesson?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, MIN(default_lessons.entity_id) FROM default_lesson_identifiers LEFT JOIN default_lessons ON default_lessons.entity_id = default_lesson_identifiers.default_lesson_id WHERE value = :id AND origin = 'vpp' GROUP BY default_lessons.entity_id")
    fun findById(id: String): Flow<EmbeddedDefaultLesson?>

    @Upsert
    suspend fun upsert(entity: DbDefaultLesson)

    @Upsert
    suspend fun upsertDefaultLessonIdentifier(defaultLessonIdentifier: DbDefaultLessonIdentifier)
}