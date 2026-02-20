package plus.vplan.app.core.database.model.database.foreign_key

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "fk_homework_file",
    primaryKeys = ["homework_id", "file_id"],
    indices = [
        Index("homework_id", unique = false),
        Index("file_id", unique = false)
    ]
)
data class FKHomeworkFile(
    @ColumnInfo("homework_id") val homeworkId: Int,
    @ColumnInfo("file_id") val fileId: Int
)