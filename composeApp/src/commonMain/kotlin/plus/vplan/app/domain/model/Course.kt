package plus.vplan.app.domain.model

data class Course(
    val id: String,
    val group: Group,
    val name: String,
    val teacher: Teacher?,
) {
    companion object {
        fun fromIndiware(
            sp24SchoolId: String,
            group: Group,
            name: String,
            teacher: Teacher?
        ) = Course(
            id = "sp24.$sp24SchoolId.$name+${teacher ?: ""}",
            group = group,
            name = name,
            teacher = teacher
        )
    }
}