package plus.vplan.app.core.database.model.embedded.besteschule

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleGrade
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade

data class EmbeddedBesteSchuleGrade(
    @Embedded val grade: DbBesteSchuleGrade,
    @Relation(
        parentColumn = "collection_id",
        entityColumn = "id",
        entity = DbBesteSchuleCollection::class
    )
    val collection: EmbeddedBesteSchuleCollection,
) {
    fun toModel() = BesteSchuleGrade(
        id = this.grade.id,
        value = this.grade.value,
        isOptional = this.grade.isOptional,
        isSelectedForFinalGrade = this.grade.isSelectedForFinalGrade,
        schulverwalterUserId = this.grade.schulverwalterUserId,
        collection = this.collection.toModel(),
        givenAt = this.grade.givenAt,
        cachedAt = this.grade.cachedAt,
    )
}