package plus.vplan.app.core.database.model.embedded

import androidx.room.Embedded
import androidx.room.Relation
import plus.vplan.app.core.database.model.database.DbProfile
import plus.vplan.app.core.database.model.database.DbTeacher
import plus.vplan.app.core.database.model.database.DbTeacherProfile

data class EmbeddedTeacherProfile(
    @Embedded val profileLink: DbTeacherProfile,
    @Relation(
        parentColumn = "profile_id",
        entityColumn = "id",
        entity = DbProfile::class
    ) val profile: DbProfile,
    @Relation(
        parentColumn = "teacher_id",
        entityColumn = "id",
        entity = DbTeacher::class
    ) val teacher: EmbeddedTeacher
)
