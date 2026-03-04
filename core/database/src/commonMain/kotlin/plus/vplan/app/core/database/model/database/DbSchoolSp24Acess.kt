package plus.vplan.app.core.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

@Entity(
    tableName = "school_sp24_access",
    primaryKeys = ["sp24_school_id"],
    indices = [
        Index(value = ["sp24_school_id"], unique = true),
        Index(value = ["school_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbSchool::class,
            parentColumns = ["id"],
            childColumns = ["school_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DbSchoolSp24Acess(
    @ColumnInfo("school_id") val schoolId: Uuid,
    @ColumnInfo("sp24_school_id") val sp24SchoolId: String,
    @ColumnInfo("username") val username: String,
    @ColumnInfo("password") val password: String,
    @ColumnInfo("days_per_week") val daysPerWeek: Int,
    @ColumnInfo("credentials_valid") val credentialsValid: Boolean
)
