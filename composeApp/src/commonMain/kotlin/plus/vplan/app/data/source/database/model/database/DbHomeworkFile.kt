package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "homework_file",
    indices = [Index("id")],
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            parentColumns = ["id"],
            childColumns = ["homework_id"],
            entity = DbHomework::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbHomeworkFile(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("homework_id") val homeworkId: Int,
    @ColumnInfo("file_name") val fileName: String,
    @ColumnInfo("size") val size: Long
)
