package plus.vplan.app.domain.model

import plus.vplan.app.domain.repository.Stundenplan24Repository
import kotlin.uuid.Uuid

data class Timetable(
    val id: Uuid,
    val schoolId: Uuid,
    val weekId: String,
    val dataState: Stundenplan24Repository.HasData
)