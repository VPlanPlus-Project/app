package plus.vplan.app.core.model.besteschule

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class BesteSchuleGrade(
    val id: Int,
    val value: String?,
    val isOptional: Boolean,
    val isSelectedForFinalGrade: Boolean,
    val schulverwalterUserId: Int,
    val collectionId: Int,
    val givenAt: LocalDate,
    val cachedAt: Instant
) {
    val numericValue: Int?
        get() {
            return this.value?.replace("(", "")?.replace(")", "")?.replace("+", "")?.replace("-", "")?.toIntOrNull()
        }
}