package plus.vplan.app.data.source.database.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import plus.vplan.app.domain.model.School

@Entity(
    tableName = "school_indiware_access",
    primaryKeys = ["school_id"],
    indices = [Index(value = ["school_id"], unique = true)],
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
data class DbSchoolIndiwareAccess(
    @ColumnInfo("school_id") val schoolId: Int,
    @ColumnInfo("sp24_school_id") val sp24SchoolId: String,
    @ColumnInfo("username") val username: String,
    @ColumnInfo("password") val password: String,
    @ColumnInfo("days_per_week") val daysPerWeek: Int,
    @ColumnInfo("students_have_full_access") val studentsHaveFullAccess: Boolean,
    @ColumnInfo("download_mode") val downloadMode: School.IndiwareSchool.SchoolDownloadMode,
    @ColumnInfo("credentials_valid") val credentialsValid: Boolean
)
