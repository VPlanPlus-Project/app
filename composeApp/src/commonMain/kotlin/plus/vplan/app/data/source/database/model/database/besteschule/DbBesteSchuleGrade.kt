package plus.vplan.app.data.source.database.model.database.besteschule

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import kotlin.time.Instant

@Entity(
    tableName = "besteschule_grades",
    primaryKeys = ["id"],
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["schulverwalter_user_id"], unique = false),
        Index(value = ["collection_id"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = DbBesteSchuleCollection::class,
            parentColumns = ["id"],
            childColumns = ["collection_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbBesteSchuleGrade(
    @ColumnInfo("id") val id: Int,
    @ColumnInfo(name = "value") val value: String?,
    @ColumnInfo(name = "is_optional") val isOptional: Boolean,
    @ColumnInfo(name = "is_selected_for_final_grade") val isSelectedForFinalGrade: Boolean,
    @ColumnInfo(name = "schulverwalter_user_id") val schulverwalterUserId: Int,
    @ColumnInfo(name = "collection_id") val collectionId: Int,
    @ColumnInfo(name = "given_at") val givenAt: LocalDate,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant
) {
    fun toModel() = BesteSchuleGrade(
        id = this.id,
        value = this.value,
        isOptional = this.isOptional,
        isSelectedForFinalGrade = this.isSelectedForFinalGrade,
        schulverwalterUserId = this.schulverwalterUserId,
        collectionId = this.collectionId,
        givenAt = this.givenAt,
        cachedAt = this.cachedAt
    )
}
