package plus.vplan.app.domain.model.besteschule

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class BesteSchuleYear(
    val id: Int,
    val name: String,
    val from: LocalDate,
    val to: LocalDate,
    val cachedAt: Instant
)