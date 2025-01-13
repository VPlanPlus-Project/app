package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.domain.model.Profile

data class EmbeddedProfile(
    @Embedded val profile: DbProfile,
    @Relation(
        entity = DbGroupProfile::class,
        parentColumn = "id",
        entityColumn = "profile_id"
    ) val embeddedGroupProfile: EmbeddedGroupProfile?,
    @Relation(
        entity = DbTeacherProfile::class,
        parentColumn = "id",
        entityColumn = "teacher_id"
    ) val embeddedTeacherProfile: EmbeddedTeacherProfile?,
    @Relation(
        entity = DbRoomProfile::class,
        parentColumn = "id",
        entityColumn = "profile_id"
    ) val embeddedRoomProfile: EmbeddedRoomProfile?
) {
    fun toModel(): Profile? {
        if (embeddedGroupProfile != null) {
            val disabledDefaultLessons = embeddedGroupProfile.disabledDefaultLesson.map { it.defaultLessonId }
            return Profile.StudentProfile(
                id = profile.id,
                name = profile.displayName ?: embeddedGroupProfile.group.group.name,
                group = embeddedGroupProfile.group.group.id,
                defaultLessons =
                    embeddedGroupProfile.defaultLessons
                        .associateWith { disabledDefaultLessons.contains(it.defaultLessonId).not() }
                        .mapKeys { it.key.defaultLessonId },
                vppId = embeddedGroupProfile.vppId?.vppId?.id
            )
        }
        if (embeddedTeacherProfile != null) {
            return Profile.TeacherProfile(
                id = profile.id,
                name = profile.displayName ?: embeddedTeacherProfile.teacher.name,
                teacher = embeddedTeacherProfile.teacher.id
            )
        }
        if (embeddedRoomProfile != null) {
            return Profile.RoomProfile(
                id = profile.id,
                name = profile.displayName ?: embeddedRoomProfile.room.name,
                room = embeddedRoomProfile.room.id
            )
        }
        return null
    }
}