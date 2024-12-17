package plus.vplan.app.domain.model

data class Course(
    val id: String,
    val group: Group,
    val name: String,
    val teacher: Teacher?,
)
