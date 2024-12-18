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
            val disabledDefaultLessons = embeddedGroupProfile.disabledDefaultLesson.map { it.id }
            return Profile.StudentProfile(
                id = profile.id,
                displayName = profile.displayName,
                group = embeddedGroupProfile.group.toModel(),
                defaultLessons = embeddedGroupProfile.defaultLessons.associateWith {
                    disabledDefaultLessons.contains(it.defaultLesson.id)
                }.mapKeys { it.key.toModel() }
            )
        }
        if (embeddedTeacherProfile != null) {
            return Profile.TeacherProfile(
                id = profile.id,
                displayName = profile.displayName,
                teacher = embeddedTeacherProfile.teacher.toModel()
            )
        }
        if (embeddedRoomProfile != null) {
            return Profile.RoomProfile(
                id = profile.id,
                displayName = profile.displayName,
                room = embeddedRoomProfile.room.toModel()
            )
        }
        return null
    }
}