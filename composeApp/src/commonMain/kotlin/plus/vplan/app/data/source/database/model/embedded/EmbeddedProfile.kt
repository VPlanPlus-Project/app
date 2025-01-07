package plus.vplan.app.data.source.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.data.source.database.model.database.DbGroupProfile
import plus.vplan.app.data.source.database.model.database.DbProfile
import plus.vplan.app.data.source.database.model.database.DbRoomProfile
import plus.vplan.app.data.source.database.model.database.DbTeacherProfile
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId

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
                customName = profile.displayName,
                group = Cacheable.Loaded(embeddedGroupProfile.group.toModel()),
                defaultLessons =
                    embeddedGroupProfile.defaultLessons
                        .associateWith { disabledDefaultLessons.contains(it.defaultLessonId).not() }
                        .mapKeys { Cacheable.Uninitialized(it.key.defaultLessonId) },
                vppId = run {
                    val model = embeddedGroupProfile?.vppId?.toModel() as? VppId.Active
                    @Suppress("UNCHECKED_CAST")
                    if (model == null) null
                    else Cacheable.Loaded(model) as Cacheable<VppId.Active>
                }
            )
        }
        if (embeddedTeacherProfile != null) {
            return Profile.TeacherProfile(
                id = profile.id,
                customName = profile.displayName,
                teacher = embeddedTeacherProfile.teacher.toModel()
            )
        }
        if (embeddedRoomProfile != null) {
            return Profile.RoomProfile(
                id = profile.id,
                customName = profile.displayName,
                room = embeddedRoomProfile.room.toModel()
            )
        }
        return null
    }
}