package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

abstract class Profile {
    abstract val id: Uuid
    abstract val customName: String?
    abstract val school: School
    abstract val profileType: ProfileType
    abstract val displayName: String

    data class StudentProfile(
        override val id: Uuid,
        override val customName: String?,
        val group: Group,
        val defaultLessons: Map<DefaultLesson, Boolean>
    ) : Profile() {
        override val school = group.school
        override val profileType = ProfileType.STUDENT
        override val displayName = customName ?: group.name
    }

    data class TeacherProfile(
        override val id: Uuid,
        override val customName: String?,
        val teacher: Teacher
    ) : Profile() {
        override val school = teacher.school
        override val profileType = ProfileType.TEACHER
        override val displayName = customName ?: teacher.name
    }

    data class RoomProfile(
        override val id: Uuid,
        override val customName: String?,
        val room: Room
    ) : Profile() {
        override val school = room.school
        override val profileType = ProfileType.ROOM
        override val displayName = customName ?: room.name
    }
}

enum class ProfileType {
    STUDENT, TEACHER, ROOM
}