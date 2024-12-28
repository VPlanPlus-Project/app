package plus.vplan.app.domain.model

import kotlinx.datetime.LocalDate

data class Day(
    val id: String,
    val date: LocalDate,
    val school: School,
    val week: Week,
    val info: String?
) {
    constructor(
        date: LocalDate,
        school: School,
        week: Week,
        info: String?
    ) : this("${school.id}/$date", date, school, week, info)
}

sealed interface SchoolDay{
    val id: String
    val date: LocalDate
    val school: School
    val nextRegularSchoolDay: LocalDate?

    data class NormalDay(
        override val id: String,
        override val date: LocalDate,
        override val school: School,
        override val nextRegularSchoolDay: LocalDate?,
        val week: Week,
        val info: String?,
        val lessons: List<Lesson>
    ) : SchoolDay

    data class Unknown(
        override val date: LocalDate,
        override val school: School
    ) : SchoolDay {
        override val id: String = "no_id"
        override val nextRegularSchoolDay: LocalDate? = null
    }

    data class Holiday(
        override val id: String,
        override val date: LocalDate,
        override val school: School,
        override val nextRegularSchoolDay: LocalDate?
    ) : SchoolDay

    data class Weekend(
        override val id: String,
        override val date: LocalDate,
        override val school: School,
        override val nextRegularSchoolDay: LocalDate?
    ) : SchoolDay
}