package plus.vplan.app.core.model

import kotlin.uuid.Uuid

/**
 * Provides a handler for timetable data related to a week.
 * This is for Stundenplan24.de, which provides timetables on a
 * per-week basis. This model is being used to track whether a
 * timetable is available for a given week. For example, if a
 * week does not have a timetable and lays in the past, then
 * it will be skipped on a full-update.
 */
data class Timetable(
    val id: Uuid,
    val schoolId: Uuid,
    val weekId: String,
    val dataState: HasData
) {
    enum class HasData {
        Yes, No, Unknown
    }
}