package plus.vplan.app.data.source.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection
import kotlin.time.Instant

@Entity(
    tableName = "besteschule_collections",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["subject_id"], unique = false),
        Index(value = ["interval_id"], unique = false),
        Index(value = ["teacher_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbBesteschuleSubject::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = DbBesteSchuleInterval::class,
            parentColumns = ["id"],
            childColumns = ["interval_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = DbBesteschuleTeacher::class,
            parentColumns = ["id"],
            childColumns = ["teacher_id"],
            onDelete = CASCADE
        )
    ]
)
data class DbBesteSchuleCollection(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo("type") val type: String,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("subject_id") val subjectId: Int,
    @ColumnInfo("given_at") val givenAt: LocalDate,
    @ColumnInfo("interval_id") val intervalId: Int,
    @ColumnInfo("teacher_id") val teacherId: Int,
    @ColumnInfo("cached_at") val cachedAt: Instant
) {
    fun toModel() = BesteSchuleCollection(
        id = this.id,
        type = this.type,
        name = this.name,
        subjectId = this.subjectId,
        givenAt = this.givenAt,
        intervalId = this.intervalId,
        teacherId = this.teacherId,
        cachedAt = this.cachedAt
    )
}