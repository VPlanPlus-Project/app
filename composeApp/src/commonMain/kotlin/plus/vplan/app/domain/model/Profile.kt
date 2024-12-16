package plus.vplan.app.domain.model

import kotlin.uuid.Uuid

abstract class Profile {
    abstract val id: Uuid
    abstract val displayName: String?

    data class StudentProfile(
        override val id: Uuid,
        override val displayName: String?,
        val group: Group
    ) : Profile()

    data class TeacherProfile(
        override val id: Uuid,
        override val displayName: String?,
        val teacher: Teacher
    ) : Profile()

    data class RoomProfile(
        override val id: Uuid,
        override val displayName: String?,
        val room: Room
    ) : Profile()
}

enum class ProfileType {
    STUDENT, TEACHER, ROOM
}