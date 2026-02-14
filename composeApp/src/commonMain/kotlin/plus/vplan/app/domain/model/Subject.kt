package plus.vplan.app.domain.model

import plus.vplan.app.App
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.getFirstValue
import kotlin.uuid.Uuid

data class Subject(
    val id: Int,
    val name: String,
    val schoolId: Uuid
) {
    var school: School? = null
        private set

    suspend fun getSchoolItem(): School? {
        return school ?: App.schoolSource.getById(schoolId).getFirstValue()?.also { school = it }
    }
}