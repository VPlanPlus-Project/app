package plus.vplan.app.domain.model

import plus.vplan.app.domain.cache.Item

/**
 * @param getEntityId The id of the default lesson. If it originates from indiware, it will be prefixed with `sp24.` followed by the indiware group name and the default lesson number separated with a dot, e.g. `sp24.6c.146`
 */
data class DefaultLesson(
    val id: String,
    val subject: String,
    val course: String?,
    val teacher: Int?,
    val groups: List<Int>
) : Item {
    override fun getEntityId(): String = this.id
}

fun Collection<DefaultLesson>.findByIndiwareId(indiwareId: String): DefaultLesson? {
    return firstOrNull { it.id.matches(Regex("^sp24\\..*\\.$indiwareId\$")) }
}