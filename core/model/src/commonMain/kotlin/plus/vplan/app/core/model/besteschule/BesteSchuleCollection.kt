package plus.vplan.app.core.model.besteschule

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class BesteSchuleCollection(
    val id: Int,
    val type: String,
    val name: String,
    val subject: BesteSchuleSubject,
    val givenAt: LocalDate,
    val interval: BesteSchuleInterval,
    val teacher: BesteSchuleTeacher,
    val cachedAt: Instant
)