package plus.vplan.app.domain.model

import plus.vplan.app.App
import plus.vplan.app.domain.cache.Item
import plus.vplan.app.domain.cache.getFirstValue

data class Group(
    val id: Int,
    val schoolId: Int,
    val name: String
) : Item {
    override fun getEntityId(): String = id.toString()

    var school: School? = null
        private set

    suspend fun getSchoolItem(): School {
        return school ?: App.schoolSource.getById(schoolId).getFirstValue()!!.also { school = it }
    }
}