package plus.vplan.app.domain.model

data class DefaultLesson(
    val id: String,
    val subject: String,
    val course: Course?,
    val teacher: Teacher?,
    val group: Group
)