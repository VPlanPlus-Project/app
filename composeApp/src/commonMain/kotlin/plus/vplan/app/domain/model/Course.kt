package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Item

data class Course(
    val id: Int,
    val indiwareId: String?,
    val groups: List<Int>,
    val name: String,
    val teacher: Int?,
): Item {
    override fun getEntityId(): String = id.toString()

    companion object {
        fun fromIndiware(
            sp24SchoolId: String,
            name: String,
            teacher: Teacher?
        ) = "sp24.$sp24SchoolId.$name+${teacher?.name ?: ""}"
    }
}