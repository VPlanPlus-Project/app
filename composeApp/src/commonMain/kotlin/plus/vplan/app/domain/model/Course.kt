package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Item

data class Course(
    val id: String,
    val groups: List<Int>,
    val name: String,
    val teacher: Int?,
): Item {
    override fun getEntityId(): String = id

    companion object {
        fun fromIndiware(
            sp24SchoolId: String,
            groups: List<Int>,
            name: String,
            teacher: Teacher?
        ) = Course(
            id = "sp24.$sp24SchoolId.$name+${teacher?.name ?: ""}",
            groups = groups,
            name = name,
            teacher = teacher?.id
        )
    }
}