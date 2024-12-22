package plus.vplan.app.domain.model

/**
 * @param id The id of the default lesson. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware group name and the default lesson number separated with a dot, e.g. `sp24.6c.146`
 */
data class DefaultLesson(
    val id: String,
    val subject: String,
    val course: Course?,
    val teacher: Teacher?,
    val group: Group
) {
    constructor(
        indiwareDefaultLessonId: String,
        subject: String,
        group: Group,
        teacher: Teacher?,
        course: Course?
    ) : this(
        id = "sp24.${group.name}.$indiwareDefaultLessonId",
        subject = subject,
        course = course,
        teacher = teacher,
        group = group
    )
}

fun Collection<DefaultLesson>.findByIndiwareId(indiwareId: String): DefaultLesson? {
    return firstOrNull { it.id.matches(Regex("^sp24\\..*\\.$indiwareId\$")) }
}