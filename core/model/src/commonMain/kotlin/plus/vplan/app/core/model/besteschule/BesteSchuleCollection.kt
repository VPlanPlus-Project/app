package plus.vplan.app.core.model.besteschule

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class BesteSchuleCollection(
    val id: Int,
    val type: String,
    val name: String,
    val subjectId: Int,
    val givenAt: LocalDate,
    val intervalId: Int,
    val teacherId: Int,
    val cachedAt: Instant
)