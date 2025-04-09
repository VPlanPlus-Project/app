package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "profile_homework_index",
    primaryKeys = ["homework_id", "profile_id"],
    indices = [
        Index("homework_id"),
        Index("profile_id"),
    ],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            entity = DbProfile::class,
            onDelete = CASCADE
        ),ForeignKey(
            parentColumns = ["id"],
            childColumns = ["homework_id"],
            entity = DbHomework::class,
            onDelete = CASCADE
        ),
    ]
)
data class DbProfileHomeworkIndex(
    @ColumnInfo(name = "homework_id") val homeworkId: Int,
    @ColumnInfo(name = "profile_id") val profileId: Uuid
)