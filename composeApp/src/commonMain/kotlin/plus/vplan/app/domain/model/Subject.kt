package plus.vplan.app.domain.model

import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue

data class Subject(
    val id: Int,
    val name: String,
    val schoolId: Int
) {
    var school: School? = null
        private set

    suspend fun getSchoolItem(): School? {
        return school ?: App.schoolSource.getById(schoolId).getFirstValue()?.also { school = it }
    }
}

data class SubjectInstance(
    val id: Int,
    val subjectId: Int,
    val teacherId: Int?,
    val groupIds: List<Int>,
    val sp24Id: String?
) {
    var subject: Subject? = null
        private set

    var teacher: Teacher? = null
        private set

    var groups: List<Group>? = null
        private set

    suspend fun getSubjectItem(): Subject? {
        return this.subject ?: TODO()
    }

    suspend fun getTeacherItem(): Teacher? {
        if (this.teacherId == null) return null
        return this.teacher ?: App.teacherSource.getById(teacherId).getFirstValue().also { this.teacher = it }
    }

    suspend fun getGroupItems(): List<Group> {
        return this.groups ?: this.groupIds.mapNotNull { App.groupSource.getById(it).getFirstValue() }.also { this.groups = it }
    }
}