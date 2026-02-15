package plus.vplan.app.core.model

import kotlin.uuid.Uuid

abstract class Profile : Item<Uuid, DataTag> {
    abstract val profileType: ProfileType
    abstract val name: String

    override val tags: Set<DataTag> = emptySet()
    abstract val school: School.AppSchool

    data class StudentProfile(
        override val id: Uuid,
        override val name: String,
        val group: Group,
        val subjectInstanceConfiguration: Map<Uuid, Boolean>,
        val vppId: VppId.Active?
    ) : Profile() {
        override val profileType = ProfileType.STUDENT
        override val school: School.AppSchool = group.school
    }

    data class TeacherProfile(
        override val id: Uuid,
        override val name: String,
        val teacher: Teacher
    ) : Profile() {
        override val profileType = ProfileType.TEACHER
        override val school: School.AppSchool = teacher.school
    }
}

enum class ProfileType {
    STUDENT, TEACHER
}