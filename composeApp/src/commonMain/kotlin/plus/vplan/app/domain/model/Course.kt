package plus.vplan.app.domain.model

data class Course(
    val id: String,
    val groups: List<Group>,
    val name: String,
    val teacher: Teacher?,
) {
    companion object {
        fun fromIndiware(
            sp24SchoolId: String,
            groups: List<Group>,
            name: String,
            teacher: Teacher?
        ) = Course(
            id = "sp24.$sp24SchoolId.$name+${teacher?.name ?: ""}",
            groups = groups,
            name = name,
            teacher = teacher
        )
    }
}